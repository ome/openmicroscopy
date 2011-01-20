 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.BooleanTemplate 
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.BooleanEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * A UI for editing the "template" (default value) of a boolean 
 * parameter. 
 * Uses a {@link BooleanEditor} to do the editing of the 
 * {@link TextParam#DEFAULT_VALUE} attribute. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BooleanTemplate 
	extends AbstractParamEditor
	implements PropertyChangeListener
{

	private void buildUI()
	{
		BooleanEditor checkBox = new BooleanEditor(getParameter(), 
				TextParam.DEFAULT_VALUE);
		checkBox.addPropertyChangeListener
			(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		
		add(new CustomLabel("Default: "));
		add(checkBox);
		add(Box.createHorizontalGlue());
		//JPanel spacer = new JPanel();
		//spacer.setBackground(null);
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The parameter in which to edit the default boolean value
	 */
	public BooleanTemplate(IAttributes param) 
	{
		super(param);
		
		buildUI();
	}
	
	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() {
		return "Checkbox default";
	}


	/**
	 * This method is implemented as specified by the 
	 * {@link PropertyChangeListener} interface.
	 * 
	 * This class listens for changes in the
	 * {@link AbstractParamEditor#VALUE_CHANGED_PROPERTY} in the 
	 * {@link BooleanEditor} that makes up the UI. 
	 * Any change events are passed on by calling 
	 * {@link #attributeEdited(String, Object)}.
	 * 
	 * @see AbstractParamEditor#attributeEdited(String, Object)
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		if (AbstractParamEditor.VALUE_CHANGED_PROPERTY.
				equals(evt.getPropertyName())) {
		
			if (evt.getSource() instanceof ITreeEditComp) {
				ITreeEditComp source = (ITreeEditComp)evt.getSource();
				String attributeName = source.getAttributeName();
				Object newValue = evt.getNewValue();
				attributeEdited(attributeName, newValue);
			}
		}
	}

}
