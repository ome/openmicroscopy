/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.fixtures;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Ehcache;
import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawPixelsStore;
import ome.model.meta.Session;
import ome.services.blitz.Main;
import ome.services.blitz.Router;
import ome.services.sessions.SessionContext;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.state.CacheFactory;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.tools.spring.ManagedServiceFactory;
import omeis.providers.re.RenderingEngine;
import omero.api.ServiceFactoryPrx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.util.ResourceUtils;

/**
 * Note: Using the {@link Router} wrapper class can cause processes to be
 * orphaned on the OS.
 */
public class BlitzServerFixture extends MockObjectTestCase {

    private static final Log log = LogFactory.getLog(BlitzServerFixture.class);

    private final Map<String, Object> STUBS = new HashMap<String, Object>();

    private final Map<String, Mock> MOCKS = new HashMap<String, Mock>();

    protected Session session;
    protected SessionContext sc;

    protected Ehcache cache;

    // Name used to look up the test context.
    protected static String DEFAULT = "OMERO.blitz.test";
    protected String name;
    protected Thread t;
    protected Main m;
    protected Router r;
    protected omero.client ice;
    protected OmeroContext ctx;

    int sessionTimeout;
    int serviceTimeout;

    // Keys for the mocks that are known to be needed
    final static String adm = "internal:ome.api.IAdmin",
            qu = "internal:ome.api.IQuery", up = "internal:ome.api.IUpdate",
            ss = "securitySystem",
            re = "managed:omeis.providers.re.RenderingEngine",
            px = "internal:ome.api.RawPixelsStore", ms = "methodSecurity",
            sm = "sessionManager";

    public BlitzServerFixture() {
        this(DEFAULT, 30, 10);
    }

    public BlitzServerFixture(int sessionTimeout, int serviceTimeout) {
        this(DEFAULT, sessionTimeout, serviceTimeout);

    }

    public BlitzServerFixture(String contextName) {
        this(contextName, 30, 10);
    }

    /** It is important to have the timeouts set before context creation */
    public BlitzServerFixture(String contextName, int sessionTimeout,
            int serviceTimeout) {

        this.name = contextName;
        this.serviceTimeout = serviceTimeout;
        this.sessionTimeout = sessionTimeout;

        // Set property before the OmeroContext is created
        System.setProperty("omero.blitz.cache.timeToIdle", "" + serviceTimeout);

        ctx = OmeroContext.getInstance(name);
        ctx.close();
        startServer();

        ManagedServiceFactory stubs = new ManagedServiceFactory();
        stubs.setApplicationContext(ctx);
        MockServiceFactory mocks = new MockServiceFactory();
        mocks.setApplicationContext(ctx);

        Mock mock;
        Object stub;

        mock = mocks.getMockByClass(IAdmin.class);
        stub = stubs.getAdminService();
        addMock(adm, mock, stub);

        mock = mocks.getMockByClass(IQuery.class);
        stub = stubs.getQueryService();
        addMock(qu, mock, stub);

        mock = mocks.getMockByClass(IUpdate.class);
        stub = stubs.getUpdateService();
        addMock(up, mock, stub);

        mock = mocks.getMockByClass(RenderingEngine.class);
        stub = stubs.createRenderingEngine();
        addMock(re, mock, stub);

        mock = mocks.getMockByClass(RawPixelsStore.class);
        stub = stubs.createRawPixelsStore();
        addMock(px, mock, stub);

        mock = (Mock) ctx.getBean("methodMock");
        stub = ctx.getBean("methodSecurity");
        addMock(ms, mock, stub);

        mock = (Mock) ctx.getBean("securityMock");
        stub = ctx.getBean("securitySystem");
        addMock(ss, mock, stub);

        mock = (Mock) ctx.getBean("sessionsMock");
        stub = ctx.getBean("sessionManager");
        addMock(sm, mock, stub);

    }

    private void startServer() {

        m = new Main(name);
        t = new Thread(m);
        r = new Router();
        r.setTimeout(sessionTimeout);
        r.allowAdministration();
        m.setRouter(r);
        t.start();
        assertTrue("Startup must succeed", m.waitForStartup());

        log
                .warn("\n"
                        + "============================================================\n"
                        + "Use ONLY Ctrl-C or 'q' in the console to cancel this process\n"
                        + "============================================================");
    }

    public void prepareLogin() {
        session = new Session("uuid", new Long(0), new Long(0), null, "rw----",
                "Test");
        sc = new SessionContextImpl(session, Collections.singletonList(1L),
                Collections.singletonList(1L), Collections
                        .singletonList("user"));
        CacheFactory factory = new CacheFactory();
        factory.setBeanName("blitz.fixture");
        factory.setOverflowToDisk(false);
        cache = factory.createCache();
        getMock(sm).expects(once()).method("executePasswordCheck").will(
                returnValue(true));
        getMock(sm).expects(once()).method("create").will(returnValue(session));
        getMock(sm).expects(once()).method("inMemoryCache").will(
                returnValue(cache));
        getMock(adm).expects(once()).method("checkPassword").will(
                returnValue(true));
        getMock(ss).expects(once()).method("getSecurityRoles").will(
                returnValue(new Roles()));
    }

    public ServiceFactoryPrx createSession() throws Exception {
        prepareLogin();
        File f1 = ResourceUtils.getFile("classpath:ice.config");
        File f2 = ResourceUtils.getFile("classpath:local.properties");
        ice = new omero.client(f1, f2);
        ServiceFactoryPrx session = ice.createSession(null, null);
        return session;
    }

    public void methodCall() throws Exception {
        getMock(sm).expects(once()).method("getEventContext").will(
                returnValue(sc));
        getMock(ms).expects(once()).method("isActive").will(returnValue(true));
        getMock(ms).expects(once()).method("checkMethod");
        getMock(ss).expects(once()).method("login");
        getMock(ss).expects(once()).method("logout").will(returnValue(0));
        getMock(re).expects(once()).method("close");
    }

    public void destroySession() throws Exception {
        ice.closeSession();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            m.setStop();
            try {
                Thread.sleep(2L);
            } catch (InterruptedException ie) {
                // ok
            }
            m.shutdown();
        }
    }

    public OmeroContext getContext() {
        return ctx;
    }

    // MOCK MANAGEMENT

    private void addMock(String name, Mock mock, Object proxy) {
        if (MOCKS.containsKey(name)) {
            throw new RuntimeException(name + " already exists.");
        }
        MOCKS.put(name, mock);
        STUBS.put(name, proxy);
    }

    public Mock getMock(String name) {
        return MOCKS.get(name);
    }

    public Object getStub(String name) {
        return STUBS.get(name);
    }

    public Mock getAdmin() {
        return MOCKS.get(adm);
    }

    public Mock getQuery() {
        return MOCKS.get(qu);
    }

    public Mock getUpdate() {
        return MOCKS.get(up);
    }

    public Mock getSecSystem() {
        return MOCKS.get(ss);
    }

    public Mock getRndEngine() {
        return MOCKS.get(re);
    }

    public Mock getPixelsStore() {
        return MOCKS.get(px);
    }

    public Mock getMethodSecurity() {
        return MOCKS.get(ms);
    }

    public Mock getSessionManager() {
        return MOCKS.get(sm);
    }
}

class MockServiceFactory extends ManagedServiceFactory {

    @Override
    protected String getPrefix() {
        return "mock:";
    }

    public Mock getMockByClass(Class klass) {
        return (Mock) ctx.getBean(getPrefix() + klass.getName());
    }
}