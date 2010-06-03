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
   
   PYTHONPATH=$PYTHONPATH:.:test:build/lib ICE_CONFIG=/Users/will/Documents/workspace/Omero/etc/ice.config python integration/emanScripts.py
   Add E.g.  TestEmanScripts.testRunSpiderProcedure to command to run a single test. 
"""
import unittest, time
#import test.integration.library as lib
import integration.library as lib
import omero
import omero.clients
from omero.rtypes import *
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice
import omero_api_IRoi_ice
import omero.util.script_utils as scriptUtil
import omero.scripts
from omero import ApiUsageException

import omero_SharedResources_ice
import omero_api_IScript_ice

import os
from PIL import Image
import numpy

from EMAN2 import *

boxerTestImage = "/Users/will/Documents/biology-data/testData/boxerTest.tiff"
smallTestImage = "/Users/will/Documents/biology-data/testData/smallTest.tiff"

runSpiderScriptPath = "scripts/EMAN2/Run_Spider_Procedure.py"
saveImageAsScriptPath = "scripts/EMAN2/Save_Image_As_Em.py"
export2emScriptPath = "scripts/EMAN2/export2em.py"
boxerScriptPath = "scripts/EMAN2/Auto_Boxer.py" 
imagesFromRoisPath = "scripts/omero/util_scripts/Images_From_ROIs.py"
emanFiltersScript = "scripts/EMAN2/Eman_Filters.py"

class TestEmanScripts(lib.ITest):
    
    def testFilterScript(self):
        
        print "testFilterScript"
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        # run as root
        session = self.root.sf
        client = self.root
        gateway = session.createGateway()
        
        # upload script
        scriptService = session.getScriptService()
        scriptId = uploadScript(scriptService, emanFiltersScript)
        
        try:
            a=test_image()
            planeData = EMNumPy.em2numpy(a)
        except:
            print "Couldn't create EMAN2 test image. EMAN2 import failed?"
            return
        imageId = importImage(session, None, "filterTestImage", planeData)
        
        # put image in dataset and project
        # create dataset
        dataset = omero.model.DatasetI()
        dsName = "emanFilters-test"
        dataset.name = rstring(dsName)
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("emanFilters-test")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put image in dataset
        dlink = omero.model.DatasetImageLinkI()
        dlink.parent = omero.model.DatasetI(dataset.id.val, False)
        dlink.child = omero.model.ImageI(imageId, False)
        gateway.saveAndReturnObject(dlink)
        
        # run script with all parameters. See http://blake.bcm.edu/emanwiki/Eman2ProgQuickstart
        newDatasetName = "filter-results"
        filterParams = {"sigma": omero.rtypes.rstring('0.125')}
        argMap = {"IDs":omero.rtypes.rlist([dataset.id]),       # process these images
            "Data_Type": omero.rtypes.rstring("Dataset"),
            "Filter_Name":omero.rtypes.rstring("filter.lowpass.gauss"),        # using this filter
            "Filter_Params":omero.rtypes.rmap(filterParams),             # additional params
            "New_Dataset_Name": omero.rtypes.rstring(newDatasetName),   # optional: put results in new dataset
        }
        runScript(scriptService, client, scriptId, argMap)
        
        # try with minimal parameters - put results into same dataset. 
        imageIds = [omero.rtypes.rlong(imageId)]
        aMap = {"IDs":omero.rtypes.rlist(imageIds),       # process these images
            "Data_Type": omero.rtypes.rstring("Image"),
            "Filter_Name":omero.rtypes.rstring("normalize"),        # using this filter - no params
        }
        runScript(scriptService, client, scriptId, aMap)
        
        rMap = {"IDs":omero.rtypes.rlist(imageIds),       # process these images
            "Data_Type": omero.rtypes.rstring("Image"),
            "Filter_Name":omero.rtypes.rstring("normalize.edgemean"),    # using this filter - no params
        }
        runScript(scriptService, client, scriptId, rMap)
        
        # should now have 2 datasets in the project above...
        pros = gateway.getProjects([project.id.val], True)
        datasetFound = False
        for p in pros:
            self.assertEquals(2, len(p.linkedDatasetList()))   # 2 datasets
            for ds in p.linkedDatasetList():
                # new dataset should have 1 image
                if ds.name.val == newDatasetName:   
                    datasetFound = True
                    iList = gateway.getImages(omero.api.ContainerClass.Dataset, [ds.id.val])
                    self.assertEquals(1, len(iList))
                else:
                    # existing dataset should have 3 images. 
                    self.assertEquals(ds.name.val, dsName)
                    iList = gateway.getImages(omero.api.ContainerClass.Dataset, [ds.id.val])
                    self.assertEquals(3, len(iList))
                    
        self.assertTrue(datasetFound, "No dataset found with EMAN-filtered images")
        
    
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
        
        # run as root
        session = self.root.sf
        client = self.root
        
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
        dataset.name = rstring("imagesFromRoi-test")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("imagesFromRoi-test")
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
        ids = [omero.rtypes.rlong(iId), ]
        argMap = {
            "Image_IDs": omero.rtypes.rlist(ids),
            "Container_Name": rstring(containerName),
            }
        runScript(scriptService, client, scriptId, argMap)
        
        
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
        self.assertTrue(datasetFound, "No dataset: %s found with images from ROIs" % newDatasetName)
        
    
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
        
        # run as root
        client = self.root
        session = self.root.sf
        
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
        
        # put image in dataset and project
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("boxer-test")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("boxer-test")
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
        
        # upload (as root) and run the boxer.py script as user 
        scriptService = self.root.sf.getScriptService()
        scriptId = uploadScript(scriptService, boxerScriptPath)
        ids = [omero.rtypes.rlong(iId), ]
        argMap = {"Image_IDs": omero.rtypes.rlist(ids),}
        runScript(scriptService, client, scriptId, argMap)
        
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
        
        # upload image as root, since only root has only root has permission to run 
        # export2em.py because it uses the scripting service to look up the 'saveImageAs.py'
        # script we just uploaded as root. 
        session = self.root.sf
        gateway = session.createGateway()
        
        # import image into dataset and project
        iId = importImage(session, smallTestImage)
        imageName = os.path.basename(smallTestImage)
        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring("export2em-test")
        dataset = gateway.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring("export2em-test")
        project = gateway.saveAndReturnObject(project)
        # put dataset in project 
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
        # put image in dataset
        dlink = omero.model.DatasetImageLinkI()
        dlink.parent = omero.model.DatasetI(dataset.id.val, False)
        dlink.child = omero.model.ImageI(iId, False)
        gateway.saveAndReturnObject(dlink)
        
        
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
        
        if not imageName.endswith(".%s" % extension):
            imageName = "%s.%s" % (imageName, extension)
        self.assertFalse(os.path.exists(imageName))     # make sure we start with no file
        
        # run from command line
        os.system(commandString)
        
        # the downloaded file should be local. Try to find and open it. 
        self.assertTrue(os.path.exists(imageName))
        
        # import again to test
        try:
            a=EMData()
            a.read_image(imageName)
            planeData = EMNumPy.em2numpy(a)
        except:
            print "Couldn't create EMAN2 test image. EMAN2 import failed?"
            return
        reImportId = importImage(session, None, imageName, planeData)
        #reImportId = importImage(session, imageName)
        dlink = omero.model.DatasetImageLinkI()
        dlink.parent = omero.model.DatasetI(dataset.id.val, False)
        dlink.child = omero.model.ImageI(reImportId, False)
        gateway.saveAndReturnObject(dlink)
        
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
        fileAnnot = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, "spider.spf", "text/plain")
        os.remove("spider.spf")
        
        newDatasetName = "spider-results"
        # run script
        ids = [omero.rtypes.rlong(iId), ]
        argMap = {"IDs":omero.rtypes.rlist(ids),       # process these images
                "Data_Type": omero.rtypes.rstring("Image"),
                "Spf_File_Id": fileAnnot.id,
                "New_Dataset_Name": omero.rtypes.rstring(newDatasetName),
                "Input_Name": omero.rtypes.rstring("test001"),
                "Output_Name": omero.rtypes.rstring("win001")}
        runScript(scriptService, self.root, scriptId, argMap)
        
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
        
        
def runScript(scriptService, client, scriptId, argMap, returnKey=None): 
    # The last parameter is how long to wait as an RInt
    proc = scriptService.runScript(scriptId, argMap, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000): # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)
         
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        # But, we still get stderr from EMAN2 import (duplicate numpy etc.)
        print "Script generated StdErr in file:" , origFile.getId().getValue()
    if returnKey and returnKey in results:
        return results[returnKey]

def uploadScript(scriptService, scriptPath):
    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    path = scriptPath.replace("scripts/EMAN2", "/EMAN2")  # convert scripts/EMAN2/script.py to /EMAN2/scripts.py
    print "uploading scriptPath",scriptPath
    try:
        scriptId = scriptService.uploadOfficialScript(path, scriptText)
    except ApiUsageException:
        scriptId = editScript(scriptService, scriptPath)
    return scriptId
    
def editScript(scriptService, scriptPath):
    file = open(scriptPath)
    scriptText = file.read()
    file.close()
    scriptPath = scriptPath.replace("scripts/EMAN2", "/EMAN2")  # convert scripts/EMAN2/script.py to /EMAN2/scripts.py
    # need the script Original File to edit
    scripts = scriptService.getScripts()
    if not scriptPath.startswith("/"): scriptPath =  "/" + scriptPath
    namedScripts = [s for s in scripts if s.path.val + s.name.val == scriptPath]
    script = namedScripts[-1]
    print "Editing script:", scriptPath
    scriptService.editScript(script, scriptText)
    return script.id.val

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


def importImage(session, imagePath, imageName=None, planeData=None):
    
    gateway = session.createGateway()
    renderingEngine = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    
    if imagePath != None:
        data = getPlaneFromImage(imagePath)
        if len(data.shape) == 3:
            plane2D = data[0]   # this actually slices the wrong way. E.g. Gives a row with 3 channels. 
        else: plane2D = data
    else:
        plane2D = planeData
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    if pixelsType == None:
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % 'float', None) # omero::model::PixelsType
    
    if imageName == None:
        imageName = imagePath
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, [plane2D], imageName, "description", dataset=None)
    return image.getId().getValue()

if __name__ == '__main__':
    unittest.main()