#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

from test.integration.clitest.cli import CLITest, RootCLITest
from omero.cli import NonZeroReturnCode
from omero.plugins.fs import FsControl
from omero.util import long_to_path

from path import path

import pytest

transfers = ['ln_s', 'ln', 'ln_rm', 'cp', 'cp_rm']
repos = ['Managed', 'Public', 'Script']


class TestFS(CLITest):

    def setup_method(self, method):
        super(TestFS, self).setup_method(method)
        self.cli.register("fs", FsControl, "TEST")
        self.args += ["fs"]

    def set_conn_args(self):
        passwd = self.root.getProperty("omero.rootpass")
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["fs", "-w", passwd]
        self.args += ["-s", host, "-p",  port]

    def parse_ids(self, output):
        ids = []
        for line in output.split('\n')[:-1]:
            ids.append(int(line.split(',')[1]))
        return ids

    def parse_repos(self, output):
        ids = []
        for line in output.split('\n')[:-1]:
            ids.append(line.split(',')[3])
        return ids

    def testRepos(self, capsys):
        """Test fs repos subcommand"""

        self.args += ["repos", "--style=plain"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()

        errs = [line
                for line in e.split("\n")
                if line.strip() and "Joined session" not in line]
        if errs:
            raise Exception(errs)

        assert set(self.parse_repos(o)) == set(repos)

    def testSetsWithTransfer(self, capsys):
        """Test --with-transfer option of fs sets subcommand"""

        f = {}
        i0 = self.importMIF(1)
        f[None] = self.get_fileset(i0)

        for transfer in transfers:
            i = self.importMIF(1, extra_args=['--transfer=%s' % transfer])
            f[transfer] = self.get_fileset(i)

        self.args += ["sets", "--style=plain"]
        self.cli.invoke(self.args, strict=True)
        o, e = capsys.readouterr()
        assert set(self.parse_ids(o)) == set([x.id.val for x in f.values()])

        for transfer in transfers:
            self.cli.invoke(self.args + ['--with-transfer', '%s' % transfer],
                            strict=True)
            o, e = capsys.readouterr()
            assert set(self.parse_ids(o)) == \
                set([x.id.val for (k, x) in f.iteritems() if k == transfer])

    def testSetsAdminOnly(self, capsys):
        """Test fs sets --check is admin-only"""

        self.args += ["sets", "--check"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")

    def testRenameAdminOnly(self, capsys):
        """Test fs rename is admin-only"""

        self.args += ["rename", "Fileset:1"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")

    def testFixPyramidAdminOnly(self, capsys):
        """Test fs fixpyramid is admin-only"""

        self.args += ["fixpyramid", "Image:1"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestFSRoot(RootCLITest):

    def setup_method(self, method):
        super(TestFSRoot, self).setup_method(method)
        self.cli.register("fs", FsControl, "TEST")
        self.args += ["fs"]

    @pytest.mark.broken(reason="Passes locally but fails on CI")
    def testFixPyramid(self, capsys):
        """Test fs fixpyramid"""

        cfg = self.root.sf.getConfigService()
        pixels_dir = path(cfg.getConfigValue("omero.data.dir")) / "Pixels"
        iid = self.importSingleImage().id.val
        subdir = path(long_to_path(iid, root=pixels_dir)).parent
        subdir.makedirs_p()
        pyramid_file = subdir / ("%s_pyramid" % iid)
        pyramid_file.touch()
        assert pyramid_file.exists()
        self.args += ["fixpyramid", "Image:%s" % iid]
        self.cli.invoke(self.args, strict=True)
        assert not pyramid_file.exists()

    def testFixPyramidWrongType(self):
        """Test fs fixpyramid with incorrect argument type"""

        self.args += ["fixpyramid", "Fileset:1"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
