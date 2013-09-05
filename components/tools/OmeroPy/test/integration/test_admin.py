#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests of the admin service

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
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

        dg = self.client.getSession().getAdminService().getDefaultGroup(uid)
        self.assertEqual(dg.id.val, grp.id.val)
    
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

    def testSetSecurityPassword(self):
        """
        Several methods require the user to have authenticated with a password.
        In 4.3, a method was added to the ServiceFactoryPrx to allow late
        password-based authentication.

        See #3202
        See @RolesAllow("HasPassword")
        """
        experimenter = self.new_user() # To have password changed

        password = self.root.getProperty("omero.rootpass")
        new_client = self.root.createClient(True) # Secure, but not password-auth'd

        admin = new_client.sf.getAdminService()
        new_password = omero.rtypes.rstring("FOO")

        # Initially, the test should fail.
        try:
            admin.changeUserPassword(experimenter.omeName.val, new_password)
            self.fail("Should not pass!")
        except omero.SecurityViolation, sv:
            pass # Good!

        # Now set the password
        new_client.sf.setSecurityPassword(password)

        # And then it should succeed
        admin.changeUserPassword(experimenter.omeName.val, new_password)

    def new_client_FAILS(self, user):
        import Glacier2
        try:
            self.new_client(user=user)
            self.fail("Where's the CCSE?")
        except Glacier2.CannotCreateSessionException, ccse:
            pass

    def new_client_RESTRICTED(self, user):
        c = self.new_client(user=user)
        try:
            c.sf.getQueryService().find("Image", -1) # Should be disallowed
            self.fail("Where's the security violation?")
        except omero.SecurityViolation, sv:
            pass

    def test9193(self):
        # Test the removal of removing users
        # from a group when the group in question
        # may be their last (i.e. default) group

        g = self.new_group()
        u = self.new_user(group=g)

        # Test removing the default group
        self.remove_experimenters(g, [u])
        self.new_client_FAILS(user=u)

        self.add_experimenters(g, [u])
        c = self.new_client(user=u)

        # Now we'll try removing and re-adding user
        UG = c.sf.getAdminService().lookupGroup("user")
        self.remove_experimenters(UG, [u])
        self.new_client_RESTRICTED(user=u)

        self.add_experimenters(UG, [u])
        c = self.new_client(user=u)

        # Now we'll try with both
        admin = self.root.sf.getAdminService()
        admin.removeGroups(u, [g, UG])
        self.new_client_FAILS(user=u)

        admin.addGroups(u, [g, UG])
        c = self.new_client(user=u)

        # And now in the other order
        admin.removeGroups(u, [UG, g])
        self.new_client_FAILS(user=u)

        admin.addGroups(u, [UG, g])
        c = self.new_client(user=u)


if __name__ == '__main__':
    unittest.main()
