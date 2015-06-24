/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.ElementSelectionAction 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;



//Java imports
import java.util.StringTokenizer;

//Third-party libraries

//Application-internal dependencies

/** 
 * Selects an element of the specified type.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ElementSelectionAction 
	implements SelectionAction
{

	/** One of the constants defined by {@link WikiDataObject}. */
	private int 			index;

	/** The id of the object. */
	private long			id;
	
	/** The name of the object. */
	private String			name;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by {@link WikiDataObject}.
	 */
	ElementSelectionAction(int index)
	{
		this.index = index;
		id = -1;
	}
	
	/**
	 * Returns the index of the {@link WikiDataObject}.
	 * 
	 * @return See above.
	 */
	int getWikiDataObjectIndex() { return index; }
	
	/**
	 * Returns the id of the object if a value is found.
	 * 
	 * @return See above.
	 */
	long getObjectID() { return id; }
	
	/** 
	 * Returns the name of the object. 
	 * 
	 * @return See above.
	 */
	String getObjectName() { return name; }
	
	/**
	 * Implemented as specified by {@link SelectionAction} I/F.
	 * @see SelectionAction#onSelection(String)
	 */
	public void onSelection(String selectedText)
	{
		if (selectedText == null) return;
		int index = 0; 
		String tok[] = new String [500];
		StringTokenizer st = new StringTokenizer(selectedText,":");
		while (st.hasMoreTokens()) { 
			tok[index] = st.nextToken(); 
			index++; 
		}
		String value = tok[1];
		//String key = tok[0];
		if (value != null)
		{
			value = value.trim();
			if (value.length() > 0) {
				value = value.replaceAll("\\W", "");
				id = Long.parseLong(value);
			}
			name = OMEWikiConstants.REF_ID;
		}
		else
		{
			name = OMEWikiConstants.REF_ID;
			id = -1;
		}
	}
	
}
