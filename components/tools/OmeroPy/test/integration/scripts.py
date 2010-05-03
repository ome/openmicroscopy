#!/usr/bin/env python

"""
   Integration test focused running interactive scripts.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import tempfile
import omero
import omero.all
from omero_model_ScriptJobI import ScriptJobI
import omero.scripts
from omero.rtypes import *

thumbnailFigurePath = "scripts/omero/figure_scripts/thumbnailFigure.py"

class TestScripts(lib.ITest):

    def testBasicUsage(self):
        job = ScriptJobI()
        proc = self.client.sf.acquireProcessor(job, 20)
        #proc.

    def testTicket1036(self):
        self.client.setInput("a", rstring("a"));
        self.client.getInput("a");

    def testUploadAndPing(self):
        pingfile = tempfile.NamedTemporaryFile(mode='w+t')
        pingfile.close();
        if True:
            name = pingfile.name
            pingfile = open(name, "w")
            pingfile.write("PINGFILE")
            pingfile.flush()
            pingfile.close()
            file = self.root.upload(name, type="text/x-python", permissions = PUBLIC)
            j = omero.model.ScriptJobI()
            j.linkOriginalFile(file)

            p = self.client.sf.sharedResources().acquireProcessor(j, 100)
            jp = p.params()
            self.assert_(jp, "Non-zero params")

    def testParseErrorTicket2185(self):
        svc = self.root.sf.getScriptService()
        try:
            script_id = svc.uploadScript('testpath', "THIS STINKS")
            svc.getParams(script_id)
        except omero.ValidationException, ve:
            self.assertTrue("THIS STINKS" in str(ve))

    def testUploadOfficalScript(self):
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
        
        print script
        
        scriptId = scriptService.uploadScript("path", script)
        map = {"message": omero.rtypes.rstring("Sending this message to the server!"), }  
        argMap = omero.rtypes.rmap(map)

        proc = scriptService.runScript(scriptId, map, None)
        try:
            cb = omero.scripts.ProcessCallbackI(client, proc)
            while not cb.block(1000): # ms.
                pass
            cb.close()
            results = proc.getResults(0)    # ms
        finally:
            proc.close(False)
            
        self.assertFalse("returnMessage" in results, "Script should not have run. No user processor!")

if __name__ == '__main__':
    unittest.main()
