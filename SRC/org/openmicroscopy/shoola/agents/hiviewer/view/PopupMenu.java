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
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerUIF;

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
class PopupMenu
    extends JPopupMenu
{
    
    /** 
     * Button to bring up the property sheet of an object (project, dataset,
     * categoryGroup, category or image).
     */
    JMenuItem                       properties;
    
    /** Button to view a project, dataset, categoryGroup, category or image. */
    JMenuItem                       view;

    /** Button to annotate a dataset or an image. */
    JMenuItem                       annotate;
    
    /** Zoom in a container with leaves as children. */
    JMenuItem                       zoomIn;
    
    /** Zoom out a container with leaves as children. */
    JMenuItem                       zoomOut;
    
    /** Zoom out a container with leaves as children. */
    JMenuItem                       classify;
    
    /** 
     * Creates a new instance.
     *
     * @param agentCtrl   The agent's control component.
     */
    PopupMenu(Action[] actions) 
    {
        initMenuItems(actions);
        buildGUI() ;
    }
    
    private void initMenuItems(Action[] actions)
    {
        view  = new JMenuItem(actions[HiViewerUIF.VIEW]);
        properties = new JMenuItem(actions[HiViewerUIF.PROPERTIES]);
        annotate = new JMenuItem(actions[HiViewerUIF.ANNOTATE]);
        classify = new JMenuItem(actions[HiViewerUIF.CLASSIFY]);
        zoomIn = new JMenuItem(actions[HiViewerUIF.ZOOM_IN]);
        zoomOut = new JMenuItem(actions[HiViewerUIF.ZOOM_OUT]);
    }

    /** Builds and lays out the GUI. */
    private void buildGUI() 
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(properties);
        add(classify);
        add(annotate);
        add(view);
        add(zoomIn);
        add(zoomOut);
    }
    
}
