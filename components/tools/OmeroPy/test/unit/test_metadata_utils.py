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

"""
Test of metadata_utils classes
"""


import pytest

from omero.util.metadata_utils import (
    BulkAnnotationConfiguration, KeyValueListPassThrough,
    KeyValueListTransformer)


def expected(**kwargs):
    """
    Create the final expected column configuration
    """
    c = {
        "clientvalue": None,
        "includeclient": True,
        "include": True,
        "split": False,
        "type": "string",
        "visible": True,
        "position": -1,
        "clientname": None,
        "omitempty": False,
    }
    for k, v in kwargs.iteritems():
        c[k] = v
    return c


class TestBulkAnnotationConfiguration(object):

    # test_init_* methods test get_default_cfg and get_column_config

    def test_init_none(self):
        c = BulkAnnotationConfiguration(None, [])
        assert c.default_cfg == expected()
        assert c.column_cfgs == []

    def test_init_def(self):
        c = BulkAnnotationConfiguration(
            {"visible": False, "omitempty": True}, [])
        assert c.default_cfg == expected(visible=False, omitempty=True)
        assert c.column_cfgs == []

    def test_init_col(self):
        c = BulkAnnotationConfiguration(None, [
            {"name": "a1", "visible": False, "position": 2},
            {"name": "b2", "position": 4}])
        assert c.default_cfg == expected()
        assert len(c.column_cfgs) == 2
        assert c.column_cfgs[0] == expected(
            name="a1", visible=False, position=2)
        assert c.column_cfgs[1] == expected(
            name="b2", position=4)

    def test_init_def_col(self):
        c = BulkAnnotationConfiguration({"omitempty": True}, [
            {"name": "a1"}, {"name": "b2", "omitempty": False}])
        assert c.default_cfg == expected(omitempty=True)
        assert len(c.column_cfgs) == 2
        assert c.column_cfgs[0] == expected(name="a1", omitempty=True)
        assert c.column_cfgs[1] == expected(name="b2")

    def test_validate_column_config(self):
        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_column_config({})

        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_column_config({"name": ""})

        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_column_config({
                "non-existent": 1})

        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_column_config({})

        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_column_config({
                "include": False})

        # Shouldn't throw
        BulkAnnotationConfiguration.validate_column_config({"name": "a"})
        BulkAnnotationConfiguration.validate_column_config({
            "name": "a", "includeclient": False, "include": False})

    def test_validate_filled_column_config(self):
        with pytest.raises(Exception):
            BulkAnnotationConfiguration.validate_filled_column_config({
                "name": "a"})

        # Shouldn't throw
        BulkAnnotationConfiguration.validate_column_config(expected(name="a"))


class TestKeyValueListPassThrough(object):

    def test_transform(self):
        headers = ["a1", "b2"]
        tr = KeyValueListPassThrough(headers)
        assert tr.transform((1, 2)) == (1, 2)


class TestKeyValueListTransformer(object):

    # test_init_* methods test get_output_configs

    def test_init_col(self):
        headers = ["a1"]
        tr = KeyValueListTransformer(
            headers, None, [{"name": "a1", "visible": False}])
        assert tr.default_cfg == expected()

        assert len(tr.output_configs) == 1
        assert tr.output_configs[0] == (expected(name="a1", visible=False), 0)

    def test_init_col_ordered_unordered(self):
        headers = ["a2", "a1"]
        tr = KeyValueListTransformer(headers, None, [
            {"name": "a2", "visible": False},
            {"name": "a1", "position": 1}])
        assert tr.default_cfg == expected()

        assert len(tr.output_configs) == 2
        assert tr.output_configs[0] == (expected(name="a1", position=1), 1)
        assert tr.output_configs[1] == (expected(name="a2", visible=False), 0)

    def test_init_col_unincluded(self):
        headers = ["a2", "a1"]
        tr = KeyValueListTransformer(headers, {
            "include": False, "includeclient": False},
            [{"name": "a2", "include": True}])
        assert tr.default_cfg == expected(include=False, includeclient=False)

        assert len(tr.output_configs) == 1
        assert tr.output_configs[0] == (expected(
            name="a2", include=True, includeclient=False), 0)

    def get_complicated_headers(self):
        # See KeyValueListTransformer.get_output_configs.__doc__
        # - a1 and a4 are positioned
        # - a2 is configured but unpositioned, which means it has precedence
        #   over a3 (which precedes it in the list of headers)
        # - a5 and a6 are also unconfigured, and since the gaps between a1 and
        #   a4 are filled they are put at the end
        # - xx is excluded
        headers = ["a3", "a2", "a4", "a1", "xx", "a5", "a6"]
        column_cfgs = [
            {"name": "a2", "visible": False},
            {"name": "a4", "position": 4, "clientvalue": "*-{{ value }}-*"},
            {"name": "a1", "position": 1, "split": "|"},
            {"name": "xx", "includeclient": False, "include": False},
        ]
        return headers, column_cfgs

    def test_init_col_complicated_order(self):
        headers, column_cfgs = self.get_complicated_headers()
        tr = KeyValueListTransformer(headers, None, column_cfgs)
        assert tr.default_cfg == expected()

        assert len(tr.output_configs) == 6
        assert tr.output_configs[0] == (expected(
            name="a1", position=1, split="|"), 3)
        assert tr.output_configs[1] == (expected(name="a2", visible=False), 1)
        assert tr.output_configs[2] == (expected(name="a3"), 0)
        assert tr.output_configs[3] == (expected(
            name="a4", position=4, clientvalue="*-{{ value }}-*"), 2)
        assert tr.output_configs[4] == (expected(name="a5"), 5)
        assert tr.output_configs[5] == (expected(name="a6"), 6)

    def test_transform1_default(self):
        cfg = expected(name="a1")
        assert KeyValueListTransformer.transform1("ab, c", cfg) == (
            "a1", ["ab, c"])

    def test_transform1_clientname(self):
        cfg = expected(name="a1", clientname="a / 1")
        assert KeyValueListTransformer.transform1("ab, c", cfg) == (
            "a / 1", ["ab, c"])

    @pytest.mark.parametrize('inout', [
        ("ab, c", {}, ("a1", ["ab, c"])),
        ("ab, c", {"clientname": "a / 1"}, ("a / 1", ["ab, c"])),
        ("ab, c", {"visible": False}, ("__a1", ["ab, c"])),
        ("ab, c", {"split": ","}, ("a1", ["ab", "c"])),
        (" ", {"omitempty": True}, ("a1", [])),
        (" , ", {"split": ",", "omitempty": True}, ("a1", [])),
        ("ab, c", {"clientvalue": "*-{{ value }}-*"}, ("a1", ["*-ab, c-*"])),
    ])
    def test_transform1(self, inout):
        cfg = expected(name="a1", **inout[1])
        assert KeyValueListTransformer.transform1(inout[0], cfg) == inout[2]

    def test_transform(self):
        headers, column_cfgs = self.get_complicated_headers()

        tr = KeyValueListTransformer(headers, None, column_cfgs)
        r = tr.transform(("3", "2", "4", "1a|1b", "x", "5", "6"))

        assert len(r) == 6
        assert r[0] == ("a1", ["1a", "1b"])
        assert r[1] == ("__a2", ["2"])
        assert r[2] == ("a3", ["3"])
        assert r[3] == ("a4", ["*-4-*"])
        assert r[4] == ("a5", ["5"])
        assert r[5] == ("a6", ["6"])
