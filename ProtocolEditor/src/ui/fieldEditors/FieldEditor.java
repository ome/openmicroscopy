/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui.fieldEditors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import tree.DataFieldConstants;
import tree.IAttributeSaver;
import tree.DataFieldObserver;
import tree.IDataFieldObservable;
import ui.components.AttributeEditor;
import ui.components.AttributeMemoFormatEditor;
import ui.components.ColourMenuItem;
import ui.components.ColourPopupMenu;
import ui.components.SimpleHTMLEditorPane;
import util.ImageFactory;

public class FieldEditor extends JPanel implements DataFieldObserver {
	
	public static final Dimension MINIMUM_SIZE = new Dimension(290,300);
	
	IAttributeSaver dataField;
	IDataFieldObservable dataFieldObs;
	
	JPanel attributeFieldsPanel;
	JPanel inputTypePanel;
	JComboBox inputTypeSelector;
	AttributeMemoFormatEditor nameFieldEditor;
	AttributeMemoFormatEditor descriptionFieldEditor;
	AttributeEditor urlFieldEditor;
	
	JPopupMenu colourPopupMenu;

	SimpleHTMLEditorPane editorPane;

	private Action boldFontAction;

	protected JButton colourSelectButton;
	
	//XMLView xmlView; 	// the UI container for displaying this panel
	
	public FieldEditor() {	// a blank 
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
	}
	
	public FieldEditor(IDataFieldObservable dataFieldObs) {
		this.dataFieldObs = dataFieldObs;
		dataFieldObs.addDataFieldObserver(this);
		
		// save a reference to the datafield as an IAttributeSaver (get and set-Attribute methods)
		if (dataFieldObs instanceof IAttributeSaver) {
			this.dataField = (IAttributeSaver)dataFieldObs;
		} else {
			throw new RuntimeException("FormField(dataField) needs dataField to implement IAttributeSaver");
		}
		
		buildPanel();
	}
	
	public void buildPanel() {
		
		attributeFieldsPanel = new JPanel();	// a sub-panel to hold all components
		attributeFieldsPanel.setLayout(new BoxLayout(attributeFieldsPanel, BoxLayout.Y_AXIS));
		attributeFieldsPanel.setBorder(new EmptyBorder(5, 5, 5,5));
		
		nameFieldEditor = new AttributeMemoFormatEditor(dataField, "Field Name: ", DataFieldConstants.ELEMENT_NAME, dataField.getAttribute(DataFieldConstants.ELEMENT_NAME));
		attributeFieldsPanel.add(nameFieldEditor);
		
		// Drop-down selector of input-type. 
		inputTypePanel = new JPanel();
		inputTypePanel.setBorder(new EmptyBorder(3,3,3,3));
		inputTypePanel.setLayout(new BoxLayout(inputTypePanel, BoxLayout.X_AXIS));
		
		inputTypeSelector = new JComboBox(DataFieldConstants.UI_INPUT_TYPES);
		inputTypeSelector.setMaximumRowCount(12);
		// Set it to the current input type
		String inputType = dataField.getAttribute(DataFieldConstants.INPUT_TYPE);
		if (inputType != null) {
			for (int i=0; i<DataFieldConstants.UI_INPUT_TYPES.length; i++)
				if (inputType.equals(DataFieldConstants.INPUT_TYPES[i]))
					inputTypeSelector.setSelectedIndex(i);
		}
		
		inputTypePanel.add(new JLabel("Field Type: "));
		inputTypePanel.add(inputTypeSelector);
		// add Listener to drop-down AFTER setting it to the correct input type!
		inputTypeSelector.addActionListener(new inputTypeSelectorListener());
		attributeFieldsPanel.add(inputTypePanel);
		
		descriptionFieldEditor = new AttributeMemoFormatEditor(dataField, "Description: ", DataFieldConstants.DESCRIPTION, dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		attributeFieldsPanel.add(descriptionFieldEditor);
		
		urlFieldEditor = new AttributeEditor(dataField, "Url: ", DataFieldConstants.URL, dataField.getAttribute(DataFieldConstants.URL));
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
		 * Child Display Orientation button
		 */
		Icon rotateIcon = ImageFactory.getInstance().getIcon(ImageFactory.ROTATE_ICON);
		JButton toggleChildLayoutButton = new JButton(rotateIcon);
		toggleChildLayoutButton.setToolTipText("Display this field's children horizontally");
		toggleChildLayoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Boolean childHorizontal = dataField.isAttributeTrue(DataFieldConstants.DISPLAY_CHILDREN_HORIZONTALLY);
				// toggle
				childHorizontal = !childHorizontal;
				dataField.setAttribute
					(DataFieldConstants.DISPLAY_CHILDREN_HORIZONTALLY, childHorizontal.toString(), true);
			}
		});
		nameFieldEditor.addToToolBar(toggleChildLayoutButton);
		
		
			
		this.setLayout(new BorderLayout());
		this.add(attributeFieldsPanel, BorderLayout.NORTH);
		
		this.setPreferredSize(MINIMUM_SIZE);
		this.setMinimumSize(MINIMUM_SIZE);
		this.validate();
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
	
	// called by dataField when something changes, eg undo() previous editing
	public void dataFieldUpdated() {
		nameFieldEditor.setTextAreaText(dataField.getAttribute(DataFieldConstants.ELEMENT_NAME));
		descriptionFieldEditor.setTextAreaText(dataField.getAttribute(DataFieldConstants.DESCRIPTION));
		urlFieldEditor.setTextFieldText(dataField.getAttribute(DataFieldConstants.URL));
	}
	
	
	/*
	 * used to process colour selection from the Colour JPopupMenu. 
	 */
	public class ColourSelectorListener implements ActionListener {
		
		public void actionPerformed (ActionEvent event) {
			JMenuItem source = (JMenuItem)event.getSource();
			// if not an instance of ColourMenuItem, then no colour selected
			if (!(source instanceof ColourMenuItem)) {
				dataField.setAttribute(DataFieldConstants.BACKGROUND_COLOUR, null, true);
				return;
			}
			// otherwise, get the background colour and set the colour attribute of dataField
			Color newColour = source.getBackground();
			String colour = newColour.getRed() + ":" + newColour.getGreen() + ":" + newColour.getBlue();
			dataField.setAttribute(DataFieldConstants.BACKGROUND_COLOUR, colour, true);
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
			String newType = DataFieldConstants.INPUT_TYPES[selectedIndex];
			inputTypeSelectorChanged(newType);
		}
	}	
	
	public void inputTypeSelectorChanged(String newType) {
		// true : rememberUndo
		dataField.setAttribute(DataFieldConstants.INPUT_TYPE, newType, true);
		
		//((DataField)dataField).notifyDataFieldObservers();
	}

}
