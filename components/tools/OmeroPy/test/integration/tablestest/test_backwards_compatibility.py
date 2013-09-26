#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


"""
Check backwards compatibility of the tables service
"""


import unittest
import os.path
import bz2
import tempfile
import omero
import omero.clients
import omero.columns
import omero.grid
from test.integration import library as lib


class BackwardsCompatibilityTest(lib.ITest):

    #def setUp(self):
    #    super(BackwardsCompatibilityTest, self).setUp()


    def uploadHdf5(self, file):
        """
        Decompress the BZ2-compressed HDF5 test file and upload to server.
        file should be relative to the directory containing this file.
        """
        dir = os.path.dirname(os.path.realpath(__file__))
        file = os.path.join(dir, file)

        tmpf = tempfile.NamedTemporaryFile(delete=False)
        bzf = bz2.BZ2File(file)
        tmpf.write(bzf.read())
        bzf.close()
        tmpf.close()

        ofile = self.client.upload(
            tmpf.name, name=file, type='application/x-hdf')
        print "Uploaded OriginalFile:", ofile.getId().val
        return ofile


    def createMaskCol(self):
        mask = omero.columns.MaskColumnI('mask', 'desc', None)
        mask.imageId = [1, 2]
        mask.theZ = [3, 4]
        mask.theT = [5, 6]
        mask.x = [7.0, 8.0]
        mask.y = [9.0, 10.0]
        mask.w = [11.0, 12.0]
        mask.h = [13.0, 14.0]
        mask.bytes = [[15],[16,17,18,19,20]]
        return mask


    def checkMaskCol(self, test):
        def arr(x):
            import numpy
            import tables
            return numpy.fromstring(x, count=len(x), dtype=tables.UInt8Atom())

        self.assertEquals(1, test.imageId[0])
        self.assertEquals(3, test.theZ[0])
        self.assertEquals(5, test.theT[0])
        self.assertEquals(7, test.x[0])
        self.assertEquals(9, test.y[0])
        self.assertEquals(11, test.w[0])
        self.assertEquals(13, test.h[0])
        self.assertEquals([15], arr(test.bytes[0]))

        self.assertEquals(2, test.imageId[1])
        self.assertEquals(4, test.theZ[1])
        self.assertEquals(6, test.theT[1])
        self.assertEquals(8, test.x[1])
        self.assertEquals(10, test.y[1])
        self.assertEquals(12, test.w[1])
        self.assertEquals(14, test.h[1])

        x = [16,17,18,19,20]
        y = arr(test.bytes[1])
        for i in range(len(x)):
            self.assertEquals(x[i], y[i])


    def createAllColumns_4_4_5(self):
        """
        Call this method to create the reference HDF5 table under a 4.4.5
        or older server. The OriginalFile ID of the table will be printed,
        and can be used to find the file under ${omero.data.dir}/Files/.

        E.g. from the command goto ``components/tools/OmeroPy/test``, and run:
        ``python -m unittest tablestest.backwards_compatibility.BackwardsCompatibilityTest.createAllColumns_4_4_5``
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )

        fcol = omero.columns.FileColumnI('filecol', 'file col')
        fcol.values = [10, 20]
        icol = omero.columns.ImageColumnI('imagecol', 'image col')
        icol.values = [30, 40]
        rcol = omero.columns.RoiColumnI('roicol', 'roi col')
        rcol.values = [50, 60]
        wcol = omero.columns.WellColumnI('wellcol', 'well col')
        wcol.values = [70, 80]
        pcol = omero.columns.PlateColumnI('platecol', 'plate col')
        pcol.values = [90, 100]

        bcol = omero.columns.BoolColumnI('boolcol', 'bool col')
        bcol.values = [True, False]
        dcol = omero.columns.DoubleColumnI('doublecol', 'double col')
        dcol.values = [0.25, 0.5]
        lcol = omero.columns.LongColumnI('longcol', 'long col')
        lcol.values = [-1, -2]

        scol = omero.columns.StringColumnI('stringcol', 'string col', 3)
        scol.values = ["abc", "de"]

        #larr = omero.columns.LongArrayColumnI('longarr', 'longarr col', 2)
        #larr.values = [[-2, -1], [1, 2]]
        #farr = omero.columns.FloatArrayColumnI('floatarr', 'floatarr col', 2)
        #farr.values = [[-0.25, -0.5], [0.125, 0.0625]]
        #darr = omero.columns.DoubleArrayColumnI('doublearr', 'doublearr col', 2)
        #darr.values = [[-0.25, -0.5], [0.125, 0.0625]]

        mask = self.createMaskCol()

        cols = [fcol, icol, rcol, wcol, pcol,
                bcol, dcol, lcol, scol, mask]
                #larr, farr, darr]

        table.initialize(cols)
        table.addData(cols)
        data = table.readCoordinates([0,1])

        testf = data.columns[0].values
        self.assertEquals(10, testf[0])
        self.assertEquals(20, testf[1])
        testi = data.columns[1].values
        self.assertEquals(30, testi[0])
        self.assertEquals(40, testi[1])
        testr = data.columns[2].values
        self.assertEquals(50, testr[0])
        self.assertEquals(60, testr[1])
        testw = data.columns[3].values
        self.assertEquals(70, testw[0])
        self.assertEquals(80, testw[1])
        testp = data.columns[4].values
        self.assertEquals(90, testp[0])
        self.assertEquals(100, testp[1])

        testb = data.columns[5].values
        self.assertEquals(True, testb[0])
        self.assertEquals(False, testb[1])
        testd = data.columns[6].values
        self.assertEquals(0.25, testd[0])
        self.assertEquals(0.5, testd[1])
        testl = data.columns[7].values
        self.assertEquals(-1, testl[0])
        self.assertEquals(-2, testl[1])

        tests = data.columns[8].values
        self.assertEquals("abc", tests[0])
        self.assertEquals("de", tests[1])

        testm = data.columns[9]
        self.checkMaskCol(testm)

        #testla = data.columns[10].values
        #self.assertEquals([-2, -1], testla[0])
        #self.assertEquals([1, 2], testla[1])
        #testda = data.columns[11].values
        #self.assertEquals([-0.25, -0.5], testda[0])
        #self.assertEquals([0.125, 0.0625], testda[1])

        ofile = table.getOriginalFile()
        print "Created OriginalFile:", ofile.getId().val


    def testAllColumns_4_4_5(self):
        """
        Check whether a table created under 4.4.5 or older is still usable
        with a newer server
        """
        ofile = self.uploadHdf5("service-reference-dev_4_4_5.h5.bz2")

        grid = self.client.sf.sharedResources()
        table = grid.openTable(ofile)
        self.assert_(table)

        expectedTypes = [
            omero.grid.FileColumn,
            omero.grid.ImageColumn,
            omero.grid.RoiColumn,
            omero.grid.WellColumn,
            omero.grid.PlateColumn,
            omero.grid.BoolColumn,
            omero.grid.DoubleColumn,
            omero.grid.LongColumn,
            omero.grid.StringColumn,
            omero.grid.MaskColumn
            ]
        #omero.grid.FloatArrayColumn,
        #omero.grid.DoubleArrayColumn,
        #omero.grid.LongArrayColumn,

        expectedNames = [
            'filecol',
            'imagecol',
            'roicol',
            'wellcol',
            'platecol',
            'boolcol',
            'doublecol',
            'longcol',
            'stringcol',
            'mask'
            ]
        #'longarr'
        #'floatarr'
        #'doublearr'

        headers = table.getHeaders()
        self.assertEquals([type(x) for x in headers], expectedTypes)
        self.assertEquals([x.name for x in headers], expectedNames)

        self.assertEquals(table.getNumberOfRows(), 2)

        data = table.readCoordinates([0,1])

        testf = data.columns[0].values
        self.assertEquals(10, testf[0])
        self.assertEquals(20, testf[1])
        testi = data.columns[1].values
        self.assertEquals(30, testi[0])
        self.assertEquals(40, testi[1])
        testr = data.columns[2].values
        self.assertEquals(50, testr[0])
        self.assertEquals(60, testr[1])
        testw = data.columns[3].values
        self.assertEquals(70, testw[0])
        self.assertEquals(80, testw[1])
        testp = data.columns[4].values
        self.assertEquals(90, testp[0])
        self.assertEquals(100, testp[1])

        testb = data.columns[5].values
        self.assertEquals(True, testb[0])
        self.assertEquals(False, testb[1])
        testd = data.columns[6].values
        self.assertEquals(0.25, testd[0])
        self.assertEquals(0.5, testd[1])
        testl = data.columns[7].values
        self.assertEquals(-1, testl[0])
        self.assertEquals(-2, testl[1])

        tests = data.columns[8].values
        self.assertEquals("abc", tests[0])
        self.assertEquals("de", tests[1])

        testm = data.columns[9]
        self.checkMaskCol(testm)

        #testla = data.columns[10].values
        #self.assertEquals([-2, -1], testla[0])
        #self.assertEquals([1, 2], testla[1])
        #testda = data.columns[11].values
        #self.assertEquals([-0.25, -0.5], testda[0])
        #self.assertEquals([0.125, 0.0625], testda[1])


        # Now try an update
        updatel = omero.grid.LongColumn('longcol', '', [12345])
        updateData = omero.grid.Data(rowNumbers = [1], columns = [updatel])
        table.update(updateData)

        self.assertEquals(table.getNumberOfRows(), 2)
        data2 = table.readCoordinates([0,1])

        for n in [0, 1, 2, 3, 4, 5, 6, 8]:
            self.assertEquals(data.columns[n].values, data2.columns[n].values)
        self.checkMaskCol(data2.columns[9])

        testl2 = data2.columns[7].values
        self.assertEquals(-1, testl2[0])
        self.assertEquals(12345, testl2[1])


if __name__ == '__main__':
    unittest.main()


