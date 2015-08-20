/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.services.util.Executor;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ExtendedMetadata;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.cmd.Chgrp;
import omero.cmd.ERR;
import omero.cmd.HandleI;
import omero.cmd.IRequest;
import omero.cmd._HandleTie;
import omero.cmd.OK;
import omero.cmd.RequestObjectFactoryRegistry;
import omero.cmd.Response;
import omero.cmd.State;
import omero.cmd.graphs.ChgrpFacadeI;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
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

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.matcher.InvokeOnceMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 */
@Test(groups = { "integration", "chgrp" })
@SuppressWarnings("deprecation")
public class ChgrpITest extends AbstractGraphTest {

    long oldGroupId = -1L;

    long newGroupId = -2L;

    @BeforeMethod
    protected void setupNewGroup() throws Exception {
        oldGroupId = user.getCurrentEventContext().getCurrentGroupId();
        newGroupId = root.newGroup();
        root.addUserToGroup(
                user.getCurrentEventContext().getCurrentUserId(), newGroupId);
        user.getCurrentEventContext(); // RELOAD.
        changeToOldGroup();
    }

    IRequest newChgrp(String type, long id, long grp) {
        return newChgrp(type, id, grp, null);
    }

    IRequest newChgrp(String type, long id, long grp,
            Map<String, String> options) {
        ChgrpFacadeI chgrp = (ChgrpFacadeI) ic.findObjectFactory(Chgrp.ice_staticId()).create("");
        chgrp.type = type;
        chgrp.id = id;
        chgrp.options = options;
        chgrp.grp = grp;
        return chgrp;
    }

    /**
     * Demonstrates a simple usage. No intention of showing validity, but can be
     * given as an example to people.
     */
    public void testBasicUsageOfChgrp() throws Exception {

        // Setup data
        long imageId = makeImage();

        // Do chgrp and wait on completion.
        IRequest chgrp = newChgrp("/Image", imageId, newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 1000);

        // Non-null response signals completion.
        assertNotNull(handle.getResponse());

        // Cancelling is not possible after completion.
        assertFalse(handle.cancel());

        // Make sure that the FAILURE flag is not set.
        assertFalse(handle.getStatus().flags.contains(State.FAILURE));
    }

    //
    // Phase 1: basic validation
    //

    /**
     * Simple test showing that if a link is found between two non-user groups
     * that the chmod will fail.
     */
    @Test(groups = "ticket:6422")
    public void testDatasetImageLinkKeepFails() throws Exception {

        // Create data
        Image i = new ImageI();
        i.setName(rstring("ticket:6422"));
        Dataset d = new DatasetI();
        d.setName(rstring("ticket:6422"));
        i.linkDataset(d);
        i = assertSaveAndReturn(i);

        Map<String, String> options = new HashMap<String, String>();
        options.put("/DatasetImageLink", "KEEP");
        IRequest chgrp = newChgrp("/Image", i.getId().getValue(), newGroupId, options);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 1000);

