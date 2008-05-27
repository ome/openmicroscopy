#!/usr/bin/env python

"""
   Test of the omero base control class

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Event, BaseControl

omeroDir = path(os.getcwd()) / "build"

class TestBase(unittest.TestCase):
    def testNoArgs(self):
        e1 = E1()
        c = BaseControl()
        c._noargs()
        c()

if __name__ == '__main__':
    unittest.main()
