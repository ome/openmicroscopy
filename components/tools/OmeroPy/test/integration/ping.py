#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IPojos interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest

PINGFILE = """
#!/usr/bin/env python
client = omero.client()
env = client.getInputEnvironment()
for key in env.keys():
    client.setOutput(key, client.getInput(key))
"""

class TestPing(lib.ITest):

    def testUploadAndPing(self):
        pingfile = tempfile.NamedTemporaryFile(mode='w+t')
        try:
            pingfile.write(PINGFILE)
            file = self.root.upload(pingfile.name, type="text/x-python")
            j = omero.model.ScriptJobI()
            j.linkOriginalFile(file)

            p = self.client.sf.acquireProcessor(j, 100)
            input = omero.RMap({})
            input.val["a"] = omero.RInt(1)
            process = p.execute(input)
            process.wait()
            output = p.getResults(process)
            self.assert_( 1 == output["a"].val )
        finally:
            pingfile.close()

if __name__ == '__main__':
    unittest.main()
