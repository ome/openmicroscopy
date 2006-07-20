/*
 * ome.api.ITypes
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

package ome.api;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Permissions;

/** 
 * Access to reflective type information. Also provides simplified access to 
 * special types like enumerations. 
 *
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $$)
 * </small>
 * @since OMERO3
 */

public interface ITypes extends ServiceInterface
{
    
    <T extends IObject> List<Class<T>> getResultTypes ( ); 
    <T extends IObject> List<Class<T>> getAnnotationTypes ( );
    <T extends IObject> List<Class<T>> getContainerTypes ( );
    <T extends IObject> List<Class<T>> getPojoTypes ( );
    <T extends IObject> List<Class<T>> getImportTypes ( );
    
    <T extends IEnum> T createEnumeration( T newEnum );
    <T extends IEnum> List<T> allEnumerations( Class<T> k);
    
    /** lookup an enumeration value. As with the get-methods of {@link IQuery}
     * queries returning no results will through an exception.
     * 
     * @param <T> The type of the enumeration. Must extend {@link IEnum}
     * @param k An enumeration class which should be searched.
     * @param string The value for which an enumeration should be found.
     * @return A managed enumeration. Never null.
     * @throws ApiUsageException if {@link IEnum} is not found.
     */
    <T extends IEnum> T getEnumeration( Class<T> k, String string ); 
    <T extends IObject> Permissions permissions( Class<T> k );
    
}
