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

from omero.testlib import ITest
import omero
import omero.processor
import omero.scripts
from omero.rtypes import rint

SENDFILE = """
#!/usr/bin/env python

# Setup to run as an integration test
import os
import sys

import omero.scripts as s
import omero.util.script_utils as su


client = s.client("test_inputs.py",
    s.Int("a"),
    s.String("b", default="c"),
    s.String("d"),
)

for method, inputs in (
        ('arg', client.getInputs(True)),
        ('kw', client.getInputs(unwrap=True)),
        ('util', su.parseInputs(client))
    ):

    # The params object contains all the metadata
    # passed to the constructor above.
    defined = client.params.inputs.keys()
    if set(["a", "b", "d"]) != set(defined):
        raise Exception("Failed!")

    a = inputs["a"]
    if not isinstance(a, (int, long)):
        raise Exception("Failed!")

    b = inputs.get("b")
    if b != "c":
        raise Exception("Failed!")

    d = inputs.get("d")
    if d is not None:
        raise Exception("Failed!")

"""


class TestInputs(ITest):

    def output(self, root, results, which):
        out = results.get(which, None)
        if out:
            rfs = root.sf.createRawFileStore()
            try:
                rfs.setFileId(out.val.id.val)
                text = rfs.read(0, rfs.size())
                if text.strip():
                    print "===", which, "==="
                    print text
            finally:
                rfs.close()

    def testInputs(self):
        import logging
        logging.basicConfig(level=10)
        root_client = self.new_client(system=True)
        scripts = root_client.sf.getScriptService()
        id = scripts.uploadScript(
            "/tests/inputs_py/%s.py" % self.uuid(), SENDFILE)
        input = {"a": rint(100)}
        impl = omero.processor.usermode_processor(root_client)
        try:
            process = scripts.runScript(id, input, None)
            cb = omero.scripts.ProcessCallbackI(root_client, process)
            try:
                count = 100
                while cb.block(2000):
                    count -= 1
                    assert count != 0
                rc = process.poll()
                results = process.getResults(0)
                self.output(root_client, results, "stdout")
                self.output(root_client, results, "stderr")
                assert rc is not None
                assert rc.val == 0
            finally:
                cb.close()
        finally:
            impl.cleanup()
