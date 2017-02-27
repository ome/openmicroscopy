#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 Glencoe Software, Inc. All Rights Reserved.
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
Test of various things under omero.util
"""

import json
import pytest
from path import path

from omero.util.text import CSVStyle, JSONStyle, PlainStyle, TableBuilder
from omero.util.upgrade_check import UpgradeCheck
from omero.util.temp_files import manager
from omero.util import get_user_dir
from omero_version import omero_version
import omero.util.image_utils as image_utils
try:
    from PIL import Image
except ImportError:
    import Image
import numpy

class MockTable(object):

    def __init__(self, names, data, csvheaders, csvrows, sqlheaders, sqlrows,
                 jsondicts):
        self.names = names
        self.data = data
        self.length = len(data)
        self.csvheaders = csvheaders
        self.csvrows = csvrows
        self.sqlheaders = sqlheaders
        self.sqlrows = sqlrows
        self.jsondicts = jsondicts
        self.columns = 6

    def get_row(self, i):
        if i is None:
            return self.names
        return self.data[i]

    def get_sql_table(self):
        sql_table = self.sqlheaders + "\n".join(self.sqlrows)
        if len(self.sqlrows) > 1:
            sql_table += "\n(%s rows)" % len(self.sqlrows)
        else:
            sql_table += "\n(%s row)" % len(self.sqlrows)
        return sql_table


tables = (
    MockTable(("c1", "c2"), (("a", "b"),),
              ['c1,c2'], ['a,b\r\n'],
              ' c1 | c2 \n----+----\n', [' a  | b  '],
              [{"c1": "a", "c2": "b"}],
              ),
    MockTable(("c1", "c2"), (("a,b", "c"),),
              ['c1,c2'], ['"a,b",c\r\n'],
              ' c1  | c2 \n-----+----\n', [' a,b | c  '],
              [{"c1": "a,b", "c2": "c"}],
              ),
    MockTable(("c1", "c2"), (("'a b'", "c"),),
              ['c1,c2'], ["'a b',c\r\n"],
              ' c1    | c2 \n-------+----\n', [" 'a b' | c  "],
              [{"c1": "'a b'", "c2": "c"}],
              ),
    MockTable(("c1", "c2"), (("a", "b"), ("c", "d")),
              ['c1,c2'], ['a,b\r\n', 'c,d\r\n'],
              ' c1 | c2 \n----+----\n', [' a  | b  ', ' c  | d  '],
              [{"c1": "a", "c2": "b"}, {"c1": "c", "c2": "d"}],
              ),
    MockTable(("c1", "c2"), (("£ö", "b"),),
              ['c1,c2'], ['£ö,b\r\n'],
              ' c1 | c2 \n----+----\n', [' £ö | b  '],
              [{"c1": u"£ö", "c2": "b"}],
              ),
    )


class TestCSVSTyle(object):

    @pytest.mark.parametrize('mock_table', tables)
    def testGetRow(self, mock_table):
        assert mock_table.get_row(None) == mock_table.names
        for i in range(mock_table.length):
            assert mock_table.get_row(i) == mock_table.data[i]

    @pytest.mark.parametrize('mock_table', tables)
    def testCSVModuleParsing(self, mock_table):
        style = CSVStyle()
        output = list(style.get_rows(mock_table))
        assert output == mock_table.csvheaders + mock_table.csvrows

    @pytest.mark.parametrize('mock_table', tables)
    def testPlainModuleParsing(self, mock_table):
        style = PlainStyle()
        output = list(style.get_rows(mock_table))
        assert output == mock_table.csvrows


class TestJSONSTyle(object):

    @pytest.mark.parametrize('mock_table', tables)
    def testPlainModuleParsing(self, mock_table):
        style = JSONStyle()
        output = ''.join(style.get_rows(mock_table))
        assert json.loads(output) == mock_table.jsondicts


class TestTableBuilder(object):

    @pytest.mark.parametrize('mock_table', tables)
    def testStr(self, mock_table):
        tb = TableBuilder(*mock_table.names)
        for row in mock_table.data:
            tb.row(*row)
        assert str(tb) == mock_table.get_sql_table()


class TestUpgradeCheck(object):

    def testNoActionOnNull(self):
        uc = UpgradeCheck("test", url=None)
        uc.run()
        assert uc.isUpgradeNeeded() is False
        assert uc.isExceptionThrown() is True

    def testNoActionOnEmpty(self):
        uc = UpgradeCheck("test", url="")
        uc.run()
        assert uc.isUpgradeNeeded() is False
        assert uc.isExceptionThrown() is False

    @pytest.mark.parametrize('port', [8000, 9998])
    def testSlowResponse(self, port):
        uc = UpgradeCheck("test", url="http://127.0.0.1:%s" % port)
        uc.run()
        assert uc.isUpgradeNeeded() is False
        assert uc.isExceptionThrown() is True

    @pytest.mark.parametrize('prefix', ['', 'XYZ'])
    def testBadIp(self, prefix):
        uc = UpgradeCheck("test", url="200.200.200.200",
                          version="%s%s" % (prefix, omero_version))
        uc.run()
        assert uc.isUpgradeNeeded() is False
        assert uc.isExceptionThrown() is True

    @pytest.mark.parametrize(
        'url', ("http://foo", "file://dev/null", "abcp", "abc://bar"))
    def testBadUrl(self, url):
        uc = UpgradeCheck("test", url=url, version="XYZ" + omero_version)
        uc.run()
        assert uc.isUpgradeNeeded() is False
        assert uc.isExceptionThrown() is True


class TestTempFileManager(object):

    @pytest.mark.parametrize('environment', (
        {'OMERO_USERDIR': None,
         'OMERO_TEMPDIR': None,
         'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': None,
         'OMERO_TEMPDIR': 'tempdir',
         'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': None,
         'OMERO_TEMPDIR': None,
         'OMERO_TMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': 'userdir',
         'OMERO_TEMPDIR': None,
         'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': 'userdir',
         'OMERO_TEMPDIR': 'tempdir',
         'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': 'userdir',
         'OMERO_TEMPDIR': None,
         'OMERO_TMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': None,
         'OMERO_TEMPDIR': 'tempdir',
         'OMERO_TMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': 'userdir',
         'OMERO_TEMPDIR': 'tempdir',
         'OMERO_TMPDIR': 'tmpdir'}))
    def testTmpdirEnvironment(self, monkeypatch, tmpdir, environment):
        for var in environment.keys():
            if environment[var]:
                monkeypatch.setenv(var, tmpdir / environment.get(var))
            else:
                monkeypatch.delenv(var, raising=False)

        if environment.get('OMERO_TEMPDIR'):
            pytest.deprecated_call(manager.tmpdir)

        if environment.get('OMERO_TMPDIR'):
            tdir = tmpdir / environment.get('OMERO_TMPDIR')
        elif environment.get('OMERO_TEMPDIR'):
            tdir = tmpdir / environment.get('OMERO_TEMPDIR') / "omero" / "tmp"
        elif environment.get('OMERO_USERDIR'):
            tdir = tmpdir / environment.get('OMERO_USERDIR') / "tmp"
        else:
            tdir = path(get_user_dir()) / "omero" / "tmp"

        assert manager.tmpdir() == str(tdir)

    def testTmpdir2805_1(self, monkeypatch, tmpdir):

        monkeypatch.setenv('OMERO_TEMPDIR', tmpdir)
        monkeypatch.delenv('OMERO_USERDIR', raising=False)
        tmpfile = tmpdir / 'omero'
        tmpfile.write('')

        assert manager.tmpdir() == path(get_user_dir()) / "omero" / "tmp"

    def testTmpdir2805_2(self, monkeypatch, tmpdir):

        monkeypatch.setenv('OMERO_TEMPDIR', tmpdir)
        monkeypatch.delenv('OMERO_USERDIR', raising=False)
        tempdir = tmpdir / 'omero'
        tempdir.mkdir()
        tmpfile = tempdir / 'tmp'
        tmpfile.write('')

        assert manager.tmpdir() == path(get_user_dir()) / "omero" / "tmp"


class TestImageUtils(object):

    @pytest.mark.parametrize(
        'size', [4, 6, 8, 10, 12, 14, 16, 18, 20])
    def test_get_front(self, size):
        font = image_utils.get_font(size)
        assert font is not None

    @pytest.mark.parametrize("color", [
        (255, 0, 0, 255, -16776961),     # Red
        (0, 255, 0, 255, 16711935),      # Green
        (0, 0, 255, 255, 65535),         # Blue
        (0, 255, 255, 255, 16777215),    # Cyan
        (255, 0, 255, 255, -16711681),   # Magenta
        (255, 255, 0, 255, -65281),      # Yellow
        (0, 0, 0, 255, 255),             # Black
        (255, 255, 255, 255, -1),        # White
        (0, 0, 0, 127, 127),             # Transparent black
        (127, 127, 127, 127, 2139062143)])  # Grey
    def test_int_to_rgba(self, color):
        v = image_utils.int_to_rgba(color[4])
        for x in range(0, 3):
            assert color[x] == v[x]

    @pytest.mark.parametrize("size", [
        (100, 100, 50, 20, 5),
        (100, 100, 20, 50, 5)])
    def test_get_zoom_factor(self, size):
        image_size = (size[0], size[1])
        r = image_utils.get_zoom_factor(image_size, size[2], size[3])
        assert r == size[4]

    @pytest.mark.parametrize("size", [
        (512, 512),
        (256, 256)])
    def test_resize_image(self, size):
        w, h = 512, 512
        data = numpy.zeros((h, w, 3), dtype=numpy.uint8)
        data[256, 256] = [255, 0, 0]
        img = Image.fromarray(data, 'RGB')
        result = image_utils.resize_image(img, size[0], size[1])
        assert result is not None
        width, height = result.size
        assert width == size[0]
        assert height == size[1]

    def test_paste_image(self):
        data = numpy.zeros((512, 512, 3), dtype=numpy.uint8)
        data[256, 256] = [255, 0, 0]
        img = Image.fromarray(data, 'RGB')

        data_canvas = numpy.zeros((512, 512, 3), dtype=numpy.uint8)
        data_canvas[256, 256] = [255, 255, 0]
        canvas = Image.fromarray(data_canvas, 'RGB')
        image_utils.paste_image(img, canvas, 0, 0)
