/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import omero.ServerError;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.TablePrx;
import omero.model.Annotation;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Rectangle;
import omero.model.RectangleI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Shape;
import omero.model.Well;

import org.testng.Assert;
import org.testng.annotations.Test;

import omero.gateway.model.FileAnnotationData;

/**
 * Collections of tests for the handling ROIs.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 */
public class RoiServiceTest extends AbstractServerTest {

    /**
     * Tests the creation of ROIs with rectangular shapes and removes one shape.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:1679")
    public void testRemoveShape() throws Exception {
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(i));
            rect.setTheT(omero.rtypes.rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        List<Shape> shapes = roi.copyShapes();
        Shape shape = roi.getShape(0);
        roi.removeShape(shape);
        int n = shapes.size();
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        shapes = roi.copyShapes();
        Assert.assertEquals(shapes.size(), (n - 1));
    }

    /**
     * Tests the retrieval of an ROI. This test uses the
     * <code>findByImage</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testFindByImage() throws Exception {
        IRoiPrx svc = factory.getRoiService();
        // create the roi.
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());
        Roi roi = new RoiI();
        roi.setImage(image);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(i));
            rect.setTheT(omero.rtypes.rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        RoiResult r = svc.findByImage(image.getId().getValue(),
                new RoiOptions());
        Assert.assertNotNull(r);
        List<Roi> rois = r.rois;
        Assert.assertEquals(rois.size(), 1);
        List<Shape> shapes;
        Iterator<Roi> i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(), 3);
        }
    }

    /**
     * Tests the retrieval of ROI measurements. This test uses the
     * <code>getRoiMeasurements</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testRoisMeasurementRetrieval() throws Exception {
        Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        // create the roi.
        Image image = well.getWellSample(0).getImage();
        Roi roi = new RoiI();
        roi.setImage(image);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(i));
            rect.setTheT(omero.rtypes.rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        // no measurements
        RoiOptions options = new RoiOptions();
        options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);

        IRoiPrx svc = factory.getRoiService();
        List<Annotation> l = svc.getRoiMeasurements(image.getId().getValue(),
                options);
        Assert.assertEquals(l.size(), 0);

        // create measurements.
        // First create a table
        String uuid = "Measurement_" + UUID.randomUUID().toString();
        TablePrx table = factory.sharedResources().newTable(1, uuid);
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        table.initialize(columns);
        Assert.assertNotNull(table);
        OriginalFile of = table.getOriginalFile();
        Assert.assertTrue(of.getId().getValue() > 0);
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.MEASUREMENT_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        // link fa to ROI
        List<IObject> links = new ArrayList<IObject>();
        RoiAnnotationLink rl = new RoiAnnotationLinkI();
        rl.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        rl.setParent(new RoiI(roi.getId().getValue(), false));
        links.add(rl);
        PlateAnnotationLink il = new PlateAnnotationLinkI();
        il.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        il.setParent(new PlateI(p.getId().getValue(), false));
        links.add(il);
        iUpdate.saveAndReturnArray(links);

        l = svc.getRoiMeasurements(image.getId().getValue(), options);
        Assert.assertEquals(l.size(), 1);
        Assert.assertTrue(l.get(0) instanceof FileAnnotation);
        // Now create another file annotation linked to the ROI

        links.clear();
        of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        fa = new FileAnnotationI();
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        rl = new RoiAnnotationLinkI();
        rl.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        rl.setParent(new RoiI(roi.getId().getValue(), false));
        links.add(rl);
        il = new PlateAnnotationLinkI();
        il.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        il.setParent(new PlateI(p.getId().getValue(), false));
        links.add(il);
        iUpdate.saveAndReturnArray(links);
        // we should still have one
        l = svc.getRoiMeasurements(image.getId().getValue(), options);
        Assert.assertEquals(l.size(), 1);
    }

    /**
     * Tests the retrieval of an ROI with measurement. This test uses the
     * <code>getMeasuredRoisMap</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testMeasuredRoisMap() throws Exception {
        Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        // create the roi.
        Image image = well.getWellSample(0).getImage();
        Roi roi = new RoiI();
        roi.setImage(image);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(i));
            rect.setTheT(omero.rtypes.rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        // no measurements
        RoiOptions options = new RoiOptions();
        options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);

        IRoiPrx svc = factory.getRoiService();
        // create measurements.
        // First create a table
        String uuid;
        TablePrx table;
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        OriginalFile of;
        FileAnnotation fa;
        RoiAnnotationLink rl;
        PlateAnnotationLink il;
        // link fa to ROI
        List<IObject> links = new ArrayList<IObject>();
        int n = 1;
        for (int i = 0; i < n; i++) {
            uuid = "Measurement_" + UUID.randomUUID().toString();
            table = factory.sharedResources().newTable(1, uuid);
            table.initialize(columns);
            of = table.getOriginalFile();
            fa = new FileAnnotationI();
            fa.setNs(omero.rtypes.rstring(FileAnnotationData.MEASUREMENT_NS));
            fa.setFile(of);
            fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
            rl = new RoiAnnotationLinkI();
            rl.setChild(new FileAnnotationI(fa.getId().getValue(), false));
            rl.setParent(new RoiI(roi.getId().getValue(), false));
            links.add(rl);
            il = new PlateAnnotationLinkI();
            il.setChild(new FileAnnotationI(fa.getId().getValue(), false));
            il.setParent(new PlateI(p.getId().getValue(), false));
            links.add(il);
            iUpdate.saveAndReturnArray(links);
        }

        List<Annotation> l = svc.getRoiMeasurements(image.getId().getValue(),
                options);
        Assert.assertEquals(l.size(), n);
        FileAnnotation f = (FileAnnotation) l.get(0);

        List<Long> ids = new ArrayList<Long>();
        ids.add(f.getId().getValue());
        Map<Long, RoiResult> values = svc.getMeasuredRoisMap(image.getId()
                .getValue(), ids, options);
        Assert.assertNotNull(values);
        Assert.assertEquals(values.size(), 1);
        Assert.assertNotNull(values.get(f.getId().getValue()));
    }

    /**
     * Tests the retrieval of a table with ROI measurements. This test uses the
     * <code>getTable</code> method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testTableResult() throws Exception {
        Plate p = mmFactory.createPlate(1, 1, 1, 0, true);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        List<Well> results = loadWells(p.getId().getValue(), true);
        Well well = results.get(0);
        // create the roi.
        Image image = well.getWellSample(0).getImage();
        Roi roi = new RoiI();
        roi.setImage(image);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(i));
            rect.setTheT(omero.rtypes.rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        // no measurements
        RoiOptions options = new RoiOptions();
        options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);

        IRoiPrx svc = factory.getRoiService();
        // create measurements.
        // First create a table
        String uuid = "Measurement_" + UUID.randomUUID().toString();
        TablePrx table = factory.sharedResources().newTable(1, uuid);
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        table.initialize(columns);
        OriginalFile of = table.getOriginalFile();
        Assert.assertTrue(of.getId().getValue() > 0);
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.MEASUREMENT_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        // link fa to ROI
        List<IObject> links = new ArrayList<IObject>();
        RoiAnnotationLink rl = new RoiAnnotationLinkI();
        rl.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        rl.setParent(new RoiI(roi.getId().getValue(), false));
        links.add(rl);
        PlateAnnotationLink il = new PlateAnnotationLinkI();
        il.setChild(new FileAnnotationI(fa.getId().getValue(), false));
        il.setParent(new PlateI(p.getId().getValue(), false));
        links.add(il);
        iUpdate.saveAndReturnArray(links);
        List<Annotation> l = svc.getRoiMeasurements(image.getId().getValue(),
                options);
        FileAnnotation f = (FileAnnotation) l.get(0);

        table = svc.getTable(f.getId().getValue());
        Assert.assertNotNull(table);
        Column[] cols = table.getHeaders();
        Assert.assertEquals(cols.length, columns.length);
    }

    /**
     * Compare findByImage, findByROI and findByPlane results (similar to
     * test/integration/test_rois.py TestRois )
     */
    @Test
    public void testFindByImageRoiPlane() throws Exception {
        IRoiPrx svc = factory.getRoiService();
        // create the roi.
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .simpleImage());

