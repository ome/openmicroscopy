#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IPojos interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, unittest
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ImageAnnotationLinkI import ImageAnnotationLinkI
from omero_model_CommentAnnotationI import CommentAnnotationI
from omero.rtypes import rstring, rtime

class TestIContainer(lib.ITest):

    def testFindAnnotations(self):
        ipojo = self.client.sf.getContainerService()
        i = ImageI()
        i.setName(rstring("name"))
        i.setAcquisitionDate(rtime(0))
        i = ipojo.createDataObject(i,None)

    
    def testFindAndCountAnnotationsForPrivateData(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        ipojo = self.root.sf.getContainerService()
        
        ### create new user
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        ## get user
        user1 = admin.getExperimenter(eid)
        
        ## login as user1 
        cl1 = omero.client()
        cl1.createSession(user1.omeName.val,"ome")
        update1 = cl1.sf.getUpdateService()
        ipojo1 = cl1.sf.getContainerService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        
        # permission 'rw----':
        img.details.permissions.setUserRead(True)
        img.details.permissions.setUserWrite(True)
        img.details.permissions.setGroupRead(False)
        img.details.permissions.setGroupWrite(False)
        img.details.permissions.setWorldRead(False)
        img.details.permissions.setWorldWrite(False)
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        ann1 = CommentAnnotationI()
        ann1.textValue = rstring("user comment - %s" % uuid)
        l_ann1 = ImageAnnotationLinkI()
        l_ann1.setParent(img)
        l_ann1.setChild(ann1)
        update1.saveObject(l_ann1)
        
        #user retrives the annotations for image
        coll_count = ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        self.assertEquals(1, coll_count.get(img.id.val, []))
        #self.assertEquals(1, len(ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, [])))
        
        ann = CommentAnnotationI()
        ann.textValue = rstring("root comment - %s" % uuid)
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(img)
        l_ann.setChild(ann)
        update.saveObject(l_ann)
        
        #do they see the same vals?
        #print ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        #print ipojo.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        #print len(ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, []))
        #print len(ipojo.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, []))
        coll_count = ipojo1.getCollectionCount("Image", "ome.model.containers.Image_annotationLinks", [img.id.val], None)
        self.assertEquals(2, coll_count.get(img.id.val, []))
        #anns = ipojo1.findAnnotations("Image", [img.id.val], None, None).get(img.id.val, [])
        #self.assertEquals(2, len(anns))
        
        #self.assert_(anns[0].details.permissions == 'rw----')
        #self.assert_(anns[1].details.permissions == 'rw----')
        
        cl1.sf.closeOnDestroy()
    
    def testCreateAfterBlitzPort(self):
        ipojo = self.client.sf.getContainerService()
        i = ImageI()
        i.setName(rstring("name"))
        i.setAcquisitionDate(rtime(0))
        i = ipojo.createDataObject(i,None)
        o = i.getDetails().owner
        self.assertEquals( -1, o.sizeOfGroupExperimenterMap() )

if __name__ == '__main__':
    unittest.main()
