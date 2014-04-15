#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the HDF storage for the Tables API.

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import time
import pytest
import omero.columns
import omero.tables
import logging
import Ice

from library import TestCase
from path import path


logging.basicConfig(level=logging.CRITICAL)


class MockAdapter(object):
    def __init__(self, ic):
        self.ic = ic

    def getCommunicator(self):
        return self.ic


class TestHdfStorage(TestCase):

    def setup_method(self, method):
        TestCase.setup_method(self, method)
        self.ic = Ice.initialize()
        self.current = Ice.Current()
        self.current.adapter = MockAdapter(self.ic)

        for of in omero.columns.ObjectFactories.values():
            of.register(self.ic)

    def cols(self):
        a = omero.columns.LongColumnI('a', 'first', None)
        b = omero.columns.LongColumnI('b', 'first', None)
        c = omero.columns.LongColumnI('c', 'first', None)
        return [a, b, c]

    def init(self, hdf, meta=False):
        if meta:
            m = {"analysisA": 1, "analysisB": "param", "analysisC": 4.1}
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
        pytest.raises(omero.ApiUsageException, omero.tables.HdfStorage, None)
        pytest.raises(omero.ApiUsageException, omero.tables.HdfStorage, '')
        bad = path(self.tmpdir()) / "doesntexist" / "test.h5"
        pytest.raises(omero.ApiUsageException, omero.tables.HdfStorage, bad)

    def testValidFile(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        hdf.cleanup()

    def testLocking(self):
        tmp = str(self.hdfpath())
        hdf1 = omero.tables.HdfStorage(tmp)
        try:
            omero.tables.HdfStorage(tmp)
            assert False, "should be locked"
        except omero.LockTimeout:
            pass
        hdf1.cleanup()
        hdf3 = omero.tables.HdfStorage(tmp)
        hdf3.cleanup()

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
        self.append(hdf, {"a": 1, "b": 2, "c": 3})
        hdf.cleanup()

    def testModifyRow(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a": 1, "b": 2, "c": 3})
        self.append(hdf, {"a": 5, "b": 6, "c": 7})
        data = hdf.readCoordinates(hdf._stamp, [0, 1], self.current)
        data.columns[0].values[0] = 100
        data.columns[0].values[1] = 200
        data.columns[1].values[0] = 300
        data.columns[1].values[1] = 400
        hdf.update(hdf._stamp, data)
        hdf.readCoordinates(hdf._stamp, [0, 1], self.current)
        hdf.cleanup()

    def testReadTicket1951(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a": 1, "b": 2, "c": 3})
        hdf.readCoordinates(hdf._stamp, [0], self.current)
        hdf.read(hdf._stamp, [0, 1, 2], 0, 1, self.current)
        hdf.cleanup()

    def testSorting(self):  # Probably shouldn't work
        hdf = omero.tables.HdfStorage(self.hdfpath())
        self.init(hdf, True)
        self.append(hdf, {"a": 0, "b": 2, "c": 3})
        self.append(hdf, {"a": 4, "b": 4, "c": 4})
        self.append(hdf, {"a": 0, "b": 1, "c": 0})
        self.append(hdf, {"a": 0, "b": 0, "c": 0})
        self.append(hdf, {"a": 0, "b": 4, "c": 0})
        self.append(hdf, {"a": 0, "b": 0, "c": 0})
        hdf.getWhereList(time.time(), '(a==0)', None, 'b', None, None, None)
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
            assert False
        except omero.ApiUsageException:
            pass
        hdf.cleanup()

    """
    Hard fails disabled. See #2067
    def testAddColumn(self):
        assert False, "NYI"

    def testMergeFiles(self):
        assert False, "NYI"

    def testVersion(self):
        assert False, "NYI"
    """

    def testHandlesExistingDirectory(self):
        t = path(self.tmpdir())
        h = t / "test.h5"
        assert t.exists()
        hdf = omero.tables.HdfStorage(h)
        hdf.cleanup()

    def testStringCol(self):
        hdf = omero.tables.HdfStorage(self.hdfpath())
        cols = [omero.columns.StringColumnI("name", "description", 16, None)]
        hdf.initialize(cols)
        cols[0].settable(hdf._HdfStorage__mea)  # Needed for size
        cols[0].values = ["foo"]
        hdf.append(cols)
        rows = hdf.getWhereList(time.time(), '(name=="foo")', None, 'b', None,
                                None, None)
        assert 1 == len(rows)
        assert 16 == hdf.readCoordinates(time.time(), [0],
                                         self.current).columns[0].size
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
        mask.bytes = [[0], [0, 1, 2, 3, 4]]
        hdf.append([mask])
        data = hdf.readCoordinates(hdf._stamp, [0, 1], self.current)
        test = data.columns[0]
        assert 1 == test.imageId[0]
        assert 2 == test.theZ[0]
        assert 3 == test.theT[0]
        assert 4 == test.x[0]
        assert 5 == test.y[0]
        assert 6 == test.w[0]
        assert 7 == test.h[0]
        assert [0] == test.bytes[0]

        assert 2 == test.imageId[1]
        assert 2 == test.theZ[1]
        assert 3 == test.theT[1]
        assert 4 == test.x[1]
        assert 5 == test.y[1]
        assert 6 == test.w[1]
        assert 7 == test.h[1]
        assert [0 == 1, 2, 3, 4], test.bytes[1]
        hdf.cleanup()
