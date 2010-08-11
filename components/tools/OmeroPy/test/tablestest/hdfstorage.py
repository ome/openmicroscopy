#!/usr/bin/env python

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os, tempfile, exceptions, time
import omero.columns
import omero.tables
import portalocker
import logging
import tables
import Ice

from tablestest.library import TestCase
from path import path


logging.basicConfig(level=logging.CRITICAL)

class MockAdapter(object):
    def __init__(self, ic):
        self.ic = ic
    def getCommunicator(self):
        return self.ic

class TestHdfStorage(TestCase):

    def setUp(self):
        self.ic = Ice.initialize()
        self.current = Ice.Current()
        self.current.adapter = MockAdapter(self.ic)

        for of in omero.columns.ObjectFactories.values():
            of.register(self.ic)

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
            hdf2 = omero.tables.HdfStorage(self.hdfpath())
            self.fail("should be locked")
        except omero.LockTimeout, lt:
            pass
        hdf1.cleanup()
        hdf3 = omero.tables.HdfStorage(self.hdfpath())

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

    def testModifyRow(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a":1,"b":2,"c":3})
        self.append(hdf, {"a":5,"b":6,"c":7})
        data = hdf.readCoordinates(hdf._stamp, [0,1], self.current)
        data.columns[0].values[0] = 100
        data.columns[0].values[1] = 200
        data.columns[1].values[0] = 300
        data.columns[1].values[1] = 400
        hdf.update(hdf._stamp, data)
        data2 = hdf.readCoordinates(hdf._stamp, [0,1], self.current)
        hdf.cleanup()

    def testReadTicket1951(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a":1,"b":2,"c":3})
        data = hdf.readCoordinates(hdf._stamp, [0], self.current)
        data2 = hdf.read(hdf._stamp, [0,1,2], 0, 1, self.current)
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
        
    """
    Hard fails disabled. See #2067
    def testAddColumn(self):
        self.fail("NYI")

    def testMergeFiles(self):
        self.fail("NYI")
        
    def testVersion(self):
        self.fail("NYI")
    """
    
    def testHandlesExistingDirectory(self):
        t = path(self.tmpdir())
        h = t / "test.h5"
        self.assertTrue(t.exists())
        hdf = omero.tables.HdfStorage(h)
        hdf.cleanup()


    def testStringCol(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        cols = [omero.columns.StringColumnI("name","description",16,None)]
        hdf.initialize(cols)
        cols[0].settable(hdf._HdfStorage__mea) # Needed for size
        cols[0].values = ["foo"]
        hdf.append(cols)
        rows = hdf.getWhereList(time.time(), '(name=="foo")', None, 'b', None, None, None)
        self.assertEquals(1, len(rows))
        self.assertEquals(16, hdf.readCoordinates(time.time(), [0], self.current).columns[0].size)
        # Doesn't work yet.
        hdf.cleanup()


    #
    # ROIs
    #

    def testMaskColumn(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        mask = omero.columns.MaskColumnI('mask', 'desc', None)
        hdf.initialize([mask], None)
        mask.imageId = [1, 2]
        mask.theZ = [2, 2]
        mask.theT = [3, 3]
        mask.x = [4, 4]
        mask.y = [5, 5]
        mask.w = [6, 6]
        mask.h = [7, 7]
        mask.bytes = [[0],[0,1,2,3,4]]
        hdf.append(mask)
        data = hdf.readCoordinates(hdf._stamp, [0,1], self.current)
        test = data.columns[0]
        self.assertEquals(1, test.imageId[0])
        self.assertEquals(2, test.theZ[0])
        self.assertEquals(3, test.theT[0])
        self.assertEquals(4, test.x[0])
        self.assertEquals(5, test.y[0])
        self.assertEquals(6, test.w[0])
        self.assertEquals(7, test.h[0])
        self.assertEquals([0], test.bytes[0])

        self.assertEquals(2, test.imageId[1])
        self.assertEquals(2, test.theZ[1])
        self.assertEquals(3, test.theT[1])
        self.assertEquals(4, test.x[1])
        self.assertEquals(5, test.y[1])
        self.assertEquals(6, test.w[1])
        self.assertEquals(7, test.h[1])
        self.assertEquals([0,1,2,3,4], test.bytes[1])
        hdf.cleanup()

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
