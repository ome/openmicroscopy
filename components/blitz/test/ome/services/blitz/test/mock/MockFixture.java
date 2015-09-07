/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.mock;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.Cache;

import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.fire.SessionManagerI;
import ome.services.blitz.fire.TopicManager;
import ome.services.blitz.test.utests.TestCache;
import ome.services.blitz.util.BlitzConfiguration;
import ome.services.roi.RoiTypes;
import ome.services.scheduler.SchedulerFactoryBean;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.util.SqlAction;
import omero.rtypes;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.constants.CLIENTUUID;
import omero.util.ModelObjectFactoryRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import Glacier2.AMD_Router_createSession;
import Glacier2.AMD_Router_createSessionFromSecureConnection;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import Glacier2.SessionNotExistException;
import Glacier2.SessionPrx;
import Ice.Current;
import Ice.InitializationData;
import Ice.ObjectPrx;

public class MockFixture {

    public final MockObjectTestCase test;

    public final SchedulerFactoryBean scheduler;
    public final BlitzConfiguration blitz;
    public final SessionManagerI sm;
    public final SessionManager mgr;
    public final SecuritySystem ss;
    public final OmeroContext ctx;
    public final Executor ex;
    public final Ring ring;
    public final String router;
    public final SqlAction sql;

    public static OmeroContext basicContext() {
        return new OmeroContext(new String[] {
                "classpath:omero/test.xml",
                "classpath:ome/config.xml",
                "classpath:ome/services/datalayer.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/blitz-graph-rules.xml",
                "classpath:ome/services/throttling/throttling.xml",
                "classpath:ome/services/messaging.xml",
                // Following 2 required by GeomTool deps.
                "classpath:ome/services/service-ome.io.nio.PixelsService.xml",
                "classpath:ome/services/service-ome.io.nio.OriginalFilesService.xml"});
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
        this.sql = (SqlAction) ctx.getBean("simpleSqlAction");

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
        id.properties.setProperty("BlitzAdapter.Endpoints",
                "default -h 127.0.0.1");
        // Cluster configuration from etc/internal.cfg
        id.properties.setProperty("Cluster.Endpoints",
                "udp -h 224.0.0.5 -p 10000");
        id.properties.setProperty("ClusterProxy",
                "Cluster:udp -h 224.0.0.5 -p 10000");

        /*
        Node node = new Node();
        this.mock("executorMock").expects(test.once()).method("execute").will(test.returnValue(node));
        this.mock("executorMock").expects(test.once()).method("execute").will(test.returnValue(true));
        this.mock("executorMock").expects(test.once()).method("execute").will(test.returnValue(Collections.EMPTY_LIST));
        */
        blitz = new BlitzConfiguration(id, ring, mgr, ss, ex, 10000);
        this.sm = (SessionManagerI) blitz.getBlitzManager();
        this.sm.setApplicationContext(ctx);
        this.ctx.addApplicationListener(this.sm);

        // Reproducing the logic of initializing the ObjectFactories
        // which typically happens within Spring
        new rtypes.RTypeObjectFactoryRegistry().setIceCommunicator(blitz.getCommunicator());
        new RoiTypes.RoiTypesObjectFactoryRegistry().setIceCommunicator(blitz.getCommunicator());
        new ModelObjectFactoryRegistry().setIceCommunicator(blitz.getCommunicator());

        /* UNUSED
        // The following is a bit of spring magic so that we can configure
        // the adapter in code. If this can be pushed to BlitzConfiguration
        // for example then we might not need it here anymore.
        HotSwappableTargetSource ts = (HotSwappableTargetSource) ctx
                .getBean("swappableAdapterSource");
        ts.swap(blitz.getBlitzAdapter());
        */

        // Add our topic manager
        TopicManager tm = new TopicManager.Impl(blitz.getCommunicator());
        this.ctx.addApplicationListener(tm);

        // Setup mock router which allows us to use omero.client
        // rather than solely ServiceFactoryProxies, though it is
        // still necessary to call the proper mock methods.
        Ice.ObjectPrx prx = blitz.getBlitzAdapter().add(
                new MockRouter(this.sm),
                Ice.Util.stringToIdentity("OMERO.Glacier2/router"));
        router = "OMERO.Glacier2/router:"
                + prx.ice_getEndpoints()[0]._toString();

        // Finally, starting a scheduler to act like a real
        // server
        try {
            MethodInvokingJobDetailFactoryBean runBeats = new MethodInvokingJobDetailFactoryBean();
            runBeats.setBeanName("runBeats");
            runBeats.setTargetMethod("requestHeartBeats");
            runBeats.setTargetObject(blitz.getBlitzManager());
            runBeats.afterPropertiesSet();
            CronTriggerBean triggerBeats = new CronTriggerBean();
            triggerBeats.setBeanName("triggerBeats");
            triggerBeats.setJobDetail((JobDetail) runBeats.getObject());
            triggerBeats.setCronExpression("0-59/5 * * * * ?");
            triggerBeats.afterPropertiesSet();
            scheduler = new SchedulerFactoryBean();
            scheduler.setApplicationContext(ctx);
            scheduler.setTriggers(new Trigger[] { triggerBeats });
            scheduler.afterPropertiesSet();
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public omero.client newClient() {
        Properties p = new Properties();
        p.setProperty("Ice.Default.Router", router);
        omero.client client = new omero.client(p);
        return client;
    }

    public void tearDown() {
        mock("executorMock").expects(test.once()).method("execute")
            .will(test.returnValue(0));
        this.blitz.destroy();
        scheduler.stop();
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
        mock("sessionsMock").expects(test.once()).method("find")
                .will(test.returnValue(s));
        mock("securityMock").expects(test.once()).method("getSecurityRoles")
                .will(test.returnValue(new Roles()));
        mock("sessionsMock").expects(test.once()).method("create").will(
                test.returnValue(s));
        mock("sessionsMock").expects(test.once()).method("inMemoryCache").will(
                test.returnValue(cache));
        mock("methodMock").expects(test.atLeastOnce()).method("isActive").will(
                test.returnValue(false));
    }

    public void prepareClose(int referenceCount) {
        mock("sessionsMock").expects(test.once()).method("detach").will(
                test.returnValue(referenceCount));
        if (referenceCount < 1) {
            mock("sessionsMock").expects(test.once()).method("close").will(
                    test.returnValue(-2));
        }
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
        return session("my-session-uuid");
    }

    public Session session(String uuid) {
        Session session = new Session();
        session.setUuid(uuid);
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

    public static class MockRouter extends Glacier2._RouterDisp {

        private final static Logger log = LoggerFactory.getLogger(MockRouter.class);

        private final SessionManagerI sm;

        private final Map<Ice.Connection, SessionPrx> sessionByConnection = new HashMap<Ice.Connection, SessionPrx>();

        public MockRouter(SessionManagerI sm) {
            this.sm = sm;
        }

        public void createSessionFromSecureConnection_async(
                AMD_Router_createSessionFromSecureConnection arg0, Current arg1)
                throws CannotCreateSessionException, PermissionDeniedException {
            arg0.ice_exception(new UnsupportedOperationException());
        }

        public void createSession_async(AMD_Router_createSession arg0,
                String arg1, String arg2, Current arg3)
                throws CannotCreateSessionException, PermissionDeniedException {
            try {
                SessionPrx prx = sm.create(arg1, null, arg3);
                sessionByConnection.put(arg3.con, prx);
                log.info(String.format("Storing %s under %s", prx, arg3.con));
                arg0.ice_response(prx);
            } catch (Exception e) {
                arg0.ice_exception(e);
            }

        }

        public void destroySession(Current arg0)
                throws SessionNotExistException {
            SessionPrx prx = sessionByConnection.get(arg0.con);
            if (prx != null) {
                log.info("Destroying " + prx);
                prx.destroy();
            }
        }

        public String getCategoryForClient(Current arg0) {
            return sessionByConnection.get(arg0.con).ice_id();
        }

        public long getSessionTimeout(Current arg0) {
            throw new UnsupportedOperationException();
        }

        public ObjectPrx[] addProxies(ObjectPrx[] arg0, Current arg1) {
            log.warn("addProxies called with " + Arrays.deepToString(arg0));
            return null;
        }

        @SuppressWarnings("deprecation")
        public void addProxy(ObjectPrx arg0, Current arg1) {
            log.warn("addProxy called with " + arg0);
        }

        public ObjectPrx getClientProxy(Current arg0) {
            return null;
        }

        public ObjectPrx getServerProxy(Current arg0) {
            return sessionByConnection.get(arg0.con);
        }

        public void refreshSession(Ice.Current current) throws Glacier2.SessionNotExistException {
            throw new UnsupportedOperationException();
        }

    }

}
