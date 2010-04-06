#!/usr/bin/env python

"""
   Integration test for testing various EMAN2 scripts functionality. 

   dir(self)
   'assertAlmostEqual', 'assertAlmostEquals', 'assertEqual', 'assertEquals', 'assertFalse', 'assertNotAlmostEqual', 'assertNotAlmostEquals', 
   'assertNotEqual', 'assertNotEquals', 'assertRaises', 'assertTrue', 'assert_', 'client', 'countTestCases', 'debug', 'defaultTestResult', 
   'fail', 'failIf', 'failIfAlmostEqual', 'failIfEqual', 'failUnless', 'failUnlessAlmostEqual', 'failUnlessEqual', 'failUnlessRaises', 
   'failureException', 'id', 'login_args', 'new_user', 'query', 'root', 'run', 'setUp', 'sf', 'shortDescription', 'tearDown', 'testfoo', 
   'tmpfile', 'tmpfiles', 'update'
   
   Run test from OmeroPy/
   
    PYTHONPATH=/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/site-packages/:/opt/Ice-3.3.1/python:.:test:build/lib ICE_CONFIG=/Users/will/Documents/workspace/Omero/etc/ice.config python test/integration/emanScripts.py
    
"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import *
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice
import omero.util.script_utils as scriptUtil

import omero_SharedResources_ice
import omero_api_IScript_ice

#from numpy import arange
from PIL import Image
import numpy

class TestIShare(lib.ITest):
    
    def testBoxer(self):
        """
        Uploads a single particle image and the boxer.py script (paths defined below)
        Then adds ROIs for a couple of user-picked particles. These match the test image.
        Then runs the boxer.py script to add auto-picked particles as ROIs to the image.  
        This test, including running of script takes > 2 mins! 
        """
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        ### create three users in 3 groups
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
        
        # import image and manually pick particles. 
        imagePath = "/Users/will/Documents/biology-data/testData/ctfTest.tiff"
        iId = importImage(session, imagePath)
        image = gateway.getImage(iId)
        x, y, width, height = (1355, 600, 330, 330)
        addRectangleRoi(gateway, x, y, width, height, image)
        x, y, width, height = (890, 200, 330, 330)
        addRectangleRoi(gateway, x, y, width, height, image)
        
        # upload (as root) and run the boxer.py script as user 
        scriptService = self.root.sf.getScriptService()
        scriptPath = "/Users/will/Documents/workspace/Omero/components/tools/OmeroPy/scripts/EMAN2/boxer.py"
        scriptId = uploadScript(scriptService, scriptPath)
        ids = [omero.rtypes.rint(iId), ]
        argMap = {"imageIds": omero.rtypes.rlist(ids),}
        runScript(session, scriptId, omero.rtypes.rmap(argMap))
        
        
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
        raise "Boxer script failed. StdErr in file:" , origFile.getId().getValue()
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


def importImage(session, imagePath):
    
    gateway = session.createGateway()
    renderingEngine = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    
    plane2D = getPlaneFromImage(imagePath)
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, [plane2D], imagePath, "description", dataset=None)
    return image.getId().getValue()

if __name__ == '__main__':
    unittest.main()