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
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;


/** 
 * Manager for {@link CreateGroupEditor}.
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
class CreateGroupEditorMng
    implements ActionListener
{
    
    /** ID used to handle events. */
    private static final int    SAVE = 0;
    
    private CreateGroupEditor   view;
    
    private DataManagerCtrl     control;
    
    /**
     * Creates a new instance.
     * 
     * @param editor
     * @param model
     * @param datasets      List of dataset summary object.
     */
    public CreateGroupEditorMng(CreateGroupEditor view, DataManagerCtrl control)
    {
        this.control = control;
        this.view = view;
    }
    
    CreateGroupEditor getView() { return view; }
    
    /** Initializes the listeners. */
    void initListeners()
    {
        attachButtonListener(view.getSaveButton(), SAVE);
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
                    save(); 
            } 
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }
    
    /** Save the new group/category. */
    private void save()
    {
        String name = view.getGroupName().getText();
        String description = view.getGroupDescription().getText();
        //Add check if name no valid.
        control.saveNewGroup(name, description);
    }
    
}
