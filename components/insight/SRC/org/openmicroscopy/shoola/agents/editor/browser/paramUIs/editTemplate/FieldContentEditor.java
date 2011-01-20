 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate
 * .FieldEditorPanel 
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.FieldTextArea;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;

/** 
 * The Panel for editing the "Template" of each field.
 * This includes the Name, Description etc.
 * Also, this panel contains the components for template editing of 0, 1 or more 
 * parameters of the field. eg Default values, units etc. 
 * This class extends {@link FieldParamEditor}, which allows editing of the 
 * Field Name and Parameters. This class adds the ability to edit the textual
 * content of the field, by adding a modified {@link FieldTextArea};
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldContentEditor 
	extends FieldParamEditor
{

	/**
	 * A panel to hold the description text area. 
	 */
	private JPanel 		descriptionPanel;
	
	/**
	 * Creates an instance. 
	 * Delegates to super-class, then adds a UI component for editing the
	 * description content of this field. 
	 * 
	 * @param field		The Field to edit
	 * @param tree		The JTree in which the field is displayed
	 * @param treeNode	The node of the Tree which contains the field
	 * @param controller	The BrowserControl for handling edits 
	 */
	public FieldContentEditor(IField field, JTree tree,
			DefaultMutableTreeNode treeNode, BrowserControl controller) {
		super(field, tree, treeNode, controller);
		
		// This UI for description editing is created in constructor where 
		// we have access to all required fields. 
		// Then added to panel inserted by addParameters() (see below).
		JPanel desc = new DescriptionTextArea(field, tree, treeNode, controller);
		descriptionPanel.add(desc, BorderLayout.CENTER);
	}
	
	/**
	 * Overridden to add additional UI components for editing the parameters
	 * of this field. This method is called by the super-class constructor. 
	 * Inserts a panel that is subsequently filled with the text panel in
	 * the constructor. 
	 * 
	 * @see FieldParamEditor#addParameters();
	 */
	protected void addParameters() 
	{
		descriptionPanel = new JPanel();
		descriptionPanel.setBackground(null);
		descriptionPanel.setLayout(new BorderLayout());
		attributeFieldsPanel.add(descriptionPanel);
		
		super.addParameters();
	}
	
	/**
	 * Subclass of {@link FieldTextArea} that modifies UI for editing of 
	 * field descriptions, but not Field Name. 
	 * Name is not displayed, and is not saved. 
	 * 
	 * @author will
	 *
	 */
	private class DescriptionTextArea
		extends FieldTextArea
	{

		public DescriptionTextArea(IField field, JTree tree,
				DefaultMutableTreeNode treeNode, BrowserControl controller) {
			super(field, tree, treeNode, controller);
			
			setBorder(selectedBorder);
			
			nameEditor.setVisible(false);
		}
		
		/**
		 * Overridden to disable the "Split-Step" triggered by hitting 'Enter' 
		 */
		public void keyReleased(KeyEvent e) {}
		
		/**
		 * Overridden to disable the display of parameter editing dialog.
		 * 
		 * @see FieldTextArea#showParamDialog(int index, Point point) 
		 */
		protected void showParamDialog(int index, Point point) {}
		
		/**
		 * Overridden to avoid adding a parameter (possible for the "Add Param"
		 * button to become enabled when field gains focus!)
		 * 
		 * @see	FieldTextArea#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {}
		
	}
	
}
