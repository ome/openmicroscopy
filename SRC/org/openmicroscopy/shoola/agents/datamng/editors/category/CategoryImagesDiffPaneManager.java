/*
 * org.openmicroscopy.shoola.agents.datamng.editors.DatasetImagesDiffPaneManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.category;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.agents.datamng.util.DatasetsSelector;
import org.openmicroscopy.shoola.agents.datamng.util.Filter;
import org.openmicroscopy.shoola.agents.datamng.util.ISelector;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class CategoryImagesDiffPaneManager
	implements ActionListener, ISelector
{
	
	/** Action command ID. */
	private static final int			ALL = 100;
	private static final int			CANCEL = 101;
	private static final int			SAVE = 102;
    private static final int            SHOW_IMAGES = 103;
    private static final int            IMAGES_SELECTION = 104;
    private static final int            FILTER = 105;
	
	/** List of images to be added. */
	private List						imagesToAdd;
	
	/** Reference to the {@link CategoryImagesDiffPane view}. */
	private CategoryImagesDiffPane 		view;
	
	private CategoryEditorManager		control;
	
	private Set						imagesDiff;
	
    private int                         selectionIndex;
    
    private Map                         filters, complexFilters;
    
    private List                        selectedDatasets;
    
    private boolean                     loaded;
    
	CategoryImagesDiffPaneManager(CategoryImagesDiffPane view, 
									CategoryEditorManager control)
	{
			this.view = view;
			this.control = control;	
            selectionIndex = -1;
			imagesToAdd = new ArrayList();
			attachListeners();					
	}

	/** Implemented as specified by {@link ISelector} I/F. */
    public void setSelectedDatasets(List l) { selectedDatasets = l; }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setFilters(Map filters)
    {
        this.filters = filters;
        loaded = true;
    }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setComplexFilters(Map complexFilters)
    { 
        this.complexFilters = complexFilters;
        loaded = true;
    }
    
	/** Attach listeners. */
	private void attachListeners()
	{
        attachButtonListener(view.selectButton, ALL);
        attachButtonListener(view.cancelButton, CANCEL);
        attachButtonListener(view.saveButton, SAVE);
        attachButtonListener(view.showImages, SHOW_IMAGES);
        attachButtonListener(view.filter, FILTER);
        attachBoxListeners(view.selections, IMAGES_SELECTION);
	}
    
    /** Attach an {@link ActionListener} to a {@link JButton}. */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Attach an {@link ActionListener} to a {@link JComboBox}. */
    private void attachBoxListeners(JComboBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }
    
	/** Handle events fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case SAVE:
					saveSelection(); break;
				case ALL:
					selectAll(); break;
				case CANCEL:
					cancelSelection(); break;
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
	
	void setSelected(boolean value, ImageData is)
	{
		if (value) imagesDiff.add(is);
		else imagesDiff.remove(is);
		buttonsEnabled(imagesDiff.size() != 0);
	}
	
	/** Set the buttons enabled. */
	void buttonsEnabled(boolean b)
	{
		view.selectButton.setEnabled(b);
		view.cancelButton.setEnabled(b);
		view.saveButton.setEnabled(b);
	}
	
	/** 
	 * Add (resp. remove) the image to (resp. from) the list of
	 * image summary objects to be added to the project.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove
	 */
	void addImage(boolean value, ImageData is) 
	{
		if (value) {
			if (!imagesToAdd.contains(is))	imagesToAdd.add(is); 
		} else 	imagesToAdd.remove(is);
	}
	
    /** Bring up the Filter widget. */
    private void bringFilter()
    {
        UIUtilities.centerAndShow(new Filter(control.getAgentControl(), this));
    }
    
    /** Bring up the datasetSelector. */
    private void bringSelector(ActionEvent e)
    {
        int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
        if (selectedIndex == CategoryImagesDiffPane.IMAGES_USED) {
            selectionIndex = selectedIndex;
            //retrieve the datasets used by the current user.
            Set d = control.getUsedDatasets();
            if (d != null && d.size() > 0) {
                DatasetsSelector dialog = new DatasetsSelector(
                    control.getAgentControl(), this, d);
                UIUtilities.centerAndShow(dialog);
            } else {
                UserNotifier un = 
                    control.getAgentControl().getRegistry().getUserNotifier();
                un.notifyInfo("Used datasets", "no dataset used ");
            }
        }
    }
    
    /** Retrieve and display the requested list of images. */
    private void showImages()
    {
        int selectedIndex = view.selections.getSelectedIndex();
        if (selectedIndex != selectionIndex || !loaded) {
            selectionIndex = selectedIndex;
            Set images = null;
            switch (selectedIndex) {
                case CategoryImagesDiffPane.IMAGES_IMPORTED:
                    images = control.getImagesDiff(filters, complexFilters); 
                    break;
                case CategoryImagesDiffPane.IMAGES_GROUP:
                    images = control.getImagesDiffInUserGroup(filters, 
                                complexFilters);
                    break;
                case CategoryImagesDiffPane.IMAGES_SYSTEM:
                    images = control.getImagesDiffInSystem(filters, 
                                        complexFilters); 
                    break;
                case CategoryImagesDiffPane.IMAGES_USED:
                    images = control.getAgentControl().loadImagesInDatasets(
                            selectedDatasets, 
                            DataManagerCtrl.FOR_CLASSIFICATION, 
                            control.getCategoryData(), filters, complexFilters);
            }
            displayListImages(images);
        }
    }
    
	/** Add the selection to the dataset. */
	private void saveSelection()
	{
		if (imagesToAdd.size() != 0) {
			Iterator i = imagesToAdd.iterator();
			while (i.hasNext())
				imagesDiff.remove(i.next());
			buttonsEnabled(imagesDiff.size() != 0);
			control.addImagesSelection(imagesToAdd);
			imagesToAdd.removeAll(imagesToAdd);
		}
		view.setVisible(false);
	}
    
    /** Display the images. */
    public void displayListImages(Set images)
    {
        if (images == null || images.size() == 0) {
            UserNotifier 
                un = control.getAgentControl().getRegistry().getUserNotifier();
            un.notifyInfo("Image retrieval", "No image matching your criteria");
            return;
        }
        imagesDiff = images;
        view.showImages(images);
        loaded = true;
    }
    
	/** Select All datasets.*/
	private void selectAll()
	{
		view.selectButton.setEnabled(false);
		view.setSelection(Boolean.TRUE);
	}
	
	/** Cancel the selection. */
	private void cancelSelection()
	{
		view.selectButton.setEnabled(true);
		view.setSelection(Boolean.FALSE);
	}
	
}
