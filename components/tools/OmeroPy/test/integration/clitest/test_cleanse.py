#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2017 University of Dundee & Open Microscopy Environment.
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


from test.integration.clitest.cli import CLITest, RootCLITest
from omero.cli import NonZeroReturnCode

import omero.plugins.admin
import pytest
import omero


class TestCleanse(CLITest):

    def setup_method(self, method):
        super(TestCleanse, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "cleanse"]

    def testCleanseAdminOnly(self, capsys):
        """Test cleanse is admin-only"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestCleanseRoot(RootCLITest):

    def setup_method(self, method):
        super(TestCleanseRoot, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "cleanse"]

    def testCleanseAdminOnly(self, capsys):
        """Test cleanse works for root with expected output"""
        config_service = self.root.sf.getConfigService()
        data_dir = config_service.getConfigValue("omero.data.dir")
        self.args += [data_dir]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_string_start = "Removing empty directories from...\n "
        output_string = output_string_start + data_dir + "ManagedRepository\n"
        assert output_string in out
