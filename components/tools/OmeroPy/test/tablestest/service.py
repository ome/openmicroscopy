#!/usr/bin/env python

"""
   Test of the Tables service

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero, omero.tables

class TestTables(unittest.TestCase):

    def testBlankTable(self):
        grid = self.client.sf.sharedResources()
        repoMap = grid.acquireRepositories()
        repoObj = repoMap.descriptions[0]
        repoPrx = repoMap.proxies[0]
        table = self.client.sf.newTable(repoObj.id.val, "/test")
        self.assert_( table )
        cols = []
        lc = omero.tables.LongColumn('lc',None,None)
        cols.append(lc)
        table.initialize(lc)
        lc.values = [1,2,3,4]
        table.addData(lc)
        self.assertEquals([1],table.getWhereList('(lc==1)'))
        return table.getOriginalFile()

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
