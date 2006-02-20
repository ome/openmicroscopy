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
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;


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
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE = 3;
    
    /** Flag to denote the <i>Loading Thumbnail</i> state. */
    public static final int         LOADING_THUMBNAIL = 4;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 5;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_EDITOR = 100;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         PROPERTIES_EDITOR = 101;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         CLASSIFIER_EDITOR = 102;
    
    /** Identifies the <code>No Editor</code> type for the editor. */
    public static final int         NO_EDITOR = 103;
    
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
    

    
    /** Identifies the <code>Delete Object</code> operation. */
    public static final int         REMOVE_OBJECT = 302;

    
    /** Bounds property to indicate that the data retrieval is cancelled. */
    public static final String      CANCEL_LOADING_PROPERTY = "cancelLoading";
    
    /** Bounds property to indicate to load a thumbnail for a given image. */
    public static final String      THUMBNAIL_LOADING_PROPERTY = 
                                                    "thumbnailLoading";
    
    /** Bounds property to indicate that the thumbanil is loaded. */
    public static final String      THUMBNAIL_LOADED_PROPERTY = 
                                                    "thumbnailLoaded";
    
    /** 
     * Bound properties indicating that {@link Browser} is selected or 
     * <code>null</code> if no there is no {@link Browser} currently selected.
     * 
     */
    public static final String		SELECTED_BROWSER_PROPERTY = 
        									"selectedBrowser";
    /**
     * Bound properties to indicate to remove the currently displayed editor.
     */
    public static final String      REMOVE_EDITOR_PROPERTY = "removeEditor";
    
    /** Bounds property to indicate that the data object has been saved. */
    public static final String      SAVE_EDITION_PROPERTY = "saveEdition";
    
    /** 
     * Bound property name indicating to show/hide the component from the
     * display.
     */
    public static final String      FINDER_VISIBLE_PROPERTY = "finderVisible";
    
    /** 
     * The title displayed in the {@link LoadingWindow} during the saving 
     * process.
     */
    public static final String      SAVING_TITLE = "Saving Data";
    
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
    
    /**
     * Retrieves the thumbnail for the specified image.
     * 
     * @param image The image the thumbnail is for.
     */
    void retrieveThumbnail(ImageData image);
    
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
     * Removes the specified {@link DataObject}.
     * 
     * @param object The {@link DataObject} to save.
     */
    public void removeObject(DataObject object);
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /** Removes the displayed editor from the panel. */
    public void removeEditor();
    
    /** 
     * Sets the results of a delete action.
     * 
     * @param object    The <code>DataObject</code> to update or to delete.
     * @param op        The type of operation performed, either
     *                  {@link #REMOVE_OBJECT}.
     */
    public void setSaveResult(DataObject object, int op);
    
    /**
     * Returns the user's details. Helper method
     * 
     * @return See above.
     */
    public ExperimenterData getUserDetails();
    
    /**
     * Shows if the passed parameter is <code>true</code>, hides
     * otherwise.
     * 
     * @param b <code>true</code> to show the component, <code>false</code>
     * 			to hide.
     */
    public void showFinder(boolean b);
    
    /** Hides the window and cancels any on-going data loading. */
    public void closeWindow();
    
    /**
     * Brings up the classifier corresponding to the specified mode.
     * 
     * @param object The image object to classify or declassify.
     * @param mode The type of classifier. 
     */
    public void showClassifier(ImageData object, int mode);

    /**
     * Returns the dialog indicating a data retrieval.
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JDialog getLoadingWindow();

    /**
     * Sets the thumbnail associated to the currently edited Image.
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail); 
    
    /**
     * Reacts to a node selection in the currently selected {@link Browser}.
     */
    public void onSelectedDisplay();
    
    /**
     * Updates the views when the data object is save.
     * The method only supports map of size one. 
     * The key is one the following constants: 
     * <code>CREATE_OBJECT</code>, <code>UPDATE_OBJECT</code> or
     * <code>REMOVE_OBJECT</code>.
     * The value is the <code>DataObject</code> created, removed or updated.
     * 
     * @param map The map to handle.
     */
    public void onDataObjectSave(Map map);
    
    /** Clears the result of a previous find action. */
    public void clearFoundResults();
    
}
