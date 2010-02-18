#!/usr/bin/env python

"""
   Test of the Tables service

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest, os
import omero, omero.tables

from test.integration import library as lib


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

def test_suite():
    return 1

if __name__ == '__main__':
    unittest.main()
