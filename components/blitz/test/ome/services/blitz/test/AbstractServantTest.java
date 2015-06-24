/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
import ome.services.blitz.impl.ShareI;
import ome.services.blitz.impl.UpdateI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.scheduler.ThreadPool;
import ome.services.sessions.SessionManager;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import omero.RType;
import omero.api.AMD_IAdmin_getEventContext;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IQuery_projection;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.model.IObject;
import omero.model.Pixels;
import omero.sys.EventContext;
import omero.util.TempFileManager;

import org.apache.commons.io.IOUtils;
import org.jmock.MockObjectTestCase;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public abstract class AbstractServantTest extends MockObjectTestCase {

    private final static AtomicReference<File> tinyHolder =
        new AtomicReference<File>();

    protected ManagedContextFixture user, root;
    protected OmeroContext ctx;
    protected File omeroDataDir;

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

        // ticket:#6417
        omeroDataDir = TempFileManager.create_path(".omeroDataDir", "test", true);
        System.setProperty("omero.data.dir", omeroDataDir.getAbsolutePath());

        // Shared
        OmeroContext inner = OmeroContext.getManagedServerContext();
        ctx = new OmeroContext(new String[] { "classpath:omero/test2.xml",
                "classpath:ome/services/messaging.xml",
                "classpath:ome/services/spec.xml", // for DeleteI
                "classpath:ome/config.xml", // for ${} in servantDefs.
                "classpath:ome/services/throttling/throttling.xml"
        }, false);
        ctx.setParent(inner);
        ctx.afterPropertiesSet();

        user = new ManagedContextFixture(ctx);
        root = new ManagedContextFixture(ctx);
    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    protected List<IObject> assertFindByQuery(String q, omero.sys.Parameters p)
            throws Exception {
        return assertFindByQuery(user.query, q, p);
    }

    @SuppressWarnings("unchecked")
    protected List<List<RType>> assertProjection(String q, omero.sys.Parameters p)
            throws Exception {
        return assertProjection(user.query, q, p);
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

    @SuppressWarnings("unchecked")
    protected List<List<RType>> assertProjection(QueryI query, String q,
            omero.sys.Parameters p) throws Exception {

        final RV rv = new RV();
        query.projection_async(new AMD_IQuery_projection() {

            public void ice_exception(Exception exc) {
                rv.ex = exc;
            }

            @SuppressWarnings("rawtypes")
            public void ice_response(List __ret) {
                rv.rv = __ret;
            }
        }, q, p, current("projection"));
        rv.assertPassed();
        return (List<List<RType>>) rv.rv;
    }

    protected <T extends IObject> T assertSaveAndReturn(T t) throws Exception {
        return assertSaveAndReturn(user.update, t);
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

            MockedOMEROImportFixture fixture = new MockedOMEROImportFixture(
                    user.managedSf, "");
            File tinyTest = getTinyFileName();
            List<Pixels> list = fixture.fullImport(tinyTest, "tinyTest");
            pixels = list.get(0).getId().getValue();
            return pixels;
        }
    }

    protected long makeImage() throws Exception, FileNotFoundException {
        long pixels = makePixels();
        //ServiceFactory sf = new InterceptingServiceFactory(this.sf, user.login);
        //return sf.getQueryService().findByQuery("select i from Image i join i.pixels p " +
		//	"where p.id = " + pixels, null).getId();
        return pixels;
    }

    /**
     * Since in some cases the tinyTest.d3d.dv file is in a jar and
     * not a regular file, we may need to copy it to a temporary file
     * which gets destroyed
     * @return
     */
    protected File getTinyFileName() throws IOException {
        File f = tinyHolder.get();
        if (f == null) {
            String tt = "classpath:tinyTest.d3d.dv";
            try {
                f = ResourceUtils.getFile(tt);
                tinyHolder.compareAndSet(null, f);
            } catch (FileNotFoundException fnfe) {
                URL url = ResourceUtils.getURL(tt);
                InputStream is = url.openStream();
                f = File.createTempFile("tinyTest", ".dv");
                FileOutputStream fos = new FileOutputStream(f);
                IOUtils.copy(is, fos);
                fos.close();
                if (tinyHolder.compareAndSet(null, f)) {
                    f.deleteOnExit();
                } else {
                    // Value was updated in another thread.
                    f.delete();
                    f = tinyHolder.get();
                }
            }
        }
        return f;
    }
}
