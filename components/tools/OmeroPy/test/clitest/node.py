#!/usr/bin/env python

"""
   Test of the omero node control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.node import NodeControl
from omero.cli import Context

omeroDir = path(os.getcwd()) / "build"
etcDir = omeroDir / "etc"
try:
    etcDir.makedirs()
except:
    pass
hostCfg = file(path(etcDir / NodeControl(None,None)._node()) + ".cfg","w")
hostCfg.write("IceGrid.Node.Data=var/TEST_DIRECTORY")
hostCfg.close()

class E1(Context):
    def popen(self, *args, **kwards):
        if not hasattr(self,"called"):
            self.called = 1
        else:
            self.called = self.called + 1

class TestNode(unittest.TestCase):
    def testStart(self):
        e1 = E1()
        c = NodeControl(omeroDir, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("start"))
        c("start")
        self.assert_(e1.called == 1)
        c("start")
        self.assert_(e1.called == 2)
    def testStop(self):
        e1 = E1()
        c = NodeControl(omeroDir, e1)
        c("stop")
        c.stop()
    def testKill(self):
        e1 = E1()

        p = subprocess.Popen(["sleep","100"])

        c = NodeControl(omeroDir, e1)
        f = file(c._pid(),"w")
        f.write(str(p.pid))
        f.close()
        c("kill")
        c.kill()
if __name__ == '__main__':
    unittest.main()
