#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import time
import test.integration.library as lib
import pytest
import omero
from omero.rtypes import rstring, rtime, rlong, rint
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ProjectDatasetLinkI import ProjectDatasetLinkI


class TestTickets2000(lib.ITest):

    def test1064(self):
        admin = self.client.sf.getAdminService()
        admin.getEventContext()

    def test1067(self):
        admin = self.root.sf.getAdminService()

        test_group0 = self.new_group()
        test_user = self.new_user(group=test_group0)

        # Non-memebr group
        groups = list()
        gr1 = self.new_group()
        groups.append(gr1)

        exp = admin.lookupExperimenter(test_user.omeName.val)
        contained_grs = admin.containedGroups(exp.id.val)
        # if groupexperimetnermap contains text group should be remove
        for gr in contained_grs:
            if gr.id.val == gr1.id.val:
                admin.removeGroups(exp, groups)

        admin.addGroups(exp, groups)

        # thrown an exception because gr1 is not on the GroupExperimenterMap
        admin.setDefaultGroup(exp, gr1)

    def test1027(self):
        self.client.sf.getAdminService().getEventContext().sessionUuid
        self.client.sf.getAdminService().lookupLdapAuthExperimenters()

    @pytest.mark.xfail(reason="See ticket #11539")
    def test1069(self):
        unique = rstring(self.uuid())
        project = ProjectI()
        project.name = unique
        project.description = rstring("NOTME")
        project = self.client.sf.getUpdateService().saveAndReturnObject(
            project)
        self.index(project)

        search = self.client.sf.createSearchService()
        search.onlyType("Project")

        search.bySomeMustNone([unique.val], [], ["NOTME"])
        assert not search.hasNext()

        search.bySomeMustNone([unique.val], [], ["NOTME", "SOMETHINGELSE"])
        assert not search.hasNext()

        search.bySomeMustNone([unique.val], [], [])
        assert search.hasNext()

    def test1071(self):
        uuid = self.root.sf.getAdminService().getEventContext().sessionUuid
        pojos = self.root.sf.getContainerService()
        update = self.root.sf.getUpdateService()

        # projects
        pr1 = ProjectI()
        pr1.setName(rstring('test1071-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(rstring('test1071-pr2-%s' % (uuid)))
        pr2 = update.saveAndReturnObject(pr2)
        pr2.unload()

        # datasets
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

        # images
        im2 = ImageI()
        im2.setName(rstring('test1071-im2-%s' % (uuid)))
        im2 = update.saveAndReturnObject(im2)
        im2.unload()

        # links
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

        # test:
        hier = pojos.findContainerHierarchies(
            "Project", [long(im2.id.val)], None)

        assert 3 == len(hier), \
            "len of hier != 3: %s" % [type(x) for x in hier]
        for c in hier:
            if c.id.val == pr1.id.val and isinstance(c, ProjectI):
                assert c.sizeOfDatasetLinks() == 2, "length 2 != " + \
                    str(c.sizeOfDatasetLinks())
                for pdl in c.copyDatasetLinks():
                    assert pdl.child.sizeOfImageLinks() == 1
                    for dil in pdl.child.copyImageLinks():
                        assert dil.child.id.val == im2.id.val
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                assert c.sizeOfDatasetLinks() == 1
            elif c.id.val == ds3.id.val and isinstance(c, DatasetI):
                assert c.sizeOfImageLinks() == 1

    def test1071_1(self):
        common_group = self.new_group(perms="rwrw--")
        c1 = self.new_client(common_group)
        c2 = self.new_client(common_group)

        c1_update = c1.sf.getUpdateService()
        c1_uuid = c1.sf.getAdminService().getEventContext().sessionUuid

        c2_pojos = c2.sf.getContainerService()
        c2_update = c2.sf.getUpdateService()
        c2_uuid = c2.sf.getAdminService().getEventContext().sessionUuid

        # projects
        pr1 = ProjectI()
        pr1.setName(rstring('test1071-pr1-%s' % (c1_uuid)))
        pr1 = c1_update.saveAndReturnObject(pr1)
        pr1.unload()

        pr2 = ProjectI()
        pr2.setName(rstring('test1071-pr2-%s' % (c2_uuid)))
        pr2 = c2_update.saveAndReturnObject(pr2)
        pr2.unload()

        # datasets
        ds1 = DatasetI()
        ds1.setName(rstring('test1071-ds1-%s' % (c1_uuid)))
        ds1 = c1_update.saveAndReturnObject(ds1)
        ds1.unload()

        ds2 = DatasetI()
        ds2.setName(rstring('test1071-ds2-%s' % (c2_uuid)))
        ds2 = c2_update.saveAndReturnObject(ds2)
        ds2.unload()

        # images
        im2 = ImageI()
        im2.setName(rstring('test1071-im2-%s' % (c2_uuid)))
        im2 = c2_update.saveAndReturnObject(im2)
        im2.unload()

        # links
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

        # test:
        hier = c2_pojos.findContainerHierarchies(
            "Project", [long(im2.id.val)], None)

        assert 2 == len(hier), "size of hier != 2: %s" % \
            [type(x) for x in hier]
        for c in hier:
            if c.id.val == pr1.id.val and isinstance(c, ProjectI):
                assert 1 == c.sizeOfDatasetLinks()
                for pdl in c.copyDatasetLinks():
                    assert 1 == pdl.child.sizeOfImageLinks()
                    for dil in pdl.child.copyImageLinks():
                        assert dil.child.id.val == im2.id.val
            elif c.id.val == pr2.id.val and isinstance(c, ProjectI):
                assert 1 == c.sizeOfDatasetLinks()
            elif c.id.val == ds2.id.val and isinstance(c, DatasetI):
                assert 1 == c.sizeOfImageLinks()

    def test1072(self):
        # create two users where both are in the same active group
        admin = self.root.sf.getAdminService()
        uuid = admin.getEventContext().sessionUuid

        new_gr = self.new_group(perms="rwr---")
        c1, test_user = self.new_client_and_user(new_gr)
        c2, test_user2 = self.new_client_and_user(new_gr)

        # login as user1
        update = c1.sf.getUpdateService()

        pr1 = ProjectI()
        pr1.setName(rstring('test1072-pr1-%s' % (uuid)))
        pr1 = update.saveAndReturnObject(pr1)
        pr1.unload()

        # datasets
        ds1 = DatasetI()
        ds1.setName(rstring('test1072-ds1-%s' % (uuid)))
        ds1 = update.saveAndReturnObject(ds1)
        ds1.unload()

        pdl1 = ProjectDatasetLinkI()
        pdl1.setParent(pr1)
        pdl1.setChild(ds1)
        update.saveObject(pdl1)

        # login as user2
        pojos = c2.sf.getContainerService()

        assert c2.sf.getAdminService().getEventContext()
        # print c1.sf.getAdminService().getEventContext()

        p = omero.sys.ParametersI()
        p.grp(rlong(c2.sf.getAdminService().getEventContext().groupId))
        pojos.loadContainerHierarchy("Project", None,  p)

    def test1088(self):
        admin = self.root.sf.getAdminService()
        q = self.root.sf.getQueryService()
        cx = admin.getEventContext()

        p = omero.sys.Parameters()
        p.map = {}
        p.map["uid"] = rlong(cx.userId)
        p.map['start'] = rtime(1218529874000)
        p.map['end'] = rtime(1221121874000)

        sql1 = "select el from EventLog el left outer join " \
               "fetch el.event ev where el.entityType in " \
               "('ome.model.core.Pixels', 'ome.model.core.Image', " \
               "'ome.model.containers.Dataset', " \
               "'ome.model.containers.Project') " \
               "and ev.id in (select id from Event where " \
               "experimenter.id=:uid and time > :start and time < :end)"

        sql2 = "select el from EventLog el left outer join "\
               "fetch el.event ev where el.entityType in " \
               "('ome.model.core.Pixels', 'ome.model.core.Image', " \
               "'ome.model.containers.Dataset', " \
               "'ome.model.containers.Project') " \
               "and ev.experimenter.id=:uid " \
               "and ev.time > :start and ev.time < :end"

        # Much of the timing code here was already commented out, to fix
        # flake8 warnings the whole lot is commented out with just the two
        # queries repeated below.
        # import time
        # sql1_start = time.time()
        # l = q.findAllByQuery(sql1, p)
        # sql1_stop = time.time()
        # # print "\nSQL1: %s objects in %s seconds" % (str(len(l)),
        # # str(sql1_stop - sql1_start))
        q.findAllByQuery(sql1, p)

        # sql2_start = time.time()
        # l = q.findAllByQuery(sql2, p)
        # sql2_stop = time.time()
        # # print "SQL2: %s objects in %s seconds\n" % (str(len(l)),
        # # str(sql2_stop - sql2_start))
        q.findAllByQuery(sql2, p)

    def test1109(self):
        uuid = self.uuid()
        admin = self.root.sf.getAdminService()

        # Replace defaultGroup with something new
        defaultGroup = self.new_group()

        # create data
        # group1
        new_gr1 = ExperimenterGroupI()
        new_gr1.name = rstring("group1_%s" % uuid)
        gid = admin.createGroup(new_gr1)

        # new user1
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

        # test
        exp = admin.getExperimenter(eid)
        # print "exp: ", exp.id.val, " his default group is: ",
        # admin.getDefaultGroup(exp.id.val).id.val

        gr1 = admin.getGroup(2)
        indefault = admin.containedExperimenters(gr1.id.val)
        # print "members of group %s %i" % (gr1.name.val, gr1.id.val)
        for m in indefault:
            if m.id.val == exp.id.val:
                assert m.copyGroupExperimenterMap()[0].parent.id.val == \
                    admin.getDefaultGroup(exp.id.val).id.val
                # print "exp: id=", m.id.val, "; GEM[0]: ",
                # type(m.copyGroupExperimenterMap()[0].parent),
                # m.copyGroupExperimenterMap()[0].parent.id.val

        gr2 = admin.getGroup(gid)
        members2 = admin.containedExperimenters(gr2.id.val)
        # print "members of group %s %i" % (gr2.name.val, gr2.id.val)
        for m in members2:
            if m.id.val == exp.id.val:
                copied_id = m.copyGroupExperimenterMap()[0].parent.id.val
                got_id = admin.getDefaultGroup(exp.id.val).id.val
                contained = admin.containedGroups(m.id.val)
                assert copied_id == got_id, \
                    """
                    %s != %s. Groups for experimenter %s = %s
                     (graph) or %s (contained)
                    """ % (
                        copied_id, got_id, exp.id.val,
                        [x.parent.id.val for x in
                            m.copyGroupExperimenterMap()],
                        [y.id.val for y in contained])
                # print "exp: id=", m.id.val, "; GEM[0]: ",
                # type(m.copyGroupExperimenterMap()[0].parent),
                # m.copyGroupExperimenterMap()[0].parent.id.val

    @pytest.mark.xfail(reason="See ticket #11539")
    def test1163(self):
        uuid = self.uuid()
        new_gr1 = self.new_group(perms="rw----")
        client_share1, new_exp_obj = self.new_client_and_user(new_gr1)
        update1 = client_share1.sf.getUpdateService()
        search1 = client_share1.sf.createSearchService()

        # create image and index
        img = ImageI()
        img.setName(rstring('test1154-img-%s' % (uuid)))
        img = update1.saveAndReturnObject(img)
        img.unload()
        self.index(img)

        # search
        search1.onlyType('Image')
        search1.addOrderByAsc("name")
        search1.byFullText("test*")
        assert search1.hasNext()
        res = search1.results()
        assert 1 == len(res)

    @pytest.mark.xfail(reason="ticket 11543")
    def test1184(self):
        uuid = self.uuid()
        client = self.new_client(perms="rw----")

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()
        admin = client.sf.getAdminService()
        cont = client.sf.getContainerService()

        ds = DatasetI()
        ds.setName(rstring('test1184-ds-%s' % (uuid)))

        for i in range(1, 2001):
            img = ImageI()
            img.setName(rstring('img1184-%s' % (uuid)))
            # Saving in one go
            # dil = DatasetImageLinkI()
            # dil.setParent(ds)
            # dil.setChild(img)
            # update.saveObject(dil)
            ds.linkImage(img)
        ds = update.saveAndReturnObject(ds)

        c = cont.getCollectionCount(
            ds.__class__.__name__, ("imageLinks"), [ds.id.val], None)
        assert c[ds.id.val] == 2000

        page = 1
        p = omero.sys.Parameters()
        p.map = {}
        p.map["eid"] = rlong(admin.getEventContext().userId)
        p.map["oid"] = rlong(ds.id.val)
        if page is not None:
            f = omero.sys.Filter()
            f.limit = rint(24)
            f.offset = rint((int(page) - 1) * 24)
            p.theFilter = f

        sql = "select im from Image im join fetch im.details.owner " \
              "join fetch im.details.group left outer join fetch " \
              "im.datasetLinks dil left outer join fetch dil.parent d " \
              "where d.id = :oid and im.details.owner.id=:eid " \
              "order by im.id asc"

        start = time.time()
        res = query.findAllByQuery(sql, p)
        assert 24 == len(res)
        end = time.time()
        elapsed = end - start
        assert elapsed < 3.0,\
            "Expected the test to complete in < 3 seconds, took: %f" % elapsed

    def test1183(self):
        # Annotation added before
        p = omero.model.ProjectI()
        p.linkAnnotation(omero.model.CommentAnnotationI())
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
        p.linkAnnotation(omero.model.CommentAnnotationI())
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloading annotation after save
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p.linkAnnotation(omero.model.CommentAnnotationI())
        p = self.update.saveAndReturnObject(p)
        for l in p.copyAnnotationLinks():
            l.child.unload()
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloaded annotation to save (before)
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        c.unload()
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p.linkAnnotation(c)
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloaded annotation to save (after)
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        c.unload()
        p = omero.model.ProjectI()
        p.name = rstring("ticket1183")
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p.linkAnnotation(c)
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)
