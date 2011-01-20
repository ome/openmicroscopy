/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.ImportableObject
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.io.File;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/**
 * Helper class where parameters required for the imports are stored.
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
public class ImportableObject
{

	/** The collection of files to import. */
	private Map<File, String> 	files;
	
	/** Flag indicating to archive the files or not. */
	private boolean 	archived;
	
	/** The depth. */
	private int			depth;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param files 	The collection of files to import.
	 * @param archived 	Pass <code>true</code> to archive the files, 
	 * 					<code>false</code> otherwise.
	 */
	ImportableObject(Map<File, String> files, boolean archived)
	{
		this.files = files;
		this.archived = archived;
		depth = -1;
	}
	
	/**
	 * Sets the depth.
	 * 
	 * @param depth The value to set.
	 */
	void setDepth(int depth) { this.depth = depth; }
	
	/**
	 * Returns the depth.
	 * 
	 * @return See above.
	 */
	public int getDepth() { return depth; }
	
	/**
	 * Returns the collection of files to import.
	 * 
	 * @return See above.
	 */
	public Map<File, String> getFiles() { return files; }
	
	/**
	 * Returns <code>true</code> to archive the files, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isArchived() { return archived; }
	
}
