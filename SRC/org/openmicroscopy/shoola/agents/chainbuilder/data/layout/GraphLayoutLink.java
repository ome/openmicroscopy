/*
 * org.openmicroscopy.shoola.agents.viewer.Viewer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
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

package org.openmicroscopy.shoola.agents.chainbuilder.data.layout;

import java.util.Iterator;

/**
 * @author <br>Harry Hochheiser &nbsp;&nbsp;&nbsp;<A HREF="mailto:hsh@nih.gov">hsh@nih.gov</A>
 *
 *  @version 2.2
 * <small>
 * </small>
 * @since OME2.2
 */
public interface GraphLayoutLink {
	/**
	 * Insert a node into the Link
	 * @param prior the new node should be inserted immediately after this node
	 * @param newNode the node to be inserted.
	 */
	public abstract void addIntermediate(GraphLayoutNode prior,
			GraphLayoutNode newNode);

	public abstract GraphLayoutNode getIntermediateNode(int i);

	public abstract void setFromNode(GraphLayoutNode node);

	public abstract void setToNode(GraphLayoutNode node);

	public abstract Iterator getNodeIterator();
	
	public abstract GraphLayoutNode getLayoutFromNode();
	
	public abstract GraphLayoutNode getLayoutToNode();
}