#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero
import unittest
import traceback
import time
from omero.rtypes import *
from omero.cmd import *
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway
from omero.gateway.scripts import dbhelpers
import omero_ext.uuid as _uuid # see ticket:3774

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANN = 'rwra--'
READWRITE = 'rwrw--'

import gatewaytest.library as lib

import logging
logging.basicConfig(level=logging.ERROR)


class ChmodBaseTest (lib.GTest):

    def setUp(self):

        try:
            super(ChmodBaseTest, self).setUp()
        except dbhelpers.BadGroupPermissionsException, bgpe:
            super(ChmodBaseTest, self).doLogin(dbhelpers.ROOT, "system")
            # For every dbhelpers.USERS entry, check that the requested
            # group has the requests permissions and if not, call chmod
            admin = self.gateway.getAdminService()
            for k, v in dbhelpers.USERS.items():
                name = v.groupname
                perms = v.groupperms
                if not perms:
                    if not name:
                        continue # These are likely the weblitz tests
                    else:
                        raise Exception("Missing permissions for %s" % name)
                try:
                    group = admin.lookupGroup(name)
                    if str(perms) != str(group.details.permissions):
                        group_id = self.__group_id(name)
                        self.doChange(group_id, perms)
                except omero.ApiUsageException:
                    # Assume that it doesn't exist
                    # and when created it'll have the
                    #proper perms
                    pass
            super(ChmodBaseTest, self).setUp()

    def doLogin(self, userEntry, groupname=None):
        """
        Attempts to reset group permissions back to the expected value
        on login.
        """

        try:
            super(ChmodBaseTest, self).doLogin(userEntry)
        except dbhelpers.BadGroupPermissionsException, bgpe:
            super(ChmodBaseTest, self).doLogin(self.ADMIN, "system")
            group_id = self.__group_id(userEntry.groupname)
            self.doChange(group_id, userEntry.groupperms)
            super(ChmodBaseTest, self).doLogin(userEntry)

    def __group_id(self, group):
        admin = self.gateway.getAdminService()
        if isinstance(group, str):
            return admin.lookupGroup(group).id.val
        raise Exception("Unknown: %s" % group)

    def doChange(self, group_id, permissions, test_should_pass=True, return_complete=True):
        """
        Performs the chmod action, waits on completion and checks that the
        result is not an error.
        """
        prx = self.gateway.chmodGroup(group_id, permissions)
        if not return_complete:
            return prx

        cb = CmdCallbackI(self.gateway.c, prx)
        cb.loop(20, 500)    # This fails (see #8439). Can comment out this line and
        # try a different way of waiting for result - This never completes...
        import time
        rsp = prx.getResponse()
        i = 0
        while (rsp is None):
            time.sleep(10)
            rsp = prx.getResponse()
            i += 1
            print "rsp", i, rsp

        self.assertNotEqual(rsp, None)

        status = prx.getStatus()

        if test_should_pass:
            if isinstance(rsp, ERR):
                self.fail("Found ERR when test_should_pass==true: %s (%s) params=%s" % (rsp.category, rsp.name, rsp.parameters))
            self.assertFalse(State.FAILURE in prx.getStatus().flags)
        else:
            if isinstance(rsp, OK):
                self.fail("Found OK when test_should_pass==false: %s", rsp)
            self.assertTrue(State.FAILURE in prx.getStatus().flags)
        return rsp

    def assertCanEdit(self, blitzObject, expected=True,
            sudo_needed=False, exc_info=False):
        """ Checks the canEdit() method AND actual behavior (ability to edit) """

        nameEdited = False
        try:
            blitzObject.setName("new name: %s" % _uuid.uuid4())
            blitzObject.save()
            nameEdited = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                nameEdited = True # assume ok
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
                update = self.gateway.getUpdateService()
                rv = update.saveObject(link, self.gateway.CONFIG['SERVICE_OPTS'])
            elif isinstance(obj, omero.model.Project):
                ds = omero.model.DatasetI()
                ds.setName(omero.rtypes.rstring("assertCanEdit"))
                link = omero.model.ProjectDatasetLinkI()
                link.setParent(obj)
                link.setChild(ds)
                update = self.gateway.getUpdateService()
                rv = update.saveObject(link, self.gateway.CONFIG['SERVICE_OPTS'])
            else:
                raise Exception("Unknown type: %s" % blitzObject)
            objectUsed = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                objectUsed = True # assume ok
        except omero.SecurityViolation:
            if exc_info:
                traceback.print_exc()

        self.assertEqual(blitzObject.canEdit(), expected, "Unexpected result of canEdit(). Expected: %s" % expected)
        self.assertEqual(nameEdited, expected, "Unexpected ability to Edit. Expected: %s" % expected)
        self.assertEqual(objectUsed|sudo_needed, expected, "Unexpected ability to Use. Expected: %s" % expected)

    def assertCanAnnotate(self, blitzObject, expected=True,
            sudo_needed=False, exc_info=False):
        """ Checks the canAnnotate() method AND actual behavior (ability to annotate) """

        annotated = False
        try:
            omero.gateway.CommentAnnotationWrapper.createAndLink(target=blitzObject, ns="gatewaytest.chmod.testCanAnnotate", val="Test Comment")
            annotated = True
        except omero.ReadOnlyGroupSecurityViolation:
            if sudo_needed:
                annotated = True # assume ok
        except omero.SecurityViolation:
            if exc_info:
                traceback.print_exc()
        self.assertEqual(blitzObject.canAnnotate(), expected, "Unexpected result of canAnnotate(). Expected: %s" % expected)
        self.assertEqual(annotated, expected, "Unexpected ability to Annotate. Expected: %s" % expected)


