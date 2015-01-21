#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2012-2014 University of Dundee & Open Microscopy Environment.
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

import os.path
import bz2
import tempfile
import omero
import omero.clients
import omero.grid
import library as lib
import pytest

from omero import columns
from omero.rtypes import rint


class TestBackwardsCompatibility(lib.ITest):

    # def setUp(self):
    #     super(BackwardsCompatibilityTest, self).setUp()

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

    def testCreateAllColumns_4_4_5(self):
        """
        Call this method to create the reference HDF5 table under a 4.4.5
        or older server. The OriginalFile ID of the table will be printed,
        and can be used to find the file under ${omero.data.dir}/Files/.

        To run manually goto ``components/tools/OmeroPy``, and run:
        ``py.test test/integration/tablestest/test_backwards_compatibility.py\
        -s -k testCreateAllColumns_4_4_5``
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        assert table

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

        # larr = columns.LongArrayColumnI('longarr', 'longarr col', 2)
        # larr.values = [[-2, -1], [1, 2]]
        # farr = columns.FloatArrayColumnI('floatarr', 'floatarr col', 2)
        # farr.values = [[-0.25, -0.5], [0.125, 0.0625]]
        # darr = columns.DoubleArrayColumnI('doublearr', 'doublearr col', 2)
        # darr.values = [[-0.25, -0.5], [0.125, 0.0625]]

        mask = self.createMaskCol()

        cols = [fcol, icol, rcol, wcol, pcol,
                bcol, dcol, lcol, scol, mask]
        # larr, farr, darr]

        table.initialize(cols)
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

        # testla = data.columns[10].values
        # assert [-2, -1] == testla[0]
        # assert [1, 2] == testla[1]
        # testda = data.columns[11].values
        # assert [-0.25, -0.5] == testda[0]
        # assert [0.125, 0.0625] == testda[1]

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
        assert table

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
        # omero.grid.FloatArrayColumn,
        # omero.grid.DoubleArrayColumn,
        # omero.grid.LongArrayColumn,

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
        # 'longarr'
        # 'floatarr'
        # 'doublearr'

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

        # testla = data.columns[10].values
        # assert [-2, -1] == testla[0]
        # assert [1, 2] == testla[1]
        # testda = data.columns[11].values
        # assert [-0.25, -0.5] == testda[0]
        # assert [0.125, 0.0625] == testda[1]

        # Now try an update
        updatel = omero.grid.LongColumn('longcol', '', [12345])
        updateData = omero.grid.Data(rowNumbers=[1], columns=[updatel])
        table.update(updateData)

        assert table.getNumberOfRows() == 2
        data2 = table.readCoordinates([0, 1])

        for n in [0, 1, 2, 3, 4, 5, 6, 8]:
            assert data.columns[n].values == data2.columns[n].values
        self.checkMaskCol(data2.columns[9])

        testl2 = data2.columns[7].values
        assert -1 == testl2[0]
        assert 12345 == testl2[1]

    def testMetadataException(self):
        """
        Check whether metadata set methods are blocked on a v1 (pre-5.1) table
        """
        ofile = self.uploadHdf5("service-reference-dev_4_4_5.h5.bz2")

        grid = self.client.sf.sharedResources()
        table = grid.openTable(ofile)

        expected = 'Tables metadata is only supported for OMERO.tables >= 2'
        with pytest.raises(omero.ApiUsageException) as exc:
            table.setMetadata('a', rint(1))
        with pytest.raises(omero.ApiUsageException) as exc:
            table.setAllMetadata({'a': rint(1)})
        assert exc.value.message == expected
