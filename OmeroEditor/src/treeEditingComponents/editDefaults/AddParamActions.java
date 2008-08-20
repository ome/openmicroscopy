 /*
 * treeEditingComponents.editDefaults.AddParamActions 
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
package treeEditingComponents.editDefaults;


//Java imports

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

//Third-party libraries

//Application-internal dependencies

import treeModel.TreeEditorControl;
import treeModel.fields.FieldParamsFactory;
import treeModel.fields.IField;
import treeModel.fields.IParam;
import treeModel.fields.SingleParam;
import ui.components.PopupMenuButton;
import util.ImageFactory;

/** 
 * This class consists of a button with a pop-up menu that allows users to 
 * add a parameter to a field. 
 * Multiple parameters allow fields to specify 2 variables. 
 * eg Incubate at 4'C for 10 minutes. 
 * The only parameter types allowed by this UI are Text-Line, Number and
 * CheckBox(boolean). These are "atomic" parameters, which may be combined
 * in a field to describe a protocol step etc. 
 * But it doesn't make sense to allow Eg. multiple date-time parameters per
 * field. Use multiple Date-Time fields instead. 
 * Get the button using getButton();
 * Button should be displayed in the template-editing panel (FieldEditorPanel).
 * Following the addition of a parameter to the field, the UI display of 
 * the field will need to be refreshed. This is indicated by the 
 * button firing a propertyChangeEvent with property named
 * PARAM_ADDED_PROPERTY. Listeners need to listen for changes in this
 * property of the button.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AddParamActions {
	
	/**
	 * The field that is being edited. Parameter objects will be added to 
	 * this field. 
	 */
	private IField field;
	
	private JTree tree;
	
	private TreeNode node;
	
	private TreeEditorControl controller;
	
	
	/**
	 * The button that launches the pop-up menu. 
	 */
	private JButton addParamButton;
	
	/**
	 * A bound property (of the button). 
	 * Change indicates that a parameter has been added to the field. 
	 */
	public static final String PARAM_ADDED_PROPERTY = "paramAddedProperty";
	
	/**
	 * ImageFactory for icons. 
	 */
	ImageFactory imF = ImageFactory.getInstance();
	
	/**
	 * Creates an instance. 
	 * Makes an array of Add-Parameter actions, uses them to initiate a 
	 * pop-up menu button.
	 * 
	 * @param field		The field that will have new parameters added.
	 */
	public AddParamActions(IField field, JTree tree, TreeNode node, 
			TreeEditorControl controller) {
		this.field = field;
		this.tree = tree;
		this.node = node;
		this.controller = controller;
		
		Action[] actions = new Action[] {new AddTextParamAction(),
				new AddNumberParamAction(),
				new AddBooleanParamAction() };
		
		Icon addIcon = imF.getIcon(ImageFactory.ADD_ICON);
		
		addParamButton = new PopupMenuButton("Add parameter", addIcon, actions);
	}
	
	/**
	 * Returns the button that launches the pop-up menu. 
	 * 
	 * @return		the button that launches the pop-up menu. 
	 */
	public JButton getButton() {
		return addParamButton;
	}
	
	/**
	 * This is called after each parameter has been added.
	 * fires a propertyChange PARAM_ADDED_PROPERTY on
	 * the button. 
	 */
	private void notifyParamAdded() {
		addParamButton.firePropertyChange(PARAM_ADDED_PROPERTY, 
				field.getParamCount() -1, field.getParamCount());
	}
	
	/**
	 * Action for adding a Text-Line Parameter
	 * 
	 * @author will
	 *
	 */
	public class AddTextParamAction 
		extends AbstractAction {

		public AddTextParamAction() {
			putValue(Action.NAME, "Add Text Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a text-line parameter to this field");
			putValue(Action.SMALL_ICON, imF.getIcon(ImageFactory.ADD_TEXT_LINE_ICON)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			controller.addParamToField(field, SingleParam.TEXT_LINE_PARAM, 
					tree, node);
		}
		
	}
	
	/**
	 * Action for adding a Number Parameter
	 * 
	 * @author will
	 *
	 */
	public class AddNumberParamAction 
		extends AbstractAction {

		public AddNumberParamAction() {
			putValue(Action.NAME, "Add Number Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a number parameter to this field");
			putValue(Action.SMALL_ICON, imF.getIcon(ImageFactory.ADD_NUMBER)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			IParam numParam = FieldParamsFactory.getFieldParam(
					SingleParam.NUMBER_PARAM);
			field.addParam(numParam);
			notifyParamAdded();
		}
	
	}
	
	/**
	 * Action for adding a CheckBox Parameter
	 * 
	 * @author will
	 *
	 */
	public class AddBooleanParamAction 
		extends AbstractAction {

		public AddBooleanParamAction() {
			putValue(Action.NAME, "Add Checkbox Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a checkbox parameter to this field");
			putValue(Action.SMALL_ICON, imF.getIcon(ImageFactory.ADD_CHECK_BOX)); 
		}
		
		public void actionPerformed(ActionEvent e) {
			IParam checkBoxParam = FieldParamsFactory.getFieldParam(
					SingleParam.BOOLEAN_PARAM);
			field.addParam(checkBoxParam);
			notifyParamAdded();
		}
		
	}

}
