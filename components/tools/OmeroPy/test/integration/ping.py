#!/usr/bin/env python

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest

PINGFILE = """
#<script>
#   <name>
#       ping
#   </name>
#   <description>
#       Simple ping
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
#               crap.
#           </description>
#       </variable>
#   </return>
#</script>
#!/usr/bin/env python

print "Printing to stdout"

import omero, omero.scripts as s
client = s.client("ping.py", "simple ping script", s.Long("a").inout(), s.String("b").inout())
client.createSession()
print "Session"
print client.getSession()
keys = client.getInputKeys()
print "Keys found:"
print keys
for key in keys:
    client.setOutput(key, client.getInput(key))

"""

class TestPing(lib.ITest):

    def testUploadAndPing(self):
        pingfile = tempfile.NamedTemporaryFile(mode='w+t')
        try:
            pingfile.write(PINGFILE)
            pingfile.flush()
            file = self.root.upload(pingfile.name, type="text/x-python")
            j = omero.model.ScriptJobI()
            j.linkOriginalFile(file)

            p = self.client.sf.acquireProcessor(j, 100)
            input = omero.RMap({})
            input.val["a"] = omero.RInt(1)
            input.val["b"] = omero.RString("c")
            process = p.execute(input)
            process.wait()
            output = p.getResults(process)
            self.assert_( 1 == output.val["a"].val )
        finally:
            pingfile.close()

    def testPingViaISCript(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadScript(PINGFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(omero.RLong(id),False))
        p = self.client.sf.acquireProcessor(j, 100)
        input = omero.RMap({})
        input.val["a"] = omero.RInt(2)
        input.val["b"] = omero.RString("d")
        process = p.execute(input)
        process.wait()
        output = p.getResults(process)
        self.assert_( 2 == output.val["a"].val )

    def testPingParametersViaISCript(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadScript(PINGFILE)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(omero.RLong(id),False))
        p = self.client.sf.acquireProcessor(j, 100)
        params = p.params()
        self.assert_( params )
        self.assert_( params.inputs["a"] )
        self.assert_( params.inputs["b"] )
        self.assert_( params.outputs["a"] )
        self.assert_( params.outputs["b"] )

if __name__ == '__main__':
    unittest.main()
