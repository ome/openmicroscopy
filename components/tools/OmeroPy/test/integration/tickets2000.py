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

class TestTicket2000(lib.ITest):

    def test1064(self):
        share = self.client.sf.getShareService()
        search = self.client.sf.createSearchService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()
        cx = admin.getEventContext()
        self.client.sf.closeOnDestroy()

    def test1067(self):
        admin = self.root.sf.getAdminService()
        
        try:
            test_user = admin.lookupExperimenter("new_test_user")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = omero.RString("new_test_user")
            new_exp.firstName = omero.RString("New")
            new_exp.lastName = omero.RString("Test")
            new_exp.email = omero.RString("newtest@emaildomain.com")
            
            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
        
        try:
            test_group1 = admin.lookupGroup("test_group1")
        except:
            new_gr = ExperimenterGroupI()
            new_gr.name = omero.RString("test_group1")
            admin.createGroup(new_gr, None)
        
        groups = list()
        gr1 = admin.lookupGroup("test_group1")
        groups.append(gr1)
            
        exp = admin.lookupExperimenter("new_test_user")
        contained_grs = admin.containedGroups(exp.id.val);
        # if groupexperimetnermap contains text group should be remove
        for gr in contained_grs:
            if gr.id.val == gr1.id.val:
                admin.removeGroups(exp,groups)
        
        admin.addGroups(exp,groups)
        
        admin.setDefaultGroup(exp,gr1) # thrown an exception because gr1 is not on the GroupExperimenterMap
        
        self.root.sf.closeOnDestroy()

    def test1027(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        self.client.sf.getAdminService().lookupLdapAuthExperimenters()

if __name__ == '__main__':
    unittest.main()
