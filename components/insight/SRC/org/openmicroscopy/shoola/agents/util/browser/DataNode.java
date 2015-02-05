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
package org.openmicroscopy.shoola.agents.util.browser;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.util.EditorUtil;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
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
public class DataNode
{

	/** The default text if no screen. */
	static final String DEFAULT_SCREEN = "--No Screen--";
	
	/** The default text if no dataset. */
	static final String DEFAULT_DATASET = "--New From Folder--";
	
	/** The default text if no project. */
	private static final String DEFAULT_PROJECT = "--No Project--";

	/** The default text if no dataset. */
    private static final String NO_DATASET = "--No Dataset--";

	/** The data to host. */
	private DataObject data;
	
	/** The node of reference. */
	private TreeImageDisplay refNode;
	
	/** Flag indicating that this node is to link the orphaned datasets. */
	private boolean orphanParent;
	
	/** The orphaned nodes. */
	private List<DataNode> children;
	
	/** The children of the parent node */
	private List<DataNode> uiChildren;
	
	/** The parent node. */
	private DataNode parent;
	
	/** The collection of nodes to add.*/
	private List<DataNode> newNodes;

	/**
	 * Creates a dataset with default name.
	 * 
	 * @return See above.
	 */
	public static DatasetData createNoDataset()
	{
	    DatasetData d = new DatasetData();
	    d.setName(NO_DATASET);
	    return d;
	}

	/**
	 * Creates a dataset with default name.
	 * 
	 * @return See above.
	 */
	public static DatasetData createDefaultDataset()
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
	public static ProjectData createDefaultProject()
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
	public static ScreenData createDefaultScreen()
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
	public DataNode(DataObject data)
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
	public DataNode(DataObject data, DataNode parent)
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
	public DataNode(List<DataNode> children)
	{
		orphanParent = true;
		this.data = createDefaultProject();
		this.children = children;
	}
	
	/**
	 * Sets the parent of the node.
	 * 
	 * @param parent The value to set.
	 */
	public void setParent(DataNode parent) { this.parent = parent; }
	
	/**
	 * Sets the data node.
	 * 
	 * @param data The data to reset.
	 */
	public void setData(DataObject data)
	{
		this.data = data;
	}
	
	/**
	 * Sets the node of reference.
	 * 
	 * @param refNode The node of reference.
	 */
	public void setRefNode(TreeImageDisplay refNode) { this.refNode = refNode; }
	
	/**
	 * Returns the node of reference.
	 * 
	 * @return See above.
	 */
	public TreeImageDisplay getRefNode() { return refNode; }
	
	/**
	 * Returns the data object.
	 * 
	 * @return See above.
	 */
	public DataObject getDataObject() { return data; }
	
	/**
	 * Returns the user who owns the data.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getOwner()
	{
		if (data.getId() < 0) return null;
		if (data instanceof GroupData || data instanceof ExperimenterData)
			return null;
		return data.getOwner();
	}
	
	/**
	 * Returns <code>true</code> if the object corresponding to the passed 
	 * name is new, <code>false</code> otherwise.
	 * 
	 * @param name The name to handle.
	 * @return See above.
	 */
	public boolean isNewDataObject(String name)
	{
		if (orphanParent) return false;
		return toString().equals(name) && data.getId() <= 0;
	}
	
	/**
	 * Returns the collection of new nodes.
	 * 
	 * @return See above.
	 */
	public List<DataNode> getNewNodes() { return newNodes; }
	
	/**
	 * Adds the specified node.
	 * 
	 * @param node The node to add.
	 */
	public void addNewNode(DataNode node)
	{
		if (newNodes == null) newNodes = new ArrayList<DataNode>();
		if (node != null) {
			if (!isDefaultNode() && children != null) {
				Iterator<DataNode> i = children.iterator();
				DataNode child;
				DataNode toRemove = null;
				while (i.hasNext()) {
					child = i.next();
					if (child.isDefaultNode()) {
						toRemove = child;
						break;
					}
				}
				if (toRemove != null) children.remove(toRemove);
			}
			newNodes.add(node);
		}
	}
	
