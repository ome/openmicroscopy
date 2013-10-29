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

from omero.plugins.tag import TagControl
from test.integration.clitest.cli import CLITest


class TestTag(CLITest):

    def setup_method(self, method):
        super(TestTag, self).setup_method(method)
        self.cli.register("tag", TagControl, "TEST")

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        args = self.login_args() + ["tag", "-h"]
        self.cli.invoke(args, strict=True)

    def testCreateHelp(self):
        args = self.login_args() + ["tag", "create", "-h"]
        self.cli.invoke(args, strict=True)

    def testCreateSetHelp(self):
        args = self.login_args() + ["tag", "createset", "-h"]
        self.cli.invoke(args, strict=True)

    def testListHelp(self):
        args = self.login_args() + ["tag", "list", "-h"]
        self.cli.invoke(args, strict=True)

    def testListSetsHelp(self):
        args = self.login_args() + ["tag", "listsets", "-h"]
        self.cli.invoke(args, strict=True)

    def testLinkHelp(self):
        args = self.login_args() + ["tag", "link", "-h"]
        self.cli.invoke(args, strict=True)

    def testLoadHelp(self):
        args = self.login_args() + ["tag", "load", "-h"]
        self.cli.invoke(args, strict=True)
