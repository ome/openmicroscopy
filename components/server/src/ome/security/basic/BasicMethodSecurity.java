/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import ome.annotations.PermitAll;
import ome.annotations.RolesAllowed;
import ome.conditions.SecurityViolation;
import ome.security.MethodSecurity;
import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.system.Principal;

import org.springframework.aop.framework.Advised;

/**
 * Implementation of {@link MethodSecurity} which checks method security based
 * on the {@link RolesAllowed} annotations of our implementation methods. To do
 * this, it is necessary to "unwrap" proxies via the {@link Advised} interface.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since 3.0-Beta2
 */
public class BasicMethodSecurity implements MethodSecurity {

    private final boolean active;

    class Info {
        RolesAllowed rolesAllowed;
        boolean permitAll;
    }

    private SessionManager sessionManager;

    public BasicMethodSecurity() {
        this(true);
    }

    public BasicMethodSecurity(boolean active) {
        this.active = active;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * See {@link MethodSecurity#isActive()}
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @see MethodSecurity#checkMethod(Object, Method, Principal, boolean)
     */
    public void checkMethod(Object o, Method m, Principal p, boolean hasPassword) {
        String[] allowedRoles = null;
        Annotation[] anns;
        //
        // Map<Method, Info> map = info.get(o.getClass());
        // if (map == null) {
        // map = new MapMaker().makeMap();
        // map.put(o.getClass(), map);
        // }
        // Info i = map.get(m);
        // if (i == null) {
        // i.
        // }
        //        
        try {
            Class<?> c = o.getClass(); // Getting runtime class
            while (Advised.class.isAssignableFrom(c)) {
                Advised advised = (Advised) o;
                o = advised.getTargetSource().getTarget();
                c = o.getClass();
            }
            Method mthd = c.getMethod(m.getName(), m.getParameterTypes());
            anns = mthd.getDeclaredAnnotations();
        } catch (Exception e) {
            throw new SecurityViolation("Invalid method accessed.");
        }

        for (Annotation annotation : anns) {
            if (annotation instanceof RolesAllowed) {
                RolesAllowed ra = (RolesAllowed) annotation;
                allowedRoles = ra.value();
                break; // Can only be one annotation of a type
            } else if (annotation instanceof PermitAll) {
                return; // EARLY EXIT
            }
        }

        // TODO add exception subclass
        if (allowedRoles == null) {
            throw new SecurityViolation("This method allows no remote access.");
        }

        boolean allow = false;
        boolean block = false;

        List<String> actualRoles = sessionManager.getUserRoles(p.getName());
        for (String allowed : allowedRoles) {
            //ticket:665
            if (actualRoles.contains(allowed)) {
                allow = true;
            }
            // ticket:911
            if ("HasPassword".equals(allowed)) {
                block = !hasPassword;
            }
        }

        if (block) {
            throw new SecurityViolation(
                    "Bad authentication credentials for this action.\n" +
                    "See setSecurityPassword for more information");

        }

        if (!allow) {
            throw new SecurityViolation(String.format(
                    "No matching roles found in %s for session %s (allowed: %s)",
                    actualRoles, p, Arrays.asList(allowedRoles)));
        }
    }

}