        Roi r1 = createRoi(image, 0, 0);
        Roi r2 = createRoi(image, 0, 1);
        Roi r3 = createRoi(image, 1, 0);
        Roi r4 = createRoi(image, 1, 1);

        RoiResult r = svc.findByImage(image.getId().getValue(),
                new RoiOptions());
        Assert.assertNotNull(r);
        List<Roi> rois = r.rois;
        Assert.assertEquals(rois.size(), 4);
        List<Shape> shapes;
        Roi roi;
        Iterator<Roi> i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(), 3);
        }

        r = svc.findByRoi(r1.getId().getValue(), new RoiOptions());
        rois = r.rois;
        Assert.assertEquals(rois.size(), 1);
        i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            Assert.assertEquals(roi.getId().getValue(), r1.getId().getValue());
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(),  3);
        }

        r = svc.findByRoi(r2.getId().getValue(), new RoiOptions());
        rois = r.rois;
        Assert.assertEquals(rois.size(),  1);
        i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            Assert.assertEquals(roi.getId().getValue(), r2.getId().getValue());
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(), 3);
        }

        r = svc.findByPlane(image.getId().getValue(), 1, 0, new RoiOptions());
        rois = r.rois;
        Assert.assertEquals(rois.size(), 1);
        i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            Assert.assertEquals(roi.getId().getValue(),  r3.getId().getValue());
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(), 3);
        }

        r = svc.findByPlane(image.getId().getValue(), 1, 1, new RoiOptions());
        rois = r.rois;
        Assert.assertEquals(rois.size(), 1);
        i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            Assert.assertEquals(roi.getId().getValue(), r4.getId().getValue());
            shapes = roi.copyShapes();
            Assert.assertEquals(shapes.size(), 3);
        }
    }
    
    /**
     * Creates an ROI with 3 rectangluar shapes on the specified plane
     * 
     * @param img
     *            The Image the ROI will be linked to
     * @param z
     *            The Z plane
     * @param t
     *            The T plane
     * @return See above.
     * @throws ServerError
     *             If ROI couldn't be created
     */
    private Roi createRoi(Image img, int z, int t) throws ServerError {
        Roi roi = new RoiI();
        roi.setImage(img);
        Rectangle rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectangleI();
            rect.setX(omero.rtypes.rdouble(10));
            rect.setY(omero.rtypes.rdouble(10));
            rect.setWidth(omero.rtypes.rdouble(10));
            rect.setHeight(omero.rtypes.rdouble(10));
            rect.setTheZ(omero.rtypes.rint(z));
            rect.setTheT(omero.rtypes.rint(t));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        return roi;
    }
}
