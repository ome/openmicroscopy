/*
 * org.openmicroscopy.shoola.agents.chainbuidler.data.LayoutNodeData
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleView;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;

/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.AnalysisNodeData}, 
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
public class LayoutNodeData  extends AnalysisNodeData 
	implements GraphLayoutNode
{
	/**Chains are laid out according to a la
	 * yered algorithm for digraphs. 
	 * (see Di Battista, Eades, Tamassia, and Tollis, _Graph_Drawing_ 
	 * Chapter 9). Layer indicates the depth in the chain, with 0 being leaf 
	 * nodes and n-1 being source nodes (nodes with no predecessors).
	 */
	private int layer = -1;
	
	/**
	 * The chain layout algorithm assigns an ordering to each node in a layer.
	 * This field stores the node's position in its layer
	 */
	private double posInLayer = 0.0;
	
	/**
	 * Store the successors and predecessors, along with appropriate links
	 * in Hashes.
	 */
	protected HashSet succLinks = new HashSet();
	protected HashSet predLinks = new HashSet();
	protected HashSet succs  = new HashSet();
	protected HashSet preds = new HashSet();
		
	public LayoutNodeData() {}
	
	private ModuleView displayModule;
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new LayoutNodeData(); }

	public String getName() {
		return getModule().getName();
	}
 	
	/**
	 * 
	 * This call sets the layer for the node
	 * @param layer the new layer
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}

	/**
	 * 
	 * @return the layer that this node is in
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * A node has a layer assigned if its layer is not -1
	 * @return true if the node has a layer, else false
	 */
	public boolean hasLayer() {
		return (!(layer == -1));
	} 
	
	/**
	 * Set the position of the node in its layer
	 * @param p the new position
	 */
	public void setPosInLayer(double p) {
		posInLayer = p;
	}

	/**
	 *
	 * @return the position of the node in the layer
	 */
	public double getPosInLayer() {
		return posInLayer;
	}

	/**
	 * Construct the lists of predcessors and successors. 
	 * The implementation of the remote framework requires that this call not 
	 * be made in the constructor.
	 *
	 */

	public void buildLinkLists() {
		buildSuccessors();
		buildPredecessors();
	}

	/**
	 * To get the list of successors, iterate over the output links, 
	 * looking at the destination node of each. If that node is not already 
	 * in the list of successors, add it, create a {@link LayoutLink}, 
	 * and add that to the list of successor links, and to the list of 
	 * predecessor links for the destination node.
	 * 
	 * This gurantees that the resulting structure will only have 1 link 
	 * between any two nodes, even if the original graph has several such links 
	 *
	 */
	private void buildSuccessors() {

		Collection outputs = getOutputLinks();
		if (outputs == null)
			return;
		Iterator iter = outputs.iterator();
		while (iter.hasNext()) {
			LayoutLinkData link = (LayoutLinkData)iter.next();
			LayoutNodeData node = (LayoutNodeData) link.getToNode();
			if (!succs.contains(node)) {
				//	add a layout link?
				succs.add(node);
				LayoutLink layoutLink = new LayoutLink(link);
				succLinks.add(layoutLink);
				node.addPredLink(layoutLink);
			}
		}
	}
	
	public Collection getSuccessors() {
		return succs;
	}

	/**
	 * Analogous to buildSuccessors
	 *
	 */
	private void buildPredecessors() {
		Collection inputs = getInputLinks();
		if (inputs == null)
			return;
		Iterator iter = inputs.iterator();
		while (iter.hasNext()) {
			LayoutLinkData link = (LayoutLinkData) iter.next();
			LayoutNodeData node = (LayoutNodeData) link.getFromNode();
			if (!preds.contains(node)) {
				preds.add(node);
				LayoutLink layoutLink = new LayoutLink(link);
				predLinks.add(layoutLink);
				node.addSuccLink(layoutLink);
			}
		}
	}

	public  Collection getPredecessors() {
		return preds;
	}

	/**
	 * Remove a successor link
	 * @param link the link to be removed
	 */
	public void removeSuccLink(LayoutLink link) {
		if (succLinks != null) {
			succLinks.remove(link);
			succs.remove(link.getToNode());
		}
	}

	/**
	 * Add a successor link
	 * @param link the link to be added
	 */
	public void addSuccLink(LayoutLink link) {
		LayoutNodeData node = (LayoutNodeData) link.getToNode();
		if (!succs.contains(node)) {
			succLinks.add(link);
			succs.add(node);
		}
	}

	public Iterator succLinkIterator() {
		return succLinks.iterator();
	}



	public void setSuccLinks(HashSet links) {
		succLinks = links;
	}


	/**
	 * Remove a predecessor link
	 * @param link the link to be removed
	 */	
	public void removePredLink(LayoutLink link) {
		if (predLinks != null) {
			predLinks.remove(link);
			preds.remove(link.getFromNode());
		}
	}

	/**
	 * Add a predecessor link
	 * @param link the link to be added
	 */
	public void addPredLink(LayoutLink link) {
		GraphLayoutNode node = link.getFromNode();
		if (!preds.contains(node)) {
			predLinks.add(link);
			preds.add(node);
		}
	}

	public Iterator predLinkIterator() {
		return predLinks.iterator();
	}

	/**
	 * Free the predecessor and other links 
	 * when we're done with them
	 */
	public void cleanupLinkLists() {
		preds = null;
		predLinks = null;
		succs = null;
		succLinks = null;
	}

	/**
	 * Set the widget corresponding to this node
	 * @param mod
	 */
	public void setModuleView(ModuleView mod) {
		displayModule = mod;
	}

	/**
	 * 
	 * @return the widget corresponding to this node
	 */
	public ModuleView getModuleView() {
		return displayModule;
	}
}
