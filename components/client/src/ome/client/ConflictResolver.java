/*
 * ome.client.ConflictResolver
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

package ome.client;

//Java imports
import java.util.ConcurrentModificationException;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;

/** 
 * strategy interface which handles version conflicts during registering 
 * of entities. 
 *  
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME3.0
 * @see ome.client.Session#register(IObject)
 */
public interface ConflictResolver
{
    /** 
     * strategy method to resolve version conflicts. Takes two objects -- the 
     * currently registered version and another non-registered version which may
     * or may not have the same version number.
     * @param registeredVersion currently registered entity
     * @param possibleReplacement entity which is to be considered for replacement
     * @return the entity which is to be used. This may be either of the existing
     *      objects or possibly a newly created one.
     * @throws ConcurrentModificationException this is a possible backdoor if a 
     *      decision cannot or should not be made, and is in fact the default
     *      strategy.
     */
    IObject resolveConflict( IObject registeredVersion, IObject possibleReplacement )
        throws ConcurrentModificationException;
}
