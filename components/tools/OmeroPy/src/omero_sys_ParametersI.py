#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""

import Ice
import omero
from omero.rtypes import rint, rbool, rtime, rlist, rstring, rlong

_omero = Ice.openModule("omero")
_omero_sys = Ice.openModule("omero.sys")
__name__ = "omero.sys"


class ParametersI(omero.sys.Parameters):
    """
    Helper subclass of omero.sys.Parameters for simplifying method
    parameter creation.
    """

    def __init__(self, parammap=None):
        """
        If no argument is provided, creates an instance to prevent later
        NoneType exceptions. To save memory, it is possible to pass None
        as the first argument.

        In order to prevent a single dict being referenced by several
        instances of ParametersI a new dict is created if an empty one is
        passed in either implicitly or explicitly.

        Uses (and does not copy) the given dict as the named parameter
        store in this instance. Be careful if either null is passed in
        or if this instance is being used in a multi-threaded environment.
        No synchronization takes place.
        """
        if parammap is None or len(parammap) == 0:
            self.map = {}
        else:
            self.map = parammap
        self.theFilter = None
        self.theOptions = None

    def _rt(self, arg, rt):
        """
        if arg is None, return None, otherwise use the
        rt callable to wrap the argument.
        """
        if arg is None:
            return None
        else:
            return rt(arg)

    # Parameters.theFilter.limit & offset
    # ====================================================================

    def noPage(self):
        """
        Nulls both the Filter.limit and Filter.offset values.
        """
        if self.theFilter:
            self.theFilter.limit = None
            self.theFilter.offset = None
        return self

    def page(self, offset, limit):
        """
        Sets both the Filter.limit and Filter.offset values by
        wrapping the arguments in omero.RInts if necessary.
        """
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.limit = self._rt(limit, rint)
        self.theFilter.offset = self._rt(offset, rint)
        return self

    def isPagination(self):
        """
        Returns True if the filter contains a limit OR an offset,
        False otherwise.
        """
        if self.theFilter:
            return None != self.theFilter.limit or \
                None != self.theFilter.offset
        return False

    def getOffset(self):
        """
        Returns the value of the offset parameter.
        """
        if self.theFilter:
            return self.theFilter.offset
        return None

    def getLimit(self):
        """
        Returns the value of the limit parameter.
        """
        if self.theFilter:
            return self.theFilter.limit
        return None

    def unique(self):
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.unique = rbool(True)
        return self

    def noUnique(self):
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.unique = rbool(False)
        return self

    def getUnique(self):
        if self.theFilter:
            return self.theFilter.unique
        return None

    # Parameters.theFilter.ownerId & groupId
    # ====================================================================

    def exp(self, i):
        """
        Sets the value of the <code>experimenter</code> parameter.
        """
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.ownerId = self._rt(i, rlong)
        return self

    def allExps(self):
        """
        Removes the <code>experimenter</code> parameter from the map.
        """
        if self.theFilter:
            self.theFilter.ownerId = None
        return self

    def isExperimenter(self):
        """
        Returns <code>true</code> if the filter contains and
        <code>ownerId</code> parameter, <code>false</code> otherwise.
        """
        if self.theFilter:
            return None != self.theFilter.ownerId
        return False

    def getExperimenter(self):
        """
        Returns the value of the <code>experimenter</code> parameter.
        """
        if self.theFilter:
            return self.theFilter.ownerId
        return None

    def grp(self, i):
        """
        Sets the value of the <code>group</code> parameter.
        """
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.groupId = self._rt(i, rlong)
        return self

    def allGrps(self):
        """
        Removes the <code>group</code> parameter from the map.
        """
        if self.theFilter:
            self.theFilter.groupId = None
        return self

    def isGroup(self):
        """
        Returns <code>true</code> if the filter contains an
        <code>groupId</code>, <code>false</code> otherwise.
        """
        if self.theFilter:
            return None != self.theFilter.groupId
        return False

    def getGroup(self):
        """
        Returns the value of the <code>group</code> parameter.
        """
        if self.theFilter:
            return self.theFilter.groupId
        return None

    # ~ Parameters.theFilter.startTime, endTime
    # ====================================================================

    def startTime(self, startTime):
        """
        Sets the value of the <code>start time</code> parameter.
        """
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
            self.theFilter.startTime = self._rt(startTime, rtime)
        return self

    def endTime(self, endTime):
        """
        Sets the value of the <code>end time</code> parameter.
        """
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.endTime = self._rt(endTime, rtime)
        return self

    def allTimes(self):
        """
        Removes the time parameters from the map.
        """
        if self.theFilter:
            self.theFilter.startTime = None
            self.theFilter.endTime = None
        return self

    def isStartTime(self):
        """
        Returns <code>true</code> if the map contains the
        <code>startTime</code> parameter, <code>false</code> otherwise.
        """
        if self.theFilter:
            return None != self.theFilter.startTime
        return False

    def getStartTime(self):
        """
        Returns the value of the <code>start time</code> parameter.
        """
        if self.theFilter:
            return self.theFilter.startTime
        return None

    def isEndTime(self):
        """
        Returns <code>true</code> if the map contains the <code>end time</code>
        parameter, <code>false</code> otherwise.
        """
        if self.theFilter:
            return None != self.theFilter.endTime
        return False

    def getEndTime(self):
        """
        Returns the value of the <code>end time</code> parameter.
        """
        if self.theFilter:
            return self.theFilter.endTime
        return None

    # ~ Parameters.theOption.leaves, orphan, acquisitionData, cacheable
    # ====================================================================

    def leaves(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.leaves = rbool(True)
        return self

    def noLeaves(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.leaves = rbool(False)
        return self

    def getLeaves(self):
        if self.theOptions:
            return self.theOptions.leaves
        return None

    def orphan(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.orphan = rbool(True)
        return self

    def noOrphan(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.orphan = rbool(False)
        return self

    def getOrphan(self):
        if self.theOptions:
            return self.theOptions.orphan
        return None

    def acquisitionData(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.acquisitionData = rbool(True)
        return self

    def noAcquisitionData(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.acquisitionData = rbool(False)
        return self

    def getAcquisitionData(self):
        if self.theOptions:
            return self.theOptions.acquisitionData
        return None

    def cache(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.cache = rbool(True)
        return self

    def noCache(self):
        if not self.theOptions:
            self.theOptions = omero.sys.Options()
        self.theOptions.cache = rbool(False)
        return self

    def getCache(self):
        if self.theOptions:
            return self.theOptions.cache
        return None

    # Parameters.map
    # ====================================================================

    def add(self, name, rtype):
        self.map[name] = rtype
        return self

    def addId(self, id):
        self.addLong("id", rlong(id))
        return self

    def addIds(self, longs):
        self.addLongs("ids", longs)
        return self

    def addLong(self, name, longValue):
        self.add(name, rlong(longValue))
        return self

    def addLongs(self, name, longs):
        rlongs = rlist([])
        for l in longs:
            rlongs.val.append(rlong(l))
        self.add(name, rlongs)
        return self

    def addString(self, name, stringValue):
        self.add(name, rstring(stringValue))
        return self

_omero_sys.ParametersI = ParametersI
