#!/usr/bin/env python

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import exceptions
import unittest
import os

from path import path

import omero
import omero_ServerErrors_ice

from omero.plugins.admin import AdminControl, NonZeroReturnCode
from omero.cli import CLI
from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"

class TestAdmin(unittest.TestCase):

    def setUp(self):
        self.cli = MockCLI()
        self.cli.register("a", AdminControl, "TEST")

    def tearDown(self):
        self.cli.tearDown()

    def invoke(self, string):
        self.cli.invoke(string, strict=True)

    def testMain(self):
        try:
            self.invoke()
        except NonZeroReturnCode:
            # Command-loop not implemented
            pass

    def testStartAsync(self):
        self.invoke("a startasync")

    def testStop(self):
        self.invoke("a stop")

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

    #
    # STATUS
    #

    def testStatusNodeFails(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(1)

        self.cli.mox.ReplayAll()
        self.assertRaises(NonZeroReturnCode, self.invoke, "a status")

    def testStatusSMFails(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["a"]
        control._intcfg = lambda: ""
        def sm(*args):
            raise exceptions.Exception("unknown")
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        self.assertRaises(NonZeroReturnCode, self.invoke, "a status")

    def testStatusPasses(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["a"]
        control._intcfg = lambda: ""
        def sm(*args):
            raise omero.WrappedCreateSessionException()
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        self.invoke("a status")
        self.assertEquals(0, self.cli.rv)

if __name__ == '__main__':
    unittest.main()
