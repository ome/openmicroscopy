/*
 * ome.security.SecureAction
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;

/**
 * action for passing to {@link SecuritySystem#doAction(IObject, SecureAction)}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public interface SecureAction {
    /**
     * executes with special privilegs within the {@link SecuritySystem}. These
     * privileges will only be granted to the top-level objects.
     */
    <T extends IObject> T updateObject(T... objs);
}
