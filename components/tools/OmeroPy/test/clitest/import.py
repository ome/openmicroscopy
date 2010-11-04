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

rootDir = path(__file__).dirname() / ".." / ".."
omeroDir = rootDir / "build"
pluginDir = rootDir / "src" / "omero" / "plugins"

def register(key, klass, help):
    map[key] = klass
loc = {"register": register}
execfile( str(pluginDir/"import.py"), loc)
ImportControl = map["import"]

class TestImport(unittest.TestCase):

    def testNoArgumentsDies(self):
        cli = CLI()
        cli.register("import", ImportControl, "HELP")

        try:
            cli.invoke([])
            self.assert_(cli.rv != 0)
        except NonZeroReturnCode, nzrc:
            pass

    def testDropBoxArgs(self):
        class MockImportControl(ImportControl):
            def importer(this, args):
                self.assertEquals(args.server, "localhost")
                self.assertEquals(args.port, "4064")
                self.assertEquals(args.key, "b0742975-03a1-4f6d-b0ac-639943f1a147")
                self.assertEquals(args.errs, "/Users/cblackburn/omero/tmp/omero_cblackburn/6915/dropboxuUGl5rerr")
                self.assertEquals(args.file, "/Users/cblackburn/omero/tmp/omero_cblackburn/6915/dropboxaDCjQlout")

        cmd = ['-s', 'localhost', '-p', '4064', '-k', 'b0742975-03a1-4f6d-b0ac-639943f1a147']
        cmd += ['import', '---errs=/Users/cblackburn/omero/tmp/omero_cblackburn/6915/dropboxuUGl5rerr']
        cmd += ['---file=/Users/cblackburn/omero/tmp/omero_cblackburn/6915/dropboxaDCjQlout']
        cmd += ['--', '/OMERO/DropBox/root/tinyTest.d3d.dv']

        cli = CLI()
        cli.register("import", MockImportControl, "HELP")
        cli.invoke(cmd)

if __name__ == '__main__':
    unittest.main()
