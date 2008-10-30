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
            self.theFilter = Filter()
        self.theFilter.limit = rint(limit)
        self.theFilter.offset = rint(offset)
        return self

    #
    # Parameters.map
    #
    
    public ParametersI add(String name, RType r) {
        this.map.put(name, r);
        return this;
    }

    public ParametersI addId(long id) {
        add("id", rlong(id));
        return this;
    }

    public ParametersI addId(RLong id) {
        add("id", id);
        return this;
    }

    public ParametersI addIds(Collection<Long> longs) {
        addLongs("ids", longs);
        return this;
    }

    public ParametersI addLong(String name, long l) {
        add(name, rlong(l));
        return this;
    }

    public ParametersI addLong(String name, RLong l) {
        add(name, l);
        return this;
    }

    public ParametersI addLongs(String name, Collection<Long> longs) {
        RList rlongs = rlist();
        for (Long l : longs) {
            rlongs.add( rlong(l) );
        }
        this.map.put(name, rlongs);
        return this;
    }

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
        self.add(name, rlong(longValue))
        return self

    def addLongs(self, name, longs):
        rlongs = rlist([])
        for l in longs:
            rlongs.val.append(rlong(l))
        self.add(name, rlongs)
        return self
