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
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelInfo;
import org.openmicroscopy.shoola.util.ui.measurement.model.ImageModel;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;

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
	
	public void saveResults(String filename)
	{
		roiComponent.saveResults(filename);
	}
	
	public Dimension getImageDimensions()
	{
		return new Dimension((int)imageModel.width, (int)imageModel.height);
	}
	
	public double getMicronsPixelX()
	{
		return imageModel.getMicronsPixelX();
	}
	
	public double getMicronsPixelY()
	{
		return imageModel.getMicronsPixelY();
	}
	
	public double getMicronsPixelZ()
	{
		return imageModel.getMicronsPixelZ();
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
	
	public long addROI(ROIFigure fig) throws 		ROICreationException, 
												ROIShapeCreationException, NoSuchROIException
	{
		ROI roi = roiComponent.createROI();
		ROIShape newShape = new ROIShape(roi, imageModel.getCoord3D(), fig, fig.getBounds());
		roiComponent.addShape(roi.getID(), imageModel.getCoord3D(), newShape);
		return roi.getID();
	}
	
	public ShapeList getShapeList(Coord3D coord) throws NoSuchShapeException
	{
		return roiComponent.getShapeList(coord);
	}

	/**
	 * @return 
	 * 
	 */
	public TreeMap<Long, ROI> getROIMap() 
	{
		return roiComponent.getROIMap();
		
	}
	
	/*public void saveMeasurements(File file)
	{
        PrintWriter outputStream = null;
        try 
        {
        	outputStream = 
	    	new PrintWriter(new FileWriter(file));
        	printHeadings(outputStream);
        	TreeMap<Long, ROI>  roiMap = roiComponent.getROIMap();
        	Iterator keyIterator = roiMap.keySet().iterator();
        	
        	while(keyIterator.hasNext())
        	{
        		printROIAttributes(outputStream, roiMap.get((Long)keyIterator.next()));
        	}
        	
                
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			if (outputStream != null) 
				outputStream.close();
		}
	}
	
	private void printROIAttributes(PrintWriter output, ROI roi)
	{
		TreeMap<Coord3D, ROIShape> shapes = roi.getShapes();
		Iterator roiShapeIterator = shapes.keySet().iterator();
		if(!roiShapeIterator.hasNext())
			return;
		ROIShape shape = shapes.get((Coord3D)roiShapeIterator.next());
		ROIFigure figure = shape.getFigure();
		output.print(roi.getID()+",");
		output.print(FIGURETYPE.get(shape.getFigure())+",");
		output.print(shape.getCoord3D().getTimePoint()+",");
		output.print(shape.getCoord3D().getZSection()+",");
		if(shape.hasAnnotation(CENTRE))
		{
			Point2D centre = CENTRE.get(shape);
			output.print("("+centre.getX()+";"+centre.getY()+"),");
		}
		else
		{
			output.print(" ,");
		}
		if(shape.hasAnnotation(AREA))
		{
			double area = AREA.get(shape);
			output.print(area+",");
		}
		else
		{
			output.print(" ,");
		}
		if(shape.hasAnnotation(PERIMETER))
		{
			double perimeter = PERIMETER.get(shape);
			output.print(perimeter+",");
		}
		else
		{
			output.print(" ,");
		}
		
		ArrayList<Double> length = new ArrayList<Double>();
		if(shape.hasAnnotation(LENGTH))
			length = LENGTH.get(shape);
		ArrayList<Double> angle = new ArrayList<Double>();
		if(shape.hasAnnotation(ANGLE))
			angle = ANGLE.get(shape);
		
		
		int count = Math.max(length.size(), angle.size());
		boolean needFinalCR = true;
		
		
		for( int i = 1 ; i < count ; i++)
		{
			needFinalCR = false;
			if(i < length.size())
			{
				output.print(length.get(i)+",");
			}
			else
				output.print(" ,");
			
			if(i < angle.size())
			{
				output.print(angle.get(i)+",");
			}
			else
				output.print(" ,");
			output.println();
		}
		if(needFinalCR)
			output.println();
		
	}
	
	private void printHeadings(PrintWriter output)
	{
		output.print("ID,");
		output.print("TYPE,");
		output.print("T,");
		output.print("Z,");
		output.print("CENTRE,");
		output.print("AREA,");
		output.print("PERIMETER,");
		output.print("LENGTH,");
		output.print("ANGLE");
		output.println();
	}*/
	
}


