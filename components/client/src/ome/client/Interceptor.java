/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import javax.security.auth.login.LoginException;

import ome.conditions.RootException;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SecurityAssociation;
import org.springframework.jndi.JndiLookupFailureException;

/**
 * Basic client-side interceptor which wraps all method calls and can be used
 * for configurable exception handling.
 */
public class Interceptor implements MethodInterceptor {

    static {
        SecurityAssociation.setServer();
    }

    private final static Principal unknown = new Principal("unknown", "", "");

    private static Log log = LogFactory.getLog(Interceptor.class);

    final protected Principal principal;

    public Interceptor(Principal principal) {
        this.principal = principal;
    }

    public Object invoke(MethodInvocation arg0) throws Throwable {
        Object toReturn = null;
        Throwable toThrow = null;
        try {
            SecurityAssociation.setPrincipal(principal);
            toReturn = arg0.proceed();
        } catch (final Throwable t) {
            toThrow = t;
            if (t instanceof RootException) {
                // This is what we're expecting.
            } else if (t instanceof javax.ejb.EJBAccessException) {
                javax.ejb.EJBAccessException ejb = (javax.ejb.EJBAccessException) t;
                if (ejb.getCause() instanceof LoginException) {
                    LoginException login = (LoginException) ejb.getCause();
                    if (login.getCause() instanceof org.jboss.util.NestedSQLException) {
                        toThrow = new OutOfService(
                                "Database appears to be down, "
                                        + "improperly configured, or corrupted.",
                                t);
                    }
                } else {
                    // These are allowed to be thrown.
                }
            } else if (t instanceof ClassNotFoundException) {
                if (t.getMessage()
                        .contains("org.postgresql.util.PSQLException")) {
                    toThrow = new OutOfService(
                            "Database appears to be down, but no exception is "
                                    + "available since org.postgresql.util.PSQLException "
                                    + "is not on your classpath", t);
                } else {
                    toThrow = new OutOfService(
                            "Client appears improperly configured.", t);
                }
            } else if (t instanceof JndiLookupFailureException) {
                toThrow = new OutOfService(
                        "Cannot find service. Is the server running?");
            } else {
                toThrow = new OutOfService("Error during invocation. "
                        + "Most likely server version does "
                        + "not match client version", t);
            }
        } finally {
            SecurityAssociation.setPrincipal(unknown);
        }
        if (toThrow != null) {
            throw toThrow;
        } else {
            return toReturn;
        }
    }
}
