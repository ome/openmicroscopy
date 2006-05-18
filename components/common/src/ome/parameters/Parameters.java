/*
 * ome.parameters.Parameters
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package ome.parameters;

//Java imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.conditions.ApiUsageException;

/** 
 * container object for {@link QueryParameter} and {@link Filter} instances.
 * 
 * @author  <br>Josh Moore&nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 3.0-M2
 */
public class Parameters implements Serializable
{
       
    // For Queries
    public final static String ID = "id";
    public final static String IDS = "ids";
    public final static String CLASS = "class";
    public final static String OPTIONS = "options";
    public final static String ALGORITHM = "algorithm";
    public final static String OWNER_ID = "ownerId"; // TODO from Fitlers I/F
    
    private Filter filter;
    
    private transient Map queryParameters = new HashMap();

    /** default constructor. {@link Filter} is left null. 
     * {@link QueryParameter queryParameters} collection is initialized to empty
     * {@link Collection}
     */ 
    public Parameters( )
    {}
    
    /** copy constructor. {@link Filter} is taken from old instance, unless one
     * is already present, in which case an {@link ApiUsageException} is thrown.
     * {@link QueryParameter queryParameters} are merged.
     * @param old
     */ 
    public Parameters( Parameters old )
    {
        if ( old == null ) return;
        addAll( old );
    }

    // ~ READ METHODS
    // =========================================================================
    public QueryParameter[] queryParameters()
    {
        return (QueryParameter[]) 
        queryParameters.values().toArray(
                new QueryParameter[queryParameters.size()]);
    }
    
    public QueryParameter get(String name)
    {
        return (QueryParameter) queryParameters.get(name);
    }
    
    public Set keySet()
    {
        return queryParameters.keySet();
    }

    // ~ WRITE METHODS
    // =========================================================================
    
    
    public Parameters setFilter( Filter filter )
    {
        this.filter = filter;
        return this;
    }
    
    public Parameters add( QueryParameter parameter )
    {
        if ( parameter == null )
            throw new ApiUsageException("Parameter argument may not be null.");
        
        queryParameters.put(parameter.name, parameter );
        return this;
    }

    public Parameters addAll( Parameters old )
    {
        if ( old == null )
            throw new ApiUsageException("Parameters argument may not be null.");
        
        if ( old.filter != null )
        {
            if ( filter == null )
            {
                throw new ApiUsageException(
                    "Two filters not allowed during copy constructor.");
            } else {
                filter = old.filter;
            }
        }
        
        QueryParameter[] qps = old.queryParameters();
        for (int i = 0; i < qps.length; i++)
        {
            add(qps[i]);
        }
        
        return this;

    }
    
    public Parameters addClass( Class klass ){
        addClass( CLASS, klass );
        return this;
    }
    
    public Parameters addClass(String name, Class value)
    {
        add( new QueryParameter(name,Class.class,value) );
        return this;
    }

    public Parameters addBoolean(String name, Boolean value)
    {
        add( new QueryParameter(name,Boolean.class,value) );
        return this;
    }
    
    public Parameters addInteger(String name, Integer value)
    {
        add( new QueryParameter(name,Integer.class,value) );
        return this;
    }
    
    public Parameters addLong(String name, Long value)
    {
        add( new QueryParameter(name,Long.class,value) );
        return this;
    }
    
    public Parameters addSet(String name, Set value)
    {
        add( new QueryParameter(name, Set.class, value) );
        return this;
    }
    
    public Parameters addList(String name, List value)
    {
        add( new QueryParameter(name,List.class,value) );
        return this;
    }
    
    public Parameters addMap(String name, Map value)
    {
        add( new QueryParameter(name,Map.class,value) );
        return this;
    }
    
    public Parameters addString(String name, String value)
    {
        add( new QueryParameter(name,String.class,value) );
        return this;
    }

    public Parameters addId( Long id )
    {
        add( new QueryParameter( ID, Long.class, id ));
        return this;
    }
    
    public Parameters addIds( Collection ids )
    {
        add( new QueryParameter( IDS, Collection.class, ids ));
        return this;
    }
    
    public Parameters addOptions( Map options ){
        addMap( OPTIONS, options );
        return this;
    }
    
    public Parameters addAlgorithm( String algo )
    {
        addString( ALGORITHM, algo );
        return this;
    }

    
    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 6428983610525830551L;
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        int size = s.readInt();
     
        queryParameters = new HashMap();
        for (int i = 0; i < size; i++)
        {
            add( (QueryParameter) s.readObject() );
        }
        
    }
    
    private void writeObject(ObjectOutputStream s)
    throws IOException 
    {
        s.defaultWriteObject();
        
        Set keySet = queryParameters.keySet();
        s.writeInt(keySet.size());
        
        Iterator it = keySet.iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            s.writeObject(queryParameters.get(key));
        }
        
    }
    
}
