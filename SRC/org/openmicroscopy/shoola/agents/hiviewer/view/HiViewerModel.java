/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataObjectRemover;
import org.openmicroscopy.shoola.agents.hiviewer.DataObjectSaver;
import org.openmicroscopy.shoola.agents.hiviewer.HiTranslator;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.RndSettingsSaver;
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailsManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardFactory;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.IconsVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.layout.Layout;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;
import org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

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
	
    /** The currently selected experimenter. */
    private ExperimenterData	experimenter;

    /** The id of the selected group of the current user. */
    private long				userGroupID;
    
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
    
    /** Used to sort the nodes by date or alphabetically. */
    private ViewerSorter        sorter;
    
    /** Reference to the component handling data. */
    private DataHandler			dataHandler;
    
    /** Reference to the component that embeds this model. */
    protected HiViewer          component;
    
    /** Cancels any on-going data retrieval. */
    private void cancel()
    {
    	if (currentLoader != null) currentLoader.cancel();
    }
    
    /** Creates a new object and sets its state to {@link HiViewer#NEW}. */
    protected HiViewerModel()
    { 
        sorter = new ViewerSorter();
        state = HiViewer.NEW; 
    }
    
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
     * @param experimenter	The currently selected experimenter.
     */
    void setRootLevel(ExperimenterData experimenter)
    {
        this.experimenter = experimenter;
    }
    
    /**
     * Returns the ID of the root. 
     * 
     * @return See above.
     */
    long getRootID() { return experimenter.getId(); }
    
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
     * Returns the selected experimenter.
     * 
     * @return See above.
     */
    ExperimenterData getExperimenter() { return experimenter; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link HiViewer} interface.  
     */
    int getState() { return state; }
    
    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link HiViewer#LOADING_HIERARCHY}. 
     * 
     * @param refresh 	Pass <code>false</code> if we retrieve the data for
     * 					the first time, <code>true</code> otherwise.
     */
    void fireHierarchyLoading(boolean refresh)
    {
    	cancel();
        state = HiViewer.LOADING_HIERARCHY;
        currentLoader = createHierarchyLoader(refresh);
        currentLoader.load();
    }
    
    /**
     * Refreshes the {@link Browser} component.
     * 
     * @param roots The root nodes.
     * @param flat  Pass <code>false</code> if it's a true hierarchy, 
     *              <code>true</code> if it's a collection of images to browse.
     */
    void refreshBrowser(Set roots, boolean flat)
    {
    	thumbsManager = null;
    	Set visTrees; 
    	//Check if the objects are readable.
    	long userID = getUserDetails().getId();
    	if (flat) 
    		visTrees = HiTranslator.transformImages(roots, userID, userGroupID);
    	else 
    		visTrees = HiTranslator.transformHierarchy(roots, userID, 
    					userGroupID);
    	int layoutIndex = browser.getSelectedLayout();
    	//TODO: Identifies the location of the nodes and pass it to 
    	Layout layout = LayoutFactory.createLayout(layoutIndex, sorter);
    	switch (layoutIndex) {
	    	case LayoutFactory.FLAT_LAYOUT:
	    		layout.setOldNodes(browser.getImageNodes());
	    		break;
	    	case LayoutFactory.SQUARY_LAYOUT:
	    	default:
	    		Set set = new HashSet();
	    		Set oldNodes = browser.getRootNodes();
	    		if (oldNodes != null) {
	    			Iterator i = oldNodes.iterator();
	    			ImageDisplay n;
	    			while (i.hasNext()) {
						n = (ImageDisplay) i.next();
						set.add(n);
						if (!n.containsImages())
							set.addAll(n.getChildrenDisplay());
					}
	    		}
	    		set.add(browser.getUI());
	    		layout.setOldNodes(set);
	    	break;
    	}
    	browser = BrowserFactory.createBrowser(visTrees);
        browser.setSelectedLayout(layoutIndex);
        switch (layoutIndex) {
			case LayoutFactory.FLAT_LAYOUT:
				browser.resetChildDisplay();
				browser.accept(layout);
				layout.doLayout();
			break;
			case LayoutFactory.SQUARY_LAYOUT:
			default:
				browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
			break;
		}
        browser.accept(new IconsVisitor(), ImageDisplayVisitor.IMAGE_SET_ONLY);
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
        if (flat) 
        	visTrees = HiTranslator.transformImages(roots, userID, userGroupID);
        else 
        	visTrees = HiTranslator.transformHierarchy(roots, userID, 
        											userGroupID);
        //Make the browser.
        browser = BrowserFactory.createBrowser(visTrees);
        
        //Do initial layout and set the icons.
        Layout layout = LayoutFactory.getDefaultLayout(sorter);
        browser.setSelectedLayout(layout.getIndex());
        browser.accept(layout, ImageDisplayVisitor.IMAGE_SET_ONLY);
        browser.accept(new IconsVisitor(), ImageDisplayVisitor.IMAGE_SET_ONLY);
    }
    
    /**
     * Creates a {@link ClipBoard} component to manage the browsed
     * hierarchy trees.
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
        if (treeView == null) 
            treeView = new TreeView((ImageDisplay) browser.getUI());
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
     * Starts the asynchronous removal of the specified objects.
     * 
     * @param nodes Collection of objects to remove.
     */
    void fireDataObjectsRemoval(List nodes)
    {
        state = HiViewer.SAVING_DATA_OBJECT;
        DataObject object, po;
        Iterator i = nodes.iterator();
        ImageDisplay node, parent;
        Object ho;
        Set<DataObject> toRemove = null;  
        Map<DataObject, Set> map = null;
        Set<DataObject> l;
        while (i.hasNext()) {
            node = (ImageDisplay) i.next();
            parent = node.getParentDisplay();
            ho = node.getHierarchyObject();
            if (ho instanceof DataObject) {
                object  = (DataObject) ho;
                if ((object instanceof ProjectData) || 
                        (object instanceof CategoryGroupData)) {
                    if (toRemove == null) toRemove = new HashSet<DataObject>();
                    toRemove.add(object);
                } else {
                    po = (DataObject) parent.getHierarchyObject();
                    if (map == null) map = new HashMap<DataObject, Set>();
                    l = (Set) map.get(po);
                    if (l == null) l = new HashSet<DataObject>();
                    l.add(object);
                    map.put(po, l);
                }
            }
        }
        if (toRemove != null) {
            currentLoader = new DataObjectRemover(component, toRemove, null);
            currentLoader.load();
        } else {
            currentLoader = new DataObjectRemover(component, map);
            currentLoader.load();
        }
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
     *                  mouses over an {@link ImageNode}, <code>false</code> 
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
     * Returns the {@link ViewerSorter}.
     * 
     * @return See above.
     */
    ViewerSorter getSorter() { return sorter; }
    
    /**
     * Creates the <code>DataHandler</code> to annotate the specified nodes.
     * 
     * @param owner	The parent of the frame.
     * @param nodes The nodes to annotate.
     * @return See above.
     */
	DataHandler annotateDataObjects(JFrame owner, Set nodes)
	{
		Object uo;
		Set toAnnotate = new HashSet();
		Iterator i = nodes.iterator();
		Class type = null;
		while (i.hasNext()) {
			uo = ((ImageDisplay) i.next()).getHierarchyObject();
			if (uo instanceof ImageData) {
				type = ImageData.class;
				toAnnotate.add(uo);	
			} else if (uo instanceof DatasetData) {
				type = DatasetData.class;
				toAnnotate.add(uo);	
			}
		}
		dataHandler = AnnotatorFactory.getAnnotator(owner, toAnnotate, 
								HiViewerAgent.getRegistry(), type);
		return dataHandler;
	}

	/**
	 * Creates the <code>DataHandler</code> to classify or declassify the 
     * specified images depending on the passed mode.
     * 
     * @param owner	The parent of the frame.
	 * @param nodes The images to classify or declassify.
	 * @param mode	The mode indicating if we classify or declassify the images.
	 * @return See above.
	 */
	DataHandler classifyImageObjects(JFrame owner, ImageData[] nodes, int mode)
	{
		Set<ImageData> images = new HashSet<ImageData>(nodes.length);
		for (int i = 0; i < nodes.length; i++) 
			images.add(nodes[i]);
		dataHandler = ClassifierFactory.getClassifier(owner, images, 
								getRootID(), mode, HiViewerAgent.getRegistry());
		return dataHandler;
	}
	   
	 /**
	  * Creates the <code>DataHandler</code> to annotate the images
	  * of the specified node.
	  * 
	  * @param owner	The parent of the frame.
	  * @param node 	The nodes containing the images to annotate.
	  * @return See above.
	  */
	DataHandler annotateChildren(JFrame owner, ImageDisplay node)
	{
		Object uo = node.getHierarchyObject();
		Set toAnnotate = new HashSet();
		if (uo instanceof DatasetData) {
			toAnnotate.add(uo);
			dataHandler = AnnotatorFactory.getChildrenAnnotator(owner, 
					toAnnotate, HiViewerAgent.getRegistry(), DatasetData.class);
			return dataHandler;
		} else if (uo instanceof CategoryData) {
			toAnnotate.add(uo);
			dataHandler = AnnotatorFactory.getChildrenAnnotator(owner, 
					toAnnotate, HiViewerAgent.getRegistry(), 
						CategoryData.class);
			return dataHandler; 
		}
		return null;
	}
	   
	 /**
	  * Creates the <code>DataHandler</code> to classify the images contained
	  * in the passed container.
	  * 
	  * @param owner	The parent of the frame.
	  * @param node 	The folder containing the images to classify.
	  * @return See above.
	  */
	DataHandler classifyChildren(JFrame owner, ImageDisplay node)
	{
		Object uo = node.getHierarchyObject();
		if ((uo instanceof DatasetData) || (uo instanceof CategoryData)) {
			Set<DataObject> folders = new HashSet<DataObject>(1);
			folders.add((DataObject) uo);
			dataHandler = ClassifierFactory.getChildrenClassifier(owner, 
							folders, getRootID(), HiViewerAgent.getRegistry());
			return dataHandler;
		}
		return null;
	}
	   
	/** Discards the <code>DataHandler</code>. */
	void discardDataHandler()
	{
		if (dataHandler != null) {
			dataHandler.discard();
			dataHandler = null;
		}
	}
	 
	/**
	 * Returns <code>true</code> if data related to a node is displayed
	 * when the user mouses over the node, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMouseOver()
	{ 
		if (browser == null) return false;
		return browser.isMouseOver();  
    }

	/**
	 * Sets to <code>true</code> if data related to a node is displayed
	 * when the user mouses over the node, to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setMouseOver(boolean b)
	{ 
		if (browser != null) browser.setMouseOver(b);  
	}
	
	/** 
	 * Sets the state to <code>READY</code> when the <code>DataObject</code>
	 * is saved.
	 */
	void onDataObjectSave()
	{
		state = HiViewer.READY;
	}
	
	/**
	 * Returns the <code>DataHandler</code> or null if not initialized.
	 * 
	 * @return See above.
	 */
	DataHandler getDataHandler() { return dataHandler; }
	
	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param klass		The type of the node
	 * @param nodesID	The nodes to handle.
	 */
	void firePasteRenderingSettings(Class klass, Set<Long> nodesID)
	{
		currentLoader = new RndSettingsSaver(component, klass, nodesID, 
											HiViewerFactory.getRefPixelsID());
		currentLoader.load();
	}
	
	/**
	 * Fires an asynchronous call to reset the rendering settings.
	 * 
	 * @param klass		The type of the node
	 * @param nodesID	The nodes to handle.
	 */
	void fireResetRenderingSettings(Class klass, Set<Long> nodesID)
	{
		currentLoader = new RndSettingsSaver(component, klass, nodesID, 
											HiViewerFactory.getRefPixelsID());
		currentLoader.load();
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
     * @param refresh	Pass <code>false</code> if we load data for the first 
     * 					time, <code>true</code> otherwise.
     * @return A suitable data loader.
     */
    protected abstract DataLoader createHierarchyLoader(boolean refresh);
    
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
