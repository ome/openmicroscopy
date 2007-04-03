/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import ome.conditions.SecurityViolation;
import ome.system.Principal;

/**
 * Interface which allows security interceptors to check if a method should be
 * executable for a given user. This determination is most likely based on
 * {@link RolesAllowed} annotations and replaces the security provided by an
 * application server.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since 3.0-Beta2
 */
public interface MethodSecurity {

    /**
     * Indicates whether or not method security is active. If not, then no
     * further checks should be made, and implementations are free to throw
     * exceptions if they are not properly initialized. Clients of this
     * interface can assume that method-level security has been configured
     * elsewhere.
     *
     * @return true if the other methods of this interface can and should be
     *         called.
     */
    boolean isActive();

    /**
     *
     * @param method
     * @param principal
     * @throws SecurityViolation if the given pr
     */
    void checkMethod(Object obj, Method method, Principal principal)
            throws SecurityViolation;

}
