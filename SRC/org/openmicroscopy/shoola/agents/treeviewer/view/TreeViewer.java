/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.DataObject;

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
public interface TreeViewer
    extends ObservableComponent
{

    /** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 2;
    
    /** Flag to denote the <i>Create</i> state. */
    public static final int     CREATE = 3;
    
    /** Identifies the Properties action in the Actions menu. */
    public static final Integer     PROPERTIES = new Integer(0);
    
    /** Identifies the View action in the Actions menu. */
    public static final Integer     VIEW = new Integer(1);
    
    /** Identifies the Refresh action in the Hierarchy menu. */
    public static final Integer     REFRESH = new Integer(2);
    
    /** Identifies the New object action in the Hierarchy menu. */
    public static final Integer     NEW_OBJECT = new Integer(3);
    
    /** Identifies the Hierarchy Explorer action in the Hierarchy menu. */
    public static final Integer     HIERARCHY_EXPLORER = new Integer(4);
    
    /** Identifies the Category Explorer action in the Hierarchy menu. */
    public static final Integer     CATEGORY_EXPLORER = new Integer(5);
    
    /** Identifies the Images Explorer action in the Hierarchy menu. */
    public static final Integer     IMAGES_EXPLORER = new Integer(6);
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Starts the initialization sequence when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Returns the available {@link Browser}s.
     * 
     * @return See above.
     */
    public Map getBrowsers();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** 
     * Adds the {@link Browser} corresponding to the specified type to
     * the display.
     * 
     * @param browserType The browser's type.
     */
    public void addBrowser(int browserType);
    
    /**
     * Creates a new <code>DataObject</code> corresponding to the 
     * specified type.
     * 
     * @param doType
     */
    public void createDataObject(Class doType);
    
    /** 
     * Creates the specified <code>DataObject</code>.
     * 
     * @param object The object to create.
     */
    public void createObject(DataObject object);
    
    /**
     * Returns the currently selected {@link Browser} or <code>null</code>
     * if no {@link Browser} is selected.
     * 
     * @return See above.
     */
    Browser getSelectedBrowser();
    
}
