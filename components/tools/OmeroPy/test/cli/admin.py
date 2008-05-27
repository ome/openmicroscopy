#!/usr/bin/env python

"""
   Test of the omero admin control.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.plugins.admin import AdminControl
from omero.cli import Context

omeroDir = path(os.getcwd()) / "build"

class E1(Context):
    def pub(self, args):
        if not hasattr(self,"called"):
            self.called = 1
        else:
            self.called = self.called + 1

class TestAdmin(unittest.TestCase):
    def testMain(self):
        e1 = E1()
        c = AdminControl(e1, omeroDir)
        c._noargs()
        c()
    def testStart(self):
        e1 = E1()
        c = AdminControl(e1, omeroDir)
        self.assert_(c._likes(None))
        self.assert_(c._likes("start"))
        c("start")
        self.assert_(e1.called == 1)
        c("start")
        self.assert_(e1.called == 2)
    def testCheck(self):
        e1 = E1()
        c = AdminControl(e1, omeroDir)
        c.check([])
        c("check")
    def testStop(self):
        e1 = E1()
        c = AdminControl(e1, omeroDir)
        c("stop")
        c.stop([])
    def testComplete(self):
        c = AdminControl()
        t = ""
        l = "admin deploy "
        b = len(l)
        l = c._complete(t,l+"lib",b,b+3)
        self.assert_( "omero" in l, str(l) )
        l = c._complete(t,l+"lib/",b,b+4)
        self.assert_( "omero" in l, str(l) )

if __name__ == '__main__':
    unittest.main()
