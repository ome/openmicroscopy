 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor 
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

import javax.swing.BoxLayout;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;

/** 
 * This Panel is the abstract superclass of all UI components
 * that edit IParam instances (parameter objects).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class AbstractParamEditor 
	extends JPanel
	implements ITreeEditComp 
{	
	/**
	 * The Parameter object that this UI component edits. 
	 */
	private IAttributes 			param;
	
	/**
	 * The attribute to be edited by this component.
	 * Or, if multiple attributes are edited, this is updated to the 
	 * name of the most-recently edited attribute.
	 */
	private String 					lastEditedAttribute;

	/**
	 * Creates an instance.
	 * 
	 * @param param		The Parameter that will be edited by the UI.
	 */
	public AbstractParamEditor(IAttributes param) 
	{
		if (param == null) throw new NullPointerException("No param.");
		
		this.param = param;
		
		// default layout is BoxLayout. Can be changed by subclasses if needed
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBackground(null);
	}
	
	/**
	 * This method can be called from other classes, or called from
	 * subclasses of this class. 
	 * 
	 * fires PropertyChange for <code>VALUE_CHANGED_PROPERTY</code>
	 * Listener (<code>FieldPanel</code>) will edit the attribute, adding the 
	 * edit to the undo/redo queue. 
	 * The newValue could be a string (the value of the named attribute)
	 * In this case, listeners will need to call <code>getAttributeName()</code>.
	 * Or newValue could be a Map of attribute:value pairs.
	 * In this case, attributeName will not be queried by listeners. 
	 */
	public void attributeEdited(String attributeName, Object newValue) 
	{
		if (attributeName == null) return;
		// Before calling propertyChange, need to make sure that 
		// getAttributeName() will return the name of the newly edited property
		
		String oldValue = param.getAttribute(attributeName);
		
		lastEditedAttribute = attributeName;
		
		firePropertyChange(ITreeEditComp.VALUE_CHANGED_PROPERTY,
				oldValue, newValue);
	}
	
	/**
	 * Gets the parameter object that this UI component is editing. 
	 * 
	 * @see ITreeEditComp#getParameter()
	 */
	public IAttributes getParameter() { return param; }
	
	/**
	 * Gets the name of the last-edited attribute.
	 * 
	 * @see ITreeEditComp#getAttributeName()
	 */
	public String getAttributeName() { return lastEditedAttribute; }

}
