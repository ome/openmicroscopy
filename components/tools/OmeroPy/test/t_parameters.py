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

    def assertNull(self, arg):
        self.assertEquals(None, arg)

    def assertNotNull(self, arg):
        self.assertTrue( arg != None )

    #
    # From PojoOptionsTest
    #

    def testBasics(self):
        p = ParametersI()
        p.exp(rlong(1))
        p.grp(rlong(1))
        p.endTime(rtime(1))

    def testDefaults(self):
        p = ParametersI()
        # Removed: self.assertFalse(p.isLeaves())
        self.assertFalse(p.isGroup())
        self.assertFalse(p.isExperimenter())
        self.assertFalse(p.isEndTime())
        self.assertFalse(p.isStartTime())
        self.assertFalse(p.isPagination())
        self.assertFalse(p.getUnique())

    def testExperimenter(self):
        p = ParametersI()
        p.exp(rlong(1))
        self.assertTrue(p.isExperimenter())
        self.assertEquals(p.getExperimenter().getValue(), 1L)
        p.allExps()
        self.assertFalse(p.isExperimenter())

    def testGroup(self):
        p = ParametersI()
        p.grp(rlong(1))
        self.assert_(p.isGroup())
        self.assertEquals(p.getGroup().getValue(), 1L)
        p.allGrps()
        self.assertFalse(p.isGroup())

    #
    # Parameters.theFilter.limit, offset
    #

    def testFilter(self):
        p = ParametersI()
        p.noPage()
        self.assertEquals(None, p.theFilter)
        p.page(2,3)
        self.assert_(p.isPagination())
        self.assertEquals( rint(2), p.theFilter.offset )
        self.assertEquals( rint(3), p.theFilter.limit )
        p.noPage()
        self.assertFalse(p.isPagination())
        self.assertEquals(None, p.theFilter.offset)
        self.assertEquals(None, p.theFilter.limit)
        self.assertEquals(None, p.getLimit())
        self.assertEquals(None, p.getOffset())

    def testUnique(self):
        p = ParametersI()
        self.assertNull(p.getUnique())
        self.assertEquals(rbool(True), p.unique().getUnique())
        self.assertEquals(rbool(False), p.noUnique().getUnique())
        self.assertNotNull(p.getUnique())

    #
    # Parameters.theFilter.ownerId, groupId
    #

    def testOwnerId(self):
        p = ParametersI()
        self.assertNull(p.theFilter)
        p.exp(rlong(1))
        self.assertNotNull(p.theFilter)
        self.assertNotNull(p.theFilter.ownerId)
        self.assertEquals(rlong(1), p.getExperimenter())
        self.assertNull(p.allExps().getExperimenter())
        self.assertNotNull(p.theFilter)

    def testGroupId(self):
        p = ParametersI()
        self.assertNull(p.theFilter)
        p.grp(rlong(1))
        self.assertNotNull(p.theFilter)
        self.assertNotNull(p.theFilter.groupId)
        self.assertEquals(rlong(1), p.getGroup())
        self.assertNull(p.allGrps().getGroup())
        self.assertNotNull(p.theFilter)

    #
    # Parameters.theFilter.startTime, endTime
    #

    def testTimes(self):
        p = ParametersI()
        self.assertNull(p.theFilter)
        p.startTime(rtime(0))
        self.assertNotNull(p.theFilter)
        self.assertNotNull(p.theFilter.startTime)
        p.endTime(rtime(1))
        self.assertNotNull(p.theFilter.endTime)
        p.allTimes()
        self.assertNotNull(p.theFilter)
        self.assertNull(p.theFilter.startTime)
        self.assertNull(p.theFilter.endTime)

    #
    # Parameters.theOptions
    #

    def testOptionsAcquisitionData(self):
        p = ParametersI()
        self.assertNull(p.getAcquisitionData())
        self.assertEquals(rbool(True), p.acquisitionData().getAcquisitionData())
        self.assertEquals(rbool(False), p.noAcquisitionData().getAcquisitionData())
        self.assertNotNull(p.getAcquisitionData())

    def testOptionsOrphan(self):
        p = ParametersI()
        self.assertNull(p.getOrphan())
        self.assertEquals(rbool(True), p.orphan().getOrphan())
        self.assertEquals(rbool(False), p.noOrphan().getOrphan())
        self.assertNotNull(p.getOrphan())

    def testOptionsLeaves(self):
        p = ParametersI()
        self.assertNull(p.getLeaves())
        self.assertEquals(rbool(True), p.leaves().getLeaves())
        self.assertEquals(rbool(False), p.noLeaves().getLeaves())
        self.assertNotNull(p.getLeaves())


    #
    # Parameters.map
    #

    def testDistinctMaps(self):
        p1 = ParametersI()
        p2 = ParametersI()
        self.assertFalse(p1.map is p2.map)
        
    def testSameMap(self):
        m = {'key':0}
        p1 = ParametersI(parammap=m)
        p2 = ParametersI(parammap=m)
        self.assertTrue(p1.map is p2.map)
        
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
