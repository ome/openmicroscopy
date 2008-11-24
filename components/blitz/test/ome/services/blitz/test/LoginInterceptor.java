/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import ome.security.basic.PrincipalHolder;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * With {@link ManagedContextFixture} this test class was copied from
 * components/server/ome/server/itests, due to lack of support in the ant
 * build for sharing testing infrastructure (Nov2008)
 * @DEV.TODO Reunite with server code.
 */
public class LoginInterceptor implements MethodInterceptor {

    final PrincipalHolder holder;
    public Principal p;

    LoginInterceptor(PrincipalHolder holder) {
        this.holder = holder;
    }

    public Object invoke(MethodInvocation arg0) throws Throwable {
        int still;
        still = holder.size();
        if (still != 0) {
            throw new RuntimeException(still + " remaining on login!");
        }
        holder.login(p);
        try {
            return arg0.proceed();
        } finally {
            still = holder.logout();
            if (still != 0) {
                throw new RuntimeException(still + " remaining on logout!");
            }
        }
    }

}