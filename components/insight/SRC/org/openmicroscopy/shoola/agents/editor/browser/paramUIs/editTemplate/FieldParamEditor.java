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
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
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
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	Scrollable,
	ActionListener
{
	
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
	 * A panel that is displayed at the top IF the id > 0
	 */
	private JPanel 						uiDisplayPanel;
	
	/**
	 * A label to display the ID.
	 */
	private JLabel						uiLabel;

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
		Border lineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0,
                 UIUtilities.LIGHT_GREY);
		Border compoundBorder = BorderFactory.createCompoundBorder
			(lineBorder, emptyBorder);
		attributeFieldsPanel.setBorder(compoundBorder);
		attributeFieldsPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		
		// Panel to display ID. Not visisible unless ID is set (>0)
		uiDisplayPanel = new JPanel(new BorderLayout());
		uiDisplayPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		Border b = BorderFactory.createCompoundBorder(lineBorder, 
				new EmptyBorder(0,0,5,5));
		uiDisplayPanel.setBorder(b);
		uiLabel = new CustomLabel();
		uiLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
		uiDisplayPanel.add(uiLabel, BorderLayout.WEST);
		uiDisplayPanel.setVisible(false);
	}
	
	/**
	 * Builds the UI. 
	 */
	private void buildPanel() {
		
		// add ID display at top. 
		attributeFieldsPanel.add(uiDisplayPanel);
		
		String defaultName = TreeModelMethods.getNodeName(treeNode);
		// Name: Label and text box
		AttributeEditLine nameEditor = new AttributeEditNoLabel
			(field, Field.FIELD_NAME, defaultName);
		nameEditor.addPropertyChangeListener
				(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
		nameEditor.setFontSize(14);
		
		// holds the name-editor and any additional buttons
		Box nameContainer = Box.createHorizontalBox();
	
		
		// show whether this step is a "SPLIT_STEP" (can't edit yet)
		String stepType = field.getAttribute(Field.STEP_TYPE);
		if (CPEimport.SPLIT_STEP.equals(stepType)) {
			Icon split = IconManager.getInstance().getIcon
												(IconManager.SPLIT_ICON_12);
			JButton splitButton = new CustomButton(split);
			splitButton.setToolTipText("This step is a 'Split Step'");
			nameContainer.add(splitButton);
		}
		
		nameContainer.add(nameEditor);
		attributeFieldsPanel.add(nameContainer);
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		
		
		// add parameters
		addParameters();
		// add notes 
		addStepNotes();
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
		// (shouldn't be any anyway). 
		if (field instanceof TextBoxStep)	return;
		
		attributeFieldsPanel.add(createParamsHeader());
		int paramCount = field.getContentCount();
		
		Box paramsContainer = Box.createVerticalBox();
		paramsContainer.setBorder(new EmptyBorder(0,10,0,0)); // left indent
		attributeFieldsPanel.add(paramsContainer);
		
		for (int i=0; i<paramCount; i++) {
			IFieldContent content = field.getContentAt(i); 
			if (content instanceof IParam) {
				IParam param = (IParam)content;
				
				addParam(param, paramsContainer);
			}
		}
	}
	
	private void addStepNotes()
	{
		int noteCount = field.getNoteCount();
		
		Box  container = Box.createVerticalBox();
		container.setBorder(new EmptyBorder(0,10,0,0)); // left indent
		attributeFieldsPanel.add(container);
		
		Note note;
		NoteEditor noteEditor;
		for (int i=0; i<noteCount; i++) {
			note = field.getNoteAt(i);
			noteEditor = new NoteEditor(note, this);
			
			container.add(Box.createVerticalStrut(10));
			container.add(noteEditor);
		}
	}
	
	/**
	 * Add additional UI components for editing the data references of this field.
	 */
	protected void addDataRefs() 
	{
		// Don't display data-refs if the field is a TextBoxStep. 
		// (shouldn't be any anyway). 
		if (field instanceof TextBoxStep)	return;
		
		int dataRefCount = field.getDataRefCount();
		if (dataRefCount == 0)	return;	// don't add header if no data-refs
		
		JPanel dataRefHeader = new JPanel(new BorderLayout());
		dataRefHeader.setBackground(null);
		dataRefHeader.add(new CustomLabel("Data References:", 12), BorderLayout.WEST);
		attributeFieldsPanel.add(Box.createVerticalStrut(10));
		attributeFieldsPanel.add(dataRefHeader);
			
		DataRefEditor drEditor;
		DataReference dataRef;
		for (int i=0; i<dataRefCount; i++) {
			dataRef = field.getDataRefAt(i);
			drEditor = new DataRefEditor(dataRef, this);
			drEditor.addPropertyChangeListener(this);
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
	private void addParam(IParam param, JComponent container) 
	{
		ParamEditor pe = new ParamEditor(param, this, controller);
		
		pe.addPropertyChangeListener(this);
		container.add(Box.createVerticalStrut(20));
		//attributeFieldsPanel.add(new JSeparator());
		//container.add(Box.createVerticalStrut(10));
		container.add(pe);
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
		
		JToolBar paramToolBar = new JToolBar();
		paramToolBar.setFloatable(false);
		paramToolBar.setBackground(null);
		
		Icon addNotes = IconManager.getInstance().getIcon
											(IconManager.ADD_STEP_NOTE_ICON);
		JButton addStepNote = new CustomButton(addNotes);
		boolean exp = controller.isModelExperiment();
		addStepNote.setVisible(exp);
		addStepNote.setToolTipText("Add a note to this step");
		addStepNote.addActionListener(this);
		paramToolBar.add(addStepNote);
		
		JButton addParamsButton = new AddParamActions();
		addParamsButton.setToolTipText("Add Parameter to END of Step");
		// don't allow addition of parameters or notes to root. 
		if(treeNode.isRoot()) {
			addParamsButton.setEnabled(false);
			addStepNote.setEnabled(false);
			paramToolBar.setVisible(false);
		}
		paramToolBar.add(addParamsButton);
		
		addParamsButton.addPropertyChangeListener(
				AddParamActions.PARAM_ADDED_PROPERTY, this);
		addParamsHeader.add(paramToolBar, BorderLayout.EAST);
		
		addParamsHeader.add(
				new CustomLabel("Parameters:", 12), BorderLayout.WEST);
		
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
	
	/**
	 * Sets the ID to display. If ID = 0, nothing is displayed. 
	 * 
	 * @param id
	 */
	public void setId(long id) 
	{
		if (id == 0) {
			// hide panel if not ID to display
			uiDisplayPanel.setVisible(false);
			
		} else {
			// show panel and set text
			uiDisplayPanel.setVisible(true);
			uiLabel.setText("File ID: " + id);
		}
		invalidate();
		repaint();
	}
	
	/**
	 * Notifies the Tree Model that the node displayed by this class has 
	 * been Changed. This causes the UI to refresh, and builds a new instance
	 * of this class. 
	 */
	public void rebuildEditorPanel() {

		if ((tree != null) && (treeNode != null)) {
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
			treeModel.nodeChanged(treeNode);
		}
	}
	
	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface
	 * Handles changing of parameter types, parameter values, adding and 
	 * deleting parameters and deleting notes or data-references. 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		
		String propName = evt.getPropertyName();
		
		// handles changing of the parameter type
		if (ParamEditor.PARAM_TYPE.equals(propName)) { 
			if (evt.getSource() instanceof ITreeEditComp) {
				// Source wil
				IAttributes op = ((ITreeEditComp)evt.getSource()).getParameter();
				IParam oldParam = (IParam)op;
				IParam newParam = null;
				String newType = evt.getNewValue().toString();
				if (newType != null) 
				newParam = FieldParamsFactory.getFieldParam(newType);
				          
				// if newParam is null, this will simply remove the parameter
				controller.changeParam(newParam, oldParam, field, 
								tree, treeNode);
			}
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
					HashMap<String,String> newVals = (HashMap)newVal;
					 controller.editAttributes(param, displayName, newVals, 
							tree, treeNode);
				}
				
				updateEditingOfTreeNode();
				
			}
		} else if (AddParamActions.PARAM_ADDED_PROPERTY.equals(propName)) {
			// get the type of new param to add...
			String paramType = evt.getNewValue().toString();
			if (AddParamActions.ADD_DATA_REF.equals(paramType)) {
				controller.addDataRefToField(field, tree, treeNode);
			} else {
				controller.addParamToField(field, paramType, tree, treeNode);
			}
			
		} else if (NoteEditor.NOTE_DELETED.equals(propName)) {
			NoteEditor ne = (NoteEditor)evt.getSource();
			Note note = ne.getNote();
			controller.deleteStepNote(field, tree, treeNode, note);
			rebuildEditorPanel();
			
		} else if (DataRefEditor.DATA_REF_DELETED.equals(propName)) {
			ITreeEditComp dre = (ITreeEditComp)evt.getSource();
			DataReference dataRef = (DataReference)dre.getParameter();
			controller.addDataRefToField(field, dataRef, tree, treeNode);
		}
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

	public void actionPerformed(ActionEvent e) {
		controller.addStepNote(field, tree, treeNode);
	}
}