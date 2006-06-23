/*
 * ome.services.query.StringQuery
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
import java.util.Collection;

// Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.Session;

// Application-internal dependencies
import ome.parameters.Parameters;


/**
 * simple HQL query. Parameters are added as named parameters 
 * ({@link org.hibernate.Query#setParameter(java.lang.String, java.lang.Object)}.
 * Parameters with a value of type {@link Collection} are added as
 * {@link org.hibernate.Query#setParameterList(java.lang.String, java.util.Collection)} 
 * 
 * No parsing is done until execution time.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class StringQuery extends Query 
{
    
    /** 
     * parameter name for the definition of the HQL string.
     */
    public final static String STRING = "::string::";

    static Definitions defs = new Definitions(
        new QueryParameterDef(STRING, String.class, false));
    
    public StringQuery(Parameters parameters ){
        super( defs, parameters );
    }

    @Override
    protected void buildQuery(Session session) 
        throws HibernateException, SQLException
    {
        org.hibernate.Query query = session.createQuery((String) value(STRING));
        String[] nParams = query.getNamedParameters();
        for (int i = 0; i < nParams.length; i++)
        {
            String p = nParams[i];
            Object v = value(p);
            if (Collection.class.isAssignableFrom(v.getClass()))
            {
                query.setParameterList(p,(Collection)v);
            } else {
                query.setParameter(p,v);
            }
        }
        
        setQuery( query );
    }
}