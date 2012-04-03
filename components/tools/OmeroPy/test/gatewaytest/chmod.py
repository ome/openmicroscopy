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


class CustomUsersTest (lib.GTest):

    def assertCanEdit(self, blitzObject, expected=True):
        """ Checks the canEdit() method AND actual behavior (ability to edit) """

        #self.assertEqual(blitzObject.canEdit(), expected, "Unexpected result of canEdit(). Expected: %s" % expected)
        nameEdited = False
        try:
            blitzObject.setName("new name")
            blitzObject.save()
            nameEdited = True
        except:
            pass
        self.assertEqual(nameEdited, expected, "Unexpected ability to Edit. Expected: %s" % expected)

    def assertCanAnnotate(self, blitzObject, expected=True):
        """ Checks the canAnnotate() method AND actual behavior (ability to annotate) """

        self.assertEqual(blitzObject.canAnnotate(), expected, "Unexpected result of canAnnotate(). Expected: %s" % expected)
        annotated = False
        try:
            omero.gateway.CommentAnnotationWrapper.createAndLink(target=blitzObject, ns="gatewaytest.chmod.testCanAnnotate", val="Test Comment")
            annotated = True
        except:
            pass
        self.assertEqual(annotated, expected, "Unexpected ability to Annotate. Expected: %s" % expected)

    def setUp (self):
        """ Here we're creating 3 groups, each with 2 users with data belonging to the first user in each"""

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
        """ In a read-write group, user should be able to Annotate and Edit"""
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
