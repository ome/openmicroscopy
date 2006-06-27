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

//Third-party libraries

//Application-internal dependencies
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

public interface ITypes
{
    
    <T extends IObject> Class<T>[] getResultTypes ( ); 
    <T extends IObject> Class<T>[] getAnnotationTypes ( );
    <T extends IObject> Class<T>[] getContainerTypes ( );
    <T extends IObject> Class<T>[] getPojoTypes ( );
    <T extends IObject> Class<T>[] getImportTypes ( );
    <T extends IObject> T[] allEnumerations( Class<T> k);
    <T extends IObject> T getEnumeration( Class<T> k, String string ); 
    <T extends IObject> Permissions permissions( Class<T> k );
    
}
