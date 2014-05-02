#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Tests for the stateful Gateway service.

"""

import test.integration.library as lib
import omero
from omero.rtypes import rint, rlong

from test.integration.helpers import createTestImage


class TestGateway(lib.ITest):

    def testBasicUsage(self):
        gateway = self.client.sf.createGateway()
        gateway.getProjects([0], False)

        try:
            # query below does not find image if created with
            # self.createTestImage() even though it uses 'identical' code to
            # createTestImage(self.client.sf), which uses script_utils
            # iid = self.createTestImage().getId().getValue()
            iid = createTestImage(self.client.sf)
            print iid, type(iid)
            query = self.client.sf.getQueryService()

            params = omero.sys.Parameters()
            params.map = {}
            params.map["oid"] = rlong(iid)
            params.theFilter = omero.sys.Filter()
            params.theFilter.offset = rint(0)
            params.theFilter.limit = rint(1)
            pixel = query.findByQuery(
                "select p from Pixels as p left outer join\
                 fetch p.image as i where i.id=:oid", params)
            print pixel
            imgid = pixel.image.id.val
            print imgid
            gateway.getRenderedImage(pixel.id.val, 0, 0)
        except omero.ValidationException:
            print "testBasicUsage - createTestImage has failed.\
                   This fixture method needs to be fixed."

        gateway.close()
