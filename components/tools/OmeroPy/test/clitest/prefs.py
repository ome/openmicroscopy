#!/usr/bin/env python

"""
   Test of the scripts plugin

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
from omero.cli import Context, BaseControl, CLI, NonZeroReturnCode
from omero.config import ConfigXml
from omero.plugins.prefs import PrefsControl, HELP
from omero.util.temp_files import create_path

class MockCLI(CLI):

    def __init__(self, *args, **kwargs):
        self.OUTPUT = []
        self.ERROR = []
        CLI.__init__(self, *args, **kwargs)
        self.register("config", PrefsControl, HELP)

    def out(self, *args):
        self.OUTPUT.append(args[0])

    def err(self, *args):
        self.ERROR.append(args[0])

class TestPrefs(unittest.TestCase):

    def setUp(self):
        unittest.TestCase.setUp(self)
        self.cli = MockCLI()
        self.p = create_path()

    def config(self):
        return ConfigXml(filename=str(self.p))

    def assertStdout(self, args):
        self.assertEquals(set(args), set(self.cli.OUTPUT))
        self.cli.OUTPUT = []

    def assertStderr(self, args):
        self.assertEquals(set(args), set(self.cli.ERROR))
        self.cli.ERROR = []

    def invoke(self, s):
        self.cli.invoke("config --source %s %s" % (self.p, s), strict=True)

    def testHelp(self):
        try:
            self.cli.invoke("config -h")
        except SystemExit:
            pass
        self.assertEquals(0, self.cli.rv)

    def testAll(self):
        config = self.config()
        config.default("test")
        config.close()
        self.invoke("all")
        self.assertStdout(["default", "test"])

    def testDefaultInitial(self):
        self.invoke("def")
        self.assertStdout(["default"])

    def testDefaultEnvironment(self):
        T = "testDefaultEnvironment"
        old = os.environ.get("OMERO_CONFIG", None)
        os.environ["OMERO_CONFIG"] = T
        try:
            self.invoke("def")
            self.assertStdout([T])
        finally:
            if old:
                os.environ["OMERO_CONFIG"] = old
            else:
                del os.environ["OMERO_CONFIG"]

    def testDefaultSet(self):
        self.invoke("def x")
        self.invoke("def")
        self.assertStdout(["x"])

    def testGetSet(self):
        self.invoke("get X")
        self.assertStdout([])
        self.invoke("set A B")
        self.assertStdout([])
        self.invoke("get A")
        self.assertStdout(["B"])
        self.invoke("get")
        self.assertStdout(["A=B"])
        self.invoke("set A")
        self.invoke("keys")
        self.assertStdout([])

    def testKeys(self):
        self.invoke("keys")
        self.assertStdout([])
        self.invoke("set A B")
        self.invoke("keys")
        self.assertStdout(["A"])

    def testVersion(self):
        self.invoke("version")
        self.assertStdout([ConfigXml.VERSION])

    def testPath(self):
        self.invoke("path")
        self.assertStdout([self.p])

    def testLoad(self):
        to_load = create_path()
        to_load.write_text("A=B")
        self.invoke("load %s" % to_load)
        self.invoke("get")
        self.assertStdout(["A=B"])

        try:
            self.invoke("load %s" % to_load)
            self.fail("No NZRC")
        except NonZeroReturnCode:
            pass

        to_load.write_text("A=C")
        self.invoke("load -q %s" % to_load)
        self.invoke("get")
        self.assertStdout(["A=C"])
        self.assertStderr(["Duplicate property: A (B => B)"])

    def testLoadDoesNotExist(self):
        # ticket:7273
        self.assertRaises(NonZeroReturnCode, self.invoke, "load THIS_FILE_SHOULD_NOT_EXIST")

    def testDrop(self):
        self.invoke("def x")
        self.invoke("def")
        self.assertStdout(["x"])
        self.invoke("all")
        self.assertStdout(["x", "default"])
        self.invoke("def y")
        self.invoke("all")
        self.assertStdout(["x", "y", "default"])
        self.invoke("drop x")
        self.invoke("all")
        self.assertStdout(["y", "default"])

    def testEdit(self):
        """
        Testing edit is a bit more complex since it wants to
        start another process. Rather than using invoke, we
        manage things ourselves here.
        """
        def fake_edit_path(tmp_file, tmp_text):
            pass
        args = self.cli.parser.parse_args("config edit".split())
        control = self.cli.controls["config"]
        config = self.config()
        try:
            control.edit(args, config, fake_edit_path)
        finally:
            config.close()

if __name__ == '__main__':
    unittest.main()
