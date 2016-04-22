#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
#  Copyright (C) 2016 University of Dundee. All rights reserved.
#
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

"""
Test of the yaml/json parameters file handling
"""

import library as lib
from omero.rtypes import unwrap
from omero.util import pydict_text_io

import pytest


class TestPydictTextIo(lib.ITest):

    def getTestJson(self):
        # space after : is optional for json but required for yaml
        return '{"a":2}'

    def getTestYaml(self):
        # quoted strings are optional for yaml but required for json
        return 'a: 2'

    @pytest.mark.parametrize('format', ['yaml', 'Yml'])
    def test_get_format_filename_yaml(self, tmpdir, format):
        f = tmpdir.join('test.%s' % format)
        content = self.getTestYaml()
        f.write(content)
        rawdata, filetype = pydict_text_io.get_format_filename(
            str(f), None)
        assert filetype == 'yaml'
        assert rawdata == content

    @pytest.mark.parametrize('format', ['json', 'JS'])
    def test_get_format_filename_json(self, tmpdir, format):
        f = tmpdir.join('test.%s' % format)
        content = self.getTestJson()
        f.write(content)
        rawdata, filetype = pydict_text_io.get_format_filename(
            str(f), None)
        assert filetype == 'json'
        assert rawdata == content

    # Mime-type overrides extension

    @pytest.mark.parametrize('format', [
        ('json', 'application/x-yaml'), ('yaml', 'application/x-yaml'),
        ('yaml', '')])
    def test_get_format_originalfileid_yaml(self, format):
        content = self.getTestYaml()
        fa = self.make_file_annotation(
            name='test.%s' % format[0], binary=content, format=format[1])
        fid = unwrap(fa.file.id)
        print fid, unwrap(fa.file.mimetype)
        retdata, rettype = pydict_text_io.get_format_originalfileid(
            fid, None, self.client.getSession())
        assert rettype == 'yaml'
        assert retdata == content

    @pytest.mark.parametrize('format', [
        ('json', 'application/json'), ('yaml', 'application/json'),
        ('json', '')])
    def test_get_format_originalfileid_json(self, format):
        content = self.getTestJson()
        fa = self.make_file_annotation(
            name='test.%s' % format[0], binary=content, format=format[1])
        fid = unwrap(fa.file.id)
        retdata, rettype = pydict_text_io.get_format_originalfileid(
            fid, None, self.client.getSession())
        assert rettype == 'json'
        assert retdata == content

    @pytest.mark.parametrize('remote', [True, False])
    @pytest.mark.parametrize('format', ['json', 'yaml'])
    def test_load(self, tmpdir, remote, format):
        if format == 'json':
            content = self.getTestJson()
        else:
            content = self.getTestYaml()

        if remote:
            fa = self.make_file_annotation(
                name='test.%s' % format, binary=content, format=format)
            fid = unwrap(fa.file.id)
            fileobj = 'OriginalFile:%d' % fid
        else:
            f = tmpdir.join('test.%s' % format)
            f.write(content)
            fileobj = str(f)

        data = pydict_text_io.load(
            fileobj, session=self.client.getSession())
        assert data == {'a': 2}

    @pytest.mark.parametrize('format', ['json', 'yaml'])
    def test_dump(data, tmpdir, format):
        d = {'a': 2}
        dumpstring = pydict_text_io.dump(d, format)
        f = tmpdir.join('test-dump.%s' % format)
        f.write(dumpstring)
        fileobj = str(f)

        assert pydict_text_io.load(fileobj, format) == d
