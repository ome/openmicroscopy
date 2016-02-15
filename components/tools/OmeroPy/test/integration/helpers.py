#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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
   Helper method for integration tests

"""

import omero.util.script_utils as scriptUtil
from numpy import arange, uint8


def createTestImage(session):

    plane2D = arange(256, dtype=uint8).reshape(16, 16)
    image = scriptUtil.createNewImage(session, [plane2D], "imageName",
                                      "description", dataset=None)

    return image.getId().getValue()


def createImageWithPixels(client, name, sizes={}):
        """
        Create a new image with pixels
        """
        pixelsService = client.sf.getPixelsService()
        queryService = client.sf.getQueryService()

        pixelsType = queryService.findByQuery(
            "from PixelsType as p where p.value='int8'", None)
        assert pixelsType is not None

        sizeX = "x" in sizes and sizes["x"] or 1
        sizeY = "y" in sizes and sizes["y"] or 1
        sizeZ = "z" in sizes and sizes["z"] or 1
        sizeT = "t" in sizes and sizes["t"] or 1
        sizeC = "c" in sizes and sizes["c"] or 1
        channelList = range(1, sizeC+1)
        id = pixelsService.createImage(
            sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType,
            name, description=None)
        return id
