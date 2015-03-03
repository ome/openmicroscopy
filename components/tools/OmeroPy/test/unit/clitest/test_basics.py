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
from omero.cli import CLI

cli = CLI()
cli.loadplugins()
commands = cli.controls.keys()
topics = cli.topics.keys()


class TestBasics(object):

    def testHelp(self):
        self.args = ["help", "-h"]
        cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('recursive', [None, "--recursive"])
    def testHelpAll(self, recursive):
        self.args = ["help", "--all"]
        if recursive:
            self.args.append(recursive)
        cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('recursive', [None, "--recursive"])
    @pytest.mark.parametrize('command', commands)
    def testHelpCommand(self, command, recursive):
        self.args = ["help", command]
        if recursive:
            self.args.append(recursive)
        cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('topic', topics)
    def testHelpTopic(self, topic):
        self.args = ["help", topic, "-h"]
        cli.invoke(self.args, strict=True)

    def testHelpList(self):
        self.args = ["help", "list"]
        cli.invoke(self.args, strict=True)

    def testQuit(object):
        cli.invoke(["quit"], strict=True)

    def testVersion(object):
        cli.invoke(["version"], strict=True)