	/**
	 * Returns the list of nodes hosted.
	 * 
	 * @return See above.
	 */
	public List<DataNode> getDatasetNodes()
	{
		if (children != null) return children;
		children = new ArrayList<DataNode>();
		if (data instanceof ProjectData) {
			if (refNode != null) {
				List<?> l = refNode.getChildrenDisplay();
				if (CollectionUtils.isNotEmpty(l)) {
					Iterator<?> i = l.iterator();
					TreeImageDisplay node;
					DataNode n;
					while (i.hasNext()) {
						node = (TreeImageDisplay) i.next();
						n = new DataNode((DataObject) node.getUserObject());
						n.setRefNode(node);
						n.parent = this;
						children.add(n);
					}
					children.add(new DataNode(
								DataNode.createDefaultDataset(), this));
				} else {
					children.add(new DataNode(DataNode.createDefaultDataset(),
							this));
				}
			}
		}
		return children;
	}

	/**
	 * Returns the list of nodes hosted.
	 * 
	 * @return See above.
	 */
	public List<DataNode> getUIDatasetNodes()
	{
		if (orphanParent) return children;
		if (uiChildren != null) return uiChildren;
		uiChildren = new ArrayList<DataNode>();
		if (data instanceof ProjectData) {
			ProjectData project= (ProjectData) data;
			Set<DatasetData> datasets =project.getDatasets();
			Iterator<DatasetData> i = datasets.iterator();
			DataNode n;
			while (i.hasNext()) {
				n = new DataNode(i.next());
				uiChildren.add(n);
				n.parent = this;
			}
		}
		return uiChildren;
	}
	
	/**
	 * Adds the new node.
	 * 
	 * @param node The node to add.
	 */
	public void addNode(DataNode node)
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
	public boolean isDefaultNode()
	{
		return (isDefaultProject() || isDefaultScreen() || isDefaultDataset() ||
		        isNoDataset());
	}
	
	/**
     * Returns <code>true</code> if the node is a default node for dataset,
     * <code>false</code> otherwise.
     *  
     * @return See above.
     */
    public boolean isNoDataset()
    { 
        return NO_DATASET.equals(toString().trim());
    }

	/**
	 * Returns <code>true</code> if the node is a default node for project,
	 * <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	public boolean isDefaultProject()
	{ 
		return DEFAULT_PROJECT.equals(toString().trim());
	}
	
	/**
	 * Returns <code>true</code> if the node is a default node for screen,
	 * <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	public boolean isDefaultScreen()
	{ 
		return DEFAULT_SCREEN.equals(toString().trim());
	}
	
	/**
	 * Returns <code>true</code> if the node is a default node for dataset,
	 * <code>false</code> otherwise.
	 *  
	 * @return See above.
	 */
	public boolean isDefaultDataset()
	{ 
		return DEFAULT_DATASET.equals(toString().trim());
	}
	
	/**
	 * Returns the parent node.
	 * 
	 * @return See above.
	 */
	public DataNode getParent() { return parent; }
	
	/**
	 * Overridden to set the name of the object.
	 * @see #toString()
	 */
	public String toString()
	{
		return EditorUtil.truncate(getFullName(), 48);
	}
	
	/**
	 * Returns the full length name of the DataNode
	 * 
	 * @return see above.
	 */
	public String getFullName()
	{
		if (data instanceof DatasetData)
			return ((DatasetData) data).getName() + " ";
		else if (data instanceof ProjectData)
			return ((ProjectData) data).getName() + " ";
		else if (data instanceof ScreenData)
			return ((ScreenData) data).getName() + " ";
		else if (data instanceof GroupData)
		    return ((GroupData) data).getName() + " ";
		return "";
	}
}
