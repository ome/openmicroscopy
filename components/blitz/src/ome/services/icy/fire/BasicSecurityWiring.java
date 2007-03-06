/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.fire;

import ome.conditions.ApiUsageException;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class BasicSecurityWiring extends HardWiredInterceptor {

    private final static Log log = LogFactory.getLog(BasicSecurityWiring.class);

    protected SecuritySystem securitySystem;
    
    public void setSecuritySystem(SecuritySystem secSys) {
        this.securitySystem = secSys;
    }
    
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