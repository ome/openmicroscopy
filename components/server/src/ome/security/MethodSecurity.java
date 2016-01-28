/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.lang.reflect.Method;

import ome.annotations.RolesAllowed;
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
     * Throws a {@link SecurityViolation} exception if the given
     * {@link Principal} does not have the proper permissions to execute the
     * given method. If {@link #isActive()} returns false, this method may also
     * throw any {@link RuntimeException} to specify that it is not in an active
     * state.
     *
     * @param obj     {@link Object} on which this method will be called.
     * @param method  {@link Method} to be called.
     * @param principal {@link Principal} for which permissions will be checked.
     * @param hasPassword flag if the user's session has been authenticated directly
     *  and not via a one-time session id or similar.
     * @throws SecurityViolation if the given pr
     */
    void checkMethod(Object obj, Method method, Principal principal, boolean hasPassword)
            throws SecurityViolation;

}
