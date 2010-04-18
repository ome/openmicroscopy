#!/usr/bin/env python

"""
   Test of the omero import control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, subprocess, StringIO
from path import path
from omero.cli import Context, CLI, NonZeroReturnCode

# Workaround for a poorly named module
map = {}
omeroDir = path(os.getcwd()) / "build"
pluginDir = path(os.getcwd()) / "src" / "omero" / "plugins"

def register(k,v):
    map[k] = v
loc = {"register": register}
execfile( str(pluginDir/"import.py"), loc)
ImportControl = map["import"]

class TestImport(unittest.TestCase):
    def testNoArgumentsDies(self):
        c = ImportControl(ctx = CLI(), dir = omeroDir)
        try:
            c()
            self.assert_(c.ctx.rv != 0)
        except NonZeroReturnCode, nzrc:
            pass
if __name__ == '__main__':
    unittest.main()
