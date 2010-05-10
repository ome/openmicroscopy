#!/usr/bin/env python

"""
   Integration test focused running interactive scripts.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import unittest
import integration.library as lib
import tempfile
import omero
import omero.all

import omero.processor
import omero.scripts
import omero.cli

from omero.rtypes import *
from omero.util.temp_files import create_path

PUBLIC = omero.model.PermissionsI("rwrwrw")

thumbnailFigurePath = "scripts/omero/figure_scripts/thumbnailFigure.py"

if "DEBUG" in os.environ:
    omero.util.configure_logging(loglevel=10)

class TestScripts(lib.ITest):

    def testBasicUsage(self):
        svc = self.client.sf.getScriptService()
        return svc

    def testTicket1036(self):
        self.client.setInput("a", rstring("a"));
        self.client.getInput("a");

    def testUploadAndPing(self):
        pingfile = tempfile.NamedTemporaryFile(mode='w+t')
        pingfile.close();
        name = pingfile.name
        pingfile = open(name, "w")
        pingfile.write("""if True:
        import omero
        import omero.scripts as OS
        import omero.grid as OG
        OS.client("ping")
        """)
        pingfile.flush()
        pingfile.close()
        file = self.client.upload(name, type="text/x-python")

        impl = omero.processor.usermode_processor(self.client)
        try:
            svc = self.client.sf.getScriptService()
            jp = svc.getParams(file.id.val)
            self.assert_(jp, "Non-zero params")
        finally:
            impl.cleanup()

    def testParseErrorTicket2185(self):
        svc = self.root.sf.getScriptService()
        impl = omero.processor.usermode_processor(self.root)
        try:
            try:
                script_id = svc.uploadScript('testpath', "THIS STINKS")
                svc.getParams(script_id)
            except omero.ValidationException, ve:
                self.assertTrue("THIS STINKS" in str(ve))
        finally:
            impl.cleanup()


    def testUploadOfficialScript(self):
        scriptService = self.root.sf.getScriptService()
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        
        scriptLines = [
        "import omero",
        "from omero.rtypes import rstring, rlong",
        "import omero.scripts as scripts",
        "if __name__ == '__main__':",
        "    client = scripts.client('HelloWorld.py', 'Hello World example script',",
        "    scripts.Int('longParam', True, description='theDesc', min=rlong(1), max=rlong(10), values=[rlong(5)]) )",
        "    client.setOutput('returnMessage', rstring('Script ran OK!'))"]
        script = "\n".join(scriptLines)

        id = scriptService.uploadOfficialScript("/testUploadOfficialScript%s.py" % uuid, script)
        # force the server to parse the file enough to get params (checks syntax etc)
        params = scriptService.getParams(id)
        for key, param in params.inputs.items():
            #print "description", param.description
            #print "prototype", param.prototype
            #print "min", param.min.getValue()
            #print "max", param.max.getValue()
            #print "values", param.values.getValue()
            self.assertEquals("longParam", key)
            self.assertNotEqual(param.prototype, None, "Parameter prototype is 'None'")
            self.assertEquals("theDesc", param.description)
            self.assertEquals(1, param.min.getValue(), "Min value not correct")
            self.assertEquals(10, param.max.getValue(), "Max value not correct")
            self.assertEquals(5, param.values.getValue()[0].getValue(), "First option value not correct")
            

    def testRunScript(self):
        # Trying to run script as described:
        #http://trac.openmicroscopy.org.uk/omero/browser/trunk/components/blitz/resources/omero/api/IScript.ice#L40
        scriptService = self.root.sf.getScriptService()
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        client = self.root

        scriptLines = [
        "import omero",
        "from omero.rtypes import rstring",
        "import omero.scripts as scripts",
        "if __name__ == '__main__':",
        "    client = scripts.client('HelloWorld.py', 'Hello World example script',",
        "    scripts.String('message', optional=True))",
        "    client.setOutput('returnMessage', rstring('Script ran OK!'))"]
        script = "\n".join(scriptLines)
        map = {"message": omero.rtypes.rstring("Sending this message to the server!"), }

        # should fail if we try to upload as 'user' script and run (no user processor)
        userScriptId = scriptService.uploadScript("user/test/script.py", script)
        results = {}
        try:
            proc = scriptService.runScript(userScriptId, map, None)
            try:
                cb = omero.scripts.ProcessCallbackI(client, proc)
                while not cb.block(1000): # ms.
                    pass
                cb.close()
                results = proc.getResults(0)    # ms
            finally:
                proc.close(False)
            self.fail("ticket:2309 - should not run without processor")
        except:
            pass

        self.assertFalse("returnMessage" in results, "Script should not have run. No user processor!")
        
        
        # should be OK for root to upload as official script (unique path) and run
        officialScriptId = scriptService.uploadOfficialScript("offical/test/script%s.py" % uuid, script)
        proc = scriptService.runScript(officialScriptId, map, None)
        try:
            cb = omero.scripts.ProcessCallbackI(client, proc)
            while not cb.block(1000): # ms.
                pass
            cb.close()
            results = proc.getResults(0)    # ms
        finally:
            proc.close(False)

        self.assertTrue("returnMessage" in results, "Script should have run as Official script")
        
    def testEditScript(self):
        scriptService = self.root.sf.getScriptService()
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid

        scriptLines = [
        "import omero",
        "from omero.rtypes import rstring",
        "import omero.scripts as scripts",
        "if __name__ == '__main__':",
        "    client = scripts.client('HelloWorld.py', 'Hello World example script',",
        "    scripts.String('message', optional=True))",
        "    client.setOutput('returnMessage', rstring('Script ran OK!'))"]
        script = "\n".join(scriptLines)
        map = {"message": omero.rtypes.rstring("Sending this message to the server!"), }
        
        scriptPath = "/test/edit/script%s.py" % uuid
        scriptId = scriptService.uploadOfficialScript(scriptPath, script)
        
        scripts = scriptService.getScripts()
        namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
        scriptFile = namedScripts[0]
        
        scriptService.editScript(scriptFile, script)
        
        
    def testScriptValidation(self):
        scriptService = self.root.sf.getScriptService()
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        
        scriptService = self.root.sf.getScriptService()
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid

        invalidScript = "This text is not valid as a script"
        
        invalidPath = "/test/validation/invalid%s.py" % uuid
        
        invalidUpload = False
        try:
            # this should throw, since the script is invalid
            invalidId = scriptService.uploadOfficialScript(invalidPath, invalidScript)
            invalidUpload = True
        except: pass
        self.assertFalse(invalidUpload, "uploadOfficialScript() uploaded invalid script")
        
        # upload a valid script - then edit
        scriptLines = [
        "import omero",
        "from omero.rtypes import rstring",
        "import omero.scripts as scripts",
        "if __name__ == '__main__':",
        "    client = scripts.client('HelloWorld.py', 'Hello World example script',",
        "    scripts.String('message', optional=True))",
        "    client.setOutput('returnMessage', rstring('Script ran OK!'))"]
        validScript = "\n".join(scriptLines)
        validPath = "/test/validation/valid%s.py" % uuid
        validId = scriptService.uploadOfficialScript(validPath, validScript)
        
        scripts = scriptService.getScripts()
        namedScripts = [s for s in scripts if s.path.val + s.name.val == validPath]
        scriptFile = namedScripts[0]
        
        invalidEdit = False
        try:
            # this should throw, since the script is invalid
            scriptService.editScript(scriptFile, invalidScript)
            invalidEdit = True
        except: pass
        self.assertFalse(invalidEdit, "editScript() failed to throw with invalid script")

    def testAutoFillTicket2326(self):
        SCRIPT = """if True:
        import omero.scripts
        import omero.rtypes
        client = omero.scripts.client("ticket2326", omero.scripts.Long("width", optional=True))
        width = client.getInput("width")
        print width
        client.setOutput("noWidthKey", omero.rtypes.rbool("width" not in client.getInputKeys()))
        client.setOutput("widthIsNull", omero.rtypes.rbool(width is None))
        """
        impl = omero.processor.usermode_processor(self.client)
        svc = self.client.sf.getScriptService()
        try:
            scriptID = svc.uploadScript("/test/testAutoFillTicket2326", SCRIPT)
            process = svc.runScript(scriptID, {}, None)
            cb = omero.scripts.ProcessCallbackI(self.client, process)
            while cb.block(500) is None:
                pass
            results = process.getResults(0)
            stdout = results["stdout"].val
            downloaded = create_path()
            self.client.download(ofile=stdout, filename=str(downloaded))
            text = downloaded.text().strip()
            self.assertEquals("None", text)
            self.assertTrue(results["widthIsNull"].val)
            self.assertTrue(results["noWidthKey"].val)
            self.assertTrue("stderr" not in results)
        finally:
            impl.cleanup()


if __name__ == '__main__':
    unittest.main()
