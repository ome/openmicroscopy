 /*
 * treeEditingComponents.EnumEditor 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import tree.DataFieldConstants;
import treeModel.fields.IParam;
import treeModel.fields.SingleParam;
import uiComponents.CustomComboBox;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
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
public class EnumEditor 
	extends JPanel
	implements ITreeEditComp,
	ActionListener {
	
	private IParam param;
	
	private String lastUpdatedAttribute;
	
	private String[] ddOptions;
	
	private JComboBox comboBox;
	
	/**
	 * The drop-down index for a "blank" (no option chosen)
	 */
	public static final int NULL_INDEX = 0;
	
	/**
	 * The String to display in the No-Option-Chosen position. 
	 */
	public static final String NO_OPTION_CHOSEN = " ";
	
	public EnumEditor(IParam param) {
		
		this.param = param;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		comboBox = new CustomComboBox(250);
		comboBox.addActionListener(this);
		
		String dropDownOptions = param.getAttribute(SingleParam.ENUM_OPTIONS);
		/* set the options and value */
		setDropDownOptions(dropDownOptions);
		
		add(comboBox);
	}
	
	
	/**
	 * Sets the drop-down options as defined by the comma-delimited options.
	 * Also a "null" (blank) option is added at index 0, so that the field
	 * can display no value. Otherwise, it can never be blank.  
	 * 
	 * @param options	A list of options, separated by commas. 
	 */
	public void setDropDownOptions(String options) {
		if (options != null) {
			String dropDownOptions = options;
			String[] optionsSplit = dropDownOptions.split(",");
			
			/*
			 * The drop-down options need to include a blank at the start
			 * and to be trimmed. 
			 */
			ddOptions = new String[optionsSplit.length + 1];
			ddOptions[NULL_INDEX] = NO_OPTION_CHOSEN;
			for(int i=1; i<ddOptions.length; i++) {
				ddOptions[i] = optionsSplit[i-1].trim();
			}
			
			comboBox.removeActionListener(this);
		
			comboBox.removeAllItems();
			for(int i=0; i<ddOptions.length; i++) {
				comboBox.addItem(ddOptions[i]);
			}
			
			// Set it to the current value, (if it exists in the new ddOptions)
			String value = param.getAttribute(SingleParam.PARAM_VALUE);
			boolean newValNotFound = true;
			if (value != null) {
				// start at index 1, since 0 is blank / null
				for (int i=1; i<ddOptions.length; i++) {
					if (value.equals(ddOptions[i])) {
						comboBox.setSelectedIndex(i);
						newValNotFound = false;
						continue;
					}
				}
				
			} else {	// value == null
				comboBox.setSelectedIndex(NULL_INDEX);
				newValNotFound = false;		// always have a null!
			}
			
			comboBox.addActionListener(this);
			
			//need to update value (if the old value isn't in the new options)
			if (newValNotFound)
				updateParam();
		} else {
			// options == null, remove all
			comboBox.removeActionListener(this);
			comboBox.removeAllItems();
			comboBox.addActionListener(this);
		}
	}
	
	/**
	 * called after the drop-down options have changed, to update the 
	 * dataField, in case the new options didn't contain the old value. 
	 * NB. This change will not be included in Undo/Redo, and therefore should
	 * really be avoided by checking that the value is in the new options 
	 * when they are set. If not, include the VALUE in the same undo/redo
	 * as the options. 
	 */
	private void updateParam() {

		String value = comboBox.getSelectedItem().toString();
		if(comboBox.getSelectedIndex() == NULL_INDEX) {
			value = null;
		}
		param.setAttribute(SingleParam.PARAM_VALUE, value);
		
	}
	
	
	/**
	 * Sets the last updated attribute to attributeName, then fires 
	 * propertyChanged. 
	 */
	public void attributeEdited(String attributeName, String newValue) {
		/*
		 * Before calling propertyChange, need to make sure that 
		 * getAttributeName() will return the name of the newly edited property
		 */
		String oldValue = param.getAttribute(attributeName);
		
		lastUpdatedAttribute = attributeName;
		
		this.firePropertyChange(ITreeEditComp.VALUE_CHANGED_PROPERTY,
				oldValue, newValue);
	}
	
	public String getAttributeName() {
		return lastUpdatedAttribute;
	}

	public IParam getParameter() {
		return param;
	}
	
	public String getEditDisplayName() {
		return "Edit Dropdown Option";
	}


	public void actionPerformed(ActionEvent e) {
		
		String newValue = comboBox.getSelectedItem().toString();
		if(comboBox.getSelectedIndex() == NULL_INDEX) {
			newValue = null;
		}
		attributeEdited(SingleParam.PARAM_VALUE, newValue);
	}

}
