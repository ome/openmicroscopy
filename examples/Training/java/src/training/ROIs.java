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
import omero.model.Image;
import omero.model.Rect;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Shape;
import pojos.ROIData;
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
	
	/** The id of an image.*/
	private long imageId = 551;
	
	private Image image;

	/** 
	 * Retrieve an image if the identifier is known.
	 */
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId).asImage();
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
        rect = new RectI();
        rect.setX(omero.rtypes.rdouble(10));
        rect.setY(omero.rtypes.rdouble(10));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        rect.setTheZ(omero.rtypes.rint(1));
        rect.setTheT(omero.rtypes.rint(0));
        roi.addShape(rect);
        roi = (Roi) entryUnencrypted.getUpdateService().saveAndReturnObject(roi);
        
        //now check that the shape has been added.
        ROIData roiData = new ROIData(roi);
        //Retrieve the shape on plane (0, 0)
        List<ShapeData> shapes = roiData.getShapes(0, 0);
        Iterator<ShapeData> i = shapes.iterator();
        while (i.hasNext()) {
			ShapeData shape = i.next();
		}
        
        
        // Retrieve the roi linked to an image
        RoiResult r = entryUnencrypted.getRoiService().findByImage(
        		image.getId().getValue(), new RoiOptions());
        List<Roi> rois = r.rois;
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
        rois = r.rois;
        System.err.println(rois.size());
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
			client.closeSession();
		} catch (Exception e) {
			e.printStackTrace();
			if (client != null) client.closeSession();
		}
		
		
	}
	
	public static void main(String[] args) 
	{
		new ROIs();
	}
	
}
