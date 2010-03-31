#!/usr/bin/env python

"""
   Integration test for getting thumbnails between members of groups.
   Testing permissions and thumbnail service on a running server. 

"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import rtime, rlong, rstring, rlist
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_PermissionsI import PermissionsI
import omero_api_Gateway_ice
import omero.util.script_utils as scriptUtil

from numpy import arange

class TestIShare(lib.ITest):
    
    def testfoo(self):
        
        # root session is root.sf
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        ### create three users in one group
        listOfGroups = list()
        #group1 - private
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("private_%s" % uuid)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldWrite(False)
        new_gr1.details.permissions = p
        gid = admin.createGroup(new_gr1)
        privateGroup = admin.getGroup(gid)
        self.assertEquals('rw----', str(privateGroup.details.permissions))
        listOfGroups.append(privateGroup)
        
        #group2 - read-only
        new_gr2 = ExperimenterGroupI()
        new_gr2.name = rstring("read-only_%s" % uuid)
        p2 = PermissionsI()
        p2.setUserRead(True)
        p2.setUserWrite(True)
        p2.setGroupRead(True)
        p2.setGroupWrite(False)
        p2.setWorldRead(False)
        p2.setWorldWrite(False)
        new_gr2.details.permissions = p2
        gid2 = admin.createGroup(new_gr2)
        readOnlyGroup = admin.getGroup(gid2)
        self.assertEquals('rwr---', str(readOnlyGroup.details.permissions))
        listOfGroups.append(readOnlyGroup)
        
        #group3 - collaborative
        new_gr3 = ExperimenterGroupI()
        new_gr3.name = rstring("collaborative_%s" % uuid)
        p = PermissionsI()
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupWrite(True)
        p.setWorldRead(False)
        p.setWorldWrite(False)
        new_gr3.details.permissions = p
        gid3 = admin.createGroup(new_gr3)
        collaborativeGroup = admin.getGroup(gid3)
        self.assertEquals('rwrw--', str(collaborativeGroup.details.permissions))
        listOfGroups.append(collaborativeGroup)
        
        #new user (group owner)
        owner = ExperimenterI()
        owner.omeName = rstring("owner_%s" % uuid)
        owner.firstName = rstring("Group")
        owner.lastName = rstring("Owner")
        owner.email = rstring("owner@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(owner, rstring("ome"), privateGroup, listOfGroups)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), privateGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring("user2_%s" % uuid)
        new_exp2.firstName = rstring("New2")
        new_exp2.lastName = rstring("Test2")
        new_exp2.email = rstring("newtest2@emaildomain.com")
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), privateGroup, listOfGroups)
        
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1 (into their default group)
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        
        # create image
        iId = createTestImage(self.root.sf)         # this works as root
        iId = createTestImage(client_share1.sf)     # this fails at queryService!
        print "imageId", iId
        
def createTestImage(session):
    
    gateway = session.createGateway()
    renderingEngine = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    
    plane2D = arange(256).reshape(16,16)
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, [plane2D], "imageName", "description", dataset=None)
    return image.getId().getValue()

if __name__ == '__main__':
    unittest.main()