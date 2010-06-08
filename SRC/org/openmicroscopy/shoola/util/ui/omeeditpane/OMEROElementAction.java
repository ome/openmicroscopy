/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEROElementAction 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;



//Java imports
import java.util.StringTokenizer;

//Third-party libraries

//Application-internal dependencies

/** 
 * Sets the value and type.
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
public class OMEROElementAction
	implements SelectionAction
{

	/** The type of the element action. */
	private String 	type;
	
	/** The value of the type. */
	private String	value;
	
	/**
	 * Returns the type.
	 * 
	 * @return See above.
	 */
	public String getType() { return type; }
	
	/**
	 * Returns the value.
	 * 
	 * @return See above.
	 */
	public String getValue() { return value; }
	
	/**
	 * Implemented as specified by {@link SelectionAction} I/F.
	 * @see SelectionAction#onSelection(String)
	 */
	public void onSelection(String selectedText)
	{
		 int index = 0; 
		 String tok[] = new String [500];
		 StringTokenizer st = new StringTokenizer(selectedText,": []");  
		 while (st.hasMoreTokens()) // make sure there is stuff to get
		 { 
			 tok[index] = st.nextToken(); 
		     index++; 
		 }
		 type = tok[1];
		 value = tok[2];
	}	
	
}



