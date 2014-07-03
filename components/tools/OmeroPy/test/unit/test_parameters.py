#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple unit test which makes various calls on the code
   generated model.

   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.rtypes import rlong, rint, rbool, rstring, rtime
from omero_sys_ParametersI import ParametersI


class TestParameters(object):

    def assertNull(self, arg):
        assert None == arg

    def assertNotNull(self, arg):
        assert arg is not None

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
        # Removed: assert not p.isLeaves()
        assert not p.isGroup()
        assert not p.isExperimenter()
        assert not p.isEndTime()
        assert not p.isStartTime()
        assert not p.isPagination()
        assert not p.getUnique()

    def testExperimenter(self):
        p = ParametersI()
        p.exp(rlong(1))
        assert p.isExperimenter()
        assert p.getExperimenter().getValue() == 1L
        p.allExps()
        assert not p.isExperimenter()

    def testGroup(self):
        p = ParametersI()
        p.grp(rlong(1))
        assert p.isGroup()
        assert p.getGroup().getValue() == 1L
        p.allGrps()
        assert not p.isGroup()

    #
    # Parameters.theFilter.limit, offset
    #

    def testFilter(self):
        p = ParametersI()
        p.noPage()
        assert None == p.theFilter
        p.page(2, 3)
        assert p.isPagination()
        assert rint(2) == p.theFilter.offset
        assert rint(3) == p.theFilter.limit
        p.noPage()
        assert not p.isPagination()
        assert None == p.theFilter.offset
        assert None == p.theFilter.limit
        assert None == p.getLimit()
        assert None == p.getOffset()

    def testUnique(self):
        p = ParametersI()
        self.assertNull(p.getUnique())
        assert rbool(True) == p.unique().getUnique()
        assert rbool(False) == p.noUnique().getUnique()
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
        assert rlong(1) == p.getExperimenter()
        self.assertNull(p.allExps().getExperimenter())
        self.assertNotNull(p.theFilter)

    def testGroupId(self):
        p = ParametersI()
        self.assertNull(p.theFilter)
        p.grp(rlong(1))
        self.assertNotNull(p.theFilter)
        self.assertNotNull(p.theFilter.groupId)
        assert rlong(1) == p.getGroup()
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
        assert rbool(True) == p.acquisitionData().getAcquisitionData()
        assert rbool(False) == p.noAcquisitionData().getAcquisitionData()
        self.assertNotNull(p.getAcquisitionData())

    def testOptionsOrphan(self):
        p = ParametersI()
        self.assertNull(p.getOrphan())
        assert rbool(True) == p.orphan().getOrphan()
        assert rbool(False) == p.noOrphan().getOrphan()
        self.assertNotNull(p.getOrphan())

    def testOptionsLeaves(self):
        p = ParametersI()
        self.assertNull(p.getLeaves())
        assert rbool(True) == p.leaves().getLeaves()
        assert rbool(False) == p.noLeaves().getLeaves()
        self.assertNotNull(p.getLeaves())

    #
    # Parameters.map
    #

    def testDistinctMaps(self):
        p1 = ParametersI()
        p2 = ParametersI()
        assert p1.map is not p2.map

    def testSameMap(self):
        m = {'key': 0}
        p1 = ParametersI(parammap=m)
        p2 = ParametersI(parammap=m)
        assert p1.map is p2.map

    def testAddBasicString(self):
        p = ParametersI()
        p.add("string", rstring("a"))
        assert rstring("a") == p.map["string"]

    def testAddBasicInt(self):
        p = ParametersI()
        p.add("int", rint(1))
        assert rint(1) == p.map["int"]

    def testAddIdRaw(self):
        p = ParametersI()
        p.addId(1)
        assert rlong(1) == p.map["id"]

    def testAddIdRType(self):
        p = ParametersI()
        p.addId(rlong(1))
        assert rlong(1) == p.map["id"]

    def testAddLongRaw(self):
        p = ParametersI()
        p.addLong("long", 1)
        assert rlong(1) == p.map["long"]

    def testAddLongRType(self):
        p = ParametersI()
        p.addLong("long", rlong(1))
        assert rlong(1) == p.map["long"]

    def testAddIds(self):
        p = ParametersI()
        p.addIds([1, 2])
        p.map["ids"].val.index(rlong(1))
        p.map["ids"].val.index(rlong(2))

    def testAddLongs(self):
        p = ParametersI()
        p.addLongs("longs", [1, 2])
        p.map["longs"].val.index(rlong(1))
        p.map["longs"].val.index(rlong(2))
