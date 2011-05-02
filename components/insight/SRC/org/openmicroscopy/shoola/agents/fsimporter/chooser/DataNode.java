/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.DataNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Hosts the node for display.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class DataNode
{

	/** The default text if no screen. */
	static final String DEFAULT_SCREEN = "--No Screen--";
	
	/** The default text if no dataset. */
	static final String DEFAULT_DATASET = "--No Dataset--";
	
	/** The default text if no project. */
	private static final String DEFAULT_PROJECT = "--No Project--";

	/** The data to host. */
	private DataObject data;
	
	/** The node of reference. */
	private TreeImageDisplay refNode;
	
	/** Flag indicating that this node is to link the orphaned datasets. */
	private boolean orphanParent;
	
	/** The orphaned nodes. */
	private List<DataNode> children;
	
	/** The parent node. */
	private DataNode parent;
	
	/**
	 * Creates a dataset with default name.
	 * 
	 * @return See above.
	 */
	static DatasetData createDefaultDataset()
	{
		DatasetData d = new DatasetData();
		d.setName(DEFAULT_DATASET);
		return d;
	}
	
	/**
	 * Creates a project with default name.
	 * 
	 * @return See above.
	 */
	static ProjectData createDefaultProject()
	{
		ProjectData d = new ProjectData();
		d.setName(DEFAULT_PROJECT);
		return d;
	}
	
	/**
	 * Creates a screen with default name.
	 * 
	 * @return See above.
	 */
	static ScreenData createDefaultScreen()
	{
		ScreenData d = new ScreenData();
		d.setName(DEFAULT_SCREEN);
		return d;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 */
	DataNode(DataObject data)
	{
		if (data == null)
			throw new IllegalArgumentException("No Object specified.");
		this.data = data;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param data The data to host.
	 * @param parent The parent of the node.
	 */
	DataNode(DataObject data, DataNode parent)
	{
		if (data == null)
			throw new IllegalArgumentException("No Object specified.");
		this.data = data;
		this.parent = parent;
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param children The orphaned nodes.
	 */
	DataNode(List<DataNode> children)
	{
		orphanParent = true;
		this.data = createDefaultProject();
		this.children = children;
	}
	
	/**
	 * Sets the data node.
	 * 
	 * @param data The data to reset.
	 */
	void setData(DataObject data)
	{
		this.data = data;
	}
	
	/**
	 * Sets the node of reference.
	 * 
	 * @param refNode The node of reference.
	 */
	void setRefNode(TreeImageDisplay refNode) { this.refNode = refNode; }
	
	/**
	 * Returns the node of reference.
	 * 
	 * @return See above.
	 */
	TreeImageDisplay getRefNode() { return refNode; }
	
	/**
	 * Returns the data object.
	 * 
	 * @return See above.
	 */
	DataObject getDataObject() { return data; }
	
	/**
	 * Returns <code>true</code> if the object corresponding to the passed 
	 * name is new, <code>false</code> otherwise.
	 * 
	 * @param name The name to handle.
	 * @return See above.
	 */
	boolean isNewDataObject(String name)
	{
		if (orphanParent) return false;
		return toString().equals(name) && data.getId() <= 0;
	}
	
	/**
	 * Returns the list of nodes hosted.
	 * 
	 * @return See above.
	 */
	List<DataNode> getDatasetNodes()
	{
		if (children != null) return children;
		children = new ArrayList<DataNode>();
		if (data instanceof ProjectData) {
			if (refNode != null) {
				List l = refNode.getChildrenDisplay();
				if (l != null) {
					Iterator i = l.iterator();
					TreeImageDisplay node;
					DataNode n;
					while (i.hasNext()) {
						node = (TreeImageDisplay) i.next();
						n = new DataNode((DataObject) node.getUserObject());
						n.setRefNode(node);
						n.parent = this;
						children.add(n);
					}
				}
			}
		}
		return children;
	}

	/**
	 * Adds the new node.
	 * 
	 * @param node The node to add.
	 */
	void addNode(DataNode node)
	{
		if (node == null) return;
		if (children == null) children = new ArrayList<DataNode>();
		node.parent = this;
		children.add(node);
	}
	
	/**
	 * Returns <code>true</code> if the node is a default node, 
	 * <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	boolean isDefaultNode()
	{
		String name = toString();
		return (DEFAULT_PROJECT.equals(name) || DEFAULT_DATASET.equals(name) ||
				DEFAULT_SCREEN.equals(name));
	}
	
	/**
	 * Returns the parent node.
	 * 
	 * @return See above.
	 */
	DataNode getParent() { return parent; }
	
	/**
	 * Overridden to set the name of the object.
	 * @see #toString()
	 */
	public String toString()
	{ 
		if (data instanceof DatasetData)
			return ((DatasetData) data).getName();
		else if (data instanceof ProjectData) 
			return ((ProjectData) data).getName();
		else if (data instanceof ScreenData) 
			return ((ScreenData) data).getName();
		return ""; 
	}
	
}
