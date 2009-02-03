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
import ome.api.local.LocalQuery;
import ome.model.containers.Dataset;
import ome.model.core.Image;
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
import omero.api.AMD_ITimeline_getMostRecentObjects;
import omero.model.Event;
import omero.model.IObject;
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
    LocalQuery query;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        // Shared
        OmeroContext inner = OmeroContext.getManagedServerContext();
        OmeroContext outer = new OmeroContext(new String[]{"classpath:omero/test2.xml"}, false);
        outer.setParent(inner);
        outer.afterPropertiesSet();
        
        BlitzExecutor be = new InThreadThrottlingStrategy();
        sm = (SessionManager) outer.getBean("sessionManager");

        user = new ManagedContextFixture(outer);
        String name = user.loginNewUserNewGroup();
        user_sf = user.createServiceFactoryI();

        user_t = new TimelineI(be);
        user_t.setServiceFactory(user_sf);
        user_t.setSessionManager(sm);
        user_t.setLocalQuery((LocalQuery) user.managedSf.getQueryService());

        root = new ManagedContextFixture(outer);
        // root.setCurrentUserAndGroup("root", "system"); TODO AFTERMERGE
        root_sf = root.createServiceFactoryI();

        root_t = new TimelineI(be);
        root_t.setServiceFactory(root_sf);
        root_t.setSessionManager(sm);
        root_t.setLocalQuery((LocalQuery) root.managedSf.getQueryService());
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

}
