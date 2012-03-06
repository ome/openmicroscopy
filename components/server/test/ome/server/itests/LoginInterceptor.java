/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import ome.security.basic.CurrentDetails;
import ome.system.Principal;

/**
 * Simple interceptor used to control login on all method calls.
 */
public class LoginInterceptor implements MethodInterceptor {

    final CurrentDetails cd;
    public Principal p;
    public Map<String, String> callContext = null;

    public LoginInterceptor(CurrentDetails cd) {
        this.cd = cd;
    }

    public Object invoke(MethodInvocation arg0) throws Throwable {
        int still;
        still = cd.size();
        if (still != 0) {
            throw new RuntimeException(still + " remaining on login!");
        }

        if (p != null) {
            cd.login(p);
            cd.setContext(callContext);
        }

        try {
            return arg0.proceed();
        } finally {
            cd.setContext(null);
            still = cd.logout();
            if (still != 0) {
                throw new RuntimeException(still + " remaining on logout!");
            }
        }
    }

}