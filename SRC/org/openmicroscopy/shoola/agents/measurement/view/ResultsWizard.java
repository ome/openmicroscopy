/*
 * org.openmicroscopy.shoola.agents.measurement.view.ResultsWizard 
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
package org.openmicroscopy.shoola.agents.measurement.view;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
class ResultsWizard
	extends JDialog implements ActionListener
{
	
	/** Action command ID to add a field to the result table. */
	private static final int ADD = 0;
	
	/** Collection of fields. */
	private List<AnnotationField>	currentFields;

	/** Collection of all the possible fields. */
	private List<AnnotationField>	allFields;
	
	/** The original selection of fields, used by the reset button. */
	private List<AnnotationField>  	originalSelection;
	
	/** The list box showing the current fields */
	private JList					currentFieldsListbox;
	
	/** The list box showing the remaining fields */
	private JList					remainingFieldsListbox;
	
	/** The button to move a field from the remaining fields to current fields. */
	private JButton 				addFieldButton;
	
	/** The button to move a field from the current fields to remaining fields. */
	private JButton 				removeFieldButton;
	
	/** The button to move all fields to the current fields. */
	private JButton 				addAllFieldsButton;
	
	/** The button to move all fields to the remaining fields. */
	private JButton 				removeAllFieldsButton;
	
	/** The button to accept current selection. */
	private JButton 				acceptButton;
	
	/** The button to accept current selection. */
	private JButton 				resetButton;

	/** The button to cancel current selection. */
	private JButton 				cancelButton;
	
	/**
	 * The constructor of the Results wizard. 
	 * 
	 * @param cFields the initial list of fields. 
	 */
	ResultsWizard(List<AnnotationField> cFields, List<AnnotationField> aFields)
	{
		this.setModal(true);
		currentFields = cFields;
		allFields = aFields;
		setSize(500, 440);
		createOriginalSelection();
		createUI();
	}
	
	/**
	 * Reset the current selection to the original selection set when wizard 
	 * called.
	 */
	private void resetSelection()
	{
		currentFields.clear();
		for(AnnotationField field : originalSelection)
			currentFields.add(field);
	}
	
	/**
	 * Create a copy of the original selection set so it can be reset by user.
	 *
	 */
	private void createOriginalSelection()
	{
		originalSelection = new ArrayList<AnnotationField>();
		for(AnnotationField field : currentFields)
			originalSelection.add(field);
	}
		
	/**
	 * Build the UI. 
	 *
	 */
	private void createUI()
	{
		currentFieldsListbox = new JList();
		remainingFieldsListbox = new JList();
		createButtons();
		addActionListeners();
		buildUI();
	}
	
	/**
	 * Add action listeners to the buttons. 
	 *
	 */
	private void addActionListeners()
	{
		addFieldButton.setActionCommand(""+ADD);
		addAllFieldsButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						addAllFields();
					}
				});
		removeFieldButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						removeField();
					}
				});
		removeAllFieldsButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						removeAllFields();
					}
				});
		resetButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						resetButtonClicked();
					}
				});
		cancelButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						cancelButtonClicked();
					}
				});
		acceptButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						acceptButtonClicked();
					}
				});
	}
	
	/**
	 * This method is involked when the user clicks the addFieldButton.
	 * It adds a field to the list and then sorts the list to maintain order. 
	 *
	 */
	private void addField()
	{
		if(remainingFieldsListbox.getSelectedIndex()==-1)
			return;
		int [] indexes = remainingFieldsListbox.getSelectedIndices();
		DefaultListModel model = (DefaultListModel)remainingFieldsListbox.getModel();
		
		for( int i = 0 ; i < indexes.length ; i++)
		{
			String annotationName = (String) model.getElementAt(indexes[i]);
			for(AnnotationField field : allFields)
			{
				if(field.getName().equals(annotationName))
				{
					currentFields.add(field);
					break;
				}
			}
		}
		sortCurrentFields();
		populateCurrentFieldsPanel();
		populateRemainingFieldsPanel();
	}
	
	/**
	 * Sorts the selection list of the user so that the order is preseved between
	 * pairs of attributes (CentreX, and CentreY).
	 *
	 */
	private void sortCurrentFields()
	{
		ArrayList<AnnotationField> sortedList = new ArrayList<AnnotationField>();
		for(AnnotationField allFieldsField : allFields)
		{
			for(AnnotationField currentFieldsField : currentFields)
			{
				if(currentFieldsField.getName().equals(allFieldsField.getName()))
					sortedList.add(currentFieldsField);
			}
		}
		currentFields.clear();
		currentFields.addAll(sortedList);	
	}
	
	/**
	 * Add all fields to the list. 
	 *
	 */
	private void addAllFields()
	{
		currentFields.clear();
		for(AnnotationField field: allFields)
			currentFields.add(field);
		populateCurrentFieldsPanel();
		populateRemainingFieldsPanel();
	}
	
	/**
	 * Remove a single field from the list. 
	 *
	 */
	private void removeField()
	{
		if(currentFieldsListbox.getSelectedIndex()==-1)
			return;
		DefaultListModel model = (DefaultListModel)
									currentFieldsListbox.getModel();
		int [] indexes = currentFieldsListbox.getSelectedIndices();
		
		for( int i = 0 ; i < indexes.length ; i++)
		{
			String annotationName = (String) model.getElementAt(indexes[i]);
			for(AnnotationField field : currentFields)
			{
				if(field.getName().equals(annotationName))
				{
					currentFields.remove(field);
					break;
				}
			}
		}
		
		sortCurrentFields();	
		populateCurrentFieldsPanel();
		populateRemainingFieldsPanel();
	}
	
	/**
	 * Remove all fields from the list. 
	 *
	 */
	private void removeAllFields()
	{
		currentFields.clear();
		populateCurrentFieldsPanel();
		populateRemainingFieldsPanel();
	}
	
	/** 
	 * The user has clicked the cancel button, reset selection to the original
	 * selection and close window. 
	 *
	 */
	private void cancelButtonClicked()
	{
		this.resetSelection();
		this.dispose();
	}
	
	/**
	 * The user has accepted the new selection, close window.
	 *
	 */
	private void acceptButtonClicked()
	{
		this.dispose();
	}
	
	/**
	 * Reset the selection to the original selection.
	 *
	 */
	private void resetButtonClicked()
	{
		this.resetSelection();
		populateCurrentFieldsPanel();
		populateRemainingFieldsPanel();
	}
	
	/**
	 * Create all the buttons in the UI.
	 *
	 */
	private void createButtons()
	{
		addFieldButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.RIGHT_ARROW));
		removeFieldButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.LEFT_ARROW));
		addAllFieldsButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.DOUBLE_RIGHT_ARROW));
		removeAllFieldsButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.DOUBLE_LEFT_ARROW));
		acceptButton = new JButton("Accept");
		cancelButton = new JButton("Cancel");
		resetButton = new JButton("Reset");
	}
	
	/**
	 * Build the ui, the info panel list boxes and control pane at the botton.
	 *
	 */
	private void buildUI()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		JPanel leftPanel = createRemainingFieldsPanel();
		UIUtilities.setDefaultSize(leftPanel, new Dimension(200,300));
		JPanel buttonPanel = createButtonPanel();
		UIUtilities.setDefaultSize(buttonPanel, new Dimension(60,300));
		JPanel rightPanel = createCurrentFieldsPanel();
		UIUtilities.setDefaultSize(rightPanel, new Dimension(200,300));
		container.add(Box.createHorizontalStrut(10));
		container.add(leftPanel);
		container.add(Box.createHorizontalStrut(20));
		container.add(buttonPanel);
		container.add(rightPanel);
		container.add(Box.createHorizontalStrut(10));
		JPanel topPanel = createInfoPanel();
		JPanel bottomPanel = createControlPanel();
		UIUtilities.setDefaultSize(topPanel, new Dimension(500, 70));
		UIUtilities.setDefaultSize(bottomPanel, new Dimension(500, 70));
		UIUtilities.setDefaultSize(container, new Dimension(500, 300));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(topPanel);
		mainPanel.add(container);
		mainPanel.add(bottomPanel);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel);
	}
	
	/**
	 * The info panel at the top the the dialog, showing a little text about the
	 * wizard. 
	 * @return the info panel.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("Results Wizard", 
			"<html><body>This is wizard to select the measurements you wish " +
			"to record in the measurement tool</body></html>",	
		IconManager.getInstance().getIcon(IconManager.WIZARD));
		return infoPanel;
	}
	
	/**
	 * The control panel has the buttons for the accepting, cancelling and 
	 * resetting of selections.
	 * 
	 * @return control panel.
	 */
	private JPanel createControlPanel()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(resetButton);
		controlPanel.add(acceptButton);
		controlPanel.add(cancelButton);
		return controlPanel;
	}
	
	/**
	 * Create the remaining fields panel, which shows the fields which have
	 * not been selected.
	 * @return remainingFieldsPanel. 
	 */
	private JPanel createRemainingFieldsPanel()
	{
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(remainingFieldsListbox);
		fieldsPanel.add(new JLabel("Unused Measurements:"), BorderLayout.NORTH);
		fieldsPanel.add(pane, BorderLayout.CENTER);
		populateRemainingFieldsPanel();
		return fieldsPanel;
	}
	
	/**
	 * Create the central button panel hosting the add and remove selection 
	 * buttons.
	 * @return buttons panel.
	 */
	private JPanel createButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalStrut(30));
		buttonPanel.add(addFieldButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeFieldButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(addAllFieldsButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeAllFieldsButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		return buttonPanel;
	}
	
	/**
	 * Create the current fields panel, which shows the fields which have
	 * been selected.
	 * @return currentFieldsPanel. 
	 */
	private JPanel createCurrentFieldsPanel()
	{
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(currentFieldsListbox);
		fieldsPanel.add(new JLabel("Using Measurements:"), BorderLayout.NORTH);
		fieldsPanel.add(pane, BorderLayout.CENTER);
		populateCurrentFieldsPanel();
		return fieldsPanel;
	}
	
	/**
	 * Update the currentFields list box.
	 *
	 */
	private void populateCurrentFieldsPanel()
	{
	    DefaultListModel listModel = new DefaultListModel();

		for(AnnotationField annotation : currentFields)
			listModel.addElement(annotation.getName());
		currentFieldsListbox.setModel(listModel);
	}

	/**
	 * Update the remaining fields list box.
	 *
	 */
	private void populateRemainingFieldsPanel()
	{
	    DefaultListModel listModel = new DefaultListModel();

		for(AnnotationField allFieldAnnotation : allFields)
		{
			boolean found = false;
			for(AnnotationField currentFieldAnnotation : currentFields)
				if(currentFieldAnnotation.getKey().
						equals(allFieldAnnotation.getKey()))
					found = true;
			if(!found)
				listModel.addElement(allFieldAnnotation.getName());
		}
		remainingFieldsListbox.setModel(listModel);
	}

	/**
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{

		int id = -1;
		try
		{
			id = Integer.parseInt(evt.getActionCommand());
			switch (id)
			{
				case ADD:
					addField();
					break;
				
				default:
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
		
	}
	
}


