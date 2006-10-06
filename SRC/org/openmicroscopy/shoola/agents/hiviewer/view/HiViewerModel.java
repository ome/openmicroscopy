/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerModel
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataObjectSaver;
import org.openmicroscopy.shoola.agents.hiviewer.HiTranslator;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailsManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardFactory;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.IconsVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.layout.Layout;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * The Model component in the <code>HiViewer</code> MVC triad.
 * This class tracks the <code>HiViewer</code>'s state and knows how to
 * initiate data retrievals.  It also knows how to store and manipulate
 * the results.  However, this class doesn't know the actual hierarchy
 * the <code>HiViewer</code> is for.  Subclasses fill this gap and provide  
 * a suitable data loader.  The {@link HiViewerComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
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
abstract class HiViewerModel
{
    
    /** 
     * The level of the root either <code>GroupData</code> or 
     * <code>ExperimenterData</code>.
     */
    private Class               rootLevel;
    
    /** The id of the group the user selects before data retrieval. */
    private long                groupID;

    /** Holds one of the state flags defined by {@link HiViewer}. */
    private int                 state;
    
    /** The sub-component that hosts the display. */
    private Browser             browser;
    
    /** The sub-component that controls the display. */
    private ClipBoard           clipBoard;
    
    /** 
     * The sub-component that presents a tree view of the data displayed
     * in the <code>Browser</code>.
     */
    private TreeView			treeView;
    
    /** 
     * Will either be a hierarchy loader, a thumbnail loader, or 
     * <code>null</code> depending on the current state. 
     */
    private DataLoader          currentLoader;
    
    /** Maps an image id to the list of thumbnail providers for that image. */
    private ThumbnailsManager   thumbsManager;
    
    /** Reference to the component that embeds this model. */
    protected HiViewer          component;
    
    
    /** Creates a new object and sets its state to {@link HiViewer#NEW}. */
    protected HiViewerModel() { state = HiViewer.NEW; }
    
    /**
     * Called by the <code>HiViewer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(HiViewer component) { this.component = component; }
    
    /**
     * Sets the user's details.
     * The details should, in theory be already binded to the agent's registry.
     * 
     * @param details The details to set.
     */
    void setUserDetails(ExperimenterData details)
    {
        HiViewerAgent.getRegistry().bind(LookupNames.CURRENT_USER_DETAILS,
                						details);
    }
    
    /**
     * Sets the root level and its id.
     * 
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The root ID.
     */
    void setRootLevel(Class rootLevel, long rootID)
    {
        this.rootLevel = rootLevel;
        this.groupID = rootID;
    }
    
    /**
     * Returns the level of the root. 
     * 
     * @return See above.
     */
    Class getRootLevel() { return rootLevel; }
    
    /**
     * Returns the ID of the root. 
     * 
     * @return See above.
     */
    long getRootID() { return groupID; }
    
    /**
     * Returns the current user's details.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails()
    { 
    	return (ExperimenterData) HiViewerAgent.getRegistry().lookup(
    			        LookupNames.CURRENT_USER_DETAILS);
    }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link HiViewer} interface.  
     */
    int getState() { return state; }
    
    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link HiViewer#LOADING_HIERARCHY}. 
     */
    void fireHierarchyLoading()
    {
        state = HiViewer.LOADING_HIERARCHY;
        currentLoader = createHierarchyLoader();
        currentLoader.load();
    }
    
    /**
     * Creates a {@link Browser} component to display the hierarchy trees
     * rooted by the specified root nodes.
     * The original hierarchy trees are mapped onto visualization trees.
     * 
     * @param roots The root nodes.
     * @param flat  Pass <code>false</code> if it's a true hierarchy, 
     *              <code>true</code> if it's a collection of images to browse.
     */
    void createBrowser(Set roots, boolean flat)
    {
        if (roots == null) throw new NullPointerException("No roots.");
        //Translate.
        Set visTrees; 
        //Check if the objects are readable.
        long userID = getUserDetails().getId();
        if (flat) visTrees = HiTranslator.transformImages(roots, userID, 
                                                        groupID);
        else visTrees = HiTranslator.transformHierarchy(roots, userID, groupID);
        //Make the browser.
        browser = BrowserFactory.createBrowser(visTrees);
        
        //Do initial layout and set the icons.
        Layout layout = LayoutFactory.getDefaultLayout();
        browser.setSelectedLayout(layout.getIndex());
        browser.accept(LayoutFactory.getDefaultLayout(),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
        browser.accept(new IconsVisitor(), ImageDisplayVisitor.IMAGE_SET_ONLY);
    }
    
    /**
     * Creates a {@link ClipBoard} component to manage the browsed
     * hierarchy trees.
     * 
     */
    void createClipBoard()
    {
        clipBoard = ClipBoardFactory.createClipBoard(component); 
    }
    
    /**
     * Returns the browser component that hosts the display.
     * 
     * @return The browser component or <code>null</code> if the state is
     *         {@link HiViewer#NEW} or {@link HiViewer#LOADING_HIERARCHY}.
     */
    Browser getBrowser() { return browser; }
    
    /**
     * Returns the clipBoard component that controls the display.
     * 
     * @return The clipBoard component or <code>null</code> if the state is
     *         {@link HiViewer#NEW} or {@link HiViewer#LOADING_HIERARCHY}.
     */
    ClipBoard getClipBoard() { return clipBoard; }
    
    /**
     * Returns the component that hosts a tree representation of the
     * data displayed in the <code>Browser</code>.
     * 
     * @return See above.
     */
    TreeView getTreeView() { return treeView; }
    
    /** Creates a new {@link TreeView}. */
    void createTreeView()
    {
        if (treeView == null) treeView = new TreeView(browser.getUI());
    }
    
    /**
     * Starts the asynchronous retrieval of the thumbnails needed for the
     * images in the display this model is for and sets the state to 
     * {@link HiViewer#LOADING_THUMBNAILS}. 
     */
    void fireThumbnailLoading()
    {
        Set images = browser.getImages();
        if (images.size() == 0) {
            state = HiViewer.READY;
            return;
        }
        state = HiViewer.LOADING_THUMBNAILS;
        currentLoader = new ThumbnailLoader(component, images);
        currentLoader.load();
    }
    
    /**
     * Starts the asynchronous update of the specified object.
     * 
     * @param object The object to update.
     */
    void fireDataObjectUpdate(DataObject object)
    {
        state = HiViewer.SAVING_DATA_OBJECT;
        currentLoader = new DataObjectSaver(component, object);
        currentLoader.load();
    }
    
    /**
     * Sets the specified thumbnail for all image nodes in the display that
     * map to the same image hierarchy object.
     * When every image object has a thumbnail, this method sets the state
     * to {@link HiViewer#READY}.
     * 
     * @param imageID The id of the hierarchy object to which the thumbnail 
     *                belongs.
     * @param thumb The thumbnail pixels.
     */
    void setThumbnail(long imageID, BufferedImage thumb)
    {
        if (thumbsManager == null) 
            thumbsManager = new ThumbnailsManager(browser.getImageNodes());
        thumbsManager.setThumbnail(imageID, thumb);
        if (thumbsManager.isDone()) {
            state = HiViewer.READY;
            thumbsManager = null;
        }
    }
    
    /**
     * Sets the object in the {@link HiViewer#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        state = HiViewer.DISCARDED;
    }
    
    /**
     * Sets the value of the <code>Roll over</code> flag.
     * 
     * @param rollOver  Pass <code>true</code> to zoom the image when the user
     *                  mouses over a{@link ImageNode}, <code>false</code> 
     *                  otherwise.
     * @see Browser#setRollOver(boolean)                 
     */
    void setRollOver(boolean rollOver)
    { 
        if (browser != null) browser.setRollOver(rollOver); 
    }
    
    /**
     * Returns <code>true</code> if the image is zoomed when the user mouses
     * over an {@link ImageNode},  <code>false</code> otherwise.
     * 
     * @return See above.
     * @see Browser#isRollOver()
     */
    boolean isRollOver()
    { 
        if (browser == null) return false;
        return browser.isRollOver(); 
    }
    
    /**
     * Returns <code>true</code> if the title bar of the {@link ImageNode}s
     * is visible, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isTitleBarVisible()
    {
        if (browser == null) return true;
        return browser.isTitleBarVisible();
    }
    
    /**
     * Indicates what kind of hierarchy this model is for.
     * 
     * @return One of the hierarchy flags defined by the {@link HiViewer} 
     *         interface.
     */
    protected abstract int getHierarchyType();
    
    /**
     * Compares another model to this one to tell if they would result in
     * having the same display.
     *  
     * @param other The other model to compare.
     * @return <code>true</code> if <code>other</code> would lead to a viewer
     *          with the same display as the one in which this model belongs;
     *          <code>false</code> otherwise.
     */
    protected abstract boolean isSameDisplay(HiViewerModel other);
    
    /**
     * Creates a data loader that can retrieve the hierarchy objects needed
     * by this model.
     * 
     * @return A suitable data loader.
     */
    protected abstract DataLoader createHierarchyLoader();
    
    /**
     * Creates a new Model from this one.
     * This method creates a new object of the same concrete type as this
     * one.  Subclasses have to clone their state (typically just ids) and
     * make a new instance, which will then be in the {@link HiViewer#NEW}
     * state.
     * 
     * @return A new Model created after this one.
     */
    protected abstract HiViewerModel reinstantiate();
    
}
