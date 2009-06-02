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

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.model.params.BooleanParam;
import org.openmicroscopy.shoola.agents.editor.model.params.DateTimeParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EditorLinkParam;
import org.openmicroscopy.shoola.agents.editor.model.params.EnumParam;
import org.openmicroscopy.shoola.agents.editor.model.params.NumberParam;
import org.openmicroscopy.shoola.agents.editor.model.params.OntologyTermParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.PopupMenuButton;


/** 
 * This class is a button with a pop-up menu that allows users to 
 * add a parameter to a field. 
 * Button should be displayed in the template-editing panel (FieldEditorPanel).
 * Choosing of an Action from the pop-up list is indicated by the 
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
	extends PopupMenuButton
{
	
	/**
	 * A bound property (of the button). 
	 * Change indicates that a parameter has been added to the field. 
	 * The type of new parameter to add is passed as the new value. 
	 */
	public static final String PARAM_ADDED_PROPERTY = "paramAddedProperty";
	
	/** The new value of the param-added-property when adding a data-ref */
	public static final String ADD_DATA_REF = "addDataReference";
	
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
				new AddNumberParamAction(),
				new AddBooleanParamAction(),
				new AddEnumParamAction(),
				new AddDateTimeParamAction(),
				new AddEditorLinkParamAction(),
				new AddOntologyParamAction(),
				new AddDataRefAction()};
		

		for (int i=0; i< actions.length; i++) {
			addAction(actions[i]);
		}
	}
	
	/**
	 * Called by all the Actions. 
	 * 
	 * @param paramType		The type of the parameter selected.  
	 */
	private void addParameter(String paramType) {
		
		firePropertyChange(PARAM_ADDED_PROPERTY, null, paramType);
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
	public AddParamActions() 
	{
		super("Add parameter", 
				IconManager.getInstance().getIcon(IconManager.ADD_NUMBER));
		
		initialise();
	}
	
	
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
			addParameter(TextParam.TEXT_LINE_PARAM);
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
			addParameter(EnumParam.ENUM_PARAM);
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
			addParameter(NumberParam.NUMBER_PARAM);
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
			addParameter(BooleanParam.BOOLEAN_PARAM);
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
			addParameter(DateTimeParam.DATE_TIME_PARAM);
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
			putValue(Action.NAME, "Add Data Reference");
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
			addParameter(ADD_DATA_REF);
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
			addParameter(OntologyTermParam.ONTOLOGY_TERM_PARAM);
		}
	}
	
	/**
	 * Action for adding an Ontology Term Parameter
	 * 
	 * @author will
	 */
	public class AddEditorLinkParamAction 
		extends AbstractAction 
	{
		/**
		 * Creates an instance. 
		 */
		public AddEditorLinkParamAction() 
		{
			putValue(Action.NAME, "Add Editor Link Parameter");
			putValue(Action.SHORT_DESCRIPTION,
					"Add a Parameter that links to another Editor file.");
			putValue(Action.SMALL_ICON, iM.getIcon(IconManager.OMERO_EDITOR)); 
		}
		
		/** 
	     * Adds a new Editor Link Parameter.
	     * 
	     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	     */
		public void actionPerformed(ActionEvent e) 
		{
			addParameter(EditorLinkParam.EDITOR_LINK_PARAM);
		}
	}
}
