/*
 * org.openmicroscopy.shoola.env.data.model.SaveAsParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.io.File;
import java.util.List;

import javax.swing.Icon;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Holds the information required to save the images as JPEG.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SaveAsParam
{

	/** Identifies the <code>Save As JPEG</code> script. */
	public static final String SAVE_AS_SCRIPT = 
		ScriptObject.EXPORT_PATH+"Batch_Image_Export.py";
	
	/** Indicates to the save the images as <code>JPEG</code>. */
	public static final int JPEG = 0;
	
	/** Indicates to the save the images as <code>PNG</code>. */
	public static final int PNG = 1;
	
	/** The folder where to save the data. */
	private File folder;
	
	/** The collection of objects to handle. */
	private List<pojos.DataObject> objects;
	
	/** One of the constants defined by this class. */
	private int index;
	
    /** The icon associated to the parameters. */
    private Icon icon;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param folder The folder where to save the data.
	 * @param objects The collection of objects to handle.
	 */
	public SaveAsParam(File folder, List<pojos.DataObject> objects)
	{
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No objects specified.");
		this.objects = objects;
		if (folder == null) folder = UIUtilities.getDefaultFolder();
		this.folder = folder;
		index = JPEG;
	}
	
	/**
	 * Sets the icon associated to the activity.
	 * 
	 * @param icon The value to set.
	 */
	public void setIcon(Icon icon) { this.icon = icon; }
	
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/** 
	 * Returns the index, one of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the index as a string.
	 * 
	 * @return See above.
	 */
	public String getIndexAsString()
	{
		switch (index) {
			case PNG: return "PNG";
			case JPEG:
			default:
				return "JPEG";
		}
	}
	
	/**
	 * Returns the folder where to save the data.
	 * 
	 * @return See above.
	 */
	public File getFolder() { return folder; }
	
	/**
	 * Returns the collection of objects to handle.
	 * 
	 * @return See above.
	 */
	public List<pojos.DataObject> getObjects() { return objects; }
	
}
