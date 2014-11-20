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
Test of the automatic JVM setting logic for OMERO startup.
"""


import pytest


from omero.config import ConfigXml, xml

from omero.install.jvmcfg import adjust_settings
from omero.install.jvmcfg import ManualStrategy
from omero.install.jvmcfg import PercentStrategy
from omero.install.jvmcfg import Settings
from omero.install.jvmcfg import Strategy
from omero.install.jvmcfg import strip_dict
from omero.install.jvmcfg import usage_charts

from omero.util.temp_files import create_path

from path import path

from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import tostring
from xml.etree.ElementTree import XML

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
        rv = strip_dict({"a.b": "c"}, prefix="a")
        assert {"b": "c"} == rv

    def test_2(self):
        rv = strip_dict({"a.b.c": "d"}, prefix="a.b")
        assert rv["c"] == "d"

    def test_3(self):
        rv = strip_dict({
            "omero.jvmcfg.foo": "a",
            "something.else": "b"})

        assert rv["foo"] == "a"
        assert "something.else" not in rv

    @pytest.mark.parametrize("input,output", (
        ({"omero.jvmcfg.heap_size.blitz": "1g"}, {"heap_size": "1g"}),
        ))
    def test_4(self, input, output):
        p = write_config(input)
        config = ConfigXml(filename=str(p), env_config="default")
        try:
            m = config.as_map()
            s = strip_dict(m, suffix="blitz")
            assert s == output
        finally:
            config.close()

    def test_5(self):
        rv = strip_dict({
            "omero.jvmcfg.a.blitz": "b",
        }, suffix="blitz")
        assert rv["a"] == "b"


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
            Strategy("blitz")

    def test_hard_coded(self):
        strategy = ManualStrategy("blitz")
        settings = strategy.get_memory_settings()
        assert settings == [
            "-Xmx512m",
            "-XX:MaxPermSize=128m",
        ]

    def test_percent_usage(self):
        strategy = PercentStrategy("blitz")
        table = list(strategy.usage_table(15, 16))[0]
        assert table[0] == 2**15
        assert table[1] == 2**15*15/100

    def test_heap_dump_on(self):
        settings = Settings({"heap_dump": "on"})
        strategy = PercentStrategy("blitz", settings)
        hd = strategy.get_heap_dump()
        append = strategy.get_append()
        assert " " not in hd
        assert "HeapDumpPath" not in hd
        assert not append

    def test_heap_dump_tmp(self):
        settings = Settings({"heap_dump": "tmp"})
        strategy = PercentStrategy("blitz", settings)
        hd = strategy.get_heap_dump()
        append = strategy.get_append()
        assert " " not in hd
        assert "HeapDumpPath" not in hd
        assert "HeapDumpPath" in "".join(append)


class AdjustFixture(object):

    def __init__(self, input, output, name, **kwargs):
        self.input = input
        self.output = output
        self.name = name
        self.kwargs = kwargs

    def validate(self, rv):
        for k, v in self.output.items():
            assert k in rv
            found = rv[k]
            found.pop(0)  # settings
            assert v == found, "%s.%s: %s <> %s" % (self.name, k,
                                                    v, found)


import json
f = open(__file__[:-3] + ".json", "r")
data = json.load(f)
AFS = []
for x in data:
    AFS.append(AdjustFixture(x["input"], x["output"], x["name"]))


def template_xml():
    templates = path(__file__) / ".." / ".." / ".."
    templates = templates / ".." / ".." / ".."
    templates = templates / "etc" / "grid" / "templates.xml"
    templates = templates.abspath()
    return XML(templates.text())


class TestAdjustStrategy(object):

    @pytest.mark.parametrize("fixture", AFS)
    def test_adjust(self, fixture):
        p = write_config(fixture.input)
        xml = template_xml()
        config = ConfigXml(filename=str(p), env_config="default")
        try:
            rv = adjust_settings(config, xml, **fixture.kwargs)
            fixture.validate(rv)
        finally:
            config.close()


class TestChart(object):

    def test_percent_chart(self):
        try:
            usage_charts("target/charts.png")
        except ImportError:
            # Requires matplotlib, etc
            pass
