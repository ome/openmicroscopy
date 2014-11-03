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
Multiple inheritance class that can be re-used by all of
the unit implementations.
"""

from omero.rtypes import unwrap


class UnitBase(object):

    def _base_unit(self, u):
        if u is not None:
            if u.isLoaded():
                u = unwrap(u.getValue())
            else:
                u = "(%s:%s)" % (u.__class__.__name__,
                                 u.id.val)
        return u

    def _base_string(self, v, u):
        print v
        if v is not None:
            return "%s %s" % (v, self._base_unit(u))
        return ""
