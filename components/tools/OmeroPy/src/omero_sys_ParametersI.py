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

class ParametersI(omero.sys.Parameters):

    def __init__(self, map = {}):
        self.map = map

    def add(self, name, rtype):
        self.map[name] = rtype
        return self

    def addId(self, longValue):
        self.addLong("id", longValue)
        return self

    def addId(self, longOrRLongValue):
        isinstance(longOrRLongValue, long) and \
            self.addLong("id", longOrRLongValue) or \
            self.add("id",longOrRLongValue)
        return self

    def addIds(self, longs):
        self.addLongs("ids", longs)
        return self

    def addLong(self, name, longValue):
        self.add(name, omero.RLong(longValue))
        return self

    def addLongs(self, name, longs):
        rlongs = omero.RList([])
        for l in longs:
            rlongs.val.append(omero.RLong(l))
        self.add(name, rlongs)
        return self
