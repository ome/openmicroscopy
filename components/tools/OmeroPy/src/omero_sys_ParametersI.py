"""
/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
"""

import omero
import omero_System_ice
from omero.rtypes import *

class ParametersI(omero.sys.Parameters):

    def __init__(self, map = {}):
        self.map = map

    #
    # Parameters.theFilter
    #

    def noPage(self):
        self.theFilter = None
        return self

    def page(self, offset, limit):
        if not self.theFilter:
            self.theFilter = omero.sys.Filter()
        self.theFilter.limit = rint(limit)
        self.theFilter.offset = rint(offset)
        return self

    #
    # Parameters.map
    #

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
