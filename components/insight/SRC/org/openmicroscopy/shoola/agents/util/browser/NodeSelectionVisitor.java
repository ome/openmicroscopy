/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.gateway.model.DataObject;

/** 
 * Finds the {@link TreeImageDisplay} hosting the selected 
 * <code>DataObject</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class NodeSelectionVisitor 
	implements TreeImageDisplayVisitor
{

	/** The parent of the selected object if any. */
	private Object 				parent;
	
	/** The selected object. */
	private DataObject 			selected;
	
	/** The node corresponding to the selected object. */
	private TreeImageDisplay	selectedNode;
	
	/** The selected objects. */
	private List<DataObject> 	selection;
	
	/** The selected objects. */
	private List<TreeImageDisplay> 	selectedNodes;
	
	/**
	 * Finds the node hosting the selected <code>DataObject</code>.
	 * 
	 * @param node The node to check.
	 */
	private void findNode(TreeImageDisplay node)
	{
		if (node == null) return;
		if (parent == null) {
			Object uo = node.getUserObject();
			if (selected.getClass().equals(uo.getClass()) &&
				selected.getId() == node.getUserObjectId()) {
				selectedNode = node;
			}
		} else {
			Object uo = node.getUserObject();
			TreeImageDisplay pN;
			if (parent instanceof String) {
				String key = parent.toString();
				if (uo instanceof DataObject) {
					pN = node.getParentDisplay();
					String n = pN.toString();
					if (key.equals(n) && selected.getId() ==
						((DataObject) uo).getId()) {
						selectedNode = node;
					}
				}
			} else {
				if (selected.getClass().equals(uo.getClass()) &&
						selected.getId() == node.getUserObjectId()) {
					pN = node.getParentDisplay();
					Object po = pN.getUserObject();
					if (po.getClass().equals(parent.getClass()))
						if (po instanceof DataObject 
								&& parent instanceof DataObject) {
							if (((DataObject) po).getId() == 
								((DataObject) parent).getId())
								selectedNode = node;
						}
				}
			}
		}
	}
	
	/**
	 * Finds the node hosting the selected <code>DataObject</code>.
	 * 
	 * @param node The node to check.
	 */
	private void findNodeFromSelection(TreeImageDisplay node)
	{
		if (node == null) return;
		Iterator<DataObject> i = selection.iterator();
		DataObject object;
		if (parent == null) {
			Object uo = node.getUserObject();
			while (i.hasNext()) {
				object = i.next();
				if (object.getClass().equals(uo.getClass()) &&
						object.getId() == node.getUserObjectId()) {
						selectedNodes.add(node);
				}
			}
		} else {
			Object uo = node.getUserObject();
			TreeImageDisplay pN;
			if (parent instanceof String) {
				String key = parent.toString();
				if (uo instanceof DataObject) {
					pN = node.getParentDisplay();
					String n = pN.toString();
					while (i.hasNext()) {
						object = i.next();
						if (key.equals(n) && object.getId() ==
							((DataObject) uo).getId()) {
								selectedNodes.add(node);
						}
					}
				}
			} else {
				while (i.hasNext()) {
					object = i.next();
					if (object.getClass().equals(uo.getClass()) &&
							object.getId() == node.getUserObjectId()) {
						pN = node.getParentDisplay();
						Object po = pN.getUserObject();
						if (po.getClass().equals(parent.getClass()))
							if (po instanceof DataObject 
									&& parent instanceof DataObject) {
								if (((DataObject) po).getId() == 
									((DataObject) parent).getId())
									selectedNodes.add(node);
							}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the selected node.
	 * @param selected	The selected node.
	 */
	public NodeSelectionVisitor(Object parent, DataObject selected)
	{
		if (selected == null)
			throw new IllegalArgumentException("No node selected.");
		this.parent = parent;
		this.selected = selected;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent	The parent of the selected node.
	 * @param selection	The selected nodes.
	 */
	public NodeSelectionVisitor(Object parent, List<DataObject> selection)
	{
		if (selection == null)
			throw new IllegalArgumentException("No node selected.");
		this.parent = parent;
		this.selection = selection;
		selectedNodes = new ArrayList<TreeImageDisplay>();
	}
	
	/**
	 * Returns the node corresponding to the selected object. 
	 * 
	 * @return See above.
	 */
	public List<TreeImageDisplay> getSelectedNodes() { return selectedNodes; }
	
	/**
	 * Returns the node corresponding to the selected object. 
	 * 
	 * @return See above.
	 */
	public TreeImageDisplay getSelectedNode() { return selectedNode; }
	
	/**
	 * Retrieves the {@link TreeImageDisplay} corresponding to the 
	 * the selected node.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node)
	{
		if (selection != null) findNodeFromSelection(node);
		else if (selected != null) findNode(node);
	}
	
	/** 
	 * Retrieves the {@link TreeImageDisplay} corresponding to the 
	 * the selected node.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node)
	{
		if (selection != null) findNodeFromSelection(node);
		else if (selected != null) findNode(node);
	}
	
}
