#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012-2015 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
   Tests of the changing permissions on groups

"""

import time
import library as lib
import omero


class BaseChmodTest(lib.ITest):

    """
    """

    def init(self, from_perms, to_perms):
        self.group = self.new_group(perms=from_perms)
        self.owner = self.new_client(group=self.group, owner=True)
        self.member = self.new_client(group=self.group, owner=False)
        self.from_perms = from_perms
        self.to_perms = to_perms

    def refresh(self, client):
        client.sf.getAdminService().getEventContext()  # Refresh

    def assertEqPerms(self, a, b):
        assert a.__class__ in (omero.model.PermissionsI, str)
        assert b.__class__ in (omero.model.PermissionsI, str)
        assert str(a) == str(b)

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
            old_ctx = admin.getEventContext()
            old_grp = admin.getGroup(self.group.id.val)
            self.change_permissions(self.group.id.val, self.to_perms, client)
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
        assert canAnnotate == perms.canAnnotate()
        assert canEdit == perms.canEdit()
        assert details.getCallContext() is not None
        assert details.getEventContext() is not None


class TestChmodEasy(BaseChmodTest):

    """
    Tests all the transitions which are known to be trivial.
    These mostly center around *adding* read permissions
    since there is nothing new to check.
    """

    def assertChmod(self):
        assert self.elapsed < 0.5
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
