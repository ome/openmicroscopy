/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import ome.conditions.ApiUsageException;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Responsible for logging users in and out via the {@link Principal} 
 * before and after the actual invocation of OMERO methods.
 *
 * This class is the only {@link HardWiredInterceptor} which is 
 * hard-wired by default into OMERO classes. This permits simple start-up
 * without the need for the ant build, which may replace the hard-wired
 * value with a more extensive list of {@link HardWiredInterceptor}
 * instances.
 *
 * Note: any internal "client" will have to handle logging in and
 * out with an appropriate {@link Principal}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public final class BasicSecurityWiring extends HardWiredInterceptor {

    private final static Log log = LogFactory.getLog(BasicSecurityWiring.class);

    protected SecuritySystem securitySystem;
    
    /**
     * Lookup name.
     * @DEV.TODO This should be replaced by a components concept
     */
    @Override
    public String getName() {
        return "securityWiring";
    }
    
    /**
     * Setter injection.
     */
    public void setSecuritySystem(SecuritySystem secSys) {
        this.securitySystem = secSys;
    }
    
    /**
     * Wraps all OMERO invocations with login/logout semantics.
     */
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {   
            login(mi);
            return mi.proceed();
        } finally {
            logout();
        }
    }

    private void login(MethodInvocation mi) {
        
        Principal p = getPrincipal(mi);
        if (p != null ) {
            securitySystem.login(p);
            if (log.isDebugEnabled()) {
                log.debug("Running with user: " + p.getName());
            }
        } else {
            throw new ApiUsageException(
                    "ome.system.Principal instance must be provided on login.");
        }

    }

    private void logout() {
        securitySystem.logout();
    }

}
