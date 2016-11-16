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

    def testThumbnailFigure(self):

        sid = super(TestFigureExportScripts, self).getScript(thumbnail_figure)
        assert sid > 0

        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=self.root)
        project = self.make_project("thumbnailFigure-test", client=self.root)
        self.link(project, dataset, client=self.root)

        # make some tags
        tagIds = []
        for t in range(5):
            tag = omero.model.TagAnnotationI()
            tag.setTextValue(omero.rtypes.rstring("TestTag_%s" % t))
            tag = self.root.sf.getUpdateService().saveAndReturnObject(tag)
            tagIds.append(tag.id)

        # put some images in dataset
        imageIds = []
        for i in range(10):
            image = self.createTestImage(100, 100, 1, 1, 1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)

            # add tag
            tIndex = i % 5
            tag = omero.model.TagAnnotationI(tagIds[tIndex].val, False)
            self.link(image, tag, client=self.root)

        # run the script twice. First with all args...
        datasetIds = [omero.rtypes.rlong(dataset.id.val), ]
        argMap = {
            "IDs": omero.rtypes.rlist(datasetIds),
            "Data_Type": omero.rtypes.rstring("Dataset"),
            "Parent_ID": omero.rtypes.rlong(project.id.val),
            "Thumbnail_Size": omero.rtypes.rint(16),
            "Max_Columns": omero.rtypes.rint(6),
            "Format": omero.rtypes.rstring("PNG"),
            "Figure_Name": omero.rtypes.rstring("thumbnail-test"),
            "Tag_IDs": omero.rtypes.rlist(tagIds)
        }
        fileAnnot1 = runScript(client, sid, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(client, fileAnnot1, True, parentType="Dataset")
        checkFileAnnotation(client, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, sid, args, "File_Annotation")
        args = {"Data_Type": omero.rtypes.rstring(
            "Dataset"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot4 = runScript(client, sid, args, "File_Annotation")

        # should have no annotation
        checkFileAnnotation(client, fileAnnot3, False)
        checkFileAnnotation(client, fileAnnot4, False)

    def testSplitViewFigure(self):

        sid = super(TestFigureExportScripts, self).getScript(split_view_figure)
        assert sid > 0

        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("thumbnailFigure-test", client=self.root)
        project = self.make_project("thumbnailFigure-test", client=self.root)
        self.link(project, dataset, client=self.root)

        # put some images in dataset
        imageIds = []
        for i in range(5):
            image = self.createTestImage(256, 200, 5, 4, 1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0': omero.rtypes.rstring("DAPI"),
                                       '1': omero.rtypes.rstring("GFP"),
                                       '2': omero.rtypes.rstring("Red"),
                                       '3': omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rlong(255)
        red = omero.rtypes.rlong(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0': blue, '1': blue, '3': red})
        argMap = {
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
        fileAnnot1 = runScript(client, sid, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "Merged_Colours": mrgdColoursMap,
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest")}
        fileAnnot2 = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(client, fileAnnot1, True)
        checkFileAnnotation(client, fileAnnot2, True)

        # Run the script with invalid args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, sid, args, "File_Annotation")

        checkFileAnnotation(client, fileAnnot3, False)

    def testRoiFigure(self):

        sid = super(TestFigureExportScripts, self).getScript(roi_figure)
        assert sid > 0

        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("roiFig-test", client=self.root)
        project = self.make_project("roiFig-test", client=self.root)
        self.link(project, dataset, client=self.root)

        # put some images in dataset
        imageIds = []
        for i in range(5):
            image = self.createTestImage(256, 200, 5, 4, 1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)
            addRectangleRoi(self.root.sf.getUpdateService(),
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
        argMap = {
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
        fileAnnot1 = runScript(client, sid, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(client, fileAnnot1, True)
        checkFileAnnotation(client, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, sid, args, "File_Annotation")

        checkFileAnnotation(client, fileAnnot3, False)

    def testMovieRoiFigure(self):

        sid = super(TestFigureExportScripts, self).getScript(movie_ROI_figure)
        assert sid > 0

        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("movieRoiFig-test", client=self.root)
        project = self.make_project("movieRoiFig-test", client=self.root)
        self.link(project, dataset, client=self.root)

        # put some images in dataset
        imageIds = []
        for i in range(5):
            image = self.createTestImage(256, 256, 10, 3, 1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)

            # add roi -   x, y, width, height
            addRectangleRoi(self.root.sf.getUpdateService(),
                            50 + (i * 10), 100 - (i * 10),
                            50 + (i * 5), 100 - (i * 5), image.id.val)

        # run the script twice. First with all args...
        argMap = {
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
        fileAnnot1 = runScript(client, sid, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(client, fileAnnot1, True)
        checkFileAnnotation(client, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, sid, args, "File_Annotation")

        checkFileAnnotation(client, fileAnnot3, False)

    def testMovieFigure(self):

        sid = super(TestFigureExportScripts, self).getScript(movie_figure)
        assert sid > 0

        client = self.root

        # create several test images in a dataset
        dataset = self.make_dataset("movieFig-test", client=self.root)
        project = self.make_project("movieFig-test", client=self.root)
        self.link(project, dataset, client=self.root)

        # put some images in dataset
        imageIds = []
        for i in range(5):
            image = self.createTestImage(256, 256, 5, 3, 20)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)

        # run the script twice. First with all args...
        red = omero.rtypes.rint(16711680)
        tIndexes = [omero.rtypes.rint(0), omero.rtypes.rint(1),
                    omero.rtypes.rint(5), omero.rtypes.rint(10),
                    omero.rtypes.rint(15)]
        argMap = {
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
        fileAnnot1 = runScript(client, sid, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "T_Indexes": omero.rtypes.rlist(tIndexes), }
        fileAnnot2 = runScript(client, sid, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(client, fileAnnot1, True)
        checkFileAnnotation(client, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, sid, args, "File_Annotation")

        checkFileAnnotation(client, fileAnnot3, False)


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
