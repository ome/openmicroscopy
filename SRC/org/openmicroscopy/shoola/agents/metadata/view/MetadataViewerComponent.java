/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerComponent 
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
import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.Map;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserSet;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Implements the {@link MetadataViewer} interface to provide the functionality
 * required of the hierarchy viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
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
class MetadataViewerComponent 
	extends AbstractComponent
	implements MetadataViewer
{

	/** The Model sub-component. */
	private MetadataViewerModel 	model;
	
	/** The Control sub-component. */
	private MetadataViewerControl	controller;
	
	/** The View sub-component. */
	private MetadataViewerUI 		view;
	
	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	MetadataViewerComponent(MetadataViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		controller = new MetadataViewerControl();
		view = new MetadataViewerUI();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(controller, model);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
				view.setOnScreen();
				break;
			case DISCARDED:
				throw new IllegalStateException(
						"This method can't be invoked in the DISCARDED state.");
		} 
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#discard()
	 */
	public void discard()
	{
		model.discard();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#showMenu(Component, Point)
	 */
	public void showMenu(Component invoker, Point loc)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		view.showMenu(invoker, loc);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#cancel(TreeBrowserDisplay)
	 */
	public void cancel(TreeBrowserDisplay refNode) { model.cancel(refNode); }

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#loadMetadata(TreeBrowserDisplay)
	 */
	public void loadMetadata(TreeBrowserDisplay node)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (node == null)
			throw new IllegalArgumentException("No node specified.");
		Object userObject = node.getUserObject();
		if (userObject instanceof DataObject) {
			model.fireStructuredDataLoading(node);
		} else if (userObject instanceof String) {
			String name = (String) userObject;
			if (Browser.TAGS.equals(name)) 
				model.fireTagLoading((TreeBrowserSet) node);
			else if (Browser.ATTACHMENTS.equals(name)) 
				model.fireAttachmentLoading((TreeBrowserSet) node);
			else if (Browser.DATASETS.equals(name)) 
				model.fireParentLoading((TreeBrowserSet) node, DatasetData.class);
			else if (Browser.PROJECTS.equals(name)) 
				model.fireParentLoading((TreeBrowserSet) node, ProjectData.class);
			else if (Browser.VIEWED_BY.equals(name)) 
				model.fireViewedByLoading((TreeBrowserSet) node);
			else if (Browser.PROPERTIES.equals(name)) 
				model.fireAttachmentLoading((TreeBrowserSet) node);
			else if (Browser.URL.equals(name)) 
				model.fireUrlLoading((TreeBrowserSet) node);
		}
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setMetadata(TreeBrowserDisplay, Object)
	 */
	public void setMetadata(TreeBrowserDisplay node, Object result)
	{
		if (node == null)
			throw new IllegalArgumentException("No node specified.");
		Object userObject = node.getUserObject();
		if (!(userObject instanceof String)) return;
		String name = (String) userObject;
		Browser browser = model.getBrowser();
		if (browser == null) return;
		if (Browser.TAGS.equals(name)) 
			browser.setTags((TreeBrowserSet) node, (Collection) result);
		else if (Browser.ATTACHMENTS.equals(name)) 
			browser.setAttachments((TreeBrowserSet) node, (Collection) result);
		else if (Browser.DATASETS.equals(name) || Browser.PROJECTS.equals(name)) 
			browser.setParents((TreeBrowserSet) node, (Collection) result);
		else if (Browser.VIEWED_BY.equals(name)) 
			browser.setViewedBy((TreeBrowserSet) node, (Map) result);
		else if (Browser.URL.equals(name)) 
			browser.setUrls((TreeBrowserSet) node, (Collection) result);
		model.notifyLoadingEnd(node);
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#getSelectionUI()
	 */
	public JComponent getSelectionUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return model.getBrowser().getUI();
	}

	/** 
	 * Implemented as specified by the {@link MetadataViewer} interface.
	 * @see MetadataViewer#setRootObject(Object)
	 */
	public void setRootObject(Object root)
	{
		// TODO: Check state.
		model.setRefObject(root);
	}
	
}
