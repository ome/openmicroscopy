/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
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
import java.util.HashMap;
import java.util.Map;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;


/** 
 * The Model component in the <code>TreeViewer</code> MVC triad.
 * This class tracks the <code>TreeViewer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. However, this class doesn't know the actual hierarchy
 * the <code>TreeViewer</code> is for. Subclasses fill this gap and provide  
 * a suitable data loader. The {@link TreeViewerComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeViewerModel
{

    /** Holds one of the state flags defined by {@link TreeViewer}. */
    private int                 	state;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private DataTreeViewerLoader	currentLoader;
    
    /** The browsers controlled by the model. */
    private Map                 	browsers;
    
    /** The currently selected {@link Browser}. */
    private Browser             	selectedBrowser;
    
    /** 
     * The type of editor. One of the following constants:
     * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR}
     * or {@link TreeViewer#NO_EDITOR}.
     */
    private int                 	editorType;
    
    /**
     * The component to find a given phrase in the currently selected
     * {@link Browser}.
     */
    private Finder					finder;
    
    /** Reference to the component that embeds this model. */
    protected TreeViewer        	component;
    
    /** Creates the browsers controlled by this model. */
    private void createBrowsers()
    {
        Browser browser = 
                BrowserFactory.createBrowser(Browser.HIERARCHY_EXPLORER);
        selectedBrowser = browser;
        browser.setSelected(true);
        browser.setHierarchyRoot(TreeViewer.USER_ROOT, -1);
        browsers.put(new Integer(Browser.HIERARCHY_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.CATEGORY_EXPLORER);
        browser.setHierarchyRoot(TreeViewer.USER_ROOT, -1);
        browsers.put(new Integer(Browser.CATEGORY_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.IMAGES_EXPLORER);
        browser.setHierarchyRoot(TreeViewer.USER_ROOT, -1);
        browsers.put(new Integer(Browser.IMAGES_EXPLORER), browser);
    }
    
    /**
     * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
     */
    protected TreeViewerModel()
    {
        state = TreeViewer.NEW;
        editorType = TreeViewer.NO_EDITOR;
        browsers = new HashMap();
        createBrowsers();
    }
    
    /**
     * Called by the <code>TreeViewer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(TreeViewer component) { this.component = component; }
    
    /**
     * Sets the currently selected {@link Browser}.
     * 
     * @param browser The currently selected {@link Browser}.
     */
    void setSelectedBrowser(Browser browser) { selectedBrowser =  browser; }
    
    /**
     * Returns the selected {@link Browser}.
     * 
     * @return See above.
     */
    Browser getSelectedBrowser() { return selectedBrowser; }
    
    /**
     * Returns the browsers.
     * 
     * @return See above.
     */
    Map getBrowsers() { return browsers; }

    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link TreeViewer} interface.  
     */
   int getState() { return state; }    
   
   /**
    * Sets the object in the {@link TreeViewer#DISCARDED} state.
    * Any ongoing data loading will be cancelled.
    */
   void discard()
   {
       cancel();
       state = TreeViewer.DISCARDED;
   }
   
   /**
    * Sets the object in the {@link TreeViewer#READY} state.
    * Any ongoing data loading will be cancelled.
    */
   void cancel()
   {
       if (currentLoader != null) {
           currentLoader.cancel();
           currentLoader = null;
       }
       state = TreeViewer.READY;
   }
   
   /**
    * Starts the asynchronous creation of the data 
    * and sets the state to {@link TreeViewer#SAVE}.
    * 
    * @param userObject The <code>DataObject</code> to remove.
    */
   void fireDataObjectDeletion(DataObject userObject)
   {
       state = TreeViewer.SAVE;
       TreeImageDisplay parent = 
           selectedBrowser.getSelectedDisplay().getParentDisplay();
       Object po = parent.getUserObject();
       if (po instanceof String) //root.
           currentLoader = new DataObjectRemover(component, userObject, null);
       else 
           currentLoader = new DataObjectRemover(component, userObject, 
                                                 (DataObject) po);
       currentLoader.load();
   }
   
   /**
    * Fires an asynchronous thumbnail retrieval for the specified image.
    * 
    * @param image The image the thumbnail is for.
    */
   void fireThumbnailLoading(ImageData image)
   {
       state = TreeViewer.LOADING_THUMBNAIL;
       currentLoader = new ThumbnailLoader(component, image);
       currentLoader.load();
   }
   
   /**
    * Sets the type of editor. One of the following constants 
    * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR},
    * {@link TreeViewer#CLASSIFIER_EDITOR} or {@link TreeViewer#NO_EDITOR}.
    * 
    * @param editorType The type of the editor.
    */
   void setEditorType(int editorType) { this.editorType = editorType; }
   
   /**
    * Returns the type of editor.
    * One of the following constants 
    * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR},
    * {@link TreeViewer#CLASSIFIER_EDITOR} or {@link TreeViewer#NO_EDITOR}.
    * 
    * @return See above.
    */
   int getEditorType() { return editorType; }

   
   /** 
    * Returns the {@link Finder} component.
    * 
    * @return See above.
    */
   Finder getFinder() { return finder; }
   
   /** 
    * Sets the finder component.
    * 
    * @param finder The component to set.
    */
   void setFinder(Finder finder) { this.finder = finder; }
   
   /**
    * Returns the current user's details.
    * 
    * @return See above.
    */
   ExperimenterData getUserDetails()
   { 
       return (ExperimenterData) TreeViewerAgent.getRegistry().lookup(
   			        LookupNames.CURRENT_USER_DETAILS);
   }
   
   /**
    * Sets the current state.
    * 
    * @param state The state to set.
    */
   void setState(int state) { this.state = state; }
   
}
