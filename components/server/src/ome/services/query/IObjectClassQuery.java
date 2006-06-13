/*
 * ome.services.query.IObjectClassQuery
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

// Third-party libraries
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

// Application-internal dependencies
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;

/**
 * simple query subclass which uses the {@link ome.parameters.Parameters#CLASS}
 * parameter value to create a {@link org.hibernate.Criteria} and then 
 * adds {@link org.hibernate.criterion.Expression} instances based on all
 * other parameter names.
 * <p>
 * For example:
 * </p>
 * <code>
 *   Parameters p = new Parameters().addClass( Image.class )
 *   .addString( "name", "LT-3059");
 * </code>
 * <p>  
 * produces a query of the form "select i from Image i where name = 'LT-3059'"
 * </p>
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0

 */
public class IObjectClassQuery extends Query 
{

    static String CLASS = Parameters.CLASS;
    
    static Definitions defs = new Definitions(
        new QueryParameterDef(CLASS, Class.class, false));
    
    public IObjectClassQuery(Parameters parameters ){
        super( defs, parameters );
    }

    @Override
    protected void buildQuery(Session session) 
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
        setCriteria( c );
    }
}