class ChmodGroupTest (ChmodBaseTest):

    def setUp (self):
        """ Create a group with Admin & Owner members"""
        # readonly with an Admin user
        dbhelpers.USERS['chmod_group_admin'] = dbhelpers.UserEntry('r-_chmod_admin','ome', firstname='chmod', lastname='admin',
                   groupname="ReadOnly_chmod_group", groupperms=READONLY, admin=True)
        dbhelpers.USERS['chmod_group_owner'] = dbhelpers.UserEntry('r-_chmod_owner','ome', firstname='chmod', lastname='owner',
                   groupname="ReadOnly_chmod_group", groupperms=READONLY, groupowner=True)
        # Calling the superclass setUp processes the dbhelpers.USERS etc to populate DB
        super(ChmodGroupTest, self).setUp()

    def testChmod(self):
        """ Test change of group permissions """

        # Login as group Admin to get group Id...
        self.doLogin(dbhelpers.USERS['chmod_group_admin'])
        group_Id = self.gateway.getEventContext().groupId
        # do we need to log out of group when changing it's permissions??
        self.tearDown()

        # let another Admin change group permissions
        self.loginAsAdmin()
        self.doChange(group_Id, READWRITE)


class CustomUsersTest (ChmodBaseTest):
    """
    Here we're creating 3 groups with different permissions (read-only, read-annotate, read-write).
    Each group has a user who owns the data (Project), another user, an admin and a group leader (groupowner).
    Then we have a test for each group, testing whether each user canEdit() and canAnnotate() the data.
    """

    def setUp (self):
        # read-only users & data
        def ReadOnly(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_only_%s'%key] = dbhelpers.UserEntry("r-_%s"%key, 'ome',
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
        dbhelpers.PROJECTS['read_only_proj'] = dbhelpers.ProjectEntry('read_only_proj', 'read_only_owner')

        # read-annotate users & data
        def ReadAnn(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_ann_%s'%key] = dbhelpers.UserEntry("ra_%s"%key, 'ome',
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
        dbhelpers.PROJECTS['read_ann_proj'] = dbhelpers.ProjectEntry('read_ann_proj', 'read_ann_owner')

        # read-write users & data
        def ReadWrite(key, admin=False, groupowner=False):
            dbhelpers.USERS['read_write_%s'%key] = dbhelpers.UserEntry("rw_%s"%key, 'ome',
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
        dbhelpers.PROJECTS['read_write_proj'] = dbhelpers.ProjectEntry('read_write_proj', 'read_write_owner')

        # Calling the superclass setUp processes the dbhelpers.USERS and dbhelpers.PROJECTS etc to populate DB
        super(CustomUsersTest, self).setUp()

    def testReadOnly(self):
        """ In a read-only group, user should NOT be able to Edit or Annotate """
        # Login as owner...
        self.doLogin(dbhelpers.USERS['read_only_owner'])
        p = dbhelpers.getProject(self.gateway, 'read_only_proj')
        pid = p.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as user...
        self.doLogin(dbhelpers.USERS['read_only_user'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, False)
        self.assertCanAnnotate(p, False)

        # Login as admin...
        self.doLogin(dbhelpers.USERS['read_only_admin'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, sudo_needed=True)
        self.assertCanAnnotate(p, True, sudo_needed=True)

        # Login as group leader...
        self.doLogin(dbhelpers.USERS['read_only_leader'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, sudo_needed=True)
        self.assertCanAnnotate(p, True, sudo_needed=True)


    def testReadAnnotate(self):
        """ In a read-annotate group, user should be able to Annotate but NOT Edit"""
        # Login as owner...
        self.doLogin(dbhelpers.USERS['read_ann_owner'])
        p = dbhelpers.getProject(self.gateway, 'read_ann_proj')
        pid = p.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as user...
        self.doLogin(dbhelpers.USERS['read_ann_user'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, False)
        self.assertCanAnnotate(p, True, exc_info=1)

        # Login as admin...
        self.doLogin(dbhelpers.USERS['read_ann_admin'])
        self.gateway.CONFIG['SERVICE_OPTS'] = {'omero.group': '-1'}
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        self.doLogin(dbhelpers.USERS['read_ann_leader'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

    def testReadWrite(self):
        """ In a read-write group, all should be able to Annotate and Edit"""
        # Login as owner...
        self.doLogin(dbhelpers.USERS['read_write_owner'])
        p = dbhelpers.getProject(self.gateway, 'read_write_proj')
        pid = p.id
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as user...
        self.doLogin(dbhelpers.USERS['read_write_user'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True, exc_info=1)
        self.assertCanAnnotate(p, True)

        # Login as admin...
        self.doLogin(dbhelpers.USERS['read_write_admin'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        self.doLogin(dbhelpers.USERS['read_write_leader'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

    def waitOnDelete(self, client, handle):
        callback = omero.callbacks.DeleteCallbackI(client, handle)
        errors = None
        count = 10
        while errors is None:
            errors = callback.block(500)
            count -= 1
            self.assert_( count != 0 )
        self.assertEquals(0, errors)

    def testDelete8723(self):
        """ Tests whether regular members can delete each other's data in rwrw-- group """
        # Login as owner...
        self.doLogin(dbhelpers.USERS['read_write_owner'])
        pr = omero.model.ProjectI()
        pr.name = rstring("test-delete")
        pr = self.gateway.getUpdateService().saveAndReturnObject(pr)
        # Login as regular member
        self.doLogin(dbhelpers.USERS['read_write_user'])
        p = self.gateway.getObject("Project", pr.id.val)
        self.assertNotEqual(None, p, "Member can access Project")
        self.assertEqual(p.canDelete(), True, "Member can delete another user's Project")
        handle = self.gateway.deleteObjects("Project", [pr.id.val])
        self.waitOnDelete(self.gateway.c, handle)

        # Must reload project
        p = self.gateway.getObject("Project", pr.id.val)
        self.assertEqual(None, p, "Project should be Deleted")


class ManualCreateEditTest (ChmodBaseTest):
    """ Here we test whether an object created and saved using update service can be edited by another user """

    def setUp (self):
        """ Here we're creating 3 groups, each with 2 users with data belonging to the first user in each"""

        # read-only users & data
        dbhelpers.USERS['read_only_owner'] = dbhelpers.UserEntry('r-_owner','ome', firstname='chmod', lastname='test',
                   groupname="ReadOnly_chmod_test", groupperms=READONLY)
        dbhelpers.USERS['read_only_user'] = dbhelpers.UserEntry('r-_user','ome', firstname='chmod2', lastname='test',
                   groupname="ReadOnly_chmod_test", groupperms=READONLY)

        # Calling the superclass setUp processes the dbhelpers.USERS and dbhelpers.PROJECTS etc to populate DB
        super(ManualCreateEditTest, self).setUp()

    def testReadOnly(self):
        """ In a read-only group, user should NOT be able to Edit or Annotate """
        # Login as owner...
        self.doLogin(dbhelpers.USERS['read_only_owner'])
        p = omero.model.ProjectI()
        p.setName(rstring("test_create_read_only_project"))
        p = self.gateway.getUpdateService().saveAndReturnObject(p)

        # Login as user...
        self.doLogin(dbhelpers.USERS['read_only_user'])
        project = self.gateway.getObject("Project", p.id.val)
        self.assertCanEdit(project, False)
        self.assertCanAnnotate(project, False)


class Test8800 (lib.GTest):
    """ Test for #8800 where ImageWrapper.canEdit() etc return different values after we load Pixels """

    def setUp (self):
        """ This is called at the start of tests """
        super(Test8800, self).setUp()
        self.loginAsAuthor()    # sets self.gateway

    def testWithBlitzWrappers(self):
        """ Uses ImageWrapper.getPrimaryPixels() which loads pixels on the fly """
        image = self.getTestImage()
        before = image.canEdit()
        image.getPrimaryPixels()
        after = image.canEdit()
        self.assertEqual(before, after, "canEdit() affected by ImageWrapper.getPrimaryPixels()")

    def testWithoutWrappers(self):
        """
        Here we can test loading the image again (with Pixels loaded) using different
        values of omero.group.
        Bug #8800 is due to the image returned with 'omero.group':'-1' has canEdit() = False.
        """

        image = self.getTestImage()
        imgObj = image._obj
        gid = image.getDetails().group.id.val
        before = imgObj.getDetails().getPermissions().canEdit()
        ctx = {'omero.group':str(gid)}
        imgObj = self.gateway.getContainerService().getImages("Image", (imgObj.id.val,), None, ctx)[0]
        after = imgObj.getDetails().getPermissions().canEdit()
        self.assertEqual(before, after, "canEdit() affected by loading image with 'omero.group':gid")

        ctx = {'omero.group':'-1'}
        imgObj = self.gateway.getContainerService().getImages("Image", (imgObj.id.val,), None, ctx)[0]
        after = imgObj.getDetails().getPermissions().canEdit()
        self.assertNotEqual(before, after, "canEdit() is False with 'omero.group':'-1'")


class DefaultSetupTest (lib.GTest):

    def setUp (self):
        """ This is called at the start of tests """
        super(DefaultSetupTest, self).setUp()
        self.loginAsAuthor()
        ctx = self.gateway.getEventContext()
        self.image = self.getTestImage()
        self.AUTHOR.check_group_perms(self.gateway, ctx.groupName, "rw----")

    def testAuthorCanEdit(self):
        """
        Tests whether the default Users created by default setUp() canEdit their Images etc.
        """

        image = self.image
        imageId = image.id
        ctx = self.gateway.getAdminService().getEventContext()
        
        group = self.gateway.getGroupFromContext()
        image_gid = image.getDetails().getGroup().id
        image_gname = image.getDetails().getGroup().name
        
        # Author should be able to Edit and Annotate their own data
        self.assertTrue(image.canEdit(), "Author can edit their own image")
        self.assertTrue(image.canAnnotate(), "Author can annotate their own image")
        
        # Login as Admin
        root_client = self.loginAsAdmin()
        user = self.gateway.getUser()
        self.gateway.CONFIG['SERVICE_OPTS'] = {'omero.group': '-1'}
        i = self.gateway.getObject("Image", imageId)
        self.assertTrue(i.canEdit(), "Admin can edit Author's image")
        self.assertTrue(i.canAnnotate(), "Admin can annotate Author's image")
        
        # Login as default "User" - NB: seems this user is not in same group as Author's image.
        self.loginAsUser()
        self.gateway.CONFIG['SERVICE_OPTS'] = {'omero.group': '-1'}
        i = self.gateway.getObject("Image", imageId)
        self.assertEqual(None, i, "User cannot access Author's image in Read-only group")

        # Create new user in the same group
        self.loginAsAdmin()
        chmod_test_user = dbhelpers.UserEntry('chmod_test_user6','foobar', firstname='User', lastname='Chmod')  #groupname = image_gname
        chmod_test_user.create(self.gateway, dbhelpers.ROOT.passwd)
        admin = self.gateway.getAdminService()
        user = admin.lookupExperimenter('chmod_test_user6')
        group = admin.getGroup(image_gid)
        admin.addGroups(user, [group])

        self.doLogin(chmod_test_user)
        user = self.gateway.getUser()
        self.assertTrue(self.gateway.setGroupForSession(image_gid))      # switch into group
        self.assertEqual(image_gid, self.gateway.getEventContext().groupId, "Confirm in same group as image")
        i = self.gateway.getObject("Image", imageId)
        self.assertEqual(None, i, \
                "User cannot access Author's image in Read-only group: %s" % i)

if __name__ == '__main__':
    unittest.main()
