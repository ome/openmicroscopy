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
Test of the automatic memory setting logic for OMERO startup.
"""


import pytest

from omero.config import ConfigXml, xml

from omero.install.memory import HardCodedStrategy
from omero.install.memory import Settings
from omero.install.memory import Strategy
from omero.install.memory import strip_prefix

from omero.util.temp_files import create_path

from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import tostring

from test.unit.test_config import initial


def write_config(data):
        p = create_path()
        i = initial()
        for k, v in data.items():
            for x in i[0:2]:  # __ACTIVE__ & default
                SubElement(x, "property", name=k, value=v)
        string = tostring(i, 'utf-8')
        txt = xml.dom.minidom.parseString(string).toprettyxml("  ", "\n", None)
        p.write_text(txt)
        return p


class TestMemoryStrip(object):

    def test_1(self):
        rv = strip_prefix({"a.b": "c"}, "a")
        assert rv["b"] == "c"

    def test_2(self):
        rv = strip_prefix({"a.b.c.d": "e"}, "a.b")
        assert rv["c.d"] == "e"

    def test_3(self):
        rv = strip_prefix({
            "omero.mem.foo": "a",
            "something.else": "b"})

        assert rv["foo"] == "a"
        assert "something.else" not in rv

    @pytest.mark.parametrize("input,output", (
        ({"omero.mem.blitz.heap_size": "1g"}, {"heap_size": "1g"}),
        ))
    def test_4(self, input, output):
        p = write_config(input)
        config = ConfigXml(filename=str(p), env_config="default")
        try:
            m = config.as_map()
            s = strip_prefix(m, "omero.mem.blitz")
            assert s == output
        finally:
            config.close()


class TestSettings(object):

    def test_initial(self):
        s = Settings()
        assert s.perm_gen == "128m"
        assert s.heap_dump == "off"
        assert s.heap_size == "512m"

    def test_explicit(self):
        s = Settings({
            "perm_gen": "xxx",
            "heap_dump": "yyy",
            "heap_size": "zzz",
            })
        assert s.perm_gen == "xxx"
        assert s.heap_dump == "yyy"
        assert s.heap_size == "zzz"

    def test_defaults(self):
        s = Settings({}, {
            "perm_gen": "xxx",
            "heap_dump": "yyy",
            "heap_size": "zzz",
            })
        assert s.perm_gen == "xxx"
        assert s.heap_dump == "yyy"
        assert s.heap_size == "zzz"

    def test_both(self):
        s = Settings({
            "perm_gen": "aaa",
            "heap_dump": "bbb",
            "heap_size": "ccc",
            }, {
            "perm_gen": "xxx",
            "heap_dump": "yyy",
            "heap_size": "zzz",
            })
        assert s.perm_gen == "aaa"
        assert s.heap_dump == "bbb"
        assert s.heap_size == "ccc"


class TestStrategy(object):

    def test_no_instantiate(self):
        with pytest.raises(Exception):
            Strategy()

    def test_hard_coded(self):
        strategy = HardCodedStrategy()
        settings = strategy.get_memory_settings()
        assert "-Xmx512m -XX:MaxPermSize=128m" == settings
