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

from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.plugins.ldap import LdapControl

import pytest

subcommands = ["active", "list", "discover", "create", "getdn", "setdn"]


class TestLDAP(CLITest):

    def setup_method(self, method):
        super(TestLDAP, self).setup_method(method)
        self.cli.register("ldap", LdapControl, "TEST")
        self.args += ["ldap"]

    @pytest.mark.parametrize('subcommand', subcommands)
    def testAdminOnly(self, subcommand, capsys):
        """Test ldap active subcommand"""

        self.args += [subcommand]
        if subcommand in ["create"]:
            self.args += [self.uuid()]
        elif subcommand in ["setdn"]:
            self.args += ["--user-name", self.uuid(), "true"]
        elif subcommand in ["getdn"]:
            self.args += ["--user-name", self.uuid()]

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")
