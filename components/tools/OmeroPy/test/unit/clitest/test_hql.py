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


import pytest
from omero.cli import CLI
from omero.plugins.hql import HqlControl


class FilterFixture(object):
    """
    Fixture to test naming arguments of bin/omero import
    """

    def __init__(self, name, values, output):
        self.name = name
        self.values = values
        self.output = output

FF = FilterFixture
FFS = (
    FF("_id", {"_id": 1}, {}),
    FF("_loaded_True", {"_loaded": True}, {}),
    FF("_loaded_false", {"_loaded": False}, {}),
    FF("_details1", {"_details": "owner=None;group=None"}, {}),
    FF("_details2", {"_details": "owner=1"}, {"details": "owner=1"}),
    FF("empty_list", {"test": []}, {}),
    FF("list", {"test": [0, 1, 2]}, {}),
    FF("empty_dict", {"test": {}}, {}),
    FF("None", {"test": None}, {}),
    FF("True", {"test": True}, {"test": True}),
    FF("False", {"test": False}, {}),
    FF("zero", {"test": 0}, {"test": 0}),
    FF("_strip", {"test": 1, "_test2": 2}, {"test": 1, "test2": 2}),
    )


class TestHql(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("hql", HqlControl, "TEST")
        self.args = ["hql"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("fixture", FFS, ids=[x.name for x in FFS])
    def testFilter(self, fixture):
        output = self.cli.controls["hql"].filter(fixture.values)
        assert output == fixture.output
