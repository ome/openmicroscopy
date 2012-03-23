#!/usr/bin/env python

"""
   Tests of the changing permissions on groups

   Copyright 2012 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import time
import unittest
import integration.library as lib
import omero
from omero.rtypes import *


class BaseChmodTest(lib.ITest):
    """
    """

    def init(self, from_perms, to_perms):
        self.group = self.new_group(perms=from_perms)
        self.client, self.user = \
                self.new_client_and_user(group=self.group, admin=True)
        self.from_perms = from_perms
        self.to_perms = to_perms

    def addData(self):
        c = omero.model.CommentAnnotationI()
        up = self.client.sf.getUpdateService()
        self.comment = up.saveAndReturnObject(c)

    def chmod(self):
        self.start = time.time()
        try:
            admin = self.client.sf.getAdminService()
            perms = omero.model.PermissionsI(self.to_perms)
            admin.changePermissions(self.group, perms)
        finally:
            self.stop = time.time()
            self.elapsed = (self.stop - self.start)

    def assertChmod(self):
        query = self.client.sf.getQueryService()
        old_comment = self.comment
        new_comment = query.get("CommentAnnotation", old_comment.id.val)
        old_obj_perms = old_comment.details.permissions
        new_obj_perms = new_comment.details.permissions

        self.assertEquals(self.from_perms, str(old_obj_perms))
        self.assertEquals(self.to_perms, str(new_obj_perms))


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
        self.chmod()
        self.assertChmod()

    def test_chmod_rw_rwre(self):
        self.init("rw----", "rwr---")
        self.addData()
        self.chmod()
        self.assertChmod()


class TestChmodHard(BaseChmodTest):
    """
    Tests all the transitions which require runtime checks.
    These mostly center around *removing* read permissions
    since there it must be shown that this won't lead to
    confusing SecurityViolations.
    """

    pass


if __name__ == '__main__':
    unittest.main()
