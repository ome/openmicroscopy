/*
 *   Copyright 2006-2017 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ome.services.scripts.ScriptRepoHelper;
import omero.ServerError;
import omero.ValidationException;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.cmd.Delete2;
import omero.cmd.Request;
import omero.gateway.util.Requests;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Detector;
import omero.model.Dichroic;
import omero.model.EllipseI;
import omero.model.ExternalInfo;
import omero.model.ExternalInfoI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Filter;
import omero.model.Folder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.Line;
import omero.model.LineI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.Mask;
import omero.model.MaskI;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionAnnotationLink;
import omero.model.PlateAcquisitionAnnotationLinkI;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Point;
import omero.model.PointI;
import omero.model.Polygon;
import omero.model.PolygonI;
import omero.model.Polyline;
import omero.model.PolylineI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Reagent;
import omero.model.Rectangle;
import omero.model.RectangleI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.ScreenPlateLink;
import omero.model.Session;
import omero.model.Shape;
import omero.model.ShapeAnnotationLink;
import omero.model.ShapeAnnotationLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.XmlAnnotation;
import omero.model.XmlAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DatasetData;
import omero.gateway.model.EllipseData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LineData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.XMLAnnotationData;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;

/**
 * Collections of tests for the <code>IUpdate</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class UpdateServiceTest extends AbstractServerTest {

    /**
     * Test to create an image and make sure the version is correct.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testVersionHandling() throws Exception {
        Image img = mmFactory.simpleImage();
        img.setName(omero.rtypes.rstring("version handling"));
        Image sent = (Image) iUpdate.saveAndReturnObject(img);
        long version = sent.getDetails().getUpdateEvent().getId().getValue();

        sent.setDescription(omero.rtypes.rstring("version handling update"));
        // Update event should be created
        Image sent2 = (Image) iUpdate.saveAndReturnObject(sent);
        long version2 = sent2.getDetails().getUpdateEvent().getId().getValue();
        Assert.assertNotEquals(version, version2);
    }

    /**
     * Test to make sure that an update event is created for an object after
     * updating an annotation linked to the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:118")
    public void testVersionNotIncreasingAfterUpdate() throws Exception {
        CommentAnnotation ann = new CommentAnnotationI();
        Image img = mmFactory.simpleImage();
        img.setName(omero.rtypes.rstring("version_test"));
        img = (Image) iUpdate.saveAndReturnObject(img);

        ann.setTextValue(omero.rtypes.rstring("version_test"));
        img.linkAnnotation(ann);

        img = (Image) iUpdate.saveAndReturnObject(img);
        ann = (CommentAnnotation) img.linkedAnnotationList().get(0);
        Assert.assertNotNull(img.getId());
        Assert.assertNotNull(ann.getId());
        long oldId = img.getDetails().getUpdateEvent().getId().getValue();
        ann.setTextValue(omero.rtypes.rstring("updated version_test"));
        ann = (CommentAnnotation) iUpdate.saveAndReturnObject(ann);
        img = (Image) iQuery.get(Image.class.getName(), img.getId().getValue());

        long newId = img.getDetails().getUpdateEvent().getId().getValue();
        Assert.assertEquals(newId, oldId);
    }

    /**
     * Test to make sure that an update event is not created when when invoking
     * the <code>SaveAndReturnObject</code> on an unmodified Object.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:118")
    public void testVersionNotIncreasingOnUnmodifiedObject() throws Exception {
        Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Assert.assertNotNull(img.getDetails().getUpdateEvent());
        long id = img.getDetails().getUpdateEvent().getId().getValue();
        Image test = (Image) iUpdate.saveAndReturnObject(img);
        Assert.assertNotNull(test.getDetails().getUpdateEvent());
        Assert.assertEquals(id, test.getDetails().getUpdateEvent().getId().getValue());
    }

    /**
     * Tests the creation of a project without datasets.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyProject() throws Exception {
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        Assert.assertNotNull(p);
        ProjectData pd = new ProjectData(p);
        Assert.assertTrue(p.getId().getValue() > 0);
        Assert.assertEquals(p.getId().getValue(), pd.getId());
        Assert.assertEquals(p.getName().getValue(), pd.getName());
        Assert.assertEquals(p.getDescription().getValue(), pd.getDescription());
    }

    /**
     * Tests the creation of a dataset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyDataset() throws Exception {
        Dataset p = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        Assert.assertNotNull(p);
        DatasetData d = new DatasetData(p);
        Assert.assertTrue(p.getId().getValue() > 0);
        Assert.assertEquals(p.getId().getValue(), d.getId());
        Assert.assertEquals(p.getName().getValue(), d.getName());
        Assert.assertEquals(p.getDescription().getValue(), d.getDescription());
    }

    /**
     * Tests the creation of a dataset.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyImage() throws Exception {
        Image p = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage());
        ImageData img = new ImageData(p);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getId().getValue() > 0);
        Assert.assertEquals(p.getId().getValue(), img.getId());
        Assert.assertEquals(p.getName().getValue(), img.getName());
        Assert.assertEquals(p.getDescription().getValue(), img.getDescription());
    }

    /**
     * Tests the creation of an image with a set of pixels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateImageWithPixels() throws Exception {
        Image img = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Assert.assertNotNull(img);
        Pixels pixels = mmFactory.createPixels();
        img.addPixels(pixels);
        img = (Image) iUpdate.saveAndReturnObject(img);

        ParametersI param = new ParametersI();
        param.addId(img.getId().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("left outer join fetch i.pixels as pix ");
        sb.append("left outer join fetch pix.pixelsType as pt ");
        sb.append("where i.id = :id");
        img = (Image) iQuery.findByQuery(sb.toString(), param);
        Assert.assertNotNull(img);
        // Make sure we have a pixels set.
        pixels = img.getPixels(0);
        Assert.assertNotNull(pixels);
    }

    /**
     * Tests the creation of a screen.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1106")
    public void testEmptyScreen() throws Exception {
        Screen p = (Screen) factory.getUpdateService().saveAndReturnObject(
                mmFactory.simpleScreenData().asIObject());
        ScreenData data = new ScreenData(p);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getId().getValue() > 0);
        Assert.assertEquals(p.getId().getValue(), data.getId());
        Assert.assertEquals(p.getName().getValue(), data.getName());
        Assert.assertEquals(p.getDescription().getValue(), data.getDescription());
    }

    /**
     * Tests the creation of a screen.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testEmptyPlate() throws Exception {
        Plate p = (Plate) factory.getUpdateService().saveAndReturnObject(
                mmFactory.simplePlateData().asIObject());
        PlateData data = new PlateData(p);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getId().getValue() > 0);
        Assert.assertEquals(p.getId().getValue(), data.getId());
        Assert.assertEquals(p.getName().getValue(), data.getName());
        Assert.assertEquals(p.getDescription().getValue(), data.getDescription());
    }

    /**
     * Tests the creation of a plate with wells, wells sample and plate
     * acquisition.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testPopulatedPlate() throws Exception {
        Plate p = mmFactory.createPlate(1, 1, 1, 1, false);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        Assert.assertNotNull(p);
        Assert.assertNotNull(p.getName().getValue());
        Assert.assertNotNull(p.getStatus().getValue());
        Assert.assertNotNull(p.getDescription().getValue());
        Assert.assertNotNull(p.getExternalIdentifier().getValue());
        String sql = "select l from PlateAcquisition as l ";
        sql += "join fetch l.plate as p ";
        sql += "where p.id = :id";
        ParametersI param = new ParametersI();
        param.addId(p.getId());
        Assert.assertNotNull(iQuery.findByQuery(sql, param));

        p = mmFactory.createPlate(1, 1, 1, 0, false);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        Assert.assertNotNull(p);
        p = mmFactory.createPlate(1, 1, 1, 1, true);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        Assert.assertNotNull(p);
    }

    /**
     * Test to create a project and link datasets to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateProjectAndLinkDatasets() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Project p = new ProjectI();
        p.setName(omero.rtypes.rstring(name));

        p = (Project) iUpdate.saveAndReturnObject(p);

        Dataset d1 = new DatasetI();
        d1.setName(omero.rtypes.rstring(name));
        d1 = (Dataset) iUpdate.saveAndReturnObject(d1);

        Dataset d2 = new DatasetI();
        d2.setName(omero.rtypes.rstring(name));
        d2 = (Dataset) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        ProjectDatasetLink link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new ProjectDatasetLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        // links dataset and project.
        iUpdate.saveAndReturnArray(links);

        // load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("select p from Project p ");
        sb.append("left outer join fetch p.datasetLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Project) iQuery.findByQuery(sb.toString(), param);

        // Check the conversion of Project to ProjectData
        ProjectData pData = new ProjectData(p);
        Set<DatasetData> datasets = pData.getDatasets();
        // We should have 2 datasets
        Assert.assertEquals(datasets.size(), 2);
        int count = 0;
        Iterator<DatasetData> i = datasets.iterator();
        DatasetData dataset;
        while (i.hasNext()) {
            dataset = i.next();
            if (dataset.getId() == d1.getId().getValue()
                    || dataset.getId() == d2.getId().getValue())
                count++;
        }
        Assert.assertEquals(count, 2);
    }

    /**
     * Test to create a dataset and link images to it.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateDatasetAndLinkImages() throws Exception {
        String name = " 2&1 " + System.currentTimeMillis();
        Dataset p = new DatasetI();
        p.setName(omero.rtypes.rstring(name));

        p = (Dataset) iUpdate.saveAndReturnObject(p);

        Image d1 = new ImageI();
        d1.setName(omero.rtypes.rstring(name));
        d1 = (Image) iUpdate.saveAndReturnObject(d1);

        Image d2 = new ImageI();
        d2.setName(omero.rtypes.rstring(name));
        d2 = (Image) iUpdate.saveAndReturnObject(d2);

        List<IObject> links = new ArrayList<IObject>();
        DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d1);
        links.add(link);
        link = new DatasetImageLinkI();
        link.setParent(p);
        link.setChild(d2);
        links.add(link);
        // links dataset and project.
        iUpdate.saveAndReturnArray(links);

        // load the project
        ParametersI param = new ParametersI();
        param.addId(p.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("select p from Dataset p ");
        sb.append("left outer join fetch p.imageLinks pdl ");
        sb.append("left outer join fetch pdl.child ds ");
        sb.append("where p.id = :id");
        p = (Dataset) iQuery.findByQuery(sb.toString(), param);

        // Check the conversion of Project to ProjectData
        DatasetData pData = new DatasetData(p);
        Set<ImageData> images = pData.getImages();
        // We should have 2 datasets
        Assert.assertEquals(images.size(), 2);
        int count = 0;
        Iterator<ImageData> i = images.iterator();
        ImageData image;
        while (i.hasNext()) {
            image = i.next();
            if (image.getId() == d1.getId().getValue()
                    || image.getId() == d2.getId().getValue())
                count++;
        }
        Assert.assertEquals(count, 2);
    }

    /**
     * Test that folder hierarchies must be a strict tree.
     * @throws ServerError unexpected
     */
    @Test
    public void testCreateCyclicFolders() throws ServerError {
        Folder α = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder β = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder γ = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder δ = saveAndReturnFolder(mmFactory.simpleFolder());

        γ.setParentFolder(β);
        γ = saveAndReturnFolder(γ);

        δ.setParentFolder(γ);
        δ = saveAndReturnFolder(δ);

        /* at this point δ ← γ ← β */

        /* only α may be β's parent */

        for (Folder parent : Arrays.asList(δ, γ, β, α)) {
            β = returnFolder(β);
            β.setParentFolder(returnFolder(parent));
            try {
                iUpdate.saveObject(β);
                Assert.assertEquals(parent, α);
            } catch (ServerError e) {
                Assert.assertNotEquals(parent, α);
            }
        }

        /* at this point δ ← γ ← β ← α */

        α = returnFolder(α);
        β = returnFolder(β);
        γ = returnFolder(γ);
        δ = returnFolder(δ);

        Assert.assertNull(α.getParentFolder());
        Assert.assertEquals(β.getParentFolder().getId().getValue(), α.getId().getValue());
        Assert.assertEquals(γ.getParentFolder().getId().getValue(), β.getId().getValue());
        Assert.assertEquals(δ.getParentFolder().getId().getValue(), γ.getId().getValue());
    }

    /**
     * Test that parentage of a folder can be changed by {@link Folder#setParentFolder(Folder)}.
     * @throws ServerError unexpected
     */
    @Test
    public void testChangeFolderParentBySetParent() throws ServerError {
        Folder child = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder oldParent = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder newParent = saveAndReturnFolder(mmFactory.simpleFolder());
        List<Folder> oldParentChildren, newParentChildren;

        /* check that all is well with old parent */

        child.setParentFolder(oldParent);

        child = saveAndReturnFolder(child);
        oldParent = returnFolder(oldParent);
        newParent = returnFolder(newParent);
        oldParentChildren = oldParent.copyChildFolders();
        newParentChildren = newParent.copyChildFolders();

        Assert.assertEquals(1, oldParentChildren.size());
        Assert.assertEquals(0, newParentChildren.size());
        Assert.assertEquals(child.getParentFolder().getId().getValue(), oldParent.getId().getValue());
        Assert.assertEquals(oldParentChildren.get(0).getId().getValue(), child.getId().getValue());

        /* change parentage */

        child.setParentFolder(newParent);

        /* check that all is well with new parent */

        child = saveAndReturnFolder(child);
        oldParent = returnFolder(oldParent);
        newParent = returnFolder(newParent);
        oldParentChildren = oldParent.copyChildFolders();
        newParentChildren = newParent.copyChildFolders();

        Assert.assertEquals(0, oldParentChildren.size());
        Assert.assertEquals(1, newParentChildren.size());
        Assert.assertEquals(child.getParentFolder().getId().getValue(), newParent.getId().getValue());
        Assert.assertEquals(newParentChildren.get(0).getId().getValue(), child.getId().getValue());
    }

    /**
     * Test that parentage of a folder can be changed by {@link Folder#addChildFolders(Folder)}.
     * @throws ServerError unexpected
     */
    @Test
    public void testChangeFolderParentByAddChildFolders() throws ServerError {
        Folder child = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder oldParent = saveAndReturnFolder(mmFactory.simpleFolder());
        Folder newParent = saveAndReturnFolder(mmFactory.simpleFolder());
        List<Folder> oldParentChildren, newParentChildren;

        /* check that all is well with old parent */

        child.setParentFolder(oldParent);

        child = saveAndReturnFolder(child);
        oldParent = returnFolder(oldParent);
        newParent = returnFolder(newParent);
        oldParentChildren = oldParent.copyChildFolders();
        newParentChildren = newParent.copyChildFolders();

        Assert.assertEquals(1, oldParentChildren.size());
        Assert.assertEquals(0, newParentChildren.size());
        Assert.assertEquals(child.getParentFolder().getId().getValue(), oldParent.getId().getValue());
        Assert.assertEquals(oldParentChildren.get(0).getId().getValue(), child.getId().getValue());

        /* change parentage */

        newParent.addChildFolders(child);

        /* check that all is well with new parent */

        child = saveAndReturnFolder(child);
        oldParent = returnFolder(oldParent);
        newParent = returnFolder(newParent);
        oldParentChildren = oldParent.copyChildFolders();
        newParentChildren = newParent.copyChildFolders();

        Assert.assertEquals(0, oldParentChildren.size());
        Assert.assertEquals(1, newParentChildren.size());
        Assert.assertEquals(child.getParentFolder().getId().getValue(), newParent.getId().getValue());
        Assert.assertEquals(newParentChildren.get(0).getId().getValue(), child.getId().getValue());
    }

    // Annotation section

    /**
     * Links the passed annotation and test if correctly linked.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private void linkAnnotationAndObjects(Annotation data) throws Exception {
        // Image
        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage());
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i.proxy());
        l.setChild((Annotation) data.proxy());
        IObject o1 = iUpdate.saveAndReturnObject(l);
        Assert.assertNotNull(o1);
        l = (ImageAnnotationLink) o1;
        Assert.assertEquals(l.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(l.getParent().getId().getValue(), i.getId().getValue());

        // Project
        Project p = (Project) iUpdate.saveAndReturnObject(mmFactory
                .simpleProjectData().asIObject());
        ProjectAnnotationLink pl = new ProjectAnnotationLinkI();
        pl.setParent((Project) p.proxy());
        pl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(pl);
        Assert.assertNotNull(o1);
        pl = (ProjectAnnotationLink) o1;
        Assert.assertEquals(pl.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(pl.getParent().getId().getValue(), p.getId().getValue());

        // Dataset
        Dataset d = (Dataset) iUpdate.saveAndReturnObject(mmFactory
                .simpleDatasetData().asIObject());
        DatasetAnnotationLink dl = new DatasetAnnotationLinkI();
        dl.setParent((Dataset) d.proxy());
        dl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(dl);
        Assert.assertNotNull(o1);
        dl = (DatasetAnnotationLink) o1;
        Assert.assertEquals(dl.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(dl.getParent().getId().getValue(), d.getId().getValue());

        // Screen
        Screen s = (Screen) iUpdate.saveAndReturnObject(mmFactory
                .simpleScreenData().asIObject());
        ScreenAnnotationLink sl = new ScreenAnnotationLinkI();
        sl.setParent((Screen) s.proxy());
        sl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(sl);
        Assert.assertNotNull(o1);
        sl = (ScreenAnnotationLink) o1;
        Assert.assertEquals(sl.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(sl.getParent().getId().getValue(), s.getId().getValue());

        // Plate
        Plate pp = (Plate) iUpdate.saveAndReturnObject(mmFactory
                .simplePlateData().asIObject());
        PlateAnnotationLink ppl = new PlateAnnotationLinkI();
        ppl.setParent((Plate) pp.proxy());
        ppl.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(ppl);
        Assert.assertNotNull(o1);
        ppl = (PlateAnnotationLink) o1;
        Assert.assertEquals(ppl.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(ppl.getParent().getId().getValue(), pp.getId().getValue());

        // Plate acquisition
        pp = (Plate) iUpdate.saveAndReturnObject(
                mmFactory.createPlate(1, 1, 1, 1, false));
        long self = factory.getAdminService().getEventContext().userId;
        ParametersI param = new ParametersI();
        param.exp(omero.rtypes.rlong(self));
        //method tested in PojosServiceTest
        List<IObject> results = factory.getContainerService().loadContainerHierarchy(
                Plate.class.getName(),
                Arrays.asList(pp.getId().getValue()), param);
        pp = (Plate) results.get(0);
        List<PlateAcquisition> list = pp.copyPlateAcquisitions();
        Assert.assertEquals(1, list.size());
        PlateAcquisition pa = list.get(0);
        PlateAcquisitionAnnotationLink pal = new PlateAcquisitionAnnotationLinkI();
        pal.setParent((PlateAcquisition) pa.proxy());
        pal.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(pal);
        Assert.assertNotNull(o1);
        pal = (PlateAcquisitionAnnotationLink) o1;
        Assert.assertEquals(pal.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(pal.getParent().getId().getValue(), pa.getId().getValue());

        //Create a roi
        int n = 0;
        ROIData roiData = new ROIData();
        roiData.setImage((Image) i.proxy());
        //Add rectangle
        ShapeData r = new RectangleData(0, 0, 1, 1);
        roiData.addShapeData(r);
        n++;
        //Add ellipse
        r = new EllipseData(2, 2, 1, 1);
        roiData.addShapeData(r);
        n++;
        //Add point
        r = new PointData(1, 1);
        roiData.addShapeData(r);
        n++;
        //Add line
        r = new LineData(0, 1, 1, 2);
        roiData.addShapeData(r);
        n++;
        //Add polygon
        String points = "points[10,10] points1[10,10] points2[10,10]";
        Polygon rect = new PolygonI();
        rect.setPoints(omero.rtypes.rstring(points));
        r = new PolygonData(rect);
        roiData.addShapeData(r);
        n++;
        //Add polyline
        Polyline polyline = new PolylineI();
        polyline.setPoints(omero.rtypes.rstring(points));
        r = new PolylineData(polyline);
        roiData.addShapeData(r);
        n++;
        Roi roi = (Roi) iUpdate.saveAndReturnObject(roiData.asIObject());
        //annotate both roi and the shape.
        RoiAnnotationLink ral = new RoiAnnotationLinkI();
        ral.setParent((Roi) roi.proxy());
        ral.setChild((Annotation) data.proxy());
        o1 = iUpdate.saveAndReturnObject(ral);
        Assert.assertNotNull(o1);
        ral = (RoiAnnotationLink) o1;
        Assert.assertEquals(ral.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(ral.getParent().getId().getValue(), roi.getId().getValue());
        List<Shape> shapes = roi.copyShapes();
        Assert.assertEquals(n, shapes.size());
        Iterator<Shape> k = shapes.iterator();
        while (k.hasNext()) {
            Shape shape = k.next();
            ShapeAnnotationLink sal = new ShapeAnnotationLinkI();
            sal.setParent((Shape) shape.proxy());
            sal.setChild((Annotation) data.proxy());
            o1 = iUpdate.saveAndReturnObject(sal);
            Assert.assertNotNull(o1);
            sal = (ShapeAnnotationLink) o1;
            Assert.assertEquals(sal.getChild().getId().getValue(), data.getId().getValue());
            Assert.assertEquals(sal.getParent().getId().getValue(), shape.getId().getValue());
        }
    }

    /**
     * Tests to create a comment annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateCommentAnnotation() throws Exception {
        CommentAnnotation annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        annotation = (CommentAnnotation) iUpdate
                .saveAndReturnObject(annotation);
        Assert.assertNotNull(annotation);
        linkAnnotationAndObjects(annotation);
        TextualAnnotationData data = new TextualAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getText(), annotation.getTextValue().getValue());
        Assert.assertNull(data.getNameSpace());
    }

    /**
     * Tests to create a tag annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateTagAnnotation() throws Exception {
        TagAnnotation annotation = new TagAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("tag"));
        annotation = (TagAnnotation) iUpdate.saveAndReturnObject(annotation);
        Assert.assertNotNull(annotation);
        linkAnnotationAndObjects(annotation);
        TagAnnotationData data = new TagAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertNull(data.getNameSpace());
        Assert.assertEquals(data.getTagValue(), annotation.getTextValue().getValue());
    }

    /**
     * Tests to create a boolean annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateBooleanAnnotation() throws Exception {
        BooleanAnnotation annotation = new BooleanAnnotationI();
        annotation.setBoolValue(omero.rtypes.rbool(true));
        annotation = (BooleanAnnotation) iUpdate
                .saveAndReturnObject(annotation);
        Assert.assertNotNull(annotation);
        linkAnnotationAndObjects(annotation);
        BooleanAnnotationData data = new BooleanAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertNull(data.getNameSpace());
    }

    /**
     * Tests to create a long annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateLongAnnotation() throws Exception {
        LongAnnotation annotation = new LongAnnotationI();
        annotation.setLongValue(omero.rtypes.rlong(1L));
        annotation = (LongAnnotation) iUpdate.saveAndReturnObject(annotation);
        Assert.assertNotNull(annotation);
        linkAnnotationAndObjects(annotation);
        LongAnnotationData data = new LongAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertNull(data.getNameSpace());
        Assert.assertEquals(data.getDataValue(), annotation.getLongValue().getValue());
    }

    /**
     * Tests to create a file annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateFileAnnotation() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        Assert.assertNotNull(of);
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        Assert.assertNotNull(data);
        linkAnnotationAndObjects(data);
    }

    /**
     * Tests to create a term and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateTermAnnotation() throws Exception {
        TermAnnotation term = new TermAnnotationI();
        term.setTermValue(omero.rtypes.rstring("term"));
        term = (TermAnnotation) iUpdate.saveAndReturnObject(term);
        Assert.assertNotNull(term);
        linkAnnotationAndObjects(term);
        TermAnnotationData data = new TermAnnotationData(term);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getTerm(), term.getTermValue().getValue());
        Assert.assertNull(data.getNameSpace());
    }

    /**
     * Tests to unlink of an annotation. Creates only one type of annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRemoveAnnotation() throws Exception {
        LongAnnotationI annotation = new LongAnnotationI();
        annotation.setLongValue(omero.rtypes.rlong(1L));
        LongAnnotation data = (LongAnnotation) iUpdate
                .saveAndReturnObject(annotation);
        Assert.assertNotNull(data);
        // Image
        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.simpleImage());
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i.proxy());
        l.setChild((Annotation) data.proxy());
        l = (ImageAnnotationLink) iUpdate.saveAndReturnObject(l);
        Assert.assertNotNull(l);
        long id = l.getId().getValue();
        // annotation and image are linked. Remove the link.
        final Delete2 dc = Requests.delete().target(l).build();
        callback(true, client, dc);
        // now check that the image is no longer linked to the annotation
        String sql = "select link from ImageAnnotationLink as link";
        sql += " where link.id = :id";
        ParametersI p = new ParametersI();
        p.addId(id);
        IObject object = iQuery.findByQuery(sql, p);
        Assert.assertNull(object);
    }

    /**
     * Tests to update an annotation.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUpdateAnnotation() throws Exception {
        CommentAnnotationI annotation = new CommentAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("comment"));
        CommentAnnotation data = (CommentAnnotation) iUpdate
                .saveAndReturnObject(annotation);
        Assert.assertNotNull(data);
        // modified the text
        String newText = "commentModified";
        data.setTextValue(omero.rtypes.rstring(newText));
        CommentAnnotation update = (CommentAnnotation) iUpdate
                .saveAndReturnObject(data);
        Assert.assertNotNull(update);

        Assert.assertEquals(data.getId().getValue(), update.getId().getValue());

        Assert.assertEquals(newText, update.getTextValue().getValue());
    }

    /**
     * Tests the creation of tag annotation, linked it to an image by a user and
     * link it to the same image by a different user.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUpdateSameTagAnnotationUsedByTwoUsers() throws Exception {
        String groupName = newUserAndGroup("rwrw--").groupName;

        // create an image.
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());

        // create the tag.
        TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag1"));

        Annotation data = (Annotation) iUpdate.saveAndReturnObject(tag);
        // link the image and the tag
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());

        IObject o1 = iUpdate.saveAndReturnObject(l);
        Assert.assertNotNull(o1);
        CreatePojosFixture2 fixture = CreatePojosFixture2.withNewUser(root,
                groupName);

        l = new ImageAnnotationLinkI();
        l.setParent((Image) image.proxy());
        l.setChild((Annotation) data.proxy());
        // l.getDetails().setOwner(fixture.e);
        IObject o2 = fixture.iUpdate.saveAndReturnObject(l);
        Assert.assertNotNull(o2);

        long self = factory.getAdminService().getEventContext().userId;

        Assert.assertNotEquals(o1.getId().getValue(), o2.getId().getValue());
        Assert.assertEquals(o1.getDetails().getOwner().getId().getValue(), self);
        Assert.assertEquals(o2.getDetails().getOwner().getId().getValue(), fixture.e
                .getId().getValue());
    }

    /**
     * Tests the creation of tag annotation, linked it to an image by a user and
     * link it to the same image by a different user.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testTagSetTagCreation() throws Exception {
        // Create a tag set.
        TagAnnotationI tagSet = new TagAnnotationI();
        tagSet.setTextValue(omero.rtypes.rstring("tagSet"));
        tagSet.setNs(omero.rtypes.rstring(TagAnnotationData.INSIGHT_TAGSET_NS));
        TagAnnotation tagSetReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tagSet);
        // create a tag and link it to the tag set
        Assert.assertNotNull(tagSetReturned);
        TagAnnotationI tag = new TagAnnotationI();
        tag.setTextValue(omero.rtypes.rstring("tag"));
        TagAnnotation tagReturned = (TagAnnotation) iUpdate
                .saveAndReturnObject(tag);
        Assert.assertNotNull(tagReturned);
        AnnotationAnnotationLinkI link = new AnnotationAnnotationLinkI();
        link.setChild(tagReturned);
        link.setParent(tagSetReturned);
        IObject l = iUpdate.saveAndReturnObject(link); // save the link.
        Assert.assertNotNull(l);

        ParametersI param = new ParametersI();
        param.addId(l.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("select l from AnnotationAnnotationLink l ");
        sb.append("left outer join fetch l.child c ");
        sb.append("left outer join fetch l.parent p ");
        sb.append("where l.id = :id");
        AnnotationAnnotationLinkI lReturned = (AnnotationAnnotationLinkI) iQuery
                .findByQuery(sb.toString(), param);
        Assert.assertNotNull(lReturned.getChild());
        Assert.assertNotNull(lReturned.getParent());
        Assert.assertEquals(lReturned.getChild().getId().getValue(), tagReturned
                .getId().getValue());
        Assert.assertEquals(lReturned.getParent().getId().getValue(), tagSetReturned
                .getId().getValue());
    }

    //
    // The following are duplicated in ome.server.itests.update.UpdateTest
    //

    /**
     * Test the creation and handling of channels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelMoveWithFullArrayGoesToEnd() throws Exception {
        Image i = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER, ModelMockFactory.UINT16);
        i = (Image) iUpdate.saveAndReturnObject(i);
        Pixels p = i.getPrimaryPixels();

        Set<Long> ids = new HashSet<Long>();
        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER,
                p.sizeOfChannels());
        for (Channel ch : p.copyChannels()) {
            Assert.assertNotNull(ch);
            ids.add(ch.getId().getValue());
        }

        // Now add another channel
        Channel extra = mmFactory.createChannel(0); // Copies dimension orders,
                                                    // etc.
        p.addChannel(extra);

        i = (Image) iUpdate.saveAndReturnObject(i);
        p = i.getPrimaryPixels();

        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER + 1,
                p.sizeOfChannels());
        Assert.assertFalse(ids.contains(p
                .getChannel(ModelMockFactory.DEFAULT_CHANNELS_NUMBER).getId()
                .getValue()));
    }

    /**
     * Test the creation and handling of channels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelMoveWithSpaceFillsSpace() throws Exception {
        Image i = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER, ModelMockFactory.UINT16);
        i = (Image) iUpdate.saveAndReturnObject(i);

        Pixels p = i.getPrimaryPixels();
        p.setChannel(1, null);
        p = (Pixels) iUpdate.saveAndReturnObject(p);

        Set<Long> ids = new HashSet<Long>();
        Channel old = p.getChannel(0);
        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER,
                p.sizeOfChannels());
        Assert.assertNotNull(old);
        ids.add(p.getChannel(0).getId().getValue());

        // Middle should be empty
        Assert.assertNull(p.getChannel(1));

        Assert.assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId().getValue());

        // Now add a channel to the front

        // extra = (Channel) iUpdate.saveAndReturnObject(extra);
        // p.setChannel(0, extra);
        p.setChannel(1, old);

        p = (Pixels) iUpdate.saveAndReturnObject(p);
        Channel extra = mmFactory.createChannel(0);
        p.setChannel(0, extra);

        p = (Pixels) iUpdate.saveAndReturnObject(p);

        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER,
                p.sizeOfChannels());
        Assert.assertFalse(ids.contains(p.getChannel(0).getId().getValue()));
    }

    /**
     * Test the creation and handling of channels.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2547")
    public void testChannelToSpaceChangesNothing() throws Exception {
        Image i = mmFactory.createImage(ModelMockFactory.SIZE_X,
                ModelMockFactory.SIZE_Y, ModelMockFactory.SIZE_Z,
                ModelMockFactory.SIZE_T,
                ModelMockFactory.DEFAULT_CHANNELS_NUMBER, ModelMockFactory.UINT16);
        i = (Image) iUpdate.saveAndReturnObject(i);

        Pixels p = i.getPrimaryPixels();
        p.setChannel(1, null);
        p = (Pixels) iUpdate.saveAndReturnObject(p);

        Set<Long> ids = new HashSet<Long>();
        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER,
                p.sizeOfChannels());
        Assert.assertNotNull(p.getChannel(0));
        ids.add(p.getChannel(0).getId().getValue());

        // Middle should be empty
        Assert.assertNull(p.getChannel(1));

        Assert.assertNotNull(p.getChannel(2));
        ids.add(p.getChannel(2).getId().getValue());

        // Now add a channel to the space
        Channel extra = mmFactory.createChannel(0);
        p.setChannel(1, extra);

        p = (Pixels) iUpdate.saveAndReturnObject(p);

        Assert.assertEquals(ModelMockFactory.DEFAULT_CHANNELS_NUMBER,
                p.sizeOfChannels());
        Assert.assertFalse(ids.contains(p.getChannel(1).getId().getValue()));
    }

    /**
     * Tests the creation of plane information objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:168", "ticket:767" })
    public void testPlaneInfoSetPixelsSavePlaneInfo() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels = image.getPrimaryPixels();
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = mmFactory.createPlaneInfo();
        planeInfo.setPixels(pixels);
        planeInfo = (PlaneInfo) iUpdate.saveAndReturnObject(planeInfo);
        ParametersI param = new ParametersI();
        param.addId(planeInfo.getId());
        Pixels test = (Pixels) iQuery.findByQuery(
                "select pi.pixels from PlaneInfo pi where pi.id = :id", param);
        Assert.assertNotNull(test);
    }

    /**
     * Tests the creation of plane information objects. This time the plane info
     * object is directly added to the pixels set. The plane info should be
     * saved.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:168")
    public void testPixelsAddToPlaneInfoSavePixels() throws Exception {
        Image image = mmFactory.createImage();
        image = (Image) iUpdate.saveAndReturnObject(image);
        Pixels pixels = image.getPrimaryPixels();
        pixels.clearPlaneInfo();
        PlaneInfo planeInfo = mmFactory.createPlaneInfo();
        pixels.addPlaneInfo(planeInfo);
        pixels = (Pixels) iUpdate.saveAndReturnObject(pixels);
        ParametersI param = new ParametersI();
        param.addId(pixels.getId());
        List<IObject> test = (List<IObject>) iQuery.findAllByQuery(
                "select pi from PlaneInfo pi where pi.pixels.id = :id", param);
        Assert.assertTrue(CollectionUtils.isNotEmpty(test));
    }

    /**
     * Tests the creation of ROIs whose shapes are Ellipses and converts them
     * into the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithEllipse() throws Exception {
        ImageI image = (ImageI) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        RoiI roi = new RoiI();
        roi.setImage(image);
        RoiI serverROI = (RoiI) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        EllipseI rect = new EllipseI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setRadiusX(omero.rtypes.rdouble(v));
        rect.setRadiusY(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        EllipseData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (EllipseData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getX(), v);
            Assert.assertEquals(shape.getY(), v);
            Assert.assertEquals(shape.getRadiusX(), v);
            Assert.assertEquals(shape.getRadiusY(), v);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Points and converts them into
     * the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithPoint() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Point rect = new PointI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        PointData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (PointData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getX(), v);
            Assert.assertEquals(shape.getY(), v);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Points and converts them into
     * the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithRectangle() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Rectangle rect = new RectangleI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setWidth(omero.rtypes.rdouble(v));
        rect.setHeight(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        RectangleData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (RectangleData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getX(), v);
            Assert.assertEquals(shape.getY(), v);
            Assert.assertEquals(shape.getWidth(), v);
            Assert.assertEquals(shape.getHeight(), v);
        }
    }

    /**
     * Tests the creation of an ROI not linked to an image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithoutImage() throws Exception {
        Roi roi = new RoiI();
        roi.setDescription(omero.rtypes.rstring("roi w/o image"));
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
    }

    /**
     * Tests the creation of ROIs whose shapes are Polygons and converts them
     * into the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithPolygon() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        double w = 11;
        int z = 0;
        int t = 0;
        int c = 0;
        String points = "points[10,10] points1[10,10] points2[10,10]";
        Polygon rect = new PolygonI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        PolygonData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (PolygonData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getPoints().size(), 1);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Polylines and converts them
     * into the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithPolyline() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        String points = "points[10,10] points1[10,10] points2[10,10]";
        int z = 0;
        int t = 0;
        int c = 0;
        Polyline rect = new PolylineI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        PolylineData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (PolylineData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getPoints().size(), 1);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Lines and converts them into
     * the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithLine() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        double w = 11;
        int z = 0;
        int t = 0;
        int c = 0;
        Line rect = new LineI();
        rect.setX1(omero.rtypes.rdouble(v));
        rect.setY1(omero.rtypes.rdouble(v));
        rect.setX2(omero.rtypes.rdouble(w));
        rect.setY2(omero.rtypes.rdouble(w));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        LineData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (LineData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getX1(), v);
            Assert.assertEquals(shape.getY1(), v);
            Assert.assertEquals(shape.getX2(), w);
            Assert.assertEquals(shape.getY2(), w);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Masks and converts them into
     * the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithMask() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        int z = 0;
        int t = 0;
        int c = 0;
        Mask rect = new MaskI();
        rect.setX(omero.rtypes.rdouble(v));
        rect.setY(omero.rtypes.rdouble(v));
        rect.setWidth(omero.rtypes.rdouble(v));
        rect.setHeight(omero.rtypes.rdouble(v));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        MaskData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (MaskData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getX(), v);
            Assert.assertEquals(shape.getY(), v);
            Assert.assertEquals(shape.getWidth(), v);
            Assert.assertEquals(shape.getHeight(), v);
        }
    }

    /**
     * Tests the creation of an instrument using the <code>Add</code> methods
     * associated to an instrument.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateInstrumentUsingAdd() throws Exception {
        Instrument instrument;
        ParametersI param;
        String sql;
        IObject test;
        String value;
        for (int i = 0; i < ModelMockFactory.LIGHT_SOURCES.length; i++) {
            value = ModelMockFactory.LIGHT_SOURCES[i];
            instrument = mmFactory.createInstrument(value);
            instrument = (Instrument) iUpdate.saveAndReturnObject(instrument);
            Assert.assertNotNull(instrument);
            param = new ParametersI();
            param.addLong("iid", instrument.getId().getValue());
            sql = "select d from Detector as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            Assert.assertNotNull(test);
            sql = "select d from Dichroic as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            Assert.assertNotNull(test);
            sql = "select d from Filter as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            Assert.assertNotNull(test);
            sql = "select d from Objective as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            Assert.assertNotNull(test);
            sql = "select d from LightSource as d where d.instrument.id = :iid";
            test = iQuery.findByQuery(sql, param);
            Assert.assertNotNull(test);
            param = new ParametersI();
            param.addLong("iid", test.getId().getValue());
            if (ModelMockFactory.LASER.equals(value)) {
                sql = "select d from Laser as d where d.id = :iid";
                test = iQuery.findByQuery(sql, param);
                Assert.assertNotNull(test);
            } else if (ModelMockFactory.FILAMENT.equals(value)) {
                sql = "select d from Filament as d where d.id = :iid";
                test = iQuery.findByQuery(sql, param);
                Assert.assertNotNull(test);
            } else if (ModelMockFactory.ARC.equals(value)) {
                sql = "select d from Arc as d where d.id = :iid";
                test = iQuery.findByQuery(sql, param);
                Assert.assertNotNull(test);
            } else if (ModelMockFactory.LIGHT_EMITTING_DIODE.equals(value)) {
                sql = "select d from LightEmittingDiode as d where d.id = :iid";
                test = iQuery.findByQuery(sql, param);
                Assert.assertNotNull(test);
            }
        }
    }

    /**
     * Tests the creation of an instrument using the <code>setInstrument</code>
     * method on the entities composing the instrument.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateInstrumentUsingSet() throws Exception {
        Instrument instrument = (Instrument) iUpdate
                .saveAndReturnObject(mmFactory.createInstrument());
        Assert.assertNotNull(instrument);

        Detector d = mmFactory.createDetector();
        d.setInstrument((Instrument) instrument.proxy());
        d = (Detector) iUpdate.saveAndReturnObject(d);
        Assert.assertNotNull(d);

        Filter f = mmFactory.createFilter(500, 560);
        f.setInstrument((Instrument) instrument.proxy());
        f = (Filter) iUpdate.saveAndReturnObject(f);
        Assert.assertNotNull(f);

        Dichroic di = mmFactory.createDichroic();
        di.setInstrument((Instrument) instrument.proxy());
        di = (Dichroic) iUpdate.saveAndReturnObject(di);
        Assert.assertNotNull(di);

        Objective o = mmFactory.createObjective();
        o.setInstrument((Instrument) instrument.proxy());
        o = (Objective) iUpdate.saveAndReturnObject(o);
        Assert.assertNotNull(o);

        Laser l = mmFactory.createLaser();
        l.setInstrument((Instrument) instrument.proxy());
        l = (Laser) iUpdate.saveAndReturnObject(l);
        Assert.assertNotNull(l);

        ParametersI param = new ParametersI();
        param.addLong("iid", instrument.getId().getValue());
        // Now check that we have a detector.
        String sql = "select d from Detector as d where d.instrument.id = :iid";
        IObject test = iQuery.findByQuery(sql, param);
        Assert.assertNotNull(test);
        Assert.assertNotNull(test.getId().getValue() == d.getId().getValue());
        sql = "select d from Dichroic as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        Assert.assertNotNull(test);
        Assert.assertNotNull(test.getId().getValue() == di.getId().getValue());
        sql = "select d from Filter as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        Assert.assertNotNull(test);
        Assert.assertNotNull(test.getId().getValue() == f.getId().getValue());
        sql = "select d from Objective as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        Assert.assertNotNull(test);
        Assert.assertNotNull(test.getId().getValue() == o.getId().getValue());
        sql = "select d from LightSource as d where d.instrument.id = :iid";
        test = iQuery.findByQuery(sql, param);
        Assert.assertNotNull(test);
    }


    /**
     * Tests the creation of a plate and reagent
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testPlateAndReagent() throws Exception {
        Screen s = mmFactory.simpleScreenData().asScreen();
        Reagent r = mmFactory.createReagent();
        s.addReagent(r);
        Plate p = mmFactory.createPlateWithReagent(1, 1, 1, r);
        s.linkPlate(p);
        s = (Screen) iUpdate.saveAndReturnObject(s);
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getName().getValue());
        Assert.assertNotNull(s.getDescription().getValue());
        Assert.assertNotNull(s.getProtocolDescription().getValue());
        Assert.assertNotNull(s.getProtocolIdentifier().getValue());
        Assert.assertNotNull(s.getReagentSetDescription().getValue());
        Assert.assertNotNull(s.getReagentSetIdentifier().getValue());

        // reagent first
        String sql = "select r from Reagent as r ";
        sql += "join fetch r.screen as s ";
        sql += "where s.id = :id";
        ParametersI param = new ParametersI();
        param.addId(s.getId().getValue());
        r = (Reagent) iQuery.findByQuery(sql, param);
        Assert.assertNotNull(r);
        Assert.assertNotNull(r.getName().getValue());
        Assert.assertNotNull(r.getDescription().getValue());
        Assert.assertNotNull(r.getReagentIdentifier().getValue());

        //
        sql = "select s from ScreenPlateLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where p.id = :id";
        param = new ParametersI();
        param.addId(s.getId().getValue());
        ScreenPlateLink link = (ScreenPlateLink) iQuery.findByQuery(sql, param);
        Assert.assertNotNull(link);
        // check the reagent.
        sql = "select s from WellReagentLink as s ";
        sql += "join fetch s.child as c ";
        sql += "join fetch s.parent as p ";
        sql += "where c.id = :id";
        param = new ParametersI();
        param.addId(r.getId().getValue());
        Assert.assertNotNull(iQuery.findByQuery(sql, param));
    }

    /**
     * Tests to create a file annotation and link it to several images using the
     * saveAndReturnArray.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:5370" })
    public void testAttachFileAnnotationToSeveralImages() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        Assert.assertNotNull(of);
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        Assert.assertNotNull(data);
        // Image
        Image i1 = (Image) iUpdate
                .saveAndReturnObject(mmFactory.simpleImage());
        Image i2 = (Image) iUpdate
                .saveAndReturnObject(mmFactory.simpleImage());
        List<IObject> links = new ArrayList<IObject>();
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i1.proxy());
        l.setChild((Annotation) data.proxy());
        links.add(l);
        l = new ImageAnnotationLinkI();
        l.setParent((Image) i2.proxy());
        l.setChild((Annotation) data.proxy());
        links.add(l);
        links = iUpdate.saveAndReturnArray(links);
        Assert.assertNotNull(links);
        Assert.assertEquals(links.size(), 2);
        Iterator<IObject> i = links.iterator();
        long id;
        List<Long> ids = new ArrayList<Long>();
        ids.add(i1.getId().getValue());
        ids.add(i2.getId().getValue());
        int n = 0;
        while (i.hasNext()) {
            l = (ImageAnnotationLink) i.next();
            Assert.assertEquals(l.getChild().getId().getValue(), data.getId()
                    .getValue());
            id = l.getParent().getId().getValue();
            if (ids.contains(id))
                n++;
        }
        Assert.assertEquals(ids.size(), n);
    }

    /**
     * Tests to create a file annotation and link it to several images using the
     * saveAndReturnArray.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = { "ticket:5370" })
    public void testAttachFileAnnotationToSeveralImagesII() throws Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        Assert.assertNotNull(of);
        FileAnnotation fa = new FileAnnotationI();
        fa.setFile(of);
        FileAnnotation data = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        Assert.assertNotNull(data);
        // Image
        Image i1 = (Image) iUpdate
                .saveAndReturnObject(mmFactory.simpleImage());
        Image i2 = (Image) iUpdate
                .saveAndReturnObject(mmFactory.simpleImage());
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setParent((Image) i1.proxy());
        l.setChild((Annotation) data.proxy());

        l = (ImageAnnotationLink) iUpdate.saveAndReturnObject(l);
        Assert.assertEquals(l.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(l.getParent().getId().getValue(), i1.getId().getValue());
        l = new ImageAnnotationLinkI();
        l.setParent((Image) i2.proxy());
        l.setChild((Annotation) data.proxy());
        l = (ImageAnnotationLink) iUpdate.saveAndReturnObject(l);

        Assert.assertEquals(l.getChild().getId().getValue(), data.getId().getValue());
        Assert.assertEquals(l.getParent().getId().getValue(), i2.getId().getValue());
    }

    /**
     * Tests the creation of ROIs whose shapes are Polylines and converts them
     * into the corresponding <code>POJO</code> objects. The list of points
     * follows the specification.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithPolylineUsingSchema() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        String points = "10,10 11,11";
        int z = 0;
        int t = 0;
        int c = 0;
        Polyline rect = new PolylineI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        PolylineData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (PolylineData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getPoints().size(), 2);
        }
    }

    /**
     * Tests the creation of ROIs whose shapes are Polygons and converts them
     * into the corresponding <code>POJO</code> objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateROIWithPolygonUsingSchema() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Roi serverROI = (Roi) iUpdate.saveAndReturnObject(roi);
        Assert.assertNotNull(serverROI);
        double v = 10;
        double w = 11;
        int z = 0;
        int t = 0;
        int c = 0;
        String points = "10,10 11,11";
        Polygon rect = new PolygonI();
        rect.setPoints(omero.rtypes.rstring(points));
        rect.setTheZ(omero.rtypes.rint(z));
        rect.setTheT(omero.rtypes.rint(t));
        rect.setTheC(omero.rtypes.rint(c));
        serverROI.addShape(rect);

        serverROI = (RoiI) iUpdate.saveAndReturnObject(serverROI);

        ROIData data = new ROIData(serverROI);
        Assert.assertEquals(data.getId(), serverROI.getId().getValue());
        Assert.assertEquals(data.getShapeCount(), 1);

        List<ShapeData> shapes = data.getShapes(z, t);
        Assert.assertNotNull(shapes);
        Assert.assertEquals(shapes.size(), 1);
        PolygonData shape;
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
            shape = (PolygonData) i.next();
            Assert.assertEquals(shape.getT(), t);
            Assert.assertEquals(shape.getZ(), z);
            Assert.assertEquals(shape.getC(), c);
            Assert.assertEquals(shape.getPoints().size(), 2);
        }
    }

    /**
     * Tests to create a XML annotation and link it to various objects.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateXmlAnnotation() throws Exception {
        XmlAnnotation term = new XmlAnnotationI();
        term.setTextValue(omero.rtypes.rstring("xml"));
        term = (XmlAnnotation) iUpdate.saveAndReturnObject(term);
        Assert.assertNotNull(term);
        linkAnnotationAndObjects(term);
        XMLAnnotationData data = new XMLAnnotationData(term);
        Assert.assertNotNull(data);
        Assert.assertNull(data.getNameSpace());
        Assert.assertEquals(data.getText(), term.getTextValue().getValue());
    }

    /**
     * Tests to create a tag set annotation i.e. a tag with a name space.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateTagSetAnnotation() throws Exception {
        TagAnnotation annotation = new TagAnnotationI();
        annotation.setTextValue(omero.rtypes.rstring("tag set"));
        annotation = (TagAnnotation) iUpdate.saveAndReturnObject(annotation);
        Assert.assertNotNull(annotation);
        linkAnnotationAndObjects(annotation);
        TagAnnotationData data = new TagAnnotationData(annotation);
        data.setNameSpace(TagAnnotationData.INSIGHT_TAGSET_NS);
        annotation = (TagAnnotation) iUpdate.saveAndReturnObject(data
                .asIObject());
        data = new TagAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getTagValue(), annotation.getTextValue().getValue());
        Assert.assertEquals(data.getNameSpace(), TagAnnotationData.INSIGHT_TAGSET_NS);
    }

    /**
     * Tests to create a tag annotation i.e. a tag with a name space. using the
     * pojo class.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateTagAnnotationUsingPojo() throws Exception {
        String name = "tag";
        TagAnnotationData data = new TagAnnotationData(name);
        TagAnnotation annotation = (TagAnnotation) iUpdate
                .saveAndReturnObject(data.asIObject());
        data = new TagAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getTagValue(), name);
        Assert.assertNull(data.getNameSpace());
    }

    /**
     * Tests to create a tag set annotation i.e. a tag with a name space. using
     * the pojo class.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testCreateTagSetAnnotationUsingPojo() throws Exception {
        String name = "tag set";
        TagAnnotationData data = new TagAnnotationData(name, true);
        TagAnnotation annotation = (TagAnnotation) iUpdate
                .saveAndReturnObject(data.asIObject());
        data = new TagAnnotationData(annotation);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getTagValue(), name);
        Assert.assertNotNull(data.getNameSpace());
        Assert.assertEquals(data.getNameSpace(), TagAnnotationData.INSIGHT_TAGSET_NS);
    }

    /**
     * Test that users cannot adjust the {@link ExternalInfo} of their own session.
     * @throws Exception unexpected
     */
    @Test
    public void testEditSessionExternalInfo() throws Exception {
        newUserAndGroup("rw----");
        /* prepare to act as root */
        final IQueryPrx iQueryRoot = root.getSession().getQueryService();
        final IUpdatePrx iUpdateRoot = root.getSession().getUpdateService();
        /* as root, add external info with a specific UUID to the user's current session */
        Session session = (Session) iQueryRoot.findByString("Session", "uuid", client.getSessionId());
        final long sessionId = session.getId().getValue();
        final ExternalInfo infoOld = new ExternalInfoI();
        infoOld.setEntityType(omero.rtypes.rstring(Session.class.getName()));
        infoOld.setEntityId(omero.rtypes.rlong(sessionId));
        infoOld.setUuid(omero.rtypes.rstring(UUID.randomUUID().toString()));
        session.getDetails().setExternalInfo(infoOld);
        iUpdateRoot.saveObject(session);
        /* prepare for querying session with external info */
        final String hql = "FROM Session s JOIN FETCH s.details.externalInfo WHERE s.id = :id";
        final Parameters params = new ParametersI().addId(sessionId);
        /* as the normal user, check that the session external info UUID is as set by root */
        session = (Session) iQuery.findByQuery(hql, params);
        ExternalInfo info = session.getDetails().getExternalInfo();
        Assert.assertEquals(info.getUuid().getValue(), infoOld.getUuid().getValue());
        /* as the normal user, attempt to set the session external info to something else */
        final ExternalInfo infoNew = new ExternalInfoI();
        infoNew.setEntityType(omero.rtypes.rstring(Session.class.getName()));
        infoNew.setEntityId(omero.rtypes.rlong(sessionId));
        infoNew.setUuid(omero.rtypes.rstring(UUID.randomUUID().toString()));
        Assert.assertNotEquals(infoOld.getUuid().getValue(), infoNew.getUuid().getValue());
        session.getDetails().setExternalInfo(infoNew);
        try {
            iUpdate.saveObject(session);
            Assert.fail("the user cannot change the external info of a session");
        } catch (ValidationException ve) {
            /* expected */
        }
        /* as the normal user, check that the session external info UUID remains as set by root */
        session = (Session) iQuery.findByQuery(hql, params);
        info = session.getDetails().getExternalInfo();
        Assert.assertEquals(info.getUuid().getValue(), infoOld.getUuid().getValue());
        /* as the normal user, attempt to set the session external info UUID to something else */
        info.setUuid(infoNew.getUuid());
        iUpdate.saveObject(info);
        /* as the normal user, check that the session external info UUID remains as set by root */
        session = (Session) iQuery.findByQuery(hql, params);
        info = session.getDetails().getExternalInfo();
        Assert.assertEquals(info.getUuid().getValue(), infoOld.getUuid().getValue());
        /* as root, check that there is external info with UUID as set by root */
        Assert.assertNotNull(iQueryRoot.findByString("ExternalInfo", "uuid", infoOld.getUuid().getValue()));
        /* as root, check that there is not external info with UUID as set by the user */
        Assert.assertNull(iQueryRoot.findByString("ExternalInfo", "uuid", infoNew.getUuid().getValue()));
        /* as root, try setting the external info UUID just as the user first did */
        session = (Session) iQueryRoot.findByQuery(hql, params);
        session.getDetails().setExternalInfo(infoNew);
        iUpdateRoot.saveObject(session);
        /* as the normal user, check that the session external info UUID is now changed */
        session = (Session) iQuery.findByQuery(hql, params);
        info = session.getDetails().getExternalInfo();
        Assert.assertEquals(info.getUuid().getValue(), infoNew.getUuid().getValue());
        /* clean up after test */
        final Request delete = Requests.delete().target(info).build();
        doChange(root, root.getSession(), delete, true);
    }

    /**
     * Test that the update service cannot be used to create a file that is in a specific repository.
     * @throws Exception expects {@link ServerError} to be thrown in the file creation attempt
     */
    @Test(expectedExceptions = ServerError.class)
    public void testFileRepoPropertyNewInstance() throws Exception {
        newUserAndGroup("rwr---");
        OriginalFile file = mmFactory.createOriginalFile();
        file.setRepo(omero.rtypes.rstring("test repo"));
        iUpdate.saveObject(file);
    }

    /**
     * Test that the update service cannot be used to move a file into a specific repository.
     * @throws Exception expects {@link ServerError} to be thrown in the file adjustment attempt
     */
    @Test(expectedExceptions = ServerError.class)
    public void testFileRepoPropertyExistingInstance() throws Exception {
        newUserAndGroup("rwr---");
        OriginalFile file = mmFactory.createOriginalFile();
        file = (OriginalFile) iUpdate.saveAndReturnObject(file);
        file.setRepo(omero.rtypes.rstring("test repo"));
        iUpdate.saveObject(file);
    }

    /**
     * Test that after creating a directory in a repository then the directory's {@code repo} property correctly reflects that
     * repository.
     * @throws Exception unexpected
     */
    @Test
    public void testFileRepoPropertyCorrect() throws Exception {
        newUserAndGroup("rwr---");
        final RepositoryMap repositories = factory.sharedResources().repositories();
        int repoIndex;
        for (repoIndex = 0; !ScriptRepoHelper.SCRIPT_REPO.equals(repositories.descriptions.get(repoIndex).getHash().getValue());
                repoIndex++);
        final RepositoryPrx repo =  repositories.proxies.get(repoIndex);
        final String userDirectory = "Test_" + getClass().getName() + '_' + UUID.randomUUID();
        repo.makeDir("/" + userDirectory, false);
        final OriginalFile file = (OriginalFile) iQuery.findByString("OriginalFile", "name", userDirectory);
        Assert.assertEquals(file.getRepo().getValue(), ScriptRepoHelper.SCRIPT_REPO);
    }
}
