#!/usr/bin/env python

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.admin import Control
from omero.cli import Event

omeroDir = path(os.getcwd()) / "build" 

class E1(Event):
    def pub(self, args):
        if not hasattr(self,"called"):
            self.called = 1
        else:
            self.called = self.called + 1

class TestAdmin(unittest.TestCase):
    def testMain(self):
        e1 = E1()
        c = Control(omeroDir, e1)
        c._noargs()
        c()
    def testStart(self):
        e1 = E1()
        c = Control(omeroDir, e1)
        self.assert_(c._likes(None))
        self.assert_(c._likes("start"))
        c("start")
        self.assert_(e1.called == 1)
        c("start")
        self.assert_(e1.called == 2)
    def testCheck(self):
        e1 = E1()
        c = Control(omeroDir, e1)
        c.check()
        c("check")
    def testStop(self):
        e1 = E1()
        c = Control(omeroDir, e1)
        c("stop")
        c.stop()
if __name__ == '__main__':
    unittest.main()
