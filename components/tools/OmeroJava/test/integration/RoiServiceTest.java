/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.Shape;
import omero.model.Well;

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
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        List<Shape> shapes = roi.copyShapes();
        Shape shape = roi.getShape(0);
        roi.removeShape(shape);
        int n = shapes.size();
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        shapes = roi.copyShapes();
        assertTrue(shapes.size() == (n - 1));
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
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        RoiResult r = svc.findByImage(image.getId().getValue(),
                new RoiOptions());
        assertNotNull(r);
        List<Roi> rois = r.rois;
        assertTrue(rois.size() == 1);
        List<Shape> shapes;
        Iterator<Roi> i = rois.iterator();
        while (i.hasNext()) {
            roi = i.next();
            shapes = roi.copyShapes();
            assertTrue(shapes.size() == 3);
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
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
            roi.addShape(rect);
        }
        roi = (RoiI) iUpdate.saveAndReturnObject(roi);
        // no measurements
        RoiOptions options = new RoiOptions();
        options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);

        IRoiPrx svc = factory.getRoiService();
        List<Annotation> l = svc.getRoiMeasurements(image.getId().getValue(),
                options);
        assertTrue(l.size() == 0);

        // create measurements.
        // First create a table
        String uuid = "Measurement_" + UUID.randomUUID().toString();
        TablePrx table = factory.sharedResources().newTable(1, uuid);
        Column[] columns = new Column[1];
        columns[0] = new LongColumn("Uid", "", new long[1]);
        table.initialize(columns);
        assertNotNull(table);
        OriginalFile of = table.getOriginalFile();
        assertTrue(of.getId().getValue() > 0);
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
        assertTrue(l.size() == 1);
        assertTrue(l.get(0) instanceof FileAnnotation);
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
        assertTrue(l.size() == 1);
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
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
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
        assertTrue(l.size() == n);
        FileAnnotation f = (FileAnnotation) l.get(0);

        List<Long> ids = new ArrayList<Long>();
        ids.add(f.getId().getValue());
        Map<Long, RoiResult> values = svc.getMeasuredRoisMap(image.getId()
                .getValue(), ids, options);
        assertNotNull(values);
        assertTrue(values.size() == 1);
        assertNotNull(values.get(f.getId().getValue()));
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
        Rect rect;
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        for (int i = 0; i < 3; i++) {
            rect = new RectI();
            rect.setX(rdouble(10));
            rect.setY(rdouble(10));
            rect.setWidth(rdouble(10));
            rect.setHeight(rdouble(10));
            rect.setTheZ(rint(i));
            rect.setTheT(rint(0));
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
        assertTrue(of.getId().getValue() > 0);
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
        assertNotNull(table);
        Column[] cols = table.getHeaders();
        assertTrue(cols.length == columns.length);
    }

}
