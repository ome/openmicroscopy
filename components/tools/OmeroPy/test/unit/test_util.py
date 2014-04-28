#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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
Test of various things under omero.util
"""

import pytest

from omero.util.text import CSVStyle


class MockTable(object):

    def __init__(self, names, data, expected):
        self.names = names
        self.data = data
        self.length = len(data)
        self.expected = expected

    def get_row(self, i):
        if i is None:
            return self.names
        return self.data[i]


csv_tables = (
    MockTable(("c1", "c2"), (("a", "b"),), ['c1,c2', 'a,b\r\n']),
    MockTable(("c1", "c2"), (("a,b", "c"),), ['c1,c2', '"a,b",c\r\n']),
    MockTable(("c1", "c2"), (("'a b'", "c"),), ['c1,c2', "'a b',c\r\n"]),
)


class TestCSVSTyle(object):

    @pytest.mark.parametrize('mock_table', csv_tables)
    def testGetRow(self, mock_table):
        assert mock_table.get_row(None) == mock_table.names
        for i in range(mock_table.length):
            assert mock_table.get_row(i) == mock_table.data[i]

    @pytest.mark.parametrize('mock_table', csv_tables)
    def testCSVModuleParsing(self, mock_table):
        style = CSVStyle()
        output = list(style.get_rows(mock_table))
        assert mock_table.expected == output
