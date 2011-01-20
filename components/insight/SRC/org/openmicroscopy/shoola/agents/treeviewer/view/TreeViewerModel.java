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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectRemover;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.ImagesImporter;
import org.openmicroscopy.shoola.agents.treeviewer.PlateWellsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ProjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.RndSettingsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.TagHierarchyLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TimeIntervalsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.agents.util.finder.FinderFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.ImportObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PermissionData;
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
			((objParent instanceof DatasetData)
			&& (obj instanceof ImageData)) ||
			((objParent instanceof ScreenData) &&
					(obj instanceof PlateData)))
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
	 * @param nodes The parents of the node to cut.
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
			BrowserFactory.createBrowser(Browser.PROJECT_EXPLORER, 
					component, experimenter, true);
		selectedBrowser = browser;
		browser.setSelected(true);
		browsers.put(Browser.PROJECT_EXPLORER, browser);
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
	}

	/** Initializes. */
	private void initialize()
	{
		state = TreeViewer.NEW;
		browsers = new HashMap<Integer, Browser>();
		recycled = false;
		refImage = null;
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
	 * Starts the asynchronous removal of the data and sets the state to 
	 * {@link TreeViewer#SAVE}.
	 * This method should be invoked to remove a collection of nodes of the
	 * same type.
	 * 
	 * @param values The values to delete.
	 */
	void fireObjectsDeletion(List<DeletableObject> values)
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
	 * Returns the available user groups.
	 * 
	 * @return See above.
	 */
	Map getAvailableUserGroups()
	{
		return (Map) TreeViewerAgent.getRegistry().lookup(
				LookupNames.USER_GROUP_DETAILS);
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
	 * @param ids 	Collection of nodes ids.
	 * @param klass Either dataset, Plate, Project, Screen or image.
	 */
	void firePasteRenderingSettings(List<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		long id = refImage.getId();
		List<Long> toKeep = new ArrayList<Long>();
		Iterator<Long> i = ids.iterator();
		long id1;
		while (i.hasNext()) {
			id1 = i.next();
			if (id1 != id) toKeep.add(id1);
		}
		if (toKeep.size() == 0) return;
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
	 * @param ids 	Collection of nodes ids.
	 * @param klass Either dataset, or image.
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
	 * @param ids 	Collection of nodes ids.
	 * @param klass Either dataset or image.
	 */
	void fireSetOriginalRenderingSettings(List<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, ids, 
										RndSettingsSaver.SET);
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ref The time reference object.	
	 */
	void fireSetOriginalRenderingSettings(TimeRefObject ref)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, ref, 
											RndSettingsSaver.SET);
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
		currentLoader = new ProjectsLoader(component, node);
		currentLoader.load();
	}
	
	/**
	 * Browses the node hosting the plates to browse.
	 * 
	 * @param nodes The nodes to browse.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	void browsePlates(List<TreeImageDisplay> nodes, boolean withThumbnails)
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
		currentLoader = new TagHierarchyLoader(component, node);
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
		if (nodes == null) return null;
		if (nodes.length == 0) return null;
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
	 * Fires an asynchronous call to import the specified files.
	 * 
	 * @param nodes 	The nodes to reload.
	 * @param files 	The files to import.
	 * @param archived 	Pass <code>true</code> to archived the files, 
	 * 					<code>false</code> otherwise.
	 */
	void importFiles(List<TreeImageDisplay> nodes, List<ImportObject> files,
			boolean archived)
	{
		ImagesImporter loader = new ImagesImporter(component, nodes, files,
			archived);
		loader.load();
	}
	
	/**
	 * Fires an asynchronous call to import the specified files.
	 * 
	 * @param node 		The node hosting the container to import the image into.
	 * @param files 	The files to import.
	 * @param archived 	Pass <code>true</code> to archived the files, 
	 * 					<code>false</code> otherwise.
	 */
	void importFiles(TreeImageDisplay node, List<ImportObject> files, 
			boolean archived)
	{
		ImagesImporter loader = new ImagesImporter(component, node, files,
				archived);
		loader.load();
	}
	
	/**
	 * Returns <code>true</code> if the file can be imported,
	 * <code>false</code> otherwise.
	 * 
	 * @param f The file to handle.
	 * @return See above.
	 */
	boolean isFileImportable(File f)
	{
		if (f.isDirectory()) return false;
		if (f.isHidden()) return false;
		if (!f.canRead()) return false;
		if (!f.exists()) return false;
		Browser b = getSelectedBrowser();
		/*
		Browser b = getBrowser(Browser.FILE_SYSTEM_EXPLORER);
		//already imported
		if (b.isFileImported(f.getAbsolutePath()))
			return false;
			*/
		if (b != null && b.getBrowserType() == Browser.FILE_SYSTEM_EXPLORER
				&& b.isFileImported(f.getAbsolutePath()))
			return false;
		
		String path = f.getAbsolutePath();
    	path = path.toLowerCase();
    	if (path.endsWith(OmeroImageService.ZIP_EXTENSION)) return true;
    	
		List<FileFilter> filters = getSupportedFormats();
		Iterator<FileFilter> i = filters.iterator();
		FileFilter filter;
		while (i.hasNext()) {
			filter = i.next();
			if (filter.accept(f)) return true;
		}
		return false;
	}
	
	/**
	 * Returns the list of the supported file formats.
	 * 
	 * @return See above.
	 */
	List<FileFilter> getSupportedFormats()
	{
		OmeroImageService svc = TreeViewerAgent.getRegistry().getImageService();
		return svc.getSupportedFileFilters();
	}

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
		Map<Long, Boolean> map = exp.isLeader();
		if (map.containsKey(userGroupID)) {
			b = map.get(userGroupID);
			if (b) return true;
		}
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
	
}
