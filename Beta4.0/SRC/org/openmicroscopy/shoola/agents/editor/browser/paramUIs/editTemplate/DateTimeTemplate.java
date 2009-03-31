 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.DateTimeTemplate 
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
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * A UI for editing the template attributes of the DateTime Parameter. 
 * Can choose whether this is an "Relative" Date or an absolute date. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DateTimeTemplate 
	extends AbstractParamEditor
	implements PropertyChangeListener
{

	/**
	 * Builds the UI.
	 */
	private void buildUI() 
	{
		add(new CustomLabel("Date-Time: "));
		add(Box.createHorizontalGlue());
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The paramter to edit.
	 */
	public DateTimeTemplate(IAttributes param) 
	{
		super(param);
		
		buildUI();
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

	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Date-Time template"; }

}
