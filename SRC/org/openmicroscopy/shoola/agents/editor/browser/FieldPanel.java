
/*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldPanel
 * 
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

package org.openmicroscopy.shoola.agents.editor.browser;

// Java Imports

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamUIFactory;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamValuesTable;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.FieldNode;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TextContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddFieldTableEdit;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.ImageBorderFactory;
import org.openmicroscopy.shoola.agents.editor.uiComponents.TableEditUI;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/**
 * This is the UI component that represents a field in the JTree.
 * Displays a name, description etc. and holds other UI 
 * components that are specific to the types of data being edited. 
 * These components display and edit the Parameters of this field.
 * 
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class FieldPanel 
	extends JPanel 
	implements 
	PropertyChangeListener,
	ActionListener
{
	
	/**
	 * This Field listens for changes to this property in the parameter
	 * editing components it contains. 
	 * Change in this property indicates that the field is currently being 
	 * edited and needs refreshing. 
	 * 
	 * @ see refreshEditingOfPanel()
	 */
	public static final String UPDATE_EDITING_PROPERTY = "sizeChangedPropery";
	
	/**
	 * This Field listens for changes to this property in the parameter
	 * editing components it contains. 
	 * Changes in this property indicates that this field needs to 
	 * call nodeChanged() on this node in the TreeModel.
	 */
	public static final String 	NODE_CHANGED_PROPERTY = "nodeChangedProperty";
	
	/**
	 * An ActionCommand for the Description button. 
	 */
	public static final String 		TOGGLE_DESCRIPTION_CMD = "toggleDesc";
	
	/**
	 * An ActionCommand for the Load defaults button. 
	 */
	public static final String 		LOAD_DEFAULTS_CMD = "loadDefaults";
	
	/**
	 * An ActionCommand for the URL-link button. 
	 */
	public static final String 		OPEN_URL_CMD = "openUrl";
	
	/**	
	 * An ActionCommmand for adding a table for displaying multiple
	 * values for each parameter. 
	 */
	public static final String 		ADD_TABLE_CMD = "addFieldTable";
	
	/**
	 * An ActionCommand for removing a table that displays multiple values
	 * for each parameter. 
	 */
	public static final String 		REMOVE_TABLE_CMD = "removeFieldTable";
	
	/**
	 * The source of data that this field is displaying and editing. 
	 */
	private IField 					field;
	
	/**
	 * The controller for managing undo/redo. Eg manages attribute editing...
	 */
	private BrowserControl 			controller;
	
	/**
	 * The JTree that this field is displayed in. 
	 * Used eg. to notify that this field has been edited (needs refreshing)
	 */
	private JTree 					tree;
	
	/**
	 * A reference to the node represented by this field. 
	 * Used eg. to set the selected field to this node with undo/redo
	 */
	private DefaultMutableTreeNode	treeNode;
	
	/**
	 * This panel (BorderLayout) contains nameLabel in the WEST of this,
	 * then the horizontalBox for all other items is in the CENTER
	 */
	private JPanel 					contentsPanel;
	
	/**
	 * A label to display the name of the field
	 */
	private JLabel 					nameLabel;
	
	/**
	 * Horizontal Box which contains the (i) descriptionButton, urlButton,
	 * requiredFieldButton and defaultButton. 
	 * Additional components for editing the experimental values are also
	 * added to this Box. 
	 */
	private Box 					horizontalBox;
	
	/**
	 * A button to toggle the display of the field's description
	 */
	private JButton 				descriptionButton;
	
	/**
	 * A button to add a table to this field, to show multiple values for
	 * the parameters in this field. 
	 */
	private JButton 				fieldTableButton;
	
	/**
	 * A label used to display the description of the field
	 * The visibility of this can be toggled
	 */
	private JLabel 					descriptionLabel;
	
	/**
	 * The source of icons
	 */
	private IconManager 			iconManager;
	
	/**
	 * A flag to indicate if this field is highlighted. 
	 */
	private boolean 				highlighted;
	
	/**
	 * The background colour of this field.
	 */
	private Color 					paintedColour;
	
	/**
	 * A border created from images, with drop shadow and rounded corners.
	 * This is the border for the FormField panel.
	 */
	private Border 					imageBorder;
	
	/**
	 * An identical border to the image border, except that the colour of 
	 * the inside of the border matches the blue highlight colour.
	 */
	private Border 					imageBorderHighlight;
	
	/**
	 * Maximum number of characters to display for the field name.
	 */
	private static final int 		MAX_CHARS = 25;
	
	/**
	 * Initialises the UI components. 
	 */
	private void initialise() 
	{
		iconManager = IconManager.getInstance();
		
		imageBorder = ImageBorderFactory.getImageBorder();
		imageBorderHighlight = ImageBorderFactory.getImageBorderHighLight();
		
		// A label to display the name of the field.
		nameLabel = new CustomLabel();
		nameLabel.setBackground(null);
		nameLabel.setOpaque(false);

		
		// A description label displays description below the field.
		descriptionLabel = new CustomLabel();
		descriptionLabel.setBackground(null);
		
		// Description button
		Icon infoIcon = iconManager.getIcon(IconManager.INFO_ICON);
		descriptionButton = new CustomButton(infoIcon);
		descriptionButton.setFocusable(false); // so it is not selected by tabbing
		descriptionButton.setActionCommand(TOGGLE_DESCRIPTION_CMD);
		descriptionButton.addActionListener(this);
		descriptionButton.setVisible(false);	// only made visible if description exists.
		setDescriptionText(); 	// will update description label
		
		// Button for adding a table for display of parameter data
		Icon paramTableIcon = iconManager.getIcon(IconManager.ADD_TABLE_ICON);
		Icon removeTableIcon = iconManager.getIcon(IconManager.REMOVE_TABLE_ICON);
		fieldTableButton = new CustomButton();
		fieldTableButton.addActionListener(this);
		if (field.getTableData() == null) {
			fieldTableButton.setToolTipText("Add a table for parameter values, " +
				"so that multiple values can be set for each parameter");
			fieldTableButton.setVisible(AddFieldTableEdit.canDo(field));
			fieldTableButton.setIcon(paramTableIcon);
			fieldTableButton.setActionCommand(ADD_TABLE_CMD);
		} else {
			fieldTableButton.setToolTipText("Remove table for parameter values");
			fieldTableButton.setIcon(removeTableIcon);
			fieldTableButton.setActionCommand(REMOVE_TABLE_CMD);
		}
		
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI() 
	{
		this.setBorder(null);
		this.setBackground(null);
		this.setLayout(new BorderLayout());
		
		/*
		 * Horizontal Box holds various buttons. More components can be 
		 * added later depending on the field type being displayed. 
		 */
		horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(descriptionButton);
		horizontalBox.add(fieldTableButton);
		horizontalBox.add(Box.createHorizontalStrut(10));
		
		/*
		 * complex layout required to limit the size of nameLabel (expands with html content)
		 * contentsPanel(BorderLayout) contains nameLabel in the WEST of this,
		 * then the horizontalBox for all other items is in the CENTER
		 */
		contentsPanel = new JPanel(new BorderLayout());
		
		contentsPanel.add(nameLabel, BorderLayout.WEST);
		contentsPanel.add(horizontalBox, BorderLayout.CENTER);
		contentsPanel.add(descriptionLabel, BorderLayout.SOUTH);
		
		contentsPanel.setBorder(imageBorder);
		
		this.add(contentsPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Add additional UI components for editing the parameters of this field.
	 * If table-data exists for this field (multiple values for each parameter)
	 * show this in a table. 
	 * Otherwise, use a Factory to create the UI components, depending on the 
	 * type of each parameter. 
	 */
	private void buildParamComponents() 
	{
		// show a table, one column per parameter, that displays multiple 
		// values for each parameter. 
		if (field.getTableData() != null) {
			
			// table sets cell renderers and editors according to parameter types
			JTable table = new ParamValuesTable(field);
			// put the table in a UI with buttons for adding and deleting rows
			TableEditUI tableUI = new TableEditUI(table);
			tableUI.addPropertyChangeListener(UPDATE_EDITING_PROPERTY, this);
			addFieldComponent(tableUI);
			
		} else {
			int paramCount = field.getContentCount();
			JComponent edit;
			for (int i=0; i<paramCount; i++) {
				IFieldContent content = field.getContentAt(i);
				if (content instanceof IParam) {
					IParam param = (IParam)content;
					edit = ParamUIFactory.getEditingComponent(param);
					if (edit != null) {
						addFieldComponent(edit);
					}
				} 
			}
		}
	}

	/**
	 * Used to add additional components to the field.
	 * Will be displayed horizontally. 
	 * Also adds this class as a property listener to these components. 
	 * 
	 * @param comp	The component to add.
	 */
	private void addFieldComponent(JComponent comp) 
	{
		
		horizontalBox.add(Box.createHorizontalStrut(5));
		horizontalBox.add(comp);
		
		comp.addPropertyChangeListener(UPDATE_EDITING_PROPERTY, this);
		comp.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		comp.addPropertyChangeListener(NODE_CHANGED_PROPERTY, this);
	}

	/**
	 * Checks to see whether a default value exists for this field.
	 * If so, the default button becomes visible, with tool-tip
	 * displaying the default value;
	 */
	private void refreshDefaultValue() {
		/*
		/TODO
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		boolean defaultExists = ((defaultValue != null) && 
					(defaultValue.length()>0));
		
		defaultButton.setVisible(defaultExists);
		
		if (defaultExists)
			defaultButton.setToolTipText("Default: " + defaultValue);
	
		else 
			defaultButton.setToolTipText(null);
			*/
	}

	// these methods called when user updates the fieldEditor panel
	
	/**
	 * Sets the value of the name label.
	 * 
	 * @param name 		The text to display as the name of the field.
	 */
	private void setNameText() 
	{
		String t = TreeOutlineCellRenderer.getFieldDisplayName(field, treeNode);
		nameLabel.setText(t);
	}

	/**
	 * Called when the UI is built. Sets the visibility, text etc of 
	 * the description box. 
	 */
	private void setDescriptionText() 
	{
		String description = getDescription();
		boolean showDesc = false;
		if (treeNode instanceof FieldNode) {
			showDesc = ((FieldNode)treeNode).getDescriptionVisisibility();
		}
		
		if ((description != null) && (description.trim().length() > 0)) {
			String htmlDescription = 
				"<html><div style='width:250px; padding-left:30px;'>" + 
				description + 
				"</div></html>";
			descriptionButton.setToolTipText(htmlDescription);
			descriptionButton.setVisible(true);
			descriptionLabel.setVisible(showDesc);
			descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 9));
			descriptionLabel.setText(htmlDescription);
		}
		else
		{
			descriptionButton.setToolTipText(null);
			descriptionButton.setVisible(false);
			descriptionLabel.setText(null);
			descriptionLabel.setVisible(false);
		}
	}
	
	/**
	 * Returns a String representation of the content of this field. 
	 * This will be text, possibly interspersed with parameters. 
	 * But, doesn't add parameters on the end, if not followed by text. 
	 */
	private String getDescription() 
	{
		if (field.getContentCount() == 0) return null;
		
		int contentCount = field.getContentCount();
		IFieldContent content;
		StringBuffer buf = new StringBuffer();

		for (int i=0; i< contentCount; i++) {
			content = field.getContentAt(i);
			// don't print parameters unless followed by text 
			if (content instanceof IParam) {
				boolean followedByText = false;
				for (int c=i; c<contentCount; c++) {
					if (field.getContentAt(c) instanceof TextContent) {
						followedByText = true;
						continue;
					}
				}
				if (followedByText) {
					buf.append("[" + content.toString() + "]");
				}
			} else {
				buf.append(content.toString());
			}
		}
		String text = buf.toString();
		if (text.length() == 0) 	return null;	// don't return blank string
		return text;
	}


	/**
	 * Gets the {@link Field#BACKGROUND_COLOUR} attribute, converts it to 
	 * a colour and refreshes the background. 
	 * Then calls {@link #refreshHighlighted()}
	 */
	private void refreshBackgroundColour() 
	{
		paintedColour = getColorFromString(field.getAttribute(
				Field.BACKGROUND_COLOUR));
	
		if (paintedColour == null) {
			String ancestorColour = TreeModelMethods.getAttributeFromAncestor
					(Field.BACKGROUND_COLOUR, treeNode);
			paintedColour = getColorFromString(ancestorColour);
		}
		
		refreshHighlighted();
	}

	/**
	 * Sets the background colour and border, depending on the highlighted
	 * state, and {@link paintedColour}. 
	 */
	private void refreshHighlighted() 
	{
		if (highlighted) { 
			contentsPanel.setBackground(paintedColour != null ? paintedColour : 
				UIUtilities.BLUE_HIGHLIGHT);
			contentsPanel.setBorder(imageBorderHighlight);
		}
		else {
			contentsPanel.setBackground((paintedColour == null) ? ImageBorderFactory.DEFAULT_BACKGROUND : paintedColour);
			contentsPanel.setBorder(imageBorder);
		}
	}

	/**
	 * Load defaults. 
	 */
	private void loadDefaultValue() 
	{
		// TODO 
		// Make this action go through the Load Default Action. 
		
		/*
		String valueAttribute = EditCopyDefaultValues.
				getValueAttributeForLoadingDefault(dataField);
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		
		dataField.setAttribute(valueAttribute, defaultValue, true);
		*/
	}

	/**
	 * This method is used to refresh the size of this panel in the JTree.
	 * It must also remain in the editing mode, otherwise the user who
	 * is currently editing it will be required to click again to 
	 * continue editing.
	 * This can be achieved by calling 
	 * {@link BasicTreeUI#startEditingAtPath(JTree, TreePath)}
	 */
	private void refreshEditingOfPanel() 
	{
		if ((tree != null) && (treeNode !=null)) {
			
			TreePath path = new TreePath(treeNode.getPath());
			
			tree.getUI().startEditingAtPath(tree, path);
		}
	}

	/**
	 * This method is used to refresh the size of this panel in the JTree,
	 * without selecting this path for editing.
	 */
	private void refreshPanel() 
	{
		if ((tree != null) && (treeNode !=null)) {
			
			DefaultTreeModel mod = (DefaultTreeModel)tree.getModel();
			
			mod.nodeChanged(treeNode);
		}
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param field		The source of data for this display	
	 * @param tree		The JTree where this panel is displayed
	 * @param treeNode	The node that this panel represents
	 */
	public FieldPanel(IField field, JTree tree, DefaultMutableTreeNode treeNode,
			BrowserControl controller) 
	{
		
		this.field = field;
		this.tree = tree;
		this.treeNode = (FieldNode) treeNode;
		this.controller = controller;
		
		initialise();	
		
		buildUI();

		// Update components with the values from field
		setNameText();
		setDescriptionText();
		
		refreshBackgroundColour();
		
		refreshDefaultValue();
		
		buildParamComponents();
	}
	
	/**
	 * This causes the field to be Selected, with a blue background etc. 
	 * 
	 * @param selected		If true, field is coloured blue. 
	 */
	public void setSelected(boolean selected) {
		highlighted = selected;
		refreshHighlighted();
	}
	
	/**
	 * Used to convert a stored string Colour attribute to a Color
	 * 
	 * @param colourAttribute	A colour in "red:green:blue" format. 
	 * @return Color			The colour object.
	 */
	public static Color getColorFromString(String colourAttribute) {
		if (colourAttribute == null) 
			return null;
		
		try{
			String[] rgb = colourAttribute.split(":");
			int red = Integer.parseInt(rgb[0]);
			int green = Integer.parseInt(rgb[1]);
			int blue = Integer.parseInt(rgb[2]);
			return new Color(red,green,blue);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * If the size of a sub-component of this panel changes, 
	 * the JTree in which it is contained must be required to 
	 * re-draw the panel. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String propName = evt.getPropertyName();
		
		if (UPDATE_EDITING_PROPERTY.equals(propName)) {
		
			refreshEditingOfPanel();
		}
		
		else if (NODE_CHANGED_PROPERTY.equals(propName)) {
			refreshPanel();
		}
		
		else if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(propName)) {
			
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
				
			}
		}
	}
	
	/**
	 * Handles the actions of several buttons that could appear in 
	 * this panel. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		 
		if (TOGGLE_DESCRIPTION_CMD.equals(cmd)) {
			if (treeNode instanceof FieldNode) {
				((FieldNode)treeNode).toggleDescriptionVisibility();
			}
			refreshEditingOfPanel();
		}
		
		else if (LOAD_DEFAULTS_CMD.equals(cmd)) {
			loadDefaultValue();
		}
		
		else if (ADD_TABLE_CMD.equals(cmd)) {
			controller.addFieldTable(field, tree, treeNode);
		}
		
		else if (REMOVE_TABLE_CMD.equals(cmd)) {
			controller.removeFieldTable(field, tree, treeNode);
		}
	}
	

}
