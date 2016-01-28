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
 * Determines the nodes similar to a specified collection of original nodes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SimilarNodesVisitor 
	implements TreeImageDisplayVisitor
{

	/** The collection of nodes found. */
	private List<TreeImageDisplay> foundNodes;
	
	/** The collection of nodes of reference. */
	private List<TreeImageDisplay> nodes;
	
	/**
	 * Checks if the passed node is similar to the ones contained in the
	 * original list of nodes.
	 * 
	 * @param node The 
	 */
	private void handleNode(TreeImageDisplay node)
	{
		if (node == null) return;
		Object ho = node.getUserObject();
		if (ho == null || !(ho instanceof DataObject)) return;
		DataObject ref = (DataObject) ho;
		Iterator<TreeImageDisplay> i = nodes.iterator();
		TreeImageDisplay n;
		Object uo;
		DataObject data;
		while (i.hasNext()) {
			n = i.next();
			uo = n.getUserObject();
			if (uo instanceof DataObject) {
				data = (DataObject) uo;
				if (data.getClass().equals(ref.getClass()) &&
					data.getId() == ref.getId()) {
					foundNodes.add(node);
					break;
				}
			}
		}
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodes The collection of original nodes.
	 */
	public SimilarNodesVisitor(List<TreeImageDisplay> nodes)
	{
		if (nodes == null || nodes.size() == 0) 
			throw new IllegalArgumentException("No nodes of reference.");
		this.nodes = nodes;
		foundNodes = new ArrayList<TreeImageDisplay>();
	}
	
	/**
	 * Returns the nodes found.
	 * 
	 * @return See above.
	 */
	public List<TreeImageDisplay> getFoundNodes() { return foundNodes; }
	
	/**
	 * Checks the node is similar to a node from the list of original nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageNode)
	 */
	public void visit(TreeImageNode node) { handleNode(node); }

	/** 
	 * Checks the node is similar to a node from the list of original nodes.
	 * @see TreeImageDisplayVisitor#visit(TreeImageSet)
	 */
	public void visit(TreeImageSet node) { handleNode(node); }
	
}
