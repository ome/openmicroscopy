/*
 * org.openmicroscopy.shoola.agents.datamng.editors.CreateProjectEditorManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.datamng.editors;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class CreateProjectEditorManager
	implements ActionListener, DocumentListener
{
	private static final int		SAVE = 0;
	private static final int		SELECT = 1;
	private static final int		CANCEL_SELECTION = 2;
	private static final int		NAME_FIELD = 3;
	
	private CreateProjectEditor 	view;
	private ProjectData 			model;
	private DataManagerCtrl			control;
	
	/** 
	 * List of datasets which belong to the user. These datasets can be added
	 * to the new project.
	 */
	private List					datasets;	
	
	/** List of datasets to be added. */
	private List					datasetsToAdd;
	
	/** Select button displayed in the {@link CreateProjectPane}. */
	private JButton 				saveButton;
	
	/** Select button displayed in the {@link CreateProjectDatasetsPane}. */
	private JButton 				selectButton;
	
	/** cancel button displayed in the {@link CreateProjectDatasetsPane}. */
	private JButton 				cancelButton;
	
	/** textArea displayed in the {@link CreateProjectPane}. */
	private JTextArea				descriptionArea;
	
	/** text field displayed in the {@link CreateProjectPane}. */
	private JTextField				nameField;
			
	/**
	 * @param editor
	 * @param model
	 * @param datasets		List of dataset summary object.
	 */
	public CreateProjectEditorManager(CreateProjectEditor view, 
									DataManagerCtrl control, ProjectData model,
									List datasets)
	{
		this.control = control;
		this.view = view;
		this.model = model;
		this.datasets = datasets;
	}
	
	ProjectData getProjectData()
	{
			return model;
	}
	
	List getDatasets()
	{
		return datasets;
	}
	
	/** Initializes the listeners. */
	void initListeners()
	{
		saveButton = view.getSaveButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		selectButton = view.getSelectButton();
		selectButton.addActionListener(this);
		selectButton.setActionCommand(""+SELECT);
		cancelButton = view.getCancelButton();
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL_SELECTION);
		nameField = view.getNameField();
		nameField.addActionListener(this);
		nameField.setActionCommand(""+NAME_FIELD);
		descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int     index = Integer.parseInt(s);
			//fo later
			switch (index) { 
				case SAVE:
					save();
					break;
				case SELECT:
					select();
					break;
				case CANCEL_SELECTION:
					cancelSelection();
					break;
				case NAME_FIELD:
					setNameField();
					break;
			}// end switch  
		} catch(NumberFormatException nfe) {
		   throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** 
	 * Add (resp. remove) the dataset summary to (resp. from) the list of
	 * dataset summary objects to add to the new project.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove
	 */
	void addDataset(boolean value, DatasetSummary ds) 
	{
		if (datasetsToAdd == null) datasetsToAdd = new ArrayList();
		if (value == true) datasetsToAdd.add(ds);
		else 	datasetsToAdd.remove(ds);
	}
	
	/** Update model when the user modifies project's name. */
	void setNameField()
	{
		model.setName(nameField.getText());
		saveButton.setEnabled(true);
	}
	
	/** 
	 * Save the new ProjectData object and forward event to the 
	 * {@link DataManagerCtrl}.
	 */
	private void save()
	{
		model.setDescription(descriptionArea.getText());
		model.setDatasets(datasetsToAdd);
		//update tree and forward event to DB.
		//forward event to DataManager.
		control.addProject(model);
		//close widget.
		view.dispose();
	}
	
	/** Select all datasets and add them to the model. */
	private void select()
	{
		datasetsToAdd = datasets;
		view.selectAll();
		selectButton.setEnabled(false);
	}
	
	/** Cancel selection. */
	private void cancelSelection()
	{
		datasetsToAdd = null;
		selectButton.setEnabled(true);
		view.cancelSelection();
	}
	
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
	{
		saveButton.setEnabled(true);
	}

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
		saveButton.setEnabled(true);
	}

	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
		saveButton.setEnabled(true);
	}
	
}
