/*
 * ome.services.query.CollectionQueryParameterDef
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

//Java imports
import java.util.Collection;
import java.util.Map;

// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;

/**
 * extension of {@link ome.services.query.QueryParameterDef} which restricts 
 * the {@link ome.services.query.QueryParameterDef#type type} to a 
 * {@link java.util.Collection}, and specifies the element types of that 
 * Collection. Also overrides validation to check that type.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class CollectionQueryParameterDef extends QueryParameterDef
{
    
    public Class elementType;
    
    public CollectionQueryParameterDef(String name, boolean optional, 
            Class elementType)
    {
        super( name, Collection.class, optional );
        this.elementType = elementType;
        
    }
    
    @Override
    /**
     * adds Collection-element tests after calling super.errorIfInvalid();
     */
    public void errorIfInvalid(QueryParameter parameter)
    {
        super.errorIfInvalid( parameter );
        
        if ( ! optional && ((Collection) parameter.value).size() < 1 )
            throw new ApiUsageException(
                    "Requried collection parameters may not be empty."
                    );
        
        if ( parameter.value != null ) 
            for (Object element : (Collection) parameter.value )
            {
                
                if ( element == null )
                    throw new ApiUsageException(
                            "Null elements are not allowed " +
                            "in parameter collections"
                    );
                
                if ( ! elementType.isAssignableFrom( element.getClass() ))  
                    throw new ApiUsageException(
                            "Elements of type "+element.getClass().getName()+
                            " are not allowed in collections of type "+
                            elementType.getName());
            }
        
    }
    
    
}
