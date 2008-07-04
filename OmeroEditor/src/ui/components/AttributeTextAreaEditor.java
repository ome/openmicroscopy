 /*
 * ui.components.AttributeTextAreaEditor 
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
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import tree.DataFieldObserver;
import tree.IAttributeSaver;
import tree.IDataFieldObservable;

/** 
 * This is a JTextArea, that is associated with a particular attribute
 * from a dataField.
 * When the text of this TextArea is edited (and focus lost), the dataField
 * is updated with the new text.
 * This Class also listens for changes to dataField, and updates it's
 * text according to the new value of the named attribute. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AttributeTextAreaEditor 
	extends JTextArea 
	implements DataFieldObserver {
		
		IAttributeSaver dataField;
		String attributeName;
		
		/**
		 * Constructor
		 * 
		 * @param dataField		The dataField that contains the attribute to 
		 * 						be edited.
		 * @param attributeName		The name of the attribute to be edited.
		 */
		public AttributeTextAreaEditor(IAttributeSaver dataField, 
				String attributeName) {
			
			this.dataField = dataField;
			this.attributeName = attributeName;
			
			if (dataField instanceof IDataFieldObservable) {
				((IDataFieldObservable)dataField).addDataFieldObserver(this);
			}
			
			this.setRows(2);
			this.setLineWrap(true);
			this.setWrapStyleWord(true);
			//JScrollPane textScroller = new JScrollPane(textInput);
			Border bevelBorder = BorderFactory.createLoweredBevelBorder();
			Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
			Border compoundBorder = BorderFactory.createCompoundBorder(bevelBorder, emptyBorder);
			this.setBorder(compoundBorder);
			
			AttributeEditorListeners listener = new AttributeEditorListeners(
					dataField, attributeName);
			
			this.addFocusListener(listener);
			this.addKeyListener(listener);
			
			dataFieldUpdated();
		}
		
		public void dataFieldUpdated() {
			String textValue = dataField.getAttribute(attributeName);
			this.setText(textValue);
		}

	}
