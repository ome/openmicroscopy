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
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;


/** 
 * Defines the interface provided by the tree viewer component.
 * The tree viewer provides a top-level window to host different types of
 * hierarchy display and let the user interact with it.
 * A display is a view with a visualization tree. A visualization tree
 * is a graphical tree that represents objects in a given  <i>OME</i> hierarchy,
 * like Project/Dataset/Image, Category Group/Category/Image or simply Images.
 * The component follows the <code>Lazy loading rule</code> i.e. the leaves 
 * of a given hierarchy are only retrieved if the parent is selected.
 * In practise, this means that we only display a Project/Dataset hierarchy
 * if a given Dataset is selected,  then the images in this Dataset are 
 * retrieved.
 * <p>The typical life-cycle of a tree viewer is as follows. The object
 * is first created using the {@link TreeViewerFactory}, the
 * {@link Browser}s hosting a hierarchy view are created. After
 * creation the object is in the {@link #NEW} state and is waiting for the
 * {@link #activate() activate} method to be called.
 * The data retrieval happens in the {@link Browser}.
 * 
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 * 
 * </p>
 *
 * @see Browser
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
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Create</i> state. */
    public static final int         SAVE = 3;
    
    /** Flag to denote the <i>Loading Details</i> state. */
    public static final int			LOADING_DETAILS = 4;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 5;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_PROPERTIES = 100;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         EDIT_PROPERTIES = 101;
    
    /** Identifies the <code>No Editor</code> type for the editor. */
    public static final int         NO_EDITOR = 102;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is the 
     * <code>World</code>. 
     */
    public static final int			WORLD_ROOT = 200;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is an OME 
     * <code>Group</code>.
     */
    public static final int			GROUP_ROOT = 201;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is a
     * <code>User</code>. 
     */
    public static final int			USER_ROOT = 202;
    
    /** Identifies the Properties action in the Actions menu. */
    public static final Integer     PROPERTIES = new Integer(0);
    
    /** Identifies the View action in the Actions menu. */
    public static final Integer     VIEW = new Integer(1);
    
    /** Identifies the Refresh action in the Actions menu. */
    public static final Integer     REFRESH = new Integer(2);
    
    /** Identifies the Create object action in the Actions menu. */
    public static final Integer     CREATE_OBJECT = new Integer(3);
    
    /** Identifies the Copy object action in the Actions menu. */
    public static final Integer     COPY_OBJECT = new Integer(4);
    
    /** Identifies the Paste object action in the Actions menu. */
    public static final Integer     PASTE_OBJECT = new Integer(5);
    
    /** Identifies the Delete object action in the Actions menu. */
    public static final Integer     DELETE_OBJECT = new Integer(6);
    
    /** Identifies the Hierarchy Explorer action in the Views menu. */
    public static final Integer     HIERARCHY_EXPLORER = new Integer(7);
    
    /** Identifies the Category Explorer action in the Views menu. */
    public static final Integer     CATEGORY_EXPLORER = new Integer(8);
    
    /** Identifies the Images Explorer action in the Views menu. */
    public static final Integer     IMAGES_EXPLORER = new Integer(9);
    
    /** Identifies the World root level action in the Hierarchy*/
    public static final Integer		WORLD_ROOT_LEVEL = new Integer(10);
    
    /** Identifies the User root level action in the Hierarchy*/
    public static final Integer		USER_ROOT_LEVEL = new Integer(11);
    
    /** Bound property indicating that the user's details have been loaded. */
    public static final String		DETAILS_LOADED_PROPERTY = "detailsLoaded";
    
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
     * Brings up the editor corresponding to the specified type ang object.
     * 
     * @param object The {@link DataObject} to edit or create.
     * @param editorType The type of editor.
     */
    public void showProperties(DataObject object, int editorType);
    
    /**
     * Saves the specified {@link DataObject} according to the 
     * type of editor.
     * 
     * @param object The {@link DataObject} to save.
     */
    public void saveObject(DataObject object);
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /** Removes the displayed editor from the panel. */
    public void removeEditor();
    
    public void setSaveResult(DataObject object);
    
    /**
     * Sets the user details.
     * 
     * @param details The details to set.
     */
    public void setUserDetails(UserDetails details);
    
    /**
     * Returns the user's details.
     * 
     * @return See above.
     */
    public UserDetails getUserDetails();
    
    /**
     * Returns the currently selected {@link Browser} or <code>null</code>
     * if no {@link Browser} is selected.
     * 
     * @return See above.
     */
    Browser getSelectedBrowser();
    
    /**
     * Sets the currently selected {@link Browser} or <code>null</code>
     * if no {@link Browser} is selected.
     * 
     * @param browser The currently selected {@link Browser}.
     */
    void setSelectedBrowser(Browser browser);
    
}
