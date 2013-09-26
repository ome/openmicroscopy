#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests of the changing permissions on groups

   Copyright 2012 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import time
import unittest
import test.integration.library as lib
import omero
from omero.rtypes import *


class BaseChmodTest(lib.ITest):
    """
    """

    def init(self, from_perms, to_perms):
        self.group = self.new_group(perms=from_perms)
        self.owner = self.new_client(group=self.group, admin=True)
        self.member = self.new_client(group=self.group, admin=False)
        self.from_perms = from_perms
        self.to_perms = to_perms

    def refresh(self, client):
        client.sf.getAdminService().getEventContext()  # Refresh

    def assertEqPerms(self, a, b):
        self.assertTrue(a.__class__ in
                (omero.model.PermissionsI, str))
        self.assertTrue(b.__class__ in
                (omero.model.PermissionsI, str))
        a = str(a)
        b = str(b)
        self.assertEquals(a, b)

    def addData(self):
        c = omero.model.CommentAnnotationI()
        up = self.owner.sf.getUpdateService()
        self.comment = up.saveAndReturnObject(c)

    def load(self, client):
        query = client.sf.getQueryService()
        return query.get("CommentAnnotation", self.comment.id.val)

    def chmod(self, client):
        self.start = time.time()
        try:
            admin = client.sf.getAdminService()
            perms = omero.model.PermissionsI(self.to_perms)
            old_ctx = admin.getEventContext()
            old_grp = admin.getGroup(self.group.id.val)
            admin.changePermissions(self.group, perms)
            new_ctx = admin.getEventContext()  # Refresh
            new_grp = admin.getGroup(self.group.id.val)
        finally:
            self.stop = time.time()
            self.elapsed = (self.stop - self.start)

        # Check old
        old_perms = old_grp.details.permissions
        self.assertEqPerms(old_ctx.groupPermissions, self.from_perms)
        self.assertEqPerms(old_ctx.groupPermissions, old_perms)

        # Check new
        new_perms = new_grp.details.permissions
        self.assertEqPerms(new_ctx.groupPermissions, self.to_perms)
        self.assertEqPerms(new_ctx.groupPermissions, new_perms)

    def assertChmod(self):
        old_comment = self.comment
        new_comment = self.load(self.owner)
        old_obj_perms = old_comment.details.permissions
        new_obj_perms = new_comment.details.permissions

        self.assertEqPerms(self.from_perms, old_obj_perms)
        self.assertEqPerms(self.to_perms, new_obj_perms)

    def assertState(self, client, canAnnotate, canEdit):
        obj = self.load(client)
        details = obj.details
        perms = details.permissions

        # Check the new perms state
        self.assertEquals(canAnnotate, perms.canAnnotate())
        self.assertEquals(canEdit, perms.canEdit())
        self.assertTrue(details.getCallContext() is not None)
        self.assertTrue(details.getEventContext() is not None)


class TestChmodEasy(BaseChmodTest):
    """
    Tests all the transitions which are known to be trivial.
    These mostly center around *adding* read permissions
    since there is nothing new to check.
    """

    def assertChmod(self):
        self.assert_(self.elapsed < 0.5)
        BaseChmodTest.assertChmod(self)

    def test_chmod_rw_rwr(self):
        self.init("rw----", "rwr---")
        self.addData()
        self.chmod(self.owner)
        self.assertChmod()
        self.assertState(self.owner, True, True)

        self.refresh(self.member)
        self.assertState(self.member, False, False)


class TestChmodHard(BaseChmodTest):
    """
    Tests all the transitions which require runtime checks.
    These mostly center around *removing* read permissions
    since there it must be shown that this won't lead to
    confusing SecurityViolations.
    """

    pass
    # What to do about non-group chmod


if __name__ == '__main__':
    unittest.main()
