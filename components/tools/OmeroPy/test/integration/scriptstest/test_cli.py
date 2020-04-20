#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
   Integration test of the use of omero.cli.CLI from within a script.

"""

from builtins import str
from omero.testlib import ITest
import os

import omero
import omero.clients
import omero.model
import omero.api

from omero.util.temp_files import create_path, remove_path
from omero.rtypes import rlong, rmap
from omero.scripts import wait

SCRIPT = """
#!/usr/bin/env python

import os
import omero, omero.scripts as s
import uuid

#
# Unique name so that IScript does not reject us
# based on duplicate file names.
#
uuid = str(uuid.uuid4())
print("I am the script named %s" % uuid)

#
# Creation
#
client = s.client(uuid, "simple CLI script")
print("Session", client.getSession())


#
# Basic check of login
#
import omero.cli as cli
c = cli.CLI()
c.loadplugins()
c._client = client.createClient(secure = True)  # TODO: setter?
c.onecmd(["login"])
c.assertRC()

#
# Try an import
#
with open("a.fake", "a"):
    pass
c.onecmd(["import", "a.fake"])
c.assertRC()
"""


class TestCLI(ITest):

    """
    Tests which use the trivial script defined by CLIFILE to
    test the usage of omero.cli.CLI from a script.
    """

    #
    # Helper methods
    #

    def _getProcessor(self):
        scripts = self.root.getSession().getScriptService()
        id = scripts.uploadOfficialScript(
            "/tests/cli/%s.py" % self.uuid(), SCRIPT)
        j = omero.model.ScriptJobI()
        j.linkOriginalFile(omero.model.OriginalFileI(rlong(id), False))
        p = self.client.sf.sharedResources().acquireProcessor(j, 100)
        return p

    def _checkstd(self, output, which):
        rfile = output.val[which]
        ofile = rfile.val
        assert ofile

        tmppath = create_path("clitest")
        try:
            self.client.download(ofile, str(tmppath))
            assert os.path.getsize(str(tmppath))
            return tmppath.text()
        finally:
            remove_path(tmppath)

    def assertIO(self, output):
        stdout = self._checkstd(output, "stdout")
        stderr = self._checkstd(output, "stderr")
        return stdout, stderr

    def assertSuccess(self, processor, process):
        wait(self.client, process)
        rc = process.poll()
        output = processor.getResults(process)
        stdout, stderr = self.assertIO(output)
        if rc is None or rc.val != 0:
            assert False, "STDOUT:\n%s\nSTDERR:\n%s\n" % (stdout, stderr)
        return output

    #
    # Test methods
    #

    def testCLI(self):
        p = self._getProcessor()
        input = rmap({})
        process = p.execute(input)
        stdout, stderr = self.assertIO(self.assertSuccess(p, process))
        print("STDOUT:\n%s\nSTDERR:\n%s\n" % (stdout, stderr))
