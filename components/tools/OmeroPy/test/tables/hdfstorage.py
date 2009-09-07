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

class TestHdfFile(unittest.TestCase):

    def testValidDirectory(self):
        self.assertRaises(exceptions.Exception, omero.tables.HdfStorage, None)
        self.assertRaises(exceptions.Exception, omero.tables.HdfStorage, '')

    def testDirectoryLocking(self):
        tmpdir = tempfile.mkdtemp()
        hdf1 = omero.tables.HdfStorage(tmpdir)
        try:
            hdf2 = omero.tables.HdfStorage(tmpdir)
            self.fail("This should throw")
        except omero.tables.StorageLockedException:
            pass
        hdf1.close()
        hdf3 = omero.tables.HdfStorage(tmpdir)
        hdf3.close()

    def testSimpleCreation(self):
        hdf = omero.tables.HdfStorage(tempfile.mkdtemp())
        hdf.create(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col])
        hdf.close()

    def testCreationWithMetadata(self):
        hdf = omero.tables.HdfStorage(tempfile.mkdtemp())
        hdf.create(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        hdf.close()

    def testAddSingleRow(self):
        hdf = omero.tables.HdfStorage(tempfile.mkdtemp())
        hdf.create(["a","b","c"],["first","second","third"],[tables.Int64Col, tables.Int64Col, tables.Int64Col],{"analysisA":1,"analysisB":"param","analysisC":4.1})
        hdf.append({"a":1,"b":2,"c":3})
        hdf.close()

    def testLsidUsage(self):
        self.fail("NYI")

    def testAddColumn(self):
        self.fail("NYI")

    def testMergeFiles(self):
        self.fail("NYI")

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
