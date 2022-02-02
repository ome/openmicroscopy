#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2021-2022 Glencoe Software, Inc.
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
Creates a table and tests it's integrity
"""

import omero
import omero.clients
import omero.grid
from omero.testlib import ITest

from omero import columns
from omero.rtypes import (
    RFloatI,
    RLongI,
    RStringI,
    rfloat,
    rint,
    rlong,
    rstring,
)


class TableIntegrityBase(ITest):

    def createMaskCol(self):
        mask = columns.MaskColumnI('mask', 'desc', None)
        mask.imageId = [1, 2]
        mask.theZ = [3, 4]
        mask.theT = [5, 6]
        mask.x = [7.0, 8.0]
        mask.y = [9.0, 10.0]
        mask.w = [11.0, 12.0]
        mask.h = [13.0, 14.0]
        mask.bytes = [[15], [16, 17, 18, 19, 20]]
        return mask

    def checkMaskCol(self, test):
        def arr(x):
            import numpy
            import tables
            return numpy.fromstring(x, count=len(x), dtype=tables.UInt8Atom())

        assert 1 == test.imageId[0]
        assert 3 == test.theZ[0]
        assert 5 == test.theT[0]
        assert 7 == test.x[0]
        assert 9 == test.y[0]
        assert 11 == test.w[0]
        assert 13 == test.h[0]
        assert [15] == arr(test.bytes[0])

        assert 2 == test.imageId[1]
        assert 4 == test.theZ[1]
        assert 6 == test.theT[1]
        assert 8 == test.x[1]
        assert 10 == test.y[1]
        assert 12 == test.w[1]
        assert 14 == test.h[1]

        x = [16, 17, 18, 19, 20]
        y = arr(test.bytes[1])
        for i in range(len(x)):
            assert x[i] == y[i]


class TestTableIntegrity(TableIntegrityBase):

    def _testCreateAllColumnsAndMetadata(self):
        """
        Call this method to create the reference HDF5 table.
        The OriginalFile ID of the table will be printed,
        and can be used to find the file under ${omero.data.dir}/Files/.
        Alternatively download it using
        ``omero download OriginalFile:FileID output.h5``

        To run manually goto ``components/tools/OmeroPy``, and run:
        ``pytest test/integration/tablestest/test_tables.py\
        -s -k _testCreateAllColumnsAndMetadata``
        """

        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        assert table

        # Supported metadata types
        # https://github.com/ome/omero-py/blob/v5.5.1/src/omero/hdfstorageV2.py#L466
        metadata = {
            'string': rstring('a'),
            'int': rint(1),
            'long': rlong(1),
            'double': rfloat(0.1),
        }

        fcol = columns.FileColumnI('filecol', 'file col')
        fcol.values = [10, 20]
        icol = columns.ImageColumnI('imagecol', 'image col')
        icol.values = [30, 40]
        rcol = columns.RoiColumnI('roicol', 'roi col')
        rcol.values = [50, 60]
        wcol = columns.WellColumnI('wellcol', 'well col')
        wcol.values = [70, 80]
        pcol = columns.PlateColumnI('platecol', 'plate col')
        pcol.values = [90, 100]

        bcol = columns.BoolColumnI('boolcol', 'bool col')
        bcol.values = [True, False]
        dcol = columns.DoubleColumnI('doublecol', 'double col')
        dcol.values = [0.25, 0.5]
        lcol = columns.LongColumnI('longcol', 'long col')
        lcol.values = [-1, -2]

        scol = columns.StringColumnI('stringcol', 'string col', 3)
        scol.values = ["abc", "de"]

        larr = columns.LongArrayColumnI('longarr', 'longarr col', 2)
        larr.values = [[-2, -1], [1, 2]]
        farr = columns.FloatArrayColumnI('floatarr', 'floatarr col', 2)
        farr.values = [[-0.25, -0.5], [0.125, 0.0625]]
        darr = columns.DoubleArrayColumnI('doublearr', 'doublearr col', 2)
        darr.values = [[-0.25, -0.5], [0.125, 0.0625]]

        dscol = columns.DatasetColumnI('datasetcol', 'dataset col')
        dscol.values = [110, 120]

        mask = self.createMaskCol()

        cols = [fcol, icol, rcol, wcol, pcol,
                bcol, dcol, lcol, scol, mask,
                larr, farr, darr, dscol]

        table.initialize(cols)
        table.setAllMetadata(metadata)

        table.addData(cols)
        data = table.readCoordinates([0, 1])

        testf = data.columns[0].values
        assert 10 == testf[0]
        assert 20 == testf[1]
        testi = data.columns[1].values
        assert 30 == testi[0]
        assert 40 == testi[1]
        testr = data.columns[2].values
        assert 50 == testr[0]
        assert 60 == testr[1]
        testw = data.columns[3].values
        assert 70 == testw[0]
        assert 80 == testw[1]
        testp = data.columns[4].values
        assert 90 == testp[0]
        assert 100 == testp[1]

        testb = data.columns[5].values
        assert testb[0]
        assert not testb[1]
        testd = data.columns[6].values
        assert 0.25 == testd[0]
        assert 0.5 == testd[1]
        testl = data.columns[7].values
        assert -1 == testl[0]
        assert -2 == testl[1]

        tests = data.columns[8].values
        assert "abc" == tests[0]
        assert "de" == tests[1]

        testm = data.columns[9]
        self.checkMaskCol(testm)

        testla = data.columns[10].values
        assert [-2, -1] == testla[0]
        assert [1, 2] == testla[1]
        testfa = data.columns[11].values
        assert [-0.25, -0.5] == testfa[0]
        assert [0.125, 0.0625] == testfa[1]
        testda = data.columns[12].values
        assert [-0.25, -0.5] == testda[0]
        assert [0.125, 0.0625] == testda[1]

        testds = data.columns[13].values
        assert 110 == testds[0]
        assert 120 == testds[1]

        ofile = table.getOriginalFile()
        print("Created OriginalFile:", ofile.getId().val)

        return table

    def testAllColumnsAndMetadata(self):
        """
        Check the integrity of a created table created.
        """

        table = self._testCreateAllColumnsAndMetadata()
        assert table

        metadata = table.getAllMetadata()
        assert metadata['__version'] == rstring('2')
        assert isinstance(metadata['double'], RFloatI)
        assert abs(metadata['double'].val - 0.1) < 1e-8
        assert isinstance(metadata['int'], RLongI)
        assert metadata['int'].val == 1
        assert isinstance(metadata['long'], RLongI)
        assert metadata['long'].val == 1
        assert isinstance(metadata['string'], RStringI)
        assert metadata['string'].val == 'a'

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
            omero.grid.MaskColumn,
            omero.grid.LongArrayColumn,
            omero.grid.FloatArrayColumn,
            omero.grid.DoubleArrayColumn,
            omero.grid.DatasetColumn
        ]

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
            'mask',
            'longarr',
            'floatarr',
            'doublearr',
            'datasetcol',
        ]

        headers = table.getHeaders()
        assert [type(x) for x in headers] == expectedTypes
        assert [x.name for x in headers] == expectedNames

        assert table.getNumberOfRows() == 2

        data = table.readCoordinates([0, 1])

        testf = data.columns[0].values
        assert 10 == testf[0]
        assert 20 == testf[1]
        testi = data.columns[1].values
        assert 30 == testi[0]
        assert 40 == testi[1]
        testr = data.columns[2].values
        assert 50 == testr[0]
        assert 60 == testr[1]
        testw = data.columns[3].values
        assert 70 == testw[0]
        assert 80 == testw[1]
        testp = data.columns[4].values
        assert 90 == testp[0]
        assert 100 == testp[1]

        testb = data.columns[5].values
        assert testb[0]
        assert not testb[1]
        testd = data.columns[6].values
        assert 0.25 == testd[0]
        assert 0.5 == testd[1]
        testl = data.columns[7].values
        assert -1 == testl[0]
        assert -2 == testl[1]

        tests = data.columns[8].values
        assert "abc" == tests[0]
        assert "de" == tests[1]

        testm = data.columns[9]
        self.checkMaskCol(testm)

        testla = data.columns[10].values
        assert [-2, -1] == testla[0]
        assert [1, 2] == testla[1]
        testfa = data.columns[11].values
        assert [-0.25, -0.5] == testfa[0]
        assert [0.125, 0.0625] == testfa[1]
        testda = data.columns[12].values
        assert [-0.25, -0.5] == testda[0]
        assert [0.125, 0.0625] == testda[1]

        testds = data.columns[13].values
        assert 110 == testds[0]
        assert 120 == testds[1]

        # Now try an update
        updatel = omero.grid.LongColumn('longcol', '', [12345])
        updateData = omero.grid.Data(rowNumbers=[1], columns=[updatel])
        table.update(updateData)

        assert table.getNumberOfRows() == 2
        data2 = table.readCoordinates([0, 1])

        for n in [0, 1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13]:
            assert data.columns[n].values == data2.columns[n].values
        self.checkMaskCol(data2.columns[9])

        testl2 = data2.columns[7].values
        assert -1 == testl2[0]
        assert 12345 == testl2[1]

        table.close()
