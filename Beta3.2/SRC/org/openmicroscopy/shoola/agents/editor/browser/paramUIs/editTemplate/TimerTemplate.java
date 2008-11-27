 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.TimerTemplate 
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
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TimerField;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.HrsMinsSecsField;

/** 
 * The UI component for editing the "Template" of the timer parameter
 * (edits the timer default value, in the {@link SingleParam#DEFAULT_VALUE}
 * attribute.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TimerTemplate 
	extends AbstractParamEditor
	implements PropertyChangeListener
{
	/**
	 * The attribute for storing the default time. 
	 */
	private String 			attributeName = SingleParam.DEFAULT_VALUE;
	
	/**
	 * Builds the UI.
	 */
	private void buildUI() 
	{
		HrsMinsSecsField hrsMinsSecs = new HrsMinsSecsField();
		hrsMinsSecs.addPropertyChangeListener(
				HrsMinsSecsField.TIME_IN_SECONDS, this);
		
		String defaultValue = getParameter().getAttribute(attributeName);
		int defaultSecs = TimerField.getSecondsFromTimeValue(defaultValue);
		hrsMinsSecs.setTimeInSecs(defaultSecs);
		
		add(new CustomLabel("Default time: "));
		add(hrsMinsSecs);
		add(Box.createHorizontalGlue());
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The paramter to edit.
	 */
	public TimerTemplate(IAttributes param) 
	{
		super(param);
		
		buildUI();
	}

	/**
	 * Listens for changes to the HrsMinsSecsField.TIME_IN_SECONDS property.
	 * Updates the timeInSeconds value, and calls attributeEdited() to 
	 * save the new value to the parameter.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{	
		if (HrsMinsSecsField.TIME_IN_SECONDS.equals(evt.getPropertyName())) {
			int timeInSeconds = Integer.parseInt(evt.getNewValue().toString());
			//timer.setCurrentSecs(timeInSeconds);
			attributeEdited(attributeName, timeInSeconds + "");
		}
	}

	/**
	 * Implemented as specified by the {@link ITreeEditComp} interface. 
	 * 
	 * @see {@link ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Default Time"; }

}
