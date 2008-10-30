 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.textFieldEditor 
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

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomTextField;

/** 
 * This is a text editing component that edits an attribute in an
 * instance of IParam. The name of the attribute can be specified in 
 * the constructor, or will otherwise be {@link SingleParam#PARAM_VALUE}
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextFieldEditor 
	extends AbstractParamEditor 
{
	
	/**
	 * The name of the attribute that you want to edit with this text field
	 */
	private String 				attributeName;
	
	/**
	 * Text field for editing the attribute's value
	 */
	private JTextField 			textField;
	
	/**
	 * Initialises the text field, adds listeners to call 
	 * {@link #attributeEdited(String, Object)}
	 * when focus lost (if text has been edited), and sets text. 
	 */
	private void initialise() 
	{
		String value = getParameter().getAttribute(attributeName);
		
		textField = new CustomTextField();	
		AttributeEditListeners.addListeners(textField, this, attributeName);
		
		textField.setText(value);
		add(textField);
	}

	/**
	 * Creates an instance.
	 * 	
	 * @param param		The parameter that you are editing
	 */
	public TextFieldEditor(IParam param) 
	{
		super(param);
		attributeName = SingleParam.PARAM_VALUE;

		initialise();
	}
	
	/**
	 * Creates an instance.
	 * 	
	 * @param param		The parameter that you are editing
	 * @param attributeName 	Specify the attribute you want to edit
	 */
	public TextFieldEditor(IAttributes param, String attributeName) 
	{	
		super(param);
		this.attributeName = attributeName;
		initialise();
	}
	
	public JTextComponent getTextField() {
		return textField;
	}
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Text"; }

}
