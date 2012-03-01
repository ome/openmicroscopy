#!/usr/bin/env python

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
from omero.rtypes import *
from path import path

class TestRepRawFileStore(lib.ITest):
    
    def setUpRepo(self):
        sess = self.root.sf
        dataDir = path(sess.getConfigService().getConfigValue("omero.data.dir"));
        self.tmp_dir = dataDir / "Repository" / self.uuid()
        path.makedirs(self.tmp_dir)

        repoMap = sess.sharedResources().repositories()
        for r in range(len(repoMap.descriptions)):
            if repoMap.descriptions[r].name.val == dataDir.parent.name:
                repoIndex = r
        self.repoPrx = repoMap.proxies[repoIndex]

    def testCreate(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

    def testWrite(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        rfs.write(wbytes,0,len(wbytes))
        self.assert_(rfs.size() == len(wbytes))
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

    def testFailedWrite(self):
        self.setUpRepo()
        repo_filename = self.tmp_dir / self.uuid() + ".txt"
        rfs = self.repoPrx.file(repo_filename, "rw") #create empty file
        rfs = self.repoPrx.file(repo_filename, "r")
        self.assert_(rfs.size() == 0)
        wbytes = "0123456789"
        try:
            rfs.write(wbytes,0,len(wbytes))
        except:
            pass
        self.assert_(rfs.size() == 0)
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

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
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

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
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

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
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)

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
        path.remove(repo_filename)
        path.rmdir(self.tmp_dir)


if __name__ == '__main__':
    unittest.main()
