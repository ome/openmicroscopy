/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.services.blitz.impl.DeleteHandleI;
import ome.services.delete.BaseDeleteSpec;
import ome.services.delete.DeleteSpecFactory;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IDelete_queueDelete;
import omero.api.IDeletePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Project;
import omero.model.ProjectI;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.Stub;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests call to {@link IDeletePrx}, especially important for testing the
 * {@link IDeletePrx#queueDelete(omero.api.delete.DeleteCommand[]) since it is
 * not available from {@link ome.api.IDelete}
 */
@Test(groups = { "integration", "delete" })
public class DeleteITest extends AbstractServantTest {

    Mock adapterMock;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
        adapterMock = (Mock) user.ctx.getBean("adapterMock");
        adapterMock.setDefaultStub(new FakeAdapter());
    }

    /**
     * Demonstrates a simple usage. No intention of showing validity, but can be
     * given as an example to people.
     */
    public void testBasicUsageOfQueueDelete() throws Exception {
        long imageId = makeImage();
        DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
        DeleteHandleI handle = doDelete(dc);
        assertEquals(dc, handle.commands()[0]);
        assertEquals(0, handle.errors());
        block(handle, 5, 1000);
        assertFalse(handle.cancel());
    }

    /**
     * Uses the /Image/Pixels/Channel delete specification to remove the
     * channels added during {@link #makeImage()} and tests that the channels
     * are gone afterwards.
     */
    public void testDeleteChannels() throws Exception {

        // Create test data
        long imageId = makeImage();
        List<List<RType>> channelIds = assertProjection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        assertTrue(channelIds.size() > 0);

        // Perform delete
        DeleteCommand dc = new DeleteCommand("/Image/Pixels/Channel", imageId,
                null);
        doDelete(dc);

        // Check that data is gone
        channelIds = assertProjection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        assertEquals(0, channelIds.size());
    }

    /**
     * Uses the /Image/Pixels/Channel delete specification to remove the
     * channels added during {@link #makeImage()} and tests that the channels
     * are gone afterwards.
     */
    public void testDeleteRenderingDef() throws Exception {

        // Create test data
        long imageId = makeImage();
        String check = "select rdef.id from RenderingDef rdef where rdef.pixels.image.id = "
                + imageId;
        List<List<RType>> ids = assertProjection(check, null);
        assertTrue(ids.size() > 0);

        // Perform delete
        DeleteCommand dc = new DeleteCommand("/Image/Pixels/RenderingDef",
                imageId, null);
        doDelete(dc);

        // Check that data is gone
        ids = assertProjection(check, null);
        assertEquals(0, ids.size());
    }

    /**
     * Deletes the whole image. This uses the "/Image/Pixels/Channel" sub
     * specification as seen in {@link #testDeleteChannels()}
     */
    @SuppressWarnings("rawtypes")
    public void testImage() throws Exception {
        long imageId = makeImage();
        DeleteCommand dc = new DeleteCommand("/Image", imageId, null);

        doDelete(dc);

        List l = assertProjection("select i.id from Image i where i.id = "
                + imageId, null);
        assertEquals(0, l.size());
    }

    /**
     * Deletes a project and all its datasets though no images are created.
     */
    @SuppressWarnings("rawtypes")
    public void testProjectNoImage() throws Exception {

        // Create test data
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("name"));
        Dataset d = new DatasetI();
        d.setName(p.getName());

        p.linkDataset(d);
        p = (Project) assertSaveAndReturn(p);
        long id = p.getId().getValue();

        // Do Delete
        DeleteCommand dc = new DeleteCommand("/Project", id, null);
        doDelete(dc);

        // Make sure its come
        List l = assertProjection("select p.id from Project p where p.id = "
                + id, null);
        assertEquals(0, l.size());
    }

    //
    // Specs
    //

    /**
     * Loads the backup ids, i.e. those ids which should be deleted after the
     * channel has already been deleted.
     */
    @SuppressWarnings("unchecked")
    public void testBackUpIds() throws Exception {

        // Make data
        final long imageId = makeImage();

        // Get target ids
        String siQuery = "select si.id from Channel ch join ch.statsInfo si join ch.pixels pix join pix.image img where img.id = "
                + imageId;
        String lcQuery = "select lc.id from Channel ch join ch.logicalChannel lc join ch.pixels pix join pix.image img where img.id = "
                + imageId;
        RLong statsInfo = (RLong) assertProjection(siQuery, null).get(0).get(0);
        RLong logicalInfo = (RLong) assertProjection(lcQuery, null).get(0).get(
                0);
        Long si = statsInfo.getValue();
        Long lc = logicalInfo.getValue();

        // Run test
        final DeleteSpecFactory dsf = specFactory();
        final BaseDeleteSpec ch = (BaseDeleteSpec) dsf
                .get("/Image/Pixels/Channel");
        ch.initialize(imageId, null, null);

        List<List<Long>> backupIds = (List<List<Long>>) user_sf.getExecutor()
                .execute(user_sf.getPrincipal(),
                        new Executor.SimpleWork(this, "testBackpIds") {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                try {
                                    List<List<Long>> backupIds = ch.backupIds(
                                            session);
                                    return backupIds;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

        // Check
        // This relies on the ordering of the description.
        assertEquals(null, backupIds.get(0));
        assertEquals(si, backupIds.get(1).get(0));
        assertEquals(lc, backupIds.get(2).get(0));

    }

    //
    // Helpers
    //

    private void block(DeleteHandleI handle, int loops, long pause)
            throws ServerError, InterruptedException {
        for (int i = 0; i < loops && !handle.finished(); i++) {
            Thread.sleep(pause);
            if (handle.finished()) {
                return;
            }
        }
    }

    private DeleteHandleI doDelete(DeleteCommand... dc) throws Exception {
        Ice.Identity id = new Ice.Identity("handle", "delete");
        DeleteSpecFactory factory = specFactory();
        DeleteHandleI handle = new DeleteHandleI(id, user_sf, factory, dc, 1000);
        handle.run();
        return handle;
    }

    private DeleteSpecFactory specFactory() {
        DeleteSpecFactory factory = (DeleteSpecFactory) ctx
                .getBean("deleteSpecFactory");
        return factory;
    }

    /**
     * Method to handle async calls like other blitz test methods; however, this
     * method returns a proxy which is hard to test, and requires an adapter,
     * etc. Therefore, {@link #doDelete(DeleteCommand...)} passes back the
     * actual servant. This method left here for possible future usage.
     */
    @SuppressWarnings("unused")
    private DeleteHandlePrx queueDelete(DeleteCommand... dc) throws Exception {

        final RV rv = new RV();
        user_delete.queueDelete_async(new AMD_IDelete_queueDelete() {

            public void ice_response(DeleteHandlePrx __ret) {
                rv.rv = __ret;
            }

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }
        }, dc, current("queueDelete"));
        rv.assertPassed();
        assertNotNull(rv.rv);
        return (DeleteHandlePrx) rv.rv;
    }

    private InvocationMatcher once() {
        return new InvokeOnceMatcher();
    }

    private static class FakeAdapter implements Stub {

        private final Map<String, Object> servants = new HashMap<String, Object>();

        public StringBuffer describeTo(StringBuffer arg0) {
            return arg0;
        }

        public Object invoke(Invocation arg0) throws Throwable {
            if (arg0.invokedMethod.getName().equals("add")) {
                return ice_add(arg0.parameterValues);
            } else if (arg0.invokedMethod.getName().equals("createDirectProxy")) {
                return ice_find(arg0.parameterValues);
            } else if (arg0.invokedMethod.getName().equals("find")) {
                return ice_find(arg0.parameterValues);
            } else {
                throw new RuntimeException("Unknown method: "
                        + arg0.invokedMethod);
            }
        }

        private Object ice_add(List parameterValues) {
            Ice.Object servant = (Ice.Object) parameterValues.get(0);
            Ice.Identity id = (Ice.Identity) parameterValues.get(1);
            String key = Ice.Util.identityToString(id);
            servants.put(key, servant);
            throw new RuntimeException("NYI");
        }

        private Object ice_createDirectPrixy(List parameterValues) {
            Ice.Identity id = (Ice.Identity) parameterValues.get(0);
            throw new RuntimeException("NYI");
        }

        private Object ice_find(List parameterValues) {
            Ice.Identity id = (Ice.Identity) parameterValues.get(0);
            String key = Ice.Util.identityToString(id);
            return servants.get(key);
        }
    }
}
