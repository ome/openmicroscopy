/*
 * ome.services.query.Definitions
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.services.query;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Third-party libraries

// Application-internal dependencies


/**
 * container for {@link ome.services.query.QueryParameterDef} instances.
 * Typically created as a static variable in a Query and passed to the super
 * constructor ({@link ome.services.query.Query#Query(Definitions, Parameters))
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class Definitions {

    /** 
     * internal storage for the {@link QueryParameterDef}s. Should not change
     * after construction.
     */
    final private Map<String,QueryParameterDef> defs 
        = new HashMap<String, QueryParameterDef>();
    
    /* no default constructor */
    private Definitions(){}
    
    public Definitions(QueryParameterDef...parameterDefs)
    {
        if ( parameterDefs != null)
            for (QueryParameterDef def : parameterDefs)
            {
                defs.put( def.name, def );
            }
    }
    
    public boolean containsKey(Object key)
    {
        return defs.containsKey(key);
    }

    public boolean isEmpty()
    {
        return defs.isEmpty();
    }

    public Set<String> keySet()
    {
        return defs.keySet();
    }

    public int size()
    {
        return defs.size();
    }

    public QueryParameterDef get(Object key)
    {
        return defs.get(key);
    }
   
}