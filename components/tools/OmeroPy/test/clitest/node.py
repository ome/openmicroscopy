#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero node control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.node import NodeControl
from omero.cli import Context
from clitest.mocks import MockCLI

omeroDir = path(os.getcwd()) / "build"
etcDir = omeroDir / "etc"
try:
    etcDir.makedirs()
except:
    pass

hostCfg = path(etcDir / NodeControl(None,None)._node()) + ".cfg"
hostCfg.write_text("IceGrid.Node.Data=var/TEST_DIRECTORY")


class TestNode(unittest.TestCase):

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
