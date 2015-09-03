/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;
import static omero.rtypes.rtime_max;
import static omero.rtypes.rtime_min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.services.blitz.impl.TimelineI;
import omero.RTime;
import omero.ServerError;
import omero.api.AMD_ITimeline_countByPeriod;
import omero.api.AMD_ITimeline_getByPeriod;
import omero.api.AMD_ITimeline_getEventLogsByPeriod;
import omero.api.AMD_ITimeline_getMostRecentAnnotationLinks;
import omero.api.AMD_ITimeline_getMostRecentObjects;
import omero.api.AMD_ITimeline_getMostRecentShareCommentLinks;
import omero.model.EventLog;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectI;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class TimelineITest extends AbstractServantTest {

    TimelineI user_t, root_t;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();

        user_t = new TimelineI(user.be);
        user_t.setServiceFactory(user.sf);
        user_t.setSessionManager(user.mgr);
        user_t.setSecuritySystem(user.ss);

        root_t = new TimelineI(root.be);
        root_t.setServiceFactory(root.sf);
        root_t.setSessionManager(root.mgr);
        root_t.setSecuritySystem(root.ss);
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
                target, null);

    }

    @Test
    public void testCountOneImage() throws Exception {
        List<String> types = Arrays.asList("Image");
        Map<String, Long> target = new HashMap<String, Long>();
        target.put("Image", 1L);

        Image i = new Image("img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        assertCountByPeriodMatchesTarget(types, rtime_min(), rtime_max(),
                target, null);

    }

    @Test
    public void testCountAll() throws Exception {
        List<String> types = Arrays.asList("Image");
        Map<String, Long> target = new HashMap<String, Long>();
        target.put("Image", 1L);

        assertCountByPeriodMatchesTarget(null, rtime_min(), rtime_max(), target, null);

    }

    // fetches

    @Test
    public void testGetOneImage() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image("img");
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
        Image i = new Image("img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Dataset d = new Dataset("ds");
        d = user.managedSf.getUpdateService().saveAndReturnObject(d);

        List<EventLog> rv = assertGetEventLogs(rtime_min(), rtime_max());
        assertTrue(rv.size() > 0);

    }

    // fetches

    @Test
    public void testMostRecentImages() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image("img");
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

        Image i = new Image("img");
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        Dataset d = new Dataset("ds");
        d = user.managedSf.getUpdateService().saveAndReturnObject(d);

        Map<String, List<IObject>> rv = assertMostRecent(types,
                new ParametersI().page(0, 1), true);

        assertTrue(rv.containsKey("Dataset"));
        assertTrue(rv.containsKey("Image"));
        assertEquals(0, rv.get("Dataset").size());
        assertEquals(1, rv.get("Image").size());
    }

    @Test
    public void testJust1RecentImage() throws Exception {
        List<String> types = Arrays.asList("Image");

        Image i = new Image("img");
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
        String member = root.loginNewUserNewGroup();
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

    @Test
    public void testMostRecentAnnotations2() throws Exception {
        List<IObject> baseLine = assertAnnotations(null, Arrays
                .asList("TagAnnotation"), null, null);

        // Now add a tag annotations
        Image i = new Image();
        i.setName("now");
        i.linkAnnotation(new TagAnnotation());
        i = user.managedSf.getUpdateService().saveAndReturnObject(i);

        List<IObject> oneMore = assertAnnotations(null, Arrays
                .asList("TagAnnotation"), null, null);
        assertEquals(baseLine.size() + 1, oneMore.size());
    }

    @Test
    public void testOrderMostRecentObjects() throws Exception {
        final long now = System.currentTimeMillis();

        omero.model.Image i1 = new omero.model.ImageI();
        i1.setName(rstring("first"));
        i1.setAcquisitionDate(rtime(now - 1));
        omero.model.Image i2 = new omero.model.ImageI();
        i2.setName(rstring("second"));
        i2.setAcquisitionDate(rtime(now + 1));

        Project p1 = new ProjectI();
        p1.setName(rstring("between1"));
        
        Project p2 = new ProjectI();
        p2.setName(rstring("between2"));
        
        i1 = assertSaveAndReturn(i1);
        p1 = assertSaveAndReturn(p1);
        p2 = assertSaveAndReturn(p2);
        i2 = assertSaveAndReturn(i2);
        
        ParametersI p = new ParametersI();
        p.page(0, 2);
        
        Map<String, List<IObject>> rv = assertMostRecent(Arrays.asList("Image"), p, false);
        List<IObject> res = rv.get("Image");
        assertEquals(i2.getId().getValue(), res.get(0).getId().getValue());
        assertEquals(i1.getId().getValue(), res.get(1).getId().getValue());
        
    }
    
    // Helpers
    // =========================================================================

    private void assertCountByPeriodMatchesTarget(List<String> types,
            RTime start, RTime end, Map<String, Long> target, Parameters p)
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
        }, types, start, end, p, null);
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

    private List<EventLog> assertGetEventLogs(RTime start, RTime end)
            throws Exception {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final List<EventLog> rv = new ArrayList<EventLog>();
        user_t.getEventLogsByPeriod_async(
                new AMD_ITimeline_getEventLogsByPeriod() {

                    public void ice_exception(Exception ex) {
                        status[0] = true;
                        exc[0] = ex;
                    }

                    public void ice_response(List<EventLog> __ret) {
                        status[1] = true;
                        rv.addAll(__ret);
                    }
                }, start, end, null, null);
        if (exc[0] != null) {
            throw exc[0];
        }
        assertFalse(status[0]);
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

    /**
     * If not filter is provided in the parameters argument, then one is
     * automatically added for the current user.
     */
    private List<IObject> assertAnnotations(List<String> parents,
            List<String> children, List<String> namespaces, Parameters p)
            throws ServerError {
        final boolean[] status = new boolean[] { false, false };
        final Exception[] exc = new Exception[1];
        final List<IObject> rv = new ArrayList<IObject>();
        if (p == null) {
            p = new ParametersI();
        }
        if (p.theFilter == null) {
            p.theFilter = new Filter();
            p.theFilter.ownerId = rlong(user.managedSf.getAdminService()
                    .getEventContext().getCurrentUserId());
        }
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
