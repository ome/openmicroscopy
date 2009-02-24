#!/usr/bin/env python

"""
   Test of the sessions pluging

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, BaseControl, CLI
from omero.plugins.sessions import SessionsControl

omeroDir = path(os.getcwd()) / "build"

class TestSessions(unittest.TestCase):

    def cli(self):
        cli = CLI()
        cli.register("s", SessionsControl)
        return cli

    def test1(self):
        cli = self.cli()
        cli.invoke(["s","login"])
        self.assertEquals(0, cli.rv)

    def test1(self):
        cli = self.cli()
        cli.invoke(["s","login"])
        self.assertEquals(0, cli.rv)

if __name__ == '__main__':
    unittest.main()
