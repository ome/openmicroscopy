 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.EnumTemplate 
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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.EnumEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * This is the UI component for editing the "Template" of an Enumeration 
 * Parameter. 
 * It includes a text box for entering a comma-delimited list of options, and
 * a comboBox for choosing the default option. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EnumTemplate
	extends AbstractParamEditor
	implements PropertyChangeListener 
{

	/**
	 * The text box for entering a list of comma-delimited options.
	 */
	private AttributeEditArea 			optionsFieldEditor;
	
	/**
	 * A comboBox for selecting the default option.
	 */
	private AbstractParamEditor			defaultValueComboBox;
	
	/**
	 * A text field for editing the units of the enumeration
	 */
	private AttributeEditLine 			unitsEditor;
	
	/**
	 * Initialises the UI components. 
	 */
	private void initialise() {
		
		IAttributes param = getParameter();
		
		// A text box to display and edit the list of options
		optionsFieldEditor = new AttributeEditArea(param, 
					EnumParam.ENUM_OPTIONS, 
					"Drop-down options: separate with commas");
		optionsFieldEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		optionsFieldEditor.setToolTipText("Add keywords, separated by commas");
		
		
		// Combo-box displaying the options - for picking a default
		defaultValueComboBox = new EnumEditor(param, TextParam.DEFAULT_VALUE);
		defaultValueComboBox.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);

		// Text field for editing/displaying units
		unitsEditor = new AttributeEditNoLabel
			(param, NumberParam.PARAM_UNITS, "units");
		unitsEditor.addPropertyChangeListener
			(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI()
	{
		setLayout(new BorderLayout());
		add(optionsFieldEditor, BorderLayout.NORTH);
		add(new CustomLabel("Default Value: "), BorderLayout.WEST);
		add(defaultValueComboBox, BorderLayout.CENTER);
		add(unitsEditor, BorderLayout.EAST);
	}
	
	public EnumTemplate(IAttributes param) {
		super(param);
		
		initialise();
		
		buildUI();
	}

	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Drop-down options"; }

	/**
	 * May be called from {@link #optionsFieldEditor} when options have changed.
	 * 
	 * Implemented as specified by the {@link PropertyChangeListener} interface.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(evt.getPropertyName())){
			if (evt.getSource().equals(defaultValueComboBox)) {
				String newDefault = null;
				if (evt.getNewValue() != null)
					newDefault = evt.getNewValue().toString();
				// simply change the new value of default attribute
				attributeEdited(TextParam.DEFAULT_VALUE, newDefault);
			}
			
			// if the drop-down options have changed, need to check that
			// the value and default value are in the new options. 
			if (evt.getSource().equals(optionsFieldEditor)) {
				String newOptions = null;
				if(evt.getNewValue() != null) 
					newOptions = evt.getNewValue().toString();
				
				String defaultValue = getParameter().getAttribute
						(TextParam.DEFAULT_VALUE);
				String paramValue = getParameter().getAttribute
				(TextParam.PARAM_VALUE);
				
				// if the new options don't contain the default value,
				// reset the default value...
				if ((defaultValue != null) && 
						(! newOptions.contains(defaultValue))) {
					defaultValue = null;
				} 
				//... and do the same for the param value.
				if ((paramValue != null) && 
						(! newOptions.contains(paramValue))) {
					paramValue = null;
				} 
					
				// have to change the default value, as well as
				// the new options. 
				HashMap<String,String> newAttributes = 
					new HashMap<String,String>();
				newAttributes.put(TextParam.DEFAULT_VALUE, defaultValue);
				newAttributes.put(TextParam.PARAM_VALUE, paramValue);
				newAttributes.put(EnumParam.ENUM_OPTIONS, newOptions);
				attributeEdited("Drop-down options", newAttributes);
				
			}
			
			if (evt.getSource().equals(unitsEditor)) {
				String newUnits = null;
				if (evt.getNewValue() != null)
					newUnits = evt.getNewValue().toString();
				// simply change the new value of default attribute
				attributeEdited(NumberParam.PARAM_UNITS, newUnits);
			}
			
		}
	}
}
