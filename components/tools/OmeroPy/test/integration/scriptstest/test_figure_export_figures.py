#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2016 University of Dundee & Open Microscopy Environment.
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
   Integration test for figure export scripts.
"""

import pytest
import omero
from test.integration.scriptstest.script import ScriptTest
from test.integration.scriptstest.script import run_script
from test.integration.scriptstest.script import check_file_annotation


thumbnail_figure = "/omero/figure_scripts/Thumbnail_Figure.py"
split_view_figure = "/omero/figure_scripts/Split_View_Figure.py"
roi_figure = "/omero/figure_scripts/ROI_Split_Figure.py"
movie_figure = "/omero/figure_scripts/Movie_Figure.py"
movie_roi_figure = "/omero/figure_scripts/Movie_ROI_Figure.py"


class TestFigureExportScripts(ScriptTest):

    @pytest.mark.parametrize('data_type', ["Dataset", "Image"])
    @pytest.mark.parametrize('all_parameters', [True, False])
    def test_thumbnail_figure(self, data_type, all_parameters):

        sid = super(TestFigureExportScripts, self).get_script(thumbnail_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=client)

        # make some tags
        tag_ids = []
        session = client.getSession()
        for t in range(5):
            tag = omero.model.TagAnnotationI()
            tag.setTextValue(omero.rtypes.rstring("TestTag_%s" % t))
            tag = session.getUpdateService().saveAndReturnObject(tag)
            tag_ids.append(tag.id)

        # put some images in dataset
        image_ids = []
        for i in range(2):
            # x,y,z,c,t
            image = self.createTestImage(100, 100, 1, 1, 1, session)
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

            # add tag
            t = i % 5
            tag = omero.model.TagAnnotationI(tag_ids[t].val, False)
            self.link(image, tag, client=client)

        # run the script twice. First with all args...
        dataset_ids = [omero.rtypes.rlong(dataset.id.val)]
        ids = image_ids
        if data_type == "Dataset":
            ids = dataset_ids
        if all_parameters:
            args = {
                "IDs": omero.rtypes.rlist(ids),
                "Data_Type": omero.rtypes.rstring(data_type),
                "Thumbnail_Size": omero.rtypes.rint(16),
                "Max_Columns": omero.rtypes.rint(6),
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("thumbnail-test"),
                "Tag_IDs": omero.rtypes.rlist(tag_ids)
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring(data_type),
                "IDs": omero.rtypes.rlist(ids)
            }
        ann = run_script(client, sid, args, "File_Annotation")

        # should have figures attached to dataset and first image.
        c = self.new_client(user=user)
        check_file_annotation(c, ann, parent_type=data_type)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def test_split_view_figure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).get_script(split_view_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=client)
        project = self.make_project("thumbnailFigure-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        session = client.getSession()
        image_ids = []
        for i in range(2):
            image = self.createTestImage(256, 200, 5, 4, 1, session)
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

        c_names_map = omero.rtypes.rmap({'0': omero.rtypes.rstring("DAPI"),
                                         '1': omero.rtypes.rstring("GFP"),
                                         '2': omero.rtypes.rstring("Red"),
                                         '3': omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rlong(255)
        red = omero.rtypes.rlong(16711680)
        mrgd_colours_map = omero.rtypes.rmap({'0': blue, '1': blue, '3': red})
        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "Z_Start": omero.rtypes.rint(0),
                "Z_End": omero.rtypes.rint(3),
                "Channel_Names": c_names_map,
                "Split_Indexes": omero.rtypes.rlist(
                    [omero.rtypes.rint(1), omero.rtypes.rint(2)]),
                "Split_Panels_Grey": omero.rtypes.rbool(True),
                "Merged_Colours": mrgd_colours_map,
                "Merged_Names": omero.rtypes.rbool(True),
                "Width": omero.rtypes.rint(200),
                "Height": omero.rtypes.rint(200),
                "Image_Labels": omero.rtypes.rstring("Datasets"),
                "Algorithm": omero.rtypes.rstring("Mean Intensity"),
                "Stepping": omero.rtypes.rint(1),
                # will be ignored since no pixelsize set
                "Scalebar": omero.rtypes.rint(10),
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest"),
                # "overlayColour": red,
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "Merged_Colours": mrgd_colours_map,
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest")
            }
        ann = run_script(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        check_file_annotation(c, ann)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def test_roi_figure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).get_script(roi_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("roiFig-test", client=client)
        project = self.make_project("roiFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        image_ids = []
        session = client.getSession()
        for i in range(2):
            image = self.createTestImage(256, 200, 5, 4, 1, session)
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)
            add_rectangle_roi(session.getUpdateService(),
                              50 + (i * 10), 100 - (i * 10),
                              50 + (i * 5), 100 - (i * 5),
                              image.getId().getValue())

        c_names_map = omero.rtypes.rmap({'0': omero.rtypes.rstring("DAPI"),
                                         '1': omero.rtypes.rstring("GFP"),
                                         '2': omero.rtypes.rstring("Red"),
                                         '3': omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rint(255)
        red = omero.rtypes.rint(16711680)
        mrgd_colours_map = omero.rtypes.rmap({'0': blue, '1': blue, '3': red})
        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "Channel_Names": c_names_map,
                "Split_Indexes": omero.rtypes.rlist(
                    [omero.rtypes.rlong(1), omero.rtypes.rlong(2)]),
                "Split_Panels_Grey": omero.rtypes.rbool(True),
                "Merged_Colours": mrgd_colours_map,
                "Merged_Names": omero.rtypes.rbool(True),
                "Width": omero.rtypes.rint(200),
                "Height": omero.rtypes.rint(200),
                "Image_Labels": omero.rtypes.rstring("Datasets"),
                "Algorithm": omero.rtypes.rstring("Mean Intensity"),
                "Stepping": omero.rtypes.rint(1),
                # will be ignored since no pixelsize set
                "Scalebar": omero.rtypes.rint(10),
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest"),
                "Overlay_Colour": omero.rtypes.rstring("Red"),
                "ROI_Zoom": omero.rtypes.rfloat(3),
                # won't be found - but should still work
                "ROI_Label": omero.rtypes.rstring("fakeTest"),
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids)
            }

        ann = run_script(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        check_file_annotation(c, ann)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def test_movie_roi_figure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).get_script(movie_roi_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("movieRoiFig-test", client=client)
        project = self.make_project("movieRoiFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        image_ids = []
        session = client.getSession()
        for i in range(2):
            image = self.createTestImage(256, 256, 10, 3, 1, session)
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

            # add roi -   x, y, width, height
            add_rectangle_roi(session.getUpdateService(),
                              50 + (i * 10), 100 - (i * 10),
                              50 + (i * 5), 100 - (i * 5), image.id.val)

        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "ROI_Zoom": omero.rtypes.rfloat(3),
                "Max_Columns": omero.rtypes.rint(10),
                "Resize_Images": omero.rtypes.rbool(True),
                "Width": omero.rtypes.rint(200),
                "Height": omero.rtypes.rint(200),
                "Image_Labels": omero.rtypes.rstring("Datasets"),
                "Show_ROI_Duration": omero.rtypes.rbool(True),
                # will be ignored since no pixelsize set
                "Scalebar": omero.rtypes.rint(10),
                # will be ignored since no pixelsize set
                "Scalebar_Colour": omero.rtypes.rstring("White"),
                # won't be found - but should still work
                "Roi_Selection_Label": omero.rtypes.rstring("fakeTest"),
                "Algorithm": omero.rtypes.rstring("Mean Intensity"),
                "Figure_Name": omero.rtypes.rstring("movieROITest")
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids)
            }
        ann = run_script(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        check_file_annotation(c, ann)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def test_movie_figure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).get_script(movie_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("movieFig-test", client=client)
        project = self.make_project("movieFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        session = client.getSession()
        image_ids = []
        for i in range(2):
            image = self.createTestImage(256, 256, 5, 3, 20, session)
            image_ids.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

        red = omero.rtypes.rint(16711680)
        t_indexes = [omero.rtypes.rint(0), omero.rtypes.rint(1),
                     omero.rtypes.rint(5), omero.rtypes.rint(10),
                     omero.rtypes.rint(15)]

        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "T_Indexes": omero.rtypes.rlist(t_indexes),
                "Z_Start": omero.rtypes.rint(1),
                "Z_End": omero.rtypes.rint(3),
                "Width": omero.rtypes.rint(150),
                "Height": omero.rtypes.rint(150),
                "Image_Labels": omero.rtypes.rstring("Datasets"),
                "Algorithm": omero.rtypes.rstring("Mean Intensity"),
                "Stepping": omero.rtypes.rint(1),
                "Scalebar": omero.rtypes.rint(10),
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("movieFigureTest"),
                "TimeUnits": omero.rtypes.rstring("MINS"),
                "Overlay_Colour": red,
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(image_ids),
                "T_Indexes": omero.rtypes.rlist(t_indexes)
            }
        ann = run_script(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        check_file_annotation(c, ann)


def add_rectangle_roi(update_service, x, y, width, height, image_id):
    """
    Adds a Rectangle (particle) to the current OMERO image, at point x, y.
    """
    # create an ROI, add the rectangle and save
    roi = omero.model.RoiI()
    roi.setImage(omero.model.ImageI(image_id, False))
    r = update_service.saveAndReturnObject(roi)

    # create and save a rectangle shape
    rect = omero.model.RectangleI()
    rect.x = omero.rtypes.rdouble(x)
    rect.y = omero.rtypes.rdouble(y)
    rect.width = omero.rtypes.rdouble(width)
    rect.height = omero.rtypes.rdouble(height)
    rect.theZ = omero.rtypes.rint(0)
    rect.theT = omero.rtypes.rint(0)
    rect.locked = omero.rtypes.rbool(True)        # don't allow editing
    rect.strokeWidth = omero.model.LengthI()
    rect.strokeWidth.setValue(1.0)
    rect.strokeWidth.setUnit(omero.model.enums.UnitsLength.POINT)

    # link the rectangle to the ROI and save it
    rect.setRoi(r)
    r.addShape(rect)
    update_service.saveAndReturnObject(rect)
