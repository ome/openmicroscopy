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


from omero.cli import CLI
from omero.plugins.sessions import SessionsControl

from library import ITest
from omero_ext.mox import Mox


class AbstractCLITest(ITest):

    def setup_method(self, method):
        super(AbstractCLITest, self).setup_method(method)
        self.cli = CLI()
        self.cli.register("sessions", SessionsControl, "TEST")

    def setup_mock(self):
        self.mox = Mox()

    def teardown_mock(self):
        self.mox.UnsetStubs()
        self.mox.VerifyAll()


class CLITest(AbstractCLITest):

    def setup_method(self, method):
        super(CLITest, self).setup_method(method)
        self.args = self.login_args()


class RootCLITest(AbstractCLITest):

    def setup_method(self, method):
        super(RootCLITest, self).setup_method(method)
        self.args = self.root_login_args()
