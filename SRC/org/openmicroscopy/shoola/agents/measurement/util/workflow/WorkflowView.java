/*
 * org.openmicroscopy.shoola.agents.measurement.util.workflow.WorkflowView
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
 */
package org.openmicroscopy.shoola.agents.measurement.util.workflow;



//Java imports
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.WorkflowData;


/** 
 * This is the UI for the create workflow creation dialog.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class WorkflowView 
	implements ActionListener, ListSelectionListener
{
	/** Title of the Dialog */
	private static String DIALOGTITLE = "New Workflow Dialog"; 
	
	/** Save action name */
	private static String SAVEACTION = "Save"; 
	
	/** Delete action name */
	private static String DELETEACTION = "Delete"; 
	
	/** Cancel action name */
	private static String CANCELACTION = "Cancel"; 
	
	/** Close action name */
	private static String CLOSEACTION = "Close"; 
	
	/** Create action name */
	private static String CREATEACTION = "Create"; 
	
	/** The parent dialog of the dialog. */
	CreateWorkflowDialog parent;
	
	/** The swind UI dialog that will display the workflow dialogs. */
	JDialog dialog;
	
	/** The model of the UI. */
	WorkflowModel model;
	
	/** The list UI shwoing the workflow names. */
	JList workflowList;
	
	/** The Save Button. */
	JButton saveButton;

	/** The create Button, clicking will create a new workflow. */
	JButton createButton;

	/** The cancel Button, this will close the dialog and dismiss all changes. */
	JButton cancelButton;
	
	/** The Delete Button, this will delete a workflow from the system. */
	JButton deleteButton;

	/** The close Button. */
	JButton closeButton;
	
	/** 
	 * The text field that hold the name of the namespace, should only be editable
	 * for newly created workflows. 
	 */
	JTextField namespaceText;
	
	/** The text field holding all the csv separated keywords. */
	JTextArea keywordsText;
	
	/** The currently selected workflow. */
	WorkflowData currentWorkflow;
	
	/** <code>true</code> if this is a newly created workflow. */
	boolean newWorkflow;
	
	/**
	 * Instantiate the new workflow view. 
	 * @param parent The dialog that is the entry point for creating a workflow.
	 * @param model The model for the view, amnipulates the WorkflowData elements.
	 */
	public WorkflowView(CreateWorkflowDialog parent, WorkflowModel model)
	{
		init(parent, model);
		buildUI(model);
	}
	
	/**
	 * Initialise all the parameters.
	 * @param parent The dialog that is the entry point for creating a workflow.
	 * @param model The model for the view, amnipulates the WorkflowData elements.
	 */
	private void init(CreateWorkflowDialog parent, WorkflowModel model)
	{
		this.parent = parent;
		this.model = model;
		this.currentWorkflow = null;
		this.newWorkflow = false;
	}
	
	/**
	 * Build the UI for the workflow dialog.
	 * @param model The model for manipulating the workflowData.
	 */
	private void buildUI(WorkflowModel model)
	{
		dialog = new JDialog();
		dialog.setTitle(DIALOGTITLE);
		dialog.setSize(400,650);
		dialog.setModal(true);
		dialog.getContentPane().add(createContent());
	}
	
	/**
	 * Create the content for the dialog, the title, buttons, and list boxes.
	 * @return See above.
	 */
	private JPanel createContent()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createInfoPanel());
		panel.add(createWorkflowPanel());
		panel.add(createButtonPanel());
		return panel;
	}
	
	/**
	 * Creates the info panel at the top the the dialog, 
	 * showing a little text about the Workflow Assistant. 
	 * 
	 * @return See above.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("Workflow Assistant", 
				"The Workflow Assistant allows the creation of new " +
				"workflows  \n" +
				"to annotate ROI as being for a particular class and" +
				"keywords \n Defining the subclasses.", 
				IconManager.getInstance().getIcon(IconManager.WIZARD_48));
		return infoPanel;
	}	
	
	/**
	 * Create the panel to display the workflow list box and its namespace 
	 * and keywords text boxes.
	 * @return See above.
	 */
	private JPanel createWorkflowPanel()
	{
		JPanel workflowPanel = new JPanel();
		workflowPanel.setLayout(new BoxLayout(workflowPanel, BoxLayout.X_AXIS));
		workflowPanel.add(createWorkflowListPanel());
		workflowPanel.add(createWorkflowKeywordsPanel());
		return workflowPanel;
	}
	
	/**
	 * Create the list panel, displaying the names of all namespaces.
	 * @return See above.
	 */
	private JPanel createWorkflowListPanel()
	{
		JPanel workflowListPanel = new JPanel();
		JPanel namespacePanel = new JPanel();
		namespacePanel.setLayout(new BoxLayout(namespacePanel, BoxLayout.X_AXIS));
		JLabel namespaceLabel = new JLabel("Namespace");
		namespacePanel.add(namespaceLabel);
		namespacePanel.add(Box.createHorizontalGlue());
		workflowListPanel.setSize(new Dimension(150,400));
		workflowListPanel.setLayout(new BoxLayout(workflowListPanel, BoxLayout.Y_AXIS));
		workflowListPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		workflowList = new JList();
		workflowList.setBorder(BorderFactory.createLoweredBevelBorder());
		workflowList.setModel(model.getListModel());
		workflowList.addListSelectionListener(this);
		workflowListPanel.add(namespacePanel);
		workflowListPanel.add(workflowList);
		workflowListPanel.add(Box.createVerticalGlue());
		return workflowListPanel;
	}

	/**
	 * Create the keywords, namespace textboxes.
	 * @return See above.
	 */
	private JPanel createWorkflowKeywordsPanel()
	{
		JPanel panel = new JPanel();
		JLabel namespaceLabel = new JLabel("Namespace");
		JLabel keywordsLabel = new JLabel("Keywords");
		namespaceText = new JTextField();
		keywordsText = new JTextArea();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		JPanel namespacePanel = new JPanel();
		namespacePanel.setLayout(new BoxLayout(namespacePanel, BoxLayout.X_AXIS));
		namespacePanel.add(namespaceLabel);
		namespacePanel.add(Box.createHorizontalGlue());
		JPanel keywordsPanel = new JPanel();
		keywordsPanel.setLayout(new BoxLayout(keywordsPanel, BoxLayout.X_AXIS));
		keywordsPanel.add(keywordsLabel);
		keywordsPanel.add(Box.createHorizontalGlue());
		panel.add(namespacePanel);
		panel.add(namespaceText);
		panel.add(Box.createVerticalGlue());
		panel.add(keywordsPanel);
		keywordsText.setBorder(BorderFactory.createLoweredBevelBorder());
		panel.add(keywordsText);
		return panel;
	}
	
	/**
	 * Create the save, cancel etc, buttons.
	 * @return See above.
	 */
	private JPanel createButtonPanel()
	{
		JPanel panel = new JPanel();
		saveButton = new JButton(SAVEACTION);
		saveButton.addActionListener(this);
		deleteButton = new JButton(DELETEACTION);
		deleteButton.addActionListener(this);
		cancelButton = new JButton(CANCELACTION);
		cancelButton.addActionListener(this);
		closeButton = new JButton(CLOSEACTION);
		closeButton.addActionListener(this);
		createButton = new JButton(CREATEACTION);
		createButton.addActionListener(this);

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		panel.add(Box.createHorizontalGlue());
		panel.add(createButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(saveButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(deleteButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(cancelButton);
		panel.add(Box.createRigidArea(new Dimension(10, 0)));
		panel.add(closeButton);
		return panel;

	}

	/**
	 * Save the workflows.
	 */
	private void saveAction()
	{
		currentWorkflow.setKeywords(CSVToList(keywordsText.getText()));
		if(newWorkflow)
		{
			currentWorkflow.setNamespace(namespaceText.getText());
			model.addItem(currentWorkflow);
		}
	}

	/** Delete the selected workflow. */
	private void deleteAction()
	{
		
	}
	
	/** Cancel the dialog, save nothing. */
	private void cancelAction()
	{
		parent.cancel();
	}

	/** Close the dialog and save all workflows. */
	private void closeAction()
	{
		dialog.setVisible(false);
	}
	
	/** Show the dialog. */
	public void show()
	{
		UIUtilities.centerAndShow(dialog);
	}

	/** 
	 * Create action, set the current workflow to new WorkflowData, and set
	 * newWorkflow <code>true</code>.
	 */
	private void createAction()
	{
		clearNamespaceKeywordsFields();
		currentWorkflow = new WorkflowData();
		newWorkflow = true;
	}
	
	/** Set the namespaces and keywords to the current workflows. */
	private void setNamespaceKeywords()
	{
		newWorkflow = false;
		namespaceText.setText(currentWorkflow.getNameSpace());
		keywordsText.setText(currentWorkflow.getKeywords());
	}
	
	/** Clear the namespaces and keywords fields. */
	private void clearNamespaceKeywordsFields()
	{
		currentWorkflow=null;
		newWorkflow = false;
		namespaceText.setText("");
		keywordsText.setText("");
	}
	
	/** Clear the selected workflows, and set namespaces, keywords to empty. */
	private void clearWorkflowSelection()
	{
		if(currentWorkflow!=null)
			if(currentWorkflow.isDirty())
			{
				String action = checkToClear();
				if(action == CANCELACTION)
					return;
				if(action == SAVEACTION)
					saveAction();
			}
		clearNamespaceKeywordsFields();
	}
	
	/** 
	 * Set the workflow selection to the index.
	 * @param index See above.
	 */
	private void setWorkflowSelection(int index)
	{
		currentWorkflow = model.getItem(index);
		setNamespaceKeywords();
	}
	
	/**
	 * Check to see if we can clear the workflow, if dirty as to save.
	 * @return See above.
	 */
	private String checkToClear()
	{
		int result = JOptionPane.showConfirmDialog(dialog, "Do you want to save workflow?", "Save confirmation", JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(result==JOptionPane.CANCEL_OPTION)
			return CANCELACTION;
		else if(result==JOptionPane.YES_OPTION)
			return SAVEACTION;
		else
			return DELETEACTION;
	}
	
	
	/**
	 * Call the different actions based on buttons pressed.
	 * @param e The action event.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand()==SAVEACTION)
			saveAction();
		else if(e.getActionCommand()==DELETEACTION)
			deleteAction();
		else if(e.getActionCommand()==CANCELACTION)
			cancelAction();
		else if(e.getActionCommand()==CLOSEACTION)
			closeAction();
		else if(e.getActionCommand()==CREATEACTION)
			createAction();
	}

	/**
	 * Called when the value in the list is changed.
	 * @param e The list selection event.
	 */
	public void valueChanged(ListSelectionEvent e)
	{
	  if (e.getValueIsAdjusting() == false) 
	        if (workflowList.getSelectedIndex() == -1) 
	        	clearWorkflowSelection();
	        else 
	        	setWorkflowSelection(workflowList.getSelectedIndex());    	
	}
	
	/**
	* Converts a CSV string to a list of strings.
	*
	* @param str The CSV string to convert.
	* @return See above.
	*/
	private List<String> CSVToList(String str)
	{
		List<String> list = new ArrayList<String>();
		String[] valueString = str.split(",");
		for(String keyword : valueString)
			if(!keyword.equals("[]"))
                list.add(keyword);
		return list;
	}
}
