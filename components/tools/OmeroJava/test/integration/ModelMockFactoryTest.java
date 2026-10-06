/*
 * Copyright 2006-2017 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import ome.specification.XMLMockObjects;
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.Annotation;
import omero.model.BooleanAnnotation;
import omero.model.CommentAnnotation;
import omero.model.Ellipse;
import omero.model.IObject;
import omero.model.ImageAnnotationLink;
import omero.model.Line;
import omero.model.LongAnnotation;
import omero.model.Mask;
import omero.model.Pixels;
import omero.model.Point;
import omero.model.Polyline;
import omero.model.Rectangle;
import omero.model.Roi;
import omero.model.Shape;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for ModelMockFactory
 */
public class ModelMockFactoryTest extends AbstractServerImportTest {

    private static final String OME_TIFF = "ome.tiff";

    /**
     * Tests that an OME-TIFF created from an image with ROI data can be
     * imported and that all expected shapes survive the round-trip.
     */
    @Test
    public void testCreateOMETiffFileWithROI() throws Throwable {
        XMLMockObjects xml = new XMLMockObjects();
        File tiffFile = mmFactory.createOMETiffFile(xml.createImageWithROI());
        try {
            List<Pixels> pixels = importFile(tiffFile, OME_TIFF);
            Assert.assertNotNull(pixels);
            Assert.assertFalse(pixels.isEmpty());

            long imageId = pixels.get(0).getImage().getId().getValue();

            IRoiPrx roiSvc = factory.getRoiService();
            RoiResult result = roiSvc.findByImage(imageId, new RoiOptions());
            Assert.assertNotNull(result);
            List<Roi> rois = result.rois;
            Assert.assertNotNull(rois);
            Assert.assertEquals(rois.size(), XMLMockObjects.SIZE_C.intValue());

            Iterator<Roi> roiIter = rois.iterator();
            while (roiIter.hasNext()) {
                Roi roi = roiIter.next();
                List<Shape> shapes = roi.copyShapes();
                Assert.assertNotNull(shapes);
                Assert.assertEquals(shapes.size(), XMLMockObjects.SHAPES.length);
                int count = 0;
                for (Shape shape : shapes) {
                    if (shape instanceof Rectangle || shape instanceof Line
                            || shape instanceof Ellipse
                            || shape instanceof Polyline || shape instanceof Mask
                            || shape instanceof Point) {
                        count++;
                    }
                }
                Assert.assertEquals(count, XMLMockObjects.SHAPES.length);
            }
        } finally {
            if (tiffFile != null && tiffFile.exists()) {
                tiffFile.delete();
            }
        }
    }

    /**
     * Tests that an OME-TIFF created from an annotated image can be imported
     * and that all expected annotation types survive the round-trip.
     */
    @Test
    public void testCreateOMETiffFileWithAnnotations() throws Throwable {
        XMLMockObjects xml = new XMLMockObjects();
        File tiffFile = mmFactory.createOMETiffFile(xml.createAnnotatedImage());
        try {
            List<Pixels> pixels = importFile(tiffFile, OME_TIFF);
            Assert.assertNotNull(pixels);
            Assert.assertFalse(pixels.isEmpty());

            long imageId = pixels.get(0).getImage().getId().getValue();

            String hql = "select l from ImageAnnotationLink as l "
                    + "left outer join fetch l.parent as p "
                    + "join fetch l.child "
                    + "where p.id = :id";
            List<IObject> links = iQuery.findAllByQuery(hql, new ParametersI().addId(imageId));

            Assert.assertTrue(links.size() >= XMLMockObjects.ANNOTATIONS.length,
                    String.format("Expected at least %d annotations, got %d",
                            XMLMockObjects.ANNOTATIONS.length, links.size()));

            int count = 0;
            for (IObject obj : links) {
                Annotation a = ((ImageAnnotationLink) obj).getChild();
                if (a instanceof CommentAnnotation
                        || a instanceof TagAnnotation
                        || a instanceof TermAnnotation
                        || a instanceof BooleanAnnotation
                        || a instanceof LongAnnotation) {
                    count++;
                }
            }
            Assert.assertEquals(count, XMLMockObjects.ANNOTATIONS.length);
        } finally {
            if (tiffFile != null && tiffFile.exists()) {
                tiffFile.delete();
            }
        }
    }
}
