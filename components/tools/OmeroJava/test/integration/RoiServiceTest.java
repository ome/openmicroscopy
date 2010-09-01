/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static omero.rtypes.*;

import java.util.Iterator;
import java.util.List;

//Java imports

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IRoiPrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.Annotation;
import omero.model.Image;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;

/**
 * Collections of tests for the handling ROIs.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 */
@Test(groups = { "client", "integration", "blitz" })
public class RoiServiceTest 
	extends AbstractTest 
{
	
	/**
	 * Tests the creation of ROIs with rectangular shapes and removes one shape.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test(groups = "ticket:1679")
    public void testRemoveShape() 
    	throws Exception
    {
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
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
        assertTrue(shapes.size() == (n-1));
    }

	/**
	 * Tests the retrieval of an ROI.
	 * This test uses the <code>findByImage</code> method.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testFindByImage() 
    	throws Exception
    {
    	IRoiPrx svc = factory.getRoiService();
    	//create the roi.
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
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
	 * Tests the retrieval of ROI measurements.
	 * This test uses the <code>getRoiMeasurements</code> method.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testRoisMeasurementRetrieval() 
    	throws Exception
    {
    	//create the roi.
        Image image = (Image) iUpdate.saveAndReturnObject(
        		mmFactory.simpleImage(0));
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
        //no measurements
        RoiOptions options = new RoiOptions();
		options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);
		
		IRoiPrx svc = factory.getRoiService();
		List<Annotation> l = 
			svc.getRoiMeasurements(image.getId().getValue(), options);
		assertTrue(l.size() == 0);
		
		//create measurements.
    }
    
	/**
	 * Tests the retrieval of an ROI with measurement.
	 * This test uses the <code>getMeasuredRoisMap</code> method.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testMeasuredRoisMap() 
    	throws Exception
    {
    	
    }
    
	/**
	 * Tests the retrieval of a table with ROI measurements.
	 * This test uses the <code>getTable</code> method.
	 * @throws Exception  Thrown if an error occurred.
	 */
    @Test
    public void testTableResult() 
    	throws Exception
    {
    	
    }
    
}
