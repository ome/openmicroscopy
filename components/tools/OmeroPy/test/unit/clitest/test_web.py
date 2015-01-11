#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

import pytest
from path import path
from omero.cli import CLI
from omero.plugins.web import WebControl

subcommands = [
    "start", "stop", "restart", "status", "iis", "config"]
TYPES = ("nginx", "apache", "apache-fcgi")


class TestWeb(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("web", WebControl, "TEST")
        self.args = ["web"]

    def add_etc_dir(self):
        dist_dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dist_dir = dist_dir.abspath()
        etc_dir = dist_dir / "etc"
        self.args += ["--etcdir", etc_dir]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('subcommand', subcommands)
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('type', TYPES)
    @pytest.mark.parametrize('system', [True, False])
    @pytest.mark.parametrize('http', [False, 8081])
    def testConfig(self, type, system, http):
        self.args += ["config", type]
        if system:
            self.args += ["--system"]
        if http:
            self.args += ["--http", str(http)]
        self.add_etc_dir()
        self.cli.invoke(self.args, strict=True)
