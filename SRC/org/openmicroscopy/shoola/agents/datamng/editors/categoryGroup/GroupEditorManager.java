/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.GroupEditorManager
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
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
class GroupEditorManager
    implements ActionListener, DocumentListener, MouseListener
{
    
    /** ID used to handle events. */
    private static final int        SAVE = 0;   
    private static final int        ADD = 1;
    private static final int        CANCEL = 2;
    private static final int        REMOVE_ADDED = 3;
    private static final int        RESET_ADDED = 4;
    
    /** Reference to the model. */
    private CategoryGroupData               model;
    
    /** Reference to the view. */
    private GroupEditor                     view;
    
    /** Reference to the control. */
    private DataManagerCtrl                 control;
    
    private List                            categoriesToAdd, 
                                            categoriesToAddToRemove;
    
    private boolean                         nameChange, isName;
    
    private GroupCategoriesDiffPane         dialog;
    
    public GroupEditorManager(GroupEditor view, DataManagerCtrl control, 
                            CategoryGroupData model)
    {
        
        this.view = view;
        this.control = control;
        this.model = model;
        nameChange = false;
        isName = false;
        categoriesToAdd = new ArrayList();
        categoriesToAddToRemove = new ArrayList();
    }

    List getCategoriesToAdd() { return categoriesToAdd; }
    
    List getCategoriesToAddToRemove() { return categoriesToAddToRemove; }
    
    GroupEditor getView() { return view; }
    
    CategoryGroupData getCategoryGroupData() { return model; }
    
    /** Initializes the listeners. */
    void initListeners()
    {
        //buttons
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getAddButton(), ADD);
        attachButtonListener(view.getCancelButton(), CANCEL);
        attachButtonListener(view.getRemoveToAddButton(), REMOVE_ADDED);
        attachButtonListener(view.getResetToAddButton(), RESET_ADDED);  
        
        //text fields.
        JTextArea nameField = view.getNameField();
        nameField.getDocument().addDocumentListener(this);
        nameField.addMouseListener(this);
        JTextArea descriptionArea = view.getDescriptionArea();
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
                case ADD:
                    showCategorieSelection(); break;
                case CANCEL:
                    cancel(); break;
                case REMOVE_ADDED:
                    removeAdded(); break;
                case RESET_ADDED:
                    resetAdded(); 
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }
    
    /** Bring up the categories selection dialog. */
    void showCategorieSelection()
    {
        if (dialog == null) {
            //tempo solution
            List categoriesDiff = control.getCategoriesDiff(model);
            if (categoriesDiff != null)
                dialog = new GroupCategoriesDiffPane(this, categoriesDiff);
        } else {
            dialog.remove(dialog.getContents());
            dialog.buildGUI();
        }
        view.setSelectedPane(GroupEditor.POS_CATEGORY);
        UIUtilities.centerAndShow(dialog);
        view.getSaveButton().setEnabled(true);  
    }
    
    /** Add the list of selected categories to the {@link GroupCategoriesPane}. */
    void addCategoriesSelection(List l)
    {
        Iterator i = l.iterator();
        CategorySummary cd;
        while (i.hasNext()) {
            cd = (CategorySummary) i.next();
            if (!categoriesToAdd.contains(cd)) categoriesToAdd.add(cd);
        }
        view.rebuildComponent();
    }
    
    /** 
     * Add (resp. remove) the category of (resp. from) the list of
     * categories to be added.
     * 
     * @param value     boolean value true if the checkBox is selected
     *                  false otherwise.
     * @param cd        category to add or remove
     */
    void setToAddToRemove(boolean value, CategorySummary cd) 
    {
        if (value) categoriesToAddToRemove.add(cd); 
        else {
            if (categoriesToAddToRemove.contains(cd)) 
                categoriesToAddToRemove.remove(cd);
        } 
    }
    
    /** Close the widget, doesn't save changes. */
    private void cancel()
    {
        view.setVisible(false);
        view.dispose();
    }
    
    /** Save in DB. */
    private void save()
    {
        model.setDescription(view.getDescriptionArea().getText());
        model.setName(view.getNameField().getText());
        control.updateCategoryGroup(model, categoriesToAdd, nameChange);
        view.dispose();
    }

    /** Remove the selected datasets from the queue of datasets to add. */
    private void removeAdded()
    {
        Iterator i = categoriesToAddToRemove.iterator();
        CategorySummary cd;
    
        while (i.hasNext()) {
            cd = (CategorySummary) i.next();
            categoriesToAdd.remove(cd);
            if (dialog != null) dialog.getManager().setSelected(true, cd);
        }
        if (categoriesToAddToRemove.size() != 0) {
            categoriesToAddToRemove.removeAll(categoriesToAddToRemove);
            view.rebuildComponent();
        }
    }

    /** Reset the default for the list of categories to add. */
    private void resetAdded()
    {
        categoriesToAddToRemove.removeAll(categoriesToAddToRemove);
        view.rebuildComponent();
    }

    /** Require by I/F. */
    public void changedUpdate(DocumentEvent e)
    {
        view.getSaveButton().setEnabled(true);
    }

    /** Require by I/F. */
    public void insertUpdate(DocumentEvent e)
    {
        if (isName) nameChange = true;
        view.getSaveButton().setEnabled(true);
    }
    
    /** Require by I/F. */
    public void removeUpdate(DocumentEvent e)
    {
        if (isName) nameChange = true;
        view.getSaveButton().setEnabled(true);
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
