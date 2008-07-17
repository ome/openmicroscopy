 /*
 * ui.components.AttributeEditorListeners 
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
package ui.components;

//Java imports
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.JTextComponent;

import fields.IField;

//Third-party libraries

//Application-internal dependencies
import tree.IAttributeSaver;

/** 
 * This is a pair of listeners that should be added to any text field 
 * that updates a field attribute.
 * This class listens for a keyTyped event, and sets a flag
 * textChanged = true.
 * Then when focus is lost, the focusListener updates the dataField
 * attribute (if textChanged), by calling 
 * setDataFieldAttribute. TextChanged is reset to false.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AttributeEditorListeners 
	implements 
	FocusListener,
	KeyListener {
	
	boolean textChanged;
	
	IField dataField;
	
	String attributeName;
	
	public AttributeEditorListeners(IField dataField, 
			String attributeName) {
		
		this.dataField = dataField;
		this.attributeName = attributeName;
	}
	
	
	public void keyTyped(KeyEvent event) {
		textChanged = true;		// some character was typed, so set this flag
	}
	public void keyPressed(KeyEvent event) {}
	public void keyReleased(KeyEvent event) {}
	
	
	public void focusLost(FocusEvent event) {
		if (textChanged) {
			JTextComponent source = (JTextComponent)event.getSource();
			
			setDataFieldAttribute(source.getText(), true);
		}
	}
	public void focusGained(FocusEvent event) {}

	
	// called to update dataField with attribute
	protected void setDataFieldAttribute(String value, boolean notifyUndoRedo) {
		dataField.setAttribute(attributeName, value);
		textChanged = false;
	}
	
	/**
	 * Convenience method for adding listeners to a text component, such that
	 * the text component will then update the IField, using its
	 * setAttribute(attributeName, value) method.
	 * 
	 * @param textComp		The text component to add the listeners to
	 * @param field			The IField that will be updated with new text value	
	 * @param attributeName	The name of the attribute to set to the new value
	 */
	public static void addListeners(JTextComponent textComp, IField field, 
			String attributeName) {
		AttributeEditorListeners listeners = new AttributeEditorListeners(field, 
				attributeName);
		textComp.addFocusListener(listeners);
		textComp.addKeyListener(listeners);
	}

}
