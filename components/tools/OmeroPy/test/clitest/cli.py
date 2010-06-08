#!/usr/bin/env python

"""
   Test of the omero cli base class including line parsing.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, os, subprocess, StringIO
from exceptions import Exception as Exc
from path import path
from omero.cli import Context, BaseControl, CLI

omeroDir = path(os.getcwd()) / "build"

class TestCli(unittest.TestCase):

    def testLineParsedCorrectly(self):
        class TestControl(BaseControl):
            def __call__(self, *args):
                if len(*args) != 2:
                    raise Exc("Args was over parsed! %s" % args)
        cli = CLI()
        cli.register("test", TestControl)
        cli.invoke(["test","a","a b c d e"])
        self.assertEquals(0, cli.rv)

    def testParametersParsedCorrectly(self):
        class TestControl(BaseControl):
            def __call__(self2, *args):
                args = Arguments(args)
                self.assertEquals("b",args["a"])
        cli = CLI()
        cli.register("test", TestControl)
        cli.invoke(["test","a=b"])
        self.assertEquals(0, cli.rv)

    def testVarious(self):
        self.fail("How to handle: background loading, unknown commands, do_ methods, delegation")
        self.fail("omero start") # delegates
        self.fail("omero login")
        self.fail("omero etc.")
        self.fail("special noarg handling") # Perhaps with parse_known_args?
        self.fail("name completion in help")
        self.fail("help on bad arguments")
        self.fail("tracing, debugging, etc")
        self.fail("loading")
        self.fail("hyphen splitting")

if __name__ == '__main__':
    unittest.main()
