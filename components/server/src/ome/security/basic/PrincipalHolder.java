/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import ome.security.SecuritySystem;
import ome.system.Principal;

/**
 * Stack of active {@link Principal} instances. As a user logs in, an empty
 * context is created which must later be primed by the {@link SecuritySystem}
 * in order to be operational.
 * 
 * @see BasicSecuritySystem
 */
public interface PrincipalHolder {

    /**
     * Get the number of active principal contexts.
     * @return the number of active principals
     */
    public int size();

    /**
     * Get the last, i.e. currently active, principal.
     * @return the current principal
     */
    public Principal getLast();

    /**
     * Add a new principal context to the stack.
     * @param principal the principal to add
     */
    public void login(Principal principal);

    /**
     * Allow logging in directly with an event context.
     * @param bec the event context to use
     */
    public void login(BasicEventContext bec);

    /**
     * Pop the last created principal context and return the number of active
     * contexts remaining.
     * @return the number of active principals after the logout
     */
    public int logout();
}
