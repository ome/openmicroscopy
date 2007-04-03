/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import ome.conditions.SecurityViolation;
import ome.security.MethodSecurity;
import ome.security.PasswordUtil;
import ome.security.SecuritySystem;
import ome.system.Principal;

import org.springframework.aop.framework.Advised;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

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
public class BasicMethodSecurity implements MethodSecurity {

    private final boolean active;

    private SimpleJdbcOperations jdbc;

    public BasicMethodSecurity() {
        active = true;
    }

    public BasicMethodSecurity(boolean active) {
        this.active = active;
    }

    public void setSimpleJdbcOperations(SimpleJdbcOperations jdbcOps) {
        this.jdbc = jdbcOps;
    }

    public boolean isActive() {
        return active;
    }

    public void checkMethod(Object o, Method m, Principal p) {
        String[] allowedRoles = null;
        Annotation[] anns;

        try {
            Class c = o.getClass(); // Getting runtime class
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
            }
        }
        // TODO add exception subclass
        if (allowedRoles==null) {
            throw new SecurityViolation("This method allows no remote access.");
        }

        // TODO need to catch PermitAll, etc.
        List<String> actualRoles = PasswordUtil.userGroups(jdbc, p.getName());
        for (String allowed : allowedRoles) {
            if (actualRoles.contains(allowed)) {
                // Only need to find one match.
                return;
            }
        }

        throw new SecurityViolation(String.format(
                "No matching roles found in %s for user %s", actualRoles, p));
    }

}
