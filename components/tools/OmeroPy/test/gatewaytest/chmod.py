#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
#                      All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero
import unittest
from omero.rtypes import *
from omero.cmd import *
from omero.callbacks import CmdCallbackI
from omero.gateway import BlitzGateway
from omero.gateway.scripts import dbhelpers

PRIVATE = 'rw----'
READONLY = 'rwr---'
READANN = 'rwra--'
READWRITE = 'rwrw--'

import gatewaytest.library as lib


class ChmodBaseTest (lib.GTest):

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

    def assertCanEdit(self, blitzObject, expected=True):
        """ Checks the canEdit() method AND actual behavior (ability to edit) """

        nameEdited = False
        try:
            blitzObject.setName("new name")
            blitzObject.save()
            nameEdited = True
        except:
            pass
        self.assertEqual(nameEdited, expected, "Unexpected ability to Edit. Expected: %s" % expected)
        self.assertEqual(blitzObject.canEdit(), expected, "Unexpected result of canEdit(). Expected: %s" % expected)

    def assertCanAnnotate(self, blitzObject, expected=True):
        """ Checks the canAnnotate() method AND actual behavior (ability to annotate) """

        annotated = False
        try:
            omero.gateway.CommentAnnotationWrapper.createAndLink(target=blitzObject, ns="gatewaytest.chmod.testCanAnnotate", val="Test Comment")
            annotated = True
        except:
            pass
        self.assertEqual(annotated, expected, "Unexpected ability to Annotate. Expected: %s" % expected)
        self.assertEqual(blitzObject.canAnnotate(), expected, "Unexpected result of canAnnotate(). Expected: %s" % expected)


class ChmodGroupTest (ChmodBaseTest):

    def setUp (self):
        """ Create a group with Admin & Owner members"""
        # readonly with an Admin user
        dbhelpers.USERS['chmod_group_admin'] = dbhelpers.UserEntry('r-_chmod_admin','ome', firstname='chmod', lastname='admin',
                   groupname="ReadOnly_chmod_group", groupperms=READONLY, admin=True)
        dbhelpers.USERS['chmod_group_owner'] = dbhelpers.UserEntry('r-_chmod_owner','ome', firstname='chmod', lastname='owner',
                   groupname="ReadOnly_chmod_group", groupowner=True)
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
        dbhelpers.USERS['read_only_owner'] = dbhelpers.UserEntry('r-_owner','ome', firstname='chmod', lastname='test', 
                   groupname="ReadOnly_chmod_test", groupperms=READONLY)
        dbhelpers.USERS['read_only_user'] = dbhelpers.UserEntry('r-_user','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadOnly_chmod_test")
        dbhelpers.USERS['read_only_admin'] = dbhelpers.UserEntry('r-_admin','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadOnly_chmod_test", admin=True)
        dbhelpers.USERS['read_only_leader'] = dbhelpers.UserEntry('r-_leader','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadOnly_chmod_test", groupowner=True)
        dbhelpers.PROJECTS['read_only_proj'] = dbhelpers.ProjectEntry('read_only_proj', 'read_only_owner')

        # read-annotate users & data
        dbhelpers.USERS['read_ann_owner'] = dbhelpers.UserEntry('ra_owner','ome', firstname='chmod', lastname='test', 
                   groupname="ReadAnn_chmod_test", groupperms=READANN)
        dbhelpers.USERS['read_ann_user'] = dbhelpers.UserEntry('ra_user','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadAnn_chmod_test")
        dbhelpers.USERS['read_ann_admin'] = dbhelpers.UserEntry('ra_admin','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadAnn_chmod_test", admin=True)
        dbhelpers.USERS['read_ann_leader'] = dbhelpers.UserEntry('ra_leader','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadAnn_chmod_test", groupowner=True)
        dbhelpers.PROJECTS['read_ann_proj'] = dbhelpers.ProjectEntry('read_ann_proj', 'read_ann_owner')
        
        # read-write users & data
        dbhelpers.USERS['read_write_owner'] = dbhelpers.UserEntry('rw_owner','ome', firstname='chmod', lastname='test', 
                   groupname="ReadWrite_chmod_test", groupperms=READWRITE)
        dbhelpers.USERS['read_write_user'] = dbhelpers.UserEntry('rw_user','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadWrite_chmod_test")
        dbhelpers.USERS['read_write_admin'] = dbhelpers.UserEntry('rw_admin','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadWrite_chmod_test", admin=True)
        dbhelpers.USERS['read_write_leader'] = dbhelpers.UserEntry('rw_leader','ome', firstname='chmod2', lastname='test', 
                   groupname="ReadWrite_chmod_test", groupowner=True)
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
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, True)

        # Login as group leader...
        self.doLogin(dbhelpers.USERS['read_only_leader'])
        p = self.gateway.getObject("Project", pid)
        self.assertCanEdit(p, True)
        self.assertCanAnnotate(p, False)


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
        self.assertCanAnnotate(p, True)

        # Login as admin...
        self.doLogin(dbhelpers.USERS['read_ann_admin'])
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
        self.assertCanEdit(p, True)
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


class ManualCreateEditTest (ChmodBaseTest):
    """ Here we test whether an object created and saved using update service can be edited by another user """

    def setUp (self):
        """ Here we're creating 3 groups, each with 2 users with data belonging to the first user in each"""

        # read-only users & data
        dbhelpers.USERS['read_only_owner'] = dbhelpers.UserEntry('r-_owner','ome', firstname='chmod', lastname='test',
                   groupname="ReadOnly_chmod_test", groupperms=READONLY)
        dbhelpers.USERS['read_only_user'] = dbhelpers.UserEntry('r-_user','ome', firstname='chmod2', lastname='test',
                   groupname="ReadOnly_chmod_test")

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


class DefaultSetupTest (lib.GTest):

    def setUp (self):
        """ This is called at the start of tests """
        super(DefaultSetupTest, self).setUp()
        self.loginAsAuthor()
        self.image = self.getTestImage()


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

        # Create new user 
        chmod_test_user = dbhelpers.UserEntry('chmod_test_user6','foobar', firstname='User', lastname='Chmod')  #groupname = image_gname
        chmod_test_user.create(self.gateway, dbhelpers.ROOT.passwd)
        self.doLogin(chmod_test_user)
        user = self.gateway.getUser()
        self.gateway.setGroupForSession(image_gid)      # switch into group 
        self.assertEqual(image_gid, self.gateway.getEventContext().groupId, "Confirm in same group as image")
        i = self.gateway.getObject("Image", imageId)
        self.assertEqual(None, i, "User cannot access Author's image in Read-only group")

if __name__ == '__main__':
    unittest.main()
