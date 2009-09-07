#!/usr/bin/env python

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, tempfile, exceptions
import omero.tables
import portalocker
import logging
import tables

from tablestest.library import TestCase
from path import path


logging.basicConfig(level=logging.CRITICAL)


class TestHdfFile(TestCase):

    def init(self, hdf, meta=False):
        if meta:
            m = {"analysisA":1,"analysisB":"param","analysisC":4.1}
        else:
            m = None
        hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],m)

    def hdfpath(self):
        tmpdir = self.tmpdir()
        return path(tmpdir) / "test.h5"

    def testInvalidFile(self):
        self.assertRaises(omero.ApiUsageException, omero.tables.HdfStorage, None)
        self.assertRaises(omero.ApiUsageException, omero.tables.HdfStorage, '')
        bad = path(self.tmpdir()) / "doesntexist" / "test.h5"
        self.assertRaises(omero.ApiUsageException, omero.tables.HdfStorage, bad)

    def testValidFile(self):
        omero.tables.HdfStorage(self.hdfpath())

    def testLocking(self):
        tmp = self.hdfpath()
        hdf1 = omero.tables.HdfStorage(tmp)
        try:
            hdf2 = omero.tables.HdfStorage(tmp)
            self.fail("should be locked")
        except omero.LockTimeout, lt:
            pass
        hdf1.cleanup()
        hdf3 = omero.tables.HdfStorage(tmp)

    def testSimpleCreation(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, False)
        hdf.cleanup()

    def testCreationWithMetadata(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        hdf.cleanup()

    def testAddSingleRow(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        hdf.append({"a":1,"b":2,"c":3})
        hdf.cleanup()

    def testInitializationOnInitializedFileFails(self):
        p = self.hdfpath()
        hdf = omero.tables.HdfStorage(p)
        self.init(hdf, True)
        hdf.cleanup()
        hdf = omero.tables.HdfStorage(p)
        try:
            self.init(hdf, True)
            self.fail()
        except omero.ApiUsageException:
            pass
        hdf.cleanup()

    def testAddColumn(self):
        self.fail("NYI")

    def testMergeFiles(self):
        self.fail("NYI")

    def testHandlesExistingDirectory(self):
        t = path(self.tmpdir())
        h = t / "test.h5"
        self.assertTrue(t.exists())
        hdf = omero.tables.HdfStorage(h)
        hdf.cleanup()

    def testVersion(self):
        self.fail()

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
