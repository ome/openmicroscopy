 /*
 * org.openmicroscopy.shoola.agents.editor.browser.ExperimentInfoPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.jdesktop.swingx.JXDatePicker;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextFieldEditor;
import org.openmicroscopy.shoola.agents.editor.model.CPEimport;
import org.openmicroscopy.shoola.agents.editor.model.ExperimentInfo;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.ProtocolRootField;
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomFont;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * A Panel to display experimental info (IF we're editing an experiment)
 * OR Protocol info (if we're editing a protocol!). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ExperimentInfoPanel 
	extends JPanel
	implements TreeModelListener,
	ActionListener, 
	TreeSelectionListener,
	PropertyChangeListener {
	
	/**
	 * A reference to the tree UI used for selection of root.
	 */
	private JTree 				navTree;
	
	/**
	 * Controller for edits etc.
	 */
	private BrowserControl 		controller;
	
	/**
	 * The tree Model. Get the root for experimental info, and listen for
	 * changes to update 'unfilled' fields. 
	 */
	private TreeModel			treeModel;
	
	/** The root node of the Tree Model, contains the field with exp info */
	TreeNode 					root;
	
	/** The Experiment Info object. Holds exp info. */
	IAttributes					field;
	
	
	private JPanel 				expInfoPanel;
	
	private JPanel 				protocolInfoPanel;
	
	/** A count of the number of unfilled parameters */
	private int 				unfilledParams;
	
	/** A count of the number of unfilled 'Required' parameters */
	private int 				unfilledReqParams;
	
	/** Label to display number of unfilled parameters in the experiment */
	private JLabel 				unfilledParamsLabel;
	
	/** Label to display number of unfilled steps in the experiment */
	private JLabel 				unfilledStepsLabel;
	
	/** The text field used for editing the investigator's name */
	private JTextField 			nameField;
	
	/** Container for name label and nameEditor */
	Box 						nameBox;
	
	/** A Date-picker to display and pick date of experiment. */
	private JXDatePicker 		datePicker;
	
	/** Label to display the last modified timestamp of the file. */
	private JLabel				lastModifiedLabel;
	
	/** The UI for editing file-locked vv edit-protocol vv edit experiment */
	private	EditingModeUI 		editingMode;
	
	/** Allows setting of the file-locked state */
	private	JCheckBox 			fileLockedCheckBox;
	
	/**
	 *  A list of the unfilled steps in the experiment. Allows user to 
	 * search through the experiment. 
	 */
	private List<TreePath>		unfilledSteps;
	
	/** The currently selected step of the unfilled steps list */
	private int					currentStepIndex;
	
	/** Button for moving to the next unfilled step */
	private JButton				nextStep;
	
	/** Button for moving to the previous unfilled step */
	private JButton				prevStep;
	
	/** Button for moving to the first unfilled step */
	private JButton				goToFirstStep;
	
	/** Action command for the Add Experiment Info button */
	public static final String	ADD_EXP_INFO = "addExpInfo";
	
	/** Action command for the Next Step button */
	public static final String	NEXT_STEP = "nextStep";
	
	/** Action command for the Previous Step button */
	public static final String	PREV_STEP = "prevStep";
	
	/** Action command for the First-Step button */
	public static final String	FIRST_STEP = "firstStep";
	
	/** Action command for the Delete Experiment-Info button */
	public static final String	DELETE_INFO = "deleteInfo";
	
	/**  A nice yellow colour (like a post-it note).  */
	public static final Color 	LIGHT_YELLOW = new Color(254,244,156);
	
	/**
	 * Initialises the various UI components
	 */
	private void initialise() 
	{
		// the main panel for showing all experimental info. 
		expInfoPanel = new JPanel();
		// panel for showing messages when Protocol
		protocolInfoPanel = new JPanel();
		
		datePicker = UIUtilities.createDatePicker();
		datePicker.setFont(new CustomFont());
		datePicker.addActionListener(this);
		
		unfilledParamsLabel = new CustomLabel();
		unfilledStepsLabel = new CustomLabel();
		unfilledStepsLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		unfilledSteps = new ArrayList<TreePath>();
		
		IconManager iM = IconManager.getInstance();
		Icon rightIcon = iM.getIcon(IconManager.ARROW_RIGHT_ICON_12);
		Icon leftIcon = iM.getIcon(IconManager.ARROW_LEFT_ICON_12);
		Icon goIcon = iM.getIcon(IconManager.GO_ICON_12_20);
		
		goToFirstStep = new CustomButton(goIcon);
		goToFirstStep.setActionCommand(FIRST_STEP);
		goToFirstStep.addActionListener(this);
		goToFirstStep.setFocusable(false);		
		goToFirstStep.setToolTipText("Go to the first un-filled step");
		goToFirstStep.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		nextStep = new CustomButton(rightIcon);
		nextStep.setActionCommand(NEXT_STEP);
		nextStep.addActionListener(this);
		nextStep.setFocusable(false);  // long focus-bug-fix story! 
		nextStep.setToolTipText("Go to the next un-filled step");
		nextStep.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		prevStep = new CustomButton(leftIcon);
		prevStep.setActionCommand(PREV_STEP);
		prevStep.addActionListener(this);
		prevStep.setFocusable(false);
		prevStep.setToolTipText("Go to the previous un-filled step");
		prevStep.setAlignmentY(Component.CENTER_ALIGNMENT);
	}

	/**
	 * Builds the UI. 
	 */
	private void buildUI() 
	{
		// only set to true if we have experimental info to display
		expInfoPanel.setVisible(false);
		
		setLayout(new BorderLayout());
		
		expInfoPanel.setLayout(new BorderLayout());
		expInfoPanel.setBackground(LIGHT_YELLOW);
		Border lineBorder = BorderFactory.createMatteBorder(1, 1, 0, 1,
	             UIUtilities.LIGHT_GREY.darker());
		expInfoPanel.setBorder(lineBorder);
		
		protocolInfoPanel.setLayout(new BorderLayout());
		protocolInfoPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		protocolInfoPanel.setBorder(lineBorder);
		
		Border eb = new EmptyBorder(5,5,5,5);
		
		// title header
		JLabel experiment = new CustomLabel("Experiment Info:");
		experiment.setBorder(eb);
		experiment.setFont(CustomFont.getFontBySize(14));
		
		IconManager iM = IconManager.getInstance();
		
		//  tool bar, only holds the delete button 
		// Delete functionality not needed now, but may need toolbar later...?
		/*
		JToolBar rightToolBar = new JToolBar();
		rightToolBar.setBackground(null);
		rightToolBar.setFloatable(false);
		Border bottomLeft = BorderFactory.createMatteBorder(0, 1, 1, 0,
                UIUtilities.LIGHT_GREY);
		rightToolBar.setBorder(bottomLeft);
		
		
		// Delete note button
		Icon delete = iM.getIcon(IconManager.DELETE_ICON_12);
		JButton deleteButton = new CustomButton(delete);
		deleteButton.setFocusable(false);
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand(DELETE_INFO);
		deleteButton.setToolTipText("<html>Remove Experiment Info:<br>This " +
				"will also remove any experiment notes from each step.</html>");
		rightToolBar.add(deleteButton);
		*/
		
		Box titleToolBar = Box.createHorizontalBox();
		experiment.setAlignmentY(Component.TOP_ALIGNMENT);
		titleToolBar.add(experiment);
		
		titleToolBar.add(Box.createHorizontalGlue());
		//rightToolBar.setAlignmentY(Component.TOP_ALIGNMENT);
		//titleToolBar.add(rightToolBar);
		
		expInfoPanel.add(titleToolBar, BorderLayout.NORTH);
		
		
		// left Panel
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(eb);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(null);
		
		// add labels to left
		nameBox = Box.createHorizontalBox();
		nameBox.add(new CustomLabel("Investigator: "));
		// nameBox will have the nameEditor added to it during refreshPanel()
		// Can't add it now because we don't have the root field required. 
		nameBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(nameBox);
		Box dateBox = Box.createHorizontalBox();
		JLabel dateLabel = new CustomLabel("Experiment Date: ");
		dateBox.add(dateLabel);
		dateBox.add(datePicker);
		dateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(dateBox);
		
		// label for last-modified timestamp
		lastModifiedLabel = new CustomLabel();
		lastModifiedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(lastModifiedLabel);
		
		
		// right Panel
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(eb);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(null);
		
		// buttons for finding unfilled steps
		Box	stepButtonsBox = Box.createHorizontalBox();
		stepButtonsBox.add(unfilledStepsLabel);
		stepButtonsBox.add(prevStep);
		stepButtonsBox.add(goToFirstStep);
		stepButtonsBox.add(nextStep);
		stepButtonsBox.add(Box.createHorizontalGlue());
		stepButtonsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		unfilledParamsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rightPanel.add(unfilledParamsLabel);
		rightPanel.add(stepButtonsBox);
		
		editingMode = new EditingModeUI(controller);
		editingMode.setAlignmentX(Component.LEFT_ALIGNMENT);
		rightPanel.add(editingMode);
		
		expInfoPanel.add(leftPanel, BorderLayout.WEST);
		expInfoPanel.add(rightPanel, BorderLayout.EAST);
		
		fileLockedCheckBox = new JCheckBox("File Locked");
		fileLockedCheckBox.addActionListener(this);
		
		// Protocol Panel
		JLabel protocolTitle = new CustomLabel("Editing Protocol:");
		protocolTitle.setBorder(eb);
		protocolTitle.setFont(CustomFont.getFontBySize(14));
		protocolInfoPanel.add(protocolTitle, BorderLayout.WEST);
		
		protocolInfoPanel.add(
			new CustomLabel("Create Experiment to edit Experimental Values"),
			BorderLayout.CENTER);
		JButton addExpInfo = new CustomButton(
										iM.getIcon(IconManager.EXP_NEW_ICON));
		addExpInfo.addActionListener(this);
		addExpInfo.setToolTipText("Create an Experiment from this Protocol");
		addExpInfo.setActionCommand(ADD_EXP_INFO);
		protocolInfoPanel.add(addExpInfo, BorderLayout.EAST);
		
		add(expInfoPanel, BorderLayout.CENTER);
		add(protocolInfoPanel, BorderLayout.SOUTH);
		//add(fileLockedCheckBox, BorderLayout.SOUTH);
	}

	/**
	 * Sets the text and visibility of this panel, according to whether the
	 * protocol has any experimental info.
	 */
	private void refreshPanel() 
	{
		
		TreeNode tn = (TreeNode)treeModel.getRoot();
		
		if (!(tn instanceof DefaultMutableTreeNode)) return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tn;
		Object userOb = node.getUserObject();
		if (!(userOb instanceof ProtocolRootField)) return;
		ProtocolRootField prf = (ProtocolRootField)userOb;
		
		field = ExperimentInfo.getExpInfo(treeModel);
		
		// add details (name and date)
		if (field != null) {
			String expDate = field.getAttribute(ExperimentInfo.EXP_DATE);
			String investigName = field.getAttribute
												(ExperimentInfo.INVESTIG_NAME);
			String lastModDate = prf.getAttribute(CPEimport.ARCHIVE_DATE);
			
			String date = "no date";
			
			SimpleDateFormat f = new SimpleDateFormat("yyyy, MMM d");
			SimpleDateFormat timeStamp = 
							new SimpleDateFormat ("yyyy, MMM d 'at' HH:mm:ss");
			try {
				// experiment date
				long millis = new Long(expDate);
				Date d = new Date(millis);
				datePicker.setDate(d);
				date = f.format(d);
				datePicker.setToolTipText(date);
				
				// last-modified date
				millis = new Long(lastModDate);
				d.setTime(millis);
				date = timeStamp.format(d);
				lastModifiedLabel.setText("Last saved: " + date); 
			} catch (NumberFormatException ex) {
				lastModifiedLabel.setText("Last saved: " + date); 
			}
			
			// nameEditor may not have been created yet. 
			if (nameField == null) {
				TextFieldEditor nameEditor = new 
						TextFieldEditor(field, ExperimentInfo.INVESTIG_NAME);
				nameEditor.addPropertyChangeListener
								(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
				nameField = nameEditor.getTextField();
				nameBox.add(nameEditor);
			}
			nameField.setText(investigName);
			
			
			searchUnfilledParams();
			String paramText = "<b>" + unfilledParams + "</b> unfilled Parameters";
			if (unfilledReqParams > 0) {
				paramText = paramText + " <b style='color:red'>" + 
				unfilledReqParams + " *required</b>";
			}
			unfilledParamsLabel.setText("<html>" + paramText + "</html>");
			unfilledStepsLabel.setText("<html>in <b>" + unfilledSteps.size() + 
					"</b> steps.</html>");
			
			selectCurrentStep();
			
			refreshEditingMode();
			
			refreshFileLocked();
			
			expInfoPanel.setVisible(true);
			protocolInfoPanel.setVisible(false);
		}
		else {
			expInfoPanel.setVisible(false);
			protocolInfoPanel.setVisible(true);
		}
		
		revalidate();
		repaint();
	}
	
	/**
	 * This method iterates through the Tree Model, counting the number of 
	 * un-filled parameters in the experiment and making a list of the steps
	 * that contain them. 
	 */
	private void searchUnfilledParams()
	{
		if (unfilledSteps == null) {
			unfilledSteps = new ArrayList<TreePath>();
		} else {
			unfilledSteps.clear();
		}
		unfilledParams = 0;
		unfilledReqParams = 0;
		currentStepIndex = -1;
		
		TreeNode tn;
		IField f;
		Object userOb;
		DefaultMutableTreeNode node;
		TreePath path;
		int paramCount;
		
		Object r = treeModel.getRoot();
		if (! (r instanceof TreeNode)) 		return;
		root = (TreeNode)r;
		
		Iterator<TreeNode> iterator = new TreeIterator(root);
		
		while (iterator.hasNext()) {
			tn = iterator.next();
			if (!(tn instanceof DefaultMutableTreeNode)) continue;
			node = (DefaultMutableTreeNode)tn;
			userOb = node.getUserObject();
			if (!(userOb instanceof IField)) continue;
			f = (IField)userOb;
			path = new TreePath(node.getPath());
			if (f != null) {
				paramCount = f.getUnfilledCount();
				if (paramCount > 0) {
					unfilledParams += paramCount;
					unfilledSteps.add(path);
					paramCount = f.getUnfilledCount(true);
					if (paramCount > 0) {
						unfilledReqParams += paramCount;
					}
				}
			}
		}
	}
	
	/**
	 * This selects the unfilled step in the {@link #navTree} according to 
	 * the {@link #currentStepIndex}.
	 * Then calls {@link #refreshButtons()} to update their enabled status. 
	 */
	private void selectCurrentStep()
	{
		// if index is valid within un-filled steps, select the step
		if (unfilledSteps != null) {
			if (currentStepIndex > -1 && 
								currentStepIndex < unfilledSteps.size()) {
				TreePath currentStep = unfilledSteps.get(currentStepIndex);
				// select path (don't want feedback!)
				
				navTree.removeTreeSelectionListener(this);
				navTree.setSelectionPath(currentStep);
				navTree.addTreeSelectionListener(this);
			}
		}
		refreshButtons();
	}
	
	/**
	 * Refreshes the enabled state of the buttons depending on the current
	 * step index 
	 */
	private void refreshButtons()
	{
		// if no steps selected (before user clicks through)
		if (currentStepIndex == -1) {
			goToFirstStep.setEnabled(true);
			nextStep.setEnabled(false);
			prevStep.setEnabled(false);
		// otherwise, set buttons depending on index 
		} else {			
			goToFirstStep.setEnabled(currentStepIndex != 0);
			prevStep.setEnabled(currentStepIndex > 0);
			nextStep.setEnabled(currentStepIndex < unfilledSteps.size()- 1);
		}
	}
	
	/**
	 * Method to handle the editing of experiment info attributes.
	 * Delegates to the controller to handle undo/redo etc. 
	 * 
	 * @param attributeName
	 * @param newValue
	 */
	private void editAttribute(String attributeName, String newValue)
	{
		controller.editAttribute(field, attributeName, newValue, 
	 			"Experiment", navTree, root);
	}

	/**
	 * Creates an instance of this class, and builds UI. 
	 * 
	 * @param tree				The JTree used for selection management.
	 * @param controller		The controller for editing, undo/redo etc. 
	 */
	ExperimentInfoPanel(JTree tree, BrowserControl controller)
	{
		this.navTree = tree;
		this.controller = controller;
		
		if (navTree != null)
			navTree.addTreeSelectionListener(this);
		
		initialise();
		buildUI();
	}
	
	/**
	 * Sets the Tree Model for this panel to display experimental info. 
	 * 
	 * @param tm		The Tree Model
	 */
	void setTreeModel(TreeModel tm) {
		
		treeModel = tm;
		
		if (treeModel != null)
			treeModel.addTreeModelListener(this);
		
		refreshPanel();
	}
	
	/**
	 * Called by the {@link BrowserUI} when the model's locked status changes.
	 */
	void refreshFileLocked() 
	{
		fileLockedCheckBox.removeActionListener(this);
		fileLockedCheckBox.setSelected(controller.isFileLocked());
		fileLockedCheckBox.addActionListener(this);
	}
	
	/**
	 * Called by the {@link BrowserUI} when the model's editing mode changes.
	 */
	void refreshEditingMode()
	{
		editingMode.refresh();
	}
	
	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Calls {@link #refreshPanel()} when the tree model changes. 
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
		refreshPanel();
	}

	/**
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Handles actions from Step buttons, and from date-picker. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (fileLockedCheckBox.equals(e.getSource())) {
			controller.setFileLocked(fileLockedCheckBox.isSelected());
			return;
		}
		
		String cmd = e.getActionCommand();
		
		if (ADD_EXP_INFO.equals(cmd)) {
			controller.addExperimentalInfo(navTree);
			return;
		}
		
		int stepCount = unfilledSteps.size();
		if (NEXT_STEP.equals(cmd)) {
			if (currentStepIndex < stepCount-1) {
				currentStepIndex++;
				selectCurrentStep();
			}
		}
		
		else if (PREV_STEP.equals(cmd)) {
			if (currentStepIndex > 0) {
				currentStepIndex--;
				selectCurrentStep();
			}
		}
		else if (FIRST_STEP.equals(cmd)) {
			if (stepCount >0) {
				currentStepIndex = 0;
				selectCurrentStep();
			}
		}
		else if (DELETE_INFO.equals(cmd)) {
			controller.deleteExperimentInfo(navTree);
		}
		else if (e.getSource().equals(datePicker)) {
			String date = datePicker.getDate().getTime() + "";
			
			editAttribute(ExperimentInfo.EXP_DATE, date);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeSelectionListener} interface
	 * Resets the {@link #currentStepIndex} index of unfilled steps 
	 * and refreshes the buttons. 
	 * 
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		currentStepIndex = -1;
		// sets enabled status of buttons. 
		refreshButtons(); 
	}

	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface
	 * Listens for edits from E.g. name editor, and calls 
	 * {@link #editAttribute(String, String)} to save the edit...
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(property)) 
		{
			if (evt.getSource() instanceof ITreeEditComp) {
				ITreeEditComp edit = (ITreeEditComp)evt.getSource();
				String attributeName = edit.getAttributeName();
				// don't handle new Objects (eg attribute map) yet.
				String newValue = evt.getNewValue().toString();
				editAttribute(attributeName, newValue);
			}
		}
	}

}
