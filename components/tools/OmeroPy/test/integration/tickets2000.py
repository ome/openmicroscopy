#!/usr/bin/env python

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest
import test.integration.library as lib
import omero, uuid
import omero_RTypes_ice
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

    def test1069(self):
        unique = omero.RString(str(uuid.uuid4()))
        project = ProjectI()
        project.name = unique
        project.description = omero.RString("NOTME")
        project = self.client.sf.getUpdateService().saveAndReturnObject(project)
        self.root.sf.getUpdateService().indexObject(project)

        search = self.client.sf.createSearchService()
        search.onlyType("Project")

        search.bySomeMustNone([unique.val], [], ["NOTME"])
        self.assert_( not search.hasNext() )

        search.bySomeMustNone([unique.val], [], ["NOTME","SOMETHINGELSE"])
        self.assert_( not search.hasNext() )

        search.bySomeMustNone([unique.val], [], [])
        self.assert_( search.hasNext() )

    def test1071(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        ipojo = self.root.sf.getPojosService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        
        #projects
        pr1 = ProjectI()
        pr1.setName(omero.RString('test1071-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)

        pr2 = ProjectI()
        pr2.setName(omero.RString('test1071-pr2-%s' % (uuid)))
        pr2 = update.saveAndReturnObject(pr2)

        #datasets
        ds1 = DatasetI()
        ds1.setName(omero.RString('test1071-ds1-%s' % (uuid)))
        ds1 = update.saveAndReturnObject(ds1)
        
        ds2 = DatasetI()
        ds2.setName(omero.RString('test1071-ds2-%s' % (uuid)))
        ds2 = update.saveAndReturnObject(ds2)
        
        ds3 = DatasetI()
        ds3.setName(omero.RString('test1071-ds3-%s' % (uuid)))
        ds3 = update.saveAndReturnObject(ds3)
        
        im2 = ImageI()
        im2.setName(omero.RString('test1071-im2-%s' % (uuid)))
        im2 = update.saveAndReturnObject(im2)
        
        #links
        pdl1 = ProjectDatasetLinkI()
        pdl1.setParent(pr1)
        pdl1.setChild(ds1)
        update.saveObject(pdl1)
        
        pdl2 = ProjectDatasetLinkI()
        pdl2.setParent(pr1)
        pdl2.setChild(ds2)
        update.saveObject(pdl2)
        
        pdl3 = ProjectDatasetLinkI()
        pdl3.setParent(pr2)
        pdl3.setChild(ds2)
        update.saveObject(pdl3)
        
        pdl4 = ProjectDatasetLinkI()
        pdl4.setParent(ds1)
        pdl4.setChild(im2)
        update.saveObject(pdl4)
        
        pdl5 = ProjectDatasetLinkI()
        pdl5.setParent(ds2)
        pdl5.setChild(im2)
        update.saveObject(pdl5)
        
        pdl6 = ProjectDatasetLinkI()
        pdl6.setParent(ds3)
        pdl6.setChild(im2)
        update.saveObject(pdl6)
        
        #test:
        hier = pojos.findContainerHierarchies("Project", [long(im2.id.val)], None)

        self.assert_(len(hier) == 3)
        for c in hier:
            if c.id.val == pr1.id.val:
                self.assert_(len(c.datasetLinks) == 2)
                for pdl in c.datasetLinks:
                    self.assert_(len(pdl.child.imageLinks) == 1)
                    for dil in pdl.child.imageLinks:
                        self.assert_(dil.child.id.val = im2.id.val)
                        
            elif c.id.val == pr2.id.val:
                self.assert_(len(c.datasetLinks) == 1)
            elif c.id.val == ds3.id.val:
                self.assert_(len(c.imageLinks) == 1)
        
if __name__ == '__main__':
    unittest.main()