        assertFailure(handle);
    }

    /**
     * Like {@link #testDatasetImageLinkFails()} but with the option to delete
     * the link turned on, the chgrp is successful.
     */
    @Test(groups = "ticket:6422")
    public void testDatasetImageLinkForcePasses() throws Exception {

        // Create data
        Image i = new ImageI();
        i.setName(rstring("ticket:6422"));
        Dataset d = new DatasetI();
        d.setName(rstring("ticket:6422"));
        i.linkDataset(d);
        i = assertSaveAndReturn(i);

        IRequest chgrp = newChgrp("/Image", i.getId().getValue(), newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 1000);

        assertSuccess(handle);
    }

    /**
     * Attempts to use the /Image/Pixels/Channel specification to chgrp the
     * channels added during {@link #makeImage()}. This should fail the group
     * validity tests.
     */
    public void testChgrpChannels() throws Exception {

        // Create test data
        long imageId = makeImage();
        List<List<RType>> channelIds = assertProjection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        int size = channelIds.size();
        assertTrue(size > 0);

        // Perform chgrp
        IRequest chgrp = newChgrp("/Image/Pixels/Channel",
                imageId, newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 500);
        assertFailure(handle);

    }

    /**
     * Like {@link #testChgrpChannels()} this should fail since it's not really
     * possible for the rdef to exist alone. This should somehow be detectable
     * by clients.
     */
    public void testChgrpRenderingDef() throws Exception {

        // Create test data
        long imageId = makeImage();
        String check = "select rdef.id from RenderingDef rdef where rdef.pixels.image.id = "
                + imageId;
        List<List<RType>> ids = assertProjection(check, null);
        assertTrue(ids.size() > 0);

        // Perform chgrp
        IRequest chgrp = newChgrp("/Image/Pixels/RenderingDef", imageId,
                newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 500);
        assertFailure(handle);
    }

    /**
     * chgrp the whole image.
     */
    @SuppressWarnings("rawtypes")
    public void testImage() throws Exception {
        long imageId = makeImage();
        IRequest chgrp = newChgrp("/Image", imageId, newGroupId);

        _HandleTie handle = submit(chgrp);
        block(handle, 5, 500);
        assertSuccess(handle);

        // For anyone logged into the old group, it should seem to disappear
        List l = assertProjection("select i.id from Image i where i.id = "
                + imageId, null);
        assertEquals(0, l.size());

        // Log into new group.
        changeToNewGroup();

        // For anyone logged into the new group, it should be present
        l = assertProjection("select i.id from Image i where i.id = "
                + imageId, null);
        assertEquals(1, l.size());
    }

    //
    // Phase 2: annotation validation
    //

    /**
     * Uses the /Image specification to chgrp an Image and its
     * annotations simply linked annotation. This is the most basic case.
     */
    @Test(groups = "ticket:6297")
    public void testImageWithAnnotations() throws Exception {

        // Create test data
        long imageId = makeImage();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(imageId, false), new TagAnnotationI());
        link = assertSaveAndReturn(link);
        long annId = link.getChild().getId().getValue();

        // Perform chgrp
        IRequest chgrp = newChgrp("/Image", imageId, newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 500);
        assertSuccess(handle);

        // Check that image and ann are gone
        assertDoesNotExist("Annotation", annId);
        assertDoesNotExist("Image", imageId);

        // Logging into the other group should reverse the situation
        changeToNewGroup();
        assertDoesExist("Annotation", annId);
        assertDoesExist("Image", imageId);

    }

    /**
     * Uses the /Image specification to chgrp an Image and attempts to
     * remove its annotations. If those annotations are multiply linked,
     * however, the attempted chgrp is rolled back (via a savepoint)
     */
    @Test(groups = { "ticket:6297" })
    public void testImageWithSharedAnnotations() throws Exception {

        // Create test data
        long imageId1 = makeImage();
        long imageId2 = makeImage();

        TagAnnotation tag = new TagAnnotationI();
        tag = assertSaveAndReturn(tag);
        long tagId = tag.getId().getValue();

        ImageAnnotationLink link1 = new ImageAnnotationLinkI();
        link1.link(new ImageI(imageId1, false), tag);
        link1 = assertSaveAndReturn(link1);

        ImageAnnotationLink link2 = new ImageAnnotationLinkI();
        link2.link(new ImageI(imageId2, false), tag);
        link2 = assertSaveAndReturn(link2);

        // Perform chgrp
        IRequest chgrp = newChgrp("/Image", imageId1, newGroupId);
        _HandleTie handle = submit(chgrp);
        block(handle, 5, 500);
        assertSuccess(handle);

        // Check that image and ann are gone
        assertDoesExist("Annotation", tagId);
        assertDoesExist("Image", imageId2);
        assertDoesNotExist("Image", imageId1);

        // Logging into the other group should reverse the situation
        changeToNewGroup();
        assertDoesNotExist("Annotation", tagId);
        assertDoesNotExist("Image", imageId2);
        assertDoesExist("Image", imageId1);

    }

    /**
     * Chgrp a project and all its datasets though no images are created.
     */
    @SuppressWarnings("rawtypes")
    public void testProjectNoImage() throws Exception {

        // Create test data
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("name"));
        Dataset d = new DatasetI();
        d.setName(p.getName());

        p.linkDataset(d);
        p = assertSaveAndReturn(p);
        long pid = p.getId().getValue();
        long did = p.linkedDatasetList().get(0).getId().getValue();

        // Do Delete
        IRequest chgrp = newChgrp("/Project", pid, newGroupId);
        submit(chgrp);

        // Make sure its been moved.
        assertDoesNotExist("Project", pid);
        assertDoesNotExist("Dataset", did);
        changeToNewGroup();
        assertDoesExist("Project", pid);
        assertDoesExist("Dataset", did);

    }

    /**
     * Chgrp a project and all its datasets which have images.
     */
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
        IRequest chgrp = newChgrp("/Project", pid, newGroupId);
        submit(chgrp);

        // Make sure its been moved.
        assertDoesNotExist("Project", pid);
        assertDoesNotExist("Dataset", did);
        assertDoesNotExist("Image", iid);
        changeToNewGroup();
        assertDoesExist("Project", pid);
        assertDoesExist("Dataset", did);
        assertDoesExist("Image", iid);

    }

    /**
     * Chgrp a very simple plate to ensure that the "/Image+WS" spec is
     * working.
     */
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
        IRequest chgrp = newChgrp("/Plate", pid, newGroupId);
        submit(chgrp);

        // Make sure its moved
        assertDoesNotExist("Plate", pid);
        assertDoesNotExist("Well", wid);
        assertDoesNotExist("WellSample", wsid);
        assertDoesNotExist("Image", iid);
        changeToNewGroup();
        assertDoesExist("Plate", pid);
        assertDoesExist("Well", wid);
        assertDoesExist("WellSample", wsid);
        assertDoesExist("Image", iid);

    }

    /**
     * Chgrp a very simple image/annotation graph, to guarantee that the basic
     * options are working
     */
    public void testSimpleImageWithAnnotation() throws Exception {

        long iid = makeImage();

        // Create test data
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long aid = link.getChild().getId().getValue();

        // Do Delete
        IRequest chgrp = newChgrp("/Image", iid, newGroupId);
        submit(chgrp);

        // Make sure its moved
        assertDoesNotExist("Image", iid);
        assertDoesNotExist("ImageAnnotationLink", lid);
        assertDoesNotExist("Annotation", aid);
        changeToNewGroup();
        assertDoesExist("Image", iid);
        assertDoesExist("ImageAnnotationLink", lid);
        assertDoesExist("Annotation", aid);

    }

    /**
     * Attempts to use the ILink type for chgrp'ing all links which point at an
     * annotation.
     */
    public void testChgrpAllAnnotationLinks() throws Exception {

        // Create test data
        AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
        link.link(new TagAnnotationI(), new TagAnnotationI());
        link = assertSaveAndReturn(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        IRequest chgrp = newChgrp("/Annotation", cid, newGroupId);
        submit(chgrp);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        assertDoesExist("Annotation", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid); // Deleted
        assertDoesNotExist("Annotation", cid);
        changeToNewGroup();
        assertDoesNotExist("Annotation", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid);
        assertDoesExist("Annotation", cid);
    }

    /**
     * Uses the {@link GraphEntry.Op#KEEP} setting to prevent a chgrp from
     * happening.
     */
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
        IRequest chgrp = newChgrp("/Annotation", cid, newGroupId, options);
        submit(chgrp);

        // Make sure everything stays put.
        assertDoesExist("Annotation", pid);
        assertDoesExist("AnnotationAnnotationLink", lid);
        assertDoesExist("Annotation", cid);
        changeToNewGroup();
        assertDoesNotExist("Annotation", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid);
        assertDoesNotExist("Annotation", cid);

    }

    /**
     * Uses the {@link GraphEntry.Op#KEEP} setting to prevent a chgrp from
     * happening.
     */
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
        IRequest chgrp = newChgrp("/Image", pid, newGroupId, options);
        submit(chgrp);

        assertDoesNotExist("Image", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid); // Deleted
        assertDoesExist("Annotation", cid);
        changeToNewGroup();
        assertDoesExist("Image", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid);
        assertDoesNotExist("Annotation", cid);

    }

    /**
     * Tests overriding the {@link GraphEntry.Op#KEEP} setting by a hard-code
     * value in spec.xml. These are well-known "unshared" annotations, that
     * should be chgrp'd, regardless of KEEP.
     */
    @SuppressWarnings("rawtypes")
    public void testDontKeepImageAnnotationIfUnsharedNS() throws Exception {

        // Create test data
        FileAnnotation file = new FileAnnotationI();
        file.setNs(omero.rtypes
                .rstring("openmicroscopy.org/omero/import/companionFile"));

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
        IRequest chgrp = newChgrp("/Image", pid, newGroupId, options);
        submit(chgrp);

        assertDoesNotExist("Image", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid);
        assertDoesNotExist("Annotation", cid);
        changeToNewGroup();
        assertDoesExist("Image", pid);
        assertDoesExist("AnnotationAnnotationLink", lid);
        assertDoesExist("Annotation", cid);

    }

    /**
     * Tests overriding the {@link GraphEntry.Op#KEEP} setting by setting a
     * namespace which should always be chgrp'd (an "unshared" annotation).
     */
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
        IRequest chgrp = newChgrp("/Image", pid, newGroupId, options);
        submit(chgrp);

        assertDoesNotExist("Image", pid);
        assertDoesNotExist("AnnotationAnnotationLink", lid);
        assertDoesNotExist("Annotation", cid);
        changeToNewGroup();
        assertDoesExist("Image", pid);
        assertDoesExist("AnnotationAnnotationLink", lid);
        assertDoesExist("Annotation", cid);

    }

    /**
     * This method is copied from DeleteServiceTest to reproduce an issue in
     * which KEEP;excludes= is not being taken into acount.
     */
    @Test
    public void testChgrpObjectWithAnnotationWithoutNS() throws Exception {
        Screen obj = new ScreenI();
        obj.setName(omero.rtypes.rstring("testChgrpObjectWithAnnotationWithoutNS"));
        obj = assertSaveAndReturn(obj);
        String type = "/Screen";
        long id = obj.getId().getValue();

        List<Long> annotationIds = createNonSharableAnnotation(obj, null);
        List<Long> annotationIdsNS = createNonSharableAnnotation(obj, "TEST");

        Map<String, String> options = new HashMap<String, String>();
        options.put("/Annotation", "KEEP;excludes=TEST");
        submit(newChgrp(type, id, newGroupId, options));

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

    List<Long> createNonSharableAnnotation(Screen obj, String ns)
            throws Exception {
        TermAnnotation ta = new TermAnnotationI();
        if (ns != null) {
            ta.setNs(omero.rtypes.rstring(ns));
        }
        ScreenAnnotationLink link = new ScreenAnnotationLinkI();
        link.link((Screen) obj.proxy(), ta);
        link = assertSaveAndReturn(link);
        return Arrays.asList(link.getChild().getId().getValue());
    }

    //
    // Helpers
    //

    private void changeToOldGroup() throws ServerError {
        IObject old =
            user.sf.setSecurityContext(
                    new ExperimenterGroupI(oldGroupId, false), null);
        old.getId().getValue();
    }

    private void changeToNewGroup() throws ServerError {
        IObject old =
            user.sf.setSecurityContext(
                    new ExperimenterGroupI(newGroupId, false), null);
        old.getId().getValue();
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
