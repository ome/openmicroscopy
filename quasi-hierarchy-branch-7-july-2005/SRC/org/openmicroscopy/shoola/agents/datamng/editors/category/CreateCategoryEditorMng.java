/*
 * org.openmicroscopy.shoola.agents.datamng.editors.category.CreateCategoryEditorMng
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


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
class CreateCategoryEditorMng
    implements ActionListener, DocumentListener, MouseListener, ISelector
{

    /** Action command ID. */
    private static final int        SAVE = 0;
    private static final int        SELECT_IMAGE = 1;
    private static final int        RESET_SELECT_IMAGE = 2;
    private static final int        SHOW_IMAGES = 3;
    private static final int        IMAGES_SELECTION = 4;
    private static final int        FILTER = 5;
    
    /** List of images to be added. */
    private List                    imagesToAdd;
    
    private boolean                 isName;
    
    /** Reference to the view. */
    private CreateCategoryEditor    view;
    
    private DataManagerCtrl         agentCtrl;
    
    private int                     selectionIndex;
    
    private Map                     filters, complexFilters;
    
    private List                    selectedDatasets;
    
    private boolean                 loaded;
    
    /**
     * @param editor
     * @param control
     */
    CreateCategoryEditorMng(CreateCategoryEditor view, DataManagerCtrl control)
    {
        this.view = view;
        agentCtrl = control;
        selectionIndex = -1;
        imagesToAdd = new ArrayList();
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
    
    CreateCategoryEditor getView() { return view; }
    
    /** Attach listeners to the components. */
    void initListeners()
    {
        attachBoxListeners(view.getImagesSelection(), IMAGES_SELECTION);
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getSelectButton(), SELECT_IMAGE);
        attachButtonListener(view.getResetButton(), RESET_SELECT_IMAGE);
        attachButtonListener(view.getShowImagesButton(), SHOW_IMAGES);
        attachButtonListener(view.getFilterButton(), FILTER);
        JTextArea nameField = view.getCategoryName();
        nameField.getDocument().addDocumentListener(this);
        nameField.addMouseListener(this);
        JTextArea descriptionArea = view.getCategoryDescription();
        descriptionArea.getDocument().addDocumentListener(this);
    }
    
    /** 
     * Add (resp. remove) the image summary to (resp. from) the list of
     * image summary objects to add to the new dataset.
     * 
     * @param value     boolean value true if the checkBox is selected
     *                  false otherwise.
     * @param ds        dataset summary to add or remove.
     */
    void addImage(boolean value, ImageSummary is) 
    {
        if (value) {
            if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
        } else  imagesToAdd.remove(is);
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

    /** 
     * Create a new Category
     * {@link DataManagerCtrl}.
     */
    private void save()
    {
        String name = view.getCategoryName().getText();
        String description = view.getCategoryDescription().getText();
        CategoryGroupData 
        group = (CategoryGroupData) view.getExistingGroups().getSelectedItem();
        //check if name is ""
        agentCtrl.createNewCategory(group, name, description, imagesToAdd);
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
        view.resetImageSelection();
    }

    /** Bring up the Filter widget. */
    private void bringFilter()
    {
        UIUtilities.centerAndShow(new Filter(agentCtrl, this));
    }
    
    /** Bring up the datasetSelector. */
    private void bringSelector(ActionEvent e)
    {
        int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
        if (selectedIndex == CategoryImagesDiffPane.IMAGES_USED) {
            selectionIndex = selectedIndex;
            //retrieve the user's datasets.
            List d = agentCtrl.getUsedDatasets();
            if (d != null && d.size() > 0) 
                UIUtilities.centerAndShow(new DatasetsSelector(agentCtrl, this,
                        d));
            else {
                UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
                un.notifyInfo("Used datasets", "no dataset used ");
            }
        }
    }
    
    /** Show the existing images. */
    private void showImages()
    {
        CategoryGroupData 
        group = (CategoryGroupData) view.getExistingGroups().getSelectedItem();
        int selectedIndex = view.getImagesSelection().getSelectedIndex();
        if (selectedIndex != selectionIndex || !loaded) {
            selectionIndex = selectedIndex;
            List images = null;
            switch (selectedIndex) {
                case CreateCategoryImagesPane.IMAGES_IMPORTED:
                    images = agentCtrl.getImagesNotInCategoryGroup(group, 
                            filters, complexFilters);
                    break;
                case CreateCategoryImagesPane.IMAGES_GROUP:
                    images = agentCtrl.getImagesInUserGroupNotInCategoryGroup(
                            group, filters, complexFilters);
                     break;
                case CreateCategoryImagesPane.IMAGES_SYSTEM:
                    images = agentCtrl.getImagesInSystemNotInCategoryGroup(
                                group, filters, complexFilters);
                    break;
                case CreateCategoryImagesPane.IMAGES_USED:
                    images = agentCtrl.loadImagesInDatasets(selectedDatasets, 
                            DataManagerCtrl.FOR_CLASSIFICATION, group, filters, 
                            complexFilters);
            }
            displayListImages(images);
        }
    }
    
    /** Display the images. */
    private void displayListImages(List images)
    {
        if (images == null || images.size() == 0) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Image retrieval", "No image matching your criteria");
            return;
        }
        loaded = true;
        view.showImages(images);
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
                case SELECT_IMAGE:
                    selectImage(); break;
                case RESET_SELECT_IMAGE:
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

    /** Require by {@link MouseListener} I/F. */
    public void changedUpdate(DocumentEvent e)
    { 
        view.getSaveButton().setEnabled(isName);
    }
    
    /** Require by {@link MouseListener} I/F. */
    public void insertUpdate(DocumentEvent e)
    {
        view.getSaveButton().setEnabled(isName);
    }

    /** Require by {@link MouseListener} I/F. */
    public void removeUpdate(DocumentEvent e)
    {
        view.getSaveButton().setEnabled(isName);
    }

    /** Indicates that the name has been modified. */
    public void mousePressed(MouseEvent e) { isName = true; }

    /** 
     * Required by {@link MouseListener} I/F but not actually needed 
     * in our case, no op implementation.
     */ 
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed 
     * in our case, no op implementation.
     */ 
    public void mouseEntered(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed 
     * in our case, no op implementation.
     */ 
    public void mouseExited(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed 
     * in our case, no op implementation.
     */ 
    public void mouseReleased(MouseEvent e) {}

}
