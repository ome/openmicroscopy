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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserControl;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.DataRefEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.model.CPEimport;
import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.model.TextBoxStep;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/** 
 * This is the UI Panel that is displayed on the right of the screen, for
 * editing the parameters of the currently selected field. 
 * Similar to {@link FieldContentEditor} except this class does not display
 * the text content of the field (descriptions); it only shows the parameters.
 * 
 * Each parameter is edited via a {@link ParamEditor}. This class is a
 * {@link PropertyChangeListener}, listens for changes to {@link ParamEditor}s, 
 * and manages the posting of parameter edits to the {@link BrowserControl}.
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
	protected JPanel 			attributeFieldsPanel;

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
			(field, Field.FIELD_NAME, "Step Name");
		nameEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		
		// holds the name-editor and any additional buttons
		Box nameContainer = Box.createHorizontalBox();
		
		// indicate whether step has notes (show with toolTip)
		// Can't currently edit these notes, 
		// but at least Editor supports cmp.xml!
		int noteCount = field.getNoteCount();
		if (noteCount > 0) {
			String notesToolTip = "<html><div style='width:250px; " +
			"padding:1px'>" + "Step Notes:<br>";
			
			Note note;
			String name, content;
			for (int i = 0; i < noteCount; i++) {
				note = field.getNoteAt(i);
				name = note.getName();
				content = note.getContent();
				notesToolTip = notesToolTip + "<div style='padding:4px'><b>" +
					name + ":</b><br>" + content + "</div>";
			}
			notesToolTip = notesToolTip + "</div></html>";
			
			Icon notes = IconManager.getInstance().getIcon
											(IconManager.INFO_12_ICON);
			JButton notesButton = new CustomButton(notes);
			notesButton.setToolTipText(notesToolTip);
			nameContainer.add(notesButton);
		}
		
		// show whether this step is a "SPLIT_STEP" (can't edit yet)
		String stepType = field.getAttribute(Field.STEP_TYPE);
		if (CPEimport.SPLIT_STEP.equals(stepType)) {
			Icon split = IconManager.getInstance().getIcon
												(IconManager.SPLIT_12_ICON);
			JButton splitButton = new CustomButton(split);
			splitButton.setToolTipText("This step is a 'Split Step'");
			nameContainer.add(splitButton);
		}
		
		// if root node, and contains 'experiment-info' display via tool tip
		// Can't edit this, but it is displayed to support cpe.xml 
		if (treeNode.isRoot()) {
			String expDate = field.getAttribute(CPEimport.EXP_DATE);
			String investigName = field.getAttribute(CPEimport.INVESTIG_NAME);
			if (expDate != null || investigName != null) {
				Icon e = IconManager.getInstance().getIcon
												(IconManager.EXP_9_11_ICON);
				String date = "no date";
				
				SimpleDateFormat f = new SimpleDateFormat("d MMM, yyyy");
				try {
					long millis = new Long(expDate);
					date = f.format(new Date(millis));
				} catch (NumberFormatException ex) {}
				
				String expToolTip = "<html><div style='width:250px; " +
				"padding:1px'>" + "Experiment Information: " +
						"<div style='padding:4px'>"
				 + date + "<br>Investigator: " + investigName + 
				 "</div></div></html>";
				
				JButton expButton = new CustomButton(e);
				expButton.setToolTipText(expToolTip);
				nameContainer.add(expButton);
			}
		}
		
		nameContainer.add(nameEditor);
		attributeFieldsPanel.add(nameContainer);
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		
		// Parameters: Label and "Add" button
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		//JLabel paramLabel = new CustomLabel("Parameters:");
		
		// For each parameter of this field, add the components for
		// editing their default or template values. 
		addParameters();
		// add data refs
		addDataRefs();
		
		this.setLayout(new BorderLayout());
		add(attributeFieldsPanel, BorderLayout.NORTH);
		setBackground(null);

		this.validate();
	}

	/**
	 * Add additional UI components for editing the parameters of this field.
	 */
	protected void addParameters() 
	{	
		// Don't display parameters if the field is a TextBoxStep. 
		if (field instanceof TextBoxStep) {
			return;
		}
		
		attributeFieldsPanel.add(createParamsHeader());
		int paramCount = field.getContentCount();
		
		for (int i=0; i<paramCount; i++) {
			IFieldContent content = field.getContentAt(i); 
			if (content instanceof IParam) {
				IParam param = (IParam)content;
				
				addParam(param);
			}
		}
	}
	
	/**
	 * Add additional UI components for editing the data references of this field.
	 */
	protected void addDataRefs() 
	{
		JPanel dataRefHeader = new JPanel(new BorderLayout());
		dataRefHeader.setBackground(null);
		dataRefHeader.add(new CustomLabel("Data Links:"), BorderLayout.WEST);
		attributeFieldsPanel.add(dataRefHeader);
		
		int dataRefCount = field.getDataRefCount();
			
		DataRefEditor drEditor;
		DataReference dataRef;
		for (int i=0; i<dataRefCount; i++) {
			dataRef = field.getDataRefAt(i);
			drEditor = new DataRefEditor(dataRef, this);
			drEditor.addPropertyChangeListener(
					ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
			attributeFieldsPanel.add(Box.createVerticalStrut(10));
			attributeFieldsPanel.add(drEditor);
		}
	}

	/**
	 * Each parameter editing component is added here.
	 * An appropriate UI component is created for each type of Parameter. 
	 * This class becomes a property change listener for each one.
	 * 
	 * @param param 	The parameter to add. 
	 */
	private void addParam(IParam param) 
	{
		ParamEditor pe = new ParamEditor(param, this);
		
		pe.addPropertyChangeListener(ParamEditor.PARAM_TYPE, this);
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		attributeFieldsPanel.add(new JSeparator());
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		attributeFieldsPanel.add(pe);
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
		
		// don't allow addition of parameters to root. 
		if(treeNode.isRoot()) {
			addParamsButton.setEnabled(false);
		}
		
		addParamsButton.addPropertyChangeListener(
				AddParamActions.PARAM_ADDED_PROPERTY, this);
		addParamsHeader.add(addParamsButton, BorderLayout.EAST);
		
		addParamsHeader.add(
				new CustomLabel("Parameters:"), BorderLayout.WEST);
		
		return addParamsHeader;
	}

	/**
	 * Creates an instance of this class for editing the field.
	 * 
	 * @param field		The Field to edit
	 * @param tree		The JTree in which the field is displayed
	 * @param treeNode	The node of the Tree which contains the field
	 * @param controller	The BrowserControl for handling edits 
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
		
		if (ParamEditor.PARAM_TYPE.equals(propName)) { 
			if (evt.getSource() instanceof ParamEditor) {
				
				IParam oldParam = ((ParamEditor)evt.getSource()).getParameter();
				IParam newParam = null;
				String newType = evt.getNewValue().toString();
				if (newType != null) 
				newParam = FieldParamsFactory.getFieldParam(newType);
				          
				// if newParam is null, this will simply remove first parameter
				controller.changeParam(newParam, oldParam, field, 
								tree, treeNode);
			}
		}
				
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