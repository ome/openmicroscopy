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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.AnnotationEditor;
import org.openmicroscopy.shoola.agents.treeviewer.AnnotationLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ClassificationPathsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ClassificationSaver;
import org.openmicroscopy.shoola.agents.treeviewer.ClassifierPathsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectEditor;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ObjectAnnotationEditor;
import org.openmicroscopy.shoola.agents.treeviewer.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.AnnotationData;
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
    
    /** The currently edited DataObject. */
    private DataObject              objectEdit;
    
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
    * and sets the state to {@link TreeViewer#SAVE_EDITION}.
    * 
    * @param userObject The <code>DataObject</code> to create.
    */
   void fireDataObjectCreation(DataObject userObject)
   {    
       state = TreeViewer.SAVE_EDITION;
       Object parent = selectedBrowser.getSelectedDisplay().getUserObject();
       if (parent instanceof String) parent = null;
       currentLoader = new DataObjectCreator(component, userObject, parent);
       currentLoader.load();
   }
   
   /**
    * Starts the asynchronous creation of the data 
    * and sets the state to {@link TreeViewer#SAVE_EDITION}.
    * 
    * @param userObject The <code>DataObject</code> to update or delete
    * 					depending on the algorithm.
    * @param algorithm	The type of operation to perform.
    */
   void fireDataObjectUpdate(DataObject userObject, int algorithm)
   {
       state = TreeViewer.SAVE_EDITION;
       if (algorithm == TreeViewer.UPDATE_OBJECT) 
           currentLoader = new DataObjectEditor(component, userObject);
       else if (algorithm == TreeViewer.DELETE_OBJECT)  {
           TreeImageDisplay parent = 
               selectedBrowser.getSelectedDisplay().getParentDisplay();
           Object po = parent.getUserObject();
           if (po instanceof String) po = null; //root.
           currentLoader = new DataObjectEditor(component, userObject, po);
       }
       currentLoader.load();
   }
   
   /**
    * Starts the asynchronous creation/update/deletion of the annotation data. 
    * and sets the state to {@link TreeViewer#SAVE_EDITION}.
    * 
    * @param annotation The annotation to handle.
    * @param operation	The type of operation to perform.
    */
   void fireAnnotationEdition(AnnotationData annotation, int operation)
   {
       int op = AnnotationEditor.CREATE;
       if (operation == TreeViewer.UPDATE_ANNOTATION)
           op = AnnotationEditor.UPDATE;
       else if (operation == TreeViewer.DELETE_ANNOTATION)
           op = AnnotationEditor.DELETE;
       DataObject obj = (DataObject) 
       				selectedBrowser.getSelectedDisplay().getUserObject();
       state = TreeViewer.SAVE_EDITION;
       currentLoader = new AnnotationEditor(component, obj, annotation, op);
       currentLoader.load();
   }
   
   /**
    * Starts the asynchronous update of the specifed {@link DataObject}
    * and create/update/delete operation for the specified annotation. 
    * and sets the state to {@link TreeViewer#SAVE_EDITION}.
    * 
    * @param object The {@link DataObject} to update.
    * @param data The {@link AnnotationData} to handle.
    * @param operation The operation to perform.
    */
   void fireDataObjectAndAnnotationEdition(DataObject object,
           		AnnotationData data, int operation)
   {
       int op = -1;
       if (operation == TreeViewer.CREATE_ANNOTATION)
           op = ObjectAnnotationEditor.CREATE;
       else if (operation == TreeViewer.UPDATE_ANNOTATION)
           op = ObjectAnnotationEditor.UPDATE;
       else if (operation == TreeViewer.DELETE_ANNOTATION)
           op = ObjectAnnotationEditor.DELETE;
       state = TreeViewer.SAVE_EDITION;
       currentLoader = new ObjectAnnotationEditor(component, object, data, op);
       currentLoader.load();
   }

   /**
    * Fires an asynchronous annotation retrieval for the specified
    * <code>DataObject</code>.
    * 
    * @param object The object to retrieve the annotations for.
    */
   void fireAnnotationLoading(DataObject object)
   {
       state = TreeViewer.LOADING_ANNOTATION;
       currentLoader = new AnnotationLoader(component, object);
       currentLoader.load();
   }
   
   /**
    * Sets the type of editor. One of the following constants 
    * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR}
    * or {@link TreeViewer#NO_EDITOR}.
    * 
    * @param editorType The type of the editor.
    */
   void setEditorType(int editorType) { this.editorType = editorType; }
   
   /**
    * Returns the type of editor.
    * One of the following constants 
    * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR}
    * or {@link TreeViewer#NO_EDITOR}.
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
    * Sets the currently edited DataObject.
    * 
    * @param object The object to set.
    */
   void setDataObject(DataObject object) { objectEdit = object; }
   
   /**
    * Returns the currently edited data object.
    * 
    * @return See above.
    */
   DataObject getDataObject() { return objectEdit; }
   
   /**
    * Fires an asynchronous thumbnail retrieval for the currently edited image.
    */
   void fireThumbnailLoading()
   {
       state = TreeViewer.LOADING_THUMBNAIL;
       currentLoader = new ThumbnailLoader(component, (ImageData) objectEdit);
       currentLoader.load();
   }
   
   /**
    * Fires an asynchronous retrieval of the CategoryGroup/Category paths 
    * containing the specified image.
    * 
    * @param imageID The id of the image.
    */
   void fireClassificationLoading(int imageID)
   {
        state = TreeViewer.LOADING_CLASSIFICATION;
        currentLoader = new ClassificationPathsLoader(component, imageID);
        currentLoader.load();
   }
   
   /**
    * Fires an asynchronous retrieval of the CategoryGroup/Category paths 
    * 
    * @param object The image to classify or declassify.
    * @param m      The type of classifier.
    */
   void fireClassificationPathsLoading(ImageData object, int m)
   {
       setDataObject(object);
       state = TreeViewer.LOADING_CLASSIFICATION_PATH;
       currentLoader = new ClassifierPathsLoader(component, object.getId(), m);
       currentLoader.load();
   }
   
   /**
    * Fires an asynchronous call to classify the currently selected image
    * in the specified categories.
    * 
    * @param categories Collection of selected categories.
    */
   void fireClassification(Map categories)
   {
       if (categories.size() == 1) {
           Iterator i = categories.keySet().iterator();
           ImageData data = null;
           Set set = null;
           while (i.hasNext()) {
               data = (ImageData) i.next();
               set = (Set) categories.get(data);
           }
           state = TreeViewer.SAVE_CLASSIFICATION;
           currentLoader = new ClassificationSaver(component, 
                           ClassificationSaver.CLASSIFY, data.getId(), set);
           currentLoader.load();
        }       
   }
   
   /**
    * Fires an asynchronous call to remove the currently selected image
    * from the specified categories.
    * 
    * @param categories Collection of selected categories.
    */
   void fireDeclassification(Map categories)
   {
       if (categories.size() == 1) {
           Iterator i = categories.keySet().iterator();
           ImageData data = null;
           Set set = null;
           while (i.hasNext()) {
               data = (ImageData) i.next();
               set = (Set) categories.get(data);
           }
           state = TreeViewer.SAVE_CLASSIFICATION;
           currentLoader = new ClassificationSaver(component, 
                           ClassificationSaver.DECLASSIFY, data.getId(), set);
           currentLoader.load();
        }       
   }
   
}
