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
from omero.plugins.fs import FsControl


class TestFS(CLITest):

    def setup_method(self, method):
        super(TestFS, self).setup_method(method)
        self.cli.register("fs", FsControl, "TEST")
        self.args += ["fs"]

    def set_conn_args(self):
        passwd = self.root.getProperty("omero.rootpass")
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["fs", "-w", passwd]
        self.args += ["-s", host, "-p",  port]

    def testRepos(self, capsys):
        """Test naming arguments for the imported image/plate"""
        print "X"*100
        self.args += ["repos", "--style=plain"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        errs = [line
                for line in e.split("\n")
                if line.strip() and "Joined session" not in line]
        if errs:
            raise Exception(errs)

        example = \
        """
        0,1,2596178c-2bbd-432d-9d3a-82567df189e5,Managed,/tmp/Iracyid4/ManagedRepository
        1,2,44e61b9c-de08-4af5-9ed9-7181ea0f71c9,Public,/tmp/Iracyid4
        2,3,ScriptRepo,Script,/opt/ome2/dist/lib/scripts\n',
        """
        outs = [line
                for line in o.split("\n")
                if line.strip() and (
                    (",Managed," in line) or
                    (",Script," in line) or
                    (",Public,") in line
                )]
