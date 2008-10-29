 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.FieldParamEditor 
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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.Scrollable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/** 
 * This is the UI Panel that is displayed on the right of the screen, for
 * editing the parameters of the currently selected field. 
 * Similar to {@link FieldContentEditor} except this class does not display
 * the text content of the field (descriptions); it only shows the parameters. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldParamEditor
	extends JPanel 
	implements PropertyChangeListener,
	Scrollable
{
	/**
	 * A bound property of this panel. 
	 * Changes in this property indicate that the panel needs to be rebuilt
	 * from the data model. 
	 */
	public static final String PANEL_CHANGED_PROPERTY = "panelChanged";
	
	/**
	 * The field that this UI component edits.
	 */
	private IField 				field;
	
	/**
	 * The controller for managing undo/redo. Eg manages attribute editing...
	 */
	private BrowserControl 		controller;
	
	/**
	 * The JTree that this field is displayed in. 
	 * Used eg. to notify that this field has been edited (needs refreshing)
	 */
	private JTree 				tree;
	
	/**
	 * A reference to the node represented by this field. 
	 * Used eg. to set the selected field to this node with undo/redo
	 */
	private DefaultMutableTreeNode treeNode;

	/**
	 * Vertical Box layout panel. Main panel.
	 */
	private JPanel 				attributeFieldsPanel;

	/**
	 * Initialises the UI components
	 */
	private void initialise() {
		
		// Panel to hold all components, vertically 
		attributeFieldsPanel = new JPanel();
		attributeFieldsPanel.setLayout(new BoxLayout
				(attributeFieldsPanel, BoxLayout.Y_AXIS));
		// set border and background
		Border emptyBorder = new EmptyBorder(10, 5, 15,5);
		Border lineBorder = BorderFactory.createMatteBorder(
                0, 0, 1, 0, UIUtilities.LIGHT_GREY);
		Border compoundBorder = BorderFactory.createCompoundBorder
			(lineBorder, emptyBorder);
		attributeFieldsPanel.setBorder(compoundBorder);
		attributeFieldsPanel.setBackground(null);
		
	}
	
	/**
	 * Builds the UI. 
	 */
	private void buildPanel() {
		
		// Name: Label and text box
		AttributeEditLine nameEditor = new AttributeEditNoLabel
			(field, Field.FIELD_NAME, "Field Name");
		nameEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		attributeFieldsPanel.add(nameEditor);
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		
		// Parameters: Label and "Add" button
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		//JLabel paramLabel = new CustomLabel("Parameters:");
		
		// For each parameter of this field, add the components for
		// editing their default or template values. 
		addFieldContents();
		
		this.setLayout(new BorderLayout());
		add(attributeFieldsPanel, BorderLayout.NORTH);
		setBackground(null);

		this.validate();
	}

	/**
	 * Each parameter editing component is added here.
	 * This class becomes a property change listener for each one.
	 * 
	 * @param defaultEdit	A component for editing the defaults of each param
	 */
	private void addFieldComponent(IParam param) 
	{
		JComponent defaultEdit = ParamTemplateUIFactory.
			getEditDefaultComponent(param);
		
		if (defaultEdit == null) return;
		
		attributeFieldsPanel.add(Box.createVerticalStrut(5));
		attributeFieldsPanel.add(new JSeparator());
		attributeFieldsPanel.add(Box.createVerticalStrut(3));
		// add name field
		AttributeEditLine nameEditor = new AttributeEditNoLabel
			(param, AbstractParam.PARAM_NAME, "Parameter Name");
		nameEditor.addPropertyChangeListener
			(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		attributeFieldsPanel.add(nameEditor);
	
		attributeFieldsPanel.add(defaultEdit);
		defaultEdit.addPropertyChangeListener( 
				ITreeEditComp.VALUE_CHANGED_PROPERTY, 
				this);
	}

	/**
	 * Add additional UI components for editing the value of this field.
	 * This deals with "Additional" parameters, not the first one, which
	 * is a special case (added earlier). 
	 * Uses the {@link ParamTemplateUIFactory} to create the UI components,
	 * depending on the value type
	 */
	private void addFieldContents() 
	{
		attributeFieldsPanel.add(createParamsHeader());
		
		int paramCount = field.getContentCount();
		if (paramCount < 2) { return; }
		
		for (int i=0; i<paramCount; i++) {
			IFieldContent content = field.getContentAt(i); 
			if (content instanceof IParam) {
				IParam param = (IParam)content;
				
				addFieldComponent(param);
			}
		}
	}
	
	/**
	 * Builds and returns the header for the list of parameters. 
	 * Has a label, and a button for adding parameters. 
	 * 
	 * @return see above. 
	 */
	private JComponent createParamsHeader() {
		JPanel addParamsHeader = new JPanel(new BorderLayout());
		addParamsHeader.setBackground(null);
		JButton addParamsButton = new AddParamActions(field, tree, 
				treeNode, controller).getButton();
		addParamsButton.addPropertyChangeListener(
				AddParamActions.PARAM_ADDED_PROPERTY, this);
		addParamsHeader.add(addParamsButton, BorderLayout.EAST);
		
		addParamsHeader.add(
				new CustomLabel("Parameters:"), BorderLayout.WEST);
		
		return addParamsHeader;
	}

	/**
	 * Changes the Parameter type of the first parameter of this field.
	 * In future, it may be preferable to allow users to change the type
	 * of other parameters of this field, depending on selection etc. 
	 * 
	 * @param newType	A String that defines the type of parameter selected
	 */
	private void paramTypeChanged(String newType) {
		
		int paramIndex = 0;
		
		IParam newParam = null;
		if (newType != null) 
			newParam = FieldParamsFactory.getFieldParam(newType);
		
		// if newParam is null, this will simply remove first parameter
		controller.changeParam(field, newParam, paramIndex,
				 tree, treeNode);
	}

	/**
	 * Creates an instance of this class for editing the field.
	 * 
	 * @param field		The Field to edit
	 * @param tree		The JTree in which the field is displayed
	 * @param treeNode	The node of the Tree which contains the field
	 */
	public FieldParamEditor(IField field, JTree tree, 
			DefaultMutableTreeNode treeNode, BrowserControl controller) 
	{
		this.field = field;
		
		this.tree = tree;
		this.treeNode = treeNode;
		this.controller = controller;
		
		initialise();
		buildPanel();
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
					HashMap<String,String> newVals = (HashMap)newVal;
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

		if ((tree != null) && (treeNode != null)) {
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
			treeModel.nodeChanged(treeNode);
		}

		/*validate();
		repaint();
		this.firePropertyChange(PANEL_CHANGED_PROPERTY, null, "refresh");
		*/
	}
	
	
	
	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns {@link #getPreferredSize()}
	 * 
	 * @see Scrollable#getPreferredScrollableViewportSize();
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns 1
	 * 
	 * @see Scrollable#getScrollableBlockIncrement(Rectangle, int, int)
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 1;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns false, since this panel does not fill the entire 
	 * height of the scroll pane.
	 * 
	 * @see Scrollable#getPreferredScrollableViewportSize();
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * Returns true, so that this panel fills the width of the scroll pane.
	 * 
	 * @see Scrollable#getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	/**
	 * Implemented as specified by the {@link Scrollable} interface.
	 * 
	 * @see Scrollable#getScrollableUnitIncrement(Rectangle, int, int)
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 1;
	}
}