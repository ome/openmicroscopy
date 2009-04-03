 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextBoxEditor 
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomFont;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;


/** 
 * This is an editing component that edits a text value in a 
 * IFieldValue object. 
 * If the text in this component is changed, then the update occurs
 * when the focus is lost, by firing property changed, with property 
 * named {@link ITreeEditComp#VALUE_CHANGED_PROPERTY}
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
	private JTextArea 			textBox;
	
	/**
	 * The name of the attribute that this text box is editing
	 */
	private String	 			attributeName;
	
	/**
	 * Initialises the UI components. 
	 * 
	 */
	private void initialise() 
	{

		String text = getParameter().getAttribute(attributeName);
		
		textBox = new JTextArea(text);
		textBox.setLineWrap(true);
		textBox.setColumns(40);
		textBox.setFont(new CustomFont());
		textBox.setWrapStyleWord(true);
		
		Border bevelBorder = BorderFactory.createLoweredBevelBorder();
		Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		Border compoundBorder = BorderFactory.createCompoundBorder(bevelBorder, emptyBorder);
		textBox.setBorder(compoundBorder);
	
		// Determine how many rows you need to display the text.
		int lines = textBox.getLineCount();
		int extraLines = 0;
		int lineLength = 0;
		int lineStartOffset = 0;
		// For each line of text...
		for (int l=0; l<lines; l++) {
			try {
				lineLength = textBox.getLineEndOffset(l) - lineStartOffset;
				// ...see how many extra lines you need
				// (approx 60 chars per line, with 40 columns!)
				extraLines = extraLines + (lineLength / 60);
				
				lineStartOffset = textBox.getLineEndOffset(l) + 1;
			} catch (BadLocationException e) {
				// ignore. This exception shouldn't happen anyway!
			}
		}
		// Add the extra lines 
		lines = lines + extraLines;
		// Show at least 2 lines.
		textBox.setRows(Math.max(lines, 2));
		
		AttributeEditListeners.addListeners(textBox, this, attributeName);
		textBox.getDocument().addDocumentListener(new NewLineListener());
	}
	
	/**
	 * Builds the UI. Adds textBox etc.
	 */
	private void buildUI() {
		
		JScrollPane scrollPane = new JScrollPane(textBox);
		//scrollPane.getViewport().setPreferredSize(preferredSize)
		Dimension textBoxSize = textBox.getPreferredSize();
		int w = (int)textBoxSize.getWidth();
		int h = (int)textBoxSize.getHeight();
		
		scrollPane.setPreferredSize(new Dimension(w + 10, h + 10));
		this.add(scrollPane);
	}
	
	/**
	 * Creates an instance, initialises the textBox, and adds it to the UI.
	 * The text box will edit the named attribute.  
	 * 
	 * @param param		The parameter object that this UI will edit.
	 * @param attributeName		The name of the attribute to edit 
	 */
	public TextBoxEditor(IAttributes param, String attributeName) 
	{	
		super(param);
		
		this.attributeName = attributeName;
		
		initialise();
		
		buildUI();
	}
	
	/**
	 * Creates an instance, initialises the textBox, and adds it to the UI. 
	 * This will edit the value of the {@link TextParam#PARAM_VALUE} 
	 * attribute.
	 * 
	 * @param param		The parameter object that this UI will edit. 
	 */
	public TextBoxEditor(IAttributes param) 
	{	
		super(param);
		
		this.attributeName = TextParam.PARAM_VALUE;
		
		initialise();
		
		buildUI();
	}
	
	/**
	 * Returns a reference to the text area used in this component. 
	 * Allows other classes to set text, change appearance etc. 
	 * 
	 * @return	see above. 
	 */
	public JTextArea getTextBox() {	return textBox; }
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Text"; }
	
	/**
	 * Need to listen to changes in the number of rows, so as to re-size 
	 * the Tree Node that this field appears in. 
	 * 
	 * @author will
	 *
	 */
	public class NewLineListener implements DocumentListener 
	{
		// keep track of the number of lines. 
		int lineCount;
		
		public NewLineListener() 
		{
			lineCount = textBox.getLineCount();
		}
		
		public void changedUpdate(DocumentEvent e) {}

		public void insertUpdate(DocumentEvent e) { checkLines(); }

		public void removeUpdate(DocumentEvent e) {	checkLines(); }
		
		private void checkLines() {
			int newLineCount = textBox.getLineCount();
			if (newLineCount != lineCount) {
				// TODO need to refresh size of the node in JTree without 
				// losing unsaved edits! 
				//firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
				lineCount = newLineCount;
			}
		}
		
	}
}
