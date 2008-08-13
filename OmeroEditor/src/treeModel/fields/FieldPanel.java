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

package treeModel.fields;


// Java Imports

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import tree.DataFieldConstants;
import treeEditingComponents.EditingComponentFactory;
import treeEditingComponents.ITreeEditComp;
import treeEditingComponents.TableEditor;
import treeModel.TreeEditorControl;
import ui.XMLView;
import ui.components.ImageBorder;
import uiComponents.CustomLabel;
import util.BareBonesBrowserLaunch;
import util.ImageFactory;


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
	implements PropertyChangeListener 
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
	public static final String NODE_CHANGED_PROPERTY = "nodeChangedProperty";
	
	/**
	 * The source of data that this field is displaying and editing. 
	 */
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
	
	
	/*
	 * swing components
	 */ 
	JPanel contentsPanel;
	
	/**
	 * A label to display the name of the field
	 */
	JLabel nameLabel;
	
	/**
	 * Horizontal Box which contains the (i) descriptionButton, urlButton,
	 * requiredFieldButton and defaultButton. 
	 * Additional components for editing the experimental values are also
	 * added to this Box. 
	 */
	Box horizontalBox;
	
	/**
	 * A button to toggle the display of the field's description
	 */
	JButton descriptionButton;
	
	/**
	 * This button is visible if a "url" value has been set. 
	 * Clicking it will open a web browser with the url.
	 */
	JButton urlButton;

	/**
	 * This button doesn't do anything. Just indicates that field is required. 
	 */
	JButton requiredFieldButton;
	
	/**
	 *  Visible if a default value set for this field. 
	 */
	JButton defaultButton;
	
	/**
	 * A label used to display the description of the field
	 * The visibility of this can be toggled
	 */
	JLabel descriptionLabel;
	
	
	Icon infoIcon;
	Icon wwwIcon;
	
	
	Cursor handCursor;
	
	
	Icon lockedTemplateIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_ICON);
	Icon lockedAllIcon = ImageFactory.getInstance().getIcon(ImageFactory.LOCKED_RED_ICON);
	

	Icon requiredIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_ASTERISK_ICON);
	Icon requiredWarningIcon = ImageFactory.getInstance().getIcon(ImageFactory.RED_ASTERISK_WARNING_ICON);
	

	/**
	 * A flag to indicate if this field is highlighted. 
	 */
	boolean highlighted = false;
	
	/**
	 * The background colour of this field.
	 */
	Color paintedColour = null;
	
	/**
	 * A default colour for the background of this field. Matches the 
	 * colour of the default border. 
	 */
	public static final Color DEFAULT_BACKGROUND = new Color(237, 239, 246);
	
	/**
	 * A border created from images, with drop shadow and rounded corners.
	 * This is the border for the FormField panel.
	 */
	Border imageBorder;
	
	/**
	 * An identical border to the image border, except that the colour of 
	 * the inside of the border matches the blue highlight colour.
	 */
	Border imageBorderHighlight;
	
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param field		The source of data for this display	
	 * @param tree		The JTree where this panel is displayed
	 * @param treeNode	The node that this panel represents
	 */
	public FieldPanel(IField field, JTree tree, DefaultMutableTreeNode treeNode) {
		
		this.dataField = field;
		this.tree = tree;
		this.treeNode = treeNode;
		
		
		/*
		 * Build borders and layout
		 */
		Border eb = BorderFactory.createEmptyBorder(2, 1, 2, 1);
		
		imageBorder = ImageBorder.getImageBorder();
		imageBorderHighlight = ImageBorder.getImageBorderHighLight();
		
		this.setBorder(null);
		this.setBackground(null);
		this.setLayout(new BorderLayout());
		
		
		/*
		 * A label to display the name of the field.
		 * This is the only component that is always visible (but could be "")
		 */
		nameLabel = new CustomLabel();
		nameLabel.setBackground(null);
		nameLabel.setOpaque(false);
		
		
		/*
		 * A description label displays description below the field. Visibility false unless 
		 * descriptionButton is clicked.
		 */
		descriptionLabel = new CustomLabel();
		descriptionLabel.setBackground(null);
		
		infoIcon = ImageFactory.getInstance().getIcon(ImageFactory.INFO_ICON);
		descriptionButton = new JButton(infoIcon);
		descriptionButton.setFocusable(false); // so it is not selected by tabbing
		descriptionButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				boolean visible = 
					"true".equals(dataField.getDisplayAttribute("descVisible"));
				visible = ! visible;
				dataField.setDisplayAttribute("descVisible", visible + "");
				refreshEditingOfPanel();
			}
		});
		descriptionButton.setBackground(null);
		descriptionButton.setBorder(eb);
		descriptionButton.setVisible(false);	// only made visible if description exists.
		setDescriptionText(); 	// will update description label
		
		
		/*
		 * A url-link button, that is only visible if a URL has been set.
		 */
		wwwIcon = ImageFactory.getInstance().getIcon(ImageFactory.WWW_ICON);
		urlButton = new JButton(wwwIcon);
		urlButton.setFocusable(false); // so it is not selected by tabbing
		urlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				BareBonesBrowserLaunch.openURL(
						dataField.getAttribute(DataFieldConstants.URL));
			}
		});
		urlButton.setBackground(null);
		handCursor = new Cursor(Cursor.HAND_CURSOR);
		urlButton.setCursor(handCursor);
		urlButton.setBorder(eb);
		urlButton.setVisible(false);	// only made visible if url exists.
		
		
		/*
		 * Default icon/button. Shows if a default is set. 
		 * Tool-tip shows what the default is. 
		 * Clicking the button loads defaults for this field,
		 * Disabled if field is fully locked.
		 */
		Icon defaultIcon = ImageFactory.getInstance().getIcon
			(ImageFactory.DEFAULT_ICON);
		defaultButton = new JButton(defaultIcon);
		defaultButton.setFocusable(false);
		defaultButton.setBackground(null);
		defaultButton.setBorder(eb);
		defaultButton.setVisible(false);	// only visible if default set. 
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadDefaultValue();
			}
		});
		
		/*
		 * Required field button. Doesn't do anything, just indicates that the field is required.
		 */
		requiredFieldButton = new JButton(requiredIcon);
		requiredFieldButton.setFocusable(false);
		requiredFieldButton.setBackground(null);
		requiredFieldButton.setBorder(eb);
		requiredFieldButton.setVisible(false);	// only visible if requiredField = true;
		
		
		/*
		 * Horizontal Box holds various buttons. More components can be 
		 * added later depending on the field type being displayed. 
		 */
		horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(descriptionButton);
		horizontalBox.add(urlButton);
		horizontalBox.add(defaultButton);
		horizontalBox.add(requiredFieldButton);
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

		

		/*
		 * Update components with the values from dataField
		 */
		setNameText(addHtmlTagsForNameLabel(
				dataField.getAttribute(Field.FIELD_NAME)));
		setDescriptionText();
		setURL(dataField.getAttribute(Field.FIELD_URL));
		
		refreshBackgroundColour();
		
		refreshDefaultValue();
		
		buildParamComponents();
	}
	
	/**
	 * Add additional UI components for editing the parameters of this field.
	 * Use a Factory to create the UI components, depending on the 
	 * type of each parameter. 
	 */
	public void buildParamComponents() {

		int paramCount = dataField.getParamCount();
		
		for (int i=0; i<paramCount; i++) {
			IParam param = dataField.getParamAt(i);
			JComponent edit = EditingComponentFactory.getEditingComponent(param);
			if (edit != null)
				addFieldComponent(edit);
		}
		
	}
	
	
	/**
	 * Used to add additional components to the field.
	 * Will be displayed horizontally. 
	 * Also adds this class as a property listener to these components. 
	 * 
	 * @param comp	The component to add.
	 */
	public void addFieldComponent(JComponent comp) {
		if (comp instanceof TableEditor) {
			/*
	         * Want to add the table to the SOUTH of contentsPanel (where descriptionLabel is). 
	         * Create new panel to hold both. 
	         */
	        JPanel tableContainer = new JPanel(new BorderLayout());
	        tableContainer.setBackground(null);
	        tableContainer.add(descriptionLabel, BorderLayout.NORTH);
	        tableContainer.add(comp, BorderLayout.SOUTH);
	        
			contentsPanel.add(tableContainer, BorderLayout.SOUTH);
		}
		else 
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
	public void refreshDefaultValue() {
		
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		boolean defaultExists = ((defaultValue != null) && 
					(defaultValue.length()>0));
		
		defaultButton.setVisible(defaultExists);
		
		if (defaultExists)
			defaultButton.setToolTipText("Default: " + defaultValue);
	
		else 
			defaultButton.setToolTipText(null);
	}
	
	
	
	// these methods called when user updates the fieldEditor panel
	public void setNameText(String name) {
		nameLabel.setText(name);
		
		String lockedLevel = dataField.getAttribute(DataFieldConstants.LOCK_LEVEL);
		if (lockedLevel == null) {
			nameLabel.setIcon(null);
			nameLabel.setToolTipText(null);
		} else {
			String toolTipText = "";
			Icon newIcon = lockedTemplateIcon;
			
			if (lockedLevel.equals(DataFieldConstants.LOCKED_TEMPLATE)) {
				toolTipText += "Template Locked";
			} else if (lockedLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES)) {
				toolTipText += "Field Locked";
				newIcon = lockedAllIcon;
			}
			
			String user = dataField.getAttribute(DataFieldConstants.LOCKED_FIELD_USER_NAME);
			if (user != null)
			toolTipText = toolTipText + " by " + user;
			
			String lockedTimeUTC = dataField.getAttribute(DataFieldConstants.LOCKED_FIELD_UTC);
			if (lockedTimeUTC != null) {
				Calendar lockedTime = new GregorianCalendar();
				lockedTime.setTimeInMillis(new Long(lockedTimeUTC));
				SimpleDateFormat time = new SimpleDateFormat("HH:mm 'on' EEE, MMM d, yyyy");
				toolTipText = toolTipText + " at " + time.format(lockedTime.getTime());
			}
			
			nameLabel.setToolTipText(toolTipText);
			nameLabel.setIcon(newIcon);
		}
	}
	

	/**
	 * Called when the UI is built. Sets the visibility, text etc of 
	 * the description box. 
	 */
	private void setDescriptionText() {
		String description = dataField.getAttribute(
				Field.FIELD_DESCRIPTION);
		boolean showDescription = 
			"true".equals(dataField.getDisplayAttribute("descVisible"));
		
		if ((description != null) && (description.trim().length() > 0)) {
			String htmlDescription = 
				"<html><div style='width:250px; padding-left:30px;'>" + 
				description + 
				"</div></html>";
			descriptionButton.setToolTipText(htmlDescription);
			descriptionButton.setVisible(true);
			descriptionLabel.setVisible(showDescription);
			descriptionLabel.setFont(XMLView.FONT_TINY);
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
	 * Called while building UI. Sets the visibility and tool tip text 
	 * for the URL button. 
	 * 
	 * @param url
	 */
	public void setURL(String url) {
		if (url == null) {
			urlButton.setVisible(false);
			return;
		}
		if (url.length() > 0) {
			urlButton.setVisible(true);
			urlButton.setToolTipText(url);
		} else {
			urlButton.setVisible(false);
		}
	}
	
	
	/**
	 * This causes the field to be Selected, with a blue background etc. 
	 * 
	 * @param selected		If true, field is coloured blue. 
	 */
	public void setSelected(boolean selected) {
		// System.out.println("FormField setSelected() " + nameLabel.getText() + " " + selected);
		highlighted = selected;
		refreshHighlighted();
	}
	private void refreshHighlighted() {
		if (highlighted) { 
			contentsPanel.setBackground(paintedColour != null ? paintedColour : 
				XMLView.BLUE_HIGHLIGHT);
			contentsPanel.setBorder(imageBorderHighlight);
		}
		else {
			contentsPanel.setBackground((paintedColour == null) ? DEFAULT_BACKGROUND : paintedColour);
			contentsPanel.setBorder(imageBorder);
		}
	}
	
	/**
	 * This method takes the value in the DataFieldConstants.DEFAULT attribute,
	 * and copies it into the attribute specified by 
	 * EditCopyDefaultValues.getValueAttributeForLoadingDefault(dataField)
	 */
	public void loadDefaultValue() {
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
	 * Allows the JTree to be set after this class is instantiated. 
	 * @param tree
	 */
	public void setTree(JTree tree) {
		this.tree = tree;
	}
	
	public void setTreeNode (DefaultMutableTreeNode node) {
		treeNode = node;
	}
	
	
	private void refreshBackgroundColour() {
		paintedColour = getColorFromString(dataField.getAttribute(DataFieldConstants.BACKGROUND_COLOUR));
		
		refreshHighlighted();
	}
	
	// used to convert a stored string Colour attribute to a Color
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

	
	private String addHtmlTagsForNameLabel(String text) {
		text = "<html>" + text + "</html>";
		return text;
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
				IParam param = src.getParameter();
				String attrName = src.getAttributeName();
				String displayName = src.getEditDisplayName();
				
				String newValue;
				Object newVal = evt.getNewValue();
				if ((newVal instanceof String) || (newVal == null)){
					newValue = (newVal == null ? null : newVal.toString());
				 	controller.editAttribute(param, attrName, newValue, 
				 			displayName, tree, treeNode);
				}
				
				//System.out.println("FieldPanel " + attrName + " " + newValue);
				
				else if (newVal instanceof HashMap) {
					HashMap newVals = (HashMap)newVal;
					controller.editAttributes(param, "edit", newVals, 
							tree, treeNode);
				}
				
			}
		}
	}

	public void setController(TreeEditorControl controller) {
		this.controller = controller;
	}
	
	/**
	 * This method is used to refresh the size of this panel in the JTree.
	 * It must also remain in the editing mode, otherwise the user who
	 * is currently editing it will be required to click again to 
	 * continue editing.
	 * This can be achieved by calling startEditingAtPath(tree, path)
	 */
	public void refreshEditingOfPanel() {
		if ((tree != null) && (treeNode !=null)) {
			
			TreePath path = new TreePath(treeNode.getPath());
			
			tree.getUI().startEditingAtPath(tree, path);
		}
	}
	
	/**
	 * This method is used to refresh the size of this panel in the JTree,
	 * without selecting this path for editing.
	 */
	public void refreshPanel() {
		if ((tree != null) && (treeNode !=null)) {
			
			TreePath path = new TreePath(treeNode.getPath());
			
			DefaultTreeModel mod = (DefaultTreeModel)tree.getModel();
			
			System.out.println("FieldPanel refreshPanel");
			mod.nodeChanged(treeNode);
		}
	}
	

}
