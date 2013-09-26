#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the Tables service

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import path
import unittest, os
import omero, omero.tables

from omero.rtypes import *
from test.integration import library as lib


class TestTables(lib.ITest):

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

    def testBlankTable(self):
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        repoPrx = repoMap.proxies[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        cols = []
        lc = omero.grid.LongColumn('lc',None,None)
        cols.append(lc)
        table.initialize(cols)
        lc.values = [1,2,3,4]
        table.addData(cols)
        self.assertEquals([0],table.getWhereList('(lc==1)',None,0,0,0))
        return table.getOriginalFile()

    def testUpdate(self):
        ofile = self.testBlankTable()
        grid = self.client.sf.sharedResources()
        table = grid.openTable(ofile)
        data = table.slice([0],[0])
        prev = data.columns[0].values[0]
        data.columns[0].values[0] = 100
        table.update(data)
        data = table.slice([0],[0])
        next = data.columns[0].values[0]
        self.assert_( prev != next )
        self.assert_( next == 100 )

    def testTicket2175(self):
        self.assert_(self.client.sf.sharedResources().areTablesEnabled())

    def testMask(self):
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        repoPrx = repoMap.proxies[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        mask = self.createMaskCol()

        table.initialize([mask])
        table.addData([mask])
        data = table.readCoordinates([0,1])

        self.checkMaskCol(data.columns[0])

    def test2098(self):
        """
        Creates and downloads an HDF file and checks
        that its size and sha1 match whats in the db
        """
        grid = self.client.sf.sharedResources()
        table = grid.newTable(1, "/test")
        self.assert_( table )

        lc = omero.columns.LongColumnI('lc', 'desc', [1])

        file = None
        try:
            file = table.getOriginalFile()
            self.assert_( file )
            table.initialize([lc])
            table.addData([lc])
        finally:
            table.close()

        # Reload the file
        file = self.client.sf.getQueryService().get("OriginalFile", file.id.val)

        # Check values
        p = path.path(self.tmpfile())
        self.client.download(file, str(p))
        self.assertEquals(p.size, file.size.val)
        self.assertEquals(self.client.sha1(p), file.hash.val)

    def test2855MetadataMethods(self):
        """
        Tests the various metadata methods for a table
        """
        grid = self.client.sf.sharedResources()
        table = grid.newTable(1, "/test")
        self.assert_( table )

        def clean(m):
            """
            Unwraps the RTypes for easier processing
            and gets rid of auto-generated values for
            easier testing.
            """
            m = unwrap(m)
            del m["initialized"]
            del m["version"]
            return m

        try:
            print table.getOriginalFile().id.val
            lc = omero.columns.LongColumnI('lc', 'desc', [1])
            table.initialize([lc])
            self.assertEquals({}, clean(table.getAllMetadata()) )

            # Set a string
            table.setMetadata("s", rstring("b"))
            self.assertEquals("b", unwrap(table.getMetadata("s")))
            self.assertEquals({"s": "b"}, clean(table.getAllMetadata()))

            # Set an int
            table.setMetadata("i", rint(1))
            self.assertEquals(1, unwrap(table.getMetadata("i")))
            self.assertEquals({"s": "b", "i":1}, clean(table.getAllMetadata()))

            # Set a float
            table.setMetadata("f", rfloat(1))
            self.assertEquals(1, unwrap(table.getMetadata("f")))
            self.assertEquals({"s": "b", "i":1, "f": 1}, clean(table.getAllMetadata()))

        finally:
            table.close()

    def test2910(self):
        group = self.new_group(perms="rwr---")
        user1 = self.new_client(group)
        user2 = self.new_client(group)

        # As the first user, create a file
        table = user1.sf.sharedResources().newTable(1, "test2910.h5")
        self.assert_( table )
        lc = omero.grid.LongColumn("lc", None, None)
        file = table.getOriginalFile()
        table.initialize([lc])
        table.close()

        # As the second user, try to modify it
        table = user2.sf.sharedResources().openTable( file )
        self.assert_( table )
        lc.values = [1]

        self.assertRaises(omero.SecurityViolation, table.initialize, None)
        self.assertRaises(omero.SecurityViolation, table.addColumn, None)
        self.assertRaises(omero.SecurityViolation, table.addData, [lc])
        self.assertRaises(omero.SecurityViolation, table.update, None)
        self.assertRaises(omero.SecurityViolation, table.delete)
        self.assertRaises(omero.SecurityViolation, table.setMetadata, "key", wrap(1))
        self.assertRaises(omero.SecurityViolation, table.setAllMetadata, {})

    def testDelete(self):
        group = self.new_group(perms="rwr---")
        user1 = self.new_client(group)

        table = user1.sf.sharedResources().newTable(1, "testDelete.h5")
        self.assert_( table )
        lc = omero.grid.LongColumn("lc", None, None)
        file = table.getOriginalFile()
        table.initialize([lc])
        table.delete()
        table.close()

    def test3714GetWhereListVars(self):
        """
        Tests that variables are correctly unwrapped
        after transport
        """
        grid = self.client.sf.sharedResources()
        table = grid.newTable(1, "/test")
        self.assert_( table )

        lc = omero.columns.LongColumnI('lc', 'desc', [1])
        table.initialize([lc])
        table.addData([lc])
        self.assertEquals([0],table.getWhereList('(lc==var)',{"var":rlong(1)},0,0,0))

    def test4000TableRead(self):
        """
        Tests that empty or zero (ice default) values for stop
        are translated appropriately.
        """
        grid = self.client.sf.sharedResources()
        table = grid.newTable(1, "/test")
        self.assert_( table )

        lc = omero.columns.LongColumnI('lc', 'desc', [123])
        table.initialize([lc])
        table.addData([lc])
        self.assertEquals([123], table.read([0], 0, 0).columns[0].values)

    def testCallContext(self):
        """
        When calling openTable with omero.group set, there have been some
        issues.
        """

        # Create a user in one group and a table.
        group1 = self.new_group()
        gid1 = str(group1.id.val)
        client = self.new_client(group1)
        admin = client.sf.getAdminService()
        sr = client.sf.sharedResources()
        table = sr.newTable(1, "/test")
        self.assert_( table )
        ofile = table.getOriginalFile()

        # Add the user to another group
        # and try to load the table
        group2 = self.new_group(experimenters=[client])
        gid2 = str(group2.id.val)
        admin.getEventContext() # Refresh
        client.sf.setSecurityContext(group2)

        # Use -1 for all group access
        sr.openTable(ofile, {"omero.group":"-1"})

        # Load the group explicitly
        sr.openTable(ofile, {"omero.group":gid1})

    def test10049openTableUnreadable(self):
        """
        Fail nicely when openTable is passed an OriginalFile that isn't
        readable by the current user
        """
        # Create a user in one group and a table.
        group1 = self.new_group()
        client1 = self.new_client(group1)
        sr1 = client1.sf.sharedResources()
        table = sr1.newTable(1, "/test")
        self.assert_( table )
        ofile = table.getOriginalFile()

        # Create a second user and try to open the table
        group2 = self.new_group()
        client2 = self.new_client(group2)
        sr2 = client2.sf.sharedResources()

        self.assertRaises(omero.SecurityViolation, sr2.openTable, ofile)

    def test9971checkStringLength(self):
        """
        Throw an error when an attempt is made to insert a string column
        wider than the StringColumn size
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        scol = omero.columns.StringColumnI('stringcol', 'string col', 3)
        table.initialize([scol])

        # 3 characters should work, 4 should cause an error
        scol.values = ['abc']
        table.addData([scol])
        data = table.readCoordinates(range(table.getNumberOfRows()))
        self.assertEquals(['abc'], data.columns[0].values)
        scol.values = ['abcd']
        self.assertRaises(omero.ValidationException, table.addData, [scol])


    def testArrayColumn(self):
        """
        A table containing only an array column
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        larr = omero.columns.LongArrayColumnI('longarr', 'desc', 2)
        larr.values = [[-2, -1], [1, 2]]

        table.initialize([larr])
        table.addData([larr])
        data = table.readCoordinates([0,1])

        testl = data.columns[0].values
        self.assertEquals([-2, -1], testl[0])
        self.assertEquals([1, 2], testl[1])

    def testArrayColumnSize1(self):
        """
        Size one arrays require special handling
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        darr = omero.columns.DoubleArrayColumnI('longarr', 'desc', 1)
        darr.values = [[0.5], [0.25]]

        table.initialize([darr])
        table.addData([darr])
        data = table.readCoordinates([0,1])

        testl = data.columns[0].values
        self.assertEquals([0.5], testl[0])
        self.assertEquals([0.25], testl[1])

    def testAllColumnsSameTable(self):
        """
        Check all column types can coexist in the same table
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

        mask = self.createMaskCol()

        larr = omero.columns.LongArrayColumnI('longarr', 'longarr col', 2)
        larr.values = [[-2, -1], [1, 2]]
        farr = omero.columns.FloatArrayColumnI('floatarr', 'floatarr col', 2)
        farr.values = [[-8.0, -4.0], [16.0, 32.0]]
        darr = omero.columns.DoubleArrayColumnI('doublearr', 'doublearr col', 2)
        darr.values = [[-0.25, -0.5], [0.125, 0.0625]]

        cols = [fcol, icol, rcol, wcol, pcol,
                bcol, dcol, lcol, scol, mask,
                larr, farr, darr]

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

        testla = data.columns[10].values
        self.assertEquals([-2, -1], testla[0])
        self.assertEquals([1, 2], testla[1])
        testfa = data.columns[11].values
        self.assertEquals([-8.0, -4.0], testfa[0])
        self.assertEquals([16.0, 32.0], testfa[1])
        testda = data.columns[12].values
        self.assertEquals([-0.25, -0.5], testda[0])
        self.assertEquals([0.125, 0.0625], testda[1])

        ofile = table.getOriginalFile()
        print "testAllColumnsSameTable", "OriginalFile:", ofile.getId().val

        # Now try an update
        updatel = omero.grid.LongColumn('longcol', '', [12345])
        updatela = omero.grid.LongArrayColumn('longarr', '', 2, [[654, 321]])
        updateData = omero.grid.Data(
            rowNumbers = [1], columns = [updatel, updatela])
        table.update(updateData)

        self.assertEquals(table.getNumberOfRows(), 2)
        data2 = table.readCoordinates([0,1])

        for n in [0, 1, 2, 3, 4, 5, 6, 8, 11, 12]:
            self.assertEquals(data.columns[n].values, data2.columns[n].values)
        self.checkMaskCol(data2.columns[9])

        testl2 = data2.columns[7].values
        self.assertEquals(-1, testl2[0])
        self.assertEquals(12345, testl2[1])
        testla2 = data2.columns[10].values
        self.assertEquals([-2, -1], testla2[0])
        self.assertEquals([654, 321], testla2[1])


    def test10431uninitialisedTableReadWrite(self):
        """
        Return an error when attempting to read/write an uninitialised table
        """
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        lcol = omero.columns.LongColumnI('longcol', 'long col')

        self.assertRaises(omero.ApiUsageException, table.addData, [lcol])
        self.assertRaises(omero.ApiUsageException, table.read, [0], 0, 0)
        self.assertRaises(omero.ApiUsageException, table.slice, [], [])
        self.assertRaises(omero.ApiUsageException, table.getWhereList,
                          '', None, 0, 0, 0)


    # TODO: Add tests for error conditions


def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
