/*
 *   $Id$
 *
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.containers.Dataset;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.sessions.stats.SessionStats;
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
        Session s = sm.createWithAgent(new Principal("root", "user", "Test"), "Test", "127.0.0.1");
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

    @Test(expectedExceptions = ValidationException.class)
    public void testDeleteUserShouldntHang() {

        Experimenter e = loginNewUser();
        loginRoot();
        iAdmin.deleteExperimenter(e);

    }

    @Test
    public void testUpdateSessionPermitsChangingDefaultGroup() {
        login("root", "user", "User");
        assertEquals("system", iAdmin.getEventContext().getCurrentGroupName());

        String uuid = uuid();
        ExperimenterGroup newGroup = new ExperimenterGroup(uuid, false);
        long gid = iAdmin.createGroup(newGroup);
        iAdmin.addGroups(new Experimenter(0L, false), new ExperimenterGroup(
                gid, false));

        ExperimenterGroup g = iAdmin.lookupGroup(uuid);
        setGroupContext(g);

        assertEquals(uuid, iAdmin.getEventContext().getCurrentGroupName());

    }

    private void setGroupContext(ExperimenterGroup g) {
        String sid = iAdmin.getEventContext().getCurrentSessionUuid();
        this.sessionManager.setSecurityContext(new Principal(sid), g);
    }

    @Test(groups = "ticket:2088", expectedExceptions = SecurityViolation.class)
    public void testSetSecurityContextChecksGroup() {
        loginNewUser();
        long gid = iAdmin.getEventContext().getCurrentGroupId();
        loginNewUser();
        setGroupContext(new ExperimenterGroup(gid, false));
    }

    @Test
    public void testInputOutputEnvironments() throws Exception {
        login("root", "user", "User");
        Session s = sm.createWithAgent(new Principal("root", "user", "Test"), "Test", "127.0.0.1");
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
    
    // Timeouts
    
    @Test
    public void testTimeouts() throws Exception {
        login("root", "user", "User");
        Session s = sm.createWithAgent(new Principal("root", "user", "Test"), "Test", "127.0.0.1");
        String uuid = s.getUuid();

        // By default TTI is non-null, we're assuming this is the case here
        s.setTimeToIdle(0L);
        try {
            sm.update(s);
            fail("No security violation!");
        } catch (SecurityViolation sv) {
            // ok
        }
        s.setTimeToIdle(12345L);
        sm.update(s);
    }
    
    @Test
    public void testTimeoutsWithNulls() throws Exception {
        login("root", "user", "User");
        Session s = sm.createWithAgent(new Principal("root", "user", "Test"), "Test", "127.0.0.1");
        String uuid = s.getUuid();

        Session newSession = new Session();
        newSession.setUuid(uuid);
        newSession.setTimeToIdle(12346L);
        sm.update(newSession);
    }
    
    @Test(groups = {"ticket:1254","manual"})
    public void testSynchronizationLocksCallers() throws Exception {
        long start = System.currentTimeMillis();
        while((System.currentTimeMillis() - start) < 5*60*1000L) {
            loginRoot();
            iQuery.find(Experimenter.class, 0L);
            Dataset[] ds = new Dataset[10];
            for (int i = 0; i < ds.length; i++) {
                ds[i] = new Dataset("ticket:1254");
            }
            iUpdate.saveArray(ds);
            sm.close(loginAop.p.getName());
        }
    }
    
    @Test(groups = {"ticket:2196"}, expectedExceptions = SecurityViolation.class)
    public void testNoSetSecurityContextOnActiveMethod() {
        loginRoot();
        SessionStats stats = sm.getSessionStats(loginAop.p.getName());
        stats.methodIn();
        sm.setSecurityContext(loginAop.p, new ExperimenterGroup(0L, false));
    }

}
