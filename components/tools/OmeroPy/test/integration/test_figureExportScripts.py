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
   Integration test for figure export scripts.

"""

import library as lib
import omero
import omero.scripts
from omero.gateway import BlitzGateway
import uuid
from omero import ApiUsageException

thumbnailFigurePath = "scripts/omero/figure_scripts/Thumbnail_Figure.py"
splitViewFigurePath = "scripts/omero/figure_scripts/Split_View_Figure.py"
roiFigurePath = "scripts/omero/figure_scripts/ROI_Split_Figure.py"
movieFigurePath = "scripts/omero/figure_scripts/Movie_Figure.py"
movieROIFigurePath = "scripts/omero/figure_scripts/Movie_ROI_Figure.py"


class TestFigureExportScripts(lib.ITest):

    def testThumbnailFigure(self):

        print "testThumbnailFigure"

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, thumbnailFigurePath)

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
        for i in range(50):
            image = self.createTestImage(100, 100, 1, 1, 1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(image.getId().getValue()))
            self.link(dataset, image, client=self.root)

            # add tag
            tIndex = i % 5
            tlink = omero.model.ImageAnnotationLinkI()
            tlink.child = omero.model.TagAnnotationI(tagIds[tIndex].val, False)
            tlink.parent = omero.model.ImageI(image.id.val, False)
            self.root.sf.getUpdateService().saveObject(tlink)

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
            "Tag_IDs": omero.rtypes.rlist(tagIds),
            # "showUntaggedImages": omero.rtypes.rbool(True),
        }
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self, fileAnnot1, True, parentType="Dataset")
        checkFileAnnotation(self, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")
        args = {"Data_Type": omero.rtypes.rstring(
            "Dataset"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot4 = runScript(client, scriptId, args, "File_Annotation")

        # should have no annotation
        checkFileAnnotation(self, fileAnnot3, False)
        checkFileAnnotation(self, fileAnnot4, False)

    def testSplitViewFigure(self):

        print "testSplitViewFigure"

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, splitViewFigurePath)

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
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "Merged_Colours": mrgdColoursMap,
                "Format": omero.rtypes.rstring("PNG"),
                "Figure_Name": omero.rtypes.rstring("splitViewTest")}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self, fileAnnot1, True)
        checkFileAnnotation(self, fileAnnot2, True)

        # Run the script with invalid args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self, fileAnnot3, False)

    def testRoiFigure(self):

        print "testRoiFigure"

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, roiFigurePath)

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
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self, fileAnnot1, True)
        checkFileAnnotation(self, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self, fileAnnot3, False)

    def testMovieRoiFigure(self):

        print "testMovieRoiFigure"

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, movieROIFigurePath)

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
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self, fileAnnot1, True)
        checkFileAnnotation(self, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self, fileAnnot3, False)

    def testMovieFigure(self):

        print "testMovieFigure"

        # root session is root.sf
        session = self.root.sf
        client = self.root

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, movieFigurePath)

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
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"),
                "IDs": omero.rtypes.rlist(imageIds),
                "T_Indexes": omero.rtypes.rlist(tIndexes), }
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self, fileAnnot1, True)
        checkFileAnnotation(self, fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring(
            "Image"), "IDs": omero.rtypes.rlist(omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self, fileAnnot3, False)


def runScript(client, scriptId, argMap, returnKey=None):

    scriptService = client.sf.getScriptService()
    proc = scriptService.runScript(scriptId, argMap, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000):  # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)

    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        print "Script generated StdOut in file:", origFile.getId().getValue()
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        # But, we still get stderr from EMAN2 import (duplicate numpy etc.)
        print "Script generated StdErr in file:", origFile.getId().getValue()
    if returnKey and returnKey in results:
        return results[returnKey]


def uploadScript(scriptService, scriptPath):
    _uuid = str(uuid.uuid4())

    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    try:
        scriptId = scriptService.uploadOfficialScript(
            "/%s/%s" % (_uuid, scriptPath), scriptText)
    except ApiUsageException:
        raise  # The next line will never be run!
        scriptId = editScript(scriptService, scriptPath)
    return scriptId


def editScript(scriptService, scriptPath):
    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    # need the script Original File to edit
    # scripts = scriptService.getScripts()
    # if not scriptPath.startswith("/"): scriptPath =  "/" + scriptPath
    # namedScripts =\
    #     [s for s in scripts if s.path.val + s.name.val == scriptPath]
    # script = namedScripts[-1]
    script = getScript(scriptService, scriptPath)
    print "Editing script:", scriptPath
    scriptService.editScript(script, scriptText)
    return script.id.val


def getScript(scriptService, scriptPath):

    scripts = scriptService.getScripts()     # returns list of OriginalFiles

    for s in scripts:
        print s.id.val, s.path.val + s.name.val

    # make sure path starts with a slash.
    # ** If you are a Windows client - will need to convert all path separators
    #    to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath = "/" + scriptPath

    namedScripts = [
        s for s in scripts if s.path.val + s.name.val == scriptPath]

    if len(namedScripts) == 0:
        print "Didn't find any scripts with specified path: %s" % scriptPath
        return

    if len(namedScripts) > 1:
        print "Found more than one script with specified path: %s" % scriptPath

    return namedScripts[0]


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
    rect = omero.model.RectI()
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


def checkFileAnnotation(self, fileAnnotation, hasFileAnnotation=True,
                        parentType="Image", isLinked=True, client=None):
    """
    Check validity of file annotation. If hasFileAnnotation, check the size,
    name and number of objects linked to the original file.
    """
    if hasFileAnnotation:
        assert fileAnnotation is not None
        assert fileAnnotation.val._file._size._val > 0
        assert fileAnnotation.val._file._name._val is not None

        if client is None:
            client = self.root
        conn = BlitzGateway(client_obj=client)
        faWrapper = conn.getObject("FileAnnotation", fileAnnotation.val.id.val)
        nLinks = sum(1 for i in faWrapper.getParentLinks(parentType))
        if isLinked:
            assert nLinks == 1
        else:
            assert nLinks == 0
    else:
        assert fileAnnotation is None
