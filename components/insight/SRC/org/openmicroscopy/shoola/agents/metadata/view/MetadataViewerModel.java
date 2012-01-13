/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.view;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.AdminEditor;
import org.openmicroscopy.shoola.agents.metadata.DataBatchSaver;
import org.openmicroscopy.shoola.agents.metadata.DataSaver;
import org.openmicroscopy.shoola.agents.metadata.ExperimenterEditor;
import org.openmicroscopy.shoola.agents.metadata.GroupEditor;
import org.openmicroscopy.shoola.agents.metadata.MetadataLoader;
import org.openmicroscopy.shoola.agents.metadata.ContainersLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.RenderingSettingsLoader;
import org.openmicroscopy.shoola.agents.metadata.StructuredDataLoader;
import org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.editor.EditorFactory;
import org.openmicroscopy.shoola.agents.metadata.rnd.Renderer;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.WellSampleData;

/** 
 * The Model component in the <code>MetadataViewer</code> MVC triad.
 * This class tracks the <code>MetadataViewer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class provides a suitable data loader.
 * The {@link MetadataViewerComponent} intercepts the results of data loadings, 
 * feeds them back to this class and fires state transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MetadataViewerModel 
{
	
	/** Holds one of the state flags defined by {@link MetadataViewer}. */
	private int										state;

	/** Reference to the component that embeds this model. */
	private MetadataViewer							component;

	/** The object of reference for the viewer i.e. the root. */
	private Object									refObject;
	
	/** The object of reference for the viewer i.e. the root. */
	private Object									parentRefObject;
	
	/** The object hosting the various annotations linked to an object. */
	private StructuredDataResults					data;
	
	/** The object hosting the various annotations linked to an object. */
	private StructuredDataResults					parentData;
	
	/** Reference to the browser. */
	private Browser									browser;
	
	/** Reference to the editor. */
	private Editor									editor;
	
	/** The active data loaders. */
	private Map<TreeBrowserDisplay, MetadataLoader>	loaders;
	
	/** Only used when it is a batch call. */
	private Class									dataType;
	
	/** 
	 * Flag indicating the selection mode, <code>true</code>
	 * if single selection, <code>false</code> otherwise.
	 */
	private boolean									singleMode;
	
	/** Collection of nodes related to the node of reference. */
	private List									relatedNodes;
	
	/** 
	 * One of the Rendering constants defined by the 
	 * <code>MetadataViewer</code> interface.
	 */
	private int										index;
	
	/** 
	 * The id of the possible owner, this should only be used
	 * to handle unregistered objects.
	 */
	private long									userID;
	
	/** The collection of rendering settings related to the image. */
	private Map										viewedBy;
	
	/**
	 * Returns the collection of the attachments linked to the 
	 * <code>DataObject</code>.
	 * 
	 * @return See above.
	 */
	private List<FileAnnotationData> getTabularData()
	{ 
		StructuredDataResults data = getStructuredData();
		List<FileAnnotationData> l = new ArrayList<FileAnnotationData>();
		if (data == null) return l;
		Collection<FileAnnotationData> attachements = data.getAttachments(); 
		if (attachements == null) return l;
		Iterator<FileAnnotationData> i = attachements.iterator();
		FileAnnotationData f;
		String ns;
		while (i.hasNext()) {
			f = i.next();
			ns = f.getNameSpace();
			if (FileAnnotationData.BULK_ANNOTATIONS_NS.equals(ns)) {
				l.add(f);
			}
		}
		return l; 
	}
	
	/**
	 * Creates a new object and sets its state to {@link MetadataViewer#NEW}.
	 * 
	 * @param refObject	The reference object.
	 * @param index		One of the rendering constants defined by the 
	 * 					<code>MetadataViewer</code> I/F.
	 */
	MetadataViewerModel(Object refObject, int index)
	{
		state = MetadataViewer.NEW;
		switch (index) {
			case MetadataViewer.RND_GENERAL:
			case MetadataViewer.RND_SPECIFIC:
				this.index = index;
				break;
			default:
				this.index = MetadataViewer.RND_GENERAL;
		}
		this.refObject = refObject;
		loaders = new HashMap<TreeBrowserDisplay, MetadataLoader>();
		data = null;
		dataType = null;
		singleMode = true;
		userID = MetadataViewerAgent.getUserDetails().getId();
	}
	
	/**
	 * Called by the <code>MetadataViewer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(MetadataViewer component)
	{ 
		this.component = component;
		browser = BrowserFactory.createBrowser(component, refObject);
		editor = EditorFactory.createEditor(component, refObject, browser);
	}
	
	/**
	 * Returns the id of the user. Only use to handle <code>FileData</code>
	 * objects.
	 * 
	 * @return See above.
	 */
	long getUserID() { return userID; }
	
	/**
	 * Sets the data type, this value is only used for batch annotation.
	 * 
	 * @param dataType The value to set.
	 */
	void setDataType(Class dataType) { this.dataType = dataType; }
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link MetadataViewer} interface.  
	 */
	int getState() { return state; }
	
	/**
	 * Sets the object in the {@link MetadataViewer#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		state = MetadataViewer.DISCARDED;
		Iterator<TreeBrowserDisplay> i = loaders.keySet().iterator();
		MetadataLoader loader;
		while (i.hasNext()) {
			loader = loaders.get(i.next());
			if (loader != null) loader.cancel();
		}
		loaders.clear();
	}
	
	/**
	 * Sets the object of reference.
	 * 
	 * @param refObject	The value to set.
	 */
	void setRootObject(Object refObject)
	{ 
		this.refObject = refObject; 
		browser.setRootObject(refObject);
		editor.setRootObject(refObject);
		data = null;
		if (!(refObject instanceof WellSampleData) && parentData != null) {
			parentData = null;
		}
		parentRefObject = null;
		viewedBy = null;
	}
	
	/** Refreshes the general view.*/
	void refresh()
	{
		data = null;
		parentData = null;
		browser.setRootObject(refObject);
	}
	
	/**
	 * Sets the parent of the object of reference.
	 * 
	 * @param parentRefObject The value to set.
	 * @param grandParent     The value to set.
	 */
	void setParentRootObject(Object parentRefObject, Object grandParent)
	{
		this.parentRefObject = parentRefObject;
		editor.setParentRootObject(parentRefObject, grandParent);
	}
	
	/**
	 * Returns the parent of the reference object.
	 * 
	 * @return See above.
	 */
	Object getParentRefObject() { return parentRefObject; }
	
	/** 
	 * Returns the object of reference.
	 * 
	 * @return See above.
	 */
	Object getRefObject()
	{ 
		if (data == null) return refObject;
		return refObject; 
	}
	
	/**
	 * Returns the <code>Browser</code> displaying the metadata.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }
	
	/**
	 * Returns the <code>Editor</code> displaying the metadata.
	 * 
	 * @return See above.
	 */
	Editor getEditor() { return editor; }

	/** 
	 * Cancels any ongoing data loading. 
	 * 
	 * @param refNode The node of reference.
	 */
	void cancel(TreeBrowserDisplay refNode)
	{
		MetadataLoader loader = loaders.get(refNode);
		if (loader != null) {
			loader.cancel();
			loaders.remove(refNode);
		}
	}

	/**
	 * Invokes when the data are loaded, the loader is then 
	 * removed from the map.
	 * 
	 * @param refNode
	 */
	void notifyLoadingEnd(TreeBrowserDisplay refNode)
	{
		MetadataLoader loader = loaders.get(refNode);
		if (loader != null) loaders.remove(refNode);
	}
	
	/**
	 * Starts the asynchronous retrieval of the attachments related 
	 * to the parent node.
	 * 
	 * @param refNode 	The menu node of reference.
	 * @param rootType	The type of reference.
	 */
	void fireParentLoading(TreeBrowserSet refNode)
	{
		cancel(refNode);
		//Object ho = getParentObject(refNode);
		Object ho = refNode.getUserObject();
		if (ho instanceof DataObject) {
			ContainersLoader loader = new ContainersLoader(
					component, refNode, ho.getClass(), 
					((DataObject) ho).getId());
			loaders.put(refNode, loader);
			loader.load();
		}
	}


	/**
	 * Starts the asynchronous retrieval of the structured data related
	 * to the passed node.
	 * 
	 * @param refNode The node to handle.
	 */
	void fireStructuredDataLoading(TreeBrowserDisplay refNode)
	{
		Object uo = refNode.getUserObject();
		//if (!(uo instanceof DataObject)) return;
		
		if (uo instanceof ExperimenterData) return;
		if ((uo instanceof DataObject)) {
			//DataObject data = (DataObject) uo;
			//if (data.getId() < 0) return;
			cancel(refNode);
			if (uo instanceof WellSampleData) {
				WellSampleData wsd = (WellSampleData) uo;
				uo = wsd.getImage();
				if (!loaders.containsKey(refNode) && parentData == null
						&& parentRefObject != null) {
					StructuredDataLoader l = new StructuredDataLoader(component,
							refNode, parentRefObject);
					loaders.put(refNode, l);
					l.load();
					state = MetadataViewer.LOADING_METADATA;
					return;
				}
			}
			StructuredDataLoader loader = new StructuredDataLoader(component, 
									refNode, uo);
			loaders.put(refNode, loader);
			loader.load();
			state = MetadataViewer.LOADING_METADATA;
		}
	}
	
	/**
	 * Returns <code>true</code> if the passed object is the reference object,
	 * <code>false</code> otherwise.
	 * 
	 * @param uo The object to compare.
	 * @return See above.
	 */
	boolean isSameObject(DataObject uo)
	{
		if (uo == null || !(refObject instanceof DataObject)) return false;
		Class klass = refObject.getClass();
		if (refObject instanceof WellSampleData) {
			klass = ((WellSampleData) refObject).getImage().getClass();
		}
		if (!uo.getClass().equals(klass))
			return false;
		DataObject object;
		if (refObject instanceof WellSampleData)
			object = ((WellSampleData) refObject).getImage();
		else object = (DataObject) refObject;
		if (uo.getId() != object.getId()) return false;
		if (data == null) return false;
		Object o = data.getRelatedObject();
		if (!(o instanceof DataObject)) return false;
		object = (DataObject) o;
		if (!uo.getClass().equals(object.getClass()))
			return false;
		if (uo.getId() != object.getId()) return false;
		return true;
	}
	
	/** 
	 * Returns the object path i.e. if a dataset is selected,
	 * the name of the project_name of the dataset.
	 * 
	 * @return See above.
	 */
	String getRefObjectPath()
	{
		return getRefObjectName();
	}
	
	/**
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() 
	{
		Object ref = getRefObject();
		if (ref instanceof ImageData)
			return ((ImageData) ref).getName();
		else if (ref instanceof DatasetData)
			return ((DatasetData) ref).getName();
		else if (ref instanceof ProjectData)
			return ((ProjectData) ref).getName();
		else if (ref instanceof PlateData)
			return ((PlateData) ref).getName();
		else if (ref instanceof ScreenData)
			return ((ScreenData) ref).getName();
		return "";
	}

	/**
	 * Fires an asynchronous call to save the data, add (resp. remove)
	 * annotations to (resp. from) the object.
	 * 
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param metadata	The acquisition metadata to save.
	 * @param data		The object to update.
	 * @param asynch 	Pass <code>true</code> to save data asynchronously,
     * 				 	<code>false</code> otherwise.
	 */
	void fireSaving(List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
			List<Object> metadata, Collection<DataObject> data, boolean asynch)
	{
		if (asynch) {
			DataSaver loader = new DataSaver(component, data, toAdd, toRemove, 
					metadata);
			loader.load();
			state = MetadataViewer.SAVING;
		} else {
			OmeroMetadataService os = 
				MetadataViewerAgent.getRegistry().getMetadataService();
			try {
            	if (metadata != null) {
            		Iterator<Object> i = metadata.iterator();
            		while (i.hasNext()) 
						os.saveAcquisitionData(i.next()) ;
            	}
            	os.saveData(data, toAdd, toRemove, userID);
			} catch (Exception e) {
				LogMessage msg = new LogMessage();
				msg.print("Unable to save annotation and/or edited data");
				msg.print(e);
				MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
			}
		}
	}
	
	/**
	 * Fires an asynchronous call to update the passed experimenter.
	 * 
	 * @param data 	 The object to update.
	 * @param asynch Pass <code>true</code> to save data asynchronously,
     * 				 <code>false</code> otherwise.
	 */
	void fireExperimenterSaving(ExperimenterData data, boolean async)
	{
		if (async) {
			ExperimenterEditor loader = new ExperimenterEditor(component, data);
			loader.load();
			state = MetadataViewer.SAVING;
		} else {
			AdminService svc = 
				MetadataViewerAgent.getRegistry().getAdminService();
			try {
				svc.updateExperimenter(data, null);
			} catch (Exception e) {
				LogMessage msg = new LogMessage();
				msg.print("Unable to update the experimenter");
				msg.print(e);
				MetadataViewerAgent.getRegistry().getLogger().error(this, msg);
			}
		}
	}
	
	/**
	 * Fires an asynchronous call to update the passed group.
	 * 
	 * @param data   The object to update.
	 * @param asynch Pass <code>true</code> to save data asynchronously,
     * 				 <code>false</code> otherwise.
	 */
	void fireAdminSaving(AdminObject data, boolean asynch)
	{
		if (asynch) {
			MetadataLoader loader = null;
			switch (data.getIndex()) {
				case AdminObject.UPDATE_GROUP:
					GroupData group = data.getGroup();
					loader = new GroupEditor(component, group, 
							data.getPermissions());
					break;
				case AdminObject.UPDATE_EXPERIMENTER:
					loader = new AdminEditor(component, data.getGroup(),
							data.getExperimenters());
			}	
			if (loader != null) {
				loader.load();
				state = MetadataViewer.SAVING;
			}
		} else {
			AdminService svc = 
				MetadataViewerAgent.getRegistry().getAdminService();
			LogMessage msg = new LogMessage();
			switch (data.getIndex()) {
				case AdminObject.UPDATE_GROUP:
					try {
						GroupData group = data.getGroup();
						GroupData g = svc.lookupGroup(group.getName());
						if (g == null || group.getId() == g.getId())
							svc.updateGroup(data.getGroup(), 
									data.getPermissions());
						else {
							UserNotifier un = 
								MetadataViewerAgent.getRegistry().getUserNotifier();
							un.notifyInfo("Update Group", "A group with the " +
									"same name already exists.");
						}
					} catch (Exception e) {
						msg.print("Unable to update the group");
						msg.print(e);
						MetadataViewerAgent.getRegistry().getLogger().error(
								this, msg);
					}
					break;
				case AdminObject.UPDATE_EXPERIMENTER:
					try {
						svc.updateExperimenters(data.getGroup(), 
								data.getExperimenters());
					} catch (Exception e) {
						msg.print("Unable to update experimenters");
						msg.print(e);
						MetadataViewerAgent.getRegistry().getLogger().error(
								this, msg);
					}
			}	
		}
	}
	
	/**
	 * Sets the structured data.
	 * 
	 * @param data The value to set.
	 * @param refNode The node of reference.
	 */
	void setStructuredDataResults(StructuredDataResults data,
			TreeBrowserDisplay refNode)
	{
		this.data = data;
		state = MetadataViewer.READY;
	}
	
	/**
	 * Sets the structured data of the parent.
	 * 
	 * @param parentData The value to set.
	 * @param refNode The node of reference.
	 */
	void setParentDataResults(StructuredDataResults parentData,
			TreeBrowserDisplay refNode)
	{
		loaders.remove(refNode);
		this.parentData = parentData;
		state = MetadataViewer.READY;
	}
	
	/**
	 * Returns the structured data.
	 * 
	 * @return See above.
	 */
	StructuredDataResults getStructuredData() { return data; }
	
	/**
	 * Returns the structured data.
	 * 
	 * @return See above.
	 */
	StructuredDataResults getParentStructuredData() { return parentData; }
	
	/**
	 * Returns <code>true</code> if the imported set of pixels has been 
	 * archived, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isArchived()
	{ 
		if (!(refObject instanceof ImageData)) return false;
		ImageData img = (ImageData) refObject;
		return img.isArchived(); 
	}

	/**
	 * Fires an asynchronous call to save the objects contained
	 * in the passed <code>DataObject</code> to save, add (resp. remove)
	 * annotations to (resp. from) the object.
	 * 
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param toSave    Collection of data objects to handle.
	 */
	void fireBatchSaving(List<AnnotationData> toAdd, List<AnnotationData> 
						toRemove, Collection<DataObject> toSave)
	{
		DataBatchSaver loader = new DataBatchSaver(component, toSave, toAdd, 
				toRemove);
		loader.load();
		state = MetadataViewer.BATCH_SAVING;
	}
	
	/** 
	 * Sets to <code>true</code> if the model is in single mode,
	 * to <code>false</code> otherwise.
	 * 
	 * @param singleMode The value to set.
	 */
	void setSelectionMode(boolean singleMode)
	{
		this.singleMode = singleMode;
		if (singleMode) relatedNodes = null;
	}
	
	/** 
	 * Returns <code>true</code> if the model is in single mode,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleMode() { return singleMode; }
	
	/**
	 * Sets the nodes related to the object of reference.
	 * 
	 * @param relatedNodes The value to set.
	 */
	void setRelatedNodes(List relatedNodes)
	{ 
		this.relatedNodes = relatedNodes;
	}
	
	/**
	 * Returns the nodes related to the object of reference.
	 * 
	 * @return See above.
	 */
	List getRelatedNodes() { return relatedNodes; }

	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }

	/**
	 * Starts an asynchronous retrieval of the containers hosting the 
	 * currently edited object.
	 * 
	 * @param type  The type of the edited object.
	 * @param id 	The id of the currently edited object.
	 */
	void loadParents(Class type, long id)
	{
		ContainersLoader loader = new ContainersLoader(component, type, id);
		loader.load();
	}

	/**
	 * Starts an asynchronous call to create a movie.
	 * 
	 * @param parameters The movie parameters.
	 */
	void createMovie(MovieExportParam parameters)
	{
		if (parameters == null) return;
		if (!(refObject instanceof ImageData)) return;
		/*
		ImageData img = (ImageData) refObject;
		MovieCreator loader = new MovieCreator(component, parameters, 
				null, img);
		loader.load();
		*/
	}
	
	/**
	 * Returns the rendering index.
	 * 
	 * @return See above.
	 */
	int getIndex() { return index; }

	/**
	 * Returns the instrument transfer function linked to the edited object.
	 * 
	 * @return See above
	 */
	FileAnnotationData getIRF()
	{
		if (!(refObject instanceof ImageData)) return null;
		if (data == null) return null;
		Collection l = data.getAttachments();
		if (l == null || l.size() == 0) return null;
		Iterator i = l.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = (FileAnnotationData) i.next();
			if (fa.getFileName().contains("irf"))
				return fa;
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if it is an image with a lot of channels.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isNumerousChannel()
	{
		if (!(refObject instanceof ImageData)) return false;
		ImageData img = (ImageData) refObject;
		return img.getDefaultPixels().getSizeC() >= Renderer.MAX_CHANNELS;
	}
	
	/** 
	 * Sets the id of the possible owner. This should only be used for
	 * unregistered objects.
	 *  
	 * @param userID The value to set.
	 */
	void setUserID(long userID) { this.userID = userID; }

	/**
	 * Returns the rendering settings associated to the image.
	 * 
	 * @return See above.
	 */
	Map getViewedBy() { return viewedBy; }
	
	/** 
	 * Sets the rendering settings associated to the image.
	 * 
	 * @param viewedBy The value to set.
	 */
	void setViewedBy(Map viewedBy)
	{ 
		Map m = new HashMap();
		if (viewedBy != null) {
			long id = MetadataViewerAgent.getUserDetails().getId();
			Entry entry;
			Iterator i = viewedBy.entrySet().iterator();
			ExperimenterData exp;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				exp = (ExperimenterData) entry.getKey();
				if (exp.getId() != id) {
					m.put(exp, entry.getValue());
				}
			}
		}
		this.viewedBy = m; 
		getEditor().getRenderer().loadRndSettings(true, null);
	}
	
	/**
	 * Starts an asynchronous call to load the rendering settings
	 * associated to the image.
	 */
	void fireViewedByLoading()
	{
		ImageData img = null;
		if (refObject instanceof ImageData) img = (ImageData) refObject;
		else if (refObject instanceof WellSampleData) 
			img = ((WellSampleData) refObject).getImage();
		if (img == null) return;
		getEditor().getRenderer().loadRndSettings(false, null);
		RenderingSettingsLoader loader = new RenderingSettingsLoader(component, 
				img.getDefaultPixels().getId());
		loader.load();
	}
	
	/** Starts an asynchronous retrieval of the thumbnails. */
	void fireThumbnailsLoading()
	{
		ImageData image = null;
		if (refObject instanceof ImageData) image = (ImageData) refObject;
		else if (refObject instanceof WellSampleData)
			image = ((WellSampleData) refObject).getImage();
		Set experimenters = viewedBy.keySet();
		Set<Long> ids = new HashSet<Long>();
		Iterator i = experimenters.iterator();
		while (i.hasNext()) {
			ids.add(((ExperimenterData) i.next()).getId());
		}
		if (ids.size() == 0) return;
		ThumbnailLoader loader = new ThumbnailLoader(component, image, ids);
		loader.load();
	}
	
	/**
	 * Applies the specified rendering settings.
	 * 
	 * @param rndDef The rendering settings to apply.
	 */
	void applyRenderingSettings(RndProxyDef rndDef)
	{
		Renderer rnd = getEditor().getRenderer();
		if (rnd != null) rnd.resetSettings(rndDef, true);
	}
	
	/**
	 * Returns <code>true</code> if the object is writable,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isWritable()
	{
		if (editor == null) return false;
		return editor.isWritable();
	}

}
