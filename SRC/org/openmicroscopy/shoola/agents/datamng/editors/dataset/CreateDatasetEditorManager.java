/*
 * org.openmicroscopy.shoola.agents.datamng.editors.CreateDatasetEditorManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.dataset;

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
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
public class CreateDatasetEditorManager
	implements ActionListener, DocumentListener, MouseListener
{
	
	/** ID used to handle events. */
	private static final int		SAVE = 0;
	private static final int		SELECT_PROJECT = 1;
	private static final int		RESET_SELECTION_PROJECT = 2;
	private static final int		SELECT_IMAGE = 3;
	private static final int		RESET_SELECTION_IMAGE = 4;
	private static final int		CANCEL = 5;
	
	private CreateDatasetEditor 	view;
	private DatasetData 			model;
	private DataManagerCtrl			control;
	
	private List					projects;
	
	private List					images;
	
	/** List of images to be added. */
	private List					imagesToAdd;
	
	/** List of projects to be added to. */
	private List					projectsToAdd;
	
	/** Cancel button displayed in the {@link CreateDatasetEditorBar}. */
	private JButton 				cancelButton;
	
	/** Select button displayed in the {@link CreateDatasetEditorBar}. */
	private JButton 				saveButton;
	
	/** Select button displayed in the {@link CreateDatasetProjectsPane}. */
	private JButton 				selectButton;
	
	/** Reset button displayed in the {@link CreateDatasetProjectsPane}. */
	private JButton 				resetProjectButton;
	
	/** Select button displayed in the {@link CreateDatasetImagesPane}. */
	private JButton					selectImageButton;
	
	/** Reset button displayed in the {@link CreateDatasetImagesPane}. */
	private JButton					resetImageButton;
	
	/** textArea displayed in the {@link CreateDatasetPane}. */
	private JTextArea				descriptionArea;
	
	/** text field displayed in the {@link CreateDatasetPane}. */
	private JTextArea				nameField;
		
	private boolean					isName;
	
	public CreateDatasetEditorManager(CreateDatasetEditor view, 
									  DataManagerCtrl control,
									  DatasetData model, List projects,
									  List images)
	{
		this.control = control;
		this.view = view;
		this.model = model;
		this.projects = projects;
		this.images = images;
		imagesToAdd = new ArrayList();
		projectsToAdd = new ArrayList();
		isName = false;
	}
	
	CreateDatasetEditor getView() { return view; }
	
	DatasetData getDatasetData() { return model; }
	
	List getProjects() { return projects; }
	
	List getImages() { return images; }
		
	/** Initializes the listeners. */
	void initListeners()
	{
		saveButton = view.getSaveButton();
        attachButtonListener(saveButton, SAVE);
		cancelButton = view.getCancelButton();
        attachButtonListener(cancelButton, CANCEL);
		selectButton = view.getSelectButton();
        attachButtonListener(selectButton, SELECT_PROJECT);
		resetProjectButton = view.getResetProjectButton();
        attachButtonListener(resetProjectButton, RESET_SELECTION_PROJECT);
		selectImageButton = view.getSelectImageButton();
        attachButtonListener(selectImageButton, SELECT_IMAGE);
		resetImageButton = view.getResetImageButton();
        attachButtonListener(resetImageButton, RESET_SELECTION_IMAGE);
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
				case SELECT_PROJECT:
					selectProject(); break;
				case RESET_SELECTION_PROJECT:
					resetSelectionProject(); break;
				case SELECT_IMAGE:
					selectImage(); break;
				case RESET_SELECTION_IMAGE:
					resetSelectionImage();
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** 
	 * Add (resp. remove) the image summary to (resp. from) the list of
	 * image summary objects to add to the new dataset.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove.
	 */
	void addImage(boolean value, ImageSummary is) 
	{
		if (value) {
			if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
		} else 	imagesToAdd.remove(is);
	}
	
	/** 
	 * Add (resp. remove) the project summary to (resp. from) the list of
	 * project summary objects to add to the new dataset.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove.
	 */
	void addProject(boolean value, ProjectSummary ps) 
	{
		if (value) {
			if (!projectsToAdd.contains(ps)) projectsToAdd.add(ps);
		} else 	projectsToAdd.remove(ps);
	}

	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}
	
	/** 
	 * Save the new DatasetData object in DB and forward event to the 
	 * {@link DataManagerCtrl}.
	 */
	private void save()
	{
		model.setDescription(descriptionArea.getText());
		model.setName(nameField.getText());
		//update tree and forward event to DB.
		//forward event to DataManager.
		control.addDataset(projectsToAdd, imagesToAdd, model);
		//close widget.
		view.dispose();
	}

	/** Select projects. */
	private void selectProject()
	{
		view.selectAllProjects();
		selectButton.setEnabled(false);
	}

	/** Cancel selection of projects. */
	private void resetSelectionProject()
	{
		selectButton.setEnabled(true);
		view.resetSelectionProject();
	}
	
	/** Select images. */
	private void selectImage()
	{
		view.selectAllImages();
		selectButton.setEnabled(false);
	}

	/** Cancel selection of images. */
	private void resetSelectionImage()
	{
		selectButton.setEnabled(true);
		view.resetSelectionImage();
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
	
	/** Tells that the name has been modified. */
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
