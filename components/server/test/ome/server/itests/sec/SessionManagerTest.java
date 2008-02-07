/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.Principal;

import org.springframework.context.ApplicationEvent;
import org.testng.annotations.BeforeMethod;

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
    }

    public void testGetsEventAndBlocksOnNextCall() throws Exception {
        login("root", "user", "User");
        ApplicationEvent event = new UserGroupUpdateEvent(this);
        Session s = sm.create(new Principal("root", "user", "Test"));
        long last1 = sc.getLastUpdated();
        sm.onApplicationEvent(event);
        sm.update(s);
        long last2 = sc.getLastUpdated();
        assertTrue(last2 > last1);
    }

    public void testProvidesCallbacksOnObjectExpiration() throws Exception {

    }

    public void testThrowsRemovedSession() {
        fail("nyi");
    }

    public void testThrowsExpiredSession() {
        fail("nyi");
    }

    public void testFakingAnotherUserDoesntWork() {
        fail("nyi");
    }

}
