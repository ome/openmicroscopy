 /*
 * org.openmicroscopy.shoola.agents.editor.model.FieldNode 
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

import javax.swing.tree.DefaultMutableTreeNode;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is the {@link DefaultMutableTreeNode} subclass used to model the 
 * Editor file as a treeModel. 
 * This node can be used to save attributes that are not part of the
 * data model, e.g. display attributes.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldNode 
	extends DefaultMutableTreeNode 
{

	private boolean 		descriptionVisible;
	
	public FieldNode(IField newField) {
		super(newField);
	}

	/**
	 * Sets the visibility of the the description. 
	 * 
	 * @param visible
	 */
	public void setDescriptionVisible(boolean visible)
	{
		descriptionVisible = visible;
	}
	
	/**
	 * Toggles the boolean {@link #descriptionVisible} and returns
	 * the new value.
	 * 	
	 * @return		The new (toggled) value of {@link #descriptionVisible}
	 */
	public boolean toggleDescriptionVisibility() 
	{
		descriptionVisible = !descriptionVisible;
		
		return descriptionVisible;
	}
	
	/**
	 * Gets the boolean {@link #descriptionVisible}
	 * 	
	 * @return		The value of {@link #descriptionVisible}
	 */
	public boolean getDescriptionVisisibility()
	{
		return descriptionVisible;
	}
}
