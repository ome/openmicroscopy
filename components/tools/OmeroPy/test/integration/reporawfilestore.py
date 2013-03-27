#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the BfPixelsStore API
   
   This test compares data got through BFPixelsStore using
   different methods. No file needs to be imported for these
   tests. bfpixelsstoreexternal.py tests that the methods
   return the same data as the equivalent rps methods would
   for imported data.
   
   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time, shutil, hashlib, difflib, binascii
import library as lib
import omero, omero.gateway

from path import path
from omero.rtypes import *
from integration.repository import AbstractRepoTest

class TestRepRawFileStore(AbstractRepoTest):

    def setUpRepo(self):
        sess = self.root.sf
        self.tmp_dir = path(self.unique_dir)
        self.repoPrx = self.getManagedRepo()

    def testCreate(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)

    def testWrite(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))

    def testFailedWrite(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"

        # Perform a touch
        rfs = self.repoPrx.file(repo_filename, "rw") #create empty file
        rfs.write([], 0, 0)
        rfs.close()

        rfs = self.repoPrx.file(repo_filename, "r")
        self.assertEquals(0, rfs.size())
        wbytes = "0123456789"
        try:
            rfs.write(wbytes,0,len(wbytes))
        except:
            pass
        self.assert_(rfs.size() == 0)

    def testFailedWriteNoFile(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"

        # Without a single write, no file is produced
        rfs = self.repoPrx.file(repo_filename, "rw") #create empty file
        rfs.close()

        rfs = self.repoPrx.file(repo_filename, "r")
        try:
            rfs.size()
            self.fail("File shouldn't exist!")
        except omero.ResourceError:
            pass
        wbytes = "0123456789"
        try:
            rfs.write(wbytes,0,len(wbytes))
        except:
            pass
        try:
            rfs.size()
            self.fail("File shouldn't exist!")
        except omero.ResourceError:
            pass

    def testWriteRead(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))
        rbytes = rfs.read(0,len(wbytes))
        self.assert_(wbytes == rbytes)

    def testAppend(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))
        end = rfs.size()
        rfs.write(wbytes,end,len(wbytes))
        self.assert_(rfs.size() == 2*len(wbytes))
        rbytes = rfs.read(0,2*len(wbytes))
        self.assert_(wbytes+wbytes == rbytes)

    def testTruncateToZero(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))
        self.assert_(rfs.truncate(0))
        self.assert_(rfs.size() == 0)

    def testClose(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))
        rbytes = rfs.read(0,len(wbytes))
        self.assert_(wbytes == rbytes)
        try:
            rfs.close()
        except:
            pass #FIXME: close throws an NPE but should close the filehandle...
        try:
            rbytes = rfs.read(0,len(wbytes))
        except:
            pass #FIXME: ... so an exception should be thrown here now.
        rfs = self.repoPrx.file(repo_filename, "r")
        self.assert_(rfs.size() == len(wbytes))


if __name__ == '__main__':
    unittest.main()
