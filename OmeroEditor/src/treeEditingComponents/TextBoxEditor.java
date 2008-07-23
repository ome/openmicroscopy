 /*
 * treeEditingComponents.TextBoxEditor 
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

import java.awt.Dimension;

import javax.swing.JPanel;

import ui.components.AttributeTextAreaEditor;
import fields.IParam;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** 
 * This is an editing component that edits a text value in a 
 * IFieldValue object. 
 * If the text in this component is changed, then the update occurs
 * when the focus is lost, using the IFieldValue.setAttribute(name, value) method.
 * This behavior is managed by the AttributeEditorListeners class.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TextBoxEditor 
	extends JPanel {
	
	public TextBoxEditor(IParam param) {
		
		super();
		setBackground(null);
		
		String[] attributes = param.getValueAttributes();
		String attributeName = "value";		// default
		if (attributes.length > 0) {
			attributeName = attributes[0];
		}
		
		AttributeTextAreaEditor textBox = new AttributeTextAreaEditor(param, 
				attributeName);
		
		textBox.setPreferredSize(new Dimension(300, 100));
		this.add(textBox);
		
		// System.out.println(attributeName + " " + value);
	}

}
