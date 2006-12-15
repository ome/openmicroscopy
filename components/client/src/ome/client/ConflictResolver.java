/*
 * ome.client.ConflictResolver
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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
