#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the omero.api.IShare interface
   a running server.

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import time
import library as lib
import pytest
import omero
from omero.rtypes import rbool, rstring, rtime, rlong, rint
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI


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

    def test1069(self):
        unique = rstring(self.uuid())
        project = self.make_project(name=unique, description="NOTME")
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
        uuid = self.ctx.sessionUuid
        pojos = self.sf.getContainerService()

        # projects
        pr1 = self.make_project(name='test1071-pr1-%s' % (uuid))
        pr2 = self.make_project(name='test1071-pr2-%s' % (uuid))

        # datasets
        ds1 = self.new_dataset(name='test1071-ds1-%s' % (uuid))
        ds2 = self.new_dataset(name='test1071-ds2-%s' % (uuid))
        ds3 = self.make_dataset(name='test1071-ds3-%s' % (uuid))

        # images
        im2 = self.make_image(name='test1071-im2-%s' % (uuid))

        # links
        #
        # im2 -> ds3
        #    +-> ds2 --> pr2
        #    |       \
        #    \-> ds1 --> pr1
        #
        self.link(pr1, ds1)
        self.link(pr1, ds2)
        self.link(pr2, ds2)
        self.link(ds1, im2)
        self.link(ds2, im2)
        self.link(ds3, im2)

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

        c1_uuid = c1.sf.getAdminService().getEventContext().sessionUuid

        c2_pojos = c2.sf.getContainerService()
        c2_uuid = c2.sf.getAdminService().getEventContext().sessionUuid

        # projects
        pr1 = self.make_project(name='test1071-pr1-%s' % (c1_uuid), client=c1)
        pr2 = self.make_project(name='test1071-pr2-%s' % (c2_uuid), client=c2)

        # datasets
        ds1 = self.make_dataset(name='test1071-ds1-%s' % (c1_uuid), client=c1)
        ds2 = self.make_dataset(name='test1071-ds2-%s' % (c2_uuid), client=c2)

        # images
        im2 = self.make_image(name='test1071-im2-%s' % (c2_uuid), client=c2)
        im2.unload()

        # links
        # im2 owned by u2
        #
        # im2   -> ds2 --> pr2 (owned by u2)
        #      |
        #      \-> ds1 --> pr1 (owned by u1)
        #
        self.link(pr1, ds1, client=c1)
        self.link(pr2, ds2, client=c2)
        self.link(ds2, im2, client=c2)
        self.link(ds1, im2, client=c1)

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
        pr1 = self.new_project(name='test1072-pr1-%s' % (uuid))
        ds1 = self.new_dataset(name='test1072-ds1-%s' % (uuid))
        self.link(pr1, ds1, client=c1)

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
        new_gr1.ldap = rbool(False)
        gid = admin.createGroup(new_gr1)

        # new user1
        new_exp = ExperimenterI()
        new_exp.omeName = rstring("user_%s" % uuid)
        new_exp.firstName = rstring("New")
        new_exp.lastName = rstring("Test")
        new_exp.ldap = rbool(False)
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

    @pytest.mark.broken(ticket="11543")
    def test1184(self):
        uuid = self.uuid()
        client = self.new_client(perms="rw----")

        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()
        admin = client.sf.getAdminService()
        cont = client.sf.getContainerService()

        ds = self.new_dataset(name='test1184-ds-%s' % (uuid))

        for i in range(1, 2001):
            img = self.new_image(name='img1184-%s' % (uuid))
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
        p = self.new_project(name="ticket1183")
        p.linkAnnotation(omero.model.CommentAnnotationI())
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)

        # Annotation added after
        p = self.make_project(name="ticket1183")
        p.description = rstring("desc")
        p.linkAnnotation(omero.model.CommentAnnotationI())
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloading annotation after save
        p = self.new_project(name="ticket1183")
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
        p = self.new_project(name="ticket1183")
        p.linkAnnotation(c)
        p = self.update.saveAndReturnObject(p)
        p.description = rstring("desc")
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)

        # Unloaded annotation to save (after)
        c = omero.model.CommentAnnotationI()
        c = self.update.saveAndReturnObject(c)
        c.unload()
        p = self.make_project(name="ticket1183")
        p.description = rstring("desc")
        p.linkAnnotation(c)
        p = self.update.saveAndReturnObject(p)
        p = self.update.saveAndReturnObject(p)
