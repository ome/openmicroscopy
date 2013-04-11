/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.concurrent.atomic.AtomicBoolean;

import ome.logic.HardWiredInterceptor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AopContextInitializer extends HardWiredInterceptor {

    private final static Logger log = LoggerFactory.getLogger(AopContextInitializer.class);

    final ServiceFactory sf;
    
    final Principal pr;

    /**
     * Whether or not the current session was created via password-based (or
     * similar) login, or whether a session id was used to login (i.e. it's
     * "reused")
     */
    final AtomicBoolean reusedSession;

    public AopContextInitializer(ServiceFactory sf, Principal p, AtomicBoolean reusedSession) {
        this.sf = sf;
        this.pr = p;
        this.reusedSession = reusedSession;
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable {
        HardWiredInterceptor.initializeUserAttributes(mi, sf, pr, reusedSession);
        return mi.proceed();
    }

}
