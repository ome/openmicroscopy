/*
 * org.openmicroscopy.shoola.env.data.ProcessReport 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.Map;

//Third-party libraries


//Application-internal dependencies
import omero.cmd.ERR;
import omero.cmd.GraphException;

/** 
 * Error that occurred when moving data, deleting etc.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ProcessReport {

	/** The error to handle.*/
	private ERR error;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param error The error to handle.
	 */
	public ProcessReport(ERR error)
	{
		if (error == null)
			throw new IllegalArgumentException("No error to handle");
		this.error = error;
	}
	
	/** 
	 * Returns the type of error.
	 * 
	 * @return See above.
	 */
	public String getCategory() { return error.category; }
	
	/** 
	 * Returns the name of error.
	 * 
	 * @return See above.
	 */
	public String getName() { return error.name; }
	
	/** 
	 * Returns the name of error.
	 * 
	 * @return See above.
	 */
	public Map<String, String> getDetails() { return error.parameters;}
	
	/** 
	 * Returns the {@link GraphException} if this ProcessReport represents
	 * a GraphException (<code>null</code> if it doesn't)
	 * 
	 * @return See above.
	 */
	public GraphException getGraphException() {
		if(error instanceof GraphException)
			return (GraphException) error;
		return null;
	}
	
}
