/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

import ome.model.IObject;
import ome.security.SecureAction;
import ome.security.auth.SimpleRoleProvider;

import org.hibernate.Session;

/**
 * Helper originally from {@link SimpleRoleProvider} for merging admin objects.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 *
 */
public class SecureMerge implements SecureAction {

    private final Session session;

    private final boolean flush;

    public SecureMerge(Session session) {
        this(session, false);
    }

    public SecureMerge(Session session, boolean flush) {
        this.flush = flush;
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public <T extends IObject> T updateObject(T... objs) {
        T t = (T) session.merge(objs[0]);
        if (flush) {
            session.flush();
        }
        return t;
    }

}