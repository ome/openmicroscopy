#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

import py.test
import omero
from omero.plugins.download import DownloadControl
from omero.cli import NonZeroReturnCode
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring


class TestDownload(CLITest):

    def setup_method(self, method):
        super(TestDownload, self).setup_method(method)
        self.cli.register("download", DownloadControl, "TEST")
        self.args += ["download"]

    def create_original_file(self, content):
        """
        Create an original file and upload it onto the server
        """
        ofile = omero.model.OriginalFileI()
        ofile.name = rstring("")
        ofile.path = rstring("")
        ofile = self.update.saveAndReturnObject(ofile)

        rfs = self.sf.createRawFileStore()
        try:
            rfs.setFileId(ofile.id.val)
            rfs.write(content, 0, len(content))
            ofile = rfs.save()
            assert len(content) == ofile.size.val
            return ofile
        finally:
            rfs.close()

    def testInvalidIDInput(self):
        self.args += ["file", 'test']
        with py.test.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testInvalidObject(self):
        self.args += ["object:name", 'test']
        with py.test.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testOriginalFileInvalidID(self, tmpdir):
        tmpfile = tmpdir.join('test')
        self.args += ["-1", str(tmpfile)]
        with py.test.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    # OriginalFile test
    # ========================================================================
    def testOriginalFileTmpfile(self, tmpdir):
        ofile = self.create_original_file("test")
        tmpfile = tmpdir.join('test')
        self.args += [str(ofile.id.val), str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            assert f.read() == "test"

    def testOriginalFileStdout(self, capsys):
        ofile = self.create_original_file("test")
        self.args += [str(ofile.id.val), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    # Image test
    # ========================================================================
    def testImage(self, tmpdir):
        filename = self.OmeroPy / ".." / ".." / ".." / \
            "components" / "common" / "test" / "tinyTest.d3d.dv"
        with open(filename) as f:
            bytes1 = f.read()
        pix_ids = self.import_image(filename)
        pixels = self.query.get("Pixels", long(pix_ids[0]))
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes2 = f.read()
        assert bytes1 == bytes2

    def testMIF(self, tmpdir):
        images = self.importMIF(2)
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % images[0].id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes = f.read()
        assert not bytes
