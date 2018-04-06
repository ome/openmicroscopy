#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
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


from test.integration.clitest.cli import CLITest
from omero.cli import NonZeroReturnCode
from omero.model import PixelsI
from omero.rtypes import rint
from omero.rtypes import unwrap
from omero.sys import ParametersI

import os
import omero.plugins.admin
import pytest
import datetime
import hashlib


class TestRemovePyramids(CLITest):

    def setup_method(self, method):
        super(TestRemovePyramids, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "removepyramids"]

    def test_removepyramids_admin_only(self, capsys):
        """Test removepyramids is admin-only"""
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        assert err.endswith("SecurityViolation: Admins only!\n")


class TestRemovePyramidsRestrictedAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Restricted
    # Admin with no privileges
    DEFAULT_PRIVILEGES = ()

    def setup_method(self, method):
        super(TestRemovePyramidsRestrictedAdmin, self).setup_method(method)
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "removepyramids"]

    def test_removepyramids_restricted_admin(self, capsys):
        """Test removepyramids cannot be run by Restricted Admin"""
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_end = "SecurityViolation: Admin restrictions:"
        assert err.startswith(output_end)


class TestRemovePyramidsFullAdmin(CLITest):

    # make the user in this test a member of system group
    DEFAULT_SYSTEM = True
    # make the new member of system group to a Full Admin
    DEFAULT_PRIVILEGES = None
    # indicate to the test locally only
    LOCAL = False
    local_only = pytest.mark.skipif(LOCAL is False, reason="Run locally only")

    def setup_method(self, method):
        super(TestRemovePyramidsFullAdmin, self).setup_method(method)
        self.keep_root_alive()
        self.cli.register("admin", omero.plugins.admin.AdminControl, "TEST")
        self.args += ["admin", "removepyramids"]
        self.group_ctx = {'omero.group': str(self.group.id.val)}

    def calculate_sha1(self, data):
        h = hashlib.sha1()
        h.update(data)
        return h.hexdigest()

    def import_pyramid_pre_fs(self, tmpdir):
        name = "test&sizeX=4000&sizeY=4000.fake"
        fakefile = tmpdir.join(name)
        fakefile.write('')
        pixels = self.import_image(filename=str(fakefile), skip="checksum")[0]
        id = long(float(pixels))
        # wait for the pyramid to be generated
        self.wait_for_pyramid(id)
        query_service = self.client.sf.getQueryService()
        pixels_service = self.client.sf.getPixelsService()
        orig_pix = query_service.findByQuery(
            "select p from Pixels p where p.id = :id",
            ParametersI().addId(id))
        orig_fs = query_service.findByQuery(
            "select f from Image i join i.fileset f where i.id = :id",
            ParametersI().addId(orig_pix.image.id.val))

        try:
            new_img = pixels_service.copyAndResizeImage(
                orig_pix.image.id.val, rint(4000), rint(4000), rint(1),
                rint(1), [0], None, True).val
            pix_id = unwrap(query_service.projection(
                "select p.id from Image i join i.pixels p where i.id = :id",
                ParametersI().addId(new_img)))[0][0]
            # This won't work but it but we then have a pyramid without fileset
            self.copyPixels(orig_pix, PixelsI(pix_id, False))
        except omero.InternalException:
            print "Cannot copy pixels for image %s" % orig_pix.image.id.val
        finally:
            self.delete([orig_fs])
        return pix_id

    def copyPixels(self, orig_pix, new_pix):
        orig_source = self.client.sf.createRawPixelsStore()
        new_sink = self.client.sf.createRawPixelsStore()

        try:
            orig_source.setPixelsId(orig_pix.id.val, False)
            new_sink.setPixelsId(new_pix.id.val, False)
            buf = orig_source.getPlane(0, 0, 0)
            new_sink.setPlane(buf, 0, 0, 0)
        finally:
            orig_source.close()
            new_sink.close()

    def test_remove_pyramids_little_endian(self, tmpdir, capsys):
        """Test removepyramids with litlle endian true"""
        img_id = self.import_pyramid(tmpdir)
        self.args += ["--endian=little"]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "Pyramid removed for image %s" % img_id
        assert output_start in out

    def test_remove_pyramids_imported_after_future(self, tmpdir, capsys):
        """Test removepyramids with date in future"""
        self.import_pyramid(tmpdir)
        date = datetime.datetime.now() + datetime.timedelta(days=1)
        value = date.strftime('%Y-%m-%d')
        self.args += ["--endian=little"]
        self.args += ["--imported-after", value]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "No pyramids to remove"
        assert output_start in out

    def test_remove_pyramids_limit(self, tmpdir, capsys):
        """Test removepyramids with date in future"""
        self.import_pyramid(tmpdir, skip=None)
        self.args += ["--endian=little"]
        self.args += ["--limit", "1"]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "No more than 1 pyramids will be removed"
        assert output_start in out

    def test_remove_pyramids_not_valid_limit(self, tmpdir, capsys):
        """Test removepyramids with date in future"""
        self.import_pyramid(tmpdir, skip=None)
        self.args += ["--endian=little"]
        self.args += ["--limit", "0"]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "No more than 500 pyramids will be removed"
        assert output_start in out

    @local_only
    def test_remove_pyramids_manual(self, tmpdir, capsys):
        """Test removepyramids for a file manually added under /Pixels"""
        proxy, description = self.client.getManagedRepository(description=True)
        base = description.path.val
        path = base + "Pixels/0_pyramid"
        with open(path, 'a'):
            os.utime(path, None)
        self.args += ["--endian=little"]
        self.cli.invoke(self.args, strict=True)
        # remove the file
        os.remove(path)
        out, err = capsys.readouterr()
        output_start = "No pyramids to remove"
        assert output_start in out

    def test_remove__pre_fs_pyramids(self, tmpdir, capsys):
        """Test removepyramids for pre-fs data"""
        self.import_pyramid_pre_fs(tmpdir)
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "Failed to remove for image"
        reason = "pyramid-requires-fileset"
        assert output_start in out
        assert reason in out

    def test_remove_pyramids_big_endian(self, tmpdir, capsys):
        """Test removepyramids with litlle endian true"""
        name = "big&sizeX=3500&sizeY=3500&little=false.fake"
        img_id = self.import_pyramid(tmpdir, name=name, skip=None)
        self.args += ["--endian=big"]
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "Pyramid removed for image %s" % img_id
        assert output_start in out

    def test_remove_pyramids(self, tmpdir, capsys):
        """Test removepyramids with litlle endian true"""
        name = "big&sizeX=3500&sizeY=3500&little=false.fake"
        big_id = self.import_pyramid(tmpdir, name=name, skip=None)
        name = "little&sizeX=3500&sizeY=3500&little=true.fake"
        little_id = self.import_pyramid(tmpdir, name=name, skip=None)
        self.cli.invoke(self.args, strict=True)
        out, err = capsys.readouterr()
        output_start = "Pyramid removed for image %s" % big_id
        assert output_start in out
        output_start = "Pyramid removed for image %s" % little_id
        assert output_start in out

    def test_remove_pyramids_check_thumbnails(self, tmpdir, capsys):
        """Test check that the thumbnail is correctly created"""
        name = "big&sizeX=3500&sizeY=3500&little=false.fake"
        img_id = self.import_pyramid(tmpdir, name=name, skip=None)
        query_service = self.client.sf.getQueryService()
        pix = query_service.findByQuery(
            "select p from Pixels p where p.image.id = :id",
            ParametersI().addId(img_id))
        tb = self.client.sf.createThumbnailStore()
        id = pix.id.val
        thumb_hash = None
        try:
            thumbs = tb.getThumbnailByLongestSideSet(rint(64), [id],
                                                     {'omero.group': '-1'})
            assert len(thumbs) == 1
            thumb_hash = self.calculate_sha1(thumbs[id])
            # remove the pyramid and the thumbnail
            self.args += ["--endian=big"]
            self.cli.invoke(self.args, strict=True)
            out, err = capsys.readouterr()
            thumbs = tb.getThumbnailByLongestSideSet(rint(64), [id],
                                                     {'omero.group': '-1'})
            assert len(thumbs) == 1
            # The clock should be returned during the pyramid generation
            digest = self.calculate_sha1(thumbs[id])
            assert digest != thumb_hash
            # The pyramid generation has now been triggered.
            self.wait_for_pyramid(id)
            thumbs = tb.getThumbnailByLongestSideSet(rint(64), [id],
                                                     {'omero.group': '-1'})
            digest = self.calculate_sha1(thumbs[id])
            # The thumbnail should now be back
            assert digest == thumb_hash
        finally:
            tb.close()
