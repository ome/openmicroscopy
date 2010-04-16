#!/usr/bin/env python

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
#import test.integration.library as lib
import integration.library as lib
import omero
from omero.rtypes import rtime, rlong, rstring, rlist, rint
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice
import omero.util.script_utils as scriptUtil

from numpy import *

thumbnailFigurePath = "scripts/thumbnailFigure.py"
splitViewFigurePath = "scripts/splitViewFigure.py"


class TestFigureExportScripts(lib.ITest):
    
    def testThumbnailFigure(self):
        
        print "testThumbnailFigure"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        session = self.root.sf
        
        # upload script 
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, thumbnailFigurePath)
        
        # create several test images in a dataset
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("thumbnailFigure-test")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("thumbnailFigure-test")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = createTestImage(session)
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            gateway.saveAndReturnObject(dlink)
            
        # run the script twice. First with all args...
        datasetIds = [omero.rtypes.rint(dataset.id.val), ]
        argMap = {
            "datasetIds": omero.rtypes.rlist(datasetIds),
            "parentId": omero.rtypes.rlong(project.id.val),
            "thumbSize": omero.rtypes.rlong(16),
            "maxColumns": omero.rtypes.rlong(2),
            "format": omero.rtypes.rstring("PNG"),
            "figureName": omero.rtypes.rstring("thumbnail-test"),
            "tagIds": omero.rtypes.rlist([omero.rtypes.rint(1)]),   # this is fake. TODO: add tags above
            "showUntaggedImages": omero.rtypes.rbool(True),
            }
        fileId1 = runScript(session, scriptId, omero.rtypes.rmap(argMap), "fileAnnotation")
        
        # ...then with bare minimum args
        args = {"imageIds": omero.rtypes.rlist(imageIds)}
        fileId2 = runScript(session, scriptId, omero.rtypes.rmap(args), "fileAnnotation")
        
        # should have figures attached to project and first image. 
        self.assertNotEqual(fileId1, None)
        self.assertNotEqual(fileId2, None)
        
    
    def testSplitViewFigure(self):

        print "testSplitViewFigure"

        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()

        session = self.root.sf
        services = {}
        gateway = session.createGateway()
        renderingEngine = session.createRenderingEngine()
        queryService = session.getQueryService()
        pixelsService = session.getPixelsService()
        rawPixelStore = session.createRawPixelsStore()
        
        services["gateway"] = gateway
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
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("splitViewFig-test")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put some images in dataset
        imageIds = []
        for i in range(5):
            imageId = createTestImage(services, 256,200,5,4,1)    # x,y,z,c,t
            imageIds.append(omero.rtypes.rlong(imageId))
            dlink = omero.model.DatasetImageLinkI()
            dlink.parent = omero.model.DatasetI(dataset.id.val, False)
            dlink.child = omero.model.ImageI(imageId, False)
            gateway.saveAndReturnObject(dlink)

        # run the script twice. First with all args...
        cNamesMap = omero.rtypes.rmap({'0':omero.rtypes.rstring("DAPI"),
            '1':omero.rtypes.rstring("GFP"), 
            '2':omero.rtypes.rstring("Red"), 
            '3':omero.rtypes.rstring("ACA")})
        blue = omero.rtypes.rlong(255)
        red = omero.rtypes.rlong(16711680)
        mrgdColoursMap = omero.rtypes.rmap({'0':blue, '1':blue, '3':red})
        argMap = {
           "imageIds": omero.rtypes.rlist(imageIds),
           "zStart": omero.rtypes.rlong(0),
           "zEnd": omero.rtypes.rlong(3),   
           "channelNames": cNamesMap,
           "splitIndexes": omero.rtypes.rlist([omero.rtypes.rlong(1),omero.rtypes.rlong(2)]),
           "splitPanelsGrey": omero.rtypes.rbool(True),
           "mergedColours": mrgdColoursMap,
           #"mergedNames": omero.rtypes.rbool(True),
           #"width": omero.rtypes.rlong(200),
           #"height": omero.rtypes.rlong(200),
           #"imageLabels": omero.rtypes.rstring("DATASETS"),
           #"algorithm": omero.rtypes.rstring("MEANINTENSITY"),
           #"stepping": omero.rtypes.rlong(1),
           #"scalebar": omero.rtypes.rlong(10), # will be ignored since no pixelsize set
           "format": omero.rtypes.rstring("PNG"),
           "figureName": omero.rtypes.rstring("splitViewTest"),
           #"overlayColour": red,
           }
        fileId1 = runScript(session, scriptId, omero.rtypes.rmap(argMap), "fileAnnotation")

        # ...then with bare minimum args
        args = {"imageIds": omero.rtypes.rlist(imageIds),
            "mergedColours": mrgdColoursMap,
            "format": omero.rtypes.rstring("PNG"),
            "figureName": omero.rtypes.rstring("splitViewTest")}
        #fileId2 = runScript(session, scriptId, omero.rtypes.rmap(args), "fileAnnotation")

        # should have figures attached to project and first image. 
        self.assertNotEqual(fileId1, None)
        #self.assertNotEqual(fileId2, None)
        

def runScript(session, scriptId, argMap, returnKey=None): 
    # TODO: this will be refactored 
    job = omero.model.ScriptJobI() 
    job.linkOriginalFile(omero.model.OriginalFileI(scriptId, False)) 
    processor = session.sharedResources().acquireProcessor(job, 10) 
    proc = processor.execute(argMap) 
    processor.setDetach(True)
    proc.wait()
    results = processor.getResults(proc).getValue()
        
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        # But, we still get stderr from EMAN2 import (duplicate numpy etc.)
        print "Script generated StdErr in file:" , origFile.getId().getValue()
    if returnKey and returnKey in results:
        return results[returnKey]
        

def uploadScript(scriptService, scriptPath):
    file = open(scriptPath)
    script = file.read()
    file.close()
    scriptId = scriptService.uploadScript(script)
    return scriptId
        
        
def createTestImage(services, sizeX = 256, sizeY = 256, sizeZ = 5, sizeC = 3, sizeT = 1):
    
    gateway = services["gateway"]
    renderingEngine = services["renderingEngine"]
    queryService = services["queryService"]
    pixelsService = services["pixelsService"]
    rawPixelStore = services["rawPixelStore"]
    
    def f(x,y):
        return x+y
    
    pType = "int16"
    # look up the PixelsType object from DB
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    if pixelsType == None and pType.startswith("float"):    # e.g. float32
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
    if pixelsType == None:
        print "Unknown pixels type for: " % pType
        return
    
    # code below here is very similar to combineImages.py
    # create an image in OMERO and populate the planes with numpy 2D arrays
    channelList = range(sizeC)
    iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, "testImage", "description")
    image = gateway.getImage(iId.getValue())
    
    pixelsId = image.getPrimaryPixels().getId().getValue()
    rawPixelStore.setPixelsId(pixelsId, True)
    
    colourMap = {0: (0,0,255,255), 1:(0,255,0,255), 2:(255,0,0,255), 3:(255,0,255,255)}
    for theC in range(sizeC):
        minValue = 0
        maxValue = 0
        for theZ in range(sizeZ):
            for theT in range(sizeT):
                plane2D = fromfunction(f,(sizeX,sizeY),dtype=int16)
                scriptUtil.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
                minValue = min(minValue, plane2D.min())
                maxValue = max(maxValue, plane2D.max())
        pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
        rgba = None
        if theC in colourMap:
            rgba = colourMap[theC]
        scriptUtil.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
    
    return image.getId().getValue()

if __name__ == '__main__':
    unittest.main()