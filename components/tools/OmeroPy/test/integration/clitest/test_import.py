#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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
from test.integration.clitest.cli import CLITest
import pytest
import re


class TestImport(CLITest):

    def setup_method(self, method):
        super(TestImport, self).setup_method(method)
        self.cli.register("import", plugin.ImportControl, "TEST")
        self.args += ["import"]
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("obj_type", ["Image", "Plate"])
    @pytest.mark.parametrize("name", [None, '-n', '--name', '--plate_name'])
    @pytest.mark.parametrize(
        "description", [None, '-x', '--description', '--plate_description'])
    def testNamingArguments(self, obj_type, name, description, tmpdir,
                            capfd):

        if obj_type == 'Image':
            fakefile = tmpdir.join("test.fake")
        else:
            fakefile = tmpdir.join("SPW&plates=1&plateRows=1&plateCols=1&"
                                   "fields=1&plateAcqs=1.fake")
        fakefile.write('')
        self.args += [str(fakefile)]
        if name:
            self.args += [name, 'name']
        if description:
            self.args += [description, 'description']

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()

        # Retrieve the created object
        pattern = re.compile('^%s:(?P<id>\d+)$' % obj_type)
        match = re.match(pattern, e.split()[-1])
        obj = self.query.get(obj_type, int(match.group('id')))

        if name:
            assert obj.getName().val == 'name'
        if description:
            assert obj.getDescription().val == 'description'
