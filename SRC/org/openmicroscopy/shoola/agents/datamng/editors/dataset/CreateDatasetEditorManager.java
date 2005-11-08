/*
 * org.openmicroscopy.shoola.agents.datamng.editors.dataset.CreateDatasetEditorManager
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
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.util.DatasetsSelector;
import org.openmicroscopy.shoola.agents.datamng.util.Filter;
import org.openmicroscopy.shoola.agents.datamng.util.ISelector;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ImageData;
import pojos.ProjectData;

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
class CreateDatasetEditorManager
	implements ActionListener, DocumentListener, MouseListener, ISelector
{
	
	/** Action command ID. */
	private static final int		SAVE = 0;
	private static final int		SELECT_PROJECT = 1;
	private static final int		RESET_SELECTION_PROJECT = 2;
	private static final int		SELECT_IMAGE = 3;
	private static final int		RESET_SELECTION_IMAGE = 4;
    private static final int        SHOW_IMAGES = 5;
    private static final int        IMAGES_SELECTION = 6;
    private static final int        FILTER = 7;
	
	private CreateDatasetEditor 	view;
    
    /** Reference to the datamodel. */
	private DatasetData 			model;
    
	private DataManagerCtrl			agentCtrl;
	
	private Set					   projects;
	
	/** List of images to be added. */
	private List					imagesToAdd;
	
	/** List of projects to be added to. */
	private List					projectsToAdd;
	
	private boolean					isName;
	
    private int                     selectionIndex;
    
    private Map                     filters, complexFilters;
    
    private List                    selectedDatasets;
    
    private boolean                 loaded;
    
    CreateDatasetEditorManager(CreateDatasetEditor view, 
									  DataManagerCtrl agentCtrl,
									  DatasetData model, Set projects)
	{
		this.agentCtrl = agentCtrl;
		this.view = view;
		this.model = model;
        selectionIndex = -1;
		this.projects = projects;
		imagesToAdd = new ArrayList();
		projectsToAdd = new ArrayList();
		isName = false;
	}
	
    Registry getRegistry() { return agentCtrl.getRegistry(); }

    /** Implemented as specified by {@link ISelector} I/F. */
    public void setSelectedDatasets(List l) { selectedDatasets = l; }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setFilters(Map filters)
    {
        this.filters = filters;
        loaded = false;
    }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setComplexFilters(Map complexFilters)
    { 
        this.complexFilters = complexFilters;
        loaded = false;
    }
    
	CreateDatasetEditor getView() { return view; }
	
	DatasetData getDatasetData() { return model; }
	
	Set getProjects() { return projects; }
		
	/** Initializes the listeners. */
	void initListeners()
	{
        attachBoxListeners(view.getImagesSelections(), IMAGES_SELECTION);
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getSelectButton(), SELECT_PROJECT);
        attachButtonListener(view.getResetProjectButton(), 
                            RESET_SELECTION_PROJECT);
        attachButtonListener(view.getSelectImageButton(), SELECT_IMAGE);
        attachButtonListener(view.getResetImageButton(), 
                                RESET_SELECTION_IMAGE);
        attachButtonListener(view.getShowImagesButton(), SHOW_IMAGES);
        attachButtonListener(view.getFilterButton(), FILTER);
        //TextArea
		JTextArea nameField = view.getNameArea();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		JTextArea descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Attach an {@link ActionListener} to a {@link JComboBox}. */
    private void attachBoxListeners(JComboBox button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = -1;
		try {
            index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case SAVE:
					save(); break;
				case SELECT_PROJECT:
					selectProject(); break;
				case RESET_SELECTION_PROJECT:
					resetSelectionProject(); break;
				case SELECT_IMAGE:
					selectImage(); break;
				case RESET_SELECTION_IMAGE:
					resetSelectionImage(); break;
                case SHOW_IMAGES:
                    showImages(); break;
                case IMAGES_SELECTION:
                    bringSelector(e); break;
                case FILTER:
                    bringFilter();
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
	void addImage(boolean value, ImageData is) 
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
	void addProject(boolean value, ProjectData ps) 
	{
		if (value) {
			if (!projectsToAdd.contains(ps)) projectsToAdd.add(ps);
		} else 	projectsToAdd.remove(ps);
	}

    /** Bring up the Filter widget. */
    private void bringFilter()
    {
        UIUtilities.centerAndShow(new Filter(agentCtrl, this));
    }
    
    /** Bring up the datasetSelector widget. */
    private void bringSelector(ActionEvent e)
    {
        int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
        if (selectedIndex == CreateDatasetImagesPane.IMAGES_USED) {
            selectionIndex = selectedIndex;
            //retrieve the datasets used by the current user.
            Set d = agentCtrl.getUsedDatasets();
            if (d != null && d.size() > 0)
                UIUtilities.centerAndShow(new DatasetsSelector(agentCtrl, this, 
                                            d));
            else {
                UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
                un.notifyInfo("Used datasets", "no dataset used ");
            }
        }
    }
    
    /** Show the images. */
    private void showImages()
    {
        int selectedIndex = view.getImagesSelections().getSelectedIndex();
        if (selectedIndex != selectionIndex || !loaded) {
            selectionIndex = selectedIndex;
            Set images = null;
            switch (selectedIndex) {
                case CreateDatasetImagesPane.IMAGES_IMPORTED:
                    images = agentCtrl.getImportedImages(filters, 
                                complexFilters);
                    break;
                case CreateDatasetImagesPane.IMAGES_GROUP:
                    images = agentCtrl.getGroupImages(filters, complexFilters);
                    break;
                case CreateDatasetImagesPane.IMAGES_SYSTEM:
                    images = agentCtrl.getSystemImages(filters, complexFilters);
                    break;
                case CreateDatasetImagesPane.IMAGES_USED:
                    images = agentCtrl.loadImagesInDatasets(selectedDatasets, 
                            DataManagerCtrl.FOR_HIERARCHY, null, filters, 
                            complexFilters);
            }
            displayListImages(images);
        }
    }
    
    /** Display the images. */
    private void displayListImages(Set images)
    {
        if (images == null || images.size() == 0) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Image retrieval", "No image matching your criteria");
            return;
        }
        view.showImages(images);
        loaded = true;
    }
	
	/** 
	 * Save the new DatasetData object in DB and forward event to the 
	 * {@link DataManagerCtrl}.
	 */
	private void save()
	{
        if (projectsToAdd.size() == 0) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Create dataset", 
                    "The dataset you want to create must " +
                    "be added to an existing project.");
            return;
        }
		model.setDescription(view.getDescriptionArea().getText());
		model.setName(view.getNameArea().getText());
		//update tree and forward event to DB.
		//forward event to DataManager.
        
		agentCtrl.addDataset(projectsToAdd, imagesToAdd, model);
	}

	/** Select projects. */
	private void selectProject()
	{
		view.selectAllProjects();
		view.getSelectButton().setEnabled(false);
	}

	/** Cancel selection of projects. */
	private void resetSelectionProject()
	{
        view.getSelectButton().setEnabled(true);
		view.resetSelectionProject();
	}
	
	/** Select images. */
	private void selectImage()
	{
		view.selectAllImages();
		view.getSelectButton().setEnabled(false);
	}

	/** Cancel selection of images. */
	private void resetSelectionImage()
	{
        view.getSelectButton().setEnabled(true);
		view.resetSelectionImage();
	}
    
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
	{ 
		view.getSaveButton().setEnabled(isName);
	}

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
        view.getSaveButton().setEnabled(isName);
	}

	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
        view.getSaveButton().setEnabled(isName);
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
	public void mouseReleased(MouseEvent e) {}
	
}
