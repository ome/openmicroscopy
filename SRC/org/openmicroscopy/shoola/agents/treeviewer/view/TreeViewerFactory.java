/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 *  Factory to create {@link TreeViewer} component.
 * This class keeps track of the {@link TreeViewer} instance that has been 
 * created and is not yet in the {@link TreeViewer#DISCARDED} state.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeViewerFactory
    implements ChangeListener
{

    /** The sole instance. */
    private static final TreeViewerFactory  singleton = new TreeViewerFactory();
    
    /**
     * Returns the {@link TreeViewer}.
     * 
     * @return See above.
     */
    public static TreeViewer getViewer() { return singleton.getTreeViewer(); }
    
    /** The tracked component. */
    private TreeViewer viewer;
    
    /** Creates a new instance. */
    private TreeViewerFactory()
    {
        viewer = null;
    }
    
    /**
     * Creates or recycles a viewer component for the specified 
     * <code>model</code>.
     * 
     * @return A {@link TreeViewer}.
     */
    private TreeViewer getTreeViewer()
    {
        if (viewer != null) return viewer;
        TreeViewerModel model = new TreeViewerModel();
        TreeViewerComponent component = new TreeViewerComponent(model);
        model.initialize(component);
        component.initialize();
        viewer = component;
        return component;
    }

    /**
     * Sets the {@link #viewer} to <code>null</code> when it is
     * {@link TreeViewer#DISCARDED discarded}. 
     */ 
    public void stateChanged(ChangeEvent ce)
    {
        TreeViewerComponent comp = (TreeViewerComponent) ce.getSource();
        if (comp.getState() == TreeViewer.DISCARDED) viewer = null;
    }
    
}
