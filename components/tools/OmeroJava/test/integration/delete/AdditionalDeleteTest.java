/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration.delete;

import integration.AbstractServerTest;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ome.testing.ObjectFactory;

import omero.ApiUsageException;
import omero.RObject;
import omero.RType;
import omero.cmd.Delete2;
import omero.cmd.SkipHead;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
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
import omero.model.Pixels;
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

import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests deletion of various elements of the graph.
 * These tests are resurrected from the previous DeleteITest class, now deleted.
 */
@Test(groups = { "integration", "delete" })
public class AdditionalDeleteTest extends AbstractServerTest {

    /**
     * Uses the /Image/Pixels/Channel delete specification to remove the
     * channels added during {@link #importImage()} and tests that the channels
     * are gone afterwards.
     */
    public void testDeleteChannels() throws Throwable {

        // Create test data
        final long imageId = importImage();
        List<?> ids = iQuery.projection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        Assert.assertFalse(ids.isEmpty());

        // Perform delete
        final SkipHead dc = Requests.skipHead("Image", imageId, "Channel", new Delete2());
        callback(true, client, dc);

        // Check that data is gone
        ids = iQuery.projection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        Assert.assertTrue(ids.isEmpty());

        // Check that the image remains
        ids = iQuery.projection(
                "select id from Image where id = "
                        + imageId, null);
        Assert.assertFalse(ids.isEmpty());
    }

    /**
     * Uses the /Image/Pixels/RenderingDef delete specification to remove the
     * channels added during {@link #importImage()} and tests that the settings
     * are gone afterwards.
     */
    public void testDeleteRenderingDef() throws Throwable {

        // Create test data
        final long imageId = importImage();
        String check = "select rdef.id from RenderingDef rdef where rdef.pixels.image.id = "
                + imageId;
        List<?> ids = iQuery.projection(check, null);
        Assert.assertFalse(ids.isEmpty());

        // Perform delete
        final SkipHead dc = Requests.skipHead("Image", imageId, "RenderingDef", new Delete2());
        callback(true, client, dc);

        // Check that data is gone
        ids = iQuery.projection(check, null);
        Assert.assertTrue(ids.isEmpty());

        // Check that the channels remain
        ids = iQuery.projection(
                "select ch.id from Channel ch where ch.pixels.image.id = "
                        + imageId, null);
        Assert.assertFalse(ids.isEmpty());

        // Check that the image remains
        ids = iQuery.projection(
                "select id from Image where id = "
                        + imageId, null);
        Assert.assertFalse(ids.isEmpty());
    }

    /**
     * Deletes the whole image.
     */
    public void testImage() throws Exception {
        final long imageId = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        final Delete2 dc = Requests.delete("Image", imageId);
        callback(true, client, dc);

        // Check that data is gone
        List<?> l = iQuery.projection("select i.id from Image i where i.id = "
                + imageId, null);
        Assert.assertTrue(l.isEmpty());
    }

    /**
     * Uses the /Image delete specification to remove an Image and its
     * annotations simply linked annotation. This is the most basic case.
     */
    @Test(groups = "ticket:2769")
    public void testImageWithAnnotations() throws Exception {

        // Create test data
        final long imageId = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(imageId, false), new TagAnnotationI());
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
        final long annId = link.getChild().getId().getValue();

        // Perform delete
        final Delete2 dc = Requests.delete("Image", imageId);
        callback(true, client, dc);

        // Check that the annotation is gone
        List<?> ids = iQuery.projection(
                "select ann.id from Annotation ann where ann.id = :id",
                new ParametersI().addId(annId));
        Assert.assertTrue(ids.isEmpty());
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
        final long imageId1 = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        final long imageId2 = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();

        TagAnnotation tag = new TagAnnotationI();
        tag = (TagAnnotation) iUpdate.saveAndReturnObject(tag);

