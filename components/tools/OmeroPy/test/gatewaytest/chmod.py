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
COLLAB = 'rwrw--'

import gatewaytest.library as lib


class CustomUsersTest (lib.GTest):

    def setUp (self):
        """ Here we're creating 2 additional users with data belonging to the first user """

        dbhelpers.USERS['chmod1'] = dbhelpers.UserEntry('chmod_user','foobar', firstname='chmod', lastname='test', 
                   groupname="ReadOnly_chmod_test", groupperms="rwr---")
        dbhelpers.USERS['chmod2'] = dbhelpers.UserEntry('chmod_user2','foobar', firstname='chmod2', lastname='test', 
                   groupname="ReadOnly_chmod_test")
        dbhelpers.PROJECTS['chmod_test'] = dbhelpers.ProjectEntry('chmod_test', 'chmod1')
        super(CustomUsersTest, self).setUp()
        self.doLogin(dbhelpers.USERS['chmod1'])

    def testUsers(self):

        p = dbhelpers.getProject(self.gateway, 'chmod_test')
        pid = p.id
        self.assertTrue(p.canEdit(), "User can Edit their own data")
        self.assertTrue(p.canAnnotate(), "User can Annotate their own data")
        # Editing shouldn't throw exception
        p.setName("test name")
        p.save()

        # Login as user 2...
        self.doLogin(dbhelpers.USERS['chmod2'])
        p = self.gateway.getObject("Project", pid)
        nameEdited = False
        try:
            p.setName("new name")
            p.save()
            nameEdited = True
        except:
            pass
        self.assertFalse(nameEdited, "Editing should throw exception above")
        self.assertFalse(p.canEdit(), "User can NOT edit another's data in read-only group")
        self.assertFalse(p.canAnnotate(), "User can NOT annotate another's data in read-only group")


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
