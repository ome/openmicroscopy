/*
 * org.openmicroscopy.shoola.util.ui.roi.io.XMLFileIOStrategy 
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
package org.openmicroscopy.shoola.util.ui.roi.io;







//Java imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROICollection;

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
	}
	
	public void read(String filename, ROICollection collection)
	{
		File file = new File(filename);
		try {
			read(file, collection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void read(File file, ROICollection collection) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
	    try 
	    {
	    	read(in, collection);
	    } 
	    finally 
	    {
	    	if (in != null) 
	    	{
	    		in.close();
	        }
	    }
	}
	
	private void read(BufferedInputStream in, ROICollection collection)
	{
		ArrayList<ROI> roiList = readROI(in);
		
	}
	
	private ArrayList<ROI> readROI(InputStream in)
	{
		ArrayList<ROI> roiList = inputStrategy.readROI(in);
		return roiList;
	}
		 
	
	public void write(String filename, ROICollection collection)
	{
		File file = new File(filename);
		try {
			write(file, collection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void write(File file, ROICollection collection) throws IOException 
	{
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	    try 
	    {
	    	outputStrategy.write(out, collection);
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


