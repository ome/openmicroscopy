#!/usr/bin/env python

"""
   Integration test for testing various EMAN2 scripts functionality. 
   
   Not all of the scripts are covered. E.g ctf.py, openInChimera.py, eman2omero.py, either because they are 
   too tricky to test or because they cover very similar functionality to other scripts tested below. 

   dir(self)
   'assertAlmostEqual', 'assertAlmostEquals', 'assertEqual', 'assertEquals', 'assertFalse', 'assertNotAlmostEqual', 'assertNotAlmostEquals', 
   'assertNotEqual', 'assertNotEquals', 'assertRaises', 'assertTrue', 'assert_', 'client', 'countTestCases', 'debug', 'defaultTestResult', 
   'fail', 'failIf', 'failIfAlmostEqual', 'failIfEqual', 'failUnless', 'failUnlessAlmostEqual', 'failUnlessEqual', 'failUnlessRaises', 
   'failureException', 'id', 'login_args', 'new_user', 'query', 'root', 'run', 'setUp', 'sf', 'shortDescription', 'tearDown', 'testfoo', 
   'tmpfile', 'tmpfiles', 'update'
   
   ** IMPORTANT: Run test from OmeroPy/  **
   
   PYTHONPATH=/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/site-packages/:/opt/Ice-3.3.1/python:.:test:build/lib ICE_CONFIG=/Users/will/Documents/workspace/Omero/etc/ice.config python test/integration/emanScripts.py
   Add E.g.  TestEmanScripts.testRunSpiderProcedure to command to run a single test. 
"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import *
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice
import omero_api_IRoi_ice
import omero.util.script_utils as scriptUtil

import omero_SharedResources_ice
import omero_api_IScript_ice

import os
from PIL import Image
import numpy

boxerTestImage = "/Users/will/Documents/biology-data/testData/boxerTest.tiff"
smallTestImage = "/Users/will/Documents/biology-data/testData/smallTest.tiff"

runSpiderScriptPath = "scripts/EMAN2/runSpiderProcedure.py"
saveImageAsScriptPath = "scripts/EMAN2/saveImageAs.py"
export2emScriptPath = "scripts/EMAN2/export2em.py"
boxerScriptPath = "scripts/EMAN2/boxer.py" 
imagesFromRoisPath = "scripts/EMAN2/imagesFromRois.py"

class TestEmanScripts(lib.ITest):
    
    
    def testImagesFromRois(self):
        """
        Uploads an image, adds rectangle ROIs, then runs the imagesFromRois.py script to generate
        new images in a dataset. Then we check that the dataset exists with the expected number of images.
        """
        print "testImagesFromRois"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        # upload script as root
        scriptService = self.root.sf.getScriptService()
        scriptId = uploadScript(scriptService, imagesFromRoisPath)
        
        ### create user in group
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))  # all users need to be in 'user' group to do anything! 
        
        #group
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("roi2images-test_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        group = admin.getGroup(gid)
        listOfGroups.append(group)
        
        # add user
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("roi2images-user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), group, listOfGroups)
        
        # log in as user
        user_client = omero.client()
        user_client.createSession(new_exp.omeName.val,"ome")
        session = user_client.sf
        
        # create user services 
        gateway = session.createGateway()    
        roiService = session.getRoiService() 
        
        # import image and manually add rois
        imagePath = boxerTestImage
        imageName = "roi2imageTest"
        iId = importImage(session, imagePath, imageName)
        image = gateway.getImage(iId)
        x, y, width, height = (1355, 600, 320, 325)   
        addRectangleRoi(gateway, x, y, width, height, image)
        x, y, width, height = (890, 200, 310, 330)
        addRectangleRoi(gateway, x, y, width, height, image)
        
        # put image in dataset and project
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("roi-dataset")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("roi-project")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put image in dataset
        dlink = omero.model.DatasetImageLinkI()
        dlink.parent = omero.model.DatasetI(dataset.id.val, False)
        dlink.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(dlink)
        
        containerName = "particles"
        ids = [omero.rtypes.rint(iId), ]
        argMap = {
            "imageIds": omero.rtypes.rlist(ids),
            "containerName": rstring(containerName),
            }
        runScript(session, scriptId, omero.rtypes.rmap(argMap))
        
        # now we should have a dataset with 2 images, in project
        newDatasetName = "%s_%s" % (imageName, containerName)
        pros = gateway.getProjects([project.id.val], True)
        datasetFound = False
        for p in pros:
            for ds in p.linkedDatasetList():
                if ds.name.val == newDatasetName:
                    datasetFound = True
                    dsId = ds.id.val
                    iList = gateway.getImages(omero.api.ContainerClass.Dataset, [dsId])
                    self.assertEquals(2, len(iList))
        self.assertTrue(datasetFound, "No dataset found with images from ROIs")
        
    
    def testBoxer(self):
        """
        Uploads a single particle image (path defined below)
        Then adds ROIs for a couple of user-picked particles. These match the test image.
        The 'root' uploads the "boxer.py" script, and it is run by a regular 
        user, to add auto-picked particles as ROIs to the image.  
        This test, including running of script takes > 2 mins! 
        """
        print "testBoxer"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        ### create user in group
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))  # all users need to be in 'user' group to do anything! 
        
        #group
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("boxer-test_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        group = admin.getGroup(gid)
        listOfGroups.append(group)
        
        # add user
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("boxer-user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), group, listOfGroups)
        
        # log in as user
        user_client = omero.client()
        user_client.createSession(new_exp.omeName.val,"ome")
        session = user_client.sf
        
        # create user services 
        gateway = session.createGateway()    
        roiService = session.getRoiService() 
        
        # import image and manually pick particles. 
        imagePath = boxerTestImage
        iId = importImage(session, imagePath)
        image = gateway.getImage(iId)
        x, y, width, height = (1355, 600, 320, 325)     # script should re-size to 330
        addRectangleRoi(gateway, x, y, width, height, image)
        x, y, width, height = (890, 200, 310, 330)
        addRectangleRoi(gateway, x, y, width, height, image)
        
        # upload (as root) and run the boxer.py script as user 
        scriptService = self.root.sf.getScriptService()
        scriptId = uploadScript(scriptService, boxerScriptPath)
        ids = [omero.rtypes.rint(iId), ]
        argMap = {"imageIds": omero.rtypes.rlist(ids),}
        runScript(session, scriptId, omero.rtypes.rmap(argMap))
        
        # if the script ran OK, we should have more than the 2 ROIs we added above
        result = roiService.findByImage(iId, None)
        
        rectCount = 0
        for roi in result.rois:
            for shape in roi.copyShapes():
                if type(shape) == omero.model.RectI:
                    width = shape.getWidth().getValue()
                    height = shape.getHeight().getValue()
                    self.assertEquals(330, width)
                    self.assertEquals(330, height)
                    rectCount += 1
        self.assertTrue(rectCount > 2, "No ROIs added by boxer.py script")
        
        
    def testExport2Em(self):
        """
        Tests the export2em.py command-line script by creating an image in OMERO, then
        running the export2em.py script from command line and checking that an image has been exported. 
        The saveImageAs.py script is first uploaded to the scripting service, since this is required by export2em.py
        """
        print "testExport2Em"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        # upload saveImageAs.py script as root
        scriptService = self.root.sf.getScriptService()
        scriptId = uploadScript(scriptService, saveImageAsScriptPath)
        
        ### create user in group
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))  # all users need to be in 'user' group to do anything! 
        
        #group
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("export-test_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        group = admin.getGroup(gid)
        listOfGroups.append(group)
        
        # add user
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("export-user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), group, listOfGroups)
        
        # log in as user
        user_client = omero.client()
        user_client.createSession(new_exp.omeName.val,"ome")
        #session = user_client.sf
        
        # upload image as root, since only root has only root has permission to run 
        # export2em.py because it uses the scripting service to look up the 'saveImageAs.py'
        # script we just uploaded as root. 
        session = self.root.sf
        
        # import image
        iId = importImage(session, smallTestImage)
        imageName = os.path.basename(smallTestImage)
        
        extension = "png"
        commandArgs = []
        commandArgs.append("python %s" % export2emScriptPath)
        commandArgs.append("-h localhost")
        #commandArgs.append("-u %s" % new_exp.omeName.val)
        commandArgs.append("-u root")
        commandArgs.append("-p omero")
        commandArgs.append("-i %s" % iId)
        commandArgs.append("-e %s" % extension)
        
        commandString = " ".join(commandArgs)
        # run from command line
        os.system(commandString)
        
        # the downloaded file should be local. Try to find and open it. 
        if not imageName.endswith(".%s" % extension):
            imageName = "%s.%s" % (imageName, extension)
        #i = Image.open(imageName)
        #i.show()
        self.assertTrue(os.path.exists(imageName))
        os.remove(imageName)
        
        
    def testRunSpiderProcedure(self):
        """
        Tests the runSpiderProcedure.py script by uploading a simple Spider Procedure File (spf) to 
        OMERO as an Original File, creating an image in OMERO, then
        running the export2em.py script from command line and checking that an image has been exported. 
        The saveImageAs.py script is first uploaded to the scripting service, since this is required by export2em.py
        """
        print "testRunSpiderProcedure"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        # upload saveImageAs.py script as root
        scriptService = self.root.sf.getScriptService()
        scriptId = uploadScript(scriptService, runSpiderScriptPath)
        
        session = self.root.sf      # do everything as root for now 
        
        # create services
        queryService = session.getQueryService()
        updateService = session.getUpdateService()
        rawFileStore = session.createRawFileStore()
        gateway = session.createGateway()
        
        # import image
        iId = importImage(session, smallTestImage)
        image = gateway.getImage(iId)
        
        # put image in dataset and project
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("spider-test")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("spider-test")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put image in dataset
        dlink = omero.model.DatasetImageLinkI()
        dlink.parent = omero.model.DatasetI(dataset.id.val, False)
        dlink.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(dlink)
        
        # create and upload a Spider Procedure File
        # make a temp text file. Example from http://www.wadsworth.org/spider_doc/spider/docs/quickstart.html
        f = open("spider.spf", 'w')
        f.write("RT\n")
        f.write("test001\n")
        f.write("rot001\n")
        f.write("60\n")
        f.write("\n")
        f.write("IP\n")
        f.write("rot001\n")
        f.write("big001\n")
        f.write("150,150\n")
        f.write("\n")
        f.write("WI\n")
        f.write("big001\n")
        f.write("win001\n")
        f.write("75,75\n")
        f.write("1,75\n")
        f.write("\n")
        f.write("EN D")
        f.close() 
        fileId = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, "spider.spf", "text/plain")
        os.remove("spider.spf")
        
        newDatasetName = "spider-results"
        # run script
        ids = [omero.rtypes.rint(iId), ]
        argMap = {"imageIds": omero.rtypes.rlist(ids),
                "spfFileId": omero.rtypes.rlong(fileId),
                "newDatasetName": omero.rtypes.rstring(newDatasetName),
                "inputName": omero.rtypes.rstring("test001"),
                "outputName": omero.rtypes.rstring("win001")}
        runScript(session, scriptId, omero.rtypes.rmap(argMap))
        
        # check that image has been created. 
        # now we should have a dataset with 1 image, in project
        pros = gateway.getProjects([project.id.val], True)
        datasetFound = False
        for p in pros:
            for ds in p.linkedDatasetList():
                if ds.name.val == newDatasetName:
                    datasetFound = True
                    dsId = ds.id.val
                    iList = gateway.getImages(omero.api.ContainerClass.Dataset, [dsId])
                    self.assertEquals(1, len(iList))
        self.assertTrue(datasetFound, "No dataset found with images from ROIs")
        
        
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

def addRectangleRoi(gateway, x, y, width, height, image):
    """
    Adds a Rectangle (particle) to the current OMERO image, at point x, y. 
    Uses the self.image (OMERO image) and self.updateService
    """

    # create an ROI, add the rectangle and save
    roi = omero.model.RoiI()
    roi.setImage(image)
    r = gateway.saveAndReturnObject(roi) 

    # create and save a rectangle shape
    rect = omero.model.RectI()
    rect.x = rdouble(x)
    rect.y = rdouble(y)
    rect.width = rdouble(width)
    rect.height = rdouble(height)
    rect.theZ = rint(0)
    rect.theT = rint(0)
    rect.locked = rbool(True)        # don't allow editing 
    rect.strokeWidth = rint(6)

    # link the rectangle to the ROI and save it 
    rect.setRoi(r)
    r.addShape(rect)    
    gateway.saveAndReturnObject(rect)
    

def getPlaneFromImage(imagePath):
    """
    Reads a local image (E.g. single plane tiff) and returns it as a numpy 2D array.
    
    @param imagePath   Path to image. 
    """
    i = Image.open(imagePath)
    a = numpy.asarray(i)
    return a


def importImage(session, imagePath, imageName=None):
    
    gateway = session.createGateway()
    renderingEngine = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    
    plane2D = getPlaneFromImage(imagePath)
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    if imageName == None:
        imageName = imagePath
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, [plane2D], imageName, "description", dataset=None)
    return image.getId().getValue()

if __name__ == '__main__':
    unittest.main()