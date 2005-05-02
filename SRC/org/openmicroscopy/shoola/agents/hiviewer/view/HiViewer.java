/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer
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
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.ObservableComponent;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;

/** 
 * Defines the interface provided by the hierarchy viewer component.
 * The hierarchy viewer provides a top-level window to host a hierarchy display
 * and let the user interact with it.  A hierarchy display is a screen with
 * one or more visualization trees, all of the same kind.  A visualization tree
 * is a graphical tree that represents objects in a given <i>OME</i> hierarchy,
 * like Project/Dataset/Image or Category Group/Category/Image.  Two such trees
 * are said to be of the same kind if they represent objects which belong in 
 * the same logical hierarchy.
 * <p>The typical life-cycle of a hierarchy viewer is as follows.  The object
 * is first created using the {@link HiViewerFactory} and specifying what kind
 * of hierarchy the viewer is for along with the root nodes to load.  After
 * creation the object is in the {@link #NEW} state and is waiting for the
 * {@link #activate() activate} method to be called.  Such a call triggers the
 * retrieval of all the <i>OME</i> objects of the specified hierarchy kind that
 * are rooted by the specified nodes.  The object is now in the 
 * {@link #LOADING_HIERARCHY} state.  After all the nodes have been retrieved,
 * the hierarchy display is built and set on screen and the object automatically 
 * starts loading the thumbnails for all the images in the display, which makes
 * it transition to the {@link #LOADING_THUMBNAILS} state.  When all thumbnails
 * have been downloaded, the object is {@link #READY} for interacting with the
 * user.  (The viewer allows the user to interact with it even before the
 * {@link #READY} state is reached, as long as the data required for the
 * interaction is already in memory.)  When the user quits the window, the
 * {@link #discard() discard} method is invoked and the object transitions to
 * the {@link #DISCARDED} state.  At which point, all clients should 
 * de-reference the component to allow for garbage collection.</p>
 *
 * @see Browser
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
public interface HiViewer
    extends ObservableComponent
{
    
    /** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading Hierarchy</i> state. */
    public static final int     LOADING_HIERARCHY = 2;
    
    /** Flag to denote the <i>Loading Thumbnails</i> state. */
    public static final int     LOADING_THUMBNAILS = 3;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 4;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 5;
    
    /** 
     * Flag to denote a Project/Dataset/Image hierarchy rooted by a given
     * Project. 
     */
    public static final int     PROJECT_HIERARCHY = 101;
    
    /** 
     * Flag to denote a Project/Dataset/Image hierarchy rooted by a given
     * Dataset. 
     */
    public static final int     DATASET_HIERARCHY = 102;
    
    /** 
     * Flag to denote a Category Group/Category/Image hierarchy rooted by a 
     * given Category Group. 
     */
    public static final int     CATEGORY_GROUP_HIERARCHY = 103;
    
    /** 
     * Flag to denote a Category Group/Category/Image hierarchy rooted by a 
     * given Category. 
     */
    public static final int     CATEGORY_HIERARCHY = 104;
    
    /** 
     * Flag to denote a Project/Dataset/Image hierarchy which contains a
     * given set of images. 
     */
    public static final int     PDI_HIERARCHY = 105;
    
    /** 
     * Flag to denote a Category Group/Category/Image hierarchy which contains
     * a given set of images. 
     */
    public static final int     CGCI_HIERARCHY = 106;
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Indicates what kind of hierarchy the viewer is displaying.
     * 
     * @return One of the hierarchy flags defined by this interface.
     */
    public int getHierarchyType();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Callback used by a data loader to set the root nodes of the retrieved 
     * hierarchy.
     * 
     * @param roots The root nodes.
     * @throws IllegalStateException If the current state is not
     *                               {@link #LOADING_HIERARCHY}.
     * @see org.openmicroscopy.shoola.agents.hiviewer.DataLoader
     */
    public void setHierarchyRoots(Set roots);
    
    /**
     * Callback used by a data loader to set thumbnails as they are retrieved.
     * 
     * @param imageID The id of the image to which the thumbnail belongs.
     * @param thumb The thumbnail pixels.
     * @see org.openmicroscopy.shoola.agents.hiviewer.DataLoader
     */
    public void setThumbnail(int imageID, BufferedImage thumb);
    
    /**
     * Callback used by data loaders to provide the viewer with feedback about
     * the data retrieval.
     * 
     * @param description Textual description of the ongoing operation.
     * @param perc Percentage of the total work done.  If negative, it is
     *             interpreted as not available.
     * @see org.openmicroscopy.shoola.agents.hiviewer.DataLoader
     */
    public void setStatus(String description, int perc);
    
    /**
     * Returns the {@link Browser} component that the viewer embeds to
     * display visualization trees.
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is not
     *                               {@link #LOADING_THUMBNAILS} nor 
     *                               {@link #READY}.
     */
    public Browser getBrowser();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** Returns the UI component. */
    public JFrame getUI();
    
    /** Returns the title of the hiViewer. */
    public String getViewTitle();
    
    public void setTitleBarVisible(boolean b);
    
    public boolean isTitleBarVisible();
    
}
