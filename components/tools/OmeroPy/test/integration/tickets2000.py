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
        pojos = self.root.sf.getPojosService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        
        #projects
        pr1 = ProjectI()
        pr1.setName(omero.RString('test1071-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(omero.RString('test1071-pr2-%s' % (uuid)))
        pr2 = update.saveAndReturnObject(pr2)
        pr2.unload()

        #datasets
        ds1 = DatasetI()
        ds1.setName(omero.RString('test1071-ds1-%s' % (uuid)))
        ds1 = update.saveAndReturnObject(ds1)
        ds1.unload()
        
        ds2 = DatasetI()
        ds2.setName(omero.RString('test1071-ds2-%s' % (uuid)))
        ds2 = update.saveAndReturnObject(ds2)
        ds2.unload()
        
        ds3 = DatasetI()
        ds3.setName(omero.RString('test1071-ds3-%s' % (uuid)))
        ds3 = update.saveAndReturnObject(ds3)
        ds3.unload()
        
        #images
        im2 = ImageI()
        im2.setName(omero.RString('test1071-im2-%s' % (uuid)))
        im2 = update.saveAndReturnObject(im2)
        im2.unload()
        
        #links
        #
        # im2 -> ds3
        #    +-> ds2 --> pr2
        #    |       \
        #    \-> ds1 --> pr1
        #
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
        
        dil4 = DatasetImageLinkI()
        dil4.setParent(ds1)
        dil4.setChild(im2)
        update.saveObject(dil4)
        
        dil5 = DatasetImageLinkI()
        dil5.setParent(ds2)
        dil5.setChild(im2)
        update.saveObject(dil5)
        
        dil6 = DatasetImageLinkI()
        dil6.setParent(ds3)
        dil6.setChild(im2)
        update.saveObject(dil6)
        
        #test:
        hier = pojos.findContainerHierarchies("Project", [long(im2.id.val)], None)

        self.assert_(len(hier) == 3)
        for c in hier:
            if c.id.val == pr1.id.val and isinstance(c, ProjectI):
                self.assert_(len(c.datasetLinks) == 2, "length 2 != " + str(len(c.datasetLinks)))
                for pdl in c.datasetLinks:
                    self.assert_(len(pdl.child.imageLinks) == 1)
                    for dil in pdl.child.imageLinks:
                        self.assert_(dil.child.id.val == im2.id.val)
                        
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                self.assert_(len(c.datasetLinks) == 1)
            elif c.id.val == ds3.id.val and isinstance(c, DatasetI):
                self.assert_(len(c.imageLinks) == 1)

    def test1071_1(self):
        admin = self.root.sf.getAdminService()
        
        #new user1
        try:
            test_user = admin.lookupExperimenter("new_test_user1")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = omero.RString("new_test_user1")
            new_exp.firstName = omero.RString("New1")
            new_exp.lastName = omero.RString("Test1")
            new_exp.email = omero.RString("newtest1@emaildomain.com")

            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))

            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)

        #new user2
        try:
            test_user = admin.lookupExperimenter("new_test_user2")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = omero.RString("new_test_user2")
            new_exp.firstName = omero.RString("New2")
            new_exp.lastName = omero.RString("Test2")
            new_exp.email = omero.RString("newtest2@emaildomain.com")

            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))

            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
        
        
        #test
        c1 = omero.client()
        c1.createSession("new_test_user1", "ome")
        c1_pojos = c1.sf.getPojosService()
        c1_query = c1.sf.getQueryService()
        c1_update = c1.sf.getUpdateService()
        c1_uuid = c1.sf.getAdminService().getEventContext().sessionUuid
        
        c2 = omero.client()
        c2.createSession("new_test_user2", "ome")
        c2_pojos = c2.sf.getPojosService()
        c2_query = c2.sf.getQueryService()
        c2_update = c2.sf.getUpdateService()
        c2_uuid = c2.sf.getAdminService().getEventContext().sessionUuid
        
        #projects
        pr1 = ProjectI()
        pr1.setName(omero.RString('test1071-pr1-%s' % (c1_uuid)))
        pr1 = c1_update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(omero.RString('test1071-pr2-%s' % (c2_uuid)))
        pr2 = c2_update.saveAndReturnObject(pr2)
        pr2.unload()

        #datasets
        ds1 = DatasetI()
        ds1.setName(omero.RString('test1071-ds1-%s' % (c1_uuid)))
        ds1 = c1.update.saveAndReturnObject(ds1)
        ds1.unload()
        
        ds2 = DatasetI()
        ds2.setName(omero.RString('test1071-ds2-%s' % (c2_uuid)))
        ds2 = c2.update.saveAndReturnObject(ds2)
        ds2.unload()
        
        #images
        im2 = ImageI()
        im2.setName(omero.RString('test1071-im2-%s' % (c2_uuid)))
        im2 = c2.update.saveAndReturnObject(im2)
        im2.unload()
        
        #links
        #
        # im2 -> ds3
        #    +-> ds2 --> pr2
        #    |       \
        #    \-> ds1 --> pr1
        #
        pdl1 = ProjectDatasetLinkI()
        pdl1.setParent(pr1)
        pdl1.setChild(ds1)
        c1_update.saveObject(pdl1)
        
        pdl2 = ProjectDatasetLinkI()
        pdl2.setParent(pr1)
        pdl2.setChild(ds2)
        c2_update.saveObject(pdl2)
        
        dil2 = DatasetImageLinkI()
        dil2.setParent(ds2)
        dil2.setChild(im2)
        c2_update.saveObject(dil2)
        
        dil1 = DatasetImageLinkI()
        dil1.setParent(ds1)
        dil1.setChild(im2)
        c1_update.saveObject(dil1)
        
        #test:
        hier = c2_pojos.findContainerHierarchies("Project", [long(im2.id.val)], None)

        self.assert_(len(hier) == 2)
        for c in hier:
            if c.id.val == pr1.id.val and isinstance(c, ProjectI):
                self.assert_(len(c.datasetLinks) == 1)
                for pdl in c.datasetLinks:
                    self.assert_(len(pdl.child.imageLinks) == 1)
                    for dil in pdl.child.imageLinks:
                        self.assert_(dil.child.id.val == im2.id.val)
                        
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                self.assert_(len(c.datasetLinks) == 1)
            elif c.id.val == ds3.id.val and isinstance(c, DatasetI):
                self.assert_(len(c.imageLinks) == 1)
        
        c1.sf.closeOnDestroy()
        c2.sf.closeOnDestroy()

    def test1072(self):
        #create two users where both are in the same active group
        admin = self.root.sf.getAdminService()
        uuid = admin.getEventContext().sessionUuid
        
        try:
            admin.lookupGroup("test_group_load_hierarchy")
        except:
            new_gr = ExperimenterGroupI()
            new_gr.name = omero.RString("test_group_load_hierarchy")
            admin.createGroup(new_gr)

        test_user = None
        try:
            test_user = admin.lookupExperimenter("test_load_hierarchy_user1")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = omero.RString("test_load_hierarchy_user1")
            new_exp.firstName = omero.RString("Test")
            new_exp.lastName = omero.RString("Test")
            new_exp.email = omero.RString("test@emaildomain.com")
            
            listOfGroups = list()
            defaultGroup = admin.lookupGroup("test_group_load_hierarchy")
            listOfGroups.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
            admin.changeUserPassword("test_load_hierarchy_user1", "ome")
            test_user = admin.lookupExperimenter("test_load_hierarchy_user1")
            
        test_user2 = None
        try:
            test_user2 = admin.lookupExperimenter("test_load_hierarchy_user2")
        except:
            new_exp2 = ExperimenterI()
            new_exp2.omeName = omero.RString("test_load_hierarchy_user2")
            new_exp2.firstName = omero.RString("Test")
            new_exp2.lastName = omero.RString("Test")
            new_exp2.email = omero.RString("test2@emaildomain.com")
            
            listOfGroups2 = list()
            defaultGroup2 = admin.lookupGroup("test_group_load_hierarchy")
            listOfGroups2.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp2, defaultGroup2, listOfGroups2)
            admin.changeUserPassword("test_load_hierarchy_user2", "ome")
            test_user2 = admin.lookupExperimenter("test_load_hierarchy_user2")
        
        #login as user1
        c1 = omero.client()
        c1.createSession("test_load_hierarchy_user1", "ome")
        update = c1.sf.getUpdateService()
        
        pr1 = ProjectI()
        pr1.setName(omero.RString('test1072-pr1-%s' % (uuid)))
        pr1.details.permissions.setUserRead(True)
        pr1.details.permissions.setUserWrite(True)
        pr1.details.permissions.setGroupRead(True)
        pr1.details.permissions.setGroupWrite(False)
        pr1.details.permissions.setWorldRead(False)
        pr1.details.permissions.setWorldWrite(False)
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()
        
        
        #datasets
        ds1 = DatasetI()
        ds1.setName(omero.RString('test1072-ds1-%s' % (uuid)))
        ds1.details.permissions.setUserRead(True)
        ds1.details.permissions.setUserWrite(True)
        ds1.details.permissions.setGroupRead(False)
        ds1.details.permissions.setGroupWrite(False)
        ds1.details.permissions.setWorldRead(False)
        ds1.details.permissions.setWorldWrite(False)
        ds1 = update.saveAndReturnObject(ds1)
        ds1.unload()
        
        pdl1 = ProjectDatasetLinkI()
        pdl1.setParent(pr1)
        pdl1.setChild(ds1)
        update.saveObject(pdl1)
        
        c1.sf.closeOnDestroy()
        #login as user2
        c2 = omero.client()
        c2.createSession("test_load_hierarchy_user2", "ome")
        pojos = c2.sf.getPojosService()
        
        print c2.sf.getAdminService().getEventContext()
        #print c1.sf.getAdminService().getEventContext()
        
        p = omero.sys.Parameters()
        p.map = {} 
        #p.map[omero.constants.POJOEXPERIMENTER] = omero.RLong(c2.sf.getAdminService().getEventContext().userId)
        p.map[omero.constants.POJOGROUP] = omero.RLong(c2.sf.getAdminService().getEventContext().groupId)
        #p.map[omero.constants.POJOLEAVES] = omero.RBool(True)
        pojos.loadContainerHierarchy("Project",None,  p.map)
        
if __name__ == '__main__':
    unittest.main()
