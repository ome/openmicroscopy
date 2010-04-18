#!/usr/bin/env python

"""
   Test of client upload/download functionality

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import tempfile
import unittest
import omero
import integration.library as lib

def tmpfile():
    file = tempfile.NamedTemporaryFile()
    file.write("abc\n")
    file.write("def\n")
    file.write("123\n")
    file.flush()
    return file

class TestFiles(lib.ITest):

    def testUploadDownload(self):
        uploaded = tmpfile()
        downloaded = tempfile.mkstemp()[1]
        ofile = self.client.upload(uploaded.name, type="text/plain")
        self.client.download(ofile, downloaded)
        file = open(downloaded,"r")
        lines = file.readlines()
        self.assert_( "abc\n" == lines[0], lines[0] )
        self.assert_( "def\n" == lines[1], lines[1] )
        self.assert_( "123\n" == lines[2], lines[2] )
        sha1_upload = self.client.sha1(uploaded.name)
        sha1_download = self.client.sha1(downloaded)
        self.assert_( sha1_upload == sha1_download )

if __name__ == '__main__':
    unittest.main()
