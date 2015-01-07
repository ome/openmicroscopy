#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2008-2014 Glencoe Software, Inc. All Rights Reserved.
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
   Test of client upload/download functionality

"""

import pytest
import library as lib

from omero.util.temp_files import create_path


def tmpfile():
    file = create_path()
    file.write_lines(["abc", "def", "123"])
    return file


class TestFiles(lib.ITest):

    def testUploadDownload(self):
        uploaded = tmpfile()
        downloaded = create_path()
        ofile = self.client.upload(str(uploaded), type="text/plain")
        self.client.download(ofile, str(downloaded))
        lines = downloaded.lines()
        assert "abc\n" == lines[0], lines[0]
        assert "def\n" == lines[1], lines[1]
        assert "123\n" == lines[2], lines[2]
        sha1_upload = self.client.sha1(str(uploaded))
        sha1_download = self.client.sha1(str(downloaded))
        assert sha1_upload == sha1_download, "%s!=%s" % (
            sha1_upload, sha1_download)

    @pytest.mark.broken(ticket="11610")
    def testUploadDifferentSizeTicket2337(self):
        uploaded = tmpfile()
        ofile = self.client.upload(str(uploaded), type="text/plain")
        uploaded.write_lines(["abc", "def"])  # Shorten
        ofile = self.client.upload(
            str(uploaded), type="text/plain", ofile=ofile)

        downloaded = create_path()
        self.client.download(ofile, str(downloaded))
        lines = downloaded.lines()
        assert 2 == len(lines)
        assert "abc\n" == lines[0], lines[0]
        assert "def\n" == lines[1], lines[1]

        sha1_upload = self.client.sha1(str(uploaded))
        sha1_download = self.client.sha1(str(downloaded))
        assert sha1_upload == sha1_download, "%s!=%s" % (
            sha1_upload, sha1_download)
