 /*
 * treeEditingComponents.FieldEditorPanel 
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import treeEditingComponents.AbstractParamEditor;
import treeEditingComponents.EditingComponentFactory;
import treeEditingComponents.ITreeEditComp;
import treeModel.TreeEditorControl;
import treeModel.fields.AbstractParam;
import treeModel.fields.Field;
import treeModel.fields.FieldParamsFactory;
import treeModel.fields.IAttributes;
import treeModel.fields.IField;
import treeModel.fields.IParam;
import ui.components.AttributeEditor;
import ui.components.AttributeMemoFormatEditor;
import ui.components.AttributeUrlEditor;
import ui.components.ColourMenuItem;
import ui.components.ColourPopupMenu;
import ui.components.SimpleHTMLEditorPane;
import uiComponents.CustomComboBox;
import uiComponents.CustomLabel;
import util.ImageFactory;

/** 
 * The Panel for editing the "Template" of each field.
 * This includes the Name, Description etc.
 * Also, this panel contains the components for template editing of 0, 1 or more 
 * parameters of the field. eg Default values, units etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldEditorPanel 
	extends JPanel 
	implements PropertyChangeListener {
	
	public static final Dimension MINIMUM_SIZE = new Dimension(290,300);
	
	/**
	 * The field that this UI component edits.
	 */
	private IField field;
	
	/**
	 * The controller for managing undo/redo. Eg manages attribute editing...
	 */
	private TreeEditorControl controller;
	
	/**
	 * The JTree that this field is displayed in. 
	 * Used eg. to notify that this field has been edited (needs refreshing)
	 */
	private JTree tree;
	
	/**
	 * A reference to the node represented by this field. 
	 * Used eg. to set the selected field to this node with undo/redo
	 */
	private DefaultMutableTreeNode treeNode;

	/**
	 * Vertical Box layout panel. Main panel.
	 */
	private JPanel attributeFieldsPanel;
	
	/**
	 * A comboBox for changing the type of parameter
	 */
	private JComboBox inputTypeSelector;
	
	/**
	 * Formatted text field for editing the field name
	 */
	private AttributeMemoFormatEditor nameFieldEditor;
	
	/**
	 * Formatted text field for editing the field description
	 */
	private AttributeMemoFormatEditor descriptionFieldEditor;
	
	/**
	 * Text Field for editing the URL
	 */
	private AttributeEditor urlFieldEditor;
	
	/**
	 * A pop-up menu for choosing a background colour for the field
	 */
	private JPopupMenu colourPopupMenu;

	/**
	 * Launches the colour pop-up menu
	 */
	protected JButton colourSelectButton;
	
	/**
	 * A bound property of this panel.
	 * A change in this property indicates that this panel should be rebuilt
	 * from the data model. 
	 */
	public static final String PANEL_CHANGED_PROPERTY = "panelChangedProperty";
	
	/**
	 * Creates a blank panel. Displayed when more than one field is selected
	 * (also when no fields selected eg when application starts)
	 */
	public FieldEditorPanel() {	// a blank 
		this.setPreferredSize(MINIMUM_SIZE);
		//this.setMinimumSize(MINIMUM_SIZE);
	}
	
	/**
	 * Creates an instance of this class for editing the field.
	 * 
	 * @param field		The Field to edit
	 * @param tree		The JTree in which the field is displayed
	 * @param treeNode	The node of the Tree which contains the field
	 */
	public FieldEditorPanel(IField field, JTree tree, 
			DefaultMutableTreeNode treeNode, TreeEditorControl controller) {
		this.field = field;
		
		this.tree = tree;
		System.out.println("FieldEditorPanel constructor treeNode: " + treeNode);
		this.treeNode = treeNode;
		this.controller = controller;
		
		buildPanel();
	}
	
	/**
	 * Sets the controller. This must be called before the field can be 
	 * used to edit (manages undo redo etc).
	 * 
	 * @param controller
	 */
	public void setController(TreeEditorControl controller) {
		this.controller = controller;
	}
	
	/**
	 * Builds the UI. 
	 */
	private void buildPanel() {
		
		/*
		 * Panel to hold all components, vertically 
		 */
		attributeFieldsPanel = new JPanel();
		attributeFieldsPanel.setLayout(new BoxLayout
				(attributeFieldsPanel, BoxLayout.Y_AXIS));
		attributeFieldsPanel.setBorder(new EmptyBorder(5, 5, 5,5));
		
		/*
		 * Field for editing name
		 */
		nameFieldEditor = new AttributeMemoFormatEditor(field, 
				"Field Name: ", 
				Field.FIELD_NAME);
		nameFieldEditor.setDisplayName("Edit Name");	// for undo/redo display
		nameFieldEditor.addPropertyChangeListener(
				ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		attributeFieldsPanel.add(nameFieldEditor);
		
		/* 
		 * Drop-down selector of input-type. 
		 */ 
		JPanel inputTypePanel = new JPanel();
		inputTypePanel.setBorder(new EmptyBorder(3,3,3,3));
		inputTypePanel.setLayout(new BorderLayout());
		
		inputTypeSelector = new CustomComboBox(FieldParamsFactory.UI_INPUT_TYPES);
		inputTypeSelector.setMaximumRowCount(
				FieldParamsFactory.UI_INPUT_TYPES.length);
		// Set it to the current input type
		String inputType = FieldParamsFactory.NO_PARAMS;
		if (field.getParamCount() > 0) {
			IParam param1 =  field.getParamAt(0);
			inputType = param1.getAttribute(AbstractParam.PARAM_TYPE);
		}
		if (inputType != null) {
			for (int i=0; i<FieldParamsFactory.UI_INPUT_TYPES.length; i++)
				if (inputType.equals(FieldParamsFactory.INPUT_TYPES[i]))
					inputTypeSelector.setSelectedIndex(i);
		}
		inputTypePanel.add(new CustomLabel("Field Type: "), BorderLayout.WEST);
		inputTypePanel.add(inputTypeSelector, BorderLayout.CENTER);
		// add Listener to drop-down AFTER setting it to the correct input type!
		inputTypeSelector.addActionListener(new ParamTypeSelectorListener());
		attributeFieldsPanel.add(inputTypePanel);
		
		/*
		 * Field for editing description.
		 */
		descriptionFieldEditor = new AttributeMemoFormatEditor(field, 
				"Description: ", 
				Field.FIELD_DESCRIPTION);
		descriptionFieldEditor.setDisplayName("Edit Description");
		descriptionFieldEditor.addPropertyChangeListener( 
				ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		attributeFieldsPanel.add(descriptionFieldEditor);
		
		urlFieldEditor = new AttributeUrlEditor(field, "Url: ", 
				Field.FIELD_URL);
		attributeFieldsPanel.add(urlFieldEditor);
		
		/* colour-picker */
		// make some colours
		Color[] colours = {Color.RED, new Color(255,153,153),new Color(204,153,51), 
				Color.YELLOW, Color.GREEN, new Color(153,255,153), Color.BLUE, 
				 new Color(51,153,204), new Color(153,153,255)};
		
		// display them in a custom popup menu, using a colourListener to get colour
		ColourSelectorListener colourListener = new ColourSelectorListener();
		colourPopupMenu = new ColourPopupMenu(colours, colourListener);
		
		//...which is displayed from a button..
		Icon colourSelectIcon = ImageFactory.getInstance().getIcon(ImageFactory.COLOUR_SELECTION_ICON);
		colourSelectButton = new JButton(colourSelectIcon);
		colourSelectButton.setToolTipText("Set background colour");
		colourSelectButton.addMouseListener(new PopupListener());
		
		//...add to tool bar at top of panel
		nameFieldEditor.addToToolBar(colourSelectButton);
		
		/*
		 * For each parameter of this field, add the components for
		 * editing their defualt or template values. 
		 */
		buildParamComponents();
			
		// disable editing if field is locked. 
		refreshLockedStatus();
		
		this.setLayout(new BorderLayout());
		this.add(attributeFieldsPanel, BorderLayout.NORTH);
		
		//this.setPreferredSize(MINIMUM_SIZE);
		//this.setMinimumSize(MINIMUM_SIZE);
		this.validate();
	}
	
	
	/**
	 * Add additional UI components for editing the value of this field.
	 * Use a Factory to create the UI components, depending on the value type
	 */
	public void buildParamComponents() {

		int paramCount = field.getParamCount();
		
		attributeFieldsPanel.add(new JSeparator());
		attributeFieldsPanel.add(Box.createVerticalStrut(5));
		JLabel paramLabel = new CustomLabel("Parameters:");
		JPanel paramHeader = new JPanel(new BorderLayout());
		paramHeader.add(paramLabel, BorderLayout.WEST);
		
		JButton addParamsButton = new AddParamActions(field, tree, 
				treeNode, controller).getButton();
		addParamsButton.addPropertyChangeListener(
				AddParamActions.PARAM_ADDED_PROPERTY, this);
		paramHeader.add(addParamsButton, BorderLayout.EAST);
		
		attributeFieldsPanel.add(paramHeader);
		
		for (int i=0; i<paramCount; i++) {
			IParam param = field.getParamAt(i);
			JComponent edit = EditingComponentFactory.getEditDefaultComponent(param);
			if (edit != null)
				addFieldComponent(edit);
		}
	}

	/**
	 * Each parameter editing component is added here.
	 * This class becomes a property change listener for each one.
	 * 
	 * @param defaultEdit	A component for editing the defaults of each param
	 */
	private void addFieldComponent(JComponent defaultEdit) {
		attributeFieldsPanel.add(Box.createVerticalStrut(5));
		attributeFieldsPanel.add(defaultEdit);
		defaultEdit.addPropertyChangeListener( 
				ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
	}
	
	/**
	 * A MouseAdapter to display the colour chooser pop-up menu.
	 * 
	 * @author will
	 *
	 */
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        showPopUp(e);
	    }
	    public void mouseReleased(MouseEvent e) {
	        showPopUp(e);
	    }
	    private void showPopUp(MouseEvent e) {
	    	colourPopupMenu.show(e.getComponent(),
	                       e.getX(), e.getY());
	    }
	}

	/**
	 * This method checks to see if the current field is locked: 
	 * Then it passes the locked status to enableEditing()
	 */
	public void refreshLockedStatus() {
		
		//TODO
		//String lockedLevel = ((DataField)dataField).getLockedLevel();
		/*
		 * Allow editing if not locked
		 */
		// enableEditing(lockedLevel == null);
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FieldEditor.
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		nameFieldEditor.getTextArea().setEnabled(enabled);
		descriptionFieldEditor.getTextArea().setEnabled(enabled);
		urlFieldEditor.getTextField().setEnabled(enabled);
		colourSelectButton.setEnabled(enabled);
		inputTypeSelector.setEnabled(enabled);
	}
	
	/**
	 * used to process colour selection from the Colour JPopupMenu. 
	 */
	public class ColourSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JMenuItem source = (JMenuItem)event.getSource();
			// if not an instance of ColourMenuItem, then no colour selected
			if (!(source instanceof ColourMenuItem)) {
				fieldEdited(Field.BACKGROUND_COLOUR, null, "Clear Colour");
				updateEditingOfTreeNode();
				return;
			}
			// otherwise, get the background colour and set the colour attribute of dataField
			Color newColour = source.getBackground();
			String colour = newColour.getRed() + ":" + newColour.getGreen() + ":" + newColour.getBlue();
			fieldEdited(Field.BACKGROUND_COLOUR, colour, "Edit Colour");
			updateEditingOfTreeNode();
		}
	}	
	
	/**
	 * takes the parameter-type of the comboBox and 
	 * calls paramTypeChanged(String paramType)
	 */
	public class ParamTypeSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JComboBox source = (JComboBox)event.getSource();
			int selectedIndex = source.getSelectedIndex();
			String newType = FieldParamsFactory.INPUT_TYPES[selectedIndex];
			paramTypeChanged(newType);
		}
	}	
	
	/**
	 * Changes the Parameter type of the first parameter of this field.
	 * In future, it may be preferable to allow users to change the type
	 * of other parameters of this field, depending on selection etc. 
	 * 
	 * @param newType	A String that defines the type of parameter selected
	 */
	public void paramTypeChanged(String newType) {
		
		if (field.getParamCount() > 0) {
			IParam oldParam = field.getParamAt(0);
			field.removeParam(oldParam);
		}
		IParam newParam = FieldParamsFactory.getFieldParam(newType);
		field.addParam(0, newParam);
		
		/* refresh this node in the JTree, and rebuild this panel*/
		updateEditingOfTreeNode();
		rebuildEditorPanel();
	}
	
	/**
	 * Called by components of this panel when they want to perform an edit
	 * that is added to the undo/redo queue. 
	 * 
	 * @param attrName		The name of the attribute (can be null if more than
	 * 		one attribute is being edited)
	 * @param newVal		The new value of the attribute. Could be a string
	 *  	(if one attribute edited) or a Map, if more than one value edited. 	
	 * @param displayName	A display name for undo/redo
	 */
	public void fieldEdited(String attrName, Object newVal, String displayName) {
		
		/* Need controller to pass on the edit  */
		if (controller == null) return;
		
		System.out.println("FieldEditorPanel fieldEdited " + attrName + " " + newVal);

		if ((newVal instanceof String) || (newVal == null)){
			String newValue = (newVal == null ? null : newVal.toString());
		 	controller.editAttribute(field, attrName, newValue, 
		 			displayName, tree, treeNode);
		}
		
		else if (newVal instanceof HashMap) {
			HashMap newVals = (HashMap)newVal;
			controller.editAttributes(field, displayName, newVals, 
					tree, treeNode);
		}
	}
	
	/**
	 * If the size of a sub-component of this panel changes, 
	 * the JTree in which it is contained must be required to 
	 * re-draw the panel. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		
		String propName = evt.getPropertyName();
		
		//System.out.println("FieldEditorPanel propertyChanege: " + propName);
				
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(propName)) {
			
			if (evt.getSource() instanceof ITreeEditComp) {
				
				/* Need controller to pass on the edit  */
				if (controller == null) return;
				
				ITreeEditComp src = (ITreeEditComp)evt.getSource();
				IAttributes param = src.getParameter();
				String attrName = src.getAttributeName();
				String displayName = src.getEditDisplayName();
				
				String newValue;
				Object newVal = evt.getNewValue();
				
				
				if ((newVal instanceof String) || (newVal == null)){
					newValue = (newVal == null ? null : newVal.toString());
				 	controller.editAttribute(param, attrName, newValue, 
				 			displayName, tree, treeNode);
				}
				
				else if (newVal instanceof HashMap) {
					HashMap newVals = (HashMap)newVal;
					controller.editAttributes(param, displayName, newVals, 
							tree, treeNode);
				}
				
				updateEditingOfTreeNode();
				
			}
		} else if (AddParamActions.PARAM_ADDED_PROPERTY.equals(propName)) {
			updateEditingOfTreeNode();
			rebuildEditorPanel();
		}
	}
	
	
	/**
	 * This method is used to refresh the size of the corresponding
	 * node in the JTree.
	 * It must also remain in the editing mode, otherwise the user who
	 * is currently editing it will be required to click again to 
	 * continue editing.
	 * This can be achieved by calling startEditingAtPath(tree, path)
	 */
	public void updateEditingOfTreeNode() {
		if ((tree != null) && (treeNode !=null)) {
			
			TreePath path = new TreePath(treeNode.getPath());
			
			tree.getUI().startEditingAtPath(tree, path);
		}
	}
	
	public void rebuildEditorPanel() {
		this.firePropertyChange(PANEL_CHANGED_PROPERTY, null, "refresh");
	}
}
