/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public void add(String name, RType r) {
        this.map.put(name, r);
    }

    public ParametersI addId(long id) {
        add("id", new RLong(id));
        return this;
    }

    public ParametersI addIds(Collection<Long> longs) {
        addLongs("ids", longs);
        return this;
    }

    public ParametersI addLong(String name, long l) {
        add(name, new RLong(l));
        return this;
    }

    public ParametersI addLongs(String name, Collection<Long> longs) {
        RList rlongs = new RList();
        rlongs.val = new ArrayList<RType>();
        for (Long l : longs) {
            rlongs.val.add(new RLong(l));
        }
        this.map.put(name, rlongs);
        return this;
    }
}
