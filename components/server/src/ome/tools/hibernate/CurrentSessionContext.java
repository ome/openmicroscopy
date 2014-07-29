/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

import org.hibernate.classic.Session;
import org.hibernate.context.JTASessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * Implementation of Hibernate's current_session_context_class extension point.
 * During the move to strict JTA compliance, nested calls to
 * {@link HibernateTemplate} and SessionFactoryUtils#getSession() were causing
 * "Session is closed" during CacheSynchronization. See
 * resources/ome/services/hibernate.xml
 */
public class CurrentSessionContext extends JTASessionContext {
    public CurrentSessionContext(SessionFactoryImplementor sf) {
        super(sf);
    }

    @Override
    protected Session buildOrObtainSession() {
        return (Session) SessionFactoryUtils.getSession(this.factory, false);
    }

    @Override
    protected boolean isAutoCloseEnabled() {
        return false;
    }

}
