/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.update;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import ome.api.ITypes;
import ome.api.Search;
import ome.conditions.TryAgain;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.enums.Correction;
import ome.model.enums.Immersion;
import ome.model.enums.Medium;
import ome.model.enums.UnitsLength;
import ome.model.jobs.ImportJob;
import ome.model.jobs.JobStatus;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.roi.Line;
import ome.model.roi.Rect;
import ome.model.roi.Roi;
import ome.model.roi.Shape;
import ome.model.units.Length;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

public class UpdateTest extends AbstractUpdateTest {

    @Test(enabled=false)
    public void testSaveSimpleObject() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        p = iUpdate.saveAndReturnObject(p);

        // FIXME This can no longer be done this way.
        // List logs = securitySystem.getCurrentEvent().collectLogs(null);
        // assertTrue(logs.size() > 0);

        Pixels check = (Pixels) iQuery.findByQuery("select p from Pixels p "
                + " left outer join fetch p.channels " + "  where p.id = :id",
                new Parameters().addId(p.getId()));

        assertTrue("channel ids differ", equalCollections(p
                .unmodifiableChannels(), check.unmodifiableChannels()));
    }

    @Test(enabled=false)
    public void test_uh_oh_duplicate_rows_0() throws Exception {
        String name = "SIMPLE:" + System.currentTimeMillis();
        Project p = new Project();
        p.setName(name);
        p = iUpdate.saveAndReturnObject(p);

        Project compare = iQuery.findByString(Project.class, "name", name);

        assertTrue(p.getId().equals(compare.getId()));

        Project send = new Project();
        send.setName(p.getName());
        send.setId(p.getId());
        send.setVersion(p.getVersion()); // This is important.
        send.setDescription("...test...");
        Project test = iUpdate.saveAndReturnObject(send);

        assertTrue(p.getId().equals(test.getId()));

        iQuery.findByString(Project.class, "name", name);

    }

    @Test(enabled=false)
    public void test_images_pixels() throws Exception {
        Image image = new Image();
        image.setName("test");

        Pixels active = ObjectFactory.createPixelGraph(null);
        image.addPixels(active);

        image = iUpdate.saveAndReturnObject(image);
        active = image.getPrimaryPixels();
        Pixels other = ObjectFactory.createPixelGraph(null);
        image.addPixels(other);

        iUpdate.saveAndReturnObject(image);

    }

    @Test(enabled=false)
    public void test_index_save() throws Exception {

        RenderingDef def = ObjectFactory.createRenderingDef();
        CodomainMapContext enhancement = ObjectFactory
                .createPlaneSlicingContext();
        ChannelBinding binding = ObjectFactory.createChannelBinding();

        // What we're interested in
        def.addChannelBinding(binding);
        def.addCodomainMapContext(enhancement);

        def = iUpdate.saveAndReturnObject(def);

    }

    @Test(enabled=false)
    public void test_index_save_order() throws Exception {

        RenderingDef def = ObjectFactory.createRenderingDef();

        ChannelBinding binding1 = ObjectFactory.createChannelBinding();
        binding1.setInputStart(new Double(1.0));

        ChannelBinding binding2 = ObjectFactory.createChannelBinding();
        binding2.setInputStart(new Double(2.0));

        ChannelBinding binding3 = ObjectFactory.createChannelBinding();
        binding3.setInputStart(new Double(3.0));

        def.addChannelBinding(binding1);
        def.addChannelBinding(binding2);
        def.addChannelBinding(binding3);

        def = iUpdate.saveAndReturnObject(def);

    }

    @Test(enabled=false)
    public void test_experimenters_groups() throws Exception {
        Experimenter e = new Experimenter();
        ExperimenterGroup g_1 = new ExperimenterGroup();
        ExperimenterGroup g_2 = new ExperimenterGroup();

        e.setOmeName("j.b." + System.currentTimeMillis());
        e.setFirstName(" Joe ");
        e.setLastName(" Brown ");
        e.setLdap(false);

        g_1.setName("DEFAULT: " + System.currentTimeMillis());
        g_2.setName("NOTDEFAULT: " + System.currentTimeMillis());
        g_1.setLdap(false);
        g_2.setLdap(false);

        // The instances must be unloaded to prevent spurious deletes!
        // Need versions. See:
        // http://trac.openmicroscopy.org.uk/ome/ticket/118
        // http://trac.openmicroscopy.org.uk/ome/ticket/346
        e = iUpdate.saveAndReturnObject(e);
        g_1 = iUpdate.saveAndReturnObject(g_1);
        g_2 = iUpdate.saveAndReturnObject(g_2);
        g_1.unload();
        g_2.unload();
        // No longer unloading the experimenter (3.0-Beta2.3) since
        // it is necessary to set the index on the map
        // e.unload();

        e.linkExperimenterGroup(g_1);
        e.linkExperimenterGroup(g_2);
        iUpdate.saveObject(e);

        Experimenter test = (Experimenter) iQuery
                .findByQuery(" select e from Experimenter e "
                        + " join fetch e.groupExperimenterMap m "
                        + " join fetch m.parent p " + " where e.id = :id "
                        + "and index(m) = 0", new Parameters().addId(e.getId()));
        assertNotNull(test.getPrimaryGroupExperimenterMap());
        assertTrue(test.getPrimaryGroupExperimenterMap().parent().getName()
                .startsWith("DEFAULT"));

    }

    @Test(enabled = false, groups = "broken")
    // This test copies the previous one, changing the relationship
    // to image/pixels since they are not protected by the security
    // system. The answer is NO. This cannot be done. A loaded
    // Image with a loaded pixels collection must be used to save
    // a new Pixels, otherwise it can not properly set the "index"
    // field.
    public void test_image_pixels() throws Exception {
        Image img = new Image();
        Pixels pix1 = new Pixels();
        Pixels pix2 = new Pixels();

        // The instances must be unloaded to prevent spurious deletes!
        // Need versions. See:
        // http://trac.openmicroscopy.org.uk/ome/ticket/118
        // http://trac.openmicroscopy.org.uk/ome/ticket/346

        img.setName("j.b." + System.currentTimeMillis());
        img = iUpdate.saveAndReturnObject(img);
        img.unload();

        pix1 = ObjectFactory.createPixelGraph(null);
        pix1.setImage(img);
        pix1 = iUpdate.saveAndReturnObject(pix1);

        pix2 = ObjectFactory.createPixelGraph(null);
        pix2.setImage(img);
        pix2 = iUpdate.saveAndReturnObject(pix2);

        // Rest deleted. Trying only to handle _backRefs.
    }

    @Test(enabled=false)
    /** attempt to reproduce an error seen on the client side */
    public void test_save_array() throws Exception {

        loginRoot();

        Long e = -1L;
        List<Experimenter> es = iQuery.findAll(Experimenter.class, null);
        for (Experimenter experimenter : es) {
            Long l = experimenter.getId();
            if (!l.equals(new Long(0L))) {
                e = l;
            }
        }

        Project[] ps = new Project[] { new Project(), new Project(),
                new Project() };
        for (Project project : ps) {
            project.setName("save-array");
        }
        ps[0].getDetails().setOwner(new Experimenter(e, false));
        ps[1].getDetails().setOwner(new Experimenter(e, false));

        Dataset[] ds = new Dataset[] { new Dataset(), new Dataset() };
        for (Dataset dataset : ds) {
            dataset.setName("save-array");
        }

        for (Dataset dataset : ds) {
            for (Project project : ps) {
                project.linkDataset(dataset);
            }
        }

        iUpdate.saveAndReturnArray(ps);
    }

    // ~ Problems with values returned by update
    // =========================================================================

    String err = "obj is loaded, set is not null AND not filled!";

    @Test(enabled=false, groups = { "broken", "ticket:346" })
    public void testAddingReturnsNonEmptySets() throws Exception {
        // using the add method works
        Pixels p = ObjectFactory.createPixelGraph(null);
        Thumbnail tb = ObjectFactory.createThumbnails(p);
        assertPixels(tb);

        // passing it in as a proxy is ok.
        p = ObjectFactory.createPixelGraph(null);
        p = iUpdate.saveAndReturnObject(p);
        p = new Pixels(p.getId(), false);
        tb = ObjectFactory.createThumbnails(p);
        tb.setPixels(p);
        assertPixels(tb);

        // issues with using the setter with a non-proxy
        p = ObjectFactory.createPixelGraph(null);
        tb = new Thumbnail();
        tb.setMimeType("");
        tb.setSizeX(1);
        tb.setSizeY(1);
        tb.setPixels(p);
        assertPixels(tb);

    }

    protected void assertPixels(Thumbnail tb) {
        Thumbnail test = iUpdate.saveAndReturnObject(tb);
        Thumbnail copy = iQuery.get(test.getClass(), test.getId());
        assertFalse(err, copy.getPixels().isLoaded()
                && copy.getPixels().sizeOfThumbnails() == 0);
        assertFalse(err, test.getPixels().isLoaded()
                && test.getPixels().sizeOfThumbnails() == 0);
    }

    @Test(enabled=false, groups = { "broken", "ticket:346" })
    public void testLinkingReturnsNonEmptySets() throws Exception {

        // using the link methods does what it's supposed to
        Project p = new Project();
        p.setName("test");
        Dataset d = new Dataset();
        d.setName("test");
        p.linkDataset(d);
        ProjectDatasetLink link = (ProjectDatasetLink) p.collectDatasetLinks(
                null).get(0);
        assertLink(link);

        // and using proxies works
        p = iUpdate.saveAndReturnObject(p);
        p = new Project(p.getId(), false);
        d = iUpdate.saveAndReturnObject(d);
        d = new Dataset(d.getId(), false);
        link = new ProjectDatasetLink();
        link.link(p, d);
        assertLink(link);

        // but there are issues with passing in non-proxies when not using the
        // reverse methods.
        p = new Project();
        p.setName("test");
        d = new Dataset();
        d.setName("test");
        link = new ProjectDatasetLink();
        link.link(p, d);
        assertLink(link);
    }

    @Test(enabled=false, groups = { "jobs", "ticket:667" })
    public void testLinkingUnidirectionally() throws Exception {
        ITypes t = this.factory.getTypesService();
        ImportJob job = new ImportJob();
        OriginalFile file = new OriginalFile();
        file.setPath("");
        file.setHash("");
        file.setName("");
        file.setSize(0L);
        job.linkOriginalFile(file);
        job.setImageDescription("test");
        job.setImageName("image name");
        job.setUsername("root");
        job.setGroupname("system");
        job.setType("Test");
        job.setMessage("foo");
        job.setSubmitted(new Timestamp(System.currentTimeMillis()));
        job.setScheduledFor(new Timestamp(System.currentTimeMillis()));
        job.setStatus(t.getEnumeration(JobStatus.class, "Submitted"));
        iUpdate.saveObject(job);

    }

    @Test
    public void testRootCanDeleteObjectFromOtherGroup() {

        // This creates a user in a new group
        Experimenter e = loginNewUser();
        Image i = new Image("rootCanDeleteObjectFromOtherGroup");
        i = this.iUpdate.saveAndReturnObject(i);

        loginRootKeepGroup();
        this.iUpdate.deleteObject(i);
    }

    @Test(enabled = false, groups = "ticket:1001")
    public void testOptimisticLockingOnLinks() {

        Experimenter e2 = loginNewUser();
        Experimenter e1 = loginNewUser();

        Project p1 = new Project("p1");
        Dataset d1 = new Dataset("d1");
        p1.linkDataset(d1);
        p1 = iUpdate.saveAndReturnObject(p1);

        ProjectDatasetLink link = new ProjectDatasetLink();
        link.setParent(new Project(p1.getId(), false));
        link
                .setChild(new Dataset(p1.linkedDatasetList().get(0).getId(),
                        false));
        iUpdate.saveObject(link);

        loginUser(e2.getOmeName());
        p1.linkDataset(new Dataset("d2"));
        iUpdate.saveObject(p1);

    }

    @Test(enabled=false)
    public void testMultiThreadedPostJta() throws Exception {
        class T extends Thread {
            @Override
            public void run() {
                iUpdate.saveAndReturnObject(new Project("multi-thread-jta"));
            }
        };
        List<T> ts = new ArrayList<T>();
        for (int i = 0; i < 4; i++) {
            T t = new T();
            ts.add(t);
        }
        for (T t : ts) {
            t.join();
        }

    }

    @Test
    public void testPixelsIndexStartsWith0() throws Exception {
        Pixels p = ObjectFactory.createPixelGraph(null);
        // p.setDimensionOrder(iQuery.findAll(DimensionOrder.class, null).get(0));
        Image i = iUpdate.saveAndReturnObject(p.getImage());
        assertEquals(1, i.sizeOfPixels());
        assertNotNull(i.collectPixels(null).get(0));
    }

    @Test(groups ="ticket:1183")
    public void testSaveAndReturnWithAnnotation() {
        Project p = new Project("ticket:1183");
        p.linkAnnotation(new CommentAnnotation());
        p = iUpdate.saveAndReturnObject(p);
        p.setDescription("something else");
        iUpdate.saveAndReturnObject(p);
    }

    @Test(groups ="ticket:1183")
    public void testImageWithObjectSettings() {
        Image i = ObjectFactory.createPixelGraph(null).getImage();
        ObjectiveSettings os = new ObjectiveSettings();
        Immersion imm = new Immersion("Air");
        Correction corr = new Correction("Other");
        Instrument instr = new Instrument();
        Objective obj = new Objective(imm, corr, instr);
        os.setObjective(obj);
        os.setMedium(new Medium("Other"));
        os.setRefractiveIndex(0.0);
        i.setObjectiveSettings(os);
        i = iUpdate.saveAndReturnObject(i);
        assertNotNull(i.getObjectiveSettings());
        i = iUpdate.saveAndReturnObject(i);
        assertNotNull(i.getObjectiveSettings());
        i.setObjectiveSettings(new ObjectiveSettings(i.getObjectiveSettings().getId(),false));
        i = iUpdate.saveAndReturnObject(i);
    }

    @Test(groups = "ticket:2547")
    public void testChannelMoveWithFullArrayGoesToEnd() {
        Pixels p = ObjectFactory.createPixelGraphWithChannels(null, 3);
        Image i = p.getImage();
        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        Set<Long> ids = new HashSet<Long>();
        assertEquals(3, p.sizeOfChannels());
        for (Channel ch : p.unmodifiableChannels()) {
            assertNotNull(ch);
            ids.add(ch.getId());
        }

        // Now add another channel
        Pixels extra = ObjectFactory.createPixelGraph(null);
        p.addChannel(extra.getChannel(0));

        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        assertEquals(4, p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(3).getId()));

    }

    @Test(groups = "ticket:2547")
    public void testChannelMoveWithSpaceFillsSpace() {
        Pixels p = ObjectFactory.createPixelGraphWithChannels(null, 3);
        p.setChannel(1, null);
        Image i = p.getImage();
        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        Set<Long> ids = new HashSet<Long>();
        assertEquals(3, p.sizeOfChannels());
        assertNotNull(p.getChannel(0));
        ids.add(p.getChannel(0).getId());

        // Middle should be empty
        assertNull(p.getChannel(1));

        assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId());

        // Now add a channel to the front
        Pixels extra = ObjectFactory.createPixelGraph(null);
        Channel old = p.getChannel(0);
        p.setChannel(0, extra.getChannel(0));
        p.setChannel(1, old);

        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        assertEquals(3, p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(0).getId()));
    }

    @Test(groups = "ticket:2547")
    public void testChannelToSpaceChangesNothing() {
        Pixels p = ObjectFactory.createPixelGraphWithChannels(null, 3);
        p.setChannel(1, null);
        Image i = p.getImage();
        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        Set<Long> ids = new HashSet<Long>();
        assertEquals(3, p.sizeOfChannels());
        assertNotNull(p.getChannel(0));
        ids.add(p.getChannel(0).getId());

        // Middle should be empty
        assertNull(p.getChannel(1));

        assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId());

        // Now add a channel to the space
        Pixels extra = ObjectFactory.createPixelGraph(null);
        p.setChannel(1, extra.getChannel(0));

        i = iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        assertEquals(3, p.sizeOfChannels());
        assertFalse(ids.contains(p.getChannel(1).getId()));
    }

    @Test(groups = {"ticket:1679", "ticket:2547"})
    public void testRoiShapeIndexIssue() {
        Image image = iUpdate.saveAndReturnObject(new_Image(""));
        Roi roi = new Roi();
        roi.setImage(image);

        roi = iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            Rect rect = new Rect();
            rect.setX(19.0);
            rect.setY(21.0);
            rect.setWidth(10.0);
            rect.setHeight(10.0);
            rect.setTheZ(i);
            rect.setTheT(0);
            roi.addShape(rect);
        }

        roi = iUpdate.saveAndReturnObject(roi);
        Shape shape = roi.getShape(0);
        roi.removeShape(shape);
        roi = iUpdate.saveAndReturnObject(roi);
        iQuery.findAllByQuery("select r from Roi r join fetch r.shapes", null);
    }

    @Test(groups = "ticket:2547")
    public void testRemoveGroups() {
        Experimenter e = loginNewUser(); // Just used to create the group
        Experimenter e2 = loginNewUserInOtherUsersGroup(e);
        loginRoot();
        ExperimenterGroup[] groups = iAdmin.containedGroups(e2.getId());
        int j = -1;
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].getId().equals(1)) {
                continue;
            }
            j = i;
        }
        iAdmin.removeGroups(e2, groups[j]);
        iAdmin.lookupGroups();
    }

    /**
     * This method primarily used while walking through the debugger.
     */
    @Test(groups = "ticket:3978")
    public void testSaveSingleAnnotation() {
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("ticket:3978");
        tag = iUpdate.saveAndReturnObject(tag);
        // This prevents reloading annotationLinks
        tag.putAt(tag.ANNOTATIONLINKS, null);
        // This does not yet prevent reloading the previous event.
        tag.getDetails().shallowCopy(tag.getDetails().shallowCopy());
        tag = iUpdate.saveAndReturnObject(tag);
        // EventContext ec = iAdmin.getEventContext();
        // SessionStats stats = sessionManager.getSessionStats(ec.getCurrentSessionUuid());
        // stats.sqlCount();
    }

    protected void assertLink(ProjectDatasetLink link) {
        ProjectDatasetLink test = iUpdate.saveAndReturnObject(link);
        ProjectDatasetLink copy = iQuery.get(test.getClass(), test.getId());
        assertFalse(err, copy.parent().isLoaded()
                && copy.parent().sizeOfDatasetLinks() == 0);
        assertFalse(err, copy.child().isLoaded()
                && copy.child().sizeOfProjectLinks() == 0);
        assertFalse(err, test.parent().isLoaded()
                && test.parent().sizeOfDatasetLinks() == 0);
        assertFalse(err, test.child().isLoaded()
                && test.child().sizeOfProjectLinks() == 0);
    }

    /**
     * iobject->details->events->sessions->events grows with each new
     * write action. These should be stripped periodically from return
     * values.
     */
    @Test(groups = "ticket:3131")
    public void testEventsInSessionsAreCleared() throws Exception {
        LongAnnotation la = new LongAnnotation();
        la.setNs("ticket:3131-A");
        la = iUpdate.saveAndReturnObject(la);
        la.setNs("ticket:3131-B");
        la = iUpdate.saveAndReturnObject(la);
        la.setNs("ticket:3131-C");
        la = iUpdate.saveAndReturnObject(la);

        assertEquals(-1,
                la.getDetails().getUpdateEvent().getSession().sizeOfEvents());

        indexObject(la);

        Search search = factory.createSearchService();
        search.onlyType(LongAnnotation.class);
        search.byFullText("3131");
        la = (LongAnnotation) search.next();

        // In this case, the session is not even loaded.
        assertFalse(la.getDetails().getUpdateEvent().getSession().isLoaded());

        /*
        assertEquals(-1,
                la.getDetails().getUpdateEvent().getSession().sizeOfEvents());
        */

    }

    @Test(groups = "ticket:2710")
    public void testRoiWithoutImage() {
        Roi r = new Roi();
        iUpdate.saveAndReturnObject(r);
    }

    @Test
    public void testRoiWithStrokeWidth() {
        Roi r = new Roi();
        Line l = new Line();
        l.setStrokeWidth(new Length(1.0, UnitsLength.MILLIMETER));
        r.addShape(l);
        iUpdate.saveAndReturnObject(r);
    }

    @Test(groups = "ticket:5639")
    public void testCatchDeadlockException() throws Exception {
        /*
        HibernateInterceptor ht = (HibernateInterceptor) this.applicationContext.getBean("hibernateHandler");
        SQLErrorCodeSQLExceptionTranslator trans = (SQLErrorCodeSQLExceptionTranslator) ht.getJdbcExceptionTranslator();
        if (trans == null) {
            trans = new SQLEr
            ht.setJdbcExceptionTranslator(jdbcExceptionTranslator)
        }
        String[] loserCodes = trans.getSqlErrorCodes().getDeadlockLoserCodes();
        String[] newLoserCodes;
        if (loserCodes != null) {
            newLoserCodes = new String[loserCodes.length+1];
            System.arraycopy(loserCodes, 0, newLoserCodes, 0, loserCodes.length);
        } else {
            newLoserCodes = new String[1];
        }
        newLoserCodes[newLoserCodes.length-1] = "40P01";
        trans.getSqlErrorCodes().setDeadlockLoserCodes(newLoserCodes);
        */
        loginNewUser();
        final Image i1 = iUpdate.saveAndReturnObject(new_Image("catchDeadLock1"));
        final Image i2 = iUpdate.saveAndReturnObject(new_Image("catchDeadLock2"));

        final CyclicBarrier barrier1 = new CyclicBarrier(2);
        final CyclicBarrier barrier2 = new CyclicBarrier(2);

        class T extends Thread {
            long first, second;
            String name;
            Exception e;
            public T(String name, long first, long second) {
                super(name);
                this.name = name;
                this.first = first;
                this.second = second;
            }
            @Override
            public void run() {
                try {
                    executor.execute(loginAop.p, new Executor.SimpleWork(this, name) {
                        @Transactional(readOnly = false)
                        public Object doWork(Session session, ServiceFactory sf) {

                            try {
                                session.get(Image.class, first, LockOptions.UPGRADE);
                                barrier1.await(5, TimeUnit.SECONDS);
                                session.get(Image.class, second, LockOptions.UPGRADE);
                            } catch (RuntimeException rt) {
                                throw rt;
                            } catch (Exception exc) {
                                throw new RuntimeException(e);
                            } finally {
                                try {
                                    barrier2.await(5, TimeUnit.SECONDS);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return null;
                        }
                });
                } catch (Exception e) {
                    this.e = e;
                }

            }
        }


        final T t1 = new T("thread1", i1.getId(), i2.getId());
        final T t2 = new T("thread2", i2.getId(), i1.getId());

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (t1.e == null) {
            assertEquals(TryAgain.class, t2.e.getClass());
        } else if (t2.e == null) {
            assertEquals(TryAgain.class, t1.e.getClass());
        } else {
            t1.e.printStackTrace();
            t2.e.printStackTrace();
            fail("Expected exactly one exception");
        }

    }

}
