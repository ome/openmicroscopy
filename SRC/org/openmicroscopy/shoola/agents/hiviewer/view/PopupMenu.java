/*
 * org.openmicroscopy.shoola.agents.hiviewer.controls.PopupMenu
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerUIF;

/** 
 * Pop-up menu for nodes in the browser display.
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
class PopupMenu
    extends JPopupMenu
{
    
    /** 
     * Button to bring up the property sheet of a hierarchy object &#151; 
     * project, dataset, category group, category, or image.
     */
    private JMenuItem   properties;
    
    /** Button to bring up the widget to annotate a dataset or an image. */
    private JMenuItem   annotate;
    
    /** Button to bring up the widget to classify an image. */
    private JMenuItem   classify;
    
    /** Button to browse a container or bring up the Viewer for an image. */
    private JMenuItem   view;
    
    /** Button to zoom in all leaf nodes in a container. */
    private JMenuItem   zoomIn;
    
    /** Button to zoom out all leaf nodes in a container. */
    private JMenuItem   zoomOut;
    
    /** Button to resize all leaf nodes in a container. */
    private JMenuItem   zoomFit;
    
    
    /**
     * Creates the menu items with the given actions.
     * 
     * @param actions The actions to build the menu items.
     */
    private void createMenuItems(Action[] actions)
    {
        properties = new JMenuItem(actions[HiViewerUIF.PROPERTIES]);
        annotate = new JMenuItem(actions[HiViewerUIF.ANNOTATE]);
        classify = new JMenuItem(actions[HiViewerUIF.CLASSIFY]);
        view = new JMenuItem(actions[HiViewerUIF.VIEW]);
        zoomIn = new JMenuItem(actions[HiViewerUIF.ZOOM_IN]);
        zoomOut = new JMenuItem(actions[HiViewerUIF.ZOOM_OUT]);
        zoomFit = new JMenuItem(actions[HiViewerUIF.ZOOM_FIT]);
    }

    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(annotate);
        add(classify);
        add(new JSeparator());
        add(view);
        add(zoomIn);
        add(zoomOut);
        add(zoomFit);
    }
    
    /** 
     * Creates a new instance.
     *
     * @param actions The actions to build the menu items.
     */
    PopupMenu(Action[] actions) 
    {
        createMenuItems(actions);
        buildGUI() ;
    }
    
}
