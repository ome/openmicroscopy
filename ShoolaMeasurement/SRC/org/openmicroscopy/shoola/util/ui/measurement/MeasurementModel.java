/*
 * measurement.MeasurementModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.measurement;

//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

//Third-party libraries
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelInfo;
import org.openmicroscopy.shoola.util.ui.measurement.model.ImageModel;
import org.openmicroscopy.shoola.util.ui.roi.ROIComponent;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.ui.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MeasurementModel
	extends Component
{
	private ImageModel		imageModel;
	private ROIComponent	roiComponent;
	private int				currentChannel;
	
	
	public  MeasurementModel(ImageModel model, ROIComponent roiComponent)
	{
		imageModel = model;
		this.roiComponent = roiComponent;
		currentChannel = 0;
	}
	
	public Dimension getImageDimensions()
	{
		return new Dimension((int)imageModel.width, (int)imageModel.height);
	}
	
	public void setCoord3D(Coord3D coord)
	{
		imageModel.setCoord3D(coord);
	}
	
	public Coord3D getCoord3D()
	{
		return imageModel.getCoord3D();
	}
	
	public ChannelInfo getChannelInfo()
	{
		return imageModel.channelInfo;
	}
	
	
	public int getCurrentChannel()
	{
		return currentChannel;
	}
	
	public long getNextID()
	{
		return roiComponent.getNextID();
	}
	
	public ROI getROI(long id) throws NoSuchROIException
	{
		return roiComponent.getROI(id);
	}
	
	public void removeROIShape(long id, Coord3D coord) throws NoSuchROIException, NoSuchShapeException
	{
		roiComponent.deleteShape(id, coord);
	}
	
	public long addROI(Figure fig) throws 		ROICreationException, 
												ROIShapeCreationException, NoSuchROIException
	{
		ROI roi = roiComponent.createROI();
		ROIShape newShape = new ROIShape(roi, imageModel.getCoord3D(), fig, fig.getBounds());
		//roi.addShape(newShape);
		roiComponent.addShape(roi.getID(), imageModel.getCoord3D(), newShape);
		return roi.getID();
	}
	
	public ShapeList getShapeList(Coord3D coord) throws NoSuchShapeException
	{
		return roiComponent.getShapeList(coord);
	}
}


