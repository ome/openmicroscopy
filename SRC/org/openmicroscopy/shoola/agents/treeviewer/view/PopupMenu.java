/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.PopupMenu
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;


/** 
 * Pop-up menu for nodes in the browser display.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class PopupMenu
    extends JPopupMenu
{

    /** 
     * Button to bring up the property sheet of a hierarchy object &#151; 
     * project, dataset, category group, category, or image.
     */
    private JMenuItem   properties;
    
    /** Button to browse a container or bring up the Viewer for an image. */
    private JMenuItem   view;
    
    /** Button to reload the data. */
    private JMenuItem   refresh;
    
    /** Button to add element to the specified container. */
    private JMenuItem   newElement;
    
    /** Button to copy the selected elements. */
    private JMenuItem   copyElement;
    
    /** Button to paste the selected elements. */
    private JMenuItem   pasteElement;
    
    /** Button to delete the selected elements. */
    private JMenuItem   deleteElement;
    
    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
        item.setBorder(null);
        item.setFont((Font) 
                TreeViewerAgent.getRegistry().lookup(
                        "/resources/fonts/Labels"));
    }
    
    /**
     * Creates the menu items with the given actions.
     * 
     * @param controller The Controller.
     */
    private void createMenuItems(TreeViewerControl controller)
    {
        properties = new JMenuItem(
                	controller.getAction(TreeViewerControl.PROPERTIES));
        initMenuItem(properties);
        view = new JMenuItem(controller.getAction(TreeViewerControl.VIEW));
        initMenuItem(view);
        refresh = new JMenuItem(
                	controller.getAction(TreeViewerControl.REFRESH));
        initMenuItem(refresh);
        newElement = new JMenuItem(
                	controller.getAction(TreeViewerControl.CREATE_OBJECT));
        initMenuItem(newElement);
        copyElement = new JMenuItem(
                	controller.getAction(TreeViewerControl.COPY_OBJECT)); 
        initMenuItem(newElement);
        pasteElement = new JMenuItem(
                	controller.getAction(TreeViewerControl.PASTE_OBJECT)); 
        initMenuItem(newElement);
        deleteElement = new JMenuItem(
                	controller.getAction(TreeViewerControl.DELETE_OBJECT)); 
        initMenuItem(newElement);
    }
    
    /**
     * Creates the sub-menu to manage the data.
     * 
     * @return See above
     */
    private JMenu createManagementMenu()
    {
        JMenu managementMenu = new JMenu("Manage");
        managementMenu.add(newElement);
        managementMenu.add(copyElement);
        managementMenu.add(pasteElement);
        managementMenu.add(deleteElement);
        return managementMenu;
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(view);
        add(createManagementMenu());
        add(refresh);
    }
    
    /** 
     * Creates a new instance.
     *
     * @param controller The Controller. Mustn't be <code>null</code>.
     */
    PopupMenu(TreeViewerControl controller)
    {
        if (controller == null) 
            throw new IllegalArgumentException("No control.");
        createMenuItems(controller);
        buildGUI() ;
    }
    
}
