/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * container for {@link ome.services.query.QueryParameterDef} instances.
 * Typically created as a static variable in a Query and passed to the super
 * constructor {@link ome.services.query.Query#Query(Definitions, Parameters)}
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since OMERO 3.0
 */
public class Definitions {

    /**
     * internal storage for the {@link QueryParameterDef}s. Should not change
     * after construction.
     */
    final private Map<String, QueryParameterDef> defs = new HashMap<String, QueryParameterDef>();

    /* no default constructor */
    private Definitions() {
    }

    public Definitions(QueryParameterDef... parameterDefs) {
        if (parameterDefs != null) {
            for (QueryParameterDef def : parameterDefs) {
                defs.put(def.name, def);
            }
        }
    }

    public boolean containsKey(Object key) {
        return defs.containsKey(key);
    }

    public boolean isEmpty() {
        return defs.isEmpty();
    }

    public Set<String> keySet() {
        return defs.keySet();
    }

    public int size() {
        return defs.size();
    }

    public QueryParameterDef get(Object key) {
        return defs.get(key);
    }

}
