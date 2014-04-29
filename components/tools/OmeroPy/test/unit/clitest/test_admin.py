#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import pytest

from path import path

import omero
import omero.clients

from omero.cli import NonZeroReturnCode
from omero.plugins.admin import AdminControl
from omero.plugins.prefs import PrefsControl
from omero.util.temp_files import create_path

from mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"


class TestAdmin(object):

    def setup_method(self, method):
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
        tmp_var_dir = tmp_dir / "var"

        # Setup tmp dir
        [x.makedirs() for x in (tmp_grid_dir, tmp_lib_dir, tmp_var_dir)]
        prefs_file.copy(tmp_lib_dir)
        master_cfg.copy(tmp_etc_dir)
        internal_cfg.copy(tmp_etc_dir)

        # Other setup
        self.cli = MockCLI()
        self.cli.dir = tmp_dir
        self.cli.register("a", AdminControl, "TEST")
        self.cli.register("config", PrefsControl, "TEST")

    def teardown_method(self, method):
        self.cli.teardown_method(method)

    def invoke(self, string, fails=False):
        try:
            self.cli.invoke(string, strict=True)
            if fails:
                assert False, "Failed to fail"
        except:
            if not fails:
                raise

    def testMain(self):
        try:
            self.invoke("")
        except NonZeroReturnCode:
            # Command-loop not implemented
            pass

    #
    # Async first because simpler
    #

    def XtestStartAsync(self):
        # DISABLED: https://trac.openmicroscopy.org.uk/ome/ticket/10584
        self.cli.addCall(0)
        self.cli.checksIceVersion()
        self.cli.checksStatus(1)  # I.e. not running

        self.invoke("a startasync")
        self.cli.assertCalled()
        self.cli.assertStderr(
            ['No descriptor given. Using etc/grid/default.xml'])

    def testStopAsyncRunning(self):
        self.cli.checksStatus(0)  # I.e. running
        self.cli.addCall(0)
        self.invoke("a stopasync")
        self.cli.assertStderr([])
        self.cli.assertStdout([])

    def testStopAsyncNotRunning(self):
        self.cli.checksStatus(1)  # I.e. not running
        self.invoke("a stopasync", fails=True)
        self.cli.assertStderr(["Server not running"])
        self.cli.assertStdout([])

    def testStop(self):
        self.cli.checksStatus(0)  # I.e. running
        self.cli.addCall(0)
        self.cli.checksStatus(1)  # I.e. not running
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
        pytest.raises(NonZeroReturnCode, self.invoke, "a status")

    def testStatusSMFails(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["a"]
        control._intcfg = lambda: ""

        def sm(*args):
            raise Exception("unknown")
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        pytest.raises(NonZeroReturnCode, self.invoke, "a status")

    def testStatusPasses(self):

        # Setup the call to bin/omero admin ice node
        popen = self.cli.createPopen()
        popen.wait().AndReturn(0)

        # Setup the call to session manager
        control = self.cli.controls["a"]
        control._intcfg = lambda: ""

        def sm(*args):

            class A(object):
                def create(self, *args):
                    raise omero.WrappedCreateSessionException()
            return A()
        control.session_manager = sm

        self.cli.mox.ReplayAll()
        self.invoke("a status")
        assert 0 == self.cli.rv

    #
    # SERVICE/FILE/TARGET Parsing
    #

    @pytest.mark.parametrize(
        "data",
        (
        # None
         ("a deploy",
          None, None, []),

        # One pos; no optional
         ("a deploy Processor-0",
          "Processor-0", None, []),

         ("a deploy exists",
          None, "exists", []),

         ("a deploy bar",
          None, None, ["bar"]),

        # Two pos; no optional

         ("a deploy Processor-0 exists",
          "Processor-0", "exists", []),

         ("a deploy exists bar",
          None, "exists", ["bar"]),

         ("a deploy bar boom",
          None, None, ["bar", "boom"]),

        # Three pos; no optional

         ("a deploy Processor-0 exists bar",
          "Processor-0", "exists", ["bar"]),

         ("a deploy Processor-0 bar boom",
          "Processor-0", None, ["bar", "boom"]),

         ("a deploy exists bar boom",
          None, "exists", ["bar", "boom"]),

         ("a deploy bar boom baz",
          None, None, ["bar", "boom", "baz"]),

        # No pos; all optional

         ("a deploy --service=Processor-0 --file=exists --target bar",
          "Processor-0", "exists", ["bar"]),

         ("a deploy --service=Processor-0 --file=exists --target bar --target boom",
          "Processor-0", "exists", ["bar", "boom"]),

        # 1 pos; 2 optional

         ("a deploy --service=Processor-0 exists --target bar",
          "Processor-0", "exists", ["bar"]),

         ("a deploy Processor-0 --file=exists --target bar",
          "Processor-0", "exists", ["bar"]),

         ))
    def testParsing(self, data):

        class InterceptingAdmin(AdminControl):
            def deploy(self, args):
                self.intercepted = args

        data_in = data[0]
        exp_service = data[1]
        exp_file = data[2]
        exp_targets = list(data[3])

        self.cli.register("a", InterceptingAdmin, "TEST")
        admin = self.cli.controls["a"]

        tmp = create_path()
        data_in = data_in.replace("exists", tmp)
        if exp_file:
            exp_file = exp_file.replace("exists", tmp)

        self.invoke(data_in)
        args = admin.intercepted
        targets = args.targets
        service, file = admin._descript(args)

        assert service == exp_service
        assert targets == exp_targets

        if exp_file is None:
            assert "default.xml" in file
        else:
            assert file == exp_file
