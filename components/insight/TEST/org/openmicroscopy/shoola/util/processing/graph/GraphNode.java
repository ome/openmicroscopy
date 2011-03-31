/*
 * org.openmicroscopy.shoola.util.processing.graph.GraphNode
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
 *----------------------------------------------------------------------------*/
package org.openmicroscopy.shoola.util.processing.graph;
//Java imports
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//Third-party libraries

import processing.core.PApplet;
import traer.physics.Particle;
//Application-internal dependencies

/** 
 * 
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
public class GraphNode
	implements NodeRenderer, Comparator
{
	/** The particle associated with the node. */
	Particle p;
	
	/** The nodeObject in the graph. */
	NodeObject nodeObject;
	
	/** The list of child nodes. */
	List<GraphNode> children;
	
	/** The parent node of this graphNode. */
	GraphNode parent;
	
	/**
	 * Set the node parent, nodeObject and particle.
	 * @param parent See above.
	 * @param nodeObject See above.
	 * @param p See above.
	 */
	GraphNode(NodeObject nodeObject, Particle p)
	{
		this.nodeObject = nodeObject;
		setNode(p);
		children = new ArrayList<GraphNode>();
		parent = null;
	}
	
	/**
	 * Set the node parent, nodeObject and particle to null.
	 * @param parent See above.
	 * @param nodeObject See above.
	 */
	GraphNode(NodeObject nodeObject)
	{
		this(nodeObject, null);
	}
	
	/**
	 * Set the particle this node refers to to p.
	 * @param p See above.
	 */
	public void setNode(Particle p)
	{
		this.p = p;
	}
	
	/**
	 * Get the particle associated with this node.
	 * @return See above.
	 */
	public Particle getNode()
	{
		return p;
	}
	
	/**
	 * Set the parent node for the GraphNode.
	 * @param parent See above.
	 */
	public void setParent(GraphNode parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Get the parent node of the current node
	 * @return See above.
	 */
	public GraphNode getParent()
	{
		return parent;
	}
	
	/** 
	 * Add the node to the child list of the node.
	 * @param node See above.
	 */
	public void addChild(GraphNode node)
	{
		children.add(node);
		node.setParent(this);
	}
	
	/**
	 * Remove the node from the child list.
	 * @param node See above.
	 */
	public void removeChild(GraphNode node)
	{
		children.remove(node);
	}
	
	/**
	 * Get the children of this node.  
	 * @return See above.
	 */
	public List<GraphNode> getChildren()
	{
		return children;
	}
	
	/**
	 * Find the node in the graph with the nodeObject equal to object, this is
	 * performed in a breadth first search
	 * @param object The node to search for.
	 * @return The Graphnode containing the object.
	 */
	public GraphNode findNodeObject(NodeObject object)
	{
		LinkedList<GraphNode> queue = new LinkedList<GraphNode>();
		queue.addLast(this);
		while(queue.size()!=0)
		{
			GraphNode node = queue.removeFirst();
			if(node.equals(object))
				return node;
			for(GraphNode child : node.getChildren())
				queue.addLast(child);
		}
		return null;
	}
	
	/**
	 * Implementation of {@see NodeObject#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		return nodeObject.compare(o1, o2);
	}
	
	/**
	 * Implementation of {@see NodeObject#equals(Object)
	 */
	public boolean equals(Object obj)
	{
		return nodeObject.equals(obj);
	}

	/**
	 * Implementation of {@see NodeObject#render(PApplet, float, float)
	 */
	public void render(PApplet parent, float x, float y)
	{
		nodeObject.render(parent, x, y);
	}
}
