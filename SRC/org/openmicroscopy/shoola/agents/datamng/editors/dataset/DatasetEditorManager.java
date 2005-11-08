/*
 * org.openmicroscopy.shoola.agents.datamng.editors.dataset.DatasetEditorManager
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ImageData;

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
class DatasetEditorManager
	implements ActionListener, DocumentListener, MouseListener
{
	
	/** Action command ID. */
	private static final int		SAVE = 0;	
	private static final int		REMOVE = 1;
	private static final int		ADD = 2;
	private static final int		RESET = 3;
	private static final int		REMOVE_ADDED = 4;
	private static final int		RESET_ADDED = 5;
    private static final int        VIEW = 6;
	
	private DatasetData				model;
	private DatasetEditor			view;
	
	/** List of images to remove. */
	private List					imagesToRemove;
	
	/**List of images to add. */
	private List					imagesToAdd;
	
	/** List of selected images to be added that have to be removed. */
	private List					imagesToAddToRemove;
	
	private DataManagerCtrl 		agentCtrl;
	
	private boolean					nameChange, isName;
	
	private DatasetImagesDiffPane	dialog;
    
	DatasetEditorManager(DatasetEditor view, DataManagerCtrl agentCtrl,
						DatasetData model)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		this.model = model;
		nameChange = false;
		isName = false;
		imagesToRemove = new ArrayList();
		imagesToAdd = new ArrayList();
		imagesToAddToRemove = new ArrayList();
	}
	
    DataManagerCtrl getAgentControl() { return agentCtrl; }
    
    DatasetEditor getView() { return view; }
    
    DatasetData getDatasetData() { return model; }
    
	Set getImages()
    { 
        Set l = getDatasetData().getImages();
        if (l == null) l = new HashSet();
        return l;
    }
        
    
	
	List getImagesToAdd() { return imagesToAdd; }
	
	List getImagesToAddToRemove() { return imagesToAddToRemove; }

    Set getImagesDiff(Map filters, Map complexFilters)
    { 
        return agentCtrl.getImagesDiff(model, filters, complexFilters);
    }
    
    Set getUserDatasets() { return agentCtrl.getUserDatasets(); }
    
    Set getUsedDatasets() { return agentCtrl.getUsedDatasets(); }
    
    Set getImagesInUserGroupDiff(Map filters, Map complexFilters)
    {
        return agentCtrl.getImagesInUserGroupDiff(model, filters, complexFilters);
    }
    
    Set getImagesInSystemDiff(Map filters, Map complexFilters)
    {
        return agentCtrl.getImagesInSystemDiff(model, filters, complexFilters);
    }
    
	/** Initializes the listeners. */
	void initListeners()
	{
		//buttons
        attachButtonListener(view.getViewButton(), VIEW);
        attachButtonListener(view.getSaveButton(), SAVE);
		attachButtonListener(view.getAddButton(), ADD);
        attachButtonListener(view.getRemoveButton(), REMOVE);
        attachButtonListener(view.getResetButton(), RESET);
        attachButtonListener(view.getRemoveToAddButton(), REMOVE_ADDED);
        attachButtonListener(view.getResetToAddButton(), RESET_ADDED);
		
		//textfields
		JTextArea nameField = view.getNameArea();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		JTextArea descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = -1;
		try {
            index = Integer.parseInt(e.getActionCommand());
			switch (index) {
                case VIEW:
                    agentCtrl.browseDataset(model); break;
				case SAVE:
					save(); break;
				case ADD:
					showImagesSelection(); break;
				case REMOVE:
					remove(); break;
				case RESET:
					resetSelection(); break;
				case REMOVE_ADDED:
					removeAdded(); break;
				case RESET_ADDED:
					resetAdded(); 
			}  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Bring up the images selection dialog. */
	private void showImagesSelection()
	{
		if (dialog == null) dialog = new DatasetImagesDiffPane(this);
		else dialog.removeDisplay();
		UIUtilities.centerAndShow(dialog);
		view.setSelectedPane(DatasetEditor.POS_IMAGE);
		view.getSaveButton().setEnabled(true);	
	}

	/** Add the list of selected images to the {@link ProjectDatasetsPane}. */
	void addImagesSelection(List l)
	{
		Iterator i = l.iterator();
		ImageData is;
		while (i.hasNext()) {
			is = (ImageData) i.next();
			if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
		}
		view.rebuildComponent();
	}
	
	/** 
	 * Add (resp. remove) the image summary of (resp. from) the list of
	 * image summary to be added (resp. removed).
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove.
	 */
	void setToAddToRemove(boolean value, ImageData is) 
	{
		if (value) imagesToAddToRemove.add(is); 
		else {
			if (imagesToAddToRemove.contains(is)) {
				imagesToAddToRemove.remove(is);
			}	
		} 
	}
	
	/** 
	 * Add (resp. remove) the image summary of (resp. from) the list of
	 * image summary objects to be removed.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove.
	 */
	void selectImage(boolean value, ImageData is) 
	{
		if (value) {
			if (!imagesToRemove.contains(is)) imagesToRemove.add(is); 
		}
		else 	imagesToRemove.remove(is);
		view.getSaveButton().setEnabled(true);
	}
	
	/** Save changes in DB. */
	private void save()
	{
		model.setDescription(view.getDescriptionArea().getText());
		model.setName(view.getNameArea().getText());
		agentCtrl.updateDataset(model, imagesToRemove, imagesToAdd, nameChange);
	}
	
	/** Select All images.*/
	private void remove()
	{
		view.getImagesPane().setSelection(new Boolean(true));
		view.getRemoveButton().setEnabled(false);
	}
	
	/** Cancel selection. */
	private void resetSelection()
	{
        view.getRemoveButton().setEnabled(true);
		view.getImagesPane().setSelection(Boolean.FALSE);
	}

	/** Remove the selected images from the queue of images to add. */
	private void removeAdded()
	{
		Iterator i = imagesToAddToRemove.iterator();
		ImageData is;
	
		while (i.hasNext()) {
			is = (ImageData) i.next();
			imagesToAdd.remove(is);
			if (dialog != null) dialog.getManager().setSelected(true, is);
		}
		if (imagesToAddToRemove.size() != 0) {
			imagesToAddToRemove.removeAll(imagesToAddToRemove);
			view.rebuildComponent();
		}
	}

	/** Reset the default for the queue of images to add. */
	private void resetAdded()
	{
		imagesToAddToRemove.removeAll(imagesToAddToRemove);
		view.rebuildComponent();
	}

    /** Attach an {@link ActionListener} to a JButton. */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
	
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
    { 
        view.getSaveButton().setEnabled(true); 
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
	public void mouseReleased(MouseEvent e){}
	
}	
	
