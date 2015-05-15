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

import pytest
from path import path

from omero.util.text import CSVStyle, PlainStyle, TableBuilder
from omero.util.upgrade_check import UpgradeCheck
from omero.util.temp_files import manager
from omero.util import get_user_dir
from omero_version import omero_version


class MockTable(object):

    def __init__(self, names, data, csvheaders, csvrows, sqlheaders, sqlrows):
        self.names = names
        self.data = data
        self.length = len(data)
        self.csvheaders = csvheaders
        self.csvrows = csvrows
        self.sqlheaders = sqlheaders
        self.sqlrows = sqlrows
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
              ' c1 | c2 \n----+----\n', [' a  | b  ']),
    MockTable(("c1", "c2"), (("a,b", "c"),),
              ['c1,c2'], ['"a,b",c\r\n'],
              ' c1  | c2 \n-----+----\n', [' a,b | c  ']),
    MockTable(("c1", "c2"), (("'a b'", "c"),),
              ['c1,c2'], ["'a b',c\r\n"],
              ' c1    | c2 \n-------+----\n', [" 'a b' | c  "],),
    MockTable(("c1", "c2"), (("a", "b"), ("c", "d")),
              ['c1,c2'], ['a,b\r\n', 'c,d\r\n'],
              ' c1 | c2 \n----+----\n', [' a  | b  ', ' c  | d  '],),
    MockTable(("c1", "c2"), (("£ö", "b"),),
              ['c1,c2'], ['£ö,b\r\n'],
              ' c1 | c2 \n----+----\n', [' £ö | b  ']),
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
        {'OMERO_USERDIR': None, 'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': None, 'OMERO_TEMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': None, 'OMERO_TMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': 'userdir', 'OMERO_TMPDIR': None},
        {'OMERO_USERDIR': 'userdir', 'OMERO_TEMPDIR': 'tmpdir'},
        {'OMERO_USERDIR': 'userdir', 'OMERO_TMPDIR': 'tmpdir'}))
    def testTmpdirEnvironment(self, monkeypatch, tmpdir, environment):
        for var in environment.keys():
            if environment[var]:
                monkeypatch.setenv(var, tmpdir / environment.get(var))
            else:
                monkeypatch.delenv(var, raising=False)

        if environment.get('OMERO_TEMPDIR'):
            pytest.deprecated_call(manager.tmpdir)
            tdir = tmpdir / environment.get('OMERO_TEMPDIR') / "omero" / "tmp"
        elif environment.get('OMERO_TMPDIR'):
            tdir = tmpdir / environment.get('OMERO_TMPDIR')
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
