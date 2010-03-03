/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;
import ome.formats.MockedOMEROImportFixture;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.impl.AdminI;
import ome.services.blitz.impl.ConfigI;
import ome.services.blitz.impl.QueryI;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.impl.UpdateI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.sessions.SessionManager;
import ome.services.throttling.InThreadThrottlingStrategy;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import omero.api.AMD_IAdmin_getEventContext;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.model.IObject;
import omero.model.Pixels;
import omero.sys.EventContext;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public abstract class AbstractServantTest extends TestCase {

    protected ManagedContextFixture user, root;
    protected SessionManager sm;
    protected SecuritySystem ss;
    protected BlitzExecutor be;
    protected ServiceFactory sf;
    protected OmeroContext ctx;
    protected List<HardWiredInterceptor> cptors;
    protected ServiceFactoryI user_sf, root_sf;
    protected AopContextInitializer user_initializer, root_initializer;
    protected UpdateI user_update, root_update;
    protected QueryI user_query, root_query;
    protected AdminI user_admin, root_admin;
    protected ConfigI user_config, root_config;

    public class RV {
        public Exception ex;
        public Object rv;

        public Object assertPassed() throws Exception {
            if (ex != null) {
                throw ex;
            }
            return rv;
        }
    }

    @Override
    protected void setUp() throws Exception {

        // Shared
        OmeroContext inner = OmeroContext.getManagedServerContext();
        ctx = new OmeroContext(new String[] { "classpath:omero/test2.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml", // geomTool
                "classpath:ome/services/messaging.xml" // Notify geomTool
        }, false);
        ctx.setParent(inner);
        ctx.afterPropertiesSet();

        sf = new ServiceFactory(ctx);
        be = new InThreadThrottlingStrategy();
        sm = (SessionManager) ctx.getBean("sessionManager");
        ss = (SecuritySystem) ctx.getBean("securitySystem");

        cptors = HardWiredInterceptor
                .parse(new String[] { "ome.security.basic.BasicSecurityWiring" });
        HardWiredInterceptor.configure(cptors, ctx);

        user = new ManagedContextFixture(ctx);
        user_sf = user.createServiceFactoryI();
        user_initializer = new AopContextInitializer(new ServiceFactory(ctx),
                user.login.p);

        user_update = new UpdateI(sf.getUpdateService(), be);
        user_query = new QueryI(sf.getQueryService(), be);
        user_admin = new AdminI(sf.getAdminService(), be);
        user_config = new ConfigI(sf.getConfigService(), be);
        configure(user_update, user_initializer);
        configure(user_query, user_initializer);
        configure(user_admin, user_initializer);
        configure(user_config, user_initializer);

        root = new ManagedContextFixture(ctx);
        root.setCurrentUserAndGroup("root", "system");
        root_sf = root.createServiceFactoryI();
        root_initializer = new AopContextInitializer(new ServiceFactory(ctx),
                root.login.p);

        root_update = new UpdateI(sf.getUpdateService(), be);
        root_query = new QueryI(sf.getQueryService(), be);
        root_admin = new AdminI(sf.getAdminService(), be);
        root_config = new ConfigI(sf.getConfigService(), be);
        configure(root_update, root_initializer);
        configure(root_query, root_initializer);
        configure(root_admin, root_initializer);
        configure(root_config, root_initializer);
    }

    protected void configure(AbstractAmdServant servant,
            AopContextInitializer ini) {
        servant.setApplicationContext(ctx);
        servant.applyHardWiredInterceptors(cptors, ini);
    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    protected List<IObject> assertFindByQuery(String q, omero.sys.Parameters p)
            throws Exception {
        return assertFindByQuery(user_query, q, p);
    }

    @SuppressWarnings("unchecked")
    protected List<IObject> assertFindByQuery(QueryI query, String q,
            omero.sys.Parameters p) throws Exception {

        final Exception[] ex = new Exception[1];
        final boolean[] status = new boolean[1];
        final List[] rv = new List[1];
        query.findAllByQuery_async(new AMD_IQuery_findAllByQuery() {

            public void ice_exception(Exception exc) {
                ex[0] = exc;
            }

            public void ice_response(List<IObject> __ret) {
                rv[0] = __ret;
                status[0] = true;
            }
        }, q, p, current("findAllByQuery"));
        if (ex[0] != null) {
            throw ex[0];
        } else {
            assertTrue(status[0]);
        }
        return rv[0];
    }

    protected <T extends IObject> T assertSaveAndReturn(T t) throws Exception {
        return assertSaveAndReturn(user_update, t);
    }

    protected <T extends IObject> T assertSaveAndReturn(UpdateI up, T t)
            throws Exception {
        final RV rv = new RV();
        up.saveAndReturnObject_async(new AMD_IUpdate_saveAndReturnObject() {

            public void ice_exception(Exception exc) {
                rv.ex = exc;
            }

            public void ice_response(IObject __ret) {
                rv.rv = __ret;
            }
        }, t, current("saveAndReturnObject"));
        rv.assertPassed();
        return (T) rv.rv;
    }

    protected EventContext assertEventContext(AdminI admin)
            throws Exception {
        final RV rv = new RV();
        admin.getEventContext_async(new AMD_IAdmin_getEventContext() {

            public void ice_exception(Exception exc) {
                rv.ex = exc;
            }

            public void ice_response(EventContext __ret) {
                rv.rv = __ret;
            }
        }, current("getEventContext"));
        rv.assertPassed();
        return (EventContext) rv.rv;
    }

    protected Ice.Current current(String method) {
        Ice.Current curr = new Ice.Current();
        curr.operation = method;
        return curr;
    }

    protected long makePixels() throws Exception, FileNotFoundException {
        if (false) {
            throw new RuntimeException(
                    "Unforunately MockedOMEROImportFixture is not supported here \n"
                            + "Instead, the service factory must be registered with a communicator \n"
                            + "and that proxy given to the OMEROImportFixture");
        } else {
            long pixels = -1;
            ServiceFactory _sf = new InterceptingServiceFactory(this.sf, user.login);

            MockedOMEROImportFixture fixture = new MockedOMEROImportFixture(_sf, "");
            List<Pixels> list = fixture.fullImport(ResourceUtils
                    .getFile("classpath:tinyTest.d3d.dv"), "tinyTest");
            pixels = list.get(0).getId().getValue();
            return pixels;
        }
	
    }
}
