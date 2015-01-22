#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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

"""
   Harness for calling scripts via `omero script`
   of omero.client

"""

import omero.cli
import library as lib


def call(*args):
    omero.cli.argv(["omero", "script"] + list(args))


class TestScriptsViaOmeroCli(lib.ITest):

    def testDefinition(self):
        call("test/scripts/definition.py",
             "--Ice.Config=test/scripts/definition.cfg")

    def testSimpleScript(self):
        call("test/scripts/simple_script.py",
             "--Ice.Config=test/scripts/simple_script.cfg")
