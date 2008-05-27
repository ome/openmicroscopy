#!/usr/bin/env python

"""
   Test of the omero base control class

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, BaseControl

omeroDir = path(os.getcwd()) / "build"

class TestBase(unittest.TestCase):
    def testNoArgs(self):
        c = BaseControl()
        c._noargs()
        c()
    def testArgs(self):
        c = BaseControl()
        c._oneArg("a")
        c("a")

if __name__ == '__main__':
    unittest.main()
