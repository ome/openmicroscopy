 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs
 * .AttributeEditListeners 
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.text.JTextComponent;

//Third-party libraries

//Application-internal dependencies

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
public class AttributeEditListeners 
	implements 
	FocusListener,
	KeyListener 
{
	/**
	 * The interface for the component that this listener class is added to.
	 * The method {@link ITreeEditComp#attributeEdited(String, Object)} is
	 * called when this listener wants to save data. 
	 */
	private ITreeEditComp editComp;
	
	/**
	 * A flag to determine that the text has changed. 
	 * Set to true when text is typed.
	 * Set to false when text is saved.
	 */
	private boolean textChanged;
	
	/**
	 * The name of the attribute being edited by the <code>editComp</code>
	 */
	private String attributeName;
	
	/**
	 * Calls {@link ITextEditComp#attributeEdited(String)}
	 * and sets <code>textChanged</code> to false.
	 * 
	 * @param value		The new value of the attribute.
	 */
	private void attributeEdited(String value) {
		editComp.attributeEdited(attributeName, value);
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
	public static void addListeners(
			JTextComponent textComp, ITreeEditComp editComp, 
			String attributeName) 
	{
		AttributeEditListeners listeners = new AttributeEditListeners(editComp, 
				attributeName);
		textComp.addFocusListener(listeners);
		textComp.addKeyListener(listeners);
	}

	/**
	 * Creates an instance;
	 * 
	 * @param editComp			The text component to add listeners to.
	 * @param attributeName		The name of the attribute being edited. 
	 */
	public AttributeEditListeners(ITreeEditComp editComp, 
			String attributeName) 
	{	
		this.editComp = editComp;
		this.attributeName = attributeName;
	}
	
	/**
	 * some character was typed, so set the <code>textChanged</code> flag.
	 * 
	 * @see KeyListener#keyTyped(KeyEvent)
	 */
	public void keyTyped(KeyEvent event) 
	{
		// some character was typed, so set this flag
		textChanged = true;	
	}
	
	/**
	 * Does nothing
	 * 
	 * @see KeyListener#keyPressed(KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {}
	
	/**
	 * Does nothing
	 * 
	 * @see KeyListener#keyReleased(KeyEvent)
	 */
	public void keyReleased(KeyEvent event) {}
	
	/**
	 * The focus is lost. 
	 * If the text has changed, call {@link #attributeEdited(String)}
	 * 
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent event) 
	{
		if (textChanged) {
			JTextComponent source = (JTextComponent)event.getSource();
			
			attributeEdited(source.getText());
		}
	}
	
	/**
	 * Does nothing
	 * 
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent event) {}

}