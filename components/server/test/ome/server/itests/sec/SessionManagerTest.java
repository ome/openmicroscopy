/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.springframework.context.ApplicationEvent;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionManagerTest extends AbstractManagedContextTest {

    SessionManagerImpl sm;
    SessionCache sc;
    Executor ex;

    @BeforeMethod
    public void setup() {
        sm = (SessionManagerImpl) this.applicationContext
                .getBean("sessionManager");
        sc = (SessionCache) this.applicationContext.getBean("sessionCache");
        ex = (Executor) this.applicationContext.getBean("executor");
    }

    @Test(expectedExceptions = SessionException.class)
    public void testBadAssert() throws Exception {
        sm.assertSession("");
    }

    public void testGetsEventAndBlocksOnNextCall() throws Exception {
        ex.execute(null, new Executor.Work() {
            public void doWork(TransactionStatus status,
                    org.hibernate.Session session, ServiceFactory sf) {
                ApplicationEvent event = new UserGroupUpdateEvent(this);
                Session s = sm.create(new Principal("root", "system", "Test"));
                long last1 = sc.getLastUpdated();
                sm.onApplicationEvent(event);
                sm.update(s);
                long last2 = sc.getLastUpdated();
                assertTrue(last2 > last1);
            }
        });
    }

    public void testProvidesCallbacksOnObjectExpiration() throws Exception {

    }
}
