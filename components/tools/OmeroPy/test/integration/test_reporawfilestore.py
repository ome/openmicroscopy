#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the BfPixelsStore API

   This test compares data got through BFPixelsStore using
   different methods. No file needs to be imported for these
   tests. bfpixelsstoreexternal.py tests that the methods
   return the same data as the equivalent rps methods would
   for imported data.

   Copyright 2011-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest
import omero
import omero.gateway

from path import path
from test.integration.test_repository import AbstractRepoTest


class TestRepoRawFileStore(AbstractRepoTest):

    def setup_method(self, method):
        super(TestRepoRawFileStore, self).setup_method(method)
        tmp_dir = path(self.unique_dir)
        self.repoPrx = self.getManagedRepo()
        self.repo_filename = tmp_dir / self.uuid() + ".txt"

    def testCreate(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0

    def testWrite(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0
        wbytes = "0123456789"
        rfs.write(wbytes, 0, len(wbytes))
        assert rfs.size() == len(wbytes)

    def testFailedWrite(self):
        # Perform a touch
        rfs = self.repoPrx.file(self.repo_filename, "rw")  # create empty file
        rfs.write([], 0, 0)
        rfs.close()

        rfs = self.repoPrx.file(self.repo_filename, "r")
        assert rfs.size() == 0
        wbytes = "0123456789"
        try:
            rfs.write(wbytes, 0, len(wbytes))
        except:
            pass
        assert rfs.size() == 0

    @pytest.mark.broken(ticket="11610")
    def testFailedWriteNoFile(self):
        # Without a single write, no file is produced
        rfs = self.repoPrx.file(self.repo_filename, "rw")  # create empty file
        rfs.close()

        rfs = self.repoPrx.file(self.repo_filename, "r")
        with pytest.raises(omero.ResourceError):
            rfs.size()
        wbytes = "0123456789"
        try:
            rfs.write(wbytes, 0, len(wbytes))
        except:
            pass
        with pytest.raises(omero.ResourceError):
            rfs.size()

    def testWriteRead(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0
        wbytes = "0123456789"
        rfs.write(wbytes, 0, len(wbytes))
        assert rfs.size() == len(wbytes)
        rbytes = rfs.read(0, len(wbytes))
        assert wbytes == rbytes

    def testAppend(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0
        wbytes = "0123456789"
        rfs.write(wbytes, 0, len(wbytes))
        assert rfs.size() == len(wbytes)
        end = rfs.size()
        rfs.write(wbytes, end, len(wbytes))
        assert rfs.size() == 2 * len(wbytes)
        rbytes = rfs.read(0, 2 * len(wbytes))
        assert wbytes + wbytes == rbytes

    def testTruncateToZero(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0
        wbytes = "0123456789"
        rfs.write(wbytes, 0, len(wbytes))
        assert rfs.size() == len(wbytes)
        assert rfs.truncate(0)
        assert rfs.size() == 0

    def testClose(self):
        rfs = self.repoPrx.file(self.repo_filename, "rw")
        assert rfs.size() == 0
        wbytes = "0123456789"
        rfs.write(wbytes, 0, len(wbytes))
        assert rfs.size() == len(wbytes)
        rbytes = rfs.read(0, len(wbytes))
        assert wbytes == rbytes
        try:
            rfs.close()
        except:
            # FIXME: close throws an NPE but should close the filehandle...
            pass
        try:
            rbytes = rfs.read(0, len(wbytes))
        except:
            pass  # FIXME: ... so an exception should be thrown here now.
        rfs = self.repoPrx.file(self.repo_filename, "r")
        assert rfs.size() == len(wbytes)

    # ticket:11154
    def testImportLogFilenameSetting(self):
        q = self.root.sf.getQueryService()

        with pytest.raises(omero.SecurityViolation):
            q.projection("select e.id from Experimenter e where e.id = 0",
                         None, {"omero.logfilename": "/tmp/foo.log"})
