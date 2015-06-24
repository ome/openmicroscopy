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
from omero.plugins.hql import HqlControl, BLACKLISTED_KEYS, WHITELISTED_VALUES


class TestHql(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("hql", HqlControl, "TEST")
        self.args = ["hql"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("key", BLACKLISTED_KEYS)
    def testFilterBlacklist(self, key):
        output = self.cli.controls["hql"].filter({key: 1})
        assert output == {}

    @pytest.mark.parametrize("key", ["rois", "groupExperimenterMap"])
    def testFilterLoaded(self, key):
        output = self.cli.controls["hql"].filter({"_" + key + "Loaded": 1})
        assert output == {}

    @pytest.mark.parametrize(
        ("value", "outcome"),
        [("owner=None;group=None", {}),
         ("owner=1", {"details": "owner=1"})])
    def testFilterDetails(self, value, outcome):
        output = self.cli.controls["hql"].filter({"_details": value})
        assert output == outcome

    @pytest.mark.parametrize("multi_value", [[0, 1]])
    def testFilterMultiValue(self, multi_value):
        output = self.cli.controls["hql"].filter({'key': multi_value})
        assert output == {}

    @pytest.mark.parametrize("empty_value", [None, [], {}])
    def testFilterEmptyValue(self, empty_value):
        output = self.cli.controls["hql"].filter({'key': empty_value})
        assert output == {}

    @pytest.mark.parametrize("value", WHITELISTED_VALUES)
    def testFilterWhitelist(self, value):
        output = self.cli.controls["hql"].filter({'key': value})
        assert output == {'key': value}

    def testFilterStrip(self):
        output = self.cli.controls["hql"].filter({'_key': 1})
        assert output == {'key': 1}
