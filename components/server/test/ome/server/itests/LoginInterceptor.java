/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import ome.security.basic.PrincipalHolder;
import ome.system.Principal;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Simple interceptor used to control login on all method calls.
 */
public class LoginInterceptor implements MethodInterceptor {

    final PrincipalHolder holder;
    Principal p;

    LoginInterceptor(PrincipalHolder holder) {
        this.holder = holder;
    }

    public Object invoke(MethodInvocation arg0) throws Throwable {
        holder.login(p);
        try {
            return arg0.proceed();
        } finally {
            holder.logout();
        }
    }

}