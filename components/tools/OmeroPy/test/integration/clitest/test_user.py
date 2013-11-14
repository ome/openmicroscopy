#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

from omero.plugins.user import UserControl
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest, RootCLITest
import pytest

subcommands = ['add', 'list', 'password', 'email', 'joingroup', 'leavegroup']
user_pairs = [('--id', 'id'), ('--name', 'name')]
group_pairs = [(None, 'id'), (None, 'omeName'), ('--user-id', 'id'),
               ('--user-name', 'omeName')]


class TestUser(CLITest):

    def setup_method(self, method):
        super(TestUser, self).setup_method(method)
        self.cli.register("user", UserControl, "TEST")
        self.args += ["user"]

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("subcommand", subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)


class TestGroupRoot(RootCLITest):

    def setup_method(self, method):
        super(TestUser, self).setup_method(method)
        self.cli.register("user", UserControl, "TEST")
        self.args += ["user"]
