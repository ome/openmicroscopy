/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.security.basic;

import ome.security.SecuritySystem;
import ome.tools.hibernate.SessionFactory;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides only the bare minimum of functionality to allow methods to succeed.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta-4.2
 */
public class NullEventHandler implements MethodInterceptor {

    private final static Logger log = LoggerFactory.getLogger(NullEventHandler.class);

    private final SecuritySystem secSys;

    private final SessionFactory sf;

    public NullEventHandler(SecuritySystem secSys, SessionFactory sf) {
        this.secSys = secSys;
        this.sf = sf;
    }

    public Object invoke(MethodInvocation arg0) throws Throwable {
        try {
            secSys.loadEventContext(true);
            return arg0.proceed();
        } finally {
            sf.getSession().clear();
            secSys.invalidateEventContext();
        }
    }
}
