/*
 * org.openmicroscopy.shoola.agents.chainbuidler.data.LayoutLinkData
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

//Java imports
import java.util.Iterator;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;

/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.AnalysisChainData}, 
 * adding some state for {@link ModuleView} layout of the chain
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class LayoutLinkData  extends AnalysisLinkData
{
	
	public LayoutLinkData(AnalysisChainData chain,AnalysisNodeData 
			fromNode,FormalOutputData fromOutput,AnalysisNodeData toNode,
			FormalInputData toInput) 
	{
		super(chain,fromNode,fromOutput,toNode,toInput);
		setFromNode(fromNode);
		setToNode(toNode);
	}
	
	public LayoutLinkData() {}
	/*
	 * A list of nodes that might include internal CLayoutNodes. This list is 
	 * needed to reconstruct the multiple points in a link that might be needed
	 * when doing an automated layout.
	 */
	private Vector nodes = new Vector();
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { 
		return new LayoutLinkData(); }
		
	
	/**
	 * Insert a node into the Link
	 * @param prior the new node should be inserted immediately after this node
	 * @param newNode the node to be inserted.
	 */
	public void addIntermediate(GraphLayoutNode prior,GraphLayoutNode newNode) {
		int index = nodes.indexOf(prior);
		nodes.insertElementAt(newNode,index+1);
	}
	
	public GraphLayoutNode getIntermediateNode(int i) {
		return (GraphLayoutNode) nodes.elementAt(i);
	}
	
	public void setFromNode(AnalysisNodeData node) {
		super.setFromNode(node);
		if (nodes.size() == 0)
			nodes.add(node);
		else
			nodes.setElementAt((LayoutNodeData) node,0);
	}
	
	public void setToNode(AnalysisNodeData node) {
		super.setToNode(node);
		//int sz = nodes.size();
		//nodes.setElementAt((LayoutNodeData) node,sz); /// was -1
		nodes.add(node);
	}
	
	public Iterator getNodeIterator() {
		return nodes.iterator();
	}
}
