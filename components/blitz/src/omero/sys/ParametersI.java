/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.sys;

import static omero.rtypes.rint;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import omero.RInt;
import omero.RList;
import omero.RLong;
import omero.RType;

public class ParametersI extends omero.sys.Parameters {

    public ParametersI() {
        this.map = new HashMap<String, RType>();
    }

    public ParametersI(Map<String, RType> map) {
        this.map = map;
    }

    //
    // Parameters.theFilter
    //
    
    public Parameters noPage() {
        this.theFilter = null;
        return this;
    }
    
    public ParametersI page(int offset, int limit) {
        return this.page(rint(offset), rint(limit));
    }
    
    public ParametersI page(RInt offset, RInt limit) {
        if (this.theFilter == null) {
            this.theFilter = new Filter();
        }
        this.theFilter.limit = limit;
        this.theFilter.offset = offset;
        return this;
    }
    
    //
    // Parameters.map
    //
    
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
}
