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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.DataBatchSaver;
import org.openmicroscopy.shoola.agents.metadata.DataSaver;
import org.openmicroscopy.shoola.agents.metadata.ExperimenterEditor;
import org.openmicroscopy.shoola.agents.metadata.MetadataLoader;
import org.openmicroscopy.shoola.agents.metadata.ContainersLoader;
import org.openmicroscopy.shoola.agents.metadata.StructuredDataLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.editor.EditorFactory;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.ProjectData;

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

	/** The ref object for the viewer i.e. the root. */
	private Object									refObject;
	
	/** The object hosting the various annotations linked to an object. */
	private StructuredDataResults					data;
	
	/** Reference to the browser. */
	private Browser									browser;
	
	/** Reference to the editor. */
	private Editor									editor;
	
	/** The active data loaders. */
	private Map<TreeBrowserDisplay, MetadataLoader>	loaders;
	
	/** Only used when it is a bacth call. */
	private Class									dataType;
	
	/** 
	 * Flag indicating the selection mode, <code>true</code>
	 * if single selection, <code>false</code> otherwise.
	 */
	private boolean									singleMode;
	
	/** Collection of nodes related to the node of reference. */
	private Collection								relatedNodes;
	
	/**
	 * Creates a new object and sets its state to {@link MetadataViewer#NEW}.
	 * 
	 * @param refObject	The reference object.
	 */
	MetadataViewerModel(Object refObject)
	{
		state = MetadataViewer.NEW;
		this.refObject = refObject;
		loaders = new HashMap<TreeBrowserDisplay, MetadataLoader>();
		data = null;
		dataType = null;
		singleMode = true;
	}
	
	/**
	 * Called by the <code>MetadataViewer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component 		The embedding component.
	 * @param thumbnailRequired Pass <code>true</code> to indicate to load the
	 * 							thumbnail, <code>false</code> otherwise.
	 */
	void initialize(MetadataViewer component, boolean thumbnailRequired)
	{ 
		this.component = component;
		browser = BrowserFactory.createBrowser(component, refObject);
		editor = EditorFactory.createEditor(component, refObject,
										thumbnailRequired, browser);
	}
	
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
	}
	
	/** 
	 * Returns the object of reference.
	 * 
	 * @return See above.
	 */
	Object getRefObject() { return refObject; }
	
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
	 * Starts the asynchronous retrieval of the attachments reladed 
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
		if (!(uo instanceof DataObject)) return;
		
		if (uo instanceof ExperimenterData) return;
		cancel(refNode);
		StructuredDataLoader loader = new StructuredDataLoader(component, 
								refNode, (DataObject) uo);
		loaders.put(refNode, loader);
		loader.load();
		state = MetadataViewer.LOADING_METADATA;
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
		if (!uo.getClass().equals(refObject.getClass()))
			return false;
		DataObject object = (DataObject) refObject;
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
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() 
	{
		if (refObject instanceof ImageData)
			return ((ImageData) refObject).getName();
		else if (refObject instanceof DatasetData)
			return ((DatasetData) refObject).getName();
		else if (refObject instanceof ProjectData)
		return ((ProjectData) refObject).getName();
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
	 */
	void fireSaving(List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
					List<Object> metadata, Collection<DataObject> data)
	{
		DataSaver loader = new DataSaver(component, data, toAdd, toRemove, 
				metadata);
		loader.load();
		state = MetadataViewer.SAVING;
	}
	
	/**
	 * Fires an asynchronous call to update the passed experimenter.
	 * 
	 * @param data The object to update.
	 */
	void fireExperimenterSaving(ExperimenterData data)
	{
		ExperimenterEditor loader = new ExperimenterEditor(component, data);
		loader.load();
		state = MetadataViewer.SAVING;
	}
	
	/**
	 * Sets the structured data.
	 * 
	 * @param data The value to set.
	 */
	void setStructuredDataResults(StructuredDataResults data)
	{
		this.data = data;
		state = MetadataViewer.READY;
	}
	
	/**
	 * Returns the structured data.
	 * 
	 * @return See above.
	 */
	StructuredDataResults getStructuredData() { return data; }
	
	/**
	 * Returns <code>true</code> if the imported set of pixels has been 
	 * archived, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isArchived()
	{ 
		if (data == null) return false;
		return data.isArchived(); 
	}

	/**
	 * Fires an asynchronous call to save the objects contained
	 * in the passed <code>DataObject</code> to save, add (resp. remove)
	 * annotations to (resp. from) the object.
	 * 
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param toSave    Collection of data objectst to handle.
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
	 * @param relateNodes The value to set.
	 */
	void setRelatedNodes(Collection relateNodes)
	{ 
		this.relatedNodes = relateNodes;
	}
	
	/**
	 * Returns the nodes related to the object of reference.
	 * 
	 * @return See above.
	 */
	Collection getRelatedNodes() { return relatedNodes; }

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
	 * @param id 	The id of the currently edited oject.
	 */
	void loadParents(Class type, long id)
	{
		ContainersLoader loader = new ContainersLoader(component, type, id);
		loader.load();
	}
	
}
