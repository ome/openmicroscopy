/*
 * ome.parameters.QueryParameter
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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

//Third-party libraries

//Application-internal dependencies
import ome.conditions.ApiUsageException;

/** 
 * arbitrary query parameter used by {@link ome.api.IQuery}.
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
public class QueryParameter implements Serializable
{
    
    final public Class type;
    final public String name;
    final public Object value;
    
    public QueryParameter(String name, Class type, Object value){

        if ( name == null )
            throw new ApiUsageException("Expecting a value for name.");
        
        if ( type == null )
            throw new ApiUsageException("Expecting a value for type.");
        
        if (value == null || type.isAssignableFrom(value.getClass()))
        {
            this.name = name;
            this.type = type;
            this.value = value;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("Value object should be of type: ");
            sb.append(type.getName());
            sb.append(" and not: ");
            sb.append(value.getClass().getName());
            throw new ApiUsageException(sb.toString());
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(64);
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
    
    public boolean equals(Object obj)
    {
        if ( ! (obj instanceof QueryParameter))
            return false;
        
        QueryParameter qp = (QueryParameter) obj;
        
        if ( this == qp )
            return true;

        if ( ! this.name.equals( qp.name) ) return false;
        if ( ! this.type.equals( qp.type ) ) return false;

        return this.value == null 
            ? qp.value == null 
                    : this.value.equals( qp.value );
        
    }
    
    public int hashCode()
    {
        int result = 11;
        result = 17 * result + name.hashCode();
        result = 19 * result + type.hashCode();
        result = 23 * result + (value == null ? 0 : value.hashCode());
        return result;
    }
    
    // ~ Serialization
    // =========================================================================
    private static final long serialVersionUID = 112229651549133492L;
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        
        if (type == null ) 
            throw new InvalidObjectException(
                    "QueryParameter type cannot be null.");
            
        if (value == null)
            throw new InvalidObjectException(
                    "QueryParameter value cannot be null.");
            
        if (! type.isAssignableFrom(value.getClass()))
            throw new InvalidObjectException(
                    "QueryParameter value must be of type type.");
    }
    
}
