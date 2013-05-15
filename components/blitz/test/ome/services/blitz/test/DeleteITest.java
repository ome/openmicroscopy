/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import ome.io.nio.AbstractFileSystemService;
import ome.services.blitz.impl.DeleteHandleI;
import ome.services.delete.DeleteStepFactory;
import ome.services.graphs.BaseGraphSpec;
import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphState;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;
import ome.tools.hibernate.ExtendedMetadata;

import omero.ApiUsageException;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IDelete_queueDelete;
import omero.api.IDeletePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.api.delete.DeleteReport;
import omero.api.delete._DeleteHandleTie;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Plate;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests call to {@link IDeletePrx}, especially important for testing the
 * {@link IDeletePrx#queueDelete(omero.api.delete.DeleteCommand[]) since it is
 * not available from {@link ome.api.IDelete}
 */
@Test(groups = { "integration", "delete" })
public class DeleteITest extends AbstractServantTest {

    Mock adapterMock;
    AbstractFileSystemService afs;
    Mock emMock;
    ExtendedMetadata em;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        adapterMock = (Mock) user.ctx.getBean("adapterMock");
        adapterMock.setDefaultStub(new FakeAdapter());
        afs = new AbstractFileSystemService(user.ctx.getProperty("omero.data.dir"));
        emMock = new Mock(ExtendedMetadata.class);
        em = (ExtendedMetadata) emMock.proxy();
    }

    /**
     * Demonstrates a simple usage. No intention of showing validity, but can be
     * given as an example to people.
     */
    public void testBasicUsageOfQueueDelete() throws Exception {
        long imageId = makeImage();
        DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
        _DeleteHandleTie handle = doDelete(dc);
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
     * Uses the /Image delete specification to remove an Image and its
     * annotations simply linked annotation. This is the most basic case.
     */
    @Test(groups = "ticket:2769")
    public void testImageWithAnnotations() throws Exception {

        // Create test data
        long imageId = makeImage();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(imageId, false), new TagAnnotationI());
        link = assertSaveAndReturn(link);
        long annId = link.getChild().getId().getValue();

        // Perform delete
        DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
        doDelete(dc);

        // Check that data is gone
        List<List<RType>> ids = assertProjection(
                "select ann.id from Annotation ann where ann.id = :id",
                new ParametersI().addId(annId));

        assertEquals(0, ids.size());
    }

    /**
     * Uses the /Image delete specification to remove an Image and attempts to
     * remove its annotations. If those annotations are multiply linked,
     * however, the attempted delete is rolled back (via a savepoint)
     *
     * As of 4.4.2, only a warning is returned for the annotationlink_child_annotation fk.
     */
    @Test(groups = {"ticket:2769", "ticket:2780"})
    public void testImageWithSharedAnnotations() throws Exception {

        // Create test data
        long imageId1 = makeImage();
        long imageId2 = makeImage();

        TagAnnotation tag = new TagAnnotationI();
        tag = assertSaveAndReturn(tag);

        ImageAnnotationLink link1 = new ImageAnnotationLinkI();
        link1.link(new ImageI(imageId1, false), tag);
        link1 = assertSaveAndReturn(link1);

        ImageAnnotationLink link2 = new ImageAnnotationLinkI();
        link2.link(new ImageI(imageId2, false), tag);
        link2 = assertSaveAndReturn(link2);

        // Perform delete
        DeleteCommand dc = new DeleteCommand("/Image", imageId1, null);
        _DeleteHandleTie handle = doDelete(dc);
        DeleteReport[] reports = handle.report();
        boolean found = false;
        for (DeleteReport report : reports) {
            found |= report.warning.contains("ConstraintViolation");
        }
        assertTrue(toString(reports), found);

        // Check that data is gone
        List<List<RType>> ids = assertProjection(
                "select img.id from Image img where img.id = :id",
                new ParametersI().addId(imageId1));

        assertEquals(0, ids.size());

        ids = assertProjection(
                "select ann.id from Annotation ann where ann.id = :id",
                new ParametersI().addId(tag.getId().getValue()));

        assertEquals(1, ids.size());
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
        List l;
        l = assertProjection("select p.id from Project p where p.id = " + id,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select d.id from Dataset d where d.id = " + id,
                null);
        assertEquals(0, l.size());
    }

    /**
     * Deletes a project and all its datasets which have images.
     */
    @SuppressWarnings("rawtypes")
    public void testProject() throws Exception {

        long iid = makeImage();

        // Create test data
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("name"));
        Dataset d = new DatasetI();
        d.setName(p.getName());

        p.linkDataset(d);
        d.linkImage(new ImageI(iid, false));
        p = assertSaveAndReturn(p);
        d = p.linkedDatasetList().get(0);
        long pid = p.getId().getValue();
        long did = d.getId().getValue();

        // Do Delete
        DeleteCommand dc = new DeleteCommand("/Project", pid, null);
        doDelete(dc);

        // Make sure its come
        List l;
        l = assertProjection("select p.id from Project p where p.id = " + pid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select d.id from Dataset d where d.id = " + did,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select i.id from Image i where i.id = " + iid,
                null);
        assertEquals(0, l.size());

    }

    /**
     * Deletes a very simple plate to ensure that the "/Image+WS" spec is
     * working.
     */
    @SuppressWarnings("rawtypes")
    public void testSimplePlate() throws Exception {

        long iid = makeImage();

        // Create test data
        Plate p = createPlate(iid);
        p = assertSaveAndReturn(p);

        long pid = p.getId().getValue();

        Well w = p.copyWells().get(0);
        long wid = w.getId().getValue();

        WellSample ws = w.getWellSample(0);
        long wsid = ws.getId().getValue();

        // Do Delete
        DeleteCommand dc = new DeleteCommand("/Plate", pid, null);
        doDelete(dc);

        // Make sure its deleted
        List l;
        l = assertProjection("select p.id from Plate p where p.id = " + pid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select w.id from Well w where w.id = " + wid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select ws.id from WellSample ws where ws.id = "
                + wsid, null);
        assertEquals(0, l.size());
        l = assertProjection("select i.id from Image i where i.id = " + iid,
                null);
        assertEquals(0, l.size());

    }

    /**
     * Deletes a very simple image/annotation graph, to guarantee that the
     * basic options are working
     */
    @SuppressWarnings("rawtypes")
    public void testSimpleImageWithAnnotation() throws Exception {

        long iid = makeImage();

        // Create test data
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long aid = link.getChild().getId().getValue();

        // Do Delete
        DeleteCommand dc = new DeleteCommand("/Image", iid, null);
        doDelete(dc);

        // Make sure its deleted
        List l;
        l = assertProjection("select i.id from Image i where i.id = " + iid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select a.id from Annotation a where a.id = "
                + aid, null);
        assertEquals(0, l.size());

    }

    /**
     * Attempts to use the ILink type for deleting all links which point
     * at an annotation.
     */
    @SuppressWarnings("rawtypes")
    public void testDeleteAllAnnotationLinks() throws Exception {

        // Create test data
        AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
        link.link(new TagAnnotationI(), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        DeleteCommand dc = new DeleteCommand("/Annotation", cid, null);
        doDelete(dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List l;
        l = assertProjection("select p.id from Annotation p where p.id = " + pid,
                null);
        assertEquals(1, l.size());
        l = assertProjection("select l.id from AnnotationAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select c.id from Annotation c where c.id = "
                + cid, null);
        assertEquals(0, l.size());

    }

    /**
     * Uses the {@link GraphEntry.Op#KEEP} setting to prevent a delete
     * from happening.
     */
    @SuppressWarnings("rawtypes")
    public void testKeepAnnotation() throws Exception {

        // Create test data
        AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
        link.link(new TagAnnotationI(), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        options.put("/TagAnnotation", "KEEP");
        DeleteCommand dc = new DeleteCommand("/Annotation", cid, options);
        doDelete(dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List l;
        l = assertProjection("select p.id from Annotation p where p.id = " + pid,
                null);
        assertEquals(1, l.size());
        l = assertProjection("select l.id from AnnotationAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(1, l.size());
        l = assertProjection("select c.id from Annotation c where c.id = "
                + cid, null);
        assertEquals(1, l.size());

    }

    /**
     * Uses the {@link GraphEntry.Op#KEEP} setting to prevent a delete
     * from happening.
     */
    @SuppressWarnings("rawtypes")
    public void testKeepImageAnnotation() throws Exception {

        // Create test data
        long iid = makeImage();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        options.put("/TagAnnotation", "KEEP");
        DeleteCommand dc = new DeleteCommand("/Image", pid, options);
        doDelete(dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List l;
        l = assertProjection("select p.id from Image p where p.id = " + pid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select c.id from Annotation c where c.id = "
                + cid, null);
        assertEquals(1, l.size());

    }

    /**
     * Tests overriding the {@link GraphEntry.Op#KEEP} setting by a hard-code
     * value in spec.xml. These are well-known "unshared" annotations, that should
     * be deleted, regardless of KEEP.
     */
    @SuppressWarnings("rawtypes")
    public void testDontKeepImageAnnotationIfUnsharedNS() throws Exception {

        // Create test data
        FileAnnotation file = new FileAnnotationI();
        file.setNs(omero.rtypes.rstring("openmicroscopy.org/omero/import/companionFile"));

        long iid = makeImage();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), file);
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        options.put("/FileAnnotation", "KEEP");
        DeleteCommand dc = new DeleteCommand("/Image", pid, options);
        doDelete(dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List l;
        l = assertProjection("select p.id from Image p where p.id = " + pid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select c.id from Annotation c where c.id = "
                + cid, null);
        assertEquals(0, l.size());

    }

    /**
     * Tests overriding the {@link GraphEntry.Op#KEEP} setting by setting
     * a namespace which should always be deleted (an "unshared" annotation).
     */
    @SuppressWarnings("rawtypes")
    public void testDontKeepImageAnnotationIfRequestedNS() throws Exception {

        // Create test data
        FileAnnotation file = new FileAnnotationI();
        file.setNs(omero.rtypes.rstring("keepme"));

        long iid = makeImage();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), file);
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        options.put("/FileAnnotation", "KEEP;excludes=keepme");
        DeleteCommand dc = new DeleteCommand("/Image", pid, options);
        doDelete(dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List l;
        l = assertProjection("select p.id from Image p where p.id = " + pid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        assertEquals(0, l.size());
        l = assertProjection("select c.id from Annotation c where c.id = "
                + cid, null);
        assertEquals(0, l.size());

    }

    /**
     * This method is copied from DeleteServiceTest to reproduce an issue
     * in which KEEP;excludes= is not being taken into acount.
     */
    @Test
    public void testDeleteObjectWithAnnotationWithoutNS()
        throws Exception
    {
        Screen obj = new ScreenI();
        obj.setName(omero.rtypes.rstring("testDelete"));
        obj = assertSaveAndReturn(obj);
        String type = "/Screen";
        long id = obj.getId().getValue();

        List<Long> annotationIds = createNonSharableAnnotation(obj, null);
        List<Long> annotationIdsNS = createNonSharableAnnotation(obj, "TEST");

        Map<String, String> options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=TEST");
        doDelete(new DeleteCommand(type, id, options));

        ParametersI param = new ParametersI();
        param.addId(obj.getId().getValue());
        String sql = "select s from Screen s where id = :id";
        assertEquals(0, assertFindByQuery(sql, param).size());
        param = new ParametersI();
        param.addIds(annotationIds);
        assertTrue(annotationIds.size() > 0);

        sql = "select i from Annotation as i where i.id in (:ids)";
        List<IObject> l = assertFindByQuery(sql, param);
        assertEquals(obj + "-->" + l.toString(), annotationIds.size(), l.size());
        param = new ParametersI();
        param.addIds(annotationIdsNS);
        assertTrue(annotationIdsNS.size() > 0);
        sql = "select i from Annotation as i where i.id in (:ids)";
        l = assertFindByQuery(sql, param);
        assertEquals(obj + "-->" + l.toString(), 0, l.size());

    }

    List<Long> createNonSharableAnnotation(Screen obj, String ns) throws Exception {
        TermAnnotation ta = new TermAnnotationI();
        if (ns != null) {
            ta.setNs(omero.rtypes.rstring(ns));
        }
        ScreenAnnotationLink link = new ScreenAnnotationLinkI();
        link.link((Screen) obj.proxy(), ta);
        link = assertSaveAndReturn(link);
        return Arrays.asList(link.getChild().getId().getValue());
    }

    // original files
    //

    @Test(groups = "ticket:7314")
    public void testOriginalFileAnnoation() throws Exception {
        final FileAnnotationI ann = mockAnnotation();
        final OriginalFile file = ann.getFile();
        final long id = file.getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        DeleteCommand dc = new DeleteCommand("/OriginalFile", id, options);
        doDelete(dc);

        assertGone(file);
        assertGone(ann);

    }

    /**
     * This is not possible without nulling the FileAnnotation.file field.
     * That functionality does not currently exist.
     */
    @Test(groups = "ticket:7314", expectedExceptions = AssertionFailedError.class)
    public void testOriginalFileAnnoationWithKeep() throws Exception {
        final FileAnnotationI ann = mockAnnotation();
        final OriginalFile file = ann.getFile();
        final long id = file.getId().getValue();

        // Do Delete
        Map<String, String> options = new HashMap<String, String>();
        options.put("/OriginalFile/Annotation", "KEEP");
        DeleteCommand dc = new DeleteCommand("/OriginalFile", id, options);
        doDelete(dc);

        assertNotGone(ann);
        assertGone(file);

    }

    private FileAnnotationI mockAnnotation()
        throws Exception
    {
        OriginalFile file = (OriginalFileI) new IceMapper().map(ObjectFactory.createFile());
        FileAnnotationI ann = new FileAnnotationI();
        ann.setFile(file);
        ann = assertSaveAndReturn(ann);
        return ann;
    }

    void assertGone(IObject obj) throws Exception {
        List<IObject> test = assertLoadObject(obj);
        assertEquals(test.toString(), 0, test.size());
    }

    void assertNotGone(IObject obj) throws Exception {
        List<IObject> test = assertLoadObject(obj);
        assertEquals(test.toString(), 1, test.size());
    }

    private List<IObject> assertLoadObject(IObject obj)
        throws ApiUsageException, Exception
    {
        String kls = new IceMapper().omeroClass(obj.getClass().getName(), true)
            .getSimpleName();
        List<IObject> test = assertFindByQuery(
            "select x from " + kls + " x where x.id = " +
            obj.getId().getValue(), null);
        return test;
    }

    //
    // Specs
    //

    /**
     * Loads the backup ids, i.e. those ids which should be deleted after the
     * channel has already been deleted.
     */
    @SuppressWarnings("unchecked")
    @Test(enabled = false)
    public void testBackUpIds() throws Exception {

        // Make data
        final long imageId = makeImage();

        // Get target ids
        String siQuery = "select si.id from Channel ch join ch.statsInfo si join ch.pixels pix join pix.image img where img.id = "
                + imageId;
        String lcQuery = "select lc.id from Channel ch join ch.logicalChannel lc join ch.pixels pix join pix.image img where img.id = "
                + imageId;
        String chQuery = "select ch.id from Channel ch join ch.pixels pix join pix.image img where img.id = "
                + imageId;

        RLong statsInfoId = (RLong) assertProjection(siQuery, null).get(0).get(
                0);
        RLong logicalId = (RLong) assertProjection(lcQuery, null).get(0).get(0);
        RLong channelId = (RLong) assertProjection(chQuery, null).get(0).get(0);

        Long si = statsInfoId.getValue();
        Long lc = logicalId.getValue();
        Long ch = channelId.getValue();

        // Run test
        final ApplicationContext dsf = user.delete.loadSpecs();
        final BaseGraphSpec spec = dsf
                .getBean("/Image/Pixels/Channel", BaseGraphSpec.class);
        spec.initialize(imageId, null, null);

        List<List<Long>> backupIds = (List<List<Long>>) user.sf.getExecutor()
                .execute(user.sf.getPrincipal(),
                        new Executor.SimpleWork(this, "testBackpIds") {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {

                                try {
                                    EventContext ec = user.getCurrentEventContext();
                                    GraphState ids = new GraphState(ec, new DeleteStepFactory(ctx, em), null, session, spec);
                                    List<List<Long>> rv = new ArrayList<List<Long>>();
                                    fail("NYI");
                                    /*
                                    rv.add(ids.getFoundIds(spec, 0));
                                    rv.add(ids.getFoundIds(spec, 1));
                                    rv.add(ids.getFoundIds(spec, 2));
                                    */
                                    return rv;
                                } catch (Exception e) {
                                    RuntimeException rt = new RuntimeException(e.getMessage());
                                    rt.initCause(e);
                                    throw rt;
                                }
                            }
                        });

        // Check
        // This relies on the ordering of the description.

        // Previously we were loading nothing if the path didn't require it
        // But do to the complexity of the SPW model (#2777) and the upcoming
        // need to log the ids that are deleted (#1423) we're storing all the
        // ids
        // assertEquals(null, backupIds.get(0));
        assertEquals(ch, backupIds.get(0).get(0));
        assertEquals(si, backupIds.get(1).get(0));
        assertEquals(lc, backupIds.get(2).get(0));

    }

    //
    // Helpers
    //

    private void block(_DeleteHandleTie handle, int loops, long pause)
            throws ServerError, InterruptedException {
        for (int i = 0; i < loops && !handle.finished(); i++) {
            Thread.sleep(pause);
            if (handle.finished()) {
                return;
            }
        }
    }

    private _DeleteHandleTie doDelete(DeleteCommand... dc) throws Exception {
        Ice.Identity id = new Ice.Identity("handle", "delete");
        //DeleteSpecFactory factory = specFactory();
        DeleteHandleI handle = new DeleteHandleI(em,
                user.delete.loadSpecs(), id, user.sf, afs, dc, 1000);
        handle.run();
        DeleteReport[] reports = handle.report(null);
        assertEquals(toString(reports), 0, handle.errors(null));
        return new _DeleteHandleTie(handle);
    }

    protected String toString(DeleteReport[] reports)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (DeleteReport report : reports) {
            sb.append(report.error);
            sb.append("\n");
        }
        return sb.toString();
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
        user.delete.queueDelete_async(new AMD_IDelete_queueDelete() {

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

    Plate createPlate(long imageId) throws Exception {
        Plate p = new PlateI();
        p.setRows(omero.rtypes.rint(1));
        p.setColumns(omero.rtypes.rint(1));
        p.setName(omero.rtypes.rstring("plate"));
        // now make wells
        Well well = new WellI();
        well.setRow(omero.rtypes.rint(0));
        well.setColumn(omero.rtypes.rint(0));
        WellSample sample = new WellSampleI();
        sample.setImage(new ImageI(imageId, false));
        well.addWellSample(sample);
        p.addWell(well);
        return p;
    }
}
