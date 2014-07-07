#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero import control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
from path import path
from omero.cli import CLI
# Workaround for a poorly named module
plugin = __import__('omero.plugins.import', globals(), locals(),
                    ['ImportControl'], -1)
ImportControl = plugin.ImportControl

help_arguments = ("-h", "--javahelp", "--advanced-help")


class TestImport(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("import", ImportControl, "TEST")
        self.args = ["import"]
        dist_dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dist_dir = dist_dir.abspath()
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def mkfakescreen(self, tmpdir):

        screen_dir = tmpdir.join("screen.fake")
        screen_dir.mkdir()
        fieldfiles = []
        for i in [0, 1]:
            plate_dir = screen_dir / ("Plate00%s" % str(i))
            plate_dir.mkdir()
            run_dir = plate_dir / "Run000"
            run_dir.mkdir()
            well_dir = run_dir / "WellA000"
            well_dir.mkdir()
            fieldfile = (well_dir / "Field000.fake")
            fieldfile.write('')
            fieldfiles.append(fieldfile)
        return fieldfiles

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

    @pytest.mark.parametrize('help_argument', help_arguments)
    def testHelp(self, help_argument):
        """Test help arguments"""
        self.args += [help_argument]
        self.cli.invoke(self.args)

    @pytest.mark.parametrize("data", (("1", False), ("3", True)))
    def testImportDepth(self, tmpdir, capfd, data):
        """Test import using depth argument"""

        dir1 = tmpdir.join("a")
        dir1.mkdir()
        dir2 = dir1 / "b"
        dir2.mkdir()
        fakefile = dir2 / "test.fake"
        fakefile.write('')

        self.args += ["-f", "--debug=ERROR"]
        self.args += [str(dir1)]

        depth, result = data
        self.cli.invoke(self.args + ["--depth=%s" % depth], strict=True)
        o, e = capfd.readouterr()
        if result:
            assert str(fakefile) in str(o)
        else:
            assert str(fakefile) not in str(o)

    def testImportFakeImage(self, tmpdir, capfd):
        """Test fake image import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += ["-f", "--debug=ERROR"]
        self.args += [str(fakefile)]

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        outputlines = str(o).split('\n')
        reader = 'loci.formats.in.FakeReader'
        assert outputlines[-2] == str(fakefile)
        assert outputlines[-3] == \
            "# Group: %s SPW: false Reader: %s" % (str(fakefile), reader)

    def testImportFakeScreen(self, tmpdir, capfd):
        """Test fake screen import"""

        fieldfiles = self.mkfakescreen(tmpdir)

        self.args += ["-f", "--debug=ERROR"]
        self.args += [str(fieldfiles[0])]

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        outputlines = str(o).split('\n')
        reader = 'loci.formats.in.FakeReader'
        assert outputlines[-2] == str(fieldfiles[1])
        assert outputlines[-3] == str(fieldfiles[0])
        assert outputlines[-4] == \
            "# Group: %s SPW: true Reader: %s" % (str(fieldfiles[0]), reader)
