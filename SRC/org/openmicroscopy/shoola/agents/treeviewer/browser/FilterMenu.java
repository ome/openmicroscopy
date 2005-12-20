/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.FilterMenu
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;


//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class FilterMenu
    extends JPopupMenu
{

    
    /** Button to retrieve the images in datasets. */
    private JMenuItem   dataset;
    
    /** Button to retrieve the images in categories. */
    private JMenuItem   category;
    
    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
        item.setBorder(null);
    }
    
    /**
     * Creates the menu items with the given actions.
     * 
     * @param controller The Controller.
     */
    private void createMenuItems(BrowserControl controller)
    {
        dataset = new JMenuItem(
                            controller.getAction(Browser.FILTER_IN_DATASET));
        initMenuItem(dataset);
        category = new JMenuItem(
                controller.getAction(Browser.FILTER_IN_CATEGORY));
        initMenuItem(category);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(dataset);
        add(category);
    }
    
    /** 
     * Creates a new instance.
     *
     * @param controller The Controller. Mustn't be <code>null</code>.
     */
    FilterMenu(BrowserControl controller)
    {
        if (controller == null) 
            throw new IllegalArgumentException("No control.");
        createMenuItems(controller);
        buildGUI() ;
    }
    
}
