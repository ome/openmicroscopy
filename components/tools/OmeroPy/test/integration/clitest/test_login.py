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

from omero.plugins.sessions import SessionsControl
from test.integration.clitest.cli import CLITest
import pytest

subcommands = ['help', 'login', 'logout', 'group',
               'list', 'keepalive', 'clear', 'file']


class TestLoginAsRoot(CLITest):

    def setup_method(self, method):
        super(TestLoginAsRoot, self).setup_method(method)
        self.cli.register("sessions", SessionsControl, "TEST")
        self.args += ["sessions"]

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("subcommand", subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    # Login subcommand
    # ========================================================================
    def testLoginAsRoot(self, capsys):
        user = self.new_user()
        passwd = self.root.getProperty("omero.rootpass")
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        args = ["sessions", "login", "--sudo", "root", "-w", passwd]
        args += ["%s@%s:%s" % (user.omeName.val, host, port)]
        self.cli.invoke(args, strict=True)

    @pytest.mark.xfail(reason="NYI")  # This must be implemented
    def testLoginAsGroupAdmin(self, capsys):
        group = self.new_group()
        grp_admin = self.new_user(group=group, admin=True)
        admin = grp_admin.omeName.val
        user = self.new_user(group=group)
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        args = ["sessions", "login", "--sudo", admin, "-w", "ignore"]
        args += ["%s@%s:%s" % (user.omeName.val, host, port)]
        self.cli.invoke(args, strict=True)
