#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import integration.library as lib
import omero, uuid
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ProjectDatasetLinkI import ProjectDatasetLinkI
from omero_sys_ParametersI import ParametersI
from omero.rtypes import *

class TestTickets3000(lib.ITest):

    def test2396(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        
        ### create two users in two groups
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid1 = admin.createGroup(new_gr1)
        #group2
        new_gr2 = ExperimenterGroupI()
        new_gr2.name = rstring("group2_%s" % uuid)
        gid2 = admin.createGroup(new_gr2)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user1_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")        
        defaultGroup1 = admin.getGroup(gid1)
        listOfGroups1 = list()
        listOfGroups1.append(admin.lookupGroup("user"))
        eid = admin.createExperimenterWithPassword(new_exp, rstring("ome"), defaultGroup1, listOfGroups1)
                
        ## get users
        user1 = admin.getExperimenter(eid)        
        
        ## login as user1 
        client1 = omero.client()
        client1.createSession(user1.omeName.val,"ome")
        update1 = client1.sf.getUpdateService()
        
        # create image
        img = ImageI()
        img.setName(rstring('test2396-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update1.saveAndReturnObject(img)
        img.unload()
        
        format = "txt"
        binary = "12345678910"
        oFile = OriginalFileI()
        oFile.setName(rstring(str("txt-name")));
        oFile.setPath(rstring(str("txt-name")));
        oFile.setSize(rlong(len(binary)));
        oFile.setSha1(rstring("pending"));
        oFile.setMimetype(rstring(str(format)));
    
        of = update1.saveAndReturnObject(oFile);
        
        store = client1.sf.createRawFileStore()
        store.setFileId(of.id.val);
        store.write(binary, 0, 0)
        store.close()
    
        fa = FileAnnotationI()
        fa.setFile(of)
        l_ia = ImageAnnotationLinkI()
        l_ia.setParent(img)
        l_ia.setChild(fa)        
        update1.saveObject(l_ia)
        
        client1.sf.closeOnDestroy()

if __name__ == '__main__':
    unittest.main()
