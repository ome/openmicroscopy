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

"""
Tests for the MapAnnotation and related base types
introduced in 5.1.
"""

import library as lib
import pytest
import omero

from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_MapAnnotationI import MapAnnotationI
from omero.rtypes import rbool, rstring
from omero.rtypes import unwrap
from omero.model import NamedValue as NV


class TestMapAnnotation(lib.ITest):

    def assertNV(self, nv, name, value):
        assert name == nv.name
        assert value == nv.value

    def assertNVs(self, nvl1, nvl2):
        for i in range(max(len(nvl1), len(nvl2))):
            assert nvl1[i].name == nvl2[i].name
            assert nvl1[i].value == nvl2[i].value

    def testMapStringField(self):
        uuid = self.uuid()
        queryService = self.root.getSession().getQueryService()
        updateService = self.root.getSession().getUpdateService()
        group = ExperimenterGroupI()
        group.setName(rstring(uuid))
        group.setLdap(rbool(False))
        group.setConfig([NV("language", "python")])
        group = updateService.saveAndReturnObject(group)
        group = queryService.findByQuery(
            ("select g from ExperimenterGroup g join fetch g.config "
             "where g.id = %s" % group.getId().getValue()), None)
        self.assertNV(group.getConfig()[0], "language", "python")

    @pytest.mark.parametrize("data", (
        ([NV("a", "")], [NV("a", "")]),
        ([NV("a", "b")], [NV("a", "b")]),
    ))
    def testGroupConfigA(self, data):

        save_value, expect_value = data

        LOAD = ("select g from ExperimenterGroup g "
                "left outer join fetch g.config where g.id = :id")

        queryService = self.root.sf.getQueryService()
        updateService = self.root.sf.getUpdateService()
        params = omero.sys.ParametersI()

        def load_group(id):
            params.addId(id)
            return queryService.findByQuery(LOAD, params)

        group = self.new_group()
        gid = group.id.val
        group.config = save_value

        updateService.saveObject(group)
        group = load_group(gid)
        config = unwrap(group.config)

        self.assertNVs(expect_value, config)

        name, value = unwrap(queryService.projection(
            """
            select m.name, m.value from ExperimenterGroup g
            left outer join g.config m where m.name = 'a'
            and g.id = :id
            """, omero.sys.ParametersI().addId(gid))[0])

        self.assertNV(expect_value[0], name, value)

    def testGroupConfigEdit(self):

        before = [
            NV("a", "b"),
            NV("c", "d"),
            NV("e", "f")
        ]

        remove_one = [
            NV("a", "b"),
            NV("e", "f")
        ]

        swapped = [
            NV("e", "f"),
            NV("a", "b")
        ]

        edited = [
            NV("e", "f"),
            NV("a", "x")
        ]

        root_update = self.root.sf.getUpdateService()

        group = self.new_group()
        group.setConfig(before)
        group = root_update.saveAndReturnObject(group)
        self.assertNVs(before, group.getConfig())

        del group.getConfig()[1]
        group = root_update.saveAndReturnObject(group)
        self.assertNVs(remove_one, group.getConfig())

        old = list(group.getConfig())
        self.assertNVs(old, remove_one)
        group.setConfig([old[1], old[0]])
        group = root_update.saveAndReturnObject(group)
        self.assertNVs(swapped, group.getConfig())

        group.getConfig()[1].value = "x"
        group = root_update.saveAndReturnObject(group)
        self.assertNVs(edited, group.getConfig())

    def testEmptyItem(self):
        a = MapAnnotationI()
        a.setMapValue([NV('Name1', 'Value1'), NV('Name2', 'Value2')])
        a = self.update.saveAndReturnObject(a)
        m = self.query.findAllByQuery((
            "from MapAnnotation m "
            "join fetch m.mapValue a "
            "where a.name='Name2'"), None)[0]
        l1 = m.getMapValue()
        assert l1[0] is None
        self.assertNV(l1[1], "Name2", "Value2")
        assert {"Name2": "Value2"} == m.getMapValueAsMap()

    def testBigKeys(self):
        uuid = self.uuid()
        big = uuid + "X" * 500
        a = MapAnnotationI()
        a.setMapValue([NV(big, big)])
        a = self.update.saveAndReturnObject(a)
        m = self.query.findAllByQuery((
            "from MapAnnotation m "
            "join fetch m.mapValue a "
            "where a.name like :name"),
            omero.sys.ParametersI().addString("name",
                                              uuid + "%")
        )[0]
        l1 = m.getMapValue()
        self.assertNV(l1[0], big, big)
