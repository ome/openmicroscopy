#!/usr/bin/env python

"""
   Test of the Tables service

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero, omero.tables

class TestTables(unittest.TestCase):

    def testUnownedTable(self):
        ofile = omero.model.OriginalFileI()
        table = self.root.sf.acquireTable(ofile, 60)
        self.assert_(table)
        ofile = table.getOriginalFile()
        self.client.sf.acquireTable(ofile, 60)

    def testAddingColumns(self):
        ofile = omero.model.OriginalFileI()
        table = self.client.sf.acquireTable(ofile, 60)
        self.assert_(table)
        col = omero.tables.LongColumn()
        col.name = "a"
        table.addColumn(col)

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
