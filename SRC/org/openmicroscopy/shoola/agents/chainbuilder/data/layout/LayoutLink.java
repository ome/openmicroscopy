/*
 * org.openmicroscopy.vis.chains.ome.LayoutLink
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.data.layout;

 

/** 
 * A placeholder class that is used during GraphLayout as the direct node-node 
 * components that get combined to build a {@link  LayoutLinkData} object that can be 
 * stored directly in the database. During layout, these links connect either 
 * {@link GraphLayoutNode} objects or {@link DummyNode} dummy nodes. Unlike {@link LayoutLinkData} 
 * objects, these objects can only connect adjacent layers in the graph. Once 
 * the graph is laid out, LayoutLink objects are no longer needed.
 * 
  @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
 public class LayoutLink {
	
	private GraphLayoutNode toNode;
	private GraphLayoutNode fromNode;
	
	/**
	 * the actual graph link that this dummy link is really part of.
	 */ 
	private LayoutLinkData semanticLink;
	
	public LayoutLink() {
		super();
	}
	
	public LayoutLink(LayoutLinkData semanticLink,GraphLayoutNode fromNode,GraphLayoutNode toNode) {
		this.semanticLink = semanticLink;
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public LayoutLink(LayoutLinkData link) {
		this.fromNode = (GraphLayoutNode) link.getFromNode();
		this.toNode = (GraphLayoutNode) link.getToNode();
		this.semanticLink = link;
	}
 	
 	public LayoutLinkData getSemanticLink() {
 		return semanticLink;
 	}
 	
	public GraphLayoutNode getToNode() {
		return toNode;
	}
	
	public GraphLayoutNode getFromNode() {
		return fromNode;
	}
	
	// we want this to throw an exception if we're ever setting it
	// to anything that can't be cast to be a GraphLayoutNode.
	public void setToNode(GraphLayoutNode node) {
		toNode =  node;
	}
	
	public void setFromNode(GraphLayoutNode node) {
		fromNode = node;
	}
	
	
}

 
 