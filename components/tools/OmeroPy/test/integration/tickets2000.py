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
from omero.rtypes import *

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
            new_exp.omeName = rstring("new_test_user")
            new_exp.firstName = rstring("New")
            new_exp.lastName = rstring("Test")
            new_exp.email = rstring("newtest@emaildomain.com")

            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))

            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)

        try:
            test_group1 = admin.lookupGroup("test_group1")
        except:
            new_gr = ExperimenterGroupI()
            new_gr.name = rstring("test_group1")
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
        unique = rstring(str(uuid.uuid4()))
        project = ProjectI()
        project.name = unique
        project.description = rstring("NOTME")
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
        pojos = self.root.sf.getContainerService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        
        #projects
        pr1 = ProjectI()
        pr1.setName(rstring('test1071-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(rstring('test1071-pr2-%s' % (uuid)))
        pr2 = update.saveAndReturnObject(pr2)
        pr2.unload()

        #datasets
        ds1 = DatasetI()
        ds1.setName(rstring('test1071-ds1-%s' % (uuid)))
        ds1 = update.saveAndReturnObject(ds1)
        ds1.unload()
        
        ds2 = DatasetI()
        ds2.setName(rstring('test1071-ds2-%s' % (uuid)))
        ds2 = update.saveAndReturnObject(ds2)
        ds2.unload()
        
        ds3 = DatasetI()
        ds3.setName(rstring('test1071-ds3-%s' % (uuid)))
        ds3 = update.saveAndReturnObject(ds3)
        ds3.unload()
        
        #images
        im2 = ImageI()
        im2.setName(rstring('test1071-im2-%s' % (uuid)))
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
                self.assert_(c.sizeOfDatasetLinks() == 2, "length 2 != " + str(c.sizeOfDatasetLinks()))
                for pdl in c.copyDatasetLinks():
                    self.assert_(pdl.child.sizeOfImageLinks() == 1)
                    for dil in pdl.child.copyImageLinks():
                        self.assert_(dil.child.id.val == im2.id.val)
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                self.assert_( c.sizeOfDatasetLinks() == 1 )
            elif c.id.val == ds3.id.val and isinstance(c, DatasetI):
                self.assert_( c.sizeOfImageLinks() == 1 )

    def test1071_1(self):
        admin = self.root.sf.getAdminService()
        
        #new user1
        try:
            test_user = admin.lookupExperimenter("new_test_user1")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = rstring("new_test_user1")
            new_exp.firstName = rstring("New1")
            new_exp.lastName = rstring("Test1")
            new_exp.email = rstring("newtest1@emaildomain.com")

            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))

            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)

        #new user2
        try:
            test_user = admin.lookupExperimenter("new_test_user2")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = rstring("new_test_user2")
            new_exp.firstName = rstring("New2")
            new_exp.lastName = rstring("Test2")
            new_exp.email = rstring("newtest2@emaildomain.com")

            listOfGroups = list()
            defaultGroup = admin.lookupGroup("default")
            listOfGroups.append(admin.lookupGroup("user"))

            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
        
        
        #test
        c1 = omero.client()
        c1.createSession("new_test_user1", "ome")
        c1_pojos = c1.sf.getContainerService()
        c1_query = c1.sf.getQueryService()
        c1_update = c1.sf.getUpdateService()
        c1_uuid = c1.sf.getAdminService().getEventContext().sessionUuid
        
        c2 = omero.client()
        c2.createSession("new_test_user2", "ome")
        c2_pojos = c2.sf.getContainerService()
        c2_query = c2.sf.getQueryService()
        c2_update = c2.sf.getUpdateService()
        c2_uuid = c2.sf.getAdminService().getEventContext().sessionUuid
        
        #projects
        pr1 = ProjectI()
        pr1.setName(rstring('test1071-pr1-%s' % (c1_uuid)))
        pr1 = c1_update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(rstring('test1071-pr2-%s' % (c2_uuid)))
        pr2 = c2_update.saveAndReturnObject(pr2)
        pr2.unload()

        #datasets
        ds1 = DatasetI()
        ds1.setName(rstring('test1071-ds1-%s' % (c1_uuid)))
        ds1 = c1_update.saveAndReturnObject(ds1)
        ds1.unload()
        
        ds2 = DatasetI()
        ds2.setName(rstring('test1071-ds2-%s' % (c2_uuid)))
        ds2 = c2_update.saveAndReturnObject(ds2)
        ds2.unload()
        
        #images
        im2 = ImageI()
        im2.setName(rstring('test1071-im2-%s' % (c2_uuid)))
        im2 = c2_update.saveAndReturnObject(im2)
        im2.unload()
        
        #links
        # im2 owned by u2
        # 
        # im2   -> ds2 --> pr2 (owned by u2)
        #      |       
        #      \-> ds1 --> pr1 (owned by u1)
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
            new_gr.name = rstring("test_group_load_hierarchy")
            admin.createGroup(new_gr)

        test_user = None
        try:
            test_user = admin.lookupExperimenter("test_load_hierarchy_user1")
        except:
            new_exp = ExperimenterI()
            new_exp.omeName = rstring("test_load_hierarchy_user1")
            new_exp.firstName = rstring("Test")
            new_exp.lastName = rstring("Test")
            new_exp.email = rstring("test@emaildomain.com")
            
            listOfGroups = list()
            defaultGroup = admin.lookupGroup("test_group_load_hierarchy")
            listOfGroups.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
            admin.changeUserPassword("test_load_hierarchy_user1", rstring("ome"))
            test_user = admin.lookupExperimenter("test_load_hierarchy_user1")
            
        test_user2 = None
        try:
            test_user2 = admin.lookupExperimenter("test_load_hierarchy_user2")
        except:
            new_exp2 = ExperimenterI()
            new_exp2.omeName = rstring("test_load_hierarchy_user2")
            new_exp2.firstName = rstring("Test")
            new_exp2.lastName = rstring("Test")
            new_exp2.email = rstring("test2@emaildomain.com")
            
            listOfGroups2 = list()
            defaultGroup2 = admin.lookupGroup("test_group_load_hierarchy")
            listOfGroups2.append(admin.lookupGroup("user"))
            
            admin.createExperimenter(new_exp2, defaultGroup2, listOfGroups2)
            admin.changeUserPassword("test_load_hierarchy_user2", rstring("ome"))
            test_user2 = admin.lookupExperimenter("test_load_hierarchy_user2")
        
        #login as user1
        c1 = omero.client()
        c1.createSession("test_load_hierarchy_user1", "ome")
        update = c1.sf.getUpdateService()
        
        pr1 = ProjectI()
        pr1.setName(rstring('test1072-pr1-%s' % (uuid)))
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
        ds1.setName(rstring('test1072-ds1-%s' % (uuid)))
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
        pojos = c2.sf.getContainerService()
        
        self.assert_( c2.sf.getAdminService().getEventContext() )
        #print c1.sf.getAdminService().getEventContext()
        
        p = omero.sys.Parameters()
        p.map = {} 
        #p.map[omero.constants.POJOEXPERIMENTER] = rlong(c2.sf.getAdminService().getEventContext().userId)
        p.map[omero.constants.POJOGROUP] = rlong(c2.sf.getAdminService().getEventContext().groupId)
        #p.map[omero.constants.POJOLEAVES] = rbool(True)
        pojos.loadContainerHierarchy("Project",None,  p.map)
    
    def test1088(self):
        admin = self.root.sf.getAdminService()
        q = self.root.sf.getQueryService()
        cx = admin.getEventContext()
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["uid"] = rlong(cx.userId)
        p.map['start'] = start = rtime(1218529874000)
        p.map['end'] = end = rtime(1221121874000)

        sql1 = "select el from EventLog el left outer join fetch el.event ev " \
               "where el.entityType in ('ome.model.core.Pixels', 'ome.model.core.Image', " \
               "'ome.model.containers.Dataset', 'ome.model.containers.Project') " \
               "and ev.id in (select id from Event where experimenter.id=:uid and time > :start and time < :end)"


        sql2 = "select el from EventLog el left outer join fetch el.event ev " \
               "where el.entityType in ('ome.model.core.Pixels', 'ome.model.core.Image', " \
               "'ome.model.containers.Dataset', 'ome.model.containers.Project') " \
               "and ev.experimenter.id=:uid and ev.time > :start and ev.time < :end"

        import time
        sql1_start = time.time()
        l = q.findAllByQuery(sql1, p)
        sql1_stop = time.time()
        print "\nSQL1: %s objects in %s seconds" % (str(len(l)), str(sql1_stop - sql1_start))

        sql2_start = time.time()
        l = q.findAllByQuery(sql2, p)
        sql2_stop = time.time()
        print "SQL2: %s objects in %s seconds\n" % (str(len(l)), str(sql2_stop - sql2_start))

    def test1109(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        admin = self.root.sf.getAdminService()
        
        # create data
        #group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)
        
        #new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.email = rstring("newtest@emaildomain.com")

        listOfGroups = list()
        defaultGroup = admin.lookupGroup("default")
        listOfGroups.append(admin.getGroup(gid))
        listOfGroups.append(admin.lookupGroup("user"))

        eid = admin.createExperimenter(new_exp, defaultGroup, listOfGroups)
        
        #test
        exp = admin.getExperimenter(eid)
        #print "exp: ", exp.id.val, " his default group is: ", admin.getDefaultGroup(exp.id.val).id.val
        
        gr1 = admin.getGroup(2)
        indefault = admin.containedExperimenters(gr1.id.val)
        # print "members of group %s %i" % (gr1.name.val, gr1.id.val)
        for m in indefault:
            if m.id.val == exp.id.val:
                self.assert_(m.copyGroupExperimenterMap()[0].parent.id.val == admin.getDefaultGroup(exp.id.val).id.val)
                # print "exp: id=", m.id.val, "; GEM[0]: ", type(m.copyGroupExperimenterMap()[0].parent), m.copyGroupExperimenterMap()[0].parent.id.val

        gr2 = admin.getGroup(gid)
        ing1 = admin.containedExperimenters(gr2.id.val)
        # print "members of group %s %i" % (gr2.name.val, gr2.id.val)
        for m in ing1:
            if m.id.val == exp.id.val:
                self.assert_(m.copyGroupExperimenterMap()[0].parent.id.val == admin.getDefaultGroup(exp.id.val).id.val)
                # print "exp: id=", m.id.val, "; GEM[0]: ", type(m.copyGroupExperimenterMap()[0].parent), m.copyGroupExperimenterMap()[0].parent.id.val
    
    def test1163(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        share = self.root.sf.getShareService()
        query = self.root.sf.getQueryService()
        update = self.root.sf.getUpdateService()
        admin = self.root.sf.getAdminService()
        
        ### create two users in one group
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
        client_share1 = omero.client()
        client_share1.createSession(user1.omeName.val,"ome")
        update1 = client_share1.sf.getUpdateService()
        search1 = client_share1.sf.createSearchService()
        
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
        
        # search
        search1.onlyType('Image')
        search1.addOrderByAsc("name")
        search1.setAllowLeadingWildcard(True)
        search1.byFullText("test")
        if search1.hasNext():
            res = search1.results()

            self.assert_(len(res) == 1)
        
        client_share1.sf.closeOnDestroy()
    
if __name__ == '__main__':
    unittest.main()
