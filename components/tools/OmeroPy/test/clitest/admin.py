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

from omero.cli import CLI, NonZeroReturnCode
from omero.plugins.admin import AdminControl
from omero.plugins.prefs import PrefsControl
from omero.util.temp_files import create_path

from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"

class TestAdmin(unittest.TestCase):

    def setUp(self):
        # Non-temp directories
        build_dir = path() / "build"
        top_dir = path() / ".." / ".." / ".."
        etc_dir = top_dir / "etc"

        # Necessary fiels
        prefs_file = build_dir / "prefs.class"
        internal_cfg = etc_dir / "internal.cfg"
        master_cfg = etc_dir / "master.cfg"

        # Temp directories
        tmp_dir = create_path(folder=True)
        tmp_etc_dir = tmp_dir / "etc"
        tmp_grid_dir = tmp_etc_dir / "grid"
        tmp_lib_dir = tmp_dir / "lib"

        # Setup tmp dir
        [x.makedirs() for x in (tmp_grid_dir, tmp_lib_dir)]
        prefs_file.copy(tmp_lib_dir)
        master_cfg.copy(tmp_etc_dir)
        internal_cfg.copy(tmp_etc_dir)

        # Other setup
        self.cli = MockCLI()
        self.cli.dir = tmp_dir
        grid_dir = self.cli.dir / "etc" / "grid"
        self.cli.register("a", AdminControl, "TEST")
        self.cli.register("config", PrefsControl, "TEST")

    def tearDown(self):
        self.cli.tearDown()

    def invoke(self, string, fails=False):
        try:
            self.cli.invoke(string, strict=True)
            if fails: self.fail("Failed to fail")
        except:
            if not fails: raise

    def testMain(self):
        try:
            self.invoke("")
        except NonZeroReturnCode:
            # Command-loop not implemented
            pass

    #
    # Async first because simpler
    #

    def testStartAsync(self):
        self.cli.addCall(0)
        self.cli.checksIceVersion()
        self.cli.checksStatus(1) # I.e. not running

        self.invoke("a startasync")
        self.cli.assertCalled()
        self.cli.assertStderr(['No descriptor given. Using etc/grid/default.xml'])

    def testStopAsyncRunning(self):
        self.cli.addCall(0)
        self.invoke("a stopasync")
        self.cli.assertCalled()
        self.cli.assertStderr([])
        self.cli.assertStdout([])

    def testStopAsyncNotRunning(self):
        self.cli.addCall(1)
        self.invoke("a stopasync", fails=True)
        self.cli.assertCalled()
        self.cli.assertStderr([])
        self.cli.assertStdout(["Was the server already stopped?"])

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
            class A(object):
                def create(self, *args): raise omero.WrappedCreateSessionException()
            return A()
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        self.invoke("a status")
        self.assertEquals(0, self.cli.rv)

if __name__ == '__main__':
    unittest.main()
