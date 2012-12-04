#!/usr/bin/env python

"""
   Test of the Tables service

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import path
import unittest, os
import omero, omero.tables

from omero.rtypes import *
from integration import library as lib


class TestTables(lib.ITest):

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
        mask = omero.columns.MaskColumnI('mask', 'desc', None)
        mask.imageId = [1, 2]
        mask.theZ = [2, 2]
        mask.theT = [3, 3]
        mask.x = [4.0, 4.0]
        mask.y = [5.0, 5.0]
        mask.w = [6.0, 6.0]
        mask.h = [7.0, 7.0]
        mask.bytes = [[0],[0,1,2,3,4]]

        table.initialize([mask])
        table.addData([mask])
        data = table.readCoordinates([0,1])

        def arr(x):
            import numpy
            import tables
            return numpy.fromstring(x, count=len(x), dtype=tables.UInt8Atom())

        test = data.columns[0]
        self.assertEquals(1, test.imageId[0])
        self.assertEquals(2, test.theZ[0])
        self.assertEquals(3, test.theT[0])
        self.assertEquals(4, test.x[0])
        self.assertEquals(5, test.y[0])
        self.assertEquals(6, test.w[0])
        self.assertEquals(7, test.h[0])
        self.assertEquals([0], arr(test.bytes[0]))

        self.assertEquals(2, test.imageId[1])
        self.assertEquals(2, test.theZ[1])
        self.assertEquals(3, test.theT[1])
        self.assertEquals(4, test.x[1])
        self.assertEquals(5, test.y[1])
        self.assertEquals(6, test.w[1])
        self.assertEquals(7, test.h[1])
        x = [0,1,2,3,4]
        y = arr(test.bytes[1])
        for i in range(len(x)):
            self.assertEquals(x[i], y[i])


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
        self.assertEquals(self.client.sha1(p), file.sha1.val)

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

    def testArrayColumn(self):
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

    def testMultipleArrayColumns(self):
        grid = self.client.sf.sharedResources()
        repoMap = grid.repositories()
        repoObj = repoMap.descriptions[0]
        table = grid.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        larr = omero.columns.LongArrayColumnI('longarr', 'desc', 3)
        larr.values = [[-1, -2, -3], [4, 5, 6]]
        darr = omero.columns.DoubleArrayColumnI('doublearr', 'desc', 2)
        darr.values = [[0.5, 0.25], [-0.125, -0.0625]]

        table.initialize([larr, darr])
        table.addData([larr, darr])
        data = table.readCoordinates([0,1])

        testl = data.columns[0].values
        self.assertEquals([-1, -2, -3], testl[0])
        self.assertEquals([4, 5, 6], testl[1])

        testd = data.columns[1].values
        self.assertEquals([0.5, 0.25], testd[0])
        self.assertEquals([-0.125, -0.0625], testd[1])

    # TODO: Add tests for error conditions


def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
