#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Integration test testing distributed processing via
   ServiceFactoryI.acquireProcessor().

"""

import library as lib
import omero
import omero.processor
import omero.scripts
from omero.rtypes import rlong

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

client = s.client(
    "rand.py", "get Random", s.Long("x").inout(), s.Long("y").inout())
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
        root_client = self.new_client(system=True)
        scripts = root_client.sf.getScriptService()
        id = scripts.uploadScript(
            "/tests/rand_py/%s.py" % self.uuid(), SENDFILE)
        input = {"x": rlong(3), "y": rlong(3)}
        impl = omero.processor.usermode_processor(root_client)
        try:
            process = scripts.runScript(id, input, None)
            cb = omero.scripts.ProcessCallbackI(root_client, process)
            cb.block(2000)  # ms
            cb.close()
            try:
                output = process.getResults(0)
                assert output["x"].val == 3
            except KeyError:
                print "Key is not in returned dictionary. Is this a fail?"
        finally:
            impl.cleanup()
