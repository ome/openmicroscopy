 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.
 * 		editTemplate.AddParamActions 
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.PopupMenuButton;


/** 
 * This class consists of a button with a pop-up menu that allows users to 
 * add a parameter to a field. 
 * Multiple parameters allow fields to specify 2 or more variables. 
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
 * <code>PARAM_ADDED_PROPERTY</code>. Listeners need to listen for changes in 
 * this property of the button.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AddParamActions 
{
	
	/**
	 * The field that is being edited. Parameter objects will be added to 
	 * this field. 
	 */
	private IField 			field;
	
	/**
	 * The tree in which the field we are editing appears. 
	 * Need a reference to this in order to refresh, highlight etc.
	 */
	private JTree 			tree;
	
	/**
	 * The Node of the Tree that contains the field we are editing. 
	 * Need a reference to this in order to refresh, highlight etc.
	 */
	private TreeNode 		node;
	
	/**
	 * The controller for managing the undo/redo etc. 
	 */
	private BrowserControl 	controller;
	
	/**
	 * The button that launches the pop-up menu. 
	 */
	private JButton 		addParamButton;
	
	/**
	 * A bound property (of the button). 
	 * Change indicates that a parameter has been added to the field. 
	 */
	public static final String PARAM_ADDED_PROPERTY = "paramAddedProperty";
	
	/**
	 * IconManager for icons. 
	 */
	private IconManager 	iM;
	
	/**
	 * Called by constructor to initialise componenets.
	 */
	private void initialise() 
	{
		iM = IconManager.getInstance();
		
		Action[] actions = new Action[] {new AddTextParamAction(),
				new AddTextBoxParamAction(),
				new AddNumberParamAction(),
				new AddBooleanParamAction(),
				new AddEnumParamAction(),
				new AddDateTimeParamAction(),
				new AddOntologyParamAction(),
				new AddDataRefAction()};
		
		Icon addIcon = iM.getIcon(IconManager.ADD_NUMBER);
		
		addParamButton = new PopupMenuButton("Add parameter", addIcon, actions);
	}
	
	/**
	 * Creates an instance. 
	 * Makes an array of Add-Parameter actions, uses them to initiate a 
	 * pop-up menu button.
	 * 
	 * @param field		The field that will have new parameters added.
	 * @param tree		The Jtree that displays the field
	 * @param node		The node that contains the field in the tree
	 * @param controller	The controller for undo/redo etc.
	 */
	public AddParamActions(IField field, JTree tree, TreeNode node, 
			BrowserControl controller) 
	{
		this.field = field;
		this.tree = tree;
		this.node = node;
		this.controller = controller;
		
		initialise();
	}
	
	/**
	 * Returns the button that launches the pop-up menu. 
	 * 
	 * @return		the button that launches the pop-up menu. 
	 */
	public JButton getButton() { return addParamButton; }
	
	/**
	 * Action for adding a Text-Line Parameter
	 * 
	 * @author will
	 *
	 */
	public class AddTextParamAction 
		extends AbstractAction 
	{

		/**
		 * Creates an instance. 
		 */
		public AddTextParamAction() 
		{
			putValue(Action.NAME, "Add Text Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a text-line parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(
					IconManager.TEXT_LINE_ICON)); 
		}
		
		/** 
	     * Adds a new Text Line Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, TextParam.TEXT_LINE_PARAM, 
					tree, node);
		}
		
	}
	
	/**
	 * Action for adding a TextBox Parameter
	 * 
	 * @author will
	 *
	 */
	public class AddTextBoxParamAction 
		extends AbstractAction 
	{

		/**
		 * Creates an instance. 
		 */
		public AddTextBoxParamAction() 
		{
			putValue(Action.NAME, "Add Text-Box Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a text-box parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.TEXT_BOX_ICON)); 
		}
		
		/** 
	     * Adds a new Text-Box Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, TextParam.TEXT_BOX_PARAM, 
					tree, node);
		}
	}
	
	/**
	 * Action for adding an Enumeration Parameter
	 * 
	 * @author will
	 */
	public class AddEnumParamAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddEnumParamAction() 
		{
			putValue(Action.NAME, "Add Drop-down Menu Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a number parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.DROP_DOWN)); 
		}
		
		/** 
	     * Adds a new Enumeration Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, EnumParam.ENUM_PARAM, 
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
		extends AbstractAction 
	{

		/**
		 * Creates an instance. 
		 */
		public AddNumberParamAction() 
		{
			putValue(Action.NAME, "Add Number Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a number parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.NUMBER)); 
		}
		
		/** 
	     * Adds a new Number Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, NumberParam.NUMBER_PARAM, 
					tree, node);
		}
	}
	
	/**
	 * Action for adding a CheckBox Parameter
	 * 
	 * @author will
	 */
	public class AddBooleanParamAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddBooleanParamAction() 
		{
			putValue(Action.NAME, "Add Checkbox Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a checkbox parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.ADD_CHECK_BOX)); 
		}
		
		/** 
	     * Adds a new Boolean Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, BooleanParam.BOOLEAN_PARAM, 
					tree, node);
		}
	}

	/**
	 * Action for adding a Date-Time Parameter
	 * 
	 * @author will
	 */
	public class AddDateTimeParamAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddDateTimeParamAction() 
		{
			putValue(Action.NAME, "Add Date-Time Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a date-time parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.CALENDAR_ICON)); 
		}
		
		/** 
	     * Adds a new Date-Time Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, DateTimeParam.DATE_TIME_PARAM, 
					tree, node);
		}
	}
	
	/**
	 * Action for adding a Image Parameter
	 * 
	 * @author will
	 */
	public class AddDataRefAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddDataRefAction() 
		{
			putValue(Action.NAME, "Add Data Link");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a link to data");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.LINK_LOCAL_ICON)); 
		}
		
		/** 
	     * Adds a new Data Reference
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addDataRefToField(field, tree, node);
		}
	}
	
	/**
	 * Action for adding an Ontology Term Parameter
	 * 
	 * @author will
	 */
	public class AddOntologyParamAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddOntologyParamAction() 
		{
			putValue(Action.NAME, "Add Ontology Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add an Ontology Term parameter to this field");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.ONTOLOGY_ICON)); 
		}
		
		/** 
	     * Adds a new Ontology Term Parameter
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			controller.addParamToField(field, 
					OntologyTermParam.ONTOLOGY_TERM_PARAM, tree, node);
		}
	}
}
