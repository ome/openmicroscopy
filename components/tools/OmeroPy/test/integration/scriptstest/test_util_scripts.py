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
from test.integration.scriptstest.script import run_script

channel_offsets = "/omero/util_scripts/Channel_Offsets.py"
combine_images = "/omero/util_scripts/Combine_Images.py"
images_from_rois = "/omero/util_scripts/Images_From_ROIs.py"


class TestUtilScripts(ScriptTest):

    def test_channel_offsets(self):
        script_id = super(TestUtilScripts, self).get_script(channel_offsets)
        assert script_id > 0

        client = self.root

        image = self.createTestImage(100, 100, 2, 3, 4)    # x,y,z,c,t
        image_id = image.getId().getValue()
        image_ids = []
        image_ids.append(omero.rtypes.rlong(image_id))
        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids),
            "Channel1_X_shift": omero.rtypes.rint(1),
            "Channel1_Y_shift": omero.rtypes.rint(1),
            "Channel2_X_shift": omero.rtypes.rint(2),
            "Channel2_Y_shift": omero.rtypes.rint(2),
            "Channel3_X_shift": omero.rtypes.rint(3),
            "Channel3_Y_shift": omero.rtypes.rint(3),
        }
        offset_img = run_script(client, script_id, args, "Image")
        # check the result
        assert offset_img is not None
        assert offset_img.getValue().getId().getValue() > 0

    def test_combine_images(self):
        script_id = super(TestUtilScripts, self).get_script(combine_images)
        assert script_id > 0
        client = self.root

        image_ids = []
        for i in range(2):
            image = self.createTestImage(100, 100, 2, 3, 4)    # x,y,z,c,t
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))

        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids)
        }
        combine_img = run_script(client, script_id, args, "Combined_Image")
        # check the result
        assert combine_img is not None
        assert combine_img.getValue().getId().getValue() > 0

    def test_images_from_rois(self):
        script_id = super(TestUtilScripts, self).get_script(images_from_rois)
        assert script_id > 0
        # root session is root.sf
        session = self.root.sf
        client = self.root

        size_x = 100
        size_y = 100
        image = self.createTestImage(size_x, size_y, 5, 1, 1)    # x,y,z,c,t
        image_id = image.getId().getValue()
        image_ids = []
        image_ids.append(omero.rtypes.rlong(image_id))

        # Add rectangle
        roi = omero.model.RoiI()
        roi.setImage(omero.model.ImageI(image_id, False))
        rect = omero.model.RectangleI()
        rect.x = omero.rtypes.rdouble(0)
        rect.y = omero.rtypes.rdouble(0)
        rect.width = omero.rtypes.rdouble(size_x / 2)
        rect.height = omero.rtypes.rdouble(size_y / 2)
        roi.addShape(rect)
        session.getUpdateService().saveAndReturnObject(roi)
        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids),
            "Make_Image_Stack": omero.rtypes.rbool(True)
        }
        img_from_rois = run_script(client, script_id, args, "Result")
        # check the result
        assert img_from_rois is not None
        assert img_from_rois.getValue().getId().getValue() > 0
