 /*
 * treeEditingComponents.ITreeEditComp 
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
package treeEditingComponents;

//Java imports

//Third-party libraries

//Application-internal dependencies

import treeModel.fields.IParam;


/** 
 * The editing components for each parameter type should implement this
 * interface. 
 * attributeEdited should be called when an attribute changes.
 * This should first make sure that getAttributeName
 * will return the name of the most recently changed attribute.
 * Then, notify propertyChangeListeners.
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
public interface ITreeEditComp {

	/**
	 * This Field listens for changes to this property in the parameter
	 * editing components it contains. 
	 * change to this property indicates that the value of the parameter
	 * has changed, requiring the change to be saved to the data model. 
	 */
	public static final String VALUE_CHANGED_PROPERTY = "valueChangedProperty";

	
	/**
	 * This method can be called from other classes, or called from
	 * subclasses of this class. 
	 * 
	 * fires PropertyChange for VALUE_CHANGED_PROPERTY
	 * Listener (FieldPanel) will edit the attribute, adding the 
	 * edit to the undo/redo queue. 
	 * To to this, it will need to call getAttributeName() and 
	 * getParameter(). 
	 */
	public void attributeEdited(String attributeName, String newValue);
	
	/**
	 * Gets the parameter object that this UI component is editing. 
	 */
	public IParam getParameter();
	
	/**
	 * Gets the name of the last-edited attribute.
	 */
	public String getAttributeName();
	
	/**
	 * This method can be specify a display name for the last edit.
	 * Used for undo/redo. 
	 * eg displayName = "Edit Time" 
	 * Undo toolTipText = "Undo Edit Time" etc. 
	 * 
	 * @return	A name for the edit, to display in UI. 
	 */
	public String getEditDisplayName();
}
