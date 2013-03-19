/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.ManagePopupMenu
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;

/** 
 * Menu brought up on screen when the user clicks on the <code>Manage</code>
 * button in the tool bar.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ManagePopupMenu
    extends JPopupMenu
{

    /** Reference to the control. */
    private TreeViewerControl controller;
    
    /** Button to add element to the specified container. */
    private JMenuItem newElement;
    
    /** Button to copy the selected elements. */
    private JMenuItem copyElement;
    
    /** Button to paste the selected elements. */
    private JMenuItem pasteElement;
    
    /** Button to delete the selected elements. */
    private JMenuItem deleteElement;
    
    /** Button to delete the selected elements. */
    private JMenuItem cutElement;
    
    /** Button to add existing element to the specified container. */
    private JMenuItem existingElement;
    
    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
    	/*
        item.setBorder(null);
        item.setFont((Font) 
                TreeViewerAgent.getRegistry().lookup(
                        "/resources/fonts/Labels"));
                        */
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	TreeViewerAction a = 
    			controller.getAction(TreeViewerControl.CREATE_OBJECT);
        newElement = new JMenuItem(a);
        newElement.setText(a.getActionName());
        initMenuItem(newElement);
        copyElement = new JMenuItem(
                controller.getAction(TreeViewerControl.COPY_OBJECT)); 
        initMenuItem(copyElement);
        pasteElement = new JMenuItem(
                controller.getAction(TreeViewerControl.PASTE_OBJECT));
        initMenuItem(pasteElement);
        deleteElement = new JMenuItem(
                controller.getAction(TreeViewerControl.DELETE_OBJECT));
        initMenuItem(deleteElement);
        existingElement = new JMenuItem(
            controller.getAction(TreeViewerControl.ADD_OBJECT));
        initMenuItem(existingElement);
        cutElement = new JMenuItem(
                controller.getAction(TreeViewerControl.CUT_OBJECT));
        initMenuItem(cutElement);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(newElement);
        //add(existingElement);
        add(cutElement);
        add(copyElement);
        add(pasteElement);
        add(deleteElement);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     */
    ManagePopupMenu(TreeViewerControl controller)
    {
        if (controller == null)
            throw new NullPointerException("No control.");
        this.controller = controller;
        initComponents();
        buildGUI();
    }
    
}
