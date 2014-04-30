#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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

"""
   Tests for the stateful ThumbnailStore service.

"""

import pytest
import test.integration.library as lib

from omero.rtypes import rint, unwrap


@pytest.mark.long_running
class TestThumbs(lib.ITest):

    def assertTb(self, buf, x=64, y=64):
            try:
                from PIL import Image, ImageDraw  # see ticket:2597
            except ImportError:
                try:
                    # Marking as noqa for now as these unused imports
                    # check for Pillow.
                    import Image  # noqa
                    import ImageDraw  # noqa # see ticket:2597
                except ImportError:
                    print "Pillow not installed"
            thumb = self.open_jpeg_buffer(buf)
            assert unwrap(x) == thumb.size[0]
            assert unwrap(y) == thumb.size[1]

    #
    # MissingPyramid tests
    #

    def pyr_tb(self):
        """
        Here we create a pixels that is not big,
        then modify its metadata so that it IS big,
        in order to trick the service into throwing
        us a MissingPyramidException.
        """
        pix = self.missing_pyramid()
        tb = self.client.sf.createThumbnailStore()
        tb.setPixelsId(pix.id.val)
        tb.resetDefaults()  # Sets new missingPyramid flag
        return tb

    def testCreateThumbnails(self):
        tb = self.pyr_tb()
        try:
            tb.createThumbnails()
        finally:
            tb.close()

    def testCreateThumbnails64x64(self):
        tb = self.pyr_tb()
        try:
            tb.createThumbnail(rint(64), rint(64))
        finally:
            tb.close()

    def testCreateThumbnailsByLongestSideSet64x64(self):
        pix1 = self.missing_pyramid()
        pix2 = self.missing_pyramid()
        tb = self.client.sf.createThumbnailStore()
        try:
            tb.createThumbnailsByLongestSideSet(
                rint(64), [pix1.id.val, pix2.id.val])
        finally:
            tb.close()

    def testThumbnailExists(self):
        tb = self.pyr_tb()
        try:
            assert not tb.thumbnailExists(rint(64), rint(64))
        finally:
            tb.close()


def assign(f, method, *args):
    name = "test%s" % method[0].upper()
    name += method[1:]
    for i in args:
        try:
            for i2 in i:
                name += "x%s" % unwrap(i2)
        except:
            name += "x%s" % unwrap(i)
    f.func_name = name
    setattr(TestThumbs, name, f)


def make_test_single(method, x, y, *args):
    def f(self):
        tb = self.pyr_tb()
        try:
            buf = getattr(tb, method)(*args)
            self.assertTb(buf, x, y)
        finally:
            tb.close()
    assign(f, method, args)


def make_test_set(method, x, y, *args):
    def f(self):
        pix1 = self.missing_pyramid()
        pix2 = self.missing_pyramid()
        tb = self.client.sf.createThumbnailStore()
        copy = list(args)
        copy.append([pix1.id.val, pix2.id.val])
        copy = tuple(copy)
        try:
            buf_map = getattr(tb, method)(*copy)
            for id, buf in buf_map.items():
                self.assertTb(buf, x, y)
        finally:
            tb.close()
    assign(f, method, args)


make_test_single("getThumbnail", 64, 64, rint(64), rint(64))
make_test_single("getThumbnailByLongestSide", 64, 64, rint(64))
make_test_single("getThumbnailDirect", 64, 64, rint(64), rint(64))
make_test_single("getThumbnailForSectionDirect",
                 64, 64, 0, 0, rint(64), rint(64))
make_test_single("getThumbnail", 64, 64, rint(64), rint(60))
make_test_single("getThumbnailByLongestSide", 60, 60, rint(60))
make_test_single("getThumbnailDirect", 64, 64, rint(64), rint(60))
make_test_single("getThumbnailForSectionDirect",
                 64, 64, 0, 0, rint(64), rint(60))
make_test_set("getThumbnailSet", 64, 64, rint(64), rint(64))
make_test_set("getThumbnailByLongestSideSet", 64, 64, rint(64))
