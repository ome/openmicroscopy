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
from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"

class TestDatabase(unittest.TestCase):

    def testBadVersionDies(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["NONE","NONE"]
        try:
            c("script")
            self.fail("must throw")
        except NonZeroReturnCode, nzrc:
            pass
    def testPasswordIsAskedForAgainIfDiffer(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["test","test","ome","bad","ome","ome"]
        c("script")
    def testPasswordIsAskedForAgainIfNull(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["test","test",None,"ome","ome"]
        c("script")
    def testPasswordIsAskedForAgainIfEmpty(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["test","test","","ome","ome"]
        c("script")
    def testPassword(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)#, self.cli)
        self.assert_(c._likes(None))
        self.assert_(c._likes("password"))
        self.cli.response = ["ome","ome"]
        c("password")
    def testAutomatedPassword(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)#, self.cli)
        self.assert_(c._likes(None))
        self.assert_(c._likes("password"))
        self.cli.response = []
        c(["password","ome"])
    def testScript(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)#, self.cli)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["test","test","ome","ome"]
        c("script")
    def testAutomatedScript(self):
        self.cli = MockCLI()
        c = DatabaseControl(ctx = self.cli, dir = omeroDir)#, self.cli)
        self.assert_(c._likes(None))
        self.assert_(c._likes("script"))
        self.cli.response = ["test","ome","ome"]
        c(["script","test"])
        self.cli.response = ["ome","ome"]
        c(["script","test","test"])
        self.cli.response = []
        c(["script","test","test","ome"])
if __name__ == '__main__':
    unittest.main()
