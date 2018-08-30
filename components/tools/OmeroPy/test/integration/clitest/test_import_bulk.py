# -*- coding: utf-8 -*-

#
# Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

plugin = __import__('omero.plugins.import', globals(), locals(),
                    ['ImportControl'], -1)
ImportControl = plugin.ImportControl

from omero.cli import NonZeroReturnCode
from omero.testlib.cli import CLITest
from omero.plugins.sessions import SessionsControl
import sys
import re
import subprocess


class TestImportBulk(CLITest):

    def setup_method(self, method):
        super(TestImportBulk, self).setup_method(method)
        self.cli.register("sessions", SessionsControl, "TEST")
        self.cli.register("import", plugin.ImportControl, "TEST")

# These methods should be refactored up a level

    def do_import(self, capfd, strip_logs=True):
        try:

            # Discard previous out/err
            # left over from previous test.
            capfd.readouterr()

            self.cli.invoke(self.args, strict=True)
            o, e = capfd.readouterr()
            if strip_logs:
                clean_o = ""
                for line in o.splitlines(True):
                    if not (re.search(r'^\d\d:\d\d:\d\d.*', line)
                            or re.search(r'.*\w\.\w.*', line)):
                        clean_o += line
                o = clean_o
        except NonZeroReturnCode:
            o, e = capfd.readouterr()
            print "O" * 40
            print o
            print "E" * 40
            print e
            raise
        return o, e

    def get_object(self, err, obj_type, query=None):
        """Retrieve the created object by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<id>\d+)$' % obj_type)
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                break
        obj_id = int(match.group('id'))
        return self.assert_object(obj_type, obj_id, query=query)

    def assert_object(self, obj_type, obj_id, query=None):
        if not query:
            query = self.query
        obj = query.get(obj_type, obj_id,
                        {"omero.group": "-1"})
        assert obj
        assert obj.id.val == obj_id
        return obj

    def get_objects(self, err, obj_type, query=None):
        """Retrieve the created objects by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<idstring>\d+)$' % obj_type)
        objs = []
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                ids = match.group('idstring').split(',')
                for obj_id in ids:
                    obj = self.assert_object(obj_type,
                                             int(obj_id), query=query)
                    objs.append(obj)
        return objs

# TESTS

    def testBulk(self, tmpdir, capfd, monkeypatch):
        """Test Bulk import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        yml = tmpdir.join("test.yml")
        yml.write("""---
dry_run: "script%s.sh"
path: test.tsv
        """)

        tsv = tmpdir.join("test.tsv")
        tsv.write("test.fake")

        script = tmpdir.join("script1.sh")

        self.args += ["import", "-f", "--bulk", str(yml),
                      "--clientdir", self.omero_dist / "lib" / "client"]

        bin = self.omero_dist / "bin" / "omero"
        monkeypatch.setattr(sys, "argv", [str(bin)])
        out, err = self.do_import(capfd)

        # At this point, script1.sh has been created
        assert script.exists()
        print script.read()

        # But we need to login and then run the script
        monkeypatch.setenv("OMERO_SESSIONDIR", tmpdir)
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["sessions", "login"]
        self.args += ["-s", host, "-p", port]
        self.args += ["-u", self.user.omeName.val]
        self.args += ["-w", self.user.omeName.val]
        self.cli.invoke(self.args, strict=True)
        popen = subprocess.Popen(
            ["bash", str(script)],
            cwd=str(tmpdir),
            stdout=subprocess.PIPE)
        out = popen.communicate()[0]
        rcode = popen.poll()
        assert rcode == 0
        assert self.get_object(out, 'Image')
