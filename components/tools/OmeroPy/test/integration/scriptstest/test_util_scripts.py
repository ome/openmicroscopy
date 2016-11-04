#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
   Integration test for util scripts.
"""

import omero
import omero.scripts
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import runScript

channel_offsets = "scripts/omero/util_scripts/Channel_Offsets.py"
combine_images = "scripts/omero/util_scripts/Combine_Images.py"
images_from_rois = "scripts/omero/util_scripts/Images_From_ROIs.py"


class TestUtilScripts(ScriptTest):

    def testChannelOffsets(self):
        scriptId = super(TestUtilScripts, self).upload(channel_offsets)

        client = self.root

        image = self.createTestImage(100, 100, 2, 3, 4)    # x,y,z,c,t
        image_id = image.getId().getValue()
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image_id))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds),
            "Channel1_X_shift": omero.rtypes.rint(1),
            "Channel1_Y_shift": omero.rtypes.rint(1),
            "Channel2_X_shift": omero.rtypes.rint(2),
            "Channel2_Y_shift": omero.rtypes.rint(2),
            "Channel3_X_shift": omero.rtypes.rint(3),
            "Channel3_Y_shift": omero.rtypes.rint(3),
        }
        offset_img = runScript(client, scriptId, argMap, "Image")
        # check the result
        assert offset_img is not None
        assert offset_img.val.id.val > 0

    def testCombineImages(self):
        scriptId = super(TestUtilScripts, self).upload(combine_images)

        client = self.root

        image = self.createTestImage(100, 100, 2, 3, 4)    # x,y,z,c,t
        image_id = image.getId().getValue()
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image_id))
        image = self.createTestImage(100, 100, 2, 3, 4)    # x,y,z,c,t
        image_id = image.getId().getValue()
        imageIds.append(omero.rtypes.rlong(image_id))
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds)
        }
        combine_img = runScript(client, scriptId, argMap, "Combined_Image")
        # check the result
        assert combine_img is not None
        assert combine_img.val.id.val > 0

    def testImagesFromROIs(self):
        scriptId = super(TestUtilScripts, self).upload(images_from_rois)

        # root session is root.sf
        session = self.root.sf
        client = self.root

        sizeX = 100
        sizeY = 100
        image = self.createTestImage(sizeX, sizeY, 5, 1, 1)    # x,y,z,c,t
        image_id = image.getId().getValue()
        imageIds = []
        imageIds.append(omero.rtypes.rlong(image_id))

        # Add rectangle
        roi = omero.model.RoiI()
        roi.setImage(omero.model.ImageI(image_id, False))
        rect = omero.model.RectangleI()
        rect.x = omero.rtypes.rdouble(0)
        rect.y = omero.rtypes.rdouble(0)
        rect.width = omero.rtypes.rdouble(sizeX / 2)
        rect.height = omero.rtypes.rdouble(sizeY / 2)
        rect.theZ = omero.rtypes.rint(0)
        rect.theT = omero.rtypes.rint(0)
        session.getUpdateService().saveAndReturnObject(roi)
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(imageIds),
            "Make_Image_Stack": omero.rtypes.rbool(True)
        }
        img_from_rois = runScript(client, scriptId, argMap, "Result")
        # check the result
        assert img_from_rois is not None
