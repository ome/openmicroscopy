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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectRemover;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.env.LookupNames;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;


/** 
 * The Model component in the <code>TreeViewer</code> MVC triad.
 * This class tracks the <code>TreeViewer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link TreeViewerComponent} intercepts the 
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
     * The level of the hierarchy root. One of the following constants:
     * {@link TreeViewer#GROUP_ROOT} or {@link TreeViewer#USER_ROOT}.
     */
    private int                     rootLevel;
    
    /** 
     * The ID of the root. This parameter will be used only when the 
     * {@link #rootLevel} is {@link TreeViewer#GROUP_ROOT}.
     */
    private long                    rootGroupID;
    
    /**
     * The component to find a given phrase in the currently selected
     * {@link Browser}.
     */
    private Finder                  finder;
    
    /** The collection of nodes to copy. */
    private TreeImageDisplay[]      nodesToCopy;
    
    /** 
     * Either {@link TreeViewer#COPY_AND_PASTE} or 
     * {@link TreeViewer#CUT_AND_PASTE}. 
     */
    private int                     copyIndex;
    
    /** Reference to the component that embeds this model. */
    protected TreeViewer            component;
    
    /**
     * Builds the map linking the nodes to copy and the parents.
     * 
     * @param parents The parents of the node to copy.
     * @return See above.
     */
    private Map buildCopyMap(TreeImageDisplay[] parents)
    {
        Object uo = nodesToCopy[0].getUserObject();
        Object uoParent = parents[0].getUserObject();
        if (!(uo instanceof DataObject)) return null;
        if (!(uoParent instanceof DataObject)) return null;
        DataObject obj = (DataObject) uo;
        DataObject objParent = (DataObject) uoParent;

        if (((objParent instanceof ProjectData) &&
             (obj instanceof DatasetData)) ||  
             ((objParent instanceof CategoryGroupData) &&
             (obj instanceof CategoryData)) || 
             ((objParent instanceof CategoryData) && 
                     (obj instanceof ImageData)) || 
             ((objParent instanceof DatasetData) && (obj instanceof ImageData)))
        {
            Map map;
            Set children;
            map = new HashMap(parents.length);
            for (int i = 0; i < parents.length; i++) {
                children = new HashSet(nodesToCopy.length);
                for (int j = 0; j < nodesToCopy.length; j++) {
                    children.add(nodesToCopy[j].getUserObject());
                }
                map.put(parents[i].getUserObject(), children);
            }
            return map;
        } 
        return null;
    }
    
    private Map buildCutMap(TreeImageDisplay[] nodes)
    {
        TreeImageDisplay parent, child;
        Map map = new HashMap();
        Object po;
        List children;
        for (int i = 0; i < nodes.length; i++) {
            child = nodes[i];
            parent = child.getParentDisplay();
            po = parent.getUserObject();
            children = (List) map.get(po);
            if (children == null) {
                children = new ArrayList();   
                map.put(po, children);
            }
            children.add(nodes[i].getUserObject());   
        }
        return map;
    }
    
    /** Creates the browsers controlled by this model. */
    private void createBrowsers()
    {
        Browser browser = 
                BrowserFactory.createBrowser(Browser.PROJECT_EXPLORER, 
                        component);
        selectedBrowser = browser;
        browser.setSelected(true);
        browsers.put(new Integer(Browser.PROJECT_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.CATEGORY_EXPLORER,
                                                component);
        browsers.put(new Integer(Browser.CATEGORY_EXPLORER), browser);
        browser = BrowserFactory.createBrowser(Browser.IMAGES_EXPLORER,
                                                component);
        browsers.put(new Integer(Browser.IMAGES_EXPLORER), browser);
    }
    
    /**
     * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
     */
    protected TreeViewerModel()
    {
        state = TreeViewer.NEW;
        editorType = TreeViewer.PROPERTIES_EDITOR;
        rootLevel = TreeViewer.USER_ROOT;
        browsers = new HashMap();
    }
    
    /**
     * Called by the <code>TreeViewer</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(TreeViewer component)
    { 
        this.component = component; 
        createBrowsers();
    }
    
    /**
     * Sets the root of the retrieved hierarchies. 
     * The rootID is taken into account if and only if 
     * the passed <code>rootLevel</code> is {@link TreeViewer#GROUP_ROOT}.
     * 
     * @param rootLevel The level of the root. One of the following constants:
     *                  {@link TreeViewer#GROUP_ROOT} or
     *                  {@link TreeViewer#USER_ROOT}.
     * @param rootID    The Id of the root.
     */
    void setHierarchyRoot(int rootLevel, long rootID)
    {
        this.rootLevel = rootLevel;
        this.rootGroupID = rootID;
    }
    
    /**
     * Returns the level of the root. 
     * One of the following constants: 
     * {@link TreeViewer#GROUP_ROOT} or {@link TreeViewer#USER_ROOT}.
     * 
     * @return See above.
     */
    int getRootLevel() { return rootLevel; }
    
    /** 
     * Returns the root ID.
     * 
     * @return See above.
     */
    long getRootGroupID() { return rootGroupID; }
    
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
    * Starts the asynchronous removal of the data 
    * and sets the state to {@link TreeViewer#SAVE}.
    * 
    * @param node The node hosting the <code>DataObject</code> to remove.
    */
   void fireDataObjectsDeletion(TreeImageDisplay node)
   {
       state = TreeViewer.SAVE;
       TreeImageDisplay parent = node.getParentDisplay();
       DataObject object = (DataObject) node.getUserObject();
       Object po = parent.getUserObject();
       DataObject data = null;
       if (!((object instanceof ProjectData) || 
               (object instanceof CategoryGroupData)))//root.
           data = ((DataObject) po);
       List l = new ArrayList(1);
       l.add(object);
       currentLoader = new DataObjectRemover(component, l, data);
       currentLoader.load();
   }
   
   /**
    * Starts the asynchronous removal of the data and sets the state to 
    * {@link TreeViewer#SAVE}.
    * This method should be invoked to remove a collection of nodes of the
    * same type.
    * 
    * @param nodes The nodes to remove.
    */
   void fireDataObjectsDeletion(List nodes)
   {
       state = TreeViewer.SAVE;
       DataObject object, po;
       Iterator i = nodes.iterator();
       TreeImageDisplay n, parent;
       Map map = null;
       List toRemove = null;  
       List l;
       while (i.hasNext()) {
           n = (TreeImageDisplay) i.next();
           parent = n.getParentDisplay();
           object = (DataObject) n.getUserObject();
           if ((object instanceof ProjectData) || 
                   (object instanceof CategoryGroupData)) {
               if (toRemove == null) toRemove = new ArrayList();
               toRemove.add(object);
           } else {
               po = (DataObject) parent.getUserObject();
               if (map == null) map = new HashMap();
               l = (List) map.get(po);
               if (l == null) l = new ArrayList();
               l.add(object);
               map.put(po, l);
           }
       }
       if (toRemove != null) {
           currentLoader = new DataObjectRemover(component, toRemove, null);
       } else { 
           currentLoader = new DataObjectRemover(component, map);
       }
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

   /**
    * Loads existing objects that can be added to the specified 
    * <code>DataObject</code>.
    * 
    * @param ho The node to add the objects to.
    */
   void fireDataExistingObjectsLoader(DataObject ho)
   {
       state = TreeViewer.LOADING_DATA;
       currentLoader = new ExistingObjectsLoader(component, ho);
       currentLoader.load();
   }

   /**
    * Fires an asynchronous call to add the specified children to the
    * the currently selected parent.
    * 
    * @param children   The children to add.
    */
   void fireAddExistingObjects(Set children)
   {
       TreeImageDisplay parent = selectedBrowser.getLastSelectedDisplay();
       if (parent == null) return;
       Object po = parent.getUserObject();
       if ((po instanceof ProjectData) || ((po instanceof DatasetData)) ||
            (po instanceof CategoryGroupData)) {
           currentLoader = new ExistingObjectsSaver(component, 
                               (DataObject) po, children);
           currentLoader.load();
       }
       state = TreeViewer.READY;
   }

   /**
    * Sets the collection of nodes to copy. 
    * 
    * @param nodes  The nodes to copy.
    * @param index  The action index.
    */
   void setNodesToCopy(TreeImageDisplay[] nodes, int index)
   {
       copyIndex = index;
       nodesToCopy  = nodes;
   }
   
   /**
    * Returns the nodes to copy.
    * 
    * @return See above.
    */
   TreeImageDisplay[] getNodesToCopy() { return nodesToCopy; }
   
   /**
    * Copies and pastes the nodes. Returns <code>true</code> if we can perform
    * the operation according to the selected nodes, <code>false</code>
    * otherwise.
    * 
    * @param parents The parents of the nodes to copy.
    * @return See above
    */
   boolean paste(TreeImageDisplay[] parents)
   {
       Map map = buildCopyMap(parents);
       if (map == null) return false;
       if (copyIndex == TreeViewer.COPY_AND_PASTE)
           currentLoader = new DataObjectUpdater(component, map, 
                       DataObjectUpdater.COPY_AND_PASTE);
       else if (copyIndex == TreeViewer.CUT_AND_PASTE) {
           Map toRemove = buildCutMap(nodesToCopy);
           currentLoader = new DataObjectUpdater(component, map, toRemove,
                   DataObjectUpdater.CUT_AND_PASTE);
       }
       
       currentLoader.load();
       state = TreeViewer.SAVE;
       nodesToCopy = null;
       return true;
   }

}
