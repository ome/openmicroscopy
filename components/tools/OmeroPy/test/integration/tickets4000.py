#!/usr/bin/env python

"""
   Integration tests for tickets between 3000 and 3999

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import integration.library as lib
import Glacier2

from omero.rtypes import *


class TestTickets3000(lib.ITest):

    def test3138(self):
        """
        Try multiple logins to see if they slow down
        """
        user = self.new_user()
        name = user.omeName.val

        self.root.sf.getAdminService().changeUserPassword(name, rstring("GOOD"))

        self.loginAttempt(name, 0.1, less=True)
        self.loginAttempt(name, 3.0)
        self.loginAttempt(name, 0.1, "GOOD", less=True)
        self.loginAttempt(name, 0.1, less=True)
        self.loginAttempt(name, 3.0)

    def loginAttempt(self, name, t, pw="BAD", less=False):
        c = omero.client()
        try:
            t1 = time.time()
            try:
                c.createSession(name, pw)
                if pw == "BAD":
                    self.fail("Should not reach this point")
            except Glacier2.PermissionDeniedException:
                if pw != "BAD":
                    raise
            t2 = time.time()
            T = (t2-t1)
            if less:
                self.assertTrue(T < t, "%s > %s" % (T, t))
            else:
                self.assertTrue(T > t, "%s < %s" % (T, t))
        finally:
            c.__del__()


if __name__ == '__main__':
    unittest.main()
