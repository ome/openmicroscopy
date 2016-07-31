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
Utilities for manipulating map-annotations used as metadata
"""

from omero.rtypes import unwrap


class CanonicalMapAnnotation(object):
    """
    A canonical representation of a map-annotation for metadata use
    This is based around the idea of a primary key derived from the
    combination of the namespace with 1+ keys-value pairs. A null
    namespace is treated as an empty string (''), but still forms part
    of the primary key.

    ma: The omero.model.MapAnnotation object
    primary_keys: Keys from key-value pairs that will be used to form the
        primary key.
    unique_keys: If False duplicate keys (with different values) aren't
        allowed, default True (allowed)
    """

    def __init__(self, ma, primary_keys=None, unique_keys=True):
        # self.date = unwrap(ma.getDate())
        # self.description = unwrap(ma.getDescription())
        self.id = unwrap(ma.getId())
        ns = unwrap(ma.getNs())
        self.ns = ns if ns else ''
        try:
            mapvalue = [(kv.name, kv.value) for kv in ma.getMapValue()]
        except TypeError:
            mapvalue = []
        self.kvpairs, self.primary = self.process_keypairs(
            mapvalue, primary_keys, unique_keys)
        self.parents = set()

    def process_keypairs(self, kvpairs, primary_keys, unique_keys):
        if len(set(kvpairs)) != len(kvpairs):
            raise ValueError('Duplicate key-value pairs found: %s' % kvpairs)

        if unique_keys:
            u_keys = [k for (k, v) in kvpairs]
            if len(set(u_keys)) != len(u_keys):
                raise ValueError('Duplicate keys found: %s' % u_keys)

        if primary_keys:
            primary_keys = set(primary_keys)
            # ns is always part of the primary key
            primary = (
                self.ns,
                frozenset((k, v) for (k, v) in kvpairs if k in primary_keys))
        else:
            primary = (self.ns, frozenset())

        return kvpairs, primary

    def merge(self, other):
        """
        Adds any key/value pairs from other that aren't in self
        Adds parents from other
        Does not update primary key
        """
        if self.kvpairs != other.kvpairs:
            kvpairsset = set(self.kvpairs)
            for okv in other.kvpairs:
                if okv not in kvpairsset:
                    self.kvpairs.append(okv)
        self.merge_parents(other)

    def merge_parents(self, other):
        self.parents.update(other.parents)

    def add_parent(self, parenttype, parentid):
        """
        Add a parent descriptor
        Parameter types are important because they are used in a set

        parenttype: An OMERO type string
        parentid: An OMERO object ID (integer)
        """
        if not isinstance(parenttype, str) or not isinstance(
                parentid, (int, long)):
            raise ValueError('Expected parenttype:str parentid:integer')
        self.parents.add((parenttype, parentid))

    def __str__(self):
        return '%s: %s' % (self.primary, self.kvpairs)


class MapAnnotationManager(object):
    """
    Handles creation and de-duplication of MapAnnotations
    """
    # Policies for combining/replacing MapAnnotations
    MA_APPEND, MA_OLD, MA_NEW = range(3)

    def __init__(self, combine=MA_APPEND):
        self.mapanns = {}
        self.combine = combine

    def add(self, ma):
        """
        Adds a map-annotation to the managed list.

        Returns any map-annotations that are no longer required, this may be
        ma or it may be a previously added annotation. The idea is that this
        can be used to de-duplicate existing OMERO MapAnnotations by calling
        add() on all MapAnnotations and deleting those which are returned

        If MapAnnotations are combined the parents of the unwanted
        MapAnnotations are appended to the one that is kept by the manager.
        """
        try:
            current = self.mapanns[ma.primary]
            if current is ma:
                # Don't re-add an identical object
                return
            if self.combine == self.MA_APPEND:
                current.merge(ma)
                return ma
            if self.combine == self.MA_NEW:
                self.mapanns[ma.primary] = ma
                ma.merge_parents(current)
                return current
            if self.combine == self.MA_OLD:
                current.merge_parents(ma)
                return ma
            raise ValueError('Invalid combine policy')
        except KeyError:
            self.mapanns[ma.primary] = ma
