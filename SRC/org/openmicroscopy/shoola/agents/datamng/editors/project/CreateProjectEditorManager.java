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

package org.openmicroscopy.shoola.agents.datamng.editors.project;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextArea;
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
	implements ActionListener, DocumentListener, MouseListener
{
	
	/** ID used to handle events. */
	private static final int		SAVE = 0;
	private static final int		SELECT = 1;
	private static final int		CANCEL = 2;
	private static final int		RESET = 3;
	
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
	
	/** Cancel button displayed in the {@link CreateProjectEditorBar}. */
	private JButton 				cancelButton;
	
	/** Select button displayed in the {@link CreateProjectEditorBar}. */
	private JButton 				saveButton;
	
	/** Select button displayed in the {@link CreateProjectDatasetsPane}. */
	private JButton 				selectButton;
	
	/** Reset button displayed in the {@link CreateProjectDatasetsPane}. */
	private JButton 				resetButton;
	
	/** textArea displayed in the {@link CreateProjectPane}. */
	private JTextArea				descriptionArea;
	
	/** text area displayed in the {@link CreateProjectPane}. */
	private JTextArea				nameField;
	
	/** 
	* <code>true</code> if the textField displaying the name has been 
	* selected, <code>false</code> toherwise.
	*/
	private boolean					isName;	
	
	/**
	 * Creates a new instance.
	 * 
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
		datasetsToAdd = new ArrayList();
		isName = false;
	}
	
	CreateProjectEditor getView() { return view; }
	
	ProjectData getProjectData() { return model; }
	
	List getDatasets() { return datasets; }
	
	/** Initializes the listeners. */
	void initListeners()
	{
		saveButton = view.getSaveButton();
        attachButtonListener(saveButton, SAVE);
		cancelButton = view.getCancelButton();
        attachButtonListener(cancelButton, CANCEL);
		
		selectButton = view.getSelectButton();
        attachButtonListener(selectButton, SELECT);
		resetButton = view.getResetButton();
        attachButtonListener(resetButton, RESET);
		nameField = view.getNameField();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case SAVE:
					save(); break;
				case CANCEL:
					cancel(); break;
				case SELECT:
					select(); break;
				case RESET:
					resetSelection();	
			} 
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
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
		if (value) {
			if (!datasetsToAdd.contains(ds)) datasetsToAdd.add(ds);
		} else 	datasetsToAdd.remove(ds);
	}

	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}
	
	/** 
	 * Save the new ProjectData object and forward event to the 
	 * {@link DataManagerCtrl}.
	 */
	private void save()
	{
		model.setDescription(descriptionArea.getText());
		model.setName(nameField.getText());
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
		view.selectAll();
		selectButton.setEnabled(false);
	}
	
	/** Cancel selection. */
	private void resetSelection()
	{
		selectButton.setEnabled(true);
		view.cancelSelection();
	}
	
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e) 
	{
		if (isName) saveButton.setEnabled(true);
	}

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
		if (isName) saveButton.setEnabled(true);
	}

	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
		if (isName) saveButton.setEnabled(true);
	}
	
	/** Indicates that the name has been modified. */
	public void mousePressed(MouseEvent e) { isName = true; }

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseClicked(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseEntered(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseExited(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseReleased(MouseEvent e){}
	
}
