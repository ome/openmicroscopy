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
Test of metadata_mapannotation classes
"""


import pytest

from omero.model import MapAnnotationI, NamedValue
from omero.rtypes import rstring
from omero.util.metadata_mapannotations import (
    CanonicalMapAnnotation, MapAnnotationManager)


class TestCanonicalMapAnnotation(object):

    # test_init_* methods test process_keypairs

    @pytest.mark.parametrize('ns', [None, '', 'NS'])
    def test_init_defaults_empty(self, ns):
        ma = MapAnnotationI(1)
        if ns is not None:
            ma.setNs(rstring(ns))

        cma = CanonicalMapAnnotation(ma)
        assert cma.id == 1
        expectedns = ns if ns else ''
        assert cma.ns == expectedns
        assert cma.kvpairs == []
        assert cma.primary == (expectedns, frozenset())
        assert cma.parents == set()

    @pytest.mark.parametrize('ns', [None, '', 'NS'])
    def test_init_defaults_kvpairs(self, ns):
        ma = MapAnnotationI(1)
        ma.setMapValue([NamedValue('b', '2'), NamedValue('a', '1')])
        if ns is not None:
            ma.setNs(rstring(ns))

        cma = CanonicalMapAnnotation(ma)
        assert cma.id == 1
        expectedns = ns if ns else ''
        assert cma.ns == expectedns
        assert cma.kvpairs == [('b', '2'), ('a', '1')]
        assert cma.primary == (expectedns, frozenset())
        assert cma.parents == set()

    def test_init_defaults_duplicates(self):
        ma = MapAnnotationI(1)
        ma.setMapValue([NamedValue('a', '1'), NamedValue('a', '1')])

        with pytest.raises(ValueError):
            CanonicalMapAnnotation(ma)

    @pytest.mark.parametrize('unique_keys', [False, True])
    def test_init_unique_keys(self, unique_keys):
        ma = MapAnnotationI(1)
        ma.setMapValue([NamedValue('b', '2'), NamedValue('b', '1')])

        if unique_keys:
            with pytest.raises(ValueError):
                CanonicalMapAnnotation(ma, unique_keys=unique_keys)
        else:
            cma = CanonicalMapAnnotation(ma, unique_keys=unique_keys)
            assert cma.kvpairs == [('b', '2'), ('b', '1')]
            assert cma.primary == ('', frozenset())

    @pytest.mark.parametrize('ns', [None, '', 'NS'])
    @pytest.mark.parametrize('primary_keys', [[], ['a'], ['a', 'b']])
    def test_init_primary_keys(self, ns, primary_keys):
        ma = MapAnnotationI(1)
        ma.setNs(rstring(ns))
        ma.setMapValue([NamedValue('b', '2'), NamedValue('a', '1')])

        cma = CanonicalMapAnnotation(ma, primary_keys=primary_keys)
        assert cma.id == 1
        expectedns = ns if ns else ''
        assert cma.ns == expectedns
        assert cma.kvpairs == [('b', '2'), ('a', '1')]
        expectedpks = [('a', '1'), ('b', '2')][:len(primary_keys)]
        expectedpri = (expectedns, frozenset(expectedpks))
        assert cma.primary == expectedpri
        assert cma.parents == set()

    @pytest.mark.parametrize('reverse', [True, False])
    def test_merge(self, reverse):
        ma1 = MapAnnotationI(1)
        ma1.setMapValue([NamedValue('a', '1')])
        cma1 = CanonicalMapAnnotation(ma1, primary_keys=['a'])
        cma1.add_parent('Parent', 1)
        ma2 = MapAnnotationI(2)
        ma2.setMapValue([NamedValue('b', '2'), NamedValue('a', '1')])
        cma2 = CanonicalMapAnnotation(ma2, primary_keys=['b'])
        cma2.add_parent('Parent', 1)
        cma2.add_parent('Parent', 2)

        if reverse:
            cma2.merge(cma1)
            assert cma2.kvpairs == [('b', '2'), ('a', '1')]
            assert cma2.parents == set([('Parent', 1), ('Parent', 2)])
            assert cma2.primary == ('', frozenset([('b', '2')]))
        else:
            cma1.merge(cma2)
            assert cma1.kvpairs == [('a', '1'), ('b', '2')]
            assert cma1.parents == set([('Parent', 1), ('Parent', 2)])
            assert cma1.primary == ('', frozenset([('a', '1')]))

    def test_add_parent(self):
        ma = MapAnnotationI(1)
        cma = CanonicalMapAnnotation(ma)
        assert cma.parents == set()
        cma.add_parent('A', 1)
        assert cma.parents == set([('A', 1)])
        cma.add_parent('B', 2)
        assert cma.parents == set([('A', 1), ('B', 2)])

        with pytest.raises(ValueError):
            cma.add_parent('C', '3')


class TestMapAnnotationManager(object):

    def create_cmas(self, pk2):
        ma1 = MapAnnotationI(1)
        ma1.setMapValue([NamedValue('a', '1')])
        cma1 = CanonicalMapAnnotation(ma1, primary_keys=['a'])
        cma1.add_parent('Parent', 1)

        ma2 = MapAnnotationI(2)
        ma2.setMapValue([NamedValue('b', '2'), NamedValue('a', '1')])
        cma2 = CanonicalMapAnnotation(ma2, primary_keys=[pk2])
        cma2.add_parent('Parent', 1)
        cma2.add_parent('Parent', 2)

        return cma1, cma2

    @pytest.mark.parametrize('combine', [
        None, MapAnnotationManager.MA_APPEND, MapAnnotationManager.MA_OLD,
        MapAnnotationManager.MA_NEW])
    def test_add_samepk(self, combine):
        cma1, cma2 = self.create_cmas('a')
        pk1 = ('', frozenset([('a', '1')]))
        parents12 = set([('Parent', 1), ('Parent', 2)])

        if combine:
            mgr = MapAnnotationManager(combine)
        else:
            mgr = MapAnnotationManager()
        assert mgr.mapanns == {}

        # These tests all make use of object identity
        # Sanity check:
        assert cma1 != cma2

        r = mgr.add(cma1)
        assert mgr.mapanns == {pk1: cma1}
        assert r is None

        # Duplicate add should have no effect
        r = mgr.add(cma1)
        assert mgr.mapanns == {pk1: cma1}
        assert r is None

        r = mgr.add(cma2)
        if combine == MapAnnotationManager.MA_OLD:
            assert mgr.mapanns == {pk1: cma1}
            assert cma1.kvpairs == [('a', '1')]
            assert cma1.parents == parents12
            assert r is cma2
        elif combine == MapAnnotationManager.MA_NEW:
            assert mgr.mapanns == {pk1: cma2}
            assert cma2.kvpairs == [('b', '2'), ('a', '1')]
            assert cma2.parents == parents12
            assert r is cma1
        else:  # None or MA_APPEND
            assert mgr.mapanns == {pk1: cma1}
            assert cma1.kvpairs == [('a', '1'), ('b', '2')]
            assert cma1.parents == parents12
            assert r is cma2

    @pytest.mark.parametrize('combine', [
        MapAnnotationManager.MA_APPEND, MapAnnotationManager.MA_OLD,
        MapAnnotationManager.MA_NEW])
    def test_add_diffpk(self, combine):
        cma1, cma2 = self.create_cmas('b')
        pk1 = ('', frozenset([('a', '1')]))
        pk2 = ('', frozenset([('b', '2')]))

        mgr = MapAnnotationManager(combine)
        r = mgr.add(cma1)
        assert r is None

        r = mgr.add(cma2)
        assert mgr.mapanns == {pk1: cma1, pk2: cma2}
        assert cma1.kvpairs == [('a', '1')]
        assert cma1.parents == set([('Parent', 1)])
        assert cma2.kvpairs == [('b', '2'), ('a', '1')]
        assert cma2.parents == set([('Parent', 1), ('Parent', 2)])
        assert r is None
