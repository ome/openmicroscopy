/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SessionTimeoutException;
import ome.logic.HardWiredInterceptor;
import ome.security.MethodSecurity;
import ome.services.sessions.stats.DelegatingStats;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for logging users in and out via the {@link Principal} before and
 * after the actual invocation of OMERO methods.
 * 
 * This class is the only {@link HardWiredInterceptor} which is hard-wired by
 * default into OMERO classes. This permits simple start-up without the need for
 * the ant build, which may replace the hard-wired value with a more extensive
 * list of {@link HardWiredInterceptor} instances.
 * 
 * Note: any internal "client" will have to handle logging in and out with an
 * appropriate {@link Principal}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public final class BasicSecurityWiring extends HardWiredInterceptor {

    private final static Logger log = LoggerFactory.getLogger(BasicSecurityWiring.class);

    protected PrincipalHolder principalHolder;

    protected MethodSecurity methodSecurity;

    /**
     * Lookup name.
     */
    // TODO This should be replaced by a components concept
    @Override
    public String getName() {
        return "securityWiring";
    }

    /**
     * Setter injection.
     */
    public void setPrincipalHolder(PrincipalHolder principalHolder) {
        this.principalHolder = principalHolder;
    }

    /**
     * Setter injection.
     */
    public void setMethodSecurity(MethodSecurity security) {
        this.methodSecurity = security;
    }

    /**
     * Wraps all OMERO invocations with login/logout semantics.
     */
    public Object invoke(MethodInvocation mi) throws Throwable {

        Principal p = getPrincipal(mi);
        boolean hp = hasPassword(mi);
        boolean close = "close".equals(mi.getMethod().getName());

        if (!close  && methodSecurity.isActive()) {
            methodSecurity.checkMethod(mi.getThis(), mi.getMethod(), p, hp);
        }

        // First try a login
        try {
            login(mi, p);
        } catch (SessionTimeoutException ste) {
            if (!close) {
                throw ste;
            }
            log.warn("SessionTimeoutException on close:" + p);
            principalHolder.login(new CloseOnNoSessionContext());
        }

        // Assuming the above block didn't throw an
        // exception, then continue and cleanup.
        try {
            return mi.proceed();
        } finally {
            logout();
        }
    }

    private void login(MethodInvocation mi, Principal p) {

        if (p == null) {
            throw new ApiUsageException(
                    "ome.system.Principal instance must be provided on login.");
        }

        int size = principalHolder.size();
        if (size > 0) {
            throw new InternalException(
                    "SecuritySystem is still active on login. " + size
                            + " logins remaining in thread.");
        }
        principalHolder.login(p);
        if (log.isDebugEnabled()) {
            log.debug("Running with user: " + p.getName());
        }

    }

    private void logout() {
        int size = principalHolder.logout();
        if (size > 0) {
            log.error("SecuritySystem is still active on logout. " + size
                    + " logins remaining in thread.");
        }
    }

    public static class CloseOnNoSessionContext extends BasicEventContext {

        public CloseOnNoSessionContext()
        {
            super(new Principal(""), new DelegatingStats());
        }

    }
}
