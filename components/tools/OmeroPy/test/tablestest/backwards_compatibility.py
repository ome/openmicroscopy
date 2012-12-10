#!/usr/bin/env python

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
import omero
import omero.clients
import omero.grid
from integration import library as lib


class BackwardsCompatibilityTest(lib.ITest):

    def setUp(self):
        super(BackwardsCompatibilityTest, self).setUp()

        dir = os.path.dirname(os.path.realpath(__file__))
        file = os.path.join(dir, "service-reference-dev_4_4_5.h5")
        self.ofile = self.client.upload(file)
        print self.ofile.getId().val


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


    def testAllColumns_4_4_5(self):
        grid = self.client.sf.sharedResources()
        table = grid.openTable(self.ofile)
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

        # In hindsight it would've be better to put mask before the new columns
        # after mask

        #testla = data.columns[9].values
        #self.assertEquals([-2, -1], testla[0])
        #self.assertEquals([1, 2], testla[1])
        #testda = data.columns[10].values
        #self.assertEquals([-0.25, -0.5], testda[0])
        #self.assertEquals([0.125, 0.0625], testda[1])

        testm = data.columns[11 - 2]
        self.checkMaskCol(testm)

        # Now try an update
        updatel = omero.grid.LongColumn('longcol', '', [12345])
        updateData = omero.grid.Data(rowNumbers = [1], columns = [updatel])
        table.update(updateData)

        self.assertEquals(table.getNumberOfRows(), 2)
        data2 = table.readCoordinates([0,1])

        for n in [0, 1, 2, 3, 4, 5, 6, 8]:
            self.assertEquals(data.columns[n].values, data2.columns[n].values)
        self.checkMaskCol(data2.columns[11 - 2])

        testl2 = data2.columns[7].values
        self.assertEquals(-1, testl2[0])
        self.assertEquals(12345, testl2[1])


if __name__ == '__main__':
    unittest.main()


