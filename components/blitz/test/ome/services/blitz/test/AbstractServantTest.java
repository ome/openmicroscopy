/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.impl.AdminI;
import ome.services.blitz.impl.QueryI;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.impl.UpdateI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.sessions.SessionManager;
import ome.services.throttling.InThreadThrottlingStrategy;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.model.IObject;
import omero.model.Pixels;
import omero.model.Project;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public abstract class AbstractServantTest extends TestCase {

    protected ManagedContextFixture user, root;
    protected ServiceFactoryI user_sf, root_sf;
    protected SessionManager sm;
    protected SecuritySystem ss;
    protected BlitzExecutor be;
    protected AopContextInitializer initializer;
    protected ServiceFactory sf;
    protected OmeroContext ctx;
    protected List<HardWiredInterceptor> cptors;
    protected UpdateI user_update;
    protected QueryI user_query;
    protected AdminI user_admin;
    
    protected class RV {
        Exception ex;
        Object rv;
        Object assertPassed() throws Exception{
            if (ex != null) {
                throw ex;
            }
            return rv;
        }
    }
    
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        // Shared
        OmeroContext inner = OmeroContext.getManagedServerContext();
        ctx = new OmeroContext(
                new String[] { "classpath:omero/test2.xml" }, false);
        ctx.setParent(inner);
        ctx.afterPropertiesSet();

        be = new InThreadThrottlingStrategy();
        sm = (SessionManager) ctx.getBean("sessionManager");
        ss = (SecuritySystem) ctx.getBean("securitySystem");

        user = new ManagedContextFixture(ctx);
        user_sf = user.createServiceFactoryI();

        cptors = HardWiredInterceptor
                .parse(new String[] { "ome.security.basic.BasicSecurityWiring" });
        HardWiredInterceptor.configure(cptors, ctx);

        initializer = new AopContextInitializer(
                new ServiceFactory(ctx), user.login.p);

        sf = new ServiceFactory(ctx);
        user_update = new UpdateI(sf.getUpdateService(), be);
        user_query = new QueryI(sf.getQueryService(), be);
        user_admin = new AdminI(sf.getAdminService(), be);
        
        configure(user_update);
        configure(user_query);
        configure(user_admin);
    }
    
    
    protected void configure(AbstractAmdServant servant) {
        servant.setApplicationContext(ctx);
        servant.applyHardWiredInterceptors(cptors, initializer);
    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    protected List<IObject> assertFindByQuery(String q, omero.sys.Parameters p)
            throws Exception {
        final Exception[] ex = new Exception[1];
        final boolean[] status = new boolean[1];
        final List[] rv = new List[1];
        user_query.findAllByQuery_async(new AMD_IQuery_findAllByQuery() {

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

    protected IObject assertSaveAndReturn(Project p) throws Exception {
        final Exception[] ex = new Exception[1];
        final boolean[] status = new boolean[1];
        final IObject[] rv = new IObject[1];
        user_update.saveAndReturnObject_async(
                new AMD_IUpdate_saveAndReturnObject() {

                    public void ice_exception(Exception exc) {
                        ex[0] = exc;
                    }

                    public void ice_response(IObject __ret) {
                        rv[0] = __ret;
                        status[0] = true;
                    }
                }, p, current("saveAndReturnObject"));
        if (ex[0] != null) {
            throw ex[0];
        } else {
            assertTrue(status[0]);
        }
        return rv[0];
    }

    protected Ice.Current current(String method) {
        Ice.Current curr = new Ice.Current();
        curr.operation = method;
        return curr;
    }


    protected long makePixels() throws Exception, FileNotFoundException {
        throw new RuntimeException("Unforunately MockedOMEROImportFixture is not supported here \n" +
        		"Instead, the service factory must be registered with a communicator \n" +
        		"and that proxy given to the OMEROImportFixture");
        /*
        ServiceFactoryPrx sf = this.user.
        ServiceFactory _sf = new InterceptingServiceFactory(this.sf, user.login);
        
        MockedOMEROImportFixture fixture = new MockedOMEROImportFixture(_sf, "");
        List<Pixels> list = fixture.fullImport(ResourceUtils
                .getFile("classpath:tinyTest.d3d.dv"), "tinyTest");
        long pixels = list.get(0).getId().getValue();
        return pixels;
        */
    }

}