        ImageAnnotationLink link1 = new ImageAnnotationLinkI();
        link1.link(new ImageI(imageId1, false), tag);
        link1 = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link1);

        ImageAnnotationLink link2 = new ImageAnnotationLinkI();
        link2.link(new ImageI(imageId2, false), tag);
        link2 = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link2);

        // Perform delete
        final Delete2 dc = Requests.delete("Image", imageId1);
        callback(true, client, dc);

        // Check that data is gone
        List<?> ids = iQuery.projection(
                "select img.id from Image img where img.id = :id",
                new ParametersI().addId(imageId1));

        Assert.assertTrue(ids.isEmpty());

        // Check that the annotation remains
        ids = iQuery.projection(
                "select ann.id from Annotation ann where ann.id = :id",
                new ParametersI().addId(tag.getId().getValue()));

        Assert.assertFalse(ids.isEmpty());
    }

    /**
     * Deletes a project and all its datasets though no images are created.
     */
    public void testProjectNoImage() throws Exception {

        // Create test data
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("name"));
        Dataset d = new DatasetI();
        d.setName(p.getName());

        p.linkDataset(d);
        p = (Project) iUpdate.saveAndReturnObject(p);
        final long id = p.getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Project", id);
        callback(true, client, dc);

        // Check that data is gone
        List<?> ids;
        ids = iQuery.projection("select p.id from Project p where p.id = " + id,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select d.id from Dataset d where d.id = " + id,
                null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Deletes a project and all its datasets which have images.
     */
    public void testProject() throws Exception {

        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();

        // Create test data
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring("name"));
        Dataset d = new DatasetI();
        d.setName(p.getName());

        p.linkDataset(d);
        d.linkImage(new ImageI(iid, false));
        p = (Project) iUpdate.saveAndReturnObject(p);
        d = p.linkedDatasetList().get(0);
        final long pid = p.getId().getValue();
        final long did = d.getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Project", pid);
        callback(true, client, dc);

        // Check that data is gone
        List<?> ids;
        ids = iQuery.projection("select p.id from Project p where p.id = " + pid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select d.id from Dataset d where d.id = " + did,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select i.id from Image i where i.id = " + iid,
                null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Deletes a very simple plate to ensure that the "/Image+WS" spec is
     * working.
     */
    public void testSimplePlate() throws Exception {

        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();

        // Create test data
        Plate p = createPlate(iid);
        p = (Plate) iUpdate.saveAndReturnObject(p);

        final long pid = p.getId().getValue();

        Well w = p.copyWells().get(0);
        final long wid = w.getId().getValue();

        WellSample ws = w.getWellSample(0);
        final long wsid = ws.getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Plate", pid);
        callback(true, client, dc);

        // Check that data is gone
        List<?> ids;
        ids = iQuery.projection("select p.id from Plate p where p.id = " + pid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select w.id from Well w where w.id = " + wid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select ws.id from WellSample ws where ws.id = "
                + wsid, null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select i.id from Image i where i.id = " + iid,
                null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Deletes a very simple image/annotation graph, to guarantee that the
     * basic options are working
     */
    public void testSimpleImageWithAnnotation() throws Exception {

        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();

        // Create test data
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), new TagAnnotationI());
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);

        final long lid = link.getId().getValue();
        final long aid = link.getChild().getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Image", iid);
        callback(true, client, dc);

        // Check that data is gone
        List<?> ids;
        ids = iQuery.projection("select i.id from Image i where i.id = " + iid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select a.id from Annotation a where a.id = "
                + aid, null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Attempts to use the ILink type for deleting all links which point
     * at an annotation.
     */
    public void testDeleteAllAnnotationLinks() throws Exception {

        // Create test data
        AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
        link.link(new TagAnnotationI(), new TagAnnotationI());
        link = (AnnotationAnnotationLink) iUpdate.saveAndReturnObject(link);

        final long lid = link.getId().getValue();
        final long pid = link.getParent().getId().getValue();
        final long cid = link.getChild().getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Annotation", cid);
        callback(true, client, dc);

        // Make sure the parent annotation still exists, but both the annotation
        // link and the annotation that was linked to (the child) are gone.
        List<?> ids;
        ids = iQuery.projection("select p.id from Annotation p where p.id = " + pid,
                null);
        Assert.assertFalse(ids.isEmpty());
        ids = iQuery.projection("select l.id from AnnotationAnnotationLink l where l.id = " + lid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select c.id from Annotation c where c.id = "
                + cid, null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Uses {@link SkipHead} to prevent a delete from happening.
     */
    public void testKeepImageAnnotation() throws Exception {

        // Create test data
        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), new TagAnnotationI());
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);

        final long lid = link.getId().getValue();
        final long pid = link.getParent().getId().getValue();
        final long cid = link.getChild().getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Image", pid);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList("TagAnnotation");
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        // Make sure the image is deleted but the annotation remains.
        List<?> ids;
        ids = iQuery.projection("select p.id from Image p where p.id = " + pid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select c.id from Annotation c where c.id = "
                + cid, null);
        Assert.assertFalse(ids.isEmpty());
    }

    /**
     * Tests overriding the {@link ChildOption#excludeType} setting by a hard-code
     * value to the graph request factory. These are well-known "unshared" annotations,
     * that should be deleted, regardless of the setting.
     */
    public void testDontKeepImageAnnotationIfUnsharedNS() throws Exception {

        // Create test data
        FileAnnotation file = new FileAnnotationI();
        file.setNs(omero.rtypes.rstring("openmicroscopy.org/omero/import/companionFile"));

        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), file);
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);

        final long lid = link.getId().getValue();
        final long pid = link.getParent().getId().getValue();
        final long cid = link.getChild().getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Image", pid);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList("FileAnnotation");
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        // Make sure the image and annotation are deleted.
        List<?> ids;
        ids = iQuery.projection("select p.id from Image p where p.id = " + pid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select c.id from Annotation c where c.id = "
                + cid, null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * Tests overriding the {@link ChildOption#excludeType} setting by setting
     * a namespace which should always be deleted (an "unshared" annotation).
     */
    public void testDontKeepImageAnnotationIfRequestedNS() throws Exception {

        // Create test data
        FileAnnotation file = new FileAnnotationI();
        file.setNs(omero.rtypes.rstring("keepme"));

        final long iid = iUpdate.saveAndReturnObject(mmFactory.createImage()).getId().getValue();
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link(new ImageI(iid, false), file);
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);

        long lid = link.getId().getValue();
        long pid = link.getParent().getId().getValue();
        long cid = link.getChild().getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("Image", pid);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList("FileAnnotation");
        option.excludeNs = Collections.singletonList("keepme");
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        // Make sure the image and annotation are deleted.
        List<?> ids;
        ids = iQuery.projection("select p.id from Image p where p.id = " + pid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select l.id from ImageAnnotationLink l where l.id = " + lid,
                null);
        Assert.assertTrue(ids.isEmpty());
        ids = iQuery.projection("select c.id from Annotation c where c.id = "
                + cid, null);
        Assert.assertTrue(ids.isEmpty());
    }

    /**
     * This method is copied from DeleteServiceTest to reproduce an issue
     * in which KEEP;excludes= was not being taken into account.
     */
    @Test
    public void testDeleteObjectWithAnnotationWithoutNS()
        throws Exception
    {
        Screen obj = new ScreenI();
        obj.setName(omero.rtypes.rstring("testDelete"));
        obj = (Screen) iUpdate.saveAndReturnObject(obj);
        String type = "Screen";
        final long id = obj.getId().getValue();

        List<Long> annotationIds = createNonSharableAnnotation(obj, null);
        List<Long> annotationIdsNS = createNonSharableAnnotation(obj, "TEST");

        final Delete2 dc = Requests.delete(type, id);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList("Annotation");
        option.excludeNs = Collections.singletonList("TEST");
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        ParametersI param = new ParametersI();
        param.addId(obj.getId().getValue());
        String hql = "select s from Screen s where id = :id";
        Assert.assertTrue(iQuery.projection(hql, param).isEmpty());
        param = new ParametersI();
        param.addIds(annotationIds);
        Assert.assertFalse(annotationIds.isEmpty());

        hql = "select id from Annotation where id in (:ids)";
        List<?> ids = iQuery.projection(hql, param);
        Assert.assertEquals(annotationIds.size(), ids.size());
        param = new ParametersI();
        param.addIds(annotationIdsNS);
        Assert.assertFalse(annotationIdsNS.isEmpty());
        ids = iQuery.projection(hql, param);
        Assert.assertTrue(ids.isEmpty());
    }

    private List<Long> createNonSharableAnnotation(Screen obj, String ns) throws Exception {
        TermAnnotation ta = new TermAnnotationI();
        if (ns != null) {
            ta.setNs(omero.rtypes.rstring(ns));
        }
        ScreenAnnotationLink link = new ScreenAnnotationLinkI();
        link.link((Screen) obj.proxy(), ta);
        link = (ScreenAnnotationLink) iUpdate.saveAndReturnObject(link);
        return Arrays.asList(link.getChild().getId().getValue());
    }

    // original files
    //

    @Test(groups = "ticket:7314")
    public void testOriginalFileAnnotation() throws Exception {
        final FileAnnotationI ann = mockAnnotation();
        final OriginalFile file = ann.getFile();
        final long id = file.getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("OriginalFile", id);
        callback(true, client, dc);

        assertGone(file);
        assertGone(ann);
    }

    /**
     * This is not possible without nulling the FileAnnotation.file field.
     * So, the FileAnnotation must be deleted if its file is.
     */
    @Test(groups = "ticket:7314")
    public void testOriginalFileAnnotationWithKeep() throws Exception {
        final FileAnnotationI ann = mockAnnotation();
        final OriginalFile file = ann.getFile();
        final long id = file.getId().getValue();

        // Do Delete
        final Delete2 dc = Requests.delete("OriginalFile", id);
        final ChildOption option = new ChildOption();
        option.excludeType = Collections.singletonList("Annotation");
        dc.childOptions = Collections.singletonList(option);
        callback(true, client, dc);

        assertGone(ann);
        assertGone(file);

    }

    private FileAnnotationI mockAnnotation()
        throws Exception
    {
        OriginalFile file = (OriginalFileI) new IceMapper().map(ObjectFactory.createFile());
        FileAnnotationI ann = new FileAnnotationI();
        ann.setFile(file);
        ann = (FileAnnotationI) iUpdate.saveAndReturnObject(ann);
        return ann;
    }

    private void assertGone(IObject obj) throws Exception {
        final IObject test = assertLoadObject(obj);
        Assert.assertNull(test);
    }

    private IObject assertLoadObject(IObject obj)
        throws ApiUsageException, Exception
    {
        final String kls = IceMapper.omeroClass(obj.getClass().getName(), true).getSimpleName();
        final List<List<RType>> objects = iQuery.projection(
            "select x from " + kls + " x where x.id = " +
            obj.getId().getValue(), null);
        if (objects.isEmpty()) {
            return null;
        } else {
            final RObject object = (RObject) objects.get(0).get(0);
            return object.getValue();
        }
    }

    //
    // Helpers
    //

    private long importImage() throws Throwable {
        final File imageFile = ResourceUtils.getFile("classpath:tinyTest.d3d.dv");
        final Pixels pixels = importFile(imageFile, "dv").get(0);
        return pixels.getImage().getId().getValue();
    }

    private Plate createPlate(long imageId) throws Exception {
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
