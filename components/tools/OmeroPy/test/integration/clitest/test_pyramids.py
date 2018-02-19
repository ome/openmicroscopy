#!/usr/bin/env python
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


from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode

import omero.plugins.admin
import pytest


class TestRemovePyramids(CLITest):

    def setup_method(self, method):
        super(TestRemovePyramids, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "removepyramids"]

    def test_removepyramids_admin_only(self, capsys):
        """Test removepyramids is admin-only"""
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestRemovePyramidsRestrictedAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Restricted
    # Admin with no privileges
    DEFAULT_PRIVILEGES = ()

    def setup_method(self, method):
        super(TestRemovePyramidsRestrictedAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "removepyramids"]

    def test_removepyramids_restricted_admin(self, capsys):
        """Test removepyramids cannot be run by Restricted Admin"""
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_end = "SecurityViolation: Admin restrictions: ReadSession\n"
        assert err.endswith(output_end)
