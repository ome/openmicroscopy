#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved. Use is subject to license terms supplied in LICENSE.txt
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
   Integration test for rendering engine, particularly
   rendering a 'region' of big images.
"""

import omero
import logging
import library as lib

try:
    from PIL import Image  # see ticket:2597
except:  # pragma: nocover
    try:
        import Image  # see ticket:2597
    except:
        logging.error('No Pillow installed')

try:
    import hashlib
    hash_sha1 = hashlib.sha1
except:
    import sha
    hash_sha1 = sha.new

from numpy import asarray


class TestFigureExportScripts(lib.ITest):

    def testRenderRegion(self):
        """
        Test attempts to compare a full image plane, cropped to a region, with
        a region retrieved from rendering engine.

        Uses PIL to convert compressed strings into 2D numpy arrays for
        cropping and comparison. ** Although cropped images and retrieved
        regions APPEAR identical, there appear to be rounding or rendering
        errors, either in the 'renderCompressed' method of the rendering engine
        or in PIL **

        For this reason, there are small differences in the pixel values of
        rendered regions. This functionality is also tested in Java. Therefore
        this test is not 'Activated' currently.
        """

        session = self.root.sf

        sizeX = 4
        sizeY = 3
        sizeZ = 1
        sizeC = 1
        sizeT = 1
        image = self.createTestImage(sizeX, sizeY, sizeZ, sizeC, sizeT)
        pixelsId = image.getPrimaryPixels().id.val

        renderingEngine = session.createRenderingEngine()
        renderingEngine.lookupPixels(pixelsId)
        if not renderingEngine.lookupRenderingDef(pixelsId):
            renderingEngine.resetDefaultSettings(save=True)
        renderingEngine.lookupRenderingDef(pixelsId)
        renderingEngine.load()

        # turn all channels on
        for i in range(sizeC):
            renderingEngine.setActive(i, True)

        regionDef = omero.romio.RegionDef()
        x = 0
        y = 0
        width = 2
        height = 2
        x2 = x + width
        y2 = y + height

        regionDef.x = x
        regionDef.y = y
        regionDef.width = width
        regionDef.height = height

        planeDef = omero.romio.PlaneDef()
        planeDef.z = long(0)
        planeDef.t = long(0)

        import StringIO

        # First, get the full rendered plane...
        img = renderingEngine.renderCompressed(planeDef)  # compressed String
        fullImage = Image.open(StringIO.StringIO(img))  # convert to numpy arr
        img_array = asarray(fullImage)  # 3D array, since each pixel is [r,g,b]

        # get the cropped image
        cropped = img_array[y:y2, x:x2, :]      # ... so we can crop to region
        cropped_img = Image.fromarray(cropped)
        h = hash_sha1()
        h.update(cropped_img.tostring())
        hash_cropped = h.hexdigest()

        # now get the region
        planeDef.region = regionDef
        img = renderingEngine.renderCompressed(planeDef)
        regionImage = Image.open(StringIO.StringIO(img))
        h = hash_sha1()
        h.update(regionImage.tostring())
        hash_region = h.hexdigest()

        assert hash_cropped == hash_region
