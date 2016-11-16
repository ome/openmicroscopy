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
from test.integration.scriptstest.script import runScript
from test.integration.scriptstest.script import checkFileAnnotation


thumbnail_figure = "/omero/figure_scripts/Thumbnail_Figure.py"
split_view_figure = "/omero/figure_scripts/Split_View_Figure.py"
roi_figure = "/omero/figure_scripts/ROI_Split_Figure.py"
movie_figure = "/omero/figure_scripts/Movie_Figure.py"
movie_ROI_figure = "/omero/figure_scripts/Movie_ROI_Figure.py"


class TestFigureExportScripts(ScriptTest):

    @pytest.mark.parametrize('data_type', ["Dataset", "Image"])
    @pytest.mark.parametrize('all_parameters', [True, False])
    def testThumbnailFigure(self, data_type, all_parameters):

        sid = super(TestFigureExportScripts, self).getScript(thumbnail_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=client)

        # make some tags
        tagIds = []
        session = client.getSession()
        for t in range(5):
            tag = omero.model.TagAnnotationI()
            tag.setTextValue(omero.rtypes.rstring("TestTag_%s" % t))
            tag = session.getUpdateService().saveAndReturnObject(tag)
            tagIds.append(tag.id)

        # put some images in dataset
        imageIds = []
        for i in range(2):
            # x,y,z,c,t
            image = self.createTestImage(100, 100, 1, 1, 1, session)
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

            # add tag
            tIndex = i % 5
            tag = omero.model.TagAnnotationI(tagIds[tIndex].val, False)
            self.link(image, tag, client=client)

        # run the script twice. First with all args...
        datasetIds = [omero.rtypes.rlong(dataset.id.val)]
        ids = imageIds
        if data_type == "Dataset":
            ids = datasetIds
        if all_parameters:
            args = {
                "IDs": omero.rtypes.rlist(ids),
                "Data_Type": omero.rtypes.rstring(data_type),
                "Thumbnail_Size": omero.rtypes.rint(16),
                "Max_Columns": omero.rtypes.rint(6),
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("thumbnail-test"),
                "Tag_IDs": omero.rtypes.rlist(tagIds)
            }
        else:
            args = {
                "Data_Type": omero.rtypes.rstring(data_type),
                "IDs": omero.rtypes.rlist(ids)
            }
        ann = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to dataset and first image.
        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True, parentType=data_type)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def testSplitViewFigure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).getScript(split_view_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=client)
        project = self.make_project("thumbnailFigure-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        session = client.getSession()
        imageIds = []
        for i in range(2):
            image = self.createTestImage(256, 200, 5, 4, 1, session)
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0': omero.rtypes.rstring("DAPI"),
                                       '1': omero.rtypes.rstring("GFP"),
                                       '2': omero.rtypes.rstring("Red"),
                                       '3': omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rlong(255)
        red = omero.rtypes.rlong(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0': blue, '1': blue, '3': red})
        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "Z_Start": omero.rtypes.rint(0),
                "Z_End": omero.rtypes.rint(3),
                "Channel_Names": cNamesMap,
                "Split_Indexes": omero.rtypes.rlist(
                    [omero.rtypes.rint(1), omero.rtypes.rint(2)]),
                "Split_Panels_Grey": omero.rtypes.rbool(True),
                "Merged_Colours": mrgdColoursMap,
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
                "IDs": omero.rtypes.rlist(imageIds),
                "Merged_Colours": mrgdColoursMap,
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest")
            }
        ann = runScript(client, sid, args, "File_Annotation")

        # ...then with bare minimum args
        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def testRoiFigure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).getScript(roi_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("roiFig-test", client=client)
        project = self.make_project("roiFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        imageIds = []
        session = client.getSession()
        for i in range(2):
            image = self.createTestImage(256, 200, 5, 4, 1, session)
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)
            addRectangleRoi(session.getUpdateService(),
                            50 + (i * 10), 100 - (i * 10),
                            50 + (i * 5), 100 - (i * 5),
                            image.getId().getValue())

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0': omero.rtypes.rstring("DAPI"),
                                       '1': omero.rtypes.rstring("GFP"),
                                       '2': omero.rtypes.rstring("Red"),
                                       '3': omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rint(255)
        red = omero.rtypes.rint(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0': blue, '1': blue, '3': red})
        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "Channel_Names": cNamesMap,
                "Split_Indexes": omero.rtypes.rlist(
                    [omero.rtypes.rlong(1), omero.rtypes.rlong(2)]),
                "Split_Panels_Grey": omero.rtypes.rbool(True),
                "Merged_Colours": mrgdColoursMap,
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
                "IDs": omero.rtypes.rlist(imageIds)
            }

        ann = runScript(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def testMovieRoiFigure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).getScript(movie_ROI_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("movieRoiFig-test", client=client)
        project = self.make_project("movieRoiFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        imageIds = []
        session = client.getSession()
        for i in range(2):
            image = self.createTestImage(256, 256, 10, 3, 1, session)
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

            # add roi -   x, y, width, height
            addRectangleRoi(session.getUpdateService(),
                            50 + (i * 10), 100 - (i * 10),
                            50 + (i * 5), 100 - (i * 5), image.id.val)

        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
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
                "IDs": omero.rtypes.rlist(imageIds)
            }
        ann = runScript(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)

    @pytest.mark.parametrize('all_parameters', [True, False])
    def testMovieFigure(self, all_parameters):

        sid = super(TestFigureExportScripts, self).getScript(movie_figure)
        assert sid > 0

        client, user = self.new_client_and_user()

        # create several test images in a dataset
        dataset = self.make_dataset("movieFig-test", client=client)
        project = self.make_project("movieFig-test", client=client)
        self.link(project, dataset, client=client)

        # put some images in dataset
        session = client.getSession()
        imageIds = []
        for i in range(2):
            image = self.createTestImage(256, 256, 5, 3, 20, session)
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=client)

        # run the script twice. First with all args...
        red = omero.rtypes.rint(16711680)
        tIndexes = [omero.rtypes.rint(0), omero.rtypes.rint(1),
                    omero.rtypes.rint(5), omero.rtypes.rint(10),
                    omero.rtypes.rint(15)]

        if all_parameters:
            args = {
                "Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "T_Indexes": omero.rtypes.rlist(tIndexes),
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
                "IDs": omero.rtypes.rlist(imageIds),
                "T_Indexes": omero.rtypes.rlist(tIndexes)
            }
        ann = runScript(client, sid, args, "File_Annotation")

        c = self.new_client(user=user)
        checkFileAnnotation(c, ann, True)


def addRectangleRoi(updateService, x, y, width, height, imageId):
    """
    Adds a Rectangle (particle) to the current OMERO image, at point x, y.
    Uses the self.image (OMERO image) and self.updateService
    """
    # create an ROI, add the rectangle and save
    roi = omero.model.RoiI()
    roi.setImage(omero.model.ImageI(imageId, False))
    r = updateService.saveAndReturnObject(roi)

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
    updateService.saveAndReturnObject(rect)
