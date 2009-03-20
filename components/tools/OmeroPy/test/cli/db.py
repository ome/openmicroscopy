#!/usr/bin/env python

"""
   Test of the omero db control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.db import DatabaseControl
from omero.cli import Context, CLI, NonZeroReturnCode

omeroDir = path(os.getcwd()) / "build"
class E1(CLI):
    def __init__(self, *args, **kwargs):
        CLI.__init__(self, *args, **kwargs)
        self.response = []
        self.called = 0
    def input(self, *args, **kwargs):
        rv = self.response.pop(0)
        print "Returning " + str(rv)
        return rv

class TestDatabase(unittest.TestCase):
    def testBadVersionDies(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["NONE","NONE"]
        try:
            c("script")
            self.fail("must throw")
        except NonZeroReturnCode, nzrc:
            pass
    def testPasswordIsAskedForAgainIfDiffer(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["test","test","ome","bad","ome","ome"]
        c("script")
    def testPasswordIsAskedForAgainIfNull(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["test","test",None,"ome","ome"]
        c("script")
    def testPasswordIsAskedForAgainIfEmpty(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["test","test","","ome","ome"]
        c("script")
    def testPassword(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)#, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("password"))
        e1.response = ["ome","ome"]
        c("password")
    def testAutomatedPassword(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)#, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("password"))
        e1.response = []
        c(["password","ome"])
    def testScript(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)#, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["test","test","ome","ome"]
        c("script")
    def testAutomatedScript(self):
        e1 = E1()
        c = DatabaseControl(ctx = e1, dir = omeroDir)#, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        e1.response = ["test","ome","ome"]
        c(["script","test"])
        e1.response = ["ome","ome"]
        c(["script","test","test"])
        e1.response = []
        c(["script","test","test","ome"])
if __name__ == '__main__':
    unittest.main()
