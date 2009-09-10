#!/usr/bin/env python

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, tempfile, exceptions, time
import omero.tables
import portalocker
import logging
import tables

from tablestest.library import TestCase
from path import path


logging.basicConfig(level=logging.CRITICAL)


class TestHdfStorage(TestCase):

    def cols(self):
        a = omero.columns.LongColumnI('a','first',None)
        b = omero.columns.LongColumnI('b','first',None)
        c = omero.columns.LongColumnI('c','first',None)
        return [a,b,c]

    def init(self, hdf, meta=False):
        if meta:
            m = {"analysisA":1,"analysisB":"param","analysisC":4.1}
        else:
            m = None
        hdf.initialize(self.cols(), m)

    def append(self, hdf, map):
        cols = self.cols()
        for col in cols:
            try:
                col.values = [map[col.name]]
            except KeyError:
                col.values = []
        hdf.append(cols)

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
        self.append(hdf, {"a":1,"b":2,"c":3})
        hdf.cleanup()

    def testSorting(self): # Probably shouldn't work
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a":0,"b":2,"c":3})
        self.append(hdf, {"a":4,"b":4,"c":4})
        self.append(hdf, {"a":0,"b":1,"c":0})
        self.append(hdf, {"a":0,"b":0,"c":0})
        self.append(hdf, {"a":0,"b":4,"c":0})
        self.append(hdf, {"a":0,"b":0,"c":0})
        rows = hdf.getWhereList(time.time(), '(a==0)', None, 'b', None, None, None)
        # Doesn't work yet.
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
