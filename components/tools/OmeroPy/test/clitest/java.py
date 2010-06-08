#!/usr/bin/env python

"""
   Test of the omero java control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.node import NodeControl
from omero.cli import Context
from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"

class TestJava(unittest.TestCase):

    def testStart(self):
        self.cli = MockCLI()
        c = NodeControl(omeroDir, self.cli)
        self.assert_(c._likes(None))
        self.assert_(c._likes("start"))
        c("start")
        self.assert_(self.cli.called == 1)
        c("start")
        self.assert_(self.cli.called == 2)

    def testStop(self):
        self.cli = MockCLI()
        c = NodeControl(omeroDir, self.cli)
        c("stop")
        c.stop()

    def testKill(self):
        self.cli = MockCLI()

        p = subprocess.Popen(["sleep","100"])

        c = NodeControl(omeroDir, self.cli)
        f = file(c._pid(),"w")
        f.write(str(p.pid))
        f.close()
        c("kill")
        c.kill()
if __name__ == '__main__':
    unittest.main()
