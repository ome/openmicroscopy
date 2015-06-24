#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Integration test focused on the omero.api.IQuery interface.

"""

import library as lib
from omero.rtypes import unwrap


class TestQuery(lib.ITest):

    # ticket:1849

    def testGetPixelsCount(self):
        q = self.root.sf.getQueryService()
        a = self.root.sf.getAdminService()
        groups = a.lookupGroups()
        for group in groups:
            rtypeseqseq = q.projection(
                """
                select p.pixelsType.value,
                sum(cast(p.sizeX as long) * p.sizeY
                    * p.sizeZ * p.sizeT * p.sizeC)
                from Pixels p group by p.pixelsType.value
                """,
                None, {"omero.group": str(group.id.val)})
            rv = unwrap(rtypeseqseq)
            as_map = dict()
            for obj_array in rv:
                as_map[obj_array[0]] = obj_array[1]
            if len(as_map) > 0:
                print "Group %s: %s" % (group.id.val, as_map)
