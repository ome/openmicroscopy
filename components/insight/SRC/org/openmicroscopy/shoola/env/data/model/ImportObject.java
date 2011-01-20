/*
 * org.openmicroscopy.shoola.env.data.model.ImportObject
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.StatusLabel;

/**
 * Helper class hosting information about the file to import.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImportObject
{

	/** The file to import. */
	private File 		file;
	
	/** The object displaying the import status. */
	private StatusLabel status;
	
	/** The name within the system to give to the file. */
	private String		name;
	
	/** The depth. */
	private int			depth;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file 		The file to import.
	 * @param status 	The object displaying the import status.
	 */
	public ImportObject(File file, StatusLabel status)
	{
		this(file, status, null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file 		The file to import.
	 * @param status 	The object displaying the import status.
	 * @param name		The name of the file.
	 */
	public ImportObject(File file, StatusLabel status, String name)
	{
		if (file == null)
			throw new IllegalArgumentException("No file to import.");
		this.file = file;
		this.name = name;
		this.status = status;
		depth = -1;
	}
	
	/**
	 * Sets the depth.
	 * 
	 * @param depth The value to set.
	 */
	public void setDepth(int depth) { this.depth = depth; }
	
	/**
	 * Returns the depth.
	 * 
	 * @return See above.
	 */
	public int getDepth() { return depth; }
	/**
	 * Returns the file to import.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/** 
	 * Returns the name to give to the imported image.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the component indicating the status of the import.
	 * 
	 * @return See above.
	 */
	public StatusLabel getStatus() { return status; }
	
}
