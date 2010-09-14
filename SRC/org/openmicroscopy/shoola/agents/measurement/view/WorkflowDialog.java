/*
* org.openmicroscopy.shoola.agents.measurement.view.WorkflowDialog
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import pojos.WorkflowData;


/**
 * The Dialog. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class WorkflowDialog
	extends JDialog
	implements ActionListener
{
	
	/** The view of the main UI. */
	private MeasurementViewerUI view;
	
	/** The model for the workflowpanel. */
	private MeasurementViewerModel model;

	/** The namespace label for the new workflow. */
	private JLabel namespaceLabel;
	
	/** The keywords label for the new workflow. */
	private  JLabel keywordsLabel;
	
	/** The namespace of the new workflow. */
	private JTextField namespace;
	
	/** The keywords of the new workflow. */
	private  JTextField keywords;
	
	/** Create the new workflow. */
	private JButton createButton;
	
	/** Cancel and hide dialog. */
	private JButton cancelButton;
	
	/** The status bar of the dialog. */
	private StatusBar statusBar;
	
	/**
	 * Initializes the components. Create the label, workflow combobox.
	 */
	private void init()
	{
		namespaceLabel = new JLabel("Namespace");
		keywordsLabel = new JLabel("Keywords");
		namespace = new JTextField();
		keywords = new JTextField();
		createButton = new JButton("Create");
		createButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		statusBar = new StatusBar();
	}
	
	/**
	 * Build the UI using the components created in init().
	 */
	private void buildUI()
	{
		setSize(500, 350);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		//panel.add(createInfoPanel());
		panel.add(namespaceLabel);
		panel.add(Box.createVerticalGlue());
		Dimension d = new Dimension(300,30);
		namespace.setSize(d);
		namespace.setMaximumSize(d);
		namespace.setMinimumSize(d);
		panel.add(wrap(namespace, d));
		panel.add(Box.createVerticalGlue());
		panel.add(keywordsLabel);
		panel.add(Box.createVerticalGlue());
		d = new Dimension(300,80);
		panel.add(wrap(keywords, d));
		panel.add(Box.createVerticalGlue());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(createButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel);
		panel.add(statusBar);
		Container container = getContentPane();
		container.add(createInfoPanel(), BorderLayout.NORTH);
		container.add(panel, BorderLayout.CENTER);
		
	}
	
	private JPanel wrap(JComponent comp, Dimension d)
	{
		JPanel panel= new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(comp);
		panel.add(Box.createHorizontalStrut(10));
		panel.setMaximumSize(d);
		panel.setSize(d);
		panel.setMinimumSize(d);
		return panel;
	}
	
	/**
	 * Creates the info panel at the top the the dialog, 
	 * showing a little text about the workflow Dialog. 
	 * 
	 * @return See above.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("Workflow Panel", 
				"The workflow panel allows you to create a new " +
				"workflow\n that associates a namespace and keywords with " +
				"an ROI.\n"
				+ "The keywords should be separated by  a comma e.g. " +
						"interphase, metaphase, anaphase", 
				IconManager.getInstance().getIcon(IconManager.WIZARD_48));
		return infoPanel;
	}

	/**
	 * Create the workflow from the panel.
	 */
	private void createWorkflow()
	{
		WorkflowData workflow = new WorkflowData(namespace.getText(), 
					keywords.getText());
		model.addWorkflow(workflow);
		view.updateWorkflow();
		statusBar.setStatus("Workflow " + namespace.getText() + " created.");
		clearFields();
	}
	
	/**
	 * Clear the fields of the panel.
	 */
	private void clearFields()
	{
		namespace.setText("");
		keywords.setText("");
	}
	
	/**
	 * Creates the new workflows.
	 * @param view The view which will indicate current workflows. 
	 * @param model The model which will indicate current workflows. 
	 */
	WorkflowDialog(MeasurementViewerUI view, MeasurementViewerModel model)
	{
		this.view = view;
		this.model = model;
		init();
		buildUI();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand() == "Create")
		{
			createWorkflow();
		}
	}

}
