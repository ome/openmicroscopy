#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero import control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import CLI
# Workaround for a poorly named module
plugin = __import__('omero.plugins.import', globals(), locals(),
                    ['ImportControl'], -1)
ImportControl = plugin.ImportControl


class TestImport(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("import", ImportControl, "TEST")
        self.args = ["import"]

    def testDropBoxArgs(self):
        class MockImportControl(ImportControl):
            def importer(this, args):
                assert args.server == "localhost"
                assert args.port == "4064"
                assert args.key == "b0742975-03a1-4f6d-b0ac-639943f1a147"
                assert args.errs == "/Users/cblackburn/omero/tmp/\
omero_cblackburn/6915/dropboxuUGl5rerr"
                assert args.file == "/Users/cblackburn/omero/tmp/\
omero_cblackburn/6915/dropboxaDCjQlout"

        self.cli.register("mock-import", MockImportControl, "HELP")
        self.args = ['-s', 'localhost', '-p', '4064', '-k',
                     'b0742975-03a1-4f6d-b0ac-639943f1a147']
        self.args += ['mock-import', '---errs=/Users/cblackburn/omero/tmp/\
omero_cblackburn/6915/dropboxuUGl5rerr']
        self.args += ['---file=/Users/cblackburn/omero/tmp/\
omero_cblackburn/6915/dropboxaDCjQlout']
        self.args += ['--', '/OMERO/DropBox/root/tinyTest.d3d.dv']

        self.cli.invoke(self.args)

    def testHelp(self):
        """Test help command"""
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)
