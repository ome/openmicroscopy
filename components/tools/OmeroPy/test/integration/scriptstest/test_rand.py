#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero
import omero.processor
import omero.scripts
from omero.rtypes import *

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
import random

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
val = random.randint(x,y);
print val

"""

class TestRand(lib.ITest):

    def testRand(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadScript("/tests/rand_py/%s.py" % self.uuid(), SENDFILE)
        input = {"x":rlong(3), "y":rlong(3)}
        impl = omero.processor.usermode_processor(self.root)
        try:
            process = scripts.runScript(id, input, None)
            cb = omero.scripts.ProcessCallbackI(self.root, process)
            cb.block(2000) # ms
            cb.close()
            try:
                output = process.getResults(0)
                self.assert_( output["x"].val == 3)
            except KeyError:
                print "Key is not in returned dictionary. Is this a fail?"
        finally:
            impl.cleanup()
