/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.sql.Timestamp;
import java.util.HashMap;

import net.sf.ehcache.Cache;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.fire.SessionManagerI;
import ome.services.blitz.test.utests.TestCache;
import ome.services.blitz.util.BlitzConfiguration;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Roles;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.constants.CLIENTUUID;
import omero.model.DetailsI;
import omero.model.PermissionsI;
import omero.util.ObjectFactoryRegistrar;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import Ice.InitializationData;

public class MockFixture {

    public final MockObjectTestCase test;

    public final BlitzConfiguration blitz;
    public final SimpleJdbcTemplate jdbc;
    public final SessionManagerI sm;
    public final SessionManager mgr;
    public final SecuritySystem ss;
    public final OmeroContext ctx;
    public final Executor ex;
    public final Ring ring;

    public static OmeroContext basicContext() {
        return new OmeroContext(new String[] { "classpath:omero/test.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/throttling/throttling.xml",
                "classpath:ome/services/messaging.xml",
                "classpath:ome/services/datalayer.xml",
                "classpath:ome/config.xml"});
    }

    public MockFixture(MockObjectTestCase test) throws Exception {
        this(test, basicContext());
    }

    /**
     * The string arguments sets the {@link SessionManagerI#self} field, which
     * is used in clustering.
     */
    public MockFixture(MockObjectTestCase test, String name) throws Exception {
        this(test, basicContext());
    }

    public MockFixture(MockObjectTestCase test, OmeroContext ctx) {

        this.test = test;
        this.ctx = ctx;
        this.ring = (Ring) ctx.getBean("ring");
        this.ex = (Executor) ctx.getBean("executor");
        this.ss = (SecuritySystem) ctx.getBean("securitySystem");
        this.mgr = (SessionManager) ctx.getBean("sessionManager");
        this.jdbc = (SimpleJdbcTemplate) ctx.getBean("simpleJdbcTemplate");
        
        // --------------------------------------------

        InitializationData id = new InitializationData();
        id.properties = Ice.Util.createProperties();

        //
        // The follow properties are necessary for Gateway
        //

        // Collocation isn't working (but should)
        id.properties.setProperty("Ice.Default.CollocationOptimized", "0");
        // Gateway calls back on the SF and so needs another thread or
        // blocks.
        id.properties.setProperty("Ice.ThreadPool.Client.Size", "2");
        id.properties.setProperty("Ice.ThreadPool.Client.SizeMax", "50");
        id.properties.setProperty("Ice.ThreadPool.Server.Size", "10");
        id.properties.setProperty("Ice.ThreadPool.Server.SizeMax", "100");
        // For testing large calls
        id.properties.setProperty("Ice.MessageSizeMax", "4096");
        // Basic configuration
        id.properties.setProperty("BlitzAdapter.Endpoints","default -h 127.0.0.1");
        // Cluster configuration from etc/internal.cfg
        id.properties.setProperty("Cluster.Endpoints","udp -h 224.0.0.5 -p 10000");
        id.properties.setProperty("ClusterProxy","Cluster:udp -h 224.0.0.5 -p 10000");
        
        blitz = new BlitzConfiguration(id, ring, mgr, ss, ex);
        this.sm = (SessionManagerI) blitz.getBlitzManager();
        // The following is a bit of spring magic so that we can configure
        // the adapter in code. If this can be pushed to BlitzConfiguration
        // for example then we might not need it here anymore.
        HotSwappableTargetSource ts = (HotSwappableTargetSource) ctx
                .getBean("swappableAdapterSource");
        ts.swap(blitz.getBlitzAdapter());
    }

    public void tearDown() {
        this.blitz.destroy();
        // this.ctx.closeAll();
    }

    public OmeroContext getContext() {
        return this.ctx;
    }

    public SessionManagerI getSessionManager() {
        return this.sm;
    }

    public ServiceFactoryPrx createServiceFactory() throws Exception {
        Session s = session();
        Cache cache = cache();
        return createServiceFactory(s, cache, "single-client");
    }

    /**
     * Makes a direct call to SessionManager.create. A call should be made on
     * <em>some</em> fixture to {@link #prepareServiceFactory(Session, Cache)}
     * since the call may be re-routed to a clustered instance.
     */
    public ServiceFactoryPrx createServiceFactory(String name, String client)
            throws Exception {
        return ServiceFactoryPrxHelper.uncheckedCast(sm.create(name, null, this
                .current("create", client)));
    }

    public ServiceFactoryPrx createServiceFactory(Session s) throws Exception {
        Cache cache = cache();
        return createServiceFactory(s, cache, "single-client");
    }

    public ServiceFactoryPrx createServiceFactory(Session s, Cache cache,
            String clientId) throws Exception {
        prepareServiceFactory(s, cache);
        return ServiceFactoryPrxHelper.uncheckedCast(sm.create("name", null,
                this.current("create", clientId)));
    }

    public void prepareServiceFactory(Session s, Cache cache) {
        mock("securityMock").expects(test.once()).method("getSecurityRoles")
                .will(test.returnValue(new Roles()));
        mock("sessionsMock").expects(test.once()).method("create").will(
                test.returnValue(s));
        mock("sessionsMock").expects(test.once()).method("inMemoryCache").will(
                test.returnValue(cache));
        mock("methodMock").expects(test.atLeastOnce()).method("isActive").will(
                test.returnValue(false));
    }

    Ring ring() {
        return blitz.getRing();
    }
    
    public Mock mock(String name) {
        return (Mock) ctx.getBean(name);
    }

    public Cache cache() {
        return new TestCache();
    }

    public Ice.Current current(String method) {
        return current(method, "my-client-uuid");
    }

    public Ice.Current current(String method, String clientId) {
        Ice.Current current = new Ice.Current();
        current.operation = method;
        current.adapter = blitz.getBlitzAdapter();
        current.ctx = new HashMap<String, String>();
        current.ctx.put(CLIENTUUID.value, clientId);
        return current;
    }

    public Session session() {
        Session session = new Session();
        session.setUuid("my-session-uuid");
        session.setStarted(new Timestamp(System.currentTimeMillis()));
        session.setTimeToIdle(0L);
        session.setTimeToLive(0L);
        return session;
    }

    public Mock blitzMock(Class serviceClass) {
        String name = serviceClass.getName();
        name = name.replaceFirst("omero", "ome").replace("PrxHelper", "");
        // WORKAROUND
        if (name.equals("ome.api.RenderingEngine")) {
            name = "omeis.providers.re.RenderingEngine";
        }
        Mock mock = mock("mock-" + name);
        if (mock == null) {
            throw new RuntimeException("No mock for serviceClass");
        }
        return mock;
    }

}
