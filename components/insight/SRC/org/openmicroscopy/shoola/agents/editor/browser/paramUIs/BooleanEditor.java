 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.BooleanEditor 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;

/** 
 * A UI Component for editing a Boolean Parameter. 
 * Extends JCheckBox. 
 * Fires propertyChanged when selected. 
 * Updates parameter and adds attribute edit to undo/redo queue. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BooleanEditor 
	extends AbstractParamEditor
	implements ActionListener 
{
	/**
	 * A JCheckBox for editing the boolean value 
	 */
	private JCheckBox 		checkBox;
	
	/**
	 * The value attribute. This is the attribute that holds the
	 * value of the boolean parameter we're editing.
	 */
	private String 			attributeName;
	
	/**
	 * Initialises the UI components. 
	 */
	private void initialise() 
	{
		checkBox = new JCheckBox();
		
		// ActionListener responds to checkBox selection
		checkBox.addActionListener(this);
		checkBox.setBackground(null);
		checkBox.setBorderPaintedFlat(true);
		
		boolean checked = getParameter().isAttributeTrue(attributeName);
		
		checkBox.setSelected(checked);
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param				The IAttributes collection we're editing
	 * @param attributeName		The name of the attribute we're editing
	 */
	public BooleanEditor(IAttributes param, String attributeName) {
		
		super(param);
		
		this.attributeName = attributeName;
		
		initialise();
		
		this.add(checkBox);
	}
	
	/**
	 * Creates an instance, which will edit the 
	 * {@link TextParam#PARAM_VALUE} attribute of the 
	 * {@link IAttributes} parameter. 
	 * 
	 * @param param		The IAttributes collection we're editing
	 */
	public BooleanEditor(IAttributes param) {
		
		super(param);
		
		attributeName = TextParam.PARAM_VALUE;
		initialise();
		
		this.add(checkBox);
	}
	
	/**
	 * Calls {@link #attributeEdited(String, Object)}
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		attributeEdited(attributeName, checkBox.isSelected() + "");
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() {
		return "Edit Checkbox";
	}
}
