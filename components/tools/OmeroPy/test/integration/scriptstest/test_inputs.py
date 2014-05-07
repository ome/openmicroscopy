#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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
    Specifically test the parseInputs functionality
    which all scripts might want to use.
"""

import test.integration.library as lib
import omero
import omero.processor
import omero.scripts
from omero.rtypes import rint

SENDFILE = """
#!/usr/bin/env python

# Setup to run as an integration test
rundir = "%s"
import os
import sys
sys.path.insert(0, rundir)
sys.path.insert(0, os.path.join(rundir, "target"))

import omero.scripts as s
import omero.util.script_utils as su

client = s.client(
    "test_inputs.py")

for method, inputs in (
        ('arg', client.getInputs(True)),
        ('kw', client.getInputs(unwrap=True)),
        ('util', su.parseInputs(client))
    ):

    a = inputs["a"]
    if not isinstance(a, (int, long)):
        raise Exception("Failed!")
"""


class TestInputs(lib.ITest):

    def testInputs(self):
        import logging
        logging.basicConfig(level=10)
        scripts = self.root.getSession().getScriptService()
        sendfile = SENDFILE % self.omeropydir()
        id = scripts.uploadScript(
            "/tests/inputs_py/%s.py" % self.uuid(), sendfile)
        input = {"a": rint(100)}
        impl = omero.processor.usermode_processor(self.root)
        try:
            process = scripts.runScript(id, input, None)
            cb = omero.scripts.ProcessCallbackI(self.root, process)
            try:
                count = 100
                while cb.block(500):
                    count -= 1
                    assert count != 0
                rc = process.poll()
                assert rc is not None
                assert rc.val == 0
            finally:
                cb.close()
        finally:
            impl.cleanup()
