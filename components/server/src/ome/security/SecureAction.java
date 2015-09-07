/*
 * ome.security.SecureAction
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import ome.model.IObject;

/**
 * Action for passing to {@link SecuritySystem#doAction(SecureAction, IObject...)}.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public interface SecureAction {
    /**
     * executes with special privileges within the {@link SecuritySystem}. These
     * privileges will be granted only to the top-level objects.
     * @param objs some model objects
     * @return the updated objects
     */
    <T extends IObject> T updateObject(T... objs);
}
