/*
 * org.openmicroscopy.shoola.util.ui.search.SearchObject 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.search;



//Java imports
import java.util.List;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class holding information needed for searching.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SearchObject
{

	/** The index associated to this object. */
	private int 			index;
	
	/** The icon associated to this object. */
	private ImageIcon		icon;
	
	/** The description associated to this object. */
	private String 			description;
	
	/** The result if any. */
	private List<String>	result;
	
	/** Creates a new instance when no context defined. */
	SearchObject()
	{
		index = -1;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index			The index associated to this object.
	 * @param icon			The icon associated to this object.
	 * @param description	The description associated to this object.
	 */
	public SearchObject(int index, ImageIcon icon, String description)
	{
		this.index = index;
		this.icon = icon;
		this.description = description;
	}
	
	/** 
	 * Sets the description associated to this object.
	 * 
	 * @param description The value to set.
	 */
	public void setDescription(String description) { this.description = description; }
	
	/**
	 * Sets the result.
	 * 
	 * @param result The value to set.
	 */
	public void setResult(List<String> result) { this.result = result; }
	
	/**
	 * Returns the icon.
	 * 
	 * @return See above.
	 */
	public ImageIcon getIcon() { return icon; }
	
	/**
	 * Returns the index.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the result.
	 * 
	 * @return See above.
	 */
	public List<String> getResult() { return result; }
	
	/**
	 * Returns the description.
	 * 
	 * @return See above.
	 */
	public String getDescription() { return description; }
	
}
