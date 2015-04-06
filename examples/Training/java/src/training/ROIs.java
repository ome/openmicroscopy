/*
 * training.ROIs 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

import omero.api.IContainerPrx;
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
import omero.sys.ParametersI;
import pojos.EllipseData;
import pojos.ImageData;
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
{
	
	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	
	/** Information to edit.*/
	private long imageId = 1;
	//end edit
	
	private ImageData image;

	/** Reference to the connector.*/
	private Connector connector;
	
	/**
	 * Loads the image.
	 * 
	 * @param imageID The id of the image to load.
	 * @return See above.
	 */
	private ImageData loadImage(long imageID)
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		List<Image> results = proxy.getImages(Image.class.getName(),
				Arrays.asList(imageID), new ParametersI());
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		return new ImageData(results.get(0));
	}
	
	/** 
	 * Creates roi and retrieve it.
	 */
	private void createROIs()
		throws Exception
	{
		Roi roi = new RoiI();
        roi.setImage(image.asImage());
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
        roi = (Roi) connector.getUpdateService().saveAndReturnObject(roi);
        
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
        RoiResult r = connector.getRoiService().findByImage(
        		image.getId(), new RoiOptions());
        if (r == null)
        	throw new Exception("No rois linked to Image:"+image.getId());
        List<Roi> rois = r.rois;
        if (rois == null)
        	throw new Exception("No rois linked to Image:"+image.getId());
        List<Shape> list;
        Iterator<Roi> j = rois.iterator();
        while (j.hasNext()) {
			roi = j.next();
			list = roi.copyShapes();
			//size = 2
			//remove first shape
			roi.removeShape(list.get(0));
			//update the roi
			connector.getUpdateService().saveAndReturnObject(roi);
		}
        
        //Check that the shape does not have shape.
        r = connector.getRoiService().findByImage(
        		image.getId(), new RoiOptions());
        if (r == null)
        	throw new Exception("No rois linked to Image:"+image.getId());
        rois = r.rois;
        if (rois == null)
        	throw new Exception("No rois linked to Image:"+image.getId());
        j = rois.iterator();
        while (j.hasNext()) {
			roi = j.next();
			list = roi.copyShapes();
			System.err.println(list.size());
		}
        //Load rois on a plane z=1, t=0
        r = connector.getRoiService().findByPlane(
                image.getId(), 1, 0, new RoiOptions());
        if (r == null)
            throw new Exception("No rois linked to image:"+image.getId());
        j = rois.iterator();
        while (j.hasNext()) {
            roi = j.next();
            list = roi.copyShapes();
            System.err.println(list.size());
        }
        //load a given rois
        r = connector.getRoiService().findByRoi(roi.getId().getValue(), null);
	}
	
	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	ROIs(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
			info.setImageId(imageId);
		}
		connector = new Connector(info);
		try {
			connector.connect();
			image = loadImage(info.getImageId());
			createROIs();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connector.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new ROIs(null);
		System.exit(0);
	}

}
