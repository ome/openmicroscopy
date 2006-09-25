/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.ClassifyPopupMenu
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
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;

//Third-party libraries

//Application-internal dependencies

/** 
 * Menu brought up on screen when the user clicks on the <code>Classify</code>
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
class ClassifyPopupMenu
    extends JPopupMenu
{

    /** Reference to the control. */
    private TreeViewerControl controller;
    
    /** Item to classify the selected images. */
    private JMenuItem           classify;
    
    /** Item to declassify the selected images. */
    private JMenuItem           declassify;
    
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
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        classify = new JMenuItem(
                controller.getAction(TreeViewerControl.CLASSIFY));
        initMenuItem(classify);
        declassify = new JMenuItem(
                controller.getAction(TreeViewerControl.DECLASSIFY));
        initMenuItem(declassify);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(classify);
        add(declassify);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     */
    ClassifyPopupMenu(TreeViewerControl controller)
    {
        if (controller == null)
            throw new NullPointerException("No control.");
        this.controller = controller;
        initComponents();
        buildGUI();
    }
    
}
