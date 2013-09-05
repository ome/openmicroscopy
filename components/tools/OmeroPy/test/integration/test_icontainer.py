#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

    def testFindAndCountAnnotationsForSharedData(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        ipojo = self.root.sf.getContainerService()
        
        ### create new users
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup, listOfGroups)
        
        #new user2
        new_exp2 = ExperimenterI()
        new_exp2.omeName = rstring("user2_%s" % uuid)
        new_exp2.firstName = rstring("New2")
        new_exp2.lastName = rstring("Test2")
        
        defaultGroup = admin.getGroup(gid)
        listOfGroups = list()
        listOfGroups.append(admin.lookupGroup("user"))
        
        eid2 = admin.createExperimenterWithPassword(new_exp2, rstring("ome"), defaultGroup, listOfGroups)
    
        ## get users
        user1 = admin.getExperimenter(eid)
        user2 = admin.getExperimenter(eid2)
        
        ## login as user1
        cl1 = self.new_client(user=user1, password="ome")
        update1 = cl1.sf.getUpdateService()
        ipojo1 = cl1.sf.getContainerService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))

        # default permission 'rw----':
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

        ## login as user2
        cl2 = self.new_client(user=user2, password="ome")
        update2 = cl1.sf.getUpdateService()
        
        ann = CommentAnnotationI()
        ann.textValue = rstring("user2 comment - %s" % uuid)
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(img)
        l_ann.setChild(ann)
        update2.saveObject(l_ann)
        
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
        cl2.sf.closeOnDestroy()
    
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
