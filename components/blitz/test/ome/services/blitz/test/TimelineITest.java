/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rtime_max;
import static omero.rtypes.rtime_min;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import static omero.rtypes.*;
import ome.api.local.LocalQuery;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.security.SecuritySystem;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.impl.TimelineI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.sessions.SessionManager;
import ome.services.throttling.InThreadThrottlingStrategy;
import ome.system.OmeroContext;
import omero.RTime;
import omero.ServerError;
import omero.api.AMD_ITimeline_countByPeriod;
import omero.api.AMD_ITimeline_getByPeriod;
import omero.api.AMD_ITimeline_getEventsByPeriod;
import omero.api.AMD_ITimeline_getMostRecentAnnotationLinks;
import omero.api.AMD_ITimeline_getMostRecentObjects;
import omero.api.AMD_ITimeline_getMostRecentShareCommentLinks;
import omero.model.CommentAnnotationI;
import omero.model.Event;
import omero.model.IObject;
import omero.model.ImageI;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class TimelineITest extends TestCase {

    ManagedContextFixture user, root;
    ServiceFactoryI user_sf, root_sf;
    TimelineI user_t, root_t;
    SessionManager sm;
    SecuritySystem ss;
    LocalQuery query;

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
        String name = user.loginNewUserNewGroup();
        user_sf = user.createServiceFactoryI();

        user_t = new TimelineI(be);
        user_t.setServiceFactory(user_sf);
        user_t.setSessionManager(sm);
        user_t.setSecuritySystem(ss);

        root = new ManagedContextFixture(outer);
        // root.setCurrentUserAndGroup("root", "system"); TODO AFTERMERGE
        root_sf = root.createServiceFactoryI();

        root_t = new TimelineI(be);
        root_t.setServiceFactory(root_sf);
        root_t.setSessionManager(sm);
        root_t.setSecuritySystem(ss);
    }

    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //
    // counts
    //

    @Test
    public void testCountNoImages() throws Exception {
        List<String> types = Arrays.asList("Image");
        Map<String, Long> target = new HashMap<String, Long>();
        target.put("Image", 0L);

        assertCountByPeriodMatchesTarget(types, rtime_max(), rtime_max(),
                target);

    }

    @Test
    public void testCountOneImage() throws Exception {
        List<String> types = Arrays.asList("Image");
        Map<String, Long> target = new HashMap<String, Long>();
        target.put("Image", 1L);

        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        assertCountByPeriodMatchesTarget(types, rtime_min(), rtime_max(),
                target);

    }

    @Test
    public void testCountAll() throws Exception {
        List<String> types = Arrays.asList("Image");
        Map<String, Long> target = new HashMap<String, Long>();
        target.put("Image", 1L);

        assertCountByPeriodMatchesTarget(null, rtime_max(), rtime_max(), target);

    }

    // fetches

    @Test
    public void testGetOneImage() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Map<String, List<IObject>> rv = assertGetByPeriod(types, rtime_min(),
                rtime_max(), false);

        List<Long> imageIds = new ArrayList<Long>();
        for (IObject obj : rv.get("Image")) {
            imageIds.add(obj.getId().getValue());
        }
        assertTrue(imageIds.contains(i.getId()));
    }

    @Test
    public void testGetEventLog() throws Exception {
        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Dataset d = new Dataset("ds");
        d = user.managedSf.getUpdateService().saveAndReturnObject(d);

        List<Event> rv = assertGetEvents(rtime_min(), rtime_max());

    }

    // fetches

    @Test
    public void testMostRecentImages() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Map<String, List<IObject>> rv = assertMostRecent(types, null, false);

        List<Long> imageIds = new ArrayList<Long>();
        for (IObject obj : rv.get("Image")) {
            imageIds.add(obj.getId().getValue());
        }
        assertTrue(imageIds.contains(i.getId()));
    }

    @Test
    public void testMostRecentMerged() throws Exception {
        List<String> types = Arrays.asList("Image", "Dataset");

        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Dataset d = new Dataset("ds");
        d = user.managedSf.getUpdateService().saveAndReturnObject(d);

        Map<String, List<IObject>> rv = assertMostRecent(types,
                new ParametersI().page(0, 1), true);

        assertTrue(rv.containsKey("Dataset"));
        assertFalse(rv.containsKey("Image"));
        assertEquals(1, rv.get("Dataset").size());
    }

    @Test
    public void testJust1RecentImage() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image(new Timestamp(System.currentTimeMillis()), "img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        ParametersI p = new ParametersI().page(0, 1);
        Map<String, List<IObject>> rv = assertMostRecent(types, p, false);
        assertEquals(1, rv.get("Image").size());
    }

    @Test
    public void testMostRecentShareComments() throws Exception {

        ParametersI justOne = new ParametersI();
        justOne.page(0, 1);

        // we'll assume that this user has no share comments for the moment
        assertEquals(0, assertShareComments(justOne).size());

        // Now create a share and add stuff to it
        String owner = user.getCurrentUser();
        long shareId = user.managedSf.getShareService().createShare("", null,
                null, null, null, true);
        user.managedSf.getShareService().addComment(shareId, "hi");

        // Still should return nothing
        assertEquals(0, assertShareComments(justOne).size());

        // After a member adds, something should be returned
        String member = user.loginNewUserNewGroup();
        user.managedSf.getShareService().addComment(shareId, "me too");
        user.setCurrentUser(owner);
        assertEquals(1, assertShareComments(justOne).size());

    }

    @Test
    public void testMostRecentAnnotations() throws Exception {

        List<IObject> baseLineAll = assertAnnotations(null, null, null, null);

        List<IObject> baseLineImage = assertAnnotations(Arrays.asList("Image"),
                null, null, null);

        List<IObject> baseLineDataset = assertAnnotations(Arrays
                .asList("Dataset"), null, null, null);

        List<IObject> baseLineComments = assertAnnotations(null, Arrays
                .asList(CommentAnnotation.class.getName()), null, null);

        // Now add two annotations
        Image i = new Image();
        i.setName("now");
        i.setAcquisitionDate(new Timestamp(System.currentTimeMillis()));
        i.linkAnnotation(new CommentAnnotation());
        i.linkAnnotation(new LongAnnotation());
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        // There should be at least two more
        List<IObject> twoMore = assertAnnotations(null, null, null, null);
        assertEquals(baseLineAll.size() + 2, twoMore.size());

        // And there should be two more Images, but no more datasets
        List<IObject> twoMoreImages = assertAnnotations(Arrays.asList("Image"),
                null, null, null);
        List<IObject> noMoreDatasets = assertAnnotations(Arrays
                .asList("Dataset"), null, null, null);
        assertEquals(baseLineImage.size() + 2, twoMoreImages.size());
        assertEquals(baseLineDataset.size(), noMoreDatasets.size());

        // Filter out only Comments
        List<IObject> oneMore = assertAnnotations(null, Arrays
                .asList(CommentAnnotation.class.getName()), null, null);
        assertEquals(baseLineComments.size() + 1, oneMore.size());
    }

    // Helpers
    // =========================================================================

    private void assertCountByPeriodMatchesTarget(List<String> types,
            RTime start, RTime end, Map<String, Long> target)
            throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final Map<String, Long> rv = new HashMap<String, Long>();
        user_t.countByPeriod_async(new AMD_ITimeline_countByPeriod() {

            public void ice_exception(Exception ex) {
                status[0] = true;
                exc[0] = ex;
            }

            public void ice_response(Map<String, Long> __ret) {
                status[1] = true;
                rv.putAll(__ret);
            }
        }, types, start, end, null);
        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        assertTrue(rv.keySet().containsAll(target.keySet()));
    }

    private Map<String, List<IObject>> assertGetByPeriod(List<String> types,
            RTime start, RTime end, boolean merge) throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final Map<String, List<IObject>> rv = new HashMap<String, List<IObject>>();
        user_t.getByPeriod_async(new AMD_ITimeline_getByPeriod() {

            public void ice_exception(Exception ex) {
                status[0] = true;
                exc[0] = ex;
            }

            public void ice_response(Map<String, List<IObject>> __ret) {
                status[1] = true;
                rv.putAll(__ret);
            }
        }, types, start, end, null, merge, null);
        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        return rv;
    }

    private List<Event> assertGetEvents(RTime start, RTime end)
            throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final List<Event> rv = new ArrayList<Event>();
        user_t.getEventsByPeriod_async(new AMD_ITimeline_getEventsByPeriod() {

            public void ice_exception(Exception ex) {
                status[0] = true;
                exc[0] = ex;
            }

            public void ice_response(List<Event> __ret) {
                status[1] = true;
                rv.addAll(__ret);
            }
        }, start, end, null, null);
        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        return rv;
    }

    private Map<String, List<IObject>> assertMostRecent(List<String> types,
            Parameters p, boolean merge) throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final Map<String, List<IObject>> rv = new HashMap<String, List<IObject>>();
        user_t.getMostRecentObjects_async(
                new AMD_ITimeline_getMostRecentObjects() {

                    public void ice_exception(Exception ex) {
                        status[0] = true;
                        exc[0] = ex;
                    }

                    public void ice_response(Map<String, List<IObject>> __ret) {
                        status[1] = true;
                        rv.putAll(__ret);
                    }
                }, types, p, merge, null);
        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        return rv;
    }

    private List<IObject> assertAnnotations(List<String> parents,
            List<String> children, List<String> namespaces, Parameters p)
            throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final List<IObject> rv = new ArrayList<IObject>();
        user_t.getMostRecentAnnotationLinks_async(
                new AMD_ITimeline_getMostRecentAnnotationLinks() {

                    public void ice_exception(Exception ex) {
                        status[0] = true;
                        exc[0] = ex;
                    }

                    public void ice_response(List<IObject> __ret) {
                        status[1] = true;
                        rv.addAll(__ret);
                    }
                }, parents, children, namespaces, p, null);

        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        return rv;
    }

    private List<IObject> assertShareComments(Parameters p) throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final List<IObject> rv = new ArrayList<IObject>();
        user_t.getMostRecentShareCommentLinks_async(
                new AMD_ITimeline_getMostRecentShareCommentLinks() {

                    public void ice_exception(Exception ex) {
                        status[0] = true;
                        exc[0] = ex;
                    }

                    public void ice_response(List<IObject> __ret) {
                        status[1] = true;
                        rv.addAll(__ret);
                    }
                }, p, null);

        assertFalse("exception thrown: " + exc[0], status[0]);
        assertTrue("didn't pass", status[1]);
        return rv;
    }

}
