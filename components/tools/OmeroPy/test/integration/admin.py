#!/usr/bin/env python

"""
   Tests of the admin service

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import omero
from omero_model_PixelsI import PixelsI
from omero_model_ImageI import ImageI
from omero_model_DatasetI import DatasetI
from omero_model_ExperimenterI import ExperimenterI
from omero_model_ExperimenterGroupI import ExperimenterGroupI
from omero_model_GroupExperimenterMapI import GroupExperimenterMapI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero.rtypes import *

class TestAdmin(lib.ITest):

    def testGetGroup(self):
        a = self.client.getSession().getAdminService()
        l = a.lookupGroups()
        g = a.getGroup(l[0].getId().val)
        self.assert_( 0 != g.sizeOfGroupExperimenterMap() )

    def testSetGroup(self):
        a = self.client.getSession().getAdminService()
        ec = a.getEventContext()
        uid = ec.userId

        # Add user to new group to test setting default
        e = a.getExperimenter(uid)
        admin = self.root.sf.getAdminService()
        grp = self.new_group()
        admin.addGroups(e, [grp])

        a.setDefaultGroup(e, grp)

    def testThumbnail(self):
        q = self.client.getSession().getQueryService()

        # Filter to only get one possible pixels
        f = omero.sys.Filter()
        f.offset = rint(0)
        f.limit  = rint(1)
        p = omero.sys.Parameters()
        p.theFilter = f

        pixel = q.findByQuery("select p from Pixels p join fetch p.thumbnails t", p)
        tstore = self.client.getSession().createThumbnailStore()
        if not tstore.setPixelsId(pixel.id.val):
            tstore.resetDefaults()
            tstore.setPixelsId(pixel.id.val)
        tstore.getThumbnail(rint(16), rint(16))

    def testChangePassword(self):
        """
        See ticket:3201
        """

        client = self.new_client()

        admin = client.sf.getAdminService()
        admin.changePassword(rstring("ome"))

        uuid = client.getSessionId()

        # Now login without a passowrd
        client2 = client.createClient(True)
        try:
            admin = client2.sf.getAdminService()

            self.assertRaises(omero.SecurityViolation, admin.changePassword, rstring("foo"))
            admin.changePasswordWithOldPassword(rstring("ome"), rstring("foo"))
        finally:
            client2.closeSession()

        # Now try to change password without a secure session
        if False: # Waiting on ticket:3232
            client3 = client.createClient(False)
            try:
                admin = client3.sf.getAdminService()
                self.assertRaises(omero.SecurityViolation, admin.changePasswordWithOldPassword, rstring("foo"), rstring("ome"))
            finally:
                client3.closeSession()

    def testChangePasswordWhenUnset(self):
        """
        Shows that it's possible to use the
        changePasswordWithOldPassword when
        previously no password was set.

        See ticket:3201
        """
        client = self.new_client()
        admin = client.sf.getAdminService()

        # By setting the user's password to the empty string
        # any password will be allowed as the old password
        admin.changePassword(rstring(""))
        admin.changePasswordWithOldPassword(rstring("IGNORED"), rstring("ome"))
        self.assertRaises(omero.SecurityViolation, admin.changePasswordWithOldPassword, rstring("BADPW"), rstring("foo"))
        admin.changePasswordWithOldPassword(rstring("ome"), rstring("foo"))

        # None disables user. No further password checks will pass.
        # Only the current session or an admin will be able to
        # reset the password
        admin.changePassword(None)
        self.assertRaises(omero.SecurityViolation, admin.changePasswordWithOldPassword, rstring(""), rstring("foo"))
        self.assertRaises(omero.SecurityViolation, admin.changePasswordWithOldPassword, rstring("ome"), rstring("foo"))
        self.assertRaises(omero.ApiUsageException, admin.changePasswordWithOldPassword, None, rstring("foo"))
        joined_client = client.createClient(True)
        try:
            self.assertRaises(omero.SecurityViolation, joined_client.sf.getAdminService().changePasswordWithOldPassword, rstring(""), rstring("ome"))
        finally:
            joined_client.__del__()
        admin.changePassword(rstring("ome")) # could be an admin

    def testGetEventContext4011(self):
        """
        Tests the "freshness" of the iAdmin.getEventContext() call.
        """
        client = self.new_client()
        group = self.new_group()
        admin = client.sf.getAdminService()
        root_admin = self.root.sf.getAdminService()

        ec1 = admin.getEventContext()
        exp = omero.model.ExperimenterI(ec1.userId, False)
        grps1 = root_admin.getMemberOfGroupIds(exp)

        # Now add the user to a group and see if the
        # event context is updated.
        root_admin.addGroups(exp, [group])
        ec2 = admin.getEventContext()
        grps2 = root_admin.getMemberOfGroupIds(exp)

        # Check via the groups
        self.assertEquals(len(grps1)+1, len(grps2))
        self.assertTrue(group.id.val in grps2)

        # Check again via the contexts
        self.assertEquals(len(ec1.memberOfGroups)+1, len(ec2.memberOfGroups))
        self.assertTrue(group.id.val in ec2.memberOfGroups)

    def testUserRoles4056(self):
        """
        Tests for optimistic lock exception when modifying roles.
        """
        client = self.new_client()
        admin = client.sf.getAdminService()
        ec = admin.getEventContext()
        roles = admin.getSecurityRoles()

        exp = omero.model.ExperimenterI(ec.userId, False)
        grp = omero.model.ExperimenterGroupI(roles.userGroupId, False)

        root_admin = self.root.sf.getAdminService()
        root_admin.removeGroups(exp, [grp])
        root_admin.addGroups(exp, [grp])
        root_admin.removeGroups(exp, [grp])
        root_admin.addGroups(exp, [grp])


if __name__ == '__main__':
    unittest.main()
