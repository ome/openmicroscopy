/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.AdminCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectRemover;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.OriginalFileLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ParentLoader;
import org.openmicroscopy.shoola.agents.treeviewer.PlateWellsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ProjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.RndSettingsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.ScriptLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ScriptsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TagHierarchyLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TimeIntervalsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.agents.util.finder.FinderFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;


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
	private Map<Integer, Browser>	browsers;

	/** The currently selected {@link Browser}. */
	private Browser             	selectedBrowser;

	/** The ID of the root. */
	private long                    rootID;

	/** The id of the selected group of the current user. */
	private long					userGroupID;

	/** The currently selected experimenter. */
	private ExperimenterData		experimenter;

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

	/** Flag indicating if the {@link TreeViewer} is recycled or not. */
	private boolean					recycled;

	/** Flag indicating to retrieve the node data when rolling over. */
	private boolean					rollOver;

	/** The image to copy the rendering settings from. */
	private ImageData				refImage;

	/** The viewer displaying the metadata. */
	private MetadataViewer 			metadataViewer;
	
	/** The viewer displaying the thumbnails. */
	private DataBrowser 			dataViewer;
	
	/** Reference to the advanced finder. */
	private AdvancedFinder			advancedFinder;
	
	/** Reference to the component that embeds this model. */
	protected TreeViewer            component;

	/** Flag indicating if there is an on-going import. */
	private boolean 				importing;
	
	/** Collection of uploaded scripts. */
	private Map<Long, ScriptObject>  scripts;
	
	/** Used to sort the various collections. */
    private ViewerSorter			sorter;
    
    /** Scripts with a UI. */
	private List<ScriptObject> scriptsWithUI;
	
    /**
     * Returns the collection of scripts with a UI, mainly the figure scripts.
     * 
     * @return See above.
     */
    private List<ScriptObject> getScriptsWithUI()
    {
    	if (scriptsWithUI != null) return scriptsWithUI;
    	try {
    		OmeroImageService svc = 
    			TreeViewerAgent.getRegistry().getImageService();
    		scriptsWithUI = svc.loadAvailableScriptsWithUI();
    		return scriptsWithUI;
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Scripts with UI");
			msg.print(e);
			MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
		}
    	return new ArrayList<ScriptObject>();
    }
    
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

		if ((objParent instanceof ProjectData &&
			obj instanceof DatasetData) || 
			(objParent instanceof DatasetData
			&& obj instanceof ImageData) ||
			(objParent instanceof ScreenData &&
					obj instanceof PlateData) || 
					(objParent instanceof GroupData &&
							obj instanceof ExperimenterData))
		{
			Map map;
			Set children;
			map = new HashMap(parents.length);
			for (int i = 0; i < parents.length; i++) {
				children = new HashSet(nodesToCopy.length);
				for (int j = 0; j < nodesToCopy.length; j++) 
					children.add(nodesToCopy[j].getUserObject());
				
				map.put(parents[i].getUserObject(), children);
			}
			return map;
		} else if ((obj instanceof TagAnnotationData) && 
				(objParent instanceof TagAnnotationData)) {
			TagAnnotationData tag = (TagAnnotationData) obj;
			TagAnnotationData tagSet = (TagAnnotationData) objParent;
			String nsSet = tagSet.getNameSpace();
			String ns = tag.getNameSpace();
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(nsSet) &&
					ns == null) {
				Map map;
				Set children;
				map = new HashMap(parents.length);
				for (int i = 0; i < parents.length; i++) {
					children = new HashSet(nodesToCopy.length);
					for (int j = 0; j < nodesToCopy.length; j++) 
						children.add(nodesToCopy[j].getUserObject());
					
					map.put(parents[i].getUserObject(), children);
				}
				return map;
			}
		}
		return null;
	}

	/**
	 * Builds the map linking the nodes to cut and the parents.
	 * 
	 * @param nodes The nodes to cut.
	 * @return See above.
	 */
	private Map buildCutMap(TreeImageDisplay[] nodes)
	{
		TreeImageDisplay parent, child;
		Map<Object, Set> map = new HashMap<Object, Set>();
		Object po;
		Set<Object> children;
		for (int i = 0; i < nodes.length; i++) {
			child = nodes[i];
			parent = child.getParentDisplay();
			if (parent != null) {
				po = parent.getUserObject();
				children = (Set) map.get(po);
				if (children == null) {
					children = new HashSet<Object>();   
					map.put(po, children);
				}
				children.add(nodes[i].getUserObject());  
			}
		}
		return map;
	}

	/** Creates the browsers controlled by this model. */
	private void createBrowsers()
	{
		Browser browser = 
			BrowserFactory.createBrowser(Browser.PROJECTS_EXPLORER, 
					component, experimenter, true);
		selectedBrowser = browser;
		browser.setSelected(true);
		browsers.put(Browser.PROJECTS_EXPLORER, browser);
		browser = BrowserFactory.createBrowser(Browser.SCREENS_EXPLORER,
									component, experimenter, true);
		browsers.put(Browser.SCREENS_EXPLORER, browser);
		browser = BrowserFactory.createBrowser(Browser.TAGS_EXPLORER,
				component, experimenter, true);
		browsers.put(Browser.TAGS_EXPLORER, browser);
		browser = BrowserFactory.createBrowser(Browser.IMAGES_EXPLORER,
				component, experimenter, true);
		browsers.put(Browser.IMAGES_EXPLORER, browser);
		browser = BrowserFactory.createBrowser(Browser.FILES_EXPLORER,
				component, experimenter, true);
		browsers.put(Browser.FILES_EXPLORER, browser);
		
		browser = BrowserFactory.createBrowser(Browser.FILE_SYSTEM_EXPLORER,
				component, experimenter, true);
		browsers.put(Browser.FILE_SYSTEM_EXPLORER, browser);
		browser = BrowserFactory.createBrowser(Browser.ADMIN_EXPLORER,
				component, experimenter, true);
		browsers.put(Browser.ADMIN_EXPLORER, browser);
	}

	/** Initializes. */
	private void initialize()
	{
		state = TreeViewer.NEW;
		browsers = new HashMap<Integer, Browser>();
		recycled = false;
		refImage = null;
		importing = false;
		sorter = new ViewerSorter();
	}
	
	/**
	 * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
	 */
	protected TreeViewerModel()
	{
		initialize();
	}

	/**
	 * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
	 * 
	 * @param exp			The experimenter this manager is for. 
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	protected TreeViewerModel(ExperimenterData exp, long userGroupID)
	{
		initialize();
		this.experimenter = exp;
		setHierarchyRoot(exp.getId(), userGroupID);
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
	 * 
	 * @param rootID    	The Id of the root. By default it is the 
	 * 						id of the current user.
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	void setHierarchyRoot(long rootID, long userGroupID)
	{
		this.rootID = rootID;
		this.userGroupID = userGroupID;
	}

	/**
	 * Compares another model to this one to tell if they would result in
	 * having the same display.
	 *  
	 * @param other The other model to compare.
	 * @return <code>true</code> if <code>other</code> would lead to a viewer
	 *          with the same display as the one in which this model belongs;
	 *          <code>false</code> otherwise.
	 */
	boolean isSameDisplay(TreeViewerModel other)
	{
		if (other == null) return false;
		return ((other.rootID == rootID) && (other.userGroupID == userGroupID));
	}

	/**
	 * Returns <code>true</code> if the {@link TreeViewer} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRecycled() { return recycled; }

	/**
	 * Sets to <code>true</code> if the {@link TreeViewer} is recycled,
	 * <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { recycled = b; }

	/** 
	 * Returns the id to the group selected for the current user.
	 * 
	 * @return See above.
	 */
	long getUserGroupID() { return userGroupID; }

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
	Map<Integer, Browser> getBrowsers() { return browsers; }

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
	 * Starts the asynchronous removal of the data and sets the state to 
	 * {@link TreeViewer#SAVE}.
	 * This method should be invoked to remove a collection of groups or 
	 * experimenters.
	 * 
	 * @param values The groups or experimenters to delete.
	 */
	void fireObjectsDeletion(List<DataObject> values)
	{
		state = TreeViewer.SAVE;
		currentLoader = new DataObjectRemover(component, values);
		currentLoader.load();
	}
	
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
		if ((po instanceof ProjectData) || ((po instanceof DatasetData))) {
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

	/** 
	 * Returns <code>true</code> if the nodes have to be cut, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean cut()
	{
		if (copyIndex != TreeViewer.CUT_AND_PASTE) return false;
		if (nodesToCopy == null || nodesToCopy.length == 0) return false;
		Map toRemove = buildCutMap(nodesToCopy);
		currentLoader = new DataObjectUpdater(component, new HashMap(), toRemove,
				DataObjectUpdater.CUT);
		currentLoader.load();
		state = TreeViewer.SAVE;
		return true;
	}

	/**
	 * Returns the first name and the last name of the currently
	 * selected experimenter as a String.
	 * 
	 * @return See above.
	 */
	String getExperimenterNames()
	{
		ExperimenterData exp = getExperimenter();
		return exp.getFirstName()+" "+exp.getLastName();
	}

	/**
	 * Returns the selected experimenter.
	 * 
	 * @return See above.
	 */
	ExperimenterData getExperimenter()
	{
		if (experimenter == null) experimenter = getUserDetails();
		return experimenter;
	}

	/**
	 * Returns <code>true</code> if the rollOver option is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isRollOver() { return rollOver; }

	/**
	 * Sets the value of the rollOver flag.
	 * 
	 * @param rollOver 	Pass <code>true</code> to turn the flag on,
	 * 					<code>false</code> to turn off.
	 */
	void setRollOver(boolean rollOver) { this.rollOver = rollOver; }

	/**
	 * Sets the parameters used to copy and paste rendering settings across
	 * a collection of pixels set.
	 * 
	 * @param refImage The image to copy the rendering settings from.
	 */
	void setRndSettings(ImageData refImage)
	{
		this.refImage = refImage;
	}

	/**
	 * Returns <code>true</code> if there are rendering settings to paste,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndSettingsToPaste() { return refImage != null; }

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes identifiers.
	 * @param klass The type of nodes to handle.
	 */
	void firePasteRenderingSettings(List<Long> ids, Class klass)
	{
		long id = refImage.getId();
		List<Long> toKeep = new ArrayList<Long>();
		Iterator<Long> i = ids.iterator();
		long id1;
		while (i.hasNext()) {
			id1 = i.next();
			if (id1 != id) toKeep.add(id1);
		}
		if (toKeep.size() == 0) return;
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, toKeep, 
								refImage.getDefaultPixels().getId());
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ref The time reference object.	
	 */
	void firePasteRenderingSettings(TimeRefObject ref)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, ref, 
				refImage.getDefaultPixels().getId());
		currentLoader.load();
	}
	
	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes identifiers.
	 * @param klass The type of nodes to handle.
	 */
	void fireResetRenderingSettings(List<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, ids, 
											RndSettingsSaver.RESET);
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ref The time reference object.	
	 */
	void fireResetRenderingSettings(TimeRefObject ref)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, ref, 
										RndSettingsSaver.RESET);
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes identifiers.
	 * @param klass The type of nodes to handle.
	 */
	void fireSetMinMax(List<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, ids, 
										RndSettingsSaver.SET_MIN_MAX);
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes identifiers.
	 * @param klass The type of nodes to handle.
	 */
	void fireSetOwnerRenderingSettings(List<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, ids, 
										RndSettingsSaver.SET_OWNER);
		currentLoader.load();
	}
	
	/**
	 * Fires an asynchronous call to set the original rendering settings.
	 * 
	 * @param ref The time reference object.	
	 */
	void fireSetOriginalRenderingSettings(TimeRefObject ref)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, ref, 
											RndSettingsSaver.SET_MIN_MAX);
		currentLoader.load();
	}
	
	/**
	 * Fires an asynchronous call to set the rendering settings used by the 
	 * owner of the images.
	 * 
	 * @param ref The time reference object.	
	 */
	void fireSetOwnerRenderingSettings(TimeRefObject ref)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, ref, 
											RndSettingsSaver.SET_MIN_MAX);
		currentLoader.load();
	}
	
	/** 
	 * Starts an asynchronous call to save a data object.
	 * 
	 * @param object 	 The object to create.
	 * @param withParent Sets to <code>true</code> if the object will 
	 * 						have a parent, <code>false</code> otherwise.
	 */
	void fireDataObjectCreation(DataObject object, boolean withParent)
	{
		DataObject data = null;
		if (withParent) {
			Browser browser = getSelectedBrowser();
			TreeImageDisplay node = browser.getLastSelectedDisplay();
	        if (node != null) {
	            Object p =  node.getUserObject();
	            if (object instanceof DatasetData) {
	            	if (p instanceof ProjectData)
	            		data = ((DataObject) p);
	            } else if (object instanceof PlateData) {
	            	if (p instanceof ScreenData)
	            		data = ((DataObject) p);
	            } else if (object instanceof TagAnnotationData) {
	            	if (p instanceof TagAnnotationData) {
	            		TagAnnotationData tag = (TagAnnotationData) p;
	            		String ns = tag.getNameSpace();
	            		if (ns != null && 
	            				TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns))
	            			data = tag;
	            	}	
	            }
	        }
		}
		currentLoader = new DataObjectCreator(component, object, data);
		currentLoader.load();
	}
	
	/**
	 * Returns the metadata viewer.
	 * 
	 * @return See above.
	 */
	MetadataViewer getMetadataViewer()
	{
		if (metadataViewer == null) 
			metadataViewer = MetadataViewerFactory.getViewer("");
		return metadataViewer;
	}
	
	/**
	 * Creates the advanced finder.
	 * 
	 * @return See above.
	 */
	AdvancedFinder getAdvancedFinder()
	{ 
		if (advancedFinder == null)
			advancedFinder = FinderFactory.getAdvancedFinder(
							TreeViewerAgent.getRegistry());
		return advancedFinder; 
	}

	/**
	 * Browses the node hosting the project to browse.
	 * 
	 * @param node The node to browse.
	 */
	void browseProject(TreeImageDisplay node)
	{
		state = TreeViewer.LOADING_DATA;
		ExperimenterData exp = getSelectedBrowser().getNodeOwner(node);
		currentLoader = new ProjectsLoader(component, node, exp.getId(), 
				getUserGroupID());
		currentLoader.load();
	}
	
	/**
	 * Browses the node hosting the plates to browse.
	 * 
	 * @param nodes The nodes to browse.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	void browsePlates(Collection<TreeImageDisplay> nodes, boolean withThumbnails)
	{
		state = TreeViewer.LOADING_DATA;
		List<TreeImageSet> plates = new ArrayList<TreeImageSet>();
		Iterator<TreeImageDisplay> i = nodes.iterator();
		while (i.hasNext())
			plates.add((TreeImageSet) i.next());
		currentLoader = new PlateWellsLoader(component, plates, withThumbnails);
		currentLoader.load();
	}
	
	/**
	 * Browses the node hosting the time interval to browse.
	 * 
	 * @param node The node to browse.
	 */
	void browseTimeInterval(TreeImageTimeSet node)
	{
		state = TreeViewer.LOADING_DATA;
		currentLoader = new TimeIntervalsLoader(component, node);
		currentLoader.load();
	}
	
	/**
	 * Browses the node hosting the tag linked to tags to browse.
	 * 
	 * @param node The node to browse.
	 */
	void browseTag(TreeImageDisplay node)
	{
		state = TreeViewer.LOADING_DATA;
		ExperimenterData exp = getSelectedBrowser().getNodeOwner(node);
		if (exp == null) exp = TreeViewerAgent.getUserDetails();
		currentLoader = new TagHierarchyLoader(component, node, exp.getId());
		currentLoader.load();
	}

	/**
	 * Returns the {@link Browser} corresponding to the passed index
	 * or <code>null</code> if the index is not supported.
	 * 
	 * @param index The index of the {@link Browser}.
	 * @return See above
	 */
	Browser getBrowser(int index) { return browsers.get(index); }
	
	/**
	 * Returns the name of the image to copy and paste.
	 * 
	 * @return See above.
	 */
	String getRefImagePartialName()
	{
		if (refImage == null) return null;
		return EditorUtil.getPartialName(refImage.getName());
	}
	
	/**
	 * Returns the name of the image to copy and paste.
	 * 
	 * @return See above.
	 */
	String getRefImageName()
	{
		if (refImage == null) return null;
		return UIUtilities.removeFileExtension(refImage.getName());
	}
	/**
	 * Returns the type of nodes to copy or <code>null</code> if no nodes 
	 * to copy or cut.
	 * 
	 * @return See above.
	 */
	Class getDataToCopyType()
	{
		TreeImageDisplay[] nodes = getNodesToCopy();
		if (nodes == null || nodes.length == 0) return null;
		Object ho = nodes[0].getUserObject();
		return ho.getClass();
	}

	/**
	 * Sets the {@link DataBrowser}.
	 * 
	 * @param dataViewer The data viewer.
	 */
	void setDataViewer(DataBrowser dataViewer)
	{
		this.dataViewer = dataViewer;
	}
	
	/**
	 * Returns the {@link DataBrowser}.
	 * 
	 * @return See above.
	 */
	DataBrowser getDataViewer() { return dataViewer; }

	/**
	 * Returns <code>true</code> if the multiple users flag is turned on,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMultiUser()
	{
		Boolean b = (Boolean) TreeViewerAgent.getRegistry().lookup(
				TreeViewerAgent.MULTI_USER);
		if (!b) return false;
		
		ExperimenterData exp = getExperimenter();
		/*
		ExperimenterData exp = getExperimenter();
		Map<Long, Boolean> map = exp.isLeader();
		if (map.containsKey(userGroupID)) {
			b = map.get(userGroupID);
			if (b) return true;
		}
		*/
		//Now we check the status of the group.
		List<GroupData> l = exp.getGroups();
		GroupData group;
		Iterator<GroupData> i = l.iterator();
		PermissionData permission;
		while (i.hasNext()) {
			group = i.next();
			//Check status of that group
			if (group.getId() == userGroupID) {
				permission = group.getPermissions();
				return (permission.isGroupRead() && permission.isGroupWrite());
			}
		}
		return false;
	}

	/**
	 * Starts an asynchronous call to download the images.
	 * 
	 * @param images The image to download.
	 * @param folder The folder where to download the image.
	 * @param application The third party application or <code>null</code>.
	 */ 
	void downloadImages(List<ImageData> images, File folder, 
			ApplicationData application)
	{
		if (images == null || images.isEmpty()) return;
		Set<Long> ids = new HashSet<Long>();
		
		ImageData img;
		PixelsData data;
		Iterator i = images.iterator();
		Object o;
		while (i.hasNext()) {
			o = (Object) i.next();
			if (o instanceof ImageData) {
				img = (ImageData) o;
				if (img.isArchived()) {
					data = img.getDefaultPixels();
					ids.add(data.getId());
				}
			}
		}
		OriginalFileLoader loader = new OriginalFileLoader(component, ids, 
				folder, application);
		loader.load();
	}

	/** Refreshes the renderer. */
	void refreshRenderer()
	{
		if (metadataViewer == null) return;
		Renderer rnd = metadataViewer.getRenderer();
		if (rnd != null) {
			rnd.refresh();
			metadataViewer.renderPlane();
		}
	}
	
	/** Resets the metadata view. */
	void resetMetadataViewer()
	{
		metadataViewer = null;
	}
	
	/**
	 * Reloads the specified thumbnails.
	 * 
	 * @param ids The collection of images' ids to reload.
	 */
	void reloadThumbnails(List<Long> ids)
	{
		if (dataViewer != null)
			dataViewer.reloadThumbnails(ids);
	}
	
	/** 
	 * Returns the MIME type to the passed object.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 */
	String getObjectMimeType(Object object)
	{
		if (object instanceof ImageData) {
			ImageData img = (ImageData) object;
			if (img.isArchived()) return TreeViewerFactory.IMAGE_ARCHIVED;
			return TreeViewerFactory.IMAGE_NOT_ARCHIVED;
		} else if (object instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) object;
			File f = new File(fa.getFileName());
			/*
			MimetypesFileTypeMap map = new MimetypesFileTypeMap();
			type = map.getContentType(f);
			f.delete();
			return type;
			*/
		}
		//MimetypesFileTypeMap map = new MimetypesFileTypeMap();
		//String type = map.getContentType(f);
		//f.delete();
		return null;//type;
	}
	
	/**
	 * Returns the MIME type of the currently selected object.
	 * 
	 * @return See above.
	 */
	String getObjectMimeType()
	{
		Browser browser = getSelectedBrowser();
		if (browser == null) return null;
		TreeImageDisplay d = browser.getLastSelectedDisplay();
		if (d == null) return null;
		return getObjectMimeType(d.getUserObject());
	}

	/**
	 * Returns <code>true</code> if the currently logged in user is 
	 * a leader of a group, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isLeader()
	{
		return TreeViewerAgent.getGroupsLeaderOf().size() > 0;
	}

	/**
	 * Returns <code>true</code> if the currently logged in user
	 * is an administrator, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAdministrator()
	{
		return TreeViewerAgent.isAdministrator();
	}
	
	/**
	 * Sets the id of the currently selected group.
	 * 
	 * @param groupID The value to set.
	 */
	void setGroupId(long groupID)
	{
		this.userGroupID = groupID;
	}
	
	/**
	 * Returns <code>true</code> if the currently logged in user is 
	 * a leader of the selected group, <code>false</code>.
	 * 
	 * @return See above.
	 */
	boolean isLeaderOfSelectedGroup()
	{
		Set groups = TreeViewerAgent.getGroupsLeaderOf();
		if (groups.size() == 0) return false;
		Iterator i = groups.iterator();
		GroupData group;
		while (i.hasNext()) {
			group = (GroupData) i.next();
			if (group.getId() == userGroupID)
				return true;
		}
		return false;
	}
	
	/**
	 * Fires an asynchronous call to create groups or experimenters.
	 * 
	 * @param object The object hosting information about object to create.
	 */
	void fireAdmin(AdminObject object)
	{
		currentLoader = new AdminCreator(component, object);
		currentLoader.load();
	}

	/**
	 * Returns <code>true</code> if on-going imports, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImporting() { return importing; }
	
	/**
	 * Sets the flag indicating if there are on-going imports.
	 * 
	 * @param importing Pass <code>true</code> if there are on-going imports,
	 * 					<code>false</code> otherwise.
	 */
	void setImporting(boolean importing) { this.importing = importing; }
	
	/** 
	 * Returns 
	 */
	boolean isFullScreen() { return true; }

	/** 
	 * Loads the parents of the specified annotation.
	 * 
	 * @param data The annotation to handle.
	 */
	void loadParentOf(FileAnnotationData data)
	{
		ParentLoader loader = new ParentLoader(component, data);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous retrieval 
	 * 
	 * @param data 		The <code>DataObject</code> to create.
	 * @param children	The children to add to the <code>DataObject</code>.
	 */
	void fireDataSaving(DataObject data, Collection children)
	{
		if (data instanceof DatasetData) {	
			DataObjectCreator loader = new DataObjectCreator(component, data, 
					null, children);
			loader.load();
		}
	}

	/**
	 * Returns the script corresponding to the passed identifier.
	 * 
	 * @param scriptID The identifier of the script.
	 * @return See above.
	 */
	ScriptObject getScript(long scriptID)
	{
		return scripts.get(scriptID);
	}
	
	/** Loads the scripts.
	 * 
	 * @param location The location of the mouse.
	 */
	void loadScripts(Point location)
	{
		ScriptsLoader loader = new ScriptsLoader(component, false, location);
		loader.load();
	}
	
	/**
	 * Returns the collection of available scripts.
	 * 
	 * @return See above.
	 */
	Collection<ScriptObject> getAvailableScripts()
	{
		if (scripts == null) return null;
		return scripts.values();
	}
	
	/**
	 * Sets the collection of scripts.
	 * 
	 * @param scripts The value to set.
	 */
	void setAvailableScripts(List scripts)
	{ 
		if (scripts == null) {
			this.scripts = null;
			return;
		}
		//sort the scripts.
		Map<Long, ScriptObject> map = new LinkedHashMap<Long, ScriptObject>();
		List l = sorter.sort(scripts);
		Iterator i = l.iterator();
		ScriptObject s;
		while (i.hasNext()) {
			s = (ScriptObject) i.next();
			map.put(s.getScriptID(), s);
		}
		this.scripts = map; 
	}

	/** 
	 * Loads the specified script.
	 * 
	 * @param scriptID The identifier of the script to load.
	 */
	void loadScript(long scriptID)
	{
		ScriptLoader loader = new ScriptLoader(component, scriptID);
		loader.load();
	}
	
	/**
	 * Sets the specified script.
	 * 
	 * @param script The loaded script.
	 */
	void setScript(ScriptObject script)
	{
		if (scripts == null || scripts.size() == 0) return;
		ScriptObject sc = scripts.get(script.getScriptID());
		if (sc != null) sc.setJobParams(script.getParameters());
	}

    /**
     * Returns the script corresponding to the specified name.
     * 
     * @return See above.
     */
    ScriptObject getScriptFromName(String name)
    { 
    	List<ScriptObject> scripts = getScriptsWithUI();
    	Iterator<ScriptObject> i = scripts.iterator();
    	ScriptObject script;
    	while (i.hasNext()) {
    		script = i.next();
			if (name.contains(script.getName()))
				return script;
		}
    	return null;
    }

	/** Transfers the nodes.
	 * 
	 * @param target The target.
	 * @param nodes The nodes to transfer.
	 */
	void transfer(TreeImageDisplay target, List<TreeImageDisplay> nodes)
	{
		nodesToCopy = (TreeImageDisplay[]) nodes.toArray(
				new TreeImageDisplay[0]);
		copyIndex = TreeViewer.CUT_AND_PASTE;
		Map toRemove = buildCutMap(nodesToCopy);
		TreeImageDisplay[] parents = new TreeImageDisplay[1];
		parents[0] = target;
		Map map = buildCopyMap(parents);
		currentLoader = new DataObjectUpdater(component, map, toRemove,
				DataObjectUpdater.CUT_AND_PASTE);
		currentLoader.load();
		state = TreeViewer.SAVE;
	}

}
