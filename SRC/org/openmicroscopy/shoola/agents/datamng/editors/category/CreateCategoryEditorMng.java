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
import org.openmicroscopy.shoola.agents.datamng.util.IDatasetsSelectorMng;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
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
    implements ActionListener, DocumentListener, MouseListener,
    IDatasetsSelectorMng
{

    /** ID used to handle events. */
    private static final int        SAVE = 0;
    private static final int        SELECT_IMAGE = 1;
    private static final int        RESET_SELECT_IMAGE = 2;
    private static final int        CANCEL = 3;
    private static final int        SHOW_IMAGES = 4;
    private static final int        IMAGES_SELECTION = 5;
    
    /** List of images to be added. */
    private List                    imagesToAdd;
    
    private boolean                 isName;
    
    /** Reference to the view. */
    private CreateCategoryEditor    view;
    
    private DataManagerCtrl         control;
    
    private int                     selectionIndex;
    
    /**
     * @param editor
     * @param control
     */
    CreateCategoryEditorMng(CreateCategoryEditor view, DataManagerCtrl control)
    {
        this.view = view;
        this.control = control;
        selectionIndex = -1;
        imagesToAdd = new ArrayList();
    }

    /** Implemented as specified in  {@link IDatasetsSelectorMng}. */
    public void displayListImages(List images)
    {
        if (images == null || images.size() == 0) return;
        view.showImages(images);
    }
    
    CreateCategoryEditor getView() { return view; }
    
    /** Attach listeners to the components. */
    void initListeners()
    {
        attachBoxListeners(view.getImagesSelection(), IMAGES_SELECTION);
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getCancelButton(), CANCEL);
        attachButtonListener(view.getSelectButton(), SELECT_IMAGE);
        attachButtonListener(view.getResetButton(), RESET_SELECT_IMAGE);
        attachButtonListener(view.getShowImagesButton(), SHOW_IMAGES);
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
    
    /** Close the widget, doesn't save changes. */
    private void cancel()
    {
        view.setVisible(false);
        view.dispose();
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
        control.createNewCategory(group, name, description, imagesToAdd);
        view.dispose();
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

    /** Bring up the datasetSelector. */
    private void bringSelector(ActionEvent e)
    {
        int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
        if (selectedIndex == CategoryImagesDiffPane.IMAGES_USED) {
            selectionIndex = selectedIndex;
            //retrieve the user's datasets.
            List d = control.getUserDatasets();
            if (d != null && d.size() > 0) {
                CategoryGroupData group = 
                (CategoryGroupData) view.getExistingGroups().getSelectedItem();
                DatasetsSelector dialog = new DatasetsSelector(
                    control, this,  d, DataManagerCtrl.IMAGES_FOR_CGI, group);
                UIUtilities.centerAndShow(dialog);
            }   
        }
    }
    
    /** Show the existing images. */
    private void showImages()
    {
        CategoryGroupData 
        group = (CategoryGroupData) view.getExistingGroups().getSelectedItem();
        int selectedIndex = view.getImagesSelection().getSelectedIndex();
        if (selectedIndex != selectionIndex) {
            selectionIndex = selectedIndex;
            List images = null;
            switch (selectedIndex) {
                case CreateCategoryImagesPane.IMAGES_IMPORTED:
                    images = control.getImagesNotInCategoryGroup(group); break;
                case CreateCategoryImagesPane.IMAGES_GROUP:
                    images = 
                        control.getImagesInUserGroupNotInCategoryGroup(group);
                     break;
                case CreateCategoryImagesPane.IMAGES_SYSTEM:
                    images = 
                        control.getImagesInSystemNotInCategoryGroup(group);
                     break;
            }
            displayListImages(images);
        }
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
                case CANCEL:
                    cancel(); break;
                case SELECT_IMAGE:
                    selectImage(); break;
                case RESET_SELECT_IMAGE:
                    resetSelectionImage(); break;
                case SHOW_IMAGES:
                    showImages(); break;
                case IMAGES_SELECTION:
                    bringSelector(e); break;    
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
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
