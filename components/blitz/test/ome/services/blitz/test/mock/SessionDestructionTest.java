/*
 *   $Id$
 *
 *   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.CacheManager;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.basic.CurrentDetails;
import ome.services.ThumbnailBean;
import ome.services.messages.DestroySessionMessage;
import ome.services.messages.GlobalMulticaster;
import ome.services.sessions.SessionManagerImpl;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.sessions.state.SessionCache;
import ome.services.util.Executor;
import ome.system.Roles;
import omero.api.ServiceFactoryPrx;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Creates sessions and tests the various ways they can be destroyed. Initially
 * (Oct. 2008) this was used to manually inspect in a profiler whether or not
 * all related instances were being properly cleaned up.
 */
public class SessionDestructionTest extends MockObjectTestCase {

    MockFixture fixture;

    @AfterMethod(groups = "integration")
    public void shutdownFixture() {
        if (fixture != null) {
            fixture.tearDown();
        }
    }

    @Test(groups = "integration")
    public void testOneSessionGlacierDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.closeOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        sf.destroy();

    }

    @Test(groups = "integration")
    public void testOneSessionNotificationDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.closeOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));

    }

    @Test(groups = "integration")
    public void testOneSessionDetachNotificationDestruction() throws Exception {
        fixture = new MockFixture(this);
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.detachOnDestroy();
        sf.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));

    }

    @Test(groups = "integration")
    public void testTwoClientsSessionGlacierDestruction() throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.closeOnDestroy();
        sf2.closeOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(1));
        sf1.destroy();
        fixture.mock("sessionsMock").expects(once()).method("detach").will(
                returnValue(0));
        fixture.mock("sessionsMock").expects(once()).method("close").will(
                returnValue(-2));
        sf2.destroy();
    }

    @Test(groups = "integration")
    public void testTwoClientsSessionNotificationDestruction() throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.closeOnDestroy();
        sf2.closeOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));
    }

    @Test(groups = "integration")
    public void testTwoClientsSessionDetachedNotificationDestruction()
            throws Exception {

        fixture = new MockFixture(this);
        Session s = fixture.session();
        net.sf.ehcache.Cache c = fixture.cache();
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s, c, "sf1");
        ServiceFactoryPrx sf2 = fixture.createServiceFactory(s, c, "sf2");
        sf1.detachOnDestroy();
        sf2.detachOnDestroy();
        sf1.getAdminService();
        sf2.getAdminService();

        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));
    }

    @Test(groups = "integration")
    public void testSessionClosesStatefulService() throws Exception {
        fixture = new MockFixture(this);
        Session s = fixture.session();
        ServiceFactoryPrx sf = fixture.createServiceFactory();
        sf.createThumbnailStore();

        //
        // Here we want to set up a proxy around a real ThumbnailBean
        // rather than a mock, in order to test that IceMethodInvoker
        // unwraps proxies.
        //
        HotSwappableTargetSource swap = (HotSwappableTargetSource)
            fixture.getContext().getBean("swappable-ome.api.ThumbnailStore");
        final boolean called[] = new boolean[]{false};
        ThumbnailBean bean = new ThumbnailBean() {
            public void close() {
                called[0] = true;
            }
        };
        ProxyFactory proxy = new ProxyFactory(bean);
	/* With #1106 bltiz migration destroy has been removed.
        proxy.addAdvice(new MethodInterceptor(){
            public Object invoke(MethodInvocation arg0) throws Throwable {
                if (arg0.getMethod().getName().equals("destroy")) {
                    fail("Should not be called");
                }
                return null;
            }

        });
	*/
        swap.swap(proxy.getProxy());

        fixture.getSessionManager().onApplicationEvent(
                new DestroySessionMessage(this, "my-session-uuid"));

        assertTrue(called[0]);
    }

    @Test(groups = "integration")
    public void testTwoSessionsClosedConcurrently() throws Exception {
        fail("NYI");
    }

    @Test(groups = "integration")
    @SuppressWarnings( { "unchecked" })
    public void testSessionTimeoutWithRealSessionCache() throws Exception {

        // Manual configuration of some lower-level objects
        fixture = new MockFixture(this);
        SessionCache cache = new SessionCache();
        cache.setApplicationContext(fixture.getContext());
        cache.setCacheManager(CacheManager.getInstance());
        cache.setUpdateInterval(1000L); // Every second check for chanages
        SessionManagerImpl manager = new SessionManagerImpl();
        manager.setApplicationContext(fixture.getContext());
        manager
                .setExecutor((Executor) fixture.getContext()
                        .getBean("executor"));
        manager.setRoles(new Roles());
        manager.setDefaultTimeToLive(1000L); // Only lives one second.
        manager.setSessionCache(cache);
        manager.setPrincipalHolder(new CurrentDetails());

        // Now insert that into our context
        HotSwappableTargetSource ts = (HotSwappableTargetSource) fixture
                .getContext().getBean("swappableSessionManagerSource");
        ts.swap(manager);

        // We also want to receive the callback about session destruction
        class Listener implements ApplicationListener {
            List<String> killed = new ArrayList<String>();

            public void onApplicationEvent(ApplicationEvent arg0) {
                if (arg0 instanceof DestroySessionMessage) {
                    DestroySessionMessage dsm = (DestroySessionMessage) arg0;
                    killed.add(dsm.getSessionId());
                }
            }
        }
        Listener listener = new Listener();
        ((GlobalMulticaster) fixture.getContext().getBean(
                "applicationEventMulticaster"))
                .addApplicationListener(listener);

        // Setup all the necessary expectations
        Experimenter exp = new Experimenter("joe", "joe", "blow", false);
        ExperimenterGroup grp = new ExperimenterGroup("cool", false);
        List<Long> grps = Arrays.asList(0L, 1L, 2L);
        List rv = new ArrayList();
        rv.add(exp);
        rv.add(grp);
        rv.add(grps);
        rv.add(grps);
        rv.add(Arrays.asList("user", "system", "cool"));

        Session s = fixture.session();
        s.setTimeToLive(1000L); // Actually have to set here because of mocking.
        Mock ex = fixture.mock("executorMock");
        ex.expects(atLeastOnce()).method("execute").will(
                onConsecutiveCalls(returnValue(new ExperimenterGroup("group", false)),
                        returnValue(s), returnValue(rv)));

        // Now create the session and wait for it to time out.
        ServiceFactoryPrx sf1 = fixture.createServiceFactory(s);

        long start = System.currentTimeMillis();
        ex.expects(atLeastOnce()).method("execute").will(returnValue(rv));
        while (System.currentTimeMillis() < (start + 1000L)) {
            Thread.sleep(500L);
            cache.updateEvent(new UserGroupUpdateEvent(this));
        }
        assertTrue(s.getUuid(), listener.killed.contains(s.getUuid()));

    }

}
