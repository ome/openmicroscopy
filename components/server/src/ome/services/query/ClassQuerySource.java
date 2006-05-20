
/*
 * ome.services.query.ClassQuerySource
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
import java.lang.reflect.Constructor;
import java.sql.SQLException;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

// Application-internal dependencies
import ome.model.IObject;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;


/**
 * creates a query based on the id string by interpreting it as a Class.
 * The class can either be a {@link ome.services.query.Query} implementation 
 * or an {@link ome.model.IObject} implementation.
 * 
 * <p>
 * If it is an {@link ome.model.IObject} implementation, the 
 * {@link ome.parameters.QueryParameter} instances passed in through
 * {@link Parameters} are interpreted as being field names whose 
 * {@link ome.parameters.QueryParameter#value values} should equals the 
 * value in the database.
 * </p> 
 * 
 * <p>
 * If it is an {@link ome.services.query.Query} implementation, then it is
 * instantiated by passing the {@link ome.parameters.Parameters} into the 
 * constructor.
 * </p>
 *  
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class ClassQuerySource extends QuerySource
{

    private static Log log = LogFactory.getLog(ClassQuerySource.class);
    
    public Query lookup(String queryID, Parameters parameters)
    {
        Query q = null;
        Class klass = null;
        try
        {
            klass = Class.forName(queryID);
        } catch (ClassNotFoundException e)
        {
            // Not an issue.
        }
        
        
        // return null immediately
        if ( klass == null ) 
        {
            return null;
        }

        // it's a query
        else if ( Query.class.isAssignableFrom( klass ))
        {
            try {
                Constructor c = klass.getConstructor(Parameters.class);
                q = (Query) c.newInstance(parameters);
            } catch (Exception e)
            {
                if ( log.isDebugEnabled() ) 
                {
                    e.printStackTrace();
                }
                throw new RuntimeException("Error while trying to instantiate:" 
                        +queryID,e);
            }
            return q;
        }
        
        // it's an IObject
        else if ( IObject.class.isAssignableFrom( klass ))
        {
            Parameters p = new Parameters( parameters );
            p.addClass( klass );
            return new IObjectClassQuery( p );
        }
        
        else
        {
            return null;
        }
    }
    
}

class IObjectClassQuery extends Query 
{

    static String CLASS = Parameters.CLASS;
    
    static Definitions defs = new Definitions(
        new QueryParameterDef(CLASS, Class.class, false));
    
    public IObjectClassQuery(Parameters parameters ){
        super( defs, parameters );
    }

    @Override
    protected Object runQuery(Session session) 
        throws HibernateException, SQLException
    {
        Criteria c = session.createCriteria((Class) value(CLASS));
        for (QueryParameter qp : params.queryParameters())
        {
            if ( ! qp.name.equals( CLASS ) )
            {
                c.add(Expression.eq(qp.name,qp.value)); // TODO checks for type.                
            }
        }
        return c.list();
        
    }
}