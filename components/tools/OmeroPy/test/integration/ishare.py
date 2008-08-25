#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero
import omero_RTypes_ice
import omero_Constants_ice
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI

class TestISShare(lib.ITest):

    def testBasicUsage(self):
        share = self.client.sf.getShareService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()
        
        test_user = None
        try:
            test_user = admin.lookupExperimenter("share_test_user")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = omero.RString("share_test_user")
            new_exp.firstName = omero.RString("Share")
            new_exp.lastName = omero.RString("Test")
            new_exp.email = omero.RString("sharetest@emaildomain.com")
            
            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
            admin.changeUserPassword(new_exp.omeName, "ome")

        # create share
        description = "my description"
        timeout = None
        objects = []
        experimenters = [admin.lookupExperimenter("share_test_user")]
        guests = ["ident@emaildomain.com"]
        enabled = omero.RTime()
        self.id = share.createShare(description, timeout, objects,experimenters, guests, enabled)
        
        self.assert_(len(share.getContents(self.id)) == 0)
        
        d = omero.model.DatasetI()
        d.setName(omero.RString("d"))
        # set permissions RW----
        d.details.permissions.setUserRead(True)
        d.details.permissions.setUserWrite(True)
        d.details.permissions.setGroupRead(False)
        d.details.permissions.setGroupWrite(False)
        d.details.permissions.setWorldRead(False)
        d.details.permissions.setWorldWrite(False)
        d = update.saveAndReturnObject(d)
        share.addObjects(self.id, [d])

        self.assert_(len(share.getContents(self.id)) == 1)

        ds = []
        for i in range(0,4):
            ds.append(omero.model.DatasetI())
            ds[i].setName(omero.RString("ds%i" % i))
        ds = update.saveAndReturnArray(ds)
        share.addObjects(self.id, ds)

        self.assert_(share.getContentSize(self.id) == 5)

        #check access by a member to see the content
        client_guest_read_only = omero.client()
        client_guest_read_only.createSession("share_test_user","ome")
        
        share = client_guest_read_only.sf.getShareService()
        share.activate(self.id)
        content = share.getContents(self.id)
        self.assert_(share.getContentSize(self.id) == 5)
        
        #check access by a member to add comments
        client_guest = omero.client()
        client_guest.createSession("share_test_user","ome")
        
        share = client_guest.sf.getShareService()
        share.addComment(self.id,"comment for share %i" % self.id)
        
        self.assert_(len(share.getComments(self.id)) == 1)
        
        # get share key and join directly
        s = share.getShare(self.id)
        
        # THIS IS NOT ALLOWED:
        client_share = omero.client()
        client_share.createSession(s.uuid,s.uuid)
        share1 = client_share.sf.getShareService()
        self.assert_(len(share1.getAllShares(True)) > 0)
        # THIS IS NOT ALLOWED: FINISH
        
        # guest looks in to the share
        guest_email = "ident@emaildomain.com"
        token =  s.uuid
        client_share_guest = omero.client()
        client_share_guest.createSession("guest","guest") # maybe there can be some verification of identity by (share_key, email) - both params could be sent to email
        
        share2 = client_share_guest.sf.getShareService()
        share2.getAllGuestShares(guest_email)
        self.assert_(share2.getGuestShare(token) > 0)
        share2.addComment(self.id,"guest comment for share %i" % self.id)
        self.assert_(len(share2.getComments(self.id)) == 1)

    def testRetrieval(self):
        shs = self.root.sf.getShareService()
        shs.getAllShares(True)

if __name__ == '__main__':
    unittest.main()
