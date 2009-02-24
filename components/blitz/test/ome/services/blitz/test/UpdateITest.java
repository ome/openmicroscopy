/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;

import java.util.List;

import junit.framework.TestCase;
import ome.logic.HardWiredInterceptor;
import ome.model.annotations.ProjectAnnotationLink;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.impl.QueryI;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.impl.UpdateI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.sessions.SessionManager;
import ome.services.throttling.InThreadThrottlingStrategy;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectI;
import omero.sys.ParametersI;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class UpdateITest extends TestCase {

    ManagedContextFixture user, root;
    ServiceFactoryI user_sf, root_sf;
    UpdateI user_update, root_update;
    QueryI user_query;
    SessionManager sm;
    SecuritySystem ss;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        // Shared
        OmeroContext inner = OmeroContext.getManagedServerContext();
        OmeroContext outer = new OmeroContext(
                new String[] { "classpath:omero/test2.xml" }, false);
        outer.setParent(inner);
        outer.afterPropertiesSet();

        BlitzExecutor be = new InThreadThrottlingStrategy();
        sm = (SessionManager) outer.getBean("sessionManager");
        ss = (SecuritySystem) outer.getBean("securitySystem");

        user = new ManagedContextFixture(outer);
        user_sf = user.createServiceFactoryI();

        // ^^^^^^^^^^^^^^
        // Above all cut-n-pasted from timeline. refactor TODO

        List<HardWiredInterceptor> cptors = HardWiredInterceptor
                .parse(new String[] { "ome.security.basic.BasicSecurityWiring" });
        HardWiredInterceptor.configure(cptors, outer);

        AopContextInitializer initializer = new AopContextInitializer(
                new ServiceFactory(outer), user.login.p);

        ServiceFactory sf = new ServiceFactory(outer);
        user_update = new UpdateI(sf.getUpdateService(), be);
        user_update.setApplicationContext(outer);
        user_update.applyHardWiredInterceptors(cptors, initializer);

        user_query = new QueryI(sf.getQueryService(), be);
        user_query.setApplicationContext(outer);
        user_query.applyHardWiredInterceptors(cptors, initializer);

    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(groups = "ticket:1183")
    public void testProjectWithAnnotationCausesError() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring("ticket:1183"));
        p.linkAnnotation(new CommentAnnotationI());
        p = (Project) assertSaveAndReturn(p);
    }

    public void testQueryWithSelect() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring(""));
        Dataset d = new DatasetI();
        d.setName(rstring(""));
        p.linkDataset(d);
        assertSaveAndReturn(p);
        List<IObject> objects = assertFindByQuery(
                "from Project p join fetch p.datasetLinks pdl "
                        + "join fetch pdl.child ", null);
        for (IObject object : objects) {
            assertTrue(object instanceof Project);
        }
    }
    
    public void testNPEOnMissingQuotes() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring(""));
        assertSaveAndReturn(p);
        List<IObject> objects = assertFindByQuery(
                "from Project p where p.name = foo ", null);
        for (IObject object : objects) {
            assertTrue(object instanceof Project);
        }
    }

    @Test(groups = "ticket:1193")
    public void testNullDetails() throws Exception {
        Project prj = new ProjectI();
        prj.setName(rstring("1193"));
        Annotation a = new CommentAnnotationI();
        prj.linkAnnotation(a);
        assertSaveAndReturn(prj);

        String q = "select pal from ProjectAnnotationLink pal join fetch pal.child";
        Parameters param = new Parameters(new Filter().page(0, 2));
        List<ProjectAnnotationLink> pals = user.managedSf.getQueryService()
                .findAllByQuery(q, param);
        assertNotNull(pals.get(0).getDetails());

        omero.sys.Parameters p = new ParametersI().page(0, 2);
        List<IObject> objects = assertFindByQuery(q, p);
        assertNotNull(objects.get(0).getDetails());
    }

    // Helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private List<IObject> assertFindByQuery(String q, omero.sys.Parameters p)
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

    private IObject assertSaveAndReturn(Project p) throws Exception {
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

    private Ice.Current current(String method) {
        Ice.Current curr = new Ice.Current();
        curr.operation = method;
        return curr;
    }

}
