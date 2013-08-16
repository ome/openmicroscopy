#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
import omero
from omero.rtypes import rstring, rtime, rlong, rint
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

class TestTickets2000(lib.ITest):

    def test1018CreationDestructionClosing(self):
        c1 = None
        c2 = None
        c3 = None
        c4 = None

        try:
            c1 = omero.client() # ok with __del__
            s1 = c1.createSession()
            s1.detachOnDestroy()
            uuid = s1.ice_getIdentity().name

            # Intermediate "disrupter"
            c2 = omero.client() # ok with __del__
            s2 = c2.createSession(uuid, uuid)
            s2.getAdminService().getEventContext()
            c2.closeSession()

            # 1 should still be able to continue
            s1.getAdminService().getEventContext()

            # Now if s1 exists another session should be able to connect
            c1.closeSession()
            c3 = omero.client() # ok with __del__
            s3 = c3.createSession(uuid, uuid)
            s3.getAdminService().getEventContext()
            c3.closeSession()

            # Now a connection should not be possible
            import Glacier2
            c4 = omero.client() # ok with __del__
            self.assertRaises(Glacier2.PermissionDeniedException, c4.joinSession, uuid)
        finally:
            c1.__del__()
            c2.__del__()
            c3.__del__()
            c4.__del__()

    def test1064(self):
        share = self.client.sf.getShareService()
        search = self.client.sf.createSearchService()
        update = self.client.sf.getUpdateService()
        admin = self.client.sf.getAdminService()
        cx = admin.getEventContext()

    def test1067(self):
        admin = self.root.sf.getAdminService()

        test_group0 = self.new_group()
        test_user = self.new_user(group = test_group0)

        # Non-memebr group
        groups = list()
        gr1 = self.new_group()
        groups.append(gr1)

        exp = admin.lookupExperimenter(test_user.omeName.val)
        contained_grs = admin.containedGroups(exp.id.val);
        # if groupexperimetnermap contains text group should be remove
        for gr in contained_grs:
            if gr.id.val == gr1.id.val:
                admin.removeGroups(exp,groups)

        admin.addGroups(exp,groups)

        admin.setDefaultGroup(exp,gr1) # thrown an exception because gr1 is not on the GroupExperimenterMap

    def test1027(self):
        uuid = self.client.sf.getAdminService().getEventContext().sessionUuid
        self.client.sf.getAdminService().lookupLdapAuthExperimenters()

    def test1069(self):
        unique = rstring(self.uuid())
        project = ProjectI()
        project.name = unique
        project.description = rstring("NOTME")
        project = self.client.sf.getUpdateService().saveAndReturnObject(project)
        self.index(project)

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
        im2.acquisitionDate = rtime(0)
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

        self.assertEquals(3, len(hier), "len of hier != 3: %s" % [type(x) for x in hier])
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

        common_group = self.new_group(perms="rwrw--")
        c1 = self.new_client(common_group)
        c2 = self.new_client(common_group)

        c1_pojos = c1.sf.getContainerService()
        c1_query = c1.sf.getQueryService()
        c1_update = c1.sf.getUpdateService()
        c1_uuid = c1.sf.getAdminService().getEventContext().sessionUuid

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
        im2.acquisitionDate = rtime(0)
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
        pdl2.setParent(pr2)
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

        self.assertEquals(2, len(hier), "size of hier != 2: %s" % [type(x) for x in hier])
        for c in hier:
            if c.id.val == pr1.id.val and isinstance(c, ProjectI):
                self.assertEquals(1, c.sizeOfDatasetLinks())
                for pdl in c.copyDatasetLinks():
                    self.assertEquals(1, pdl.child.sizeOfImageLinks())
                    for dil in pdl.child.copyImageLinks():
                        self.assert_(dil.child.id.val == im2.id.val)
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                self.assertEquals(1, c.sizeOfDatasetLinks())
            elif c.id.val == ds3.id.val and isinstance(c, DatasetI):
                self.assertEquals(1, c.sizeOfImageLinks())

    def test1072(self):
        #create two users where both are in the same active group
        admin = self.root.sf.getAdminService()
        uuid = admin.getEventContext().sessionUuid

        new_gr = self.new_group(perms="rwr---")
        c1, test_user = self.new_client_and_user(new_gr)
        c2, test_user2 = self.new_client_and_user(new_gr)

        #login as user1
        update = c1.sf.getUpdateService()

        pr1 = ProjectI()
        pr1.setName(rstring('test1072-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()

        #datasets
        ds1 = DatasetI()
        ds1.setName(rstring('test1072-ds1-%s' % (uuid)))
        ds1 = update.saveAndReturnObject(ds1)
        ds1.unload()

        pdl1 = ProjectDatasetLinkI()
        pdl1.setParent(pr1)
        pdl1.setChild(ds1)
        update.saveObject(pdl1)

        #login as user2
        pojos = c2.sf.getContainerService()

        self.assert_( c2.sf.getAdminService().getEventContext() )
        #print c1.sf.getAdminService().getEventContext()

        p = omero.sys.ParametersI()
        p.grp( rlong(c2.sf.getAdminService().getEventContext().groupId) )
        pojos.loadContainerHierarchy("Project",None,  p)

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
        # print "\nSQL1: %s objects in %s seconds" % (str(len(l)), str(sql1_stop - sql1_start))

        sql2_start = time.time()
        l = q.findAllByQuery(sql2, p)
        sql2_stop = time.time()
        # print "SQL2: %s objects in %s seconds\n" % (str(len(l)), str(sql2_stop - sql2_start))

    def test1109(self):
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # Replace defaultGroup with something new
        defaultGroup = self.new_group()

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
        # defaultGroup = admin.lookupGroup("default") Removed in 4.2
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
        members2 = admin.containedExperimenters(gr2.id.val)
        # print "members of group %s %i" % (gr2.name.val, gr2.id.val)
        for m in members2:
            if m.id.val == exp.id.val:
                copied_id = m.copyGroupExperimenterMap()[0].parent.id.val
                got_id = admin.getDefaultGroup(exp.id.val).id.val
                contained = admin.containedGroups(m.id.val)
                self.assertEquals(copied_id, got_id,\
                """
                %s != %s. Groups for experimenter %s = %s (graph) or %s (contained)
                """ % ( copied_id, got_id, exp.id.val, [ x.parent.id.val for x in m.copyGroupExperimenterMap() ], [ y.id.val for y in contained ] ))
                # print "exp: id=", m.id.val, "; GEM[0]: ", type(m.copyGroupExperimenterMap()[0].parent), m.copyGroupExperimenterMap()[0].parent.id.val

    def test1163(self):
        uuid = self.uuid()
        new_gr1 = self.new_group(perms="rw----")
        client_share1, new_exp_obj = self.new_client_and_user(new_gr1)
        update1 = client_share1.sf.getUpdateService()
        search1 = client_share1.sf.createSearchService()

        # create image and index
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img.setAcquisitionDate(rtime(0))
        img = update1.saveAndReturnObject(img)
        img.unload()
        self.index(img)

        # search
        search1.onlyType('Image')
        search1.addOrderByAsc("name")
        search1.byFullText("test*")
        self.assertTrue(search1.hasNext())
        res = search1.results()
        self.assertEquals(1, len(res))

    def test1184(self):
        uuid = self.uuid()
        client = self.new_client(perms="rw----")

        share = client.sf.getShareService()
        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()
        admin = client.sf.getAdminService()
        cont = client.sf.getContainerService()

        ds = DatasetI()
        ds.setName(rstring('test1184-ds-%s' % (uuid)))

        for i in range(1,2001):
            img = ImageI()
            img.setName(rstring('img1184-%s' % (uuid)))
            img.setAcquisitionDate(rtime(time.time()))
            # Saving in one go
            #dil = DatasetImageLinkI()
            #dil.setParent(ds)
            #dil.setChild(img)
            #update.saveObject(dil)
            ds.linkImage(img)
        ds = update.saveAndReturnObject(ds)

        c = cont.getCollectionCount(ds.__class__.__name__, ("imageLinks"), [ds.id.val], None)
        self.assert_(c[ds.id.val] == 2000)

        page = 1
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(admin.getEventContext().userId)
        p.map["oid"] = rlong(ds.id.val)
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page)-1)*24)
            p.theFilter = f

        sql = "select im from Image im join fetch im.details.owner join fetch im.details.group " \
              "left outer join fetch im.datasetLinks dil left outer join fetch dil.parent d " \
              "where d.id = :oid and im.details.owner.id=:eid order by im.id asc"

        start = time.time()
        res = query.findAllByQuery(sql,p)
        self.assertEquals(24, len(res))
        end = time.time()
        elapsed = end - start
        self.assertTrue(elapsed < 3.0,
            "Expected the test to complete in < 3 seconds, took: %f" % elapsed)

    def test1183(self):
        # Annotation added before
        p = omero.model.ProjectI()
        p.linkAnnotation( omero.model.CommentAnnotationI() )
        p.name = rstring("ticket1183")
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Annotation added after
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p.linkAnnotation( omero.model.CommentAnnotationI() )
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloading annotation after save
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p.linkAnnotation( omero.model.CommentAnnotationI() )
        p = self.update.saveAndReturnObject(p)
        for l in p.copyAnnotationLinks():
            l.child.unload()
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloaded annotation to save (before)
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject( c )
        c.unload()
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p.linkAnnotation( c )
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloaded annotation to save (after)
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject( c )
        c.unload()
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p.linkAnnotation( c )
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

if __name__ == '__main__':
    unittest.main()
