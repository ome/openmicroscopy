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

    def testPermissionsSetters(self):
        # start with everythin false
        p = omero.model.PermissionsI('------')

        ## read flags are easy, straight binary
        ## user flags
        p.setUserRead(True)
        self.assert_( p.isUserRead() )
        self.assertEquals('r', str(p)[0])
        p.setUserRead(False)
        self.assert_( not p.isUserRead() )
        self.assertEquals('-', str(p)[0])

        ## group flags
        p.setGroupRead(True)
        self.assert_( p.isGroupRead() )
        self.assertEquals('r', str(p)[2])
        p.setGroupRead(False)
        self.assert_( not p.isGroupRead() )
        self.assertEquals('-', str(p)[2])

        ## world flags
        p.setWorldRead(True)
        self.assert_( p.isWorldRead() )
        self.assertEquals('r', str(p)[4])
        p.setWorldRead(False)
        self.assert_( not p.isWorldRead() )
        self.assertEquals('-', str(p)[4])

        ## write flags are trickier as the string
        ## representation is ternary
        ## user flags
        p.setUserAnnotate(True)
        self.assert_( p.isUserAnnotate() )
        self.assert_( not p.isUserWrite() )
        self.assertEquals('a', str(p)[1])
        p.setUserWrite(True)
        self.assert_( p.isUserAnnotate() )
        self.assert_( p.isUserWrite() )
        self.assertEquals('w', str(p)[1])
        p.setUserWrite(False)
        self.assert_( p.isUserAnnotate() )
        self.assert_( not p.isUserWrite() )
        self.assertEquals('a', str(p)[1])
        p.setUserAnnotate(False)
        self.assert_( not p.isUserAnnotate() )
        self.assert_( not p.isUserWrite() )
        self.assertEquals('-', str(p)[1])

        ## group flags
        p.setGroupAnnotate(True)
        self.assert_( p.isGroupAnnotate() )
        self.assert_( not p.isGroupWrite() )
        self.assertEquals('a', str(p)[3])
        p.setGroupWrite(True)
        self.assert_( p.isGroupAnnotate() )
        self.assert_( p.isGroupWrite() )
        self.assertEquals('w', str(p)[3])
        p.setGroupWrite(False)
        self.assert_( p.isGroupAnnotate() )
        self.assert_( not p.isGroupWrite() )
        self.assertEquals('a', str(p)[3])
        p.setGroupAnnotate(False)
        self.assert_( not p.isGroupAnnotate() )
        self.assert_( not p.isGroupWrite() )
        self.assertEquals('-', str(p)[3])

        ## world flags
        p.setWorldAnnotate(True)
        self.assert_( p.isWorldAnnotate() )
        self.assert_( not p.isWorldWrite() )
        self.assertEquals('a', str(p)[5])
        p.setWorldWrite(True)
        self.assert_( p.isWorldAnnotate() )
        self.assert_( p.isWorldWrite() )
        self.assertEquals('w', str(p)[5])
        p.setWorldWrite(False)
        self.assert_( p.isWorldAnnotate() )
        self.assert_( not p.isWorldWrite() )
        self.assertEquals('a', str(p)[5])
        p.setWorldAnnotate(False)
        self.assert_( not p.isWorldAnnotate() )
        self.assert_( not p.isWorldWrite() )
        self.assertEquals('-', str(p)[5])

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
