 /*
 * org.openmicroscopy.shoola.agents.editor.model.IField 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model;

import java.util.List;

import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
 * This interface specifies methods of a Field, which corresponds to a node
 * in the data tree.
 * IField extends the IAttributes interface, 
 * which has getAttribute() and setAttribute() methods to access the field
 * attributes.
 * A field may contain several parameters, so the IField has methods to get the 
 * count of parameters, and to get a parameter by index.
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface IField 
	extends IAttributes {
	
	/**
	 * Returns the number of parameters for this field
	 * 
	 * @return 	the number of parameters for this field
	 */
	public int getContentCount();
	
	/**
	 * Returns a parameter object at the specified index
	 * 
	 * @param index		
	 * @return
	 */
	public IFieldContent getContentAt(int index);
	
	/**
	 * Adds a parameter object to the field
	 * 
	 * @param param
	 */
	public void addContent(IFieldContent param);
	
	/**
	 * Adds a parameter to the list for this field,
	 * at the specified index.
	 */
	public void addContent(int index, IFieldContent param);
	
	/**
	 * Removes a parameter object from the field
	 * 
	 * @param param		The object to remove
	 * @return		The index of the parameter before removal (or -1 if not found)
	 */
	public int removeContent(IFieldContent param);
	
	/**
	 * Removes a content object from the field
	 * 
	 * @param index		The index of the object to remove
	 */
	public void removeContent(int index);
	
	/**
	 * Returns a list of the parameters for this field
	 * 
	 * @return 		see above
	 */
	public List<IParam> getParams();
	
	/**
	 * Indicates whether all parameters have been filled out. 
	 * 
	 * @return		True if all the parameters have been filled. 
	 */
	public boolean isFieldFilled();
	
	/**
	 * Returns a String containing the field description, plus the 
	 * tool-tip-text from it's parameters. 
	 * 
	 * @return		see above.
	 */
	public String getToolTipText();
	
	/**
	 * Returns true if the is a lock on this field.
	 * 
	 * @return		see above
	 */
	public boolean isFieldLocked();
	
	/**
	 * Returns this field's lock (or null if no lock).
	 * 
	 * @return		see above
	 */
	public Lock getLock();
	
	/**
	 * Returns a html-formatted string representation of this field. 
	 * 
	 * @return		see above.
	 */
	public String toHtmlString();
}
