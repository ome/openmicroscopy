/*
 * ome.services.query.Query
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;

// Application-internal dependencies

/**
 * source of all our queries.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public abstract class Query implements HibernateCallback
{
    private static Log log = LogFactory.getLog(Query.class);
    
    public final static Map<Class,Integer> DEPTH = new HashMap<Class,Integer>();
    static {
        DEPTH.put(Project.class,2);
        DEPTH.put(Dataset.class,1);
        DEPTH.put(CategoryGroup.class,2);
        DEPTH.put(Category.class,1);
        DEPTH.put(Image.class,0);
    }
    public final static Map<Class,List<String>> LINEAGE = new HashMap<Class,List<String>>();
    static {
        LINEAGE.put(Project.class,Arrays.asList("datasetLinks","imageLinks"));
        LINEAGE.put(Dataset.class,Arrays.asList("imageLinks"));
    }
    
    protected void fetchParents(Criteria c, Class klass, int stopDepth)
    {
        List<String> l = LINEAGE.get(klass);
        String parents = l.get(l.size()-1);
        String parent = parents+".parent";
        String grandparents = parent+"."+l.get(l.size()-2);
        String grandparent = grandparents+".parent";
        
        walk(c,klass,stopDepth,parents,parent,grandparents,grandparent);
    }
    
    protected void fetchChildren(Criteria c, Class klass, int stopDepth)
    {
        String children = LINEAGE.get(klass).get(0);
        String child = children+".child";
        String grandchildren = child+"."+LINEAGE.get(klass).get(1);
        String grandchild = grandchildren+".child";
        
        walk(c,klass,stopDepth,children,child,grandchildren,grandchild);
    }
    
    private void walk(Criteria c, Class klass, int stopDepth, String... path){
        switch (DEPTH.get(klass))
        {
            case 2:
                if (stopDepth >= 2) 
                {
                    c.createCriteria(path[3],LEFT_JOIN);
                    c.createCriteria(path[2],LEFT_JOIN);
                }
            case 1:
                if (stopDepth >= 1) 
                {
                    c.createCriteria(path[1],LEFT_JOIN);
                    c.createCriteria(path[0],LEFT_JOIN);
                }
            case 0:
                return;
            default:
                throw new RuntimeException("Unhandled container depth.");
        }
    }

    public final static String OWNER_ID = "ownerId"; // TODO from Fitlers I/F
    // For Criteria
    public final static FetchMode FETCH = FetchMode.JOIN;
    public final static int LEFT_JOIN = Criteria.LEFT_JOIN;
    
    protected Map options;
    protected QueryParameterDef[] defs;
    protected QueryParameter[] qps;

    public Query(QueryParameter... parameters)
    {
        this.options = options;
        this.qps = parameters;
        defineParameters();
        checkParameters();
    }
    
    public int find(String name){
        for (int i = 0; i < qps.length; i++)
        {
            if (qps[i].name.equals(name))
                return i;
        }
        throw new IllegalArgumentException("Unknown parameter: "+name);
    }
    
    public boolean check(String name){
        return qps[find(name)].value == null ? false : true;
    }
    
    public QueryParameter get(String name){
        return qps[find(name)];
    }
    
    public Object value(String name){
        return qps[find(name)].value;
    }
    
    protected abstract void defineParameters();
    
    protected void checkParameters(){
        
        if (defs == null)
            throw new IllegalStateException(
                    "Query parameter definitions not set.");
        
        if (qps == null) 
            throw new IllegalArgumentException(
                    "Null arrays "+
                    "are not valid for definitions.");
        
        if (defs.length > qps.length)
            throw new IllegalArgumentException(
                    "As many Query parameters needed " +
                    "as Query parameter defitions. Currently missing "+
                    (defs.length-qps.length));
        
        for (int i = 0; i < qps.length; i++)
        {
            QueryParameter parameter = qps[i];
            QueryParameterDef def = defs[i];
            
            if (!def.name.equals(parameter.name))
                throw new IllegalArgumentException(
                        String.format(
                        " Parameter name %d doesn't match: %s != %s",
                        i,def.name,parameter.name
                        ));

            
            if ( parameter.type == null )
            {
                if (! def.optional)
                    throw new IllegalArgumentException("Parameter type cannot " +
                            "be null if not optional.");
                
            } else {
                if (!def.type.isAssignableFrom(parameter.type))
                        throw new IllegalArgumentException(
                                String.format(
                                        " Parameter type %d doesn't match: %s != %s",
                                        i,def.type,parameter.type));
            
            }
                
        }
        
    }
    
    public Object doInHibernate(Session session)
        throws HibernateException, SQLException
    {
        try {
            enableFilters(session);
            return runQuery(session);
        } finally {
            disableFilters(session);
        }
    }
    
    protected abstract Object 
    runQuery(Session session) throws HibernateException, SQLException;
    
    protected void enableFilters(Session session){
        
    }
    
    protected void disableFilters(Session session){
        
    }
}
