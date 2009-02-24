/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.hibernate;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Simple source of Thread-aware {@link Session} instances. Wraps a
 * call to {@link SessionFactoryUtils}. Should be safe to call from
 * within any service implementation call or inside of Executor.execute.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class SessionFactory {

    private final org.hibernate.SessionFactory factory;

    public SessionFactory(org.hibernate.SessionFactory factory) {
        this.factory = factory;
    }

    public Session getSession() {
        return SessionFactoryUtils.getSession(factory, false);
    }

}
