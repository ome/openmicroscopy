#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for getting thumbnails between members of groups.
   Testing permissions and thumbnail service on a running server.

   dir(self)
   'assertAlmostEqual', 'assertAlmostEquals', 'assertEqual', 'assertEquals', 'assertFalse', 'assertNotAlmostEqual', 'assertNotAlmostEquals', 
   'assertNotEqual', 'assertNotEquals', 'assertRaises', 'assertTrue', 'assert_', 'client', 'countTestCases', 'debug', 'defaultTestResult', 
   'fail', 'failIf', 'failIfAlmostEqual', 'failIfEqual', 'failUnless', 'failUnlessAlmostEqual', 'failUnlessEqual', 'failUnlessRaises', 
   'failureException', 'id', 'login_args', 'new_user', 'query', 'root', 'run', 'setUp', 'sf', 'shortDescription', 'tearDown', 'testfoo', 
   'tmpfile', 'tmpfiles', 'update'

   PYTHONPATH=/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/site-packages/:/opt/Ice-3.3.1/python:.:test:build/lib ICE_CONFIG=/Users/will/Documents/workspace/Omero/etc/ice.config python test/integration/thumbnailPerms.py

"""
import unittest, time
import test.integration.library as lib
import omero
import omero.scripts
from omero.rtypes import *
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero.util.script_utils as scriptUtil
from omero.gateway import BlitzGateway
import omero_ext.uuid as uuid # see ticket:3774
from omero import ApiUsageException

from numpy import *

thumbnailFigurePath = "scripts/omero/figure_scripts/Thumbnail_Figure.py"
splitViewFigurePath = "scripts/omero/figure_scripts/Split_View_Figure.py"
roiFigurePath = "scripts/omero/figure_scripts/ROI_Split_Figure.py"
movieFigurePath = "scripts/omero/figure_scripts/Movie_Figure.py"
movieROIFigurePath = "scripts/omero/figure_scripts/Movie_ROI_Figure.py"


class TestFigureExportScripts(lib.ITest):

    def testThumbnailFigure(self):

        print "testThumbnailFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        services = {}

        services["containerService"] = session.getContainerService()
        services["renderingEngine"] = session.createRenderingEngine()
        services["queryService"] = session.getQueryService()
        services["pixelsService"] = session.getPixelsService()
        services["rawPixelStore"] = session.createRawPixelsStore()
        services["updateService"] = session.getUpdateService()

        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, thumbnailFigurePath)

        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("thumbnailFigure-test")
        dataset = services["updateService"].saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("thumbnailFigure-test")
        project = services["updateService"].saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        services["updateService"].saveAndReturnObject(link)
        # make some tags
        tagIds = []
        for t in range(5):
            tag = omero.model.TagAnnotationI()
            tag.setTextValue(omero.rtypes.rstring("TestTag_%s"%t))
            tag = services["updateService"].saveAndReturnObject(tag)
            tagIds.append(tag.id)
        # put some images in dataset
        imageIds = []
        for i in range(50):
            imageId = self.createTestImage(100, 100, 1, 1, 1).getId().getValue()  # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            services["updateService"].saveAndReturnObject(dlink)
            # add tag
            tIndex = i%5
            tlink = omero.model.ImageAnnotationLinkI()
            tlink.child = omero.model.TagAnnotationI(tagIds[tIndex].val, False)
            tlink.parent = omero.model.ImageI(imageId, False)
            services["updateService"].saveObject(tlink)
            
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
            #"showUntaggedImages": omero.rtypes.rbool(True),
            }
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")
        
        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image.
        checkFileAnnotation(self,fileAnnot1, True, parentType="Dataset")
        checkFileAnnotation(self,fileAnnot2, True)
        
        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}        
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")
        args = {"Data_Type": omero.rtypes.rstring("Dataset"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}
        fileAnnot4 = runScript(client, scriptId, args, "File_Annotation")
    
        # should have no annotation
        checkFileAnnotation(self,fileAnnot3, False)
        checkFileAnnotation(self,fileAnnot4, False)
        
    def testSplitViewFigure(self):

        print "testSplitViewFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        services = {}
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        updateService = session.getUpdateService()
        
        services["containerService"] = session.getContainerService()
        services["renderingEngine"] = renderingEngine
        services["queryService"] = queryService
        services["pixelsService"] = pixelsService
        services["rawPixelStore"] = rawPixelStore
        
        # upload script 
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, splitViewFigurePath)

        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("splitViewFig-test")
        dataset = updateService.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("splitViewFig-test")
        project = updateService.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        updateService.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = self.createTestImage(256,200,5,4,1).getId().getValue()    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            updateService.saveAndReturnObject(dlink)

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0':omero.rtypes.rstring("DAPI"),
            '1':omero.rtypes.rstring("GFP"), 
            '2':omero.rtypes.rstring("Red"), 
            '3':omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rlong(255)
        red = omero.rtypes.rlong(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0':blue, '1':blue, '3':red})
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
           "IDs": omero.rtypes.rlist(imageIds),
           "Z_Start": omero.rtypes.rint(0),
           "Z_End": omero.rtypes.rint(3),   
           "Channel_Names": cNamesMap,
           "Split_Indexes": omero.rtypes.rlist([omero.rtypes.rint(1),omero.rtypes.rint(2)]),
           "Split_Panels_Grey": omero.rtypes.rbool(True),
           "Merged_Colours": mrgdColoursMap,
           "Merged_Names": omero.rtypes.rbool(True),
           "Width": omero.rtypes.rint(200),
           "Height": omero.rtypes.rint(200),
           "Image_Labels": omero.rtypes.rstring("Datasets"),
           "Algorithm": omero.rtypes.rstring("Mean Intensity"),
           "Stepping": omero.rtypes.rint(1),
           "Scalebar": omero.rtypes.rint(10), # will be ignored since no pixelsize set
           "Format": omero.rtypes.rstring("PNG"),
           "Figure_Name": omero.rtypes.rstring("splitViewTest"),
           #"overlayColour": red,
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
        checkFileAnnotation(self,fileAnnot1, True)
        checkFileAnnotation(self,fileAnnot2, True)
        
        # Run the script with invalid args
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")
        
        checkFileAnnotation(self,fileAnnot3, False)
        
    
    def testRoiFigure(self):

        print "testRoiFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        services = {}
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        updateService = session.getUpdateService()
        
        services["containerService"] = session.getContainerService()
        services["renderingEngine"] = renderingEngine
        services["queryService"] = queryService
        services["pixelsService"] = pixelsService
        services["rawPixelStore"] = rawPixelStore
        
        # upload script 
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, roiFigurePath)

        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("roiFig-test")
        dataset = updateService.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("roiFig-test")
        project = updateService.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        updateService.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = self.createTestImage(256,256,10,3,1).getId().getValue()    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            updateService.saveAndReturnObject(dlink)
            # add roi
            addRectangleRoi(updateService, 50 + (i*10), 100 - (i*10), 50+(i*5), 100-(i*5), imageId) # x, y, width, height

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0':omero.rtypes.rstring("DAPI"),
            '1':omero.rtypes.rstring("GFP"), 
            '2':omero.rtypes.rstring("Red"), 
            '3':omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rint(255)
        red = omero.rtypes.rint(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0':blue, '1':blue, '3':red})
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
           "IDs": omero.rtypes.rlist(imageIds),
           "Channel_Names": cNamesMap,
           "Split_Indexes": omero.rtypes.rlist([omero.rtypes.rlong(1),omero.rtypes.rlong(2)]),
           "Split_Panels_Grey": omero.rtypes.rbool(True),
           "Merged_Colours": mrgdColoursMap,
           "Merged_Names": omero.rtypes.rbool(True),
           "Width": omero.rtypes.rint(200),
           "Height": omero.rtypes.rint(200),
           "Image_Labels": omero.rtypes.rstring("Datasets"),
           "Algorithm": omero.rtypes.rstring("Mean Intensity"),
           "Stepping": omero.rtypes.rint(1),
           "Scalebar": omero.rtypes.rint(10), # will be ignored since no pixelsize set
           "Format": omero.rtypes.rstring("PNG"),
           "Figure_Name": omero.rtypes.rstring("splitViewTest"),
           "Overlay_Colour": omero.rtypes.rstring("Red"),
           "ROI_Zoom":omero.rtypes.rfloat(3),
           "ROI_Label":omero.rtypes.rstring("fakeTest"), # won't be found - but should still work
           }
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")
        
        # should have figures attached to project and first image. 
        checkFileAnnotation(self,fileAnnot1, True)
        checkFileAnnotation(self,fileAnnot2, True)
        
        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")
        
        checkFileAnnotation(self,fileAnnot3, False)        
    
    def testMovieRoiFigure(self):

        print "testMovieRoiFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        services = {}
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        updateService = session.getUpdateService()

        services["containerService"] = session.getContainerService()
        services["renderingEngine"] = renderingEngine
        services["queryService"] = queryService
        services["pixelsService"] = pixelsService
        services["rawPixelStore"] = rawPixelStore

        # upload script 
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, movieROIFigurePath)

        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("roiFig-test")
        dataset = updateService.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("roiFig-test")
        project = updateService.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        updateService.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = self.createTestImage(256,256,10,3,1).getId().getValue()    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            updateService.saveAndReturnObject(dlink)
            # add roi
            addRectangleRoi(updateService, 50 + (i*10), 100 - (i*10), 50+(i*5), 100-(i*5), imageId) # x, y, width, height

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0':omero.rtypes.rstring("DAPI"),
            '1':omero.rtypes.rstring("GFP"), 
            '2':omero.rtypes.rstring("Red"), 
            '3':omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rint(255)
        red = omero.rtypes.rint(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0':blue, '1':blue, '3':red})
        argMap = {
            "Data_Type": omero.rtypes.rstring("Image"),
           "IDs": omero.rtypes.rlist(imageIds),
           "ROI_Zoom":omero.rtypes.rfloat(3),
           "Max_Columns": omero.rtypes.rint(10),
           "Resize_Images": omero.rtypes.rbool(True),
           "Width": omero.rtypes.rint(200),
           "Height": omero.rtypes.rint(200),
           "Image_Labels": omero.rtypes.rstring("Datasets"),
           "Show_ROI_Duration": omero.rtypes.rbool(True),
           "Scalebar": omero.rtypes.rint(10), # will be ignored since no pixelsize set
           "Scalebar_Colour": omero.rtypes.rstring("White"), # will be ignored since no pixelsize set
           "Roi_Selection_Label": omero.rtypes.rstring("fakeTest"), # won't be found - but should still work
           "Algorithm": omero.rtypes.rstring("Mean Intensity"),
           "Figure_Name": omero.rtypes.rstring("movieROITest") 
           }
        fileAnnot1 = runScript(client, scriptId, argMap, "File_Annotation")

        # ...then with bare minimum args
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist(imageIds)}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image. 
        checkFileAnnotation(self,fileAnnot1, True)
        checkFileAnnotation(self,fileAnnot2, True)

        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self,fileAnnot3, False)        

    def testMovieFigure(self):

        print "testMovieFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        client = self.root
        services = {}
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        updateService = session.getUpdateService()
        
        services["containerService"] = session.getContainerService()
        services["renderingEngine"] = renderingEngine
        services["queryService"] = queryService
        services["pixelsService"] = pixelsService
        services["rawPixelStore"] = rawPixelStore
        
        # upload script 
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, movieFigurePath)

        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("movieFig-test")
        dataset = updateService.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("movieFig-test")
        project = updateService.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        updateService.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = self.createTestImage(256,256,5,3,20).getId().getValue()    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            updateService.saveAndReturnObject(dlink)
            
        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0':omero.rtypes.rstring("DAPI"),
            '1':omero.rtypes.rstring("GFP"), 
            '2':omero.rtypes.rstring("Red"), 
            '3':omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rint(255)
        red = omero.rtypes.rint(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0':blue, '1':blue, '3':red})
        tIndexes = [omero.rtypes.rint(0),omero.rtypes.rint(1),omero.rtypes.rint(5),omero.rtypes.rint(10),
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
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist(imageIds),
            "T_Indexes": omero.rtypes.rlist(tIndexes),}
        fileAnnot2 = runScript(client, scriptId, args, "File_Annotation")

        # should have figures attached to project and first image. 
        checkFileAnnotation(self,fileAnnot1, True)
        checkFileAnnotation(self,fileAnnot2, True)
        
        # Run the script with invalid IDs
        args = {"Data_Type": omero.rtypes.rstring("Image"), "IDs": omero.rtypes.rlist( omero.rtypes.rlong(-1))}        
        fileAnnot3 = runScript(client, scriptId, args, "File_Annotation")

        checkFileAnnotation(self,fileAnnot3, False)
        

def runScript(client, scriptId, argMap, returnKey=None): 
    
    scriptService = client.sf.getScriptService()
    proc = scriptService.runScript(scriptId, argMap, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000): # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)

    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        print "Script generated StdOut in file:" , origFile.getId().getValue()
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        # But, we still get stderr from EMAN2 import (duplicate numpy etc.)
        print "Script generated StdErr in file:" , origFile.getId().getValue()
    if returnKey and returnKey in results:
        return results[returnKey]

def uploadScript(scriptService, scriptPath):
    _uuid = str(uuid.uuid4())

    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    try:
        scriptId = scriptService.uploadOfficialScript("/%s/%s" % (_uuid, scriptPath), scriptText)
    except ApiUsageException:
        raise # The next line will never be run!
        scriptId = editScript(scriptService, scriptPath)
    return scriptId

def editScript(scriptService, scriptPath):
    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    # need the script Original File to edit
    #scripts = scriptService.getScripts()
    #if not scriptPath.startswith("/"): scriptPath =  "/" + scriptPath
    #namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
    #script = namedScripts[-1]
    script = getScript(scriptService, scriptPath)
    print "Editing script:", scriptPath
    scriptService.editScript(script, scriptText)
    return script.id.val
    

def getScript(scriptService, scriptPath):
    
    scripts = scriptService.getScripts()     # returns list of OriginalFiles     
        
    for s in scripts:
        print s.id.val, s.path.val + s.name.val
        
    # make sure path starts with a slash. 
    # ** If you are a Windows client - will need to convert all path separators to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath =  "/" + scriptPath
        
    namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
    
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
    rect.x = rdouble(x)
    rect.y = rdouble(y)
    rect.width = rdouble(width)
    rect.height = rdouble(height)
    rect.theZ = omero.rtypes.rint(0)
    rect.theT = omero.rtypes.rint(0)
    rect.locked = rbool(True)        # don't allow editing 
    rect.strokeWidth = omero.rtypes.rint(6)

    # link the rectangle to the ROI and save it 
    rect.setRoi(r)
    r.addShape(rect)    
    updateService.saveAndReturnObject(rect)

def checkFileAnnotation(self, fileAnnotation, hasFileAnnotation=True, parentType="Image", isLinked=True, client=None):
    """
    Check validity of file annotation. If hasFileAnnotation, check the size, name and number of objects linked to the original file.
    """
    if hasFileAnnotation:
        self.assertNotEqual(fileAnnotation,None)
        self.assertTrue(fileAnnotation.val._file._size._val>0)
        self.assertNotEqual(fileAnnotation.val._file._name._val,None)

        if client is None: client = self.root
        conn = BlitzGateway(client_obj = client)
        faWrapper = conn.getObject("FileAnnotation", fileAnnotation.val.id.val)
        nLinks = sum(1 for i in faWrapper.getParentLinks(parentType))
        if isLinked:
            self.assertEqual(nLinks,1)
        else:
            self.assertEqual(nLinks,0)
    else:
        self.assertEqual(fileAnnotation,None)

if __name__ == '__main__':
    unittest.main()
