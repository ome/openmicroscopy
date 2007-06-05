/*
 * org.openmicroscopy.shoola.util.roi.io.XMLFileIOStrategy 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io;


//Java imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.exception.ROIShapeCreationException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROICollection;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

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
public 	class XMLFileIOStrategy
		implements XMLIOStrategy
{
	private OutputStrategy outputStrategy;
	private InputStrategy  inputStrategy;
	
	public XMLFileIOStrategy()
	{
		outputStrategy = new OutputStrategy();
		inputStrategy = new InputStrategy();
	}
	
	public void read(String filename, ROIComponent component) throws ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		File file = new File(filename);
		try {
			read(file, component);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void read(File file, ROIComponent component) throws IOException, ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
	    try 
	    {
	    	read(in, component);
	    } 
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    finally 
	    {
	    	if (in != null) 
	    	{
	    		in.close();
	        }
	    }
	}
	
	private void read(BufferedInputStream in, ROIComponent component) throws IOException, ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		ArrayList<ROI> roiList = readROI(in, component);
		
	}
	
	private ArrayList<ROI> readROI(InputStream in, ROIComponent component) throws IOException, ROIShapeCreationException, NoSuchROIException, ROICreationException
	{
		return inputStrategy.readROI(in, component);
	}
		 
	
	public void write(String filename, ROIComponent component)
	{
		File file = new File(filename);
		try {
			write(file, component);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void write(File file, ROIComponent component) throws IOException 
	{
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	    try 
	    {
	    	outputStrategy.write(out, component);
	    } 
	    finally 
	    {
	    	if (out != null) 
	    	{
	    		out.close();
	        }
	    }
	}

	
	
}


