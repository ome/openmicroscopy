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
import pytest
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import run_script
from omero.cmd import Delete2

channel_offsets = "/omero/util_scripts/Channel_Offsets.py"
combine_images = "/omero/util_scripts/Combine_Images.py"
images_from_rois = "/omero/util_scripts/Images_From_ROIs.py"
dataset_to_plate = "/omero/util_scripts/Dataset_To_Plate.py"
move_annotations = "/omero/util_scripts/Move_Annotations.py"


class TestUtilScripts(ScriptTest):

    def test_channel_offsets(self):
        script_id = super(TestUtilScripts, self).get_script(channel_offsets)
        assert script_id > 0

        client = self.root

        image = self.create_test_image(100, 100, 2, 3, 4)    # x,y,z,c,t
        image_id = image.id.val
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
        assert offset_img.getValue().id.val > 0

    def test_combine_images(self):
        script_id = super(TestUtilScripts, self).get_script(combine_images)
        assert script_id > 0
        client = self.root

        image_ids = []
        for i in range(2):
            image = self.create_test_image(100, 100, 2, 3, 4)    # x,y,z,c,t
            image_ids.append(omero.rtypes.rlong(image.id.val))

        args = {
            "Data_Type": omero.rtypes.rstring("Image"),
            "IDs": omero.rtypes.rlist(image_ids)
        }
        combine_img = run_script(client, script_id, args, "Combined_Image")
        # check the result
        assert combine_img is not None
        assert combine_img.getValue().id.val > 0

    def test_images_from_rois(self):
        script_id = super(TestUtilScripts, self).get_script(images_from_rois)
        assert script_id > 0
        # root session is root.sf
        session = self.root.sf
        client = self.root

        size_x = 100
        size_y = 100
        image = self.create_test_image(size_x, size_y, 5, 1, 1)    # x,y,z,c,t
        image_id = image.id.val
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
        assert img_from_rois.getValue().id.val > 0

    def test_dataset_to_plate(self):
        script_id = super(TestUtilScripts, self).get_script(dataset_to_plate)
        assert script_id > 0
        # root session is root.sf
        session = self.root.sf
        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("dataset_to_plate-test", client=client)
        n = 10
        image_ids = []
        for i in range(n):
            # x,y,z,c,t
            image = self.create_test_image(100, 100, 1, 1, 1, session)
            self.link(dataset, image, client=client)
            image_ids.append(image.id.val)

        # run the script twice. First with all args...
        dataset_ids = [omero.rtypes.rlong(dataset.id.val)]
        args = {
            "Data_Type": omero.rtypes.rstring("Dataset"),
            "IDs": omero.rtypes.rlist(dataset_ids)
        }

        d_to_p = run_script(client, script_id, args, "New_Object")
        # check the result
        assert d_to_p is not None
        plate_id = d_to_p.getValue().id.val
        assert plate_id > 0
        qs = client.getSession().getQueryService()
        query = "select well from Well as well left outer join fetch well.plate \
                 as pt left outer join fetch well.wellSamples as ws left \
                 outer join fetch ws.image as img where well.plate.id = :oid"

        params = omero.sys.ParametersI()
        params.addLong('oid', omero.rtypes.rlong(plate_id))
        wells = qs.findAllByQuery(query, params)
        # check the plate
        assert len(wells) == n
        count = 0
        for w in wells:
            img = w.getWellSample(0).getImage()
            assert img is not None
            id = img.id.val
            if id in image_ids:
                count += 1

        assert count == n

    @pytest.mark.parametrize("remove", [True, False])
    @pytest.mark.parametrize("script_runner", ['user', 'admin'])
    def test_move_annotations(self, remove, script_runner):

        script_id = super(TestUtilScripts, self).get_script(move_annotations)
        assert script_id > 0

        # create new Admin and user in same group
        admin_client, admin = self.new_client_and_user(system=True)
        group = admin_client.sf.getAdminService().getEventContext().groupId
        client, user = self.new_client_and_user(group=group)
        user_id = user.id.val
        field_count = 2

        # User creates Plate and Adds annotations...
        plate = self.import_plates(client=client, fields=field_count)[0]

        well_ids = []
        for well in plate.copyWells():
            well_ids.append(well.id)
            for well_sample in well.copyWellSamples():
                image = well_sample.getImage()
                # Add annotations
                tag = omero.model.TagAnnotationI()
                tag.textValue = omero.rtypes.rstring("testTag")
                self.link(image, tag, client=client)
                comment = omero.model.CommentAnnotationI()
                comment.textValue = omero.rtypes.rstring("test Comment")
                self.link(image, comment, client=client)
                rating = omero.model.LongAnnotationI()
                rating.longValue = omero.rtypes.rlong(5)
                rating.ns = omero.rtypes.rstring(
                    omero.constants.metadata.NSINSIGHTRATING)
                self.link(image, rating, client=client)

        # Either the 'user' or 'root' will run the script
        if script_runner == 'user':
            script_runner_client = client
        else:
            script_runner_client = admin_client

        # Run script on each type, with/without removing Annotations
        for anntype in ('Tag', 'Comment', 'Rating'):
            args = {
                "Data_Type": omero.rtypes.rstring("Well"),
                "IDs": omero.rtypes.rlist(well_ids),
                "Annotation_Type": omero.rtypes.rstring(anntype),
                "Remove_Annotations_From_Images": omero.rtypes.rbool(remove)
            }
            message = run_script(script_runner_client, script_id,
                                 args, "Message")
            assert message.val == "Moved %s Annotations" % field_count

        # Check new links are owned by user
        # and remove annotations from Wells...
        query_service = client.getSession().getQueryService()
        query = ("select l from WellAnnotationLink as l"
                 " join fetch l.child as ann"
                 " join fetch l.details.owner as owner"
                 " where l.parent.id in (:ids)")
        params = omero.sys.ParametersI().addIds(well_ids)
        links = query_service.findAllByQuery(query, params)
        link_ids = [l.id.val for l in links]
        assert len(link_ids) == field_count * 3
        for l in links:
            assert l.getDetails().owner.id.val == user_id
        delete = Delete2(targetObjects={'WellAnnotationLink': link_ids})
        handle = client.sf.submit(delete)
        client.waitOnCmd(handle, loops=10, ms=500, failonerror=True,
                         failontimeout=False, closehandle=False)

        # Run again with 'All' annotations.
        args = {
            "Data_Type": omero.rtypes.rstring("Plate"),
            "IDs": omero.rtypes.rlist([plate.id]),
            "Annotation_Type": omero.rtypes.rstring("All"),
            "Remove_Annotations_From_Images": omero.rtypes.rbool(remove)
        }
        message = run_script(script_runner_client, script_id, args, "Message")
        # If we've been removing annotations above,
        # there will be None left to move
        if remove:
            expected = "No annotations moved. See info."
        else:
            expected = "Moved %s Annotations" % (field_count * 3)
        assert message.val == expected

        # Run again - None moved since Annotations are already on Well
        message = run_script(script_runner_client, script_id, args, "Message")
        assert message.val == "No annotations moved. See info."
