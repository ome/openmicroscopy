/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

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
	
	/** 
	 * Links up the MVC triad. 
	 * 
	 * @param comp The component to register.
	 */
	void initialize(ObservableComponent comp)
	{
		controller.initialize(this, view);
		comp.addPropertyChangeListener(controller);
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
	 * @see Browser#loadMetadata(TreeBrowserDisplay)
	 */
	public void loadMetadata(TreeBrowserDisplay node)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node to handle.");
		model.loadMetadata(node);
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setParents(TreeBrowserDisplay, Collection)
	 */
	public void setParents(TreeBrowserDisplay node, Collection parents)
	{
		if (node == null) node = model.getRoot();
		//	throw new IllegalArgumentException("No node to handle.");
		if (parents == null || parents.size() == 0) {
			view.addDefaultNode(node, null);//BrowserUI.NO_PARENTS_MSG);
			return;
		}
		Iterator i = parents.iterator();
		List<TreeBrowserDisplay> nodes = new ArrayList<TreeBrowserDisplay>();
		Object uo;
		while (i.hasNext()) {
			uo = i.next();
			if (uo instanceof ProjectData)
				nodes.add(new TreeBrowserNode(uo));
			else if (uo instanceof ScreenData)
				nodes.add(new TreeBrowserNode(uo));
			else if (uo instanceof TagAnnotationData) {
				TagAnnotationData tag = (TagAnnotationData) uo;
				if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
						tag.getNameSpace()))
					nodes.add(new TreeBrowserNode(uo));
			} else nodes.add(new TreeBrowserSet(uo));
		}
		view.setNodes(node, nodes);
	}
	
}
