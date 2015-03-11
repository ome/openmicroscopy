#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the omero import control.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
from path import path
import omero.clients
import omero_ext.uuid as uuid
from omero.cli import CLI, NonZeroReturnCode
# Workaround for a poorly named module
plugin = __import__('omero.plugins.import', globals(), locals(),
                    ['ImportControl'], -1)
ImportControl = plugin.ImportControl

help_arguments = ("-h", "--javahelp", "--java-help", "--advanced-help")


class MockClient(omero.clients.BaseClient):

    def setSessionId(self, uuid):
        self._uuid = uuid

    def getSessionId(self):
        return self._uuid


class TestImport(object):

    def setup_method(self, method):
        self.cli = CLI()
        self.cli.register("import", ImportControl, "TEST")
        self.args = ["import"]

    def add_client_dir(self):
        dist_dir = path(__file__) / ".." / ".." / ".." / ".." / ".." / ".." /\
            ".." / "dist"  # FIXME: should not be hard-coded
        dist_dir = dist_dir.abspath()
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def mkdir(self, parent, name, with_ds_store=False):
        child = parent / name
        child.mkdir()
        if with_ds_store:
            ds_store = child / ".DS_STORE"
            ds_store.write("")
        return child

    def mkfakescreen(self, screen_dir, nplates=2, nruns=2, nwells=2,
                     nfields=4, with_ds_store=False):

        fieldfiles = []
        for iplate in range(nplates):
            plate_dir = self.mkdir(
                screen_dir, "Plate00%s" % str(iplate),
                with_ds_store=with_ds_store)
            for irun in range(nruns):
                run_dir = self.mkdir(
                    plate_dir, "Run00%s" % str(irun),
                    with_ds_store=with_ds_store)
                for iwell in range(nwells):
                    well_dir = self.mkdir(
                        run_dir, "WellA00%s" % str(iwell),
                        with_ds_store=with_ds_store)
                    for ifield in range(nfields):
                        fieldfile = (well_dir / ("Field00%s.fake" %
                                                 str(ifield)))
                        fieldfile.write('')
                        fieldfiles.append(fieldfile)
        return fieldfiles

    def mkfakepattern(self, tmpdir, nangles=7, ntimepoints=10):

        spim_dir = tmpdir.join("SPIM")
        spim_dir.mkdir()
        tiffiles = []
        for angle in range(1, nangles + 1):
            for timepoint in range(1, ntimepoints + 1):
                tiffile = (spim_dir / ("spim_TL%s_Angle%s.fake" %
                                       (str(timepoint), str(angle))))
                tiffile.write('')
                print str(tiffile)
                tiffiles.append(tiffile)
        patternfile = spim_dir / "spim.pattern"
        patternfile.write("spim_TL<1-%s>_Angle<1-%s>.fake"
                          % (str(ntimepoints), str(nangles)))
        assert len(tiffiles) == nangles * ntimepoints
        return patternfile, tiffiles

    def testDropBoxArgs(self):
        class MockImportControl(ImportControl):
            def importer(this, args):
                assert args.server == "localhost"
                assert args.port == "4064"
                assert args.key == "b0742975-03a1-4f6d-b0ac-639943f1a147"
                assert args.errs == "/tmp/dropbox.err"
                assert args.file == "/tmp/dropbox.out"

        self.cli.register("mock-import", MockImportControl, "HELP")
        self.args = ['-s', 'localhost', '-p', '4064', '-k',
                     'b0742975-03a1-4f6d-b0ac-639943f1a147']
        self.args += ['mock-import', '---errs=/tmp/dropbox.err']
        self.args += ['---file=/tmp/dropbox.out']
        self.args += ['--', '/OMERO/DropBox/root/tinyTest.d3d.dv']

        self.cli.invoke(self.args)

    @pytest.mark.parametrize('help_argument', help_arguments)
    def testHelp(self, help_argument):
        """Test help arguments"""
        self.args += [help_argument]
        self.cli.invoke(self.args)

    @pytest.mark.parametrize('clientdir_exists', [True, False])
    def testImportNoClientDirFails(self, tmpdir, clientdir_exists):
        """Test fake screen import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        if clientdir_exists:
            self.args += ["--clientdir", str(tmpdir)]
        self.args += [str(fakefile)]

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("data", (("1", False), ("3", True)))
    def testImportDepth(self, tmpdir, capfd, data):
        """Test import using depth argument"""

        dir1 = tmpdir.join("a")
        dir1.mkdir()
        dir2 = dir1 / "b"
        dir2.mkdir()
        fakefile = dir2 / "test.fake"
        fakefile.write('')

        self.add_client_dir()
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

        self.add_client_dir()
        self.args += ["-f", "--debug=ERROR"]
        self.args += [str(fakefile)]

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        outputlines = str(o).split('\n')
        reader = 'loci.formats.in.FakeReader'
        assert outputlines[-2] == str(fakefile)
        assert outputlines[-3] == \
            "# Group: %s SPW: false Reader: %s" % (str(fakefile), reader)

    @pytest.mark.parametrize('with_ds_store', (True, False))
    def testImportFakeScreen(self, tmpdir, capfd, with_ds_store):
        """Test fake screen import"""

        screen_dir = tmpdir.join("screen.fake")
        screen_dir.mkdir()
        fieldfiles = self.mkfakescreen(
            screen_dir, with_ds_store=with_ds_store)

        self.add_client_dir()
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

    def testImportPattern(self, tmpdir, capfd):
        """Test pattern import"""

        patternfile, tiffiles = self.mkfakepattern(tmpdir)

        self.add_client_dir()
        self.args += ["-f", "--debug=ERROR"]
        self.args += [str(patternfile)]

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        outputlines = str(o).split('\n')
        reader = 'loci.formats.in.FilePatternReader'
        print o
        assert outputlines[-len(tiffiles)-3] == \
            "# Group: %s SPW: false Reader: %s" % (str(patternfile), reader)
        assert outputlines[-len(tiffiles)-2] == str(patternfile)
        for i in range(len(tiffiles)):
            assert outputlines[-1-len(tiffiles)+i] == str(tiffiles[i])

    @pytest.mark.parametrize('hostname', ['localhost', 'servername'])
    @pytest.mark.parametrize('port', [None, 4064, 14064])
    def testLoginArguments(self, monkeypatch, hostname, port):
        self.args += ['test.fake']
        control = self.cli.controls['import']
        control.login_args = []
        sessionid = str(uuid.uuid4())

        def new_client(x):
            if port:
                c = MockClient(hostname, port)
            else:
                c = MockClient(hostname)
            c.setSessionId(sessionid)
            return c
        monkeypatch.setattr(self.cli, 'conn', new_client)
        control.set_login_arguments(self.cli.parser.parse_args(self.args))

        expected_args = ['-s', '%s' % hostname]
        expected_args += ['-p', '%s' % (port or 4064)]
        expected_args += ['-k', '%s' % sessionid]
        assert control.login_args == expected_args
