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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.model.ROI;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class XMLFileIOStrategy
{
	
	/** Used to write the rois to the stream. */
	private OutputStrategy outputStrategy;
	
	/** Used to read the rois from the stream. */
	private InputStrategy  inputStrategy;
	
	/** Creates a new instance. */
	public XMLFileIOStrategy()
	{
		outputStrategy = new OutputStrategy();
		inputStrategy = new InputStrategy();
	}
	
	/** Indicates to reset the identifier when loading from local file.*/
	public void reset()
	{
		inputStrategy.reset();
	}
	
	/**
	 * Converts the ROIs hosted in the passed input stream.
	 * 
	 * @param input
	 * @param component
	 * @throws NoSuchROIException
	 * @throws ParsingException
	 * @throws ROICreationException
	 * @return list of the newly loaded ROI.
	 */
	public List<ROI> read(InputStream input, ROIComponent component)
		throws NoSuchROIException, ParsingException, ROICreationException, 
				ROICreationException	   
	{
		if (input == null)
			throw new NullPointerException("No input stream specified.");
		//TODO: REview that code.
		List<ROI> roiList = inputStrategy.readROI(input, component);
		try
		{
			input.close();
		}
		catch (IOException e)
		{
			throw new ParsingException("Unable to close input file.");
		}
		return roiList;
	}
	
	/**
	 * 
	 * @param output
	 * @param component
	 * @throws ParsingException
	 */
	public void write(OutputStream output, ROIComponent component)
		throws ParsingException
	{
		if (output == null)
			throw new NullPointerException("No input stream specified.");
		outputStrategy.write(output, component);
		try
		{
			output.close();
		}
		catch (IOException e)
		{
			throw new ParsingException("Unable to close output file.");
		}
	}
	
}


