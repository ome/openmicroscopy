#!/usr/bin/env python

"""
   Test of the return code functionality

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, BaseControl, CLI

omeroDir = path(os.getcwd()) / "build"

class TestRCode(unittest.TestCase):

    class T(BaseControl):
        def __call__(self, *args):
            self.ctx.rv = 1

    def testOne(self):
        cli = CLI()
        cli.register("t", TestRCode.T, "TEST")
        cli.invoke(["t"])
        self.assert_(cli.rv == 1, cli.rv)
if __name__ == '__main__':
    unittest.main()
