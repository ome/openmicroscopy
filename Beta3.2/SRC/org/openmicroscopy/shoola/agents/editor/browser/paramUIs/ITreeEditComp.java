 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;

/** 
 * The editing components for each parameter type should implement this
 * interface. 
 * attributeEdited should be called when an attribute changes.
 * This should first make sure that {@link getAttributeName()}
 * will return the name of the most recently changed attribute.
 * Then, notify propertyChangeListeners, which will 
 * allow the {@link FieldPanel} to save changes. 
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
	 * fires PropertyChange for {@link #VALUE_CHANGED_PROPERTY}
	 * Listener (FieldPanel) will edit the attribute, adding the 
	 * edit to the undo/redo queue. 
	 * The newValue could be a string (the value of the named attribute)
	 * To to this, listeners will need to call getAttributeName().
	 * Or newValue could be a Map of attribute:value pairs.  
	 */
	public void attributeEdited(String attributeName, Object newValue);
	
	/**
	 * Gets the parameter object that this UI component is editing. 
	 */
	public IAttributes getParameter();
	
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
