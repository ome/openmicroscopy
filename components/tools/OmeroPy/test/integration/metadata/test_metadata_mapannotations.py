#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
Integration test of metadata_mapannotation
"""


import library as lib
from omero.model import MapAnnotationI, NamedValue
from omero.rtypes import wrap
from omero.util.metadata_mapannotations import MapAnnotationManager


def assert_equal_name_value(a, b):
    assert isinstance(a, NamedValue)
    assert isinstance(b, NamedValue)
    assert a.name == b.name
    assert a.value == b.value


class TestMapAnnotationManager(lib.ITest):

    def create_mas(self):
        ns1 = self.uuid()
        ns3 = self.uuid()
        ma1 = MapAnnotationI()
        ma1.setNs(wrap(ns1))
        ma1.setMapValue([NamedValue('a', '1')])
        ma2 = MapAnnotationI()
        ma2.setNs(wrap(ns1))
        ma2.setMapValue([NamedValue('a', '2')])
        ma3 = MapAnnotationI()
        ma3.setNs(wrap(ns3))
        ma3.setMapValue([NamedValue('a', '1')])

        mids = self.update.saveAndReturnIds([ma1, ma2, ma3])
        print ns1, ns3, mids
        return ns1, ns3, mids

    def test_add_from_namespace_query(self):
        ns1, ns3, mids = self.create_mas()
        pks = ['a']
        mgr = MapAnnotationManager()
        mgr.add_from_namespace_query(self.sf, ns1, pks)

        assert len(mgr.mapanns) == 2
        pk1 = (ns1, frozenset([('a', '1')]))
        pk2 = (ns1, frozenset([('a', '2')]))
        assert len(mgr.mapanns) == 2
        assert pk1 in mgr.mapanns
        assert pk2 in mgr.mapanns

        cma1 = mgr.mapanns[pk1]
        assert cma1.kvpairs == [('a', '1')]
        assert cma1.parents == set()
        mv1 = cma1.get_mapann().getMapValue()
        assert len(mv1) == 1
        assert_equal_name_value(mv1[0], NamedValue('a', '1'))

        cma2 = mgr.mapanns[pk2]
        assert cma2.kvpairs == [('a', '2')]
        assert cma2.parents == set()
        mv2 = cma2.get_mapann().getMapValue()
        assert len(mv2) == 1
        assert_equal_name_value(mv2[0], NamedValue('a', '2'))
