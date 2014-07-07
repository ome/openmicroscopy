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

    def mkfakescreen(self, tmpdir, nplates=2, nruns=2, nwells=2, nfields=4):

        screen_dir = tmpdir.join("screen.fake")
        screen_dir.mkdir()
        fieldfiles = []
        for iplate in range(nplates):
            plate_dir = screen_dir / ("Plate00%s" % str(iplate))
            plate_dir.mkdir()
            for irun in range(nruns):
                run_dir = plate_dir / ("Run00%s" % str(irun))
                run_dir.mkdir()
                for iwell in range(nwells):
                    well_dir = run_dir / ("WellA00%s" % str(iwell))
                    well_dir.mkdir()
                    for ifield in range(nfields):
                        fieldfile = (well_dir / ("Field00%s.fake" %
                                                 str(ifield)))
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
        assert outputlines[-len(fieldfiles)-2] == \
            "# Group: %s SPW: true Reader: %s" % (str(fieldfiles[0]), reader)
        for i in range(len(fieldfiles)):
            assert outputlines[-1-len(fieldfiles)+i] == str(fieldfiles[i])
