/*
 * training.ROIs 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;


//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.model.EllipseI;
import omero.model.Image;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import pojos.EllipseData;
import pojos.LineData;
import pojos.PointData;
import pojos.ROIData;
import pojos.RectangleData;
import pojos.ShapeData;

/** 
 * Sample code showing how interact with Region of interests.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ROIs 
	extends ConnectToOMERO
{
	
	/** Information to edit.*/
	private long imageId = 27544;
	
	private Image image;

	/** 
	 * Retrieve an image if the identifier is known.
	 */
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId).asImage();
		if (image == null)
			throw new Exception("Image does not exist. Check ID.");
	}
	
	/** 
	 * Creates roi and retrieve it.
	 */
	private void createROIs()
		throws Exception
	{
		Roi roi = new RoiI();
        roi.setImage(image);
        Rect rect = new RectI();
        rect.setX(omero.rtypes.rdouble(10));
        rect.setY(omero.rtypes.rdouble(10));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        rect.setTheZ(omero.rtypes.rint(0));
        rect.setTheT(omero.rtypes.rint(0));
        roi.addShape(rect);
        
        //Create a rectangular shape
        rect = new RectI();
        rect.setX(omero.rtypes.rdouble(10));
        rect.setY(omero.rtypes.rdouble(10));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        rect.setTheZ(omero.rtypes.rint(1));
        rect.setTheT(omero.rtypes.rint(0));
        
        //Add the shape
        roi.addShape(rect);
        
        //Create an ellipse.
        EllipseI ellipse = new EllipseI();
        ellipse.setCx(omero.rtypes.rdouble(10));
        ellipse.setCy(omero.rtypes.rdouble(10));
        ellipse.setRx(omero.rtypes.rdouble(10));
        ellipse.setRy(omero.rtypes.rdouble(10));
        ellipse.setTheZ(omero.rtypes.rint(1));
        ellipse.setTheT(omero.rtypes.rint(0));
        ellipse.setTextValue(omero.rtypes.rstring("ellipse text"));
        
        //Add the shape
        roi.addShape(ellipse);
        roi = (Roi) entryUnencrypted.getUpdateService().saveAndReturnObject(roi);
        
        //now check that the shape has been added.
        ROIData roiData = new ROIData(roi);
        //Retrieve the shape on plane (0, 0)
        List<ShapeData> shapes = roiData.getShapes(0, 0);
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
			ShapeData shape = i.next();
			//plane info
			int z = shape.getZ();
			int t = shape.getT();
			long id = shape.getId();
			if (shape instanceof RectangleData) {
				RectangleData rectData = (RectangleData) shape;
				//Handle rectangle
			} else if (shape instanceof EllipseData) {
				EllipseData ellipseData = (EllipseData) shape;
				//Handle ellipse
			} else if (shape instanceof LineData) {
				LineData lineData = (LineData) shape;
				//Handle line
			} else if (shape instanceof PointData) {
				PointData pointData = (PointData) shape;
				//Handle line
			}
		}
        
        
        // Retrieve the roi linked to an image
        RoiResult r = entryUnencrypted.getRoiService().findByImage(
        		image.getId().getValue(), new RoiOptions());
        if (r == null)
        	throw new Exception("No rois linked to Image:"+image.getId().getValue());
        List<Roi> rois = r.rois;
        if (rois == null)
        	throw new Exception("No rois linked to Image:"+image.getId().getValue());
        List<Shape> list;
        Iterator<Roi> j = rois.iterator();
        while (j.hasNext()) {
			roi = j.next();
			list = roi.copyShapes();
			//size = 2
			//remove first shape
			roi.removeShape(list.get(0));
			//update the roi
			entryUnencrypted.getUpdateService().saveAndReturnObject(roi);
		}
        
        //Check that the shape does not have shape.
        r = entryUnencrypted.getRoiService().findByImage(
        		image.getId().getValue(), new RoiOptions());
        if (r == null)
        	throw new Exception("No rois linked to Image:"+image.getId().getValue());
        rois = r.rois;
        if (rois == null)
        	throw new Exception("No rois linked to Image:"+image.getId().getValue());
        j = rois.iterator();
        while (j.hasNext()) {
			roi = j.next();
			list = roi.copyShapes();
			//size = 1
		}
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	ROIs()
	{
		try {
			connect();
			loadImage();
			createROIs();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new ROIs();
		System.exit(0);
	}

}
