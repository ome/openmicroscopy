#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of client upload/download functionality

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import test.integration.library as lib

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
        self.assert_( "abc\n" == lines[0], lines[0] )
        self.assert_( "def\n" == lines[1], lines[1] )
        self.assert_( "123\n" == lines[2], lines[2] )
        sha1_upload = self.client.sha1(str(uploaded))
        sha1_download = self.client.sha1(str(downloaded))
        self.assertEquals(sha1_upload, sha1_download, "%s!=%s" % (sha1_upload, sha1_download))

    def testUploadDifferentSizeTicket2337(self):
        uploaded = tmpfile()
        ofile = self.client.upload(str(uploaded), type="text/plain")
        uploaded.write_lines(["abc", "def"]) # Shorten
        ofile = self.client.upload(str(uploaded), type="text/plain", ofile=ofile)

        downloaded = create_path()
        self.client.download(ofile, str(downloaded))
        lines = downloaded.lines()
        self.assertEquals(2, len(lines))
        self.assert_( "abc\n" == lines[0], lines[0] )
        self.assert_( "def\n" == lines[1], lines[1] )

        sha1_upload = self.client.sha1(str(uploaded))
        sha1_download = self.client.sha1(str(downloaded))
        self.assertEquals(sha1_upload, sha1_download, "%s!=%s" % (sha1_upload, sha1_download))


if __name__ == '__main__':
    unittest.main()
