#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
                      All Rights Reserved.
   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper
   - author_testimg_generated

"""

import omero
import traceback
from omero.rtypes import rstring
from omero.cmd import State, ERR, OK
from omero.gateway.scripts import dbhelpers
import uuid
import pytest

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANN = 'rwra--'
READWRITE = 'rwrw--'


import logging
logging.basicConfig(level=logging.ERROR)


class ChmodBase (object):
    def doChange(self, gateway, group_id, permissions, test_should_pass=True,
                 return_complete=True):
        """
        Performs the chmod action, waits on completion and checks that the
        result is not an error.
        """
        prx = gateway.chmodGroup(group_id, permissions)
        if not return_complete:
            return prx
        rsp = gateway._waitOnCmd(prx, loops=20, ms=500, failonerror=False)
        assert rsp is not None

        if test_should_pass:
            assert not isinstance(rsp, ERR), \
                "Found ERR when test_should_pass==true: %s (%s) params=%s" % \
                (rsp.category, rsp.name, rsp.parameters)
            assert State.FAILURE not in prx.getStatus().flags
        else:
            assert not isinstance(rsp, OK), \
                "Found OK when test_should_pass==false: %s" % rsp
            assert State.FAILURE in prx.getStatus().flags
        return rsp

    def assertCanEdit(self, blitzObject, expected=True,
                      sudo_needed=False, exc_info=False):
        """
        Checks the canEdit() method AND actual behavior (ability to edit)
        """

        assert blitzObject.canEdit() == expected, \
            "Unexpected result of canEdit(). Expected: %s" % expected
        gateway = blitzObject._conn
        # Now test if we can actually Edit and 'Hard link' the object
        nameEdited = False
        # for saves, omero group must *not* be -1
        # need to switch back to this after edits
        origGroup = gateway.SERVICE_OPTS.getOmeroGroup()
        gid = blitzObject.details.group.id.val
        gateway.SERVICE_OPTS.setOmeroGroup(gid)
        try:
            blitzObject.setName("new name: %s" % uuid.uuid4())
            blitzObject.save()
            nameEdited = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                nameEdited = True  # assume ok
        except omero.SecurityViolation:
            if exc_info:
                traceback.print_exc()

        objectUsed = False
        try:
            obj = blitzObject._obj
            if isinstance(obj, omero.model.Image):
                ds = omero.model.DatasetI()
                ds.setName(omero.rtypes.rstring("assertCanEdit"))
                link = omero.model.DatasetImageLinkI()
                link.setParent(ds)
                link.setChild(obj)
                update = gateway.getUpdateService()
                update.saveObject(link, gateway.SERVICE_OPTS)
            elif isinstance(obj, omero.model.Project):
                ds = omero.model.DatasetI()
                ds.setName(omero.rtypes.rstring("assertCanEdit"))
                link = omero.model.ProjectDatasetLinkI()
                link.setParent(obj)
                link.setChild(ds)
                update = gateway.getUpdateService()
                update.saveObject(link, gateway.SERVICE_OPTS)
            else:
                raise Exception("Unknown type: %s" % blitzObject)
            objectUsed = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                objectUsed = True  # assume ok
        except omero.SecurityViolation:
            if exc_info:
                traceback.print_exc()

        gateway.SERVICE_OPTS.setOmeroGroup(origGroup)  # revert back
        assert blitzObject.canEdit() == expected, \
            "Unexpected result of canEdit(). Expected: %s" % expected
        assert nameEdited == expected, \
            "Unexpected ability to Edit. Expected: %s" % expected
        assert objectUsed | sudo_needed == expected, \
            "Unexpected ability to Use. Expected: %s" % expected

    def assertCanAnnotate(self, blitzObject, expected=True,
                          sudo_needed=False, exc_info=False):
        """
        Checks the canAnnotate() method AND actual behavior (ability to
        annotate)
        """

        assert blitzObject.canAnnotate() == expected, \
            "Unexpected result of canAnnotate(). Expected: %s" % expected
        annotated = False
        try:
            omero.gateway.CommentAnnotationWrapper.createAndLink(
                target=blitzObject, ns="gatewaytest.chmod.testCanAnnotate",
                val="Test Comment")
            annotated = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                annotated = True  # assume ok
        except omero.SecurityViolation:
            if exc_info:
                traceback.print_exc()
        assert annotated == expected, \
            "Unexpected ability to Annotate. Expected: %s" % expected


class TestChmodGroup (ChmodBase):
    @pytest.fixture(autouse=True)
    def setUp(self):
        """ Create a group with Admin & Owner members"""
        # readonly with an Admin user
        dbhelpers.USERS['chmod_group_admin'] = dbhelpers.UserEntry(
            'r-_chmod_admin', 'ome',
            firstname='chmod',
            lastname='admin',
            groupname="ReadOnly_chmod_group",
            groupperms=READONLY,
            admin=True)
        dbhelpers.USERS['chmod_group_owner'] = dbhelpers.UserEntry(
            'r-_chmod_owner', 'ome',
            firstname='chmod',
            lastname='owner',
            groupname="ReadOnly_chmod_group",
            groupperms=READONLY,
            groupowner=True)
        dbhelpers.bootstrap(onlyUsers=True)

    def testChmod(self, gatewaywrapper):
        """ Test change of group permissions """

        # Login as group Admin to get group Id...
        gatewaywrapper.doLogin(dbhelpers.USERS['chmod_group_admin'])
        group_Id = gatewaywrapper.gateway.getEventContext().groupId
        group_Name = gatewaywrapper.gateway.getEventContext().groupName
        # do we need to log out of group when changing it's permissions??
        gatewaywrapper.doDisconnect()
        # let another Admin change group permissions
        gatewaywrapper.loginAsAdmin()
        dbhelpers.UserEntry.check_group_perms(gatewaywrapper.gateway,
                                              group_Name, READONLY)
        self.doChange(gatewaywrapper.gateway, group_Id, READWRITE)
        dbhelpers.UserEntry.check_group_perms(gatewaywrapper.gateway,
                                              group_Name, READWRITE)


class TestCustomUsers (ChmodBase):
    """
    Here we're creating 3 groups with different permissions (read-only,
    read-annotate, read-write).
    Each group has a user who owns the data (Project), another user, an admin
    and a group leader (groupowner).
    Then we have a test for each group, testing whether each user canEdit()
    and canAnnotate() the data.
    """

    @pytest.fixture(autouse=True)
    def setUp(self):
        # read-only users & data
        def ReadOnly(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_only_%s' % key] = dbhelpers.UserEntry(
                "r-_%s" % key, 'ome',
                firstname='chmod',
                lastname='test',
                groupname="ReadOnly_chmod_test",
                groupperms=READONLY,
                groupowner=groupowner,
                admin=admin)
        ReadOnly('owner')
        ReadOnly('user')
        ReadOnly('admin', admin=True)
        ReadOnly('leader', groupowner=True)
        dbhelpers.PROJECTS['read_only_proj'] = dbhelpers.ProjectEntry(
            'read_only_proj', 'read_only_owner')
        dbhelpers.PROJECTS['read_only_proj_2'] = dbhelpers.ProjectEntry(
            'read_only_proj_2', 'read_only_owner')

        # read-annotate users & data
        def ReadAnn(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_ann_%s' % key] = dbhelpers.UserEntry(
                "ra_%s" % key, 'ome',
                firstname='chmod',
                lastname='test',
                groupname="ReadAnn_chmod_test",
                groupperms=READANN,
                groupowner=groupowner,
                admin=admin)
        ReadAnn('owner')
        ReadAnn('user')
        ReadAnn('admin', admin=True)
        ReadAnn('leader', groupowner=True)
        dbhelpers.PROJECTS['read_ann_proj'] = dbhelpers.ProjectEntry(
            'read_ann_proj', 'read_ann_owner')

        # read-write users & data
        def ReadWrite(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_write_%s' % key] = dbhelpers.UserEntry(
                "rw_%s" % key, 'ome',
                firstname='chmod',
                lastname='test',
                groupname="ReadWrite_chmod_test",
                groupperms=READWRITE,
                groupowner=groupowner,
                admin=admin)
        ReadWrite('owner')
        ReadWrite('user')
        ReadWrite('admin', admin=True)
        ReadWrite('leader', groupowner=True)
        dbhelpers.PROJECTS['read_write_proj'] = dbhelpers.ProjectEntry(
            'read_write_proj', 'read_write_owner')

        dbhelpers.bootstrap()

    def testReadOnly(self, gatewaywrapper):
        """
        In a read-only group, user should NOT be able to Edit or Annotate
        """
        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_owner'])
        p = dbhelpers.getProject(gatewaywrapper.gateway, 'read_only_proj')
        p2 = dbhelpers.getProject(gatewaywrapper.gateway, 'read_only_proj_2')
        pid = p.id
        pid2 = p2.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)
        # Test Bug from #9505 commits: Second Project canEdit() is False
        pros = gatewaywrapper.gateway.getObjects("Project", [pid, pid2])
        for p in pros:
            self.assertCanEdit(p, True)

        # Login as user...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_user'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, False)
        self.assertCanAnnotate(p, False)

        # Login as admin...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_admin'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, sudo_needed=True)
        self.assertCanAnnotate(p, True, sudo_needed=True)

        # Login as group leader...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_leader'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, sudo_needed=True)
        self.assertCanAnnotate(p, True, sudo_needed=True)

    def testReadAnnotate(self, gatewaywrapper):
        """
        In a read-annotate group, user should be able to Annotate but NOT Edit
        """
        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_owner'])
        p = dbhelpers.getProject(gatewaywrapper.gateway, 'read_ann_proj')
        pid = p.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as user...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_user'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, False)
        self.assertCanAnnotate(p, True, exc_info=1)

        # Login as admin...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_admin'])
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_leader'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

    def testGroupMinusOne(self, gatewaywrapper):
        """
        Should be able to Annotate and Edit object retrieved with
        omero.group:'-1'
        """
        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_owner'])
        p = dbhelpers.getProject(gatewaywrapper.gateway, 'read_ann_proj')
        pid = p.id
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        # Need to get object again since p.save() in assertCanEdit() reloads
        # it under different context
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_ann_leader'])
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanAnnotate(p, True)

    def testReadWrite(self, gatewaywrapper):
        """ In a read-write group, all should be able to Annotate and Edit"""
        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_owner'])
        p = dbhelpers.getProject(gatewaywrapper.gateway, 'read_write_proj')
        pid = p.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as user...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_user'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, exc_info=1)
        self.assertCanAnnotate(p, True)

        # Login as admin...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_admin'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_leader'])
        p = gatewaywrapper.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

    def testDelete8723(self, gatewaywrapper):
        """
        Tests whether regular members can delete each other's data in rwrw--
        group
        """
        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_owner'])
        pr = omero.model.ProjectI()
        pr.name = rstring("test-delete")
        pr = gatewaywrapper.gateway.getUpdateService().saveAndReturnObject(pr)
        # Login as regular member
        gatewaywrapper.doLogin(dbhelpers.USERS['read_write_user'])
        p = gatewaywrapper.gateway.getObject("Project", pr.id.val)
        assert None != p, "Member can access Project"
        assert p.canDelete() is True, \
            "Member can delete another user's Project"
        handle = gatewaywrapper.gateway.deleteObjects("Project", [pr.id.val])
        gatewaywrapper.gateway._waitOnCmd(handle)

        # Must reload project
        p = gatewaywrapper.gateway.getObject("Project", pr.id.val)
        assert None == p, "Project should be Deleted"


class TestManualCreateEdit (ChmodBase):
    """
    Here we test whether an object created and saved using update service can
    be edited by another user
    """

    def testReadOnly(self, gatewaywrapper):
        """
        In a read-only group, user should NOT be able to Edit or Annotate
        """
        dbhelpers.USERS['read_only_owner'] = dbhelpers.UserEntry(
            'r-_owner', 'ome', firstname='chmod', lastname='test',
            groupname="ReadOnly_chmod_test", groupperms=READONLY)
        dbhelpers.USERS['read_only_user'] = dbhelpers.UserEntry(
            'r-_user', 'ome', firstname='chmod2', lastname='test',
            groupname="ReadOnly_chmod_test", groupperms=READONLY)

        dbhelpers.bootstrap(onlyUsers=True)

        # Login as owner...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_owner'])
        p = omero.model.ProjectI()
        p.setName(rstring("test_create_read_only_project"))
        p = gatewaywrapper.gateway.getUpdateService().saveAndReturnObject(p)

        # Login as user...
        gatewaywrapper.doLogin(dbhelpers.USERS['read_only_user'])
        project = gatewaywrapper.gateway.getObject("Project", p.id.val)
        self.assertCanEdit(project, False)
        self.assertCanAnnotate(project, False)


class Test8800 (object):
    """
    Test for #8800 where ImageWrapper.canEdit() etc return different
    values after we load Pixels
    """

    def testWithBlitzWrappers(self, author_testimg_generated):
        """
        Uses ImageWrapper.getPrimaryPixels() which loads pixels on the fly
        """
        image = author_testimg_generated
        before = image.canEdit()
        image.getPrimaryPixels()
        after = image.canEdit()
        assert before == after, \
            "canEdit() affected by ImageWrapper.getPrimaryPixels()"

    def testWithoutWrappers(self, gatewaywrapper, author_testimg_generated):
        """
        Here we can test loading the image again (with Pixels loaded) using
        different values of omero.group.
        Bug #8800 is due to the image returned with 'omero.group':'-1' has
        canEdit() = False.
        """

        image = author_testimg_generated
        imgObj = image._obj
        gid = image.getDetails().group.id.val
        before = imgObj.getDetails().getPermissions().canEdit()
        ctx = {'omero.group': str(gid)}
        imgObj = gatewaywrapper.gateway.getContainerService().getImages(
            "Image", (imgObj.id.val,), None, ctx)[0]
        after = imgObj.getDetails().getPermissions().canEdit()
        assert before == after, \
            "canEdit() affected by loading image with 'omero.group':gid"

        ctx = {'omero.group': '-1'}
        imgObj = gatewaywrapper.gateway.getContainerService().getImages(
            "Image", (imgObj.id.val,), None, ctx)[0]
        after = imgObj.getDetails().getPermissions().canEdit()
        assert before == after, \
            "canEdit() should not be affected by 'omero.group':'-1'"


class TestDefaultSetup (object):

    def testAuthorCanEdit(self, gatewaywrapper, author_testimg_generated):
        """
        Tests whether the default Users created by default setUp() canEdit
        their Images etc.
        """

        image = author_testimg_generated
        imageId = image.id
        gatewaywrapper.gateway.getAdminService().getEventContext()

        group = gatewaywrapper.gateway.getGroupFromContext()
        image_gid = image.getDetails().getGroup().id
        image.getDetails().getGroup().name

        # Author should be able to Edit and Annotate their own data
        assert image.canEdit(), "Author can edit their own image"
        assert image.canAnnotate(), "Author can annotate their own image"

        # Login as Admin
        gatewaywrapper.loginAsAdmin()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        i = gatewaywrapper.gateway.getObject("Image", imageId)
        assert i.canEdit(), "Admin can edit Author's image"
        assert i.canAnnotate(), "Admin can annotate Author's image"

        # Login as default "User"
        # NB: seems this user is not in same group as Author's image.
        gatewaywrapper.loginAsUser()
        gatewaywrapper.gateway.SERVICE_OPTS.setOmeroGroup('-1')
        i = gatewaywrapper.gateway.getObject("Image", imageId)
        assert None == i, \
            "User cannot access Author's image in Read-only group"

        # Create new user in the same group
        gatewaywrapper.loginAsAdmin()
        # groupname = image_gname
        chmod_test_user = dbhelpers.UserEntry(
            'chmod_test_user6', 'foobar', firstname='User', lastname='Chmod')
        chmod_test_user.create(gatewaywrapper.gateway, dbhelpers.ROOT.passwd)
        admin = gatewaywrapper.gateway.getAdminService()
        user = admin.lookupExperimenter('chmod_test_user6')
        group = admin.getGroup(image_gid)
        admin.addGroups(user, [group])

        gatewaywrapper.doLogin(chmod_test_user)
        user = gatewaywrapper.gateway.getUser()
        # switch into group
        assert gatewaywrapper.gateway.setGroupForSession(image_gid)
        assert image_gid == gatewaywrapper.gateway.getEventContext().groupId,\
            "Confirm in same group as image"
        i = gatewaywrapper.gateway.getObject("Image", imageId)
        assert i is not None, \
            "User cannot access Author's image in Read-only group: %s" % i
