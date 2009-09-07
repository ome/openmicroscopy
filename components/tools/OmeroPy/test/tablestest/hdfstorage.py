#!/usr/bin/env python

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, tempfile, exceptions
import omero.tables
import portalocker
import tables

from tablestest.library import TestCase
from path import path

class TestHdfFile(TestCase):

    def check(self, hdf, isInit, isRW):
        self.assertEquals(isInit, hdf.initialized)
        if hdf.initialized:
            self.assertTrue( hdf.ome )
            self.assertTrue( hdf.mea )

    def testValidDirectory(self):
        self.assertRaises(omero.ApiUsageException, omero.tables.HdfStorage, None)
        self.assertRaises(omero.ApiUsageException, omero.tables.HdfStorage, '')

    def testDirectoryLockingWithRO(self):
        tmpdir = self.tmpdir()
        hdf1 = omero.tables.HdfStorage(tmpdir)
        self.check(hdf1, False, True)

        hdf2 = omero.tables.HdfStorage(tmpdir, True)
        self.check(hdf2, False, False)

        hdf2.close()
        hdf1.close()

        hdf3 = omero.tables.HdfStorage(tmpdir)
        self.check(hd3, False, True)
        hdf3.close()

    def testDirectoryLockingWithoutRO(self):
        tmpdir = self.tmpdir()
        hdf1 = omero.tables.HdfStorage(tmpdir)
        self.check(hdf1, False, True)

        try:
            hdf2 = omero.tables.HdfStorage(tmpdir)
            self.fail("This should throw")
        except omero.LockTimeout, lt:
            pass
        hdf1.close()

        hdf3 = omero.tables.HdfStorage(tmpdir)
        self.check(hdf3, False, True)
        hdf3.close()

    def testSimpleCreation(self):
        hdf = omero.tables.HdfStorage(self.tmpdir())
        self.check(hdf, False, True)
        hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col])
        self.check(hdf, True, True)
        hdf.close()

    def testCreationWithMetadata(self):
        hdf = omero.tables.HdfStorage(self.tmpdir())
        self.check(hdf, False, True)
        hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        self.check(hdf, True, True)
        hdf.close()

    def testAddSingleRow(self):
        hdf = omero.tables.HdfStorage(self.tmpdir())
        self.check(hdf, False, True)
        hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        hdf.append({"a":1,"b":2,"c":3})
        self.check(hdf, True, True)
        hdf.close()

    def testInitializationOnInitializedFileFails(self):
        t = self.tmpdir()
        hdf = omero.tables.HdfStorage(t)
        self.check(hdf, False, True)
        hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        self.check(hdf, True, True)
        hdf.close()
        hdf = omero.tables.HdfStorage(t)
        self.check(hdf, True, True)
        try:
            hdf.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
            self.fail()
        except omero.ApiUsageException:
            pass
        hdf.close()

    def testInitializationOnReadyFileFailsWithInit(self):
        t = self.tmpdir()
        hdf1 = omero.tables.HdfStorage(t)
        hdf1.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        self.check(hdf1, True, True)
        hdf2 = omero.tables.HdfStorage(t)
        self.check(hdf2, True, False)
        try:
            hdf2.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
            self.fail()
        except omero.ApiUsageException:
            pass
        hdf2.close()
        hdf1.close()

    def testInitializationOnReadyFileFailsWithoutInit(self):
        t = self.tmpdir()
        hdf1 = omero.tables.HdfStorage(t)
        self.check(hdf1, False, True)
        #hdf1.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        hdf2 = omero.tables.HdfStorage(t)
        self.check(hdf2, False, False)
        try:
            hdf2.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
            self.fail()
        except omero.ApiUsageException:
            pass
        hdf2.close()
        hdf1.close()

    def testReadOnlyWaitsForFileCreation(self):
        t = self.tmpdir()
        hdf1 = omero.tables.HdfStorage(t)
        self.check(hdf1, False, True)
        hdf2 = omero.tables.HdfStorage(t)
        self.check(hdf1, False, False)
        try:
            hdf2.initialize(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
            self.fail()
        except omero.LockError:
            pass
        hdf2.close()
        hdf1.close()

    def testAddColumn(self):
        self.fail("NYI")

    def testMergeFiles(self):
        self.fail("NYI")

    def testHandlesExistingDirectory(self):
        t = path(self.tmpdir())
        t.mkdir()
        hdf = omero.tables.HdfStorage(t)
        hdf.close()

    def testVersion(self):
        self.fail()

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
