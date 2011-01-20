 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.EnumEditor 
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

import javax.swing.BoxLayout;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomComboBox;

/** 
 * This is a UI component for choosing the value of an Enumeration parameter.
 * Enumeration options are presented in a comboBox. 
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
	extends AbstractParamEditor
	implements ActionListener 
{
	
	/**
	 * A string array of options to choose from. 
	 */
	private String[] 			ddOptions;
	
	/**
	 * ComboBox for displaying options. 
	 */
	private JComboBox 			comboBox;
	
	/**
	 * The name of the attribute that is edited by this UI.
	 * The new value of the comboBox will be mapped to this attribute.
	 */
	private String 				attributeName;
	
	/**
	 * The comboBox index for a "blank" (no option chosen)
	 */
	public static final int NULL_INDEX = 0;
	
	/**
	 * The String to display in the No-Option-Chosen position. 
	 */
	public static final String NO_OPTION_CHOSEN = " ";
	
	/**
	 * Sets the drop-down options as defined by the comma-delimited options.
	 * Also a "null" (blank) option is added at index 0, so that the field
	 * can display no value. Otherwise, it can never be blank.  
	 * 
	 * @param options	A list of options, separated by commas. 
	 */
	private void setDropDownOptions(String options) {
		
		comboBox.removeAllItems();
		if (options != null) {
			String dropDownOptions = options;
			String[] optionsSplit = dropDownOptions.split(",");
			
			// The drop-down options need to include a blank at the start
			// and to be trimmed. 
			ddOptions = new String[optionsSplit.length + 1];
			ddOptions[NULL_INDEX] = NO_OPTION_CHOSEN;
			for(int i=1; i<ddOptions.length; i++) {
				ddOptions[i] = optionsSplit[i-1].trim();
			}
		
			for(int i=0; i<ddOptions.length; i++) {
				comboBox.addItem(ddOptions[i]);
			}
			
			// Set it to the current value, (if it exists in the new ddOptions)
			String value = getParameter().getAttribute(attributeName);

			if (value != null) {
				// start at index 1, since 0 is blank / null
				for (int i=1; i<ddOptions.length; i++) {
					if (value.equals(ddOptions[i])) {
						comboBox.setSelectedIndex(i);
						continue;
					}
				}
			} else {	// value == null
				comboBox.setSelectedIndex(NULL_INDEX);
			}
		}
	}

	/**
	 * Initialises the comboBox
	 */
	private void initialise() 
	{
		comboBox = new CustomComboBox(150);
		
		String dropDownOptions = getParameter()
						.getAttribute(EnumParam.ENUM_OPTIONS);
		/* set the options and value */
		setDropDownOptions(dropDownOptions);
		comboBox.addActionListener(this);
	}
	
	/**
	 * Builds the UI. 
	 */
	private void buildUI() 
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(null);
		
		add(comboBox);
	}

	/**
	 * Creates an instance. Builds the UI. 
	 * 
	 * @param param		The parameter this field is editing.
	 */
	public EnumEditor(IAttributes param) 
	{	
		super(param);
		
		attributeName = TextParam.PARAM_VALUE;
		initialise();
		
		buildUI();
	}
	
	/**
	 * Creates an instance. Builds the UI. 
	 * 
	 * @param param		The parameter this field is editing.
	 * @param attributeName		The attribute edited by the comboBox.
	 */
	public EnumEditor(IAttributes param, String attributeName) 
	{	
		super(param);
		
		this.attributeName = attributeName;
		initialise();
		
		buildUI();
	}
	
	
	/**
	 * ActionPerformed for the comboBox. 
	 * Calls {@link AbstractParamEditor#attributeEdited(String, Object)}
	 * with the new value of the comboBox.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{	
		Object ob = comboBox.getSelectedItem();
		if (ob == null)		return;
		String newValue = comboBox.getSelectedItem().toString();
		if(comboBox.getSelectedIndex() == NULL_INDEX) {
			newValue = null;
		}
		attributeEdited(attributeName, newValue);
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Dropdown Option"; }

}
