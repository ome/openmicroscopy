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
import omero.clients

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
        self.cli.checksStatus(0) # I.e. running
        self.cli.addCall(0)
        self.invoke("a stopasync")
        self.cli.assertStderr([])
        self.cli.assertStdout([])

    def testStopAsyncNotRunning(self):
        self.cli.checksStatus(1) # I.e. not running
        self.invoke("a stopasync", fails=True)
        self.cli.assertStderr(["Server not running"])
        self.cli.assertStdout([])

    def testStop(self):
        self.cli.checksStatus(0) # I.e. running
        self.cli.addCall(0)
        self.cli.checksStatus(1) # I.e. not running
        self.invoke("a stop")
        self.cli.assertStderr([])
        self.cli.assertStdout(['Waiting on shutdown. Use CTRL-C to exit'])

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

    #
    # Bugs
    #

    """
    Issues with error handling in certain situations
    especially with changing networks interfaces.
    """

    def test7325NoWaitForShutdown(self):
        """
        First issue in the error reported in Will's description
        (ignoring the comments) is that if the master could
        not be reached, there should be no waiting on shutdown.
        """

        # Although later status says the servers not running
        # the node ping much return a 0 because otherwise
        # stopasync returns immediately
        self.cli.checksStatus(0)     # node ping

        # Then since "Was the server already stopped?" was
        # printed, the call to shutdown master must return 1
        self.cli.addCall(1)

        self.invoke("a restart")
        self.cli.assertStderr([])
        self.cli.assertStdout([])

        # This test fails. With whatever solution is chosen
        # for 7325, the restart here should not wait on shutdown
        # if there's no connection available.

if __name__ == '__main__':
    unittest.main()
