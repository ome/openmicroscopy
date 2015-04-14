#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful ThumbnailStore service.

   Copyright 2011-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import library as lib
import pytest

from omero import MissingPyramidException
from omero.sys import ParametersI
from omero.util.concurrency import get_event
from omero.rtypes import rint, unwrap


class TestThumbs(lib.ITest):

    def assertTb(self, buf, x=64, y=64):
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
        tb.setPixelsId(long(pix))
        tb.resetDefaults()
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
                rint(64), [long(pix1), long(pix2)])
        finally:
            tb.close()

    def testThumbnailExists(self):
        tb = self.pyr_tb()
        try:
            assert not tb.thumbnailExists(rint(64), rint(64))
        finally:
            tb.close()

    @pytest.mark.parametrize("meth", ("one", "set"))
    def testThumbnailVersion(self, meth):
        i64 = rint(64)
        pix = self.missing_pyramid()
        q = ("select tb from Thumbnail tb "
             "where tb.pixels.id = %s "
             "order by tb.id desc ")
        q = q % pix
        p = ParametersI().page(0, 1)
        get = lambda: self.query.findByQuery(q, p)

        # Before anything has been called, there
        # should be no thumbnail
        assert not get()

        # At this stage, there should still be no
        # thumbnail
        tb = self.client.sf.createThumbnailStore()
        tb.setPixelsId(long(pix))
        tb.resetDefaults()
        assert not tb.thumbnailExists(i64, i64)
        assert tb.isInProgress()

        # As soon as it's requested, it should have a -1
        # version to mark pyramid creation as ongoing.
        before = tb.getThumbnail(i64, i64)
        assert get().version.val == -1
        assert not tb.thumbnailExists(i64, i64)
        assert tb.isInProgress()

        # Now we wait until the pyramid has been created
        # and test that a proper version has been set.
        event = get_event("test_thumbs")
        secs = 20
        rps = self.client.sf.createRawPixelsStore()
        for x in range(secs):
            try:
                rps.setPixelsId(long(pix), True)
                event = None
                break
            except MissingPyramidException:
                event.wait(1)
        if event:
            assert "Pyramid was not generated %ss" % secs

        # Re-load the thumbnail store now that
        # the pyramid is generated.
        tb.close()
        tb = self.client.sf.createThumbnailStore()
        if not tb.setPixelsId(long(pix)):
            tb.resetDefaults()
            assert tb.setPixelsId(long(pix))

        assert meth in ("one", "set")
        if meth == "one":
            after = tb.getThumbnail(i64, i64)
            assert before != after
            assert tb.thumbnailExists(i64, i64)
            assert not tb.isInProgress()
        elif meth == "set":
            tb.getThumbnailSet(i64, i64, [long(pix)])
        assert get().version.val >= 0


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
        copy.append([long(pix1), long(pix2)])
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
make_test_single(
    "getThumbnailForSectionDirect", 64, 64, 0, 0, rint(64), rint(64))
make_test_single("getThumbnail", 64, 64, rint(64), rint(60))
make_test_single("getThumbnailByLongestSide", 60, 60, rint(60))
make_test_single("getThumbnailDirect", 64, 64, rint(64), rint(60))
make_test_single(
    "getThumbnailForSectionDirect", 64, 64, 0, 0, rint(64), rint(60))
make_test_set("getThumbnailSet", 64, 64, rint(64), rint(64))
make_test_set("getThumbnailByLongestSideSet", 64, 64, rint(64))
