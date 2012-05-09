#!/usr/bin/env python

"""
   Simple unit test which stipulates what the default permissions
   values should be.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, omero.model, omero_model_PermissionsI

class TestPermissions(unittest.TestCase):

    def setUp(self):
        self.p = omero.model.PermissionsI()

    def testperm1(self):
        # The default
        self.assert_( self.p.isUserRead() )
        self.assert_( self.p.isUserAnnotate() )
        self.assert_( self.p.isUserWrite() )
        self.assert_( self.p.isGroupRead() )
        self.assert_( self.p.isGroupAnnotate() )
        self.assert_( self.p.isGroupWrite() )
        self.assert_( self.p.isWorldRead() )
        self.assert_( self.p.isWorldAnnotate() )
        self.assert_( self.p.isWorldWrite() )

        # All off
        self.p._perm1 = 0L
        self.assert_( not  self.p.isUserRead() )
        self.assert_( not  self.p.isUserAnnotate() )
        self.assert_( not  self.p.isUserWrite() )
        self.assert_( not  self.p.isGroupRead() )
        self.assert_( not  self.p.isGroupAnnotate() )
        self.assert_( not  self.p.isGroupWrite() )
        self.assert_( not  self.p.isWorldRead() )
        self.assert_( not  self.p.isWorldAnnotate() )
        self.assert_( not  self.p.isWorldWrite() )

        # All on
        self.p._perm1 = -1L
        self.assert_( self.p.isUserRead() )
        self.assert_( self.p.isUserAnnotate() )
        self.assert_( self.p.isUserWrite() )
        self.assert_( self.p.isGroupRead() )
        self.assert_( self.p.isGroupAnnotate() )
        self.assert_( self.p.isGroupWrite() )
        self.assert_( self.p.isWorldRead() )
        self.assert_( self.p.isWorldAnnotate() )
        self.assert_( self.p.isWorldWrite() )

        # Various swaps
        self.p.setUserRead(False)
        self.assert_( not self.p.isUserRead() )
        self.p.setGroupWrite(True)
        self.assert_( self.p.isGroupWrite() )

        # Now reverse each of the above
        self.p.setUserRead(True)
        self.assert_( self.p.isUserRead() )
        self.p.setGroupWrite(False)
        self.assert_( not self.p.isGroupWrite() )

    def test8564(self):

        p = omero.model.PermissionsI("rwrwrw")
        self.assertRW(p, "User", "Group", "World")
        self.assertEquals("rwrwrw", str(p))

        p = omero.model.PermissionsI("rarara")
        self.assertRA(p, "User", "Group", "World")
        self.assertEquals("rarara", str(p))

        p = omero.model.PermissionsI("rwrar-")
        self.assertRW(p, "User")
        self.assertRA(p, "Group")
        self.assertRO(p, "World")
        self.assertEquals("rwrar-", str(p))

    # Helpers

    def assertRO(self, p, *roles):
        for role in roles:
            self.assertRAE(p, role, True, False, False)

    def assertRA(self, p, *roles):
        for role in roles:
            self.assertRAE(p, role, True, True, False)

    def assertRW(self, p, *roles):
        for role in roles:
            self.assertRAE(p, role, True, True, True)

    def assertRAE(self, p, role, read, annotate, edit):
        isRead = getattr(p, "is%sRead" % role)()
        isAnno = getattr(p, "is%sAnnotate" % role)()
        isEdit = getattr(p, "is%sWrite" % role)()

        msg = """Permissions: %s Role: %s
          Expected READ: %s \t Found: %s
          Expected ANNO: %s \t Found: %s
          Expected EDIT: %s \t Found: %s""" % \
                  (p, role, read, isRead, annotate, isAnno, edit, isEdit)

        self.assertEquals(read, isRead, msg)
        self.assertEquals(annotate, isAnno, msg)
        self.assertEquals(edit, isEdit, msg)

if __name__ == '__main__':
    unittest.main()
