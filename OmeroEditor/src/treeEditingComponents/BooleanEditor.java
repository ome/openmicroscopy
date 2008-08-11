 /*
 * treeEditingComponents.BooleanEditor 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

//Third-party libraries

//Application-internal dependencies

import treeModel.fields.IParam;
import treeModel.fields.SingleParam;

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
	extends JCheckBox
	implements ITreeEditComp,
	ActionListener {

	/**
	 * The Parameter object being edited
	 */
	private IParam param;
	
	/**
	 * The name of the attribute used to hold the value of the boolean.
	 */
	String valueAttribute = SingleParam.PARAM_VALUE;
	
	/**
	 * Creates an instance.
	 * 
	 * @param param
	 */
	public BooleanEditor(IParam param) {
		
		super();
		this.param = param;
		
		/*
		 * ActionListener responds to checkBox selection
		 */
		addActionListener(this);
		setBackground(null);
		this.setBorderPaintedFlat(true);
		
		boolean checked = param.isAttributeTrue(valueAttribute);
		
		this.setSelected(checked);
	}
	
	/**
	 * Fires propertyChange 
	 */
	public void attributeEdited(String attributeName, String newValue) {
		this.firePropertyChange(ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				null, newValue);
	}

	/**
	 * This is the only attribute that you can modify from this class. 
	 */
	public String getAttributeName() {
		return valueAttribute;
	}

	public IParam getParameter() {
		return param;
	}

	public void actionPerformed(ActionEvent e) {
		attributeEdited(valueAttribute, this.isSelected() + "");
	}
}
