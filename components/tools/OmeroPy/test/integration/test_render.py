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
from omero.testlib import ITest
import io

try:
    from PIL import Image  # see ticket:2597
except:  # pragma: nocover
    try:
        import Image  # see ticket:2597
    except:
        logging.error('No Pillow installed')


from numpy import asarray, array_equal


class TestRendering(ITest):

    def test_render_region(self):
        """
        Test attempts to compare a full image plane, cropped to a region, with
        a region retrieved from rendering engine.

        Uses PIL to convert compressed strings into 2D numpy arrays for
        cropping and comparison.
        """

        session = self.root.sf

        size_x = 4
        size_y = 3
        size_z = 1
        size_c = 1
        size_t = 1
        image = self.create_test_image(size_x, size_y, size_z, size_c, size_t)
        pixels_id = image.getPrimaryPixels().getId().getValue()

        rendering_engine = session.createRenderingEngine()
        rendering_engine.lookupPixels(pixels_id)
        if not rendering_engine.lookupRenderingDef(pixels_id):
            rendering_engine.resetDefaultSettings(save=True)
        rendering_engine.lookupRenderingDef(pixels_id)
        rendering_engine.load()

        # turn all channels on
        for i in range(size_c):
            rendering_engine.setActive(i, True)

        region_def = omero.romio.RegionDef()
        x = 0
        y = 0
        width = 2
        height = 2
        x2 = x + width
        y2 = y + height

        region_def.x = x
        region_def.y = y
        region_def.width = width
        region_def.height = height

        plane_def = omero.romio.PlaneDef()
        plane_def.z = long(0)
        plane_def.t = long(0)

        # First, get the full rendered plane...
        img = rendering_engine.renderCompressed(plane_def)  # compressed String
        full_image = Image.open(io.BytesIO(img))  # convert to numpy arr
        # 3D array, since each pixel is [r,g,b]
        img_array = asarray(full_image)

        # get the cropped image
        cropped = img_array[y:y2, x:x2, :]      # ... so we can crop to region

        # now get the region
        plane_def.region = region_def
        img = rendering_engine.renderCompressed(plane_def)
        region_image = Image.open(io.BytesIO(img))
        region_array = asarray(region_image)

        # compare the values of the arrays
        assert array_equal(cropped, region_array)
