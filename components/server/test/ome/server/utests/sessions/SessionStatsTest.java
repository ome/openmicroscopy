/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.utests.sessions;

import java.util.NoSuchElementException;

import ome.security.basic.CurrentDetails;
import ome.server.utests.TestSessionCache;
import ome.services.messages.GlobalMulticaster;
import ome.services.messages.stats.ObjectsReadStatsMessage;
import ome.services.sessions.SessionManager;
import ome.services.sessions.stats.CounterFactory;
import ome.services.sessions.stats.CurrentSessionStats;
import ome.services.sessions.stats.MethodCounter;
import ome.services.sessions.stats.ObjectsReadCounter;
import ome.services.sessions.stats.SessionStats;
import ome.services.sessions.stats.SimpleSessionStats;
import ome.services.sessions.stats.PerSessionStats;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.DefaultResultStub;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
@Test(groups = "sessions")
public class SessionStatsTest extends MockObjectTestCase {

    OmeroContext ctx;
    GlobalMulticaster mc;
    CurrentDetails cd;
    SessionManager sm;
    TestSessionCache cache;
    
    @BeforeMethod
    public void setup() {
        ctx = new OmeroContext(new String[]{"classpath:ome/services/messaging.xml"});
        mc = (GlobalMulticaster) ctx.getBean("applicationEventMulticaster");
        cache = new TestSessionCache(this);
        cd = new CurrentDetails(cache);
    }

    @Test
    public void testStatsReadObjectsResets( ) {
        boolean called[] = readCalled();
        ObjectsReadCounter read = read(2);
        SessionStats stats = new SimpleSessionStats(read, null, null);
        stats.loadedObjects(1);
        assertFalse(called[0]);
        stats.loadedObjects(1);
        assertTrue(called[0]);
        called[0]=false;
        stats.loadedObjects(1);
        assertFalse(called[0]);
        stats.loadedObjects(1);
        assertTrue(called[0]);
    }
    
    @Test
    public void testSimpleStatsReadObjects( ) {
        boolean called[] = readCalled();
        ObjectsReadCounter read = read(1);
        SessionStats stats = new SimpleSessionStats(read, null, null);
        stats.loadedObjects(1);
        assertTrue(called[0]);
    }
    
    @Test
    public void testCurrentStats( ) {
        cd.login(new Principal("u","g","e"));
        boolean[] called = readCalled();
        ObjectsReadCounter read = read(1);
        SessionStats internal = new SimpleSessionStats(read, null, null);
        
        Mock mock = new Mock(SessionManager.class);
        mock.expects(once()).method("getSessionStats").will(returnValue(internal));
        sm = (SessionManager) mock.proxy();
        CurrentSessionStats stats = new CurrentSessionStats(cd, sm);
        
        stats.loadedObjects(1);
        assertTrue(called[0]);
    }
    
    @Test
    public void testThreadLocalStats( ) {
        boolean[] called = readCalled();
        ObjectsReadCounter read = read(1);
        SessionStats internal = new SimpleSessionStats(read, null, new MethodCounter(1));
        
        Mock mock = new Mock(SessionManager.class);
        mock.expects(once()).method("getSessionStats").will(returnValue(internal));
        sm = (SessionManager) mock.proxy();

        PerSessionStats stats = new PerSessionStats(cd);
        cache.setSessionStats(internal);
        // Need to re-login after changing the stats.
        try {
            cd.logout();
        } catch (NoSuchElementException nsee) {
            LoggerFactory.getLogger(this.getClass()).warn("Something logged out?!");
        }
        cd.login(new Principal("u","g","e"));

        stats.loadedObjects(1);
        assertTrue(called[0]);
    }

    @Test(groups = "ticket:2196")
    public void testThreadAndSessionCanShareStats() {
        CounterFactory cf = new CounterFactory();
        SessionStats stats = cf.createStats();
        stats.methodIn();
    }

    // Helpers
    // =========================================================================
    
    private boolean[] readCalled() {
        final boolean called[] = new boolean[]{false};
        mc.addApplicationListener(new ApplicationListener<ApplicationEvent>(){
            public void onApplicationEvent(ApplicationEvent arg0) {
                if (arg0 instanceof ObjectsReadStatsMessage) {
                    called[0] = true;
                }
            }});
        return called;
    }
    
    private EventContext ec() {
        Mock mock = new Mock(EventContext.class);
        mock.setDefaultStub(new DefaultResultStub());
        return (EventContext) mock.proxy();
    }
    

    private ObjectsReadCounter read(int incr) {
        ObjectsReadCounter read = new ObjectsReadCounter(incr);
        read.setApplicationEventPublisher(ctx);
        return read;
    }
}
