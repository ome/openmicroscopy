#!/usr/bin/env python

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest

SENDFILE = """
#<script>
#   <name>
#       rand
#   </name>
#   <description>
#	get rand numbers from matlab instance.
#   </description>
#   <parameters>
#       <variable name="inputParam" type="type" optional="true">
#           <description>
#               This variable can have a name, type and be optional.
#           </description>
#       </variable>
#   </parameters>
#   <return>
#       <variable name="outputParam" type="type">
#           <description>
#           </description>
#       </variable>
#   </return>
#</script>
#!/usr/bin/env python

import omero, omero.scripts as s
from mlabwrap import mlab;  
client = s.client("rand.py", "get Random", s.Long("x").inout(), s.Long("y").inout())
client.createSession()
print "Session"
print client.getSession()
keys = client.getInputKeys()
print "Keys found:"
print keys
for key in keys:
    client.setOutput(key, client.getInput(key))

x = client.getInput("x").val
y  = client.getInput("y").val
val = mlab.rand(x,y);
print val

"""

class TestPing(lib.ITest):

    def testPingViaISCript(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadScript(SENDFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(omero.RLong(id),False))
        p = self.client.sf.acquireProcessor(j, 100)
        input = omero.RMap({})
        input = omero.RMap({"x":omero.RLong(3), "y":omero.RLong(3)})
        
        process = p.execute(input)
        process.wait()
        output = p.getResults(process)
        print output
        self.assert_( output.val["x"].val == 3)
        
if __name__ == '__main__':
    unittest.main()
