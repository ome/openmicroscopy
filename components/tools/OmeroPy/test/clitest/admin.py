#!/usr/bin/env python

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.admin import AdminControl, NonZeroReturnCode
from omero.cli import CLI
from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"

class TestAdmin(unittest.TestCase):

    def setUp(self):
        self.cli = MockCLI()
        self.cli.register("a", AdminControl, "TEST")

    def invoke(self, string):
        self.cli.invoke(string, strict=True)

    def testMain(self):
        try:
            self.invoke("a")
        except NonZeroReturnCode:
            # Command-loop not implemented
            pass

    def testStartAsync(self):
        self.invoke("a startasync")

    def testCheck(self):
        self.cli = MockCLI()
        c = AdminControl(self.cli, omeroDir)
        c.check([])
        c("check")
    def testStop(self):
        self.cli = MockCLI()
        c = AdminControl(self.cli, omeroDir)
        c("stop")
        c.stop([])
    def testComplete(self):
        c = AdminControl()
        t = ""
        l = "admin deploy "
        b = len(l)
        l = c._complete(t,l+"lib",b,b+3)
        self.assert_( "omero" in l, str(l) )
        l = c._complete(t,l+"lib/",b,b+4)
        self.assert_( "omero" in l, str(l) )

    def testProperMethodsUseConfigXml(self):
        self.fail("NYI")

if __name__ == '__main__':
    unittest.main()
