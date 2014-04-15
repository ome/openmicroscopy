#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple unit test which stipulates what the default permissions
   values should be.

   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import omero.model


class TestPermissions(object):

    def setup_method(self, method):
        self.p = omero.model.PermissionsI()

    def testperm1(self):
        # The default
        assert self.p.isUserRead()
        assert self.p.isUserAnnotate()
        assert self.p.isUserWrite()
        assert self.p.isGroupRead()
        assert self.p.isGroupAnnotate()
        assert self.p.isGroupWrite()
        assert self.p.isWorldRead()
        assert self.p.isWorldAnnotate()
        assert self.p.isWorldWrite()

        # All off
        self.p._perm1 = 0L
        assert not self.p.isUserRead()
        assert not self.p.isUserAnnotate()
        assert not self.p.isUserWrite()
        assert not self.p.isGroupRead()
        assert not self.p.isGroupAnnotate()
        assert not self.p.isGroupWrite()
        assert not self.p.isWorldRead()
        assert not self.p.isWorldAnnotate()
        assert not self.p.isWorldWrite()

        # All on
        self.p._perm1 = -1L
        assert self.p.isUserRead()
        assert self.p.isUserAnnotate()
        assert self.p.isUserWrite()
        assert self.p.isGroupRead()
        assert self.p.isGroupAnnotate()
        assert self.p.isGroupWrite()
        assert self.p.isWorldRead()
        assert self.p.isWorldAnnotate()
        assert self.p.isWorldWrite()

        # Various swaps
        self.p.setUserRead(False)
        assert not self.p.isUserRead()
        self.p.setGroupWrite(True)
        assert self.p.isGroupWrite()

        # Now reverse each of the above
        self.p.setUserRead(True)
        assert self.p.isUserRead()
        self.p.setGroupWrite(False)
        assert not self.p.isGroupWrite()

    def testPermissionsSetters(self):
        # start with everythin false
        p = omero.model.PermissionsI('------')

        # read flags are easy, straight binary
        # user flags
        p.setUserRead(True)
        assert p.isUserRead()
        assert 'r' == str(p)[0]
        p.setUserRead(False)
        assert not p.isUserRead()
        assert '-' == str(p)[0]

        # group flags
        p.setGroupRead(True)
        assert p.isGroupRead()
        assert 'r' == str(p)[2]
        p.setGroupRead(False)
        assert not p.isGroupRead()
        assert '-' == str(p)[2]

        # world flags
        p.setWorldRead(True)
        assert p.isWorldRead()
        assert 'r' == str(p)[4]
        p.setWorldRead(False)
        assert not p.isWorldRead()
        assert '-' == str(p)[4]

        # write flags are trickier as the string
        # representation is ternary
        # user flags
        p.setUserAnnotate(True)
        assert p.isUserAnnotate()
        assert not p.isUserWrite()
        assert 'a' == str(p)[1]
        p.setUserWrite(True)
        assert p.isUserAnnotate()
        assert p.isUserWrite()
        assert 'w' == str(p)[1]
        p.setUserWrite(False)
        assert p.isUserAnnotate()
        assert not p.isUserWrite()
        assert 'a' == str(p)[1]
        p.setUserAnnotate(False)
        assert not p.isUserAnnotate()
        assert not p.isUserWrite()
        assert '-' == str(p)[1]

        # group flags
        p.setGroupAnnotate(True)
        assert p.isGroupAnnotate()
        assert not p.isGroupWrite()
        assert 'a' == str(p)[3]
        p.setGroupWrite(True)
        assert p.isGroupAnnotate()
        assert p.isGroupWrite()
        assert 'w' == str(p)[3]
        p.setGroupWrite(False)
        assert p.isGroupAnnotate()
        assert not p.isGroupWrite()
        assert 'a' == str(p)[3]
        p.setGroupAnnotate(False)
        assert not p.isGroupAnnotate()
        assert not p.isGroupWrite()
        assert '-' == str(p)[3]

        # world flags
        p.setWorldAnnotate(True)
        assert p.isWorldAnnotate()
        assert not p.isWorldWrite()
        assert 'a' == str(p)[5]
        p.setWorldWrite(True)
        assert p.isWorldAnnotate()
        assert p.isWorldWrite()
        assert 'w' == str(p)[5]
        p.setWorldWrite(False)
        assert p.isWorldAnnotate()
        assert not p.isWorldWrite()
        assert 'a' == str(p)[5]
        p.setWorldAnnotate(False)
        assert not p.isWorldAnnotate()
        assert not p.isWorldWrite()
        assert '-' == str(p)[5]

    def test8564(self):

        p = omero.model.PermissionsI("rwrwrw")
        self.assertRW(p, "User", "Group", "World")
        assert "rwrwrw" == str(p)

        p = omero.model.PermissionsI("rarara")
        self.assertRA(p, "User", "Group", "World")
        assert "rarara" == str(p)

        p = omero.model.PermissionsI("rwrar-")
        self.assertRW(p, "User")
        self.assertRA(p, "Group")
        self.assertRO(p, "World")
        assert "rwrar-" == str(p)

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

        msg = """
Permissions: %s Role: %s
Expected READ: %s \t Found: %s
Expected ANNO: %s \t Found: %s
Expected EDIT: %s \t Found: %s""" % \
            (p, role, read, isRead, annotate, isAnno, edit, isEdit)

        assert read == isRead, msg
        assert annotate == isAnno, msg
        assert edit == isEdit, msg
