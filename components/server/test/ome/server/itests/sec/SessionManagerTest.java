/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.Principal;

import org.springframework.context.ApplicationEvent;
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
    }

    @Test
    public void testGetsEventAndBlocksOnNextCall() throws Exception {
        login("root", "user", "User");
        ApplicationEvent event = new UserGroupUpdateEvent(this);
        Session s = sm.create(new Principal("root", "user", "Test"));
        long last1 = sc.getLastUpdated();
        sm.onApplicationEvent(event);
        Thread.sleep(2000L);
        sm.update(s);
        long last2 = sc.getLastUpdated();
        assertTrue(last2 > last1);
    }

    @Test
    public void testProvidesCallbacksOnObjectExpiration() throws Exception {

    }

    @Test
    public void testThrowsRemovedSession() {
        fail("nyi");
    }

    @Test
    public void testThrowsExpiredSession() {
        fail("nyi");
    }

    @Test
    public void testFakingAnotherUserDoesntWork() {
        fail("nyi");
    }

    @Test
    public void testDeleteUserShouldntHang() {

        Experimenter e = loginNewUser();
        loginRoot();
        iAdmin.deleteExperimenter(e);

    }

    @Test
    public void testInputOutputEnvironments() throws Exception {
        login("root", "user", "User");
        Session s = sm.create(new Principal("root", "user", "Test"));
        String uuid = s.getUuid();

        assertNull(sessionManager.getInput(uuid, "a"));
        sessionManager.setInput(uuid, "a", 1L);
        assertEquals(1L, sessionManager.getInput(uuid, "a"));
        sessionManager.setInput(uuid, "a", null);
        assertNull(sessionManager.getInput(uuid, "a"));

        assertNull(sessionManager.getOutput(uuid, "a"));
        sessionManager.setOutput(uuid, "a", 2L);
        assertEquals(2L, sessionManager.getOutput(uuid, "a"));
        sessionManager.setOutput(uuid, "a", null);
        assertNull(sessionManager.getOutput(uuid, "a"));
    }
}
