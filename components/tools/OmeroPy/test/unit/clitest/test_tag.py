#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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
from omero.cli import CLI, NonZeroReturnCode
from omero.plugins.tag import TagControl


class TestTag(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("tag", TagControl, "TEST")
        self.args = ["tag"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('subcommand', TagControl().get_subcommands())
    def testSubcommandHelp(self, subcommand):
        self.args += [subcommand, "-h"]
        self.cli.invoke(self.args, strict=True)

    def testCreateTagsetFails(self):
        self.args += ["createset", "--tag", "A"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testListFails(self):
        self.args += ["list", "--tagset", "tagset"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testListsetsFails(self):
        self.args += ["listsets", "--tag", "tag"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize(
        ('object_arg', 'tag_arg'),
        [('Image:1', 'test'), ('Image', '1'), ('Image:image', '1'),
         ('1', '1')])
    def testLinkFails(self, object_arg, tag_arg):
        self.args += ["link", object_arg, tag_arg]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
