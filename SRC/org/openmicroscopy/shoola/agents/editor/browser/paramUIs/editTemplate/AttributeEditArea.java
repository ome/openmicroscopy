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
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate;

//Java imports

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AbstractParamEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.AttributeEditListeners;
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
public class AttributeEditArea 
	extends AbstractParamEditor 
{
	
	/**
	 * This is the name of the attribute being edited by this UI
	 */
	private String 			attributeName;
	
	/**
	 * A string for the label beside the text field. e.g. "Name".
	 * This is also used for the {@link #getEditDisplayName()} to provide 
	 * an undo/redo display name. 
	 */
	private String 			labelText;
	
	/**
	 * The text box that edits the value of this parameter
	 */
	private JTextArea 			textBox;
	
	/**
	 * Initialises the UI components. 
	 * 
	 */
	private void initialise() 
	{

		String text = getParameter().getAttribute(attributeName);
		
		textBox = new JTextArea(text);
		textBox.setLineWrap(true);
		textBox.setFont(new CustomFont());
		textBox.setWrapStyleWord(true);
		
		Border bevelBorder = BorderFactory.createLoweredBevelBorder();
		Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		Border compoundBorder = BorderFactory.createCompoundBorder
			(bevelBorder, emptyBorder);
		textBox.setBorder(compoundBorder);
		
		AttributeEditListeners.addListeners(textBox, this, attributeName);
		textBox.getDocument().addDocumentListener(new NewLineListener());
	}
	
	/**
	 * Builds the UI. Adds textBox etc.
	 */
	private void buildUI() {
		
		setLayout(new BorderLayout());
		
		add(new CustomLabel (labelText + ": "), BorderLayout.NORTH);
		this.add(textBox, BorderLayout.CENTER);
	}
	
	/**
	 * Creates an instance, initialises the textBox, and adds it to the UI.
	 * The text box will edit the named attribute.  
	 * 
	 * @param param		The parameter object that this UI will edit.
	 * @param attributeName		The name of the attribute to edit 
	 */
	public AttributeEditArea(IAttributes param, 
			String attributeName, String label) 
	{	
		super(param);
		
		this.attributeName = attributeName;
		this.labelText = label;
		
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
	public AttributeEditArea(IAttributes param) 
	{	
		super(param);
		
		this.attributeName = TextParam.PARAM_VALUE;
		
		initialise();
		
		buildUI();
	}
	
	public void setCols(int cols) 
	{
		textBox.setColumns(cols);
	}
	
	/**
	 * This allows other classes to manipulate the text box, to set cols or
	 * set Text etc. 
	 * 
	 * @return		The Text Area used by this UI for editing text
	 */
	public JTextArea getTextArea() { return textBox; }
	
	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit " + labelText; }
	
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
