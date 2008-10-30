#!/usr/bin/env python

"""
   Simple unit test which makes various calls on the code
   generated model.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
from omero.rtypes import *
from omero_sys_ParametersI import ParametersI

class TestParameters(unittest.TestCase):

    #
    # Parameters.theFilter
    #

    def testFilter(self):
        p = ParametersI()
        p.noPage()
        self.assertEquals(None, p.theFilter)
        p.page(2,3)
        self.assertEquals( rint(2), p.theFilter.offset )
        self.assertEquals( rint(3), p.theFilter.limit )
        p.noPage()
        self.assertEquals(None, p.theFilter)

    #
    # Parameters.map
    #

    def testAddBasicString(self):
        p = ParametersI()
        p.add("string", rstring("a"))
        self.assertEquals( rstring("a"), p.map["string"])

    def testAddBasicInt(self):
        p = ParametersI()
        p.add("int", rint(1))
        self.assertEquals(rint(1), p.map["int"])

    def testAddIdRaw(self):
        p = ParametersI()
        p.addId(1)
        self.assertEquals(rlong(1), p.map["id"])

    def testAddIdRType(self):
        p = ParametersI()
        p.addId(rlong(1))
        self.assertEquals(rlong(1), p.map["id"])

    def testAddLongRaw(self):
        p = ParametersI()
        p.addLong("long",1)
        self.assertEquals(rlong(1), p.map["long"])

    def testAddLongRType(self):
        p = ParametersI()
        p.addLong("long", rlong(1))
        self.assertEquals(rlong(1), p.map["long"])

    def testAddIds(self):
        p = ParametersI()
        p.addIds([1,2])
        p.map["ids"].val.index(rlong(1))
        p.map["ids"].val.index(rlong(2))

    def testAddLongs(self):
        p = ParametersI()
        p.addLongs("longs", [1,2])
        p.map["longs"].val.index(rlong(1))
        p.map["longs"].val.index(rlong(2))

if __name__ == '__main__':
    unittest.main()
