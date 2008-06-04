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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectCreator;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectRemover;
import org.openmicroscopy.shoola.agents.treeviewer.DataObjectUpdater;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExistingObjectsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.RndSettingsSaver;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;
import org.openmicroscopy.shoola.agents.util.classifier.view.ClassifierFactory;
import org.openmicroscopy.shoola.agents.util.finder.AdvancedFinder;
import org.openmicroscopy.shoola.agents.util.finder.FinderFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
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

	/** Reference to the component handling data. */
	private DataHandler				dataHandler;

	/** Flag indicating if the {@link TreeViewer} is recycled or not. */
	private boolean					recycled;

	/** Flag indicating to retrieve the node data when rolling over. */
	private boolean					rollOver;

	/** The id of the pixels set to copy. */
	private long					refPixelsID;

	/** The viewer displaying the metadata. */
	private MetadataViewer 			metadataViewer;
	
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
			((objParent instanceof CategoryGroupData) &&
			(obj instanceof CategoryData)) || 
			((objParent instanceof CategoryData) && 
			(obj instanceof ImageData)) || 
			((objParent instanceof DatasetData)
			&& (obj instanceof ImageData)))
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
			po = parent.getUserObject();
			children = (Set) map.get(po);
			if (children == null) {
				children = new HashSet<Object>();   
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
					component, experimenter);
		selectedBrowser = browser;
		browser.setSelected(true);
		browsers.put(new Integer(Browser.PROJECT_EXPLORER), browser);
		browser = BrowserFactory.createBrowser(Browser.TAGS_EXPLORER,
				component, experimenter);
		browsers.put(new Integer(Browser.TAGS_EXPLORER), browser);
		/*
		browser = BrowserFactory.createBrowser(Browser.CATEGORY_EXPLORER,
				component, experimenter);
		browsers.put(new Integer(Browser.CATEGORY_EXPLORER), browser);
		*/
		browser = BrowserFactory.createBrowser(Browser.IMAGES_EXPLORER,
				component, experimenter);
		browsers.put(new Integer(Browser.IMAGES_EXPLORER), browser);
	}

	/**
	 * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
	 */
	protected TreeViewerModel()
	{
		state = TreeViewer.NEW;
		browsers = new HashMap<Integer, Browser>();
		recycled = false;
		refPixelsID = -1;
	}

	/**
	 * Creates a new instance and sets the state to {@link TreeViewer#NEW}.
	 * 
	 * @param exp			The experimenter this manager is for. 
	 * @param userGroupID 	The id to the group selected for the current user.
	 */
	protected TreeViewerModel(ExperimenterData exp, long userGroupID)
	{
		state = TreeViewer.NEW;
		recycled = false;
		refPixelsID = -1;
		this.experimenter = exp;
		browsers = new HashMap<Integer, Browser>();
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
		Set l = new HashSet(1);
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
		Map<DataObject, Set> map = null;
		Set<DataObject> toRemove = null;  
		Set<DataObject> l;
		while (i.hasNext()) {
			n = (TreeImageDisplay) i.next();
			parent = n.getParentDisplay();
			if (n.getUserObject() instanceof DataObject) {
				object = (DataObject) n.getUserObject();
				if ((object instanceof ProjectData) || 
						(object instanceof CategoryGroupData)) {
					if (toRemove == null) toRemove = new HashSet<DataObject>();
					toRemove.add(object);
				} else {

					po = (DataObject) parent.getUserObject();
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

	/**
	 * Creates the <code>DataHandler</code> to annotate the images imported
	 * during a given period of time.
	 * 
	 * @param owner	The parent of the frame.
	 * @param ref 	The time reference.
	 * @return See above.
	 */
	DataHandler annotateDataObjects(JFrame owner, TimeRefObject ref)
	{
		dataHandler = AnnotatorFactory.getAnnotator(owner, ref, 
				TreeViewerAgent.getRegistry());
		return dataHandler;
	}

	/**
	 * Creates the <code>DataHandler</code> to annotate the specified nodes.
	 * 
	 * @param owner	The parent of the frame.
	 * @param type 	The type of node. Either <code>DatasetData</code> 
	 * 				or <code>ImageData</code>.
	 * @param nodes	The nodes to annotate.
	 * @return See above.
	 */
	DataHandler annotateDataObjects(JFrame owner, Class type,
			Set<DataObject> nodes)
	{
		dataHandler = AnnotatorFactory.getAnnotator(owner, nodes, 
				TreeViewerAgent.getRegistry(), type);
		return dataHandler;
	}

	/**
	 * Creates the <code>DataHandler</code> to annotate the images
	 * of the specified node.
	 * 
	 * @param owner	The parent of the frame.
	 * @param klass The data type. Either
	 * @param nodes The nodes containing the images to annotate.
	 * @return See above.
	 */
	DataHandler annotateChildren(JFrame owner, Class klass, 
			Set<DataObject> nodes)
	{
		dataHandler = AnnotatorFactory.getChildrenAnnotator(owner, nodes, 
				TreeViewerAgent.getRegistry(), klass);
		return dataHandler;

	}

	/**
	 * Creates the <code>DataHandler</code> to classify or declassify the 
	 * specified images depending on the passed mode.
	 * 
	 * @param owner	The parent of the frame.
	 * @param nodes	The images to classify or declassify.
	 * @param m		The mode indicating if we classify or declassify the images.
	 * @return See above.
	 */
	DataHandler classifyImageObjects(JFrame owner, Set<ImageData> nodes, int m)
	{
		dataHandler = ClassifierFactory.getClassifier(owner, nodes, rootID, m,
				TreeViewerAgent.getRegistry());
		return dataHandler;
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
	 * Returns the <code>DataHandler</code> or null if not initialized.
	 * 
	 * @return See above.
	 */
	DataHandler getDataHandler() { return dataHandler; }

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
	 * @param refPixelsID	The id of the pixels set of reference.
	 */
	void setRndSettings(long refPixelsID)
	{
		this.refPixelsID = refPixelsID;
	}

	/**
	 * Returns <code>true</code> if we can paste some rendering settings,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndSettingsToPaste() { return refPixelsID != -1; }

	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes ids.
	 * @param klass Either dataset, catgory or image.
	 */
	void firePasteRenderingSettings(Set<Long> ids, Class klass)
	{
		state = TreeViewer.SETTINGS_RND;
		currentLoader = new RndSettingsSaver(component, klass, ids, 
								refPixelsID);
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
		currentLoader = new RndSettingsSaver(component, ref, refPixelsID);
		currentLoader.load();
	}
	
	/**
	 * Fires an asynchronous call to paste the rendering settings.
	 * 
	 * @param ids 	Collection of nodes ids.
	 * @param klass Either dataset, catgory or image.
	 */
	void fireResetRenderingSettings(Set<Long> ids, Class klass)
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
	 * @param klass Either dataset, catgory or image.
	 */
	void fireSetOriginalRenderingSettings(Set<Long> ids, Class klass)
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
	            if (!((object instanceof ProjectData) || 
	                (object instanceof CategoryGroupData)))//root.
	             data = ((DataObject) p);
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
			metadataViewer = MetadataViewerFactory.getViewer("", false,
					MetadataViewer.VERTICAL_LAYOUT);
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

}
