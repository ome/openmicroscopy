/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import ome.conditions.ApiUsageException;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class AopContextInitializer extends HardWiredInterceptor {

    private final static Log log = LogFactory.getLog(AopContextInitializer.class);

    final ServiceFactory sf;
    
    final Principal pr;
    
    public AopContextInitializer(ServiceFactory sf, Principal p) {
        this.sf = sf;
        this.pr = p;
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable {
        HardWiredInterceptor.initializeUserAttributes(mi, sf, pr);
        return mi.proceed();
    }

}