/*
 * org.openmicroscopy.shoola.agents.metadata.browser.BrowserComponent 
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
package org.openmicroscopy.shoola.agents.metadata.browser;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.viewedby.ViewedByComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.ImageData;

/** 
 * Implements the {@link Browser} interface to provide the functionality
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
class BrowserComponent
	extends AbstractComponent
	implements Browser
{

	/** The Model sub-component. */
	private BrowserModel	model;
	
	/** The Control sub-component. */
	private BrowserControl	controller;
	
	/** The View sub-component. */
	private BrowserUI		view;

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	BrowserComponent(BrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		view = new BrowserUI();
		controller = new BrowserControl();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(model, controller);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getUI()
	 */
	public JComponent getUI() { return view; }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getLastSelectedNode()
	 */
	public TreeBrowserDisplay getLastSelectedNode()
	{
		return model.getLastSelectedNode();
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedNodes(List)
	 */
	public void setSelectedNodes(List<TreeBrowserDisplay> nodes)
	{
		List n = model.getSelectedNodes();
		model.setSelectedDisplays(nodes);
		firePropertyChange(SELECTED_NODES_PROPERTY, n, nodes);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelectedNode(TreeBrowserDisplay)
	 */
	public void setSelectedNode(TreeBrowserDisplay node)
	{
		List<TreeBrowserDisplay> nodes = new ArrayList<TreeBrowserDisplay>(1);
		nodes.add(node);
		setSelectedNodes(nodes);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setRootObject(Object)
	 */
	public void setRootObject(Object refObject)
	{
		if (refObject == null)
			throw new IllegalArgumentException("Root object not valid.");
		model.setRootObject(refObject);
		view.setRootNode();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#cancel(TreeBrowserDisplay)
	 */
	public void cancel(TreeBrowserDisplay node)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		model.cancel(node);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#loadMetadata(TreeBrowserDisplay)
	 */
	public void loadMetadata(TreeBrowserDisplay node)
	{
		model.loadMetadata(node);
		
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setTags(TreeBrowserDisplay, Collection)
	 */
	public void setTags(TreeBrowserDisplay node, Collection tags)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		if (tags == null || tags.size() == 0) {
			view.addDefaultNode(node, BrowserUI.NO_TAGS_MSG);
			return;
		}
		Iterator i = tags.iterator();
		List<TreeBrowserSet> nodes = new ArrayList<TreeBrowserSet>();
		while (i.hasNext()) {
			nodes.add(new TreeBrowserSet(i.next()));
		}
		view.setNodes(node, nodes);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setViewedBy(TreeBrowserDisplay, Map)
	 */
	public void setViewedBy(TreeBrowserDisplay node, Map values)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		if (values == null || values.size() == 0) {
			view.addDefaultNode(node, BrowserUI.NOT_VIEWED_MSG);
			return;
		}
		Iterator i = values.keySet().iterator();
		List<TreeBrowserNode> nodes = new ArrayList<TreeBrowserNode>();
		while (i.hasNext()) {
			nodes.add(new TreeBrowserNode(i.next()));
		}
		view.setNodes(node, nodes);
		TreeBrowserDisplay parent = node.getParentDisplay();
		ImageData img = (ImageData) parent.getUserObject();
		//Test find a solution
		JDialog d = new JDialog();
		d.getContentPane().add(new ViewedByComponent(values, img));
		d.setSize(200, 200);
		UIUtilities.centerAndShow(d);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setAttachments(TreeBrowserDisplay, Collection)
	 */
	public void setAttachments(TreeBrowserSet node, Collection attachments)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		if (attachments == null || attachments.size() == 0) {
			view.addDefaultNode(node, BrowserUI.NO_ATTACHMENTS_MSG);
			return;
		}
		Iterator i = attachments.iterator();
		List<TreeBrowserSet> nodes = new ArrayList<TreeBrowserSet>();
		while (i.hasNext()) {
			nodes.add(new TreeBrowserSet(i.next()));
		}
		view.setNodes(node, nodes);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setParents(TreeBrowserDisplay, Collection)
	 */
	public void setParents(TreeBrowserSet node, Collection parents)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		if (parents == null || parents.size() == 0) {
			view.addDefaultNode(node, BrowserUI.NO_PARENTS_MSG);
			return;
		}
		Iterator i = parents.iterator();
		List<TreeBrowserSet> nodes = new ArrayList<TreeBrowserSet>();
		while (i.hasNext()) {
			nodes.add(new TreeBrowserSet(i.next()));
		}
		view.setNodes(node, nodes);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setUrls(TreeBrowserDisplay, Collection)
	 */
	public void setUrls(TreeBrowserSet node, Collection urls)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		if (urls == null || urls.size() == 0) {
			view.addDefaultNode(node, BrowserUI.NO_URLS_MSG);
			return;
		}
		Iterator i = urls.iterator();
		List<TreeBrowserNode> nodes = new ArrayList<TreeBrowserNode>();
		while (i.hasNext()) {
			nodes.add(new TreeBrowserNode(i.next()));
		}
		view.setNodes(node, nodes);
	}
	
}
