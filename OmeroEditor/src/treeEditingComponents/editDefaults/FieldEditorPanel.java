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

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import treeEditingComponents.EditingComponentFactory;
import treeModel.TreeEditorControl;
import treeModel.fields.AbstractParam;
import treeModel.fields.Field;
import treeModel.fields.FieldParamsFactory;
import treeModel.fields.IField;
import treeModel.fields.IParam;
import ui.components.AttributeEditor;
import ui.components.AttributeMemoFormatEditor;
import ui.components.AttributeUrlEditor;
import ui.components.ColourMenuItem;
import ui.components.ColourPopupMenu;
import ui.components.CustomComboBox;
import ui.components.SimpleHTMLEditorPane;
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
public class FieldEditorPanel extends JPanel {
	
	public static final Dimension MINIMUM_SIZE = new Dimension(290,300);
	
	IField dataField;
	
	/**
	 * The controller for managing undo/redo. Eg manages attribute editing...
	 */
	TreeEditorControl controller;
	
	/**
	 * The JTree that this field is displayed in. 
	 * Used eg. to notify that this field has been edited (needs refreshing)
	 */
	JTree tree;
	
	/**
	 * A reference to the node represented by this field. 
	 * Used eg. to set the selected field to this node with undo/redo
	 */
	DefaultMutableTreeNode treeNode;
	
	
	JPanel attributeFieldsPanel;
	JPanel inputTypePanel;
	JComboBox inputTypeSelector;
	AttributeMemoFormatEditor nameFieldEditor;
	AttributeMemoFormatEditor descriptionFieldEditor;
	AttributeEditor urlFieldEditor;
	
	JPopupMenu colourPopupMenu;

	SimpleHTMLEditorPane editorPane;

	protected JButton colourSelectButton;
	
	
	public FieldEditorPanel() {	// a blank 
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
	}
	
	public FieldEditorPanel(IField field, JTree tree, 
			DefaultMutableTreeNode treeNode) {
		dataField = field;
		
		buildPanel();
	}
	
	public void buildPanel() {
		
		attributeFieldsPanel = new JPanel();	// a sub-panel to hold all components
		attributeFieldsPanel.setLayout(new BoxLayout(attributeFieldsPanel, BoxLayout.Y_AXIS));
		attributeFieldsPanel.setBorder(new EmptyBorder(5, 5, 5,5));
		
		nameFieldEditor = new AttributeMemoFormatEditor(dataField, 
				"Field Name: ", 
				Field.FIELD_NAME);
		attributeFieldsPanel.add(nameFieldEditor);
		
		// Drop-down selector of input-type. 
		inputTypePanel = new JPanel();
		inputTypePanel.setBorder(new EmptyBorder(3,3,3,3));
		inputTypePanel.setLayout(new BoxLayout(inputTypePanel, BoxLayout.X_AXIS));
		
		inputTypeSelector = new CustomComboBox(FieldParamsFactory.UI_INPUT_TYPES);
		inputTypeSelector.setMaximumRowCount(12);
		// Set it to the current input type
		String inputType = FieldParamsFactory.NO_PARAMS;
		if (dataField.getParamCount() > 0) {
			IParam param1 =  dataField.getParamAt(0);
			inputType = param1.getAttribute(AbstractParam.PARAM_TYPE);
		}
		if (inputType != null) {
			for (int i=0; i<FieldParamsFactory.UI_INPUT_TYPES.length; i++)
				if (inputType.equals(FieldParamsFactory.INPUT_TYPES[i]))
					inputTypeSelector.setSelectedIndex(i);
		}
		
		inputTypePanel.add(new CustomLabel("Field Type: "));
		inputTypePanel.add(inputTypeSelector);
		// add Listener to drop-down AFTER setting it to the correct input type!
		inputTypeSelector.addActionListener(new inputTypeSelectorListener());
		attributeFieldsPanel.add(inputTypePanel);
		
		descriptionFieldEditor = new AttributeMemoFormatEditor(dataField, 
				"Description: ", 
				Field.FIELD_DESCRIPTION);
		attributeFieldsPanel.add(descriptionFieldEditor);
		
		urlFieldEditor = new AttributeUrlEditor(dataField, "Url: ", 
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
		
		
		
		// disable editing if field is locked. 
		refreshLockedStatus();
		
		/*
		 * For each parameter of this field, add the components for
		 * editing their defualt or template values. 
		 */
		buildParamComponents();
			
		this.setLayout(new BorderLayout());
		this.add(attributeFieldsPanel, BorderLayout.NORTH);
		
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
		this.validate();
	}
	
	
	/**
	 * Add additional UI components for editing the value of this field.
	 * Use a Factory to create the UI components, depending on the value type
	 */
	public void buildParamComponents() {

		int paramCount = dataField.getParamCount();
		
		for (int i=0; i<paramCount; i++) {
			IParam param = dataField.getParamAt(i);
			JComponent edit = EditingComponentFactory.getEditDefaultComponent(param);
			if (edit != null)
				addFieldComponent(edit);
		}
	}

	private void addFieldComponent(JComponent defaultEdit) {
		attributeFieldsPanel.add(defaultEdit);
	}
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    private void maybeShowPopup(MouseEvent e) {
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
	
	/*
	 * used to process colour selection from the Colour JPopupMenu. 
	 */
	public class ColourSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JMenuItem source = (JMenuItem)event.getSource();
			// if not an instance of ColourMenuItem, then no colour selected
			if (!(source instanceof ColourMenuItem)) {
				dataField.setAttribute(Field.BACKGROUND_COLOUR, null);
				return;
			}
			// otherwise, get the background colour and set the colour attribute of dataField
			Color newColour = source.getBackground();
			String colour = newColour.getRed() + ":" + newColour.getGreen() + ":" + newColour.getBlue();
			dataField.setAttribute(Field.BACKGROUND_COLOUR, colour);
		}
	}	
	
	/*
	 * takes the input-type of the field and sets the inputType attribute of dataField
	 * This causes new FieldEditor and FormField subclasses to be created for this inputType
	 */
	public class inputTypeSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JComboBox source = (JComboBox)event.getSource();
			int selectedIndex = source.getSelectedIndex();
			String newType = FieldParamsFactory.INPUT_TYPES[selectedIndex];
			inputTypeSelectorChanged(newType);
		}
	}	
	
	public void inputTypeSelectorChanged(String newType) {
		
		if (dataField.getParamCount() > 0) {
			IParam oldParam = dataField.getParamAt(0);
			dataField.removeParam(oldParam);
		}
		IParam newParam = FieldParamsFactory.getFieldParam(newType);
		dataField.addParam(0, newParam);
		
		refreshSizeOfPanel();
	}
	
	
	
	/**
	 * This method is used to refresh the size of this panel in the JTree.
	 * It must also remain in the editing mode, otherwise the user who
	 * is currently editing it will be required to click again to 
	 * continue editing.
	 * This can be achieved by calling startEditingAtPath(tree, path)
	 */
	public void refreshSizeOfPanel() {
		if ((tree != null) && (treeNode !=null)) {
			
			TreePath path = new TreePath(treeNode.getPath());
			
			tree.getUI().startEditingAtPath(tree, path);
		}
	}

}
