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
   Tests for the Pixels service.

"""

import omero
import omero.gateway
import library as lib


class TestPixelsService(lib.ITest):

    def createImage(self):
        """
        Create a new image
        """
        pixelsService = self.client.sf.getPixelsService()
        queryService = self.client.sf.getQueryService()

        pixelsType = queryService.findByQuery(
            "from PixelsType as p where p.value='int8'", None)
        assert pixelsType is not None

        sizeX = 1
        sizeY = 1
        sizeZ = 1
        sizeT = 1
        channelList = range(1, 4)
        id = pixelsService.createImage(
            sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType,
            self.uuid(), description=None)
        return id

    def test9655(self):
        # Create an image without statsinfo objects and attempt
        # to retrieve it from the Rendering service.

        # Get the pixels
        image_id = self.createImage()
        gateway = omero.gateway.BlitzGateway(client_obj=self.client)
        image = gateway.getObject("Image", image_id)
        pixels_id = image.getPrimaryPixels().id

        # Save the pixels
        rps = self.client.sf.createRawPixelsStore()
        rps.setPixelsId(pixels_id, False)
        rps.setPlane([0], 0, 0, 0)
        rps.save()
        rps.close()

        # Now use the RE to load
        re = self.client.sf.createRenderingEngine()
        re.lookupPixels(pixels_id)
        re.resetDefaultSettings(save=True)
        re.lookupPixels(pixels_id)
        re.lookupRenderingDef(pixels_id)
        re.getPixels()
