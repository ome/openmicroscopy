/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.CreateEditorManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup;





//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;


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
class CreateEditorManager
    implements ActionListener
{
    
    /** ID used to handle events. */
    private static final int    SAVE = 0;
    private static final int    SELECT = 1;
    private static final int    CANCEL = 2;
    private static final int    RESET = 3;
    
    private CreateEditor        view;
    private DataManagerCtrl     control;
    
    private List                imagesToAdd;
    
    /**
     * Creates a new instance.
     * 
     * @param editor
     * @param model
     * @param datasets      List of dataset summary object.
     */
    public CreateEditorManager(CreateEditor view, DataManagerCtrl control)
    {
        this.control = control;
        this.view = view;
        imagesToAdd = new ArrayList();
    }
    
    CreateEditor getView() { return view; }
    
    /** Initializes the listeners. */
    void initListeners()
    {
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getCancelButton(), CANCEL);
        attachButtonListener(view.getSelectButton(), SELECT);
        attachButtonListener(view.getResetButton(), RESET);
    }
    
    /** Attach a listener to a JButton. */
    private void attachButtonListener(JButton button, int id)
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
     * Add (resp. remove) the image to (resp. from) the list of
     * images to add to the new category.
     * 
     * @param value     boolean value true if the checkBox is selected
     *                  false otherwise.
     * @param ds        dataset summary to add or remove
     */
    void addImage(boolean value, ImageSummary is) 
    {
        if (value) {
            if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
        } else  imagesToAdd.remove(is);
    }

    /** Close the widget, doesn't save changes. */
    private void cancel()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    /** Save the new group/category. */
    private void save()
    {
        String nameGroup = view.getNameGroup().getText();
        CategoryGroupData data = null;
        if (nameGroup.equals("")) {
            //check if a lis
            data = (CategoryGroupData) view.getListGroup().getSelectedValue();
            if (data == null) {
                control.getRegistry().getUserNotifier().notifyInfo(
                        "Creation ", "Must select or create a new group."); 
                return;
            } 
        }
        String nameCategory = view.getNameCategory().getText();
        //If we are here we are ready to save.
        if (data != null) {
            //Create a new category+images
           if (nameCategory.length() > 1) {
               control.createNewCategory(data, nameCategory,
                       view.getDescriptionCategory().getText(), 
                       imagesToAdd);
           } else {
               control.getRegistry().getUserNotifier().notifyInfo(
                       "Creation ", "You must create a new category."); 
           } 
        } else {
            if (nameCategory.length() > 1) {
                control.createNews(nameGroup, nameCategory,
                        view.getDescriptionGroup().getText(),
                        view.getDescriptionCategory().getText(), 
                        imagesToAdd);
            } else {
                control.createNewGroup(nameGroup, 
                        view.getDescriptionGroup().getText());
            }
        }
        //close widget.
        view.dispose();
    }
    
    /** Select all images and add them to the model. */
    private void select()
    {
        view.selectAllImages(Boolean.TRUE);
        view.getSelectButton().setEnabled(false);
    }
    
    /** Remove all selected images from the selection. */
    private void resetSelection()
    {
        view.selectAllImages(Boolean.FALSE);
        view.getSelectButton().setEnabled(true);
    }
    
}
