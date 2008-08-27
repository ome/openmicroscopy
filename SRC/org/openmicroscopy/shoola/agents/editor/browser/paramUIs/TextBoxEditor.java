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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java imports

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.SingleParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;


/** 
 * This is an editing component that edits a text value in a 
 * IFieldValue object. 
 * If the text in this component is changed, then the update occurs
 * when the focus is lost, by firing property changed.
 * This behavior is managed by the AttributeEditListeners class.
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
	extends AbstractParamEditor {
	
	/**
	 * The text box that edits the value of this parameter
	 */
	private JTextArea textBox;
	
	public TextBoxEditor(IParam param) {
		
		super(param);
		
		String attributeName = SingleParam.PARAM_VALUE;
		
		String text = param.getAttribute(attributeName);
		
		textBox = new JTextArea(text);
		textBox.setRows(2);
		textBox.setLineWrap(true);
		textBox.setFont(CustomLabel.CUSTOM_FONT);
		textBox.setWrapStyleWord(true);
		//JScrollPane textScroller = new JScrollPane(textInput);
		Border bevelBorder = BorderFactory.createLoweredBevelBorder();
		Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		Border compoundBorder = BorderFactory.createCompoundBorder(bevelBorder, emptyBorder);
		textBox.setBorder(compoundBorder);
		
		AttributeEditListeners.addListeners(textBox, this, attributeName);
		
		//textBox.setPreferredSize(new Dimension(300, 100));
		this.add(textBox);
		
		textBox.getDocument().addDocumentListener(new NewLineListener());
		
		// System.out.println(attributeName + " " + value);
	}
	
	public String getEditDisplayName() {
		return "Edit Text";
	}
	
	/**
	 * Need to listen to changes in the number of rows, so as to re-size 
	 * the Tree Node that this field appears in. 
	 * 
	 * @author will
	 *
	 */
	public class NewLineListener implements DocumentListener {

		int lineCount = textBox.getLineCount();
		
		public void changedUpdate(DocumentEvent e) {
			int newLineCount = textBox.getLineCount();
			if (newLineCount != lineCount) {
			System.out.println("TextBoxEditor oldLines" + lineCount + " " + newLineCount);
				lineCount = newLineCount;
			}
		}

		public void insertUpdate(DocumentEvent e) {
		}

		public void removeUpdate(DocumentEvent e) {	
		}
		
	}
	
	/**
	 * Returns a preferred size that is a fixed width, but the same 
	 * height as super.getPreferredSize();
	 */
	public Dimension getPreferredSize() {
		
		Dimension textBoxSize = textBox.getPreferredSize();
		
		int height = (int)textBoxSize.getHeight() + 20;
		int width = 400;
		
		return new Dimension (width, height);
	}
	

}
