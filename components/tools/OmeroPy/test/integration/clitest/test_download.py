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

import pytest
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

    def setup_user_and_two_groups(self):
        group1 = self.new_group(perms='rw----')
        group2 = self.new_group(perms='rw----')
        user = self.new_user()
        self.add_groups(user, [group1, group2], owner=True)
        return user, group1, group2

    @pytest.mark.parametrize(
        'bad_input',
        ['-1', 'OriginalFile:-1', 'FileAnnotation:-1', 'Image:-1'])
    def testInvalidInput(self, bad_input):
        self.args += [bad_input, '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    # OriginalFile tests
    # ========================================================================
    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testNonExistingOriginalFile(self, tmpdir, prefix):
        ofile = self.create_original_file("test")
        self.args += ['%s%s' % (prefix, str(ofile.id.val + 1)), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileTmpfile(self, prefix, tmpdir):
        ofile = self.create_original_file("test")
        tmpfile = tmpdir.join('test')
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            assert f.read() == "test"

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileStdout(self, prefix, capsys):
        ofile = self.create_original_file("test")
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    @pytest.mark.parametrize('prefix', ['', 'OriginalFile:'])
    def testOriginalFileMultipleGroups(self, prefix, capsys):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        ofile = self.create_original_file("test")
        self.set_context(client, group2.id.val)
        self.args += ['%s%s' % (prefix, str(ofile.id.val)), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    # FileAnnotation tests
    # ========================================================================
    def testNonExistingFileAnnotation(self, tmpdir):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.args += ['FileAnnotation:%s' % str(fa.id.val + 1), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testFileAnnotationTmpfile(self, tmpdir):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        tmpfile = tmpdir.join('test')
        self.args += ['FileAnnotation:%s' % str(fa.id.val), str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            assert f.read() == "test"

    def testFileAnnotationStdout(self, capsys):
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.args += ['FileAnnotation:%s' % str(fa.id.val), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    def testFileAnnotationMultipleGroups(self, capsys):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        ofile = self.create_original_file("test")
        fa = omero.model.FileAnnotationI()
        fa.setFile(ofile)
        fa = self.update.saveAndReturnObject(fa)
        self.set_context(client, group2.id.val)
        self.args += ['FileAnnotation:%s' % str(fa.id.val), '-']
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert out == "test"

    # Image tests
    # ========================================================================
    def testNonExistingImage(self, tmpdir):
        image = self.importSingleImageWithCompanion()
        self.args += ["Image:%s" % str(image.id.val + 1), '-']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

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

    def testSingleImageWithCompanion(self, tmpdir):
        image = self.importSingleImageWithCompanion()
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % image.id.val, str(tmpfile)]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testMIF(self, tmpdir):
        images = self.importMIF(2)
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % images[0].id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes = f.read()
        assert not bytes

    def testImageNoFileset(self, tmpdir):
        pixels = self.pix()
        tmpfile = tmpdir.join('test')
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    @py.test.mark.parametrize('repeat', xrange(0, 10))
    def testImageMultipleGroups(self, repeat, tmpdir):
        user, group1, group2 = self.setup_user_and_two_groups()
        client = self.new_client(user=user)
        filename = self.OmeroPy / ".." / ".." / ".." / \
            "components" / "common" / "test" / "tinyTest.d3d.dv"
        with open(filename) as f:
            bytes1 = f.read()
        pix_ids = self.import_image(filename)
        pixels = self.query.get("Pixels", long(pix_ids[0]))
        tmpfile = tmpdir.join('test')
        self.set_context(client, group2.id.val)
        self.args += ["Image:%s" % pixels.getImage().id.val, str(tmpfile)]
        self.cli.invoke(self.args, strict=True)
        with open(str(tmpfile)) as f:
            bytes2 = f.read()
        assert bytes1 == bytes2
