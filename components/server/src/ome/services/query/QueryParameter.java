/*
 * ome.services.query.QueryParameter
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

// Third-party libraries

// Application-internal dependencies


/**
 * source of all our queries.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class QueryParameter
{

    public Class type;
    public String name;
    public Object value;
    
    public QueryParameter(String name, Class type, Object value){

        if ( type == null )
            throw new IllegalArgumentException("Expecting a value for type.");
        
        if (value == null || type.isAssignableFrom(value.getClass()))
        {
            this.name = name;
            this.type = type;
            this.value = value;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                    "Value object should be of"+
                    "type %s not %s",type.getName(),value.getClass().getName()));
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("QP{");
        sb.append("name=");
        sb.append(name);
        sb.append(",type=");
        sb.append(type.getName());
        sb.append(",value=");
        sb.append(value);
        sb.append("}");
        return sb.toString();
    }
    
    
}