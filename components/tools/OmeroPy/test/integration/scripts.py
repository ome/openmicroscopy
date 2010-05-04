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

import omero.util.concurrency
import omero.processor
import omero.scripts
import omero.cli

from omero.rtypes import *

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
        file = open(thumbnailFigurePath)
        script = file.read()
        file.close()
        id = scriptService.uploadOfficialScript(thumbnailFigurePath, script)
        # force the server to parse the file enough to get params (checks syntax etc)
        params = scriptService.getParams(id)

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
        
if __name__ == '__main__':
    unittest.main()
