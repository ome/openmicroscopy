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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.AttachmentLoader;
import org.openmicroscopy.shoola.agents.metadata.MetadataLoader;
import org.openmicroscopy.shoola.agents.metadata.ParentLoader;
import org.openmicroscopy.shoola.agents.metadata.StructuredDataLoader;
import org.openmicroscopy.shoola.agents.metadata.ViewedByLoader;
import org.openmicroscopy.shoola.agents.metadata.TagLoader;
import org.openmicroscopy.shoola.agents.metadata.UrlLoader;
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import pojos.DataObject;
import pojos.ImageData;

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
	private int									state;

	/** Reference to the component that embeds this model. */
	private MetadataViewer						component;

	/** The ref object for the viewer i.e. the root. */
	private Object								refObject;
	
	/** Reference to the browser. */
	private Browser								browser;
	
	/** The active data loaders. */
	private Map<TreeBrowserSet, MetadataLoader>	loaders;
	
	/**
	 * Returns the parent object of the passed node.
	 * 
	 * @param refNode	The node of reference.
	 * @return See above.
	 */
	private Object getParentObject(TreeBrowserSet refNode)
	{
		TreeBrowserDisplay parent = refNode.getParentDisplay();
		if (parent == null) return null; //This should not happen
		return parent.getUserObject();
	}
	
	/**
	 * Creates a new object and sets its state to {@link MetadataViewer#NEW}.
	 * 
	 * @param refObject
	 */
	MetadataViewerModel(Object refObject)
	{
		state = MetadataViewer.NEW;
		this.refObject = refObject;
		loaders = new HashMap<TreeBrowserSet, MetadataLoader>();
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
	}
	
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
		Iterator<TreeBrowserSet> i = loaders.keySet().iterator();
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
	void setRefObject(Object refObject)
	{ 
		this.refObject = refObject; 
		browser.setRootObject(refObject);
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
	 * 
	 * Starts the asynchronous retrieval of the tag reladed to the parent of 
	 * the passed node.
	 * 
	 * @param refNode The menu node of reference.
	 */
	void fireTagLoading(TreeBrowserSet refNode)
	{
		cancel(refNode);
		Object ho = getParentObject(refNode);
		long id = -1;
		if (ho instanceof DataObject)
			id = ((DataObject) ho).getId();
		TagLoader loader = new TagLoader(component, refNode, ho.getClass(), id);
		loaders.put(refNode, loader);
		loader.load();
	}
	
	/**
	 * Starts the asynchronous retrieval of the attachments reladed 
	 * to the parent node.
	 * 
	 * @param refNode The menu node of reference.
	 */
	void fireAttachmentLoading(TreeBrowserSet refNode)
	{
		//first cancel the loading related to the specified node.
		cancel(refNode);
		Object ho = getParentObject(refNode);
		long id = -1;
		if (ho instanceof DataObject)
			id = ((DataObject) ho).getId();
		AttachmentLoader loader = new AttachmentLoader(component, refNode, 
													ho.getClass(), id);
		loaders.put(refNode, loader);
		loader.load();
	}
	
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
			loaders.remove(loader);
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
		if (loader != null) loaders.remove(loader);
	}
	
	/**
	 * Starts the asynchronous retrieval of the attachments reladed 
	 * to the parent node.
	 * 
	 * @param refNode 	The menu node of reference.
	 * @param rootType	The type of reference.
	 */
	void fireParentLoading(TreeBrowserSet refNode, Class rootType)
	{
		cancel(refNode);
		Object ho = getParentObject(refNode);
		if (ho instanceof DataObject) {
			ParentLoader loader = new ParentLoader(
					component, refNode, rootType, ((DataObject) ho).getId());
			loaders.put(refNode, loader);
			loader.load();
		}
	}

	/**
	 * Starts the asynchronous retrieval of the rendering settings 
	 * reladed to a given set of pixels.
	 * 
	 * @param refNode The menu node of reference.
	 */
	void fireViewedByLoading(TreeBrowserSet refNode)
	{
		Object ho = getParentObject(refNode);
		if (ho instanceof ImageData) {
			ImageData img = (ImageData) ho;
			long id = img.getDefaultPixels().getId();
			ViewedByLoader loader = new ViewedByLoader(component, refNode, 
													img.getId(), id);
			loaders.put(refNode, loader);
			loader.load();
		}
	}

	/**
	 * Starts the asynchronous retrieval of urls linked to a given object.
	 * 
	 * @param refNode The menu node of reference.
	 */
	void fireUrlLoading(TreeBrowserSet refNode)
	{
		cancel(refNode);
		Object ho = getParentObject(refNode);
		long id = -1;
		if (ho instanceof DataObject)
			id = ((DataObject) ho).getId();
		UrlLoader loader = new UrlLoader(component, refNode, ho.getClass(), id);
		loaders.put(refNode, loader);
		loader.load();
	}

	/**
	 * Starts the asynchronous retrieval of the structured data related
	 * to the passed node.
	 * 
	 * @param refNode The node to handle.
	 */
	void fireStructuredDataLoading(TreeBrowserDisplay refNode)
	{
		cancel(refNode);
		StructuredDataLoader loader = new StructuredDataLoader(component, refNode, refNode.getUserObject());
	}

	
}
