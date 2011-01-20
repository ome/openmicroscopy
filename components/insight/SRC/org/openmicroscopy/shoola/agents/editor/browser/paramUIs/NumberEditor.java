 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.NumberEditor 
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML.Tag;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.TextAreaFilter;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;

/** 
 * This is the UI component for displaying the Number Field (with units label)
 * and editing the number. 
 * It delegates the editing to the TextFieldEditor. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NumberEditor 
	extends AbstractParamEditor 
	implements ITreeEditComp,
	PropertyChangeListener 
{
	
	/**
	 * Builds the UI.
	 * Uses a {@link TextFieldEditor} to display the link and adds this class
	 * as a {@link PropertyChangeListener}.
	 */
	private void buildUI()
	{
		TextFieldEditor numberField = new TextFieldEditor(getParameter(), 
				TextParam.PARAM_VALUE);
		numberField.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
		
		Document d = numberField.getTextField().getDocument();
		AbstractDocument doc;
		if (d instanceof AbstractDocument) {
            doc = (AbstractDocument)d;
            doc.setDocumentFilter(new NumberFilter(doc));
        }
		
		add(numberField);
		
		add(Box.createHorizontalStrut(10));
		
		String units = getParameter().getAttribute(NumberParam.PARAM_UNITS);
		add(new CustomLabel(units));
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param param		The Number Parameter that this UI displays and edits. 
	 */
	public NumberEditor(IParam param) 
	{
		super(param);
		
		buildUI();
	}

	/**
	 * Simply pass on the property change event by calling 
	 * {@link #attributeEdited(String, Object)}
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		attributeEdited(TextParam.PARAM_VALUE, evt.getNewValue());
	}
	
	/**
	 * A display name for undo/redo
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Number"; }

}
