/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.layout.Layering
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.chainbuilder.data.layout;
 
//Java imports
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies


/** 
 * A layering appropriat for DAG layout
 * 
 * * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public class Layering {

	/** Static parameters for layout */
	private static final int CROSSING_ITERATIONS=4;
	
	/** has the order of children changed? */
	private boolean orderChanged = false;	
	
	private Vector layers = new Vector();
	
	public Layering(List nodes) {
		layerNodes(nodes);
		flattenLayers();
		makeProper();
		reduceCrossings();
	}
	
	/** 
	* some code for computing a layered graph layout of this chain.
	* Builds on chapter 9  of Graph Drawing (di Battista, et al.),
	* and on GNU code from the Matrix Algorithm Simulation tool
	* 
	* 	http://www.cs.hut.fi/Research/Matrix/	      	   	
	*/
	
	/**
	 * Assigns layers to each of the nodes by finding the longest path to each 
	 * node. Essentially goes through until it finds things that don't have any 
	 * successors that don't have layers, assigns them to the current layer, 
	 * continues until all nodes have been checked, and then moves onto the next
	 * layer
	 */
	private void layerNodes(List nodes) {
		int numAssigned = 0, currentLayer = 0;
		boolean ok= true;
		int nodeCount = nodes.size();

		GraphLayoutNode node  = null;

		GraphLayoutNode succ;
		
		do {
			Iterator iter = nodes.iterator();
			while (iter.hasNext()) {
				ok = true;
				node = (GraphLayoutNode) iter.next();
					
				if (node.hasLayer())
					continue;
				
				Collection succs = node.getSuccessors();	
						
					
				Iterator succIter = succs.iterator();	
				while (succIter.hasNext()) {
					succ = (GraphLayoutNode) succIter.next();

					if (!succ.hasLayer() || succ.getLayer() == currentLayer) {
						// this is not ok.
						ok = false;
						break;
					}
				}
                
				if (ok == true) {
					//no unassigned successors, so assign this node to
					// the current layer
					node.setLayer(currentLayer);
					addToLayer(currentLayer,node);
					numAssigned++;
				}
			}
			currentLayer++;	
		} while (numAssigned < nodeCount);
	}
	
	
	/**
	 *  make the layout proper - insert additional nodes on paths 
	 *  that skip  levels .. ie., paths between levels l_i and l_j where j>i+1.
	 * 
	 */
	private void makeProper() {
		int count = getLayerCount();
		// for top (source) level and every level except for the last 2
		// (any outlinks from level 1 already go to only level 0, by
		// definition.
		for (int i = count-1; i>1; i--)
			makeProperLayer(i);
	}
	
	/**
	 * Make a layer proper by examining each of the nodes in the layer
	 * @param i the layer to make proper
	 */
	private void makeProperLayer(int i) {
		GraphLayoutNode node;
		try {
			Iterator iter = layerIterator(i);
			if (iter != null) {
				while (iter.hasNext()) {
					node = (GraphLayoutNode) iter.next();
					makeProperNode(node,i);		
				}
			}
		}
		catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	/**
	 * Make a node in a layer proper, by making all of its links proper and 
	 * giving it a new set of successor links.
	 * 
	 * @param node the node to make proper
	 * @param i the layer for that node
	 * 
	 */
	private void makeProperNode(GraphLayoutNode node,int i) {
		HashSet newLinks = new HashSet();
		Iterator iter = node.succLinkIterator();
		LayoutLink link;
		
		while (iter.hasNext()) {
			link = (LayoutLink) iter.next();
			makeProperLink(node,link,i,newLinks);
		}
		node.setSuccLinks(newLinks);
	}
	
	/**
	 * Make a given link proper If the link does not got to the next layer,
	 * create a new dummy node. this node will be on level i-i and will go 
	 * between node and its original destination.
	 * Note that if the link between the dummy node and the original destination
	 * is not itself proper, this will be fixed when level i-1 is made proper,
	 * potentially by creating a nother dummy node.
	 * 
	 * @param node the origin of the link
	 * @param link the link to make proper
	 * @param i the layer of the original node
	 * @param newLinks the new successors of that node.
	 */
	private void makeProperLink(GraphLayoutNode node,LayoutLink link,int i,
		Set newLinks) {
		// we know node is at i.
		
		GraphLayoutNode to = link.getToNode();
		int toLayer = to.getLayer();
		if (toLayer == (i-1)) {
			// layer is correct
			newLinks.add(link);
		}
		else {
			// create new dummy node
			DummyNode dummy = new DummyNode();
			GraphLayoutLink semanticLayoutLink = link.getSemanticLink(); 
				//was LayoutLinkData
			// make this node point to "to"
			LayoutLink dummyOutLink = new LayoutLink(semanticLayoutLink,dummy,to);
			dummy.addSuccLink(dummyOutLink);
			
			// make node point to new node
			LayoutLink newOutLink = new LayoutLink(semanticLayoutLink,node,dummy);
			
			dummy.addPredLink(newOutLink);
			
			// add new link to links.
			newLinks.add(newOutLink);
			
			// remove successor from node
			// don't need to do this, as we set the successors of this node
			// en masse 
			
			// adjust predecessors for to
			to.removePredLink(link);
			to.addPredLink(dummyOutLink);
			
			// add dummy to next layer.
			//System.err.println("adding a dummy node at layer"+(i-1));
			//System.err.println("node is "+dummy);
			addToLayer(i-1,dummy);
			
			// adjust the semantic link to put dummy in between "from" and "to".
			// invariant is that "from" is directly before "to", so just put it
			//after "from"
			semanticLayoutLink.addIntermediate(node,dummy);
		}
	}
	
	/** 
	 * Reduce the crossings in the graph by iterating the layers, first
	 * going forwards and then backwards, repeating this up to 
	 * CROSSING_ITERATIONS times, and stopping if a given iteration does 
	 * not permute the nodes in the layer. 
	 *
	 */
	private void reduceCrossings() {
		
		// first entry in layers is bottom layer (1 or zero)
		
		assignPosFromLayer(0);
		int count=1;
		
		for (int i =0;  i < CROSSING_ITERATIONS; i++) {
			orderChanged = false;
			
			
			for (count = 1; count < getLayerCount(); count++) {
				crossingReduction(count,false);
			}
			
			for (int curLayer = getLayerCount()-2; 
				curLayer>=0;curLayer--) {
				crossingReduction(curLayer,true);
			}
			//stop if no changes
			if (!orderChanged)
				break;
		}
	}
	
	/**
	 * To reduce the crossings between two layers, uterate over the nodes in 
	 *  one layer. Calculate the barycenter of the nodes in the other layer,
	 * and assign the node the position equal to that barycenter.
	 * Then, sort the layer by position and assign positions to each node.
	 *  
	 * @param layerNumber the source layer to be adjusted
	 * @param pred true if layerNumber should be adjusted 
	 * 	relative to predecessors, false if successors should be used.
	 */
	private void crossingReduction(int layerNumber,boolean pred) {
		try {
			// Iterator iter = layer.iterator(); 11/10/03 hsh
		//	System.err.println("crossing reduction - layer "+layerNumber);
			Iterator iter =layerIterator(layerNumber);
			GraphLayoutNode node;
			Collection adjs;
			double baryCenter=0.0;
		
			while (iter.hasNext()) {
				node = (GraphLayoutNode) iter.next();
				if (pred == true)
					adjs = node.getPredecessors();
				else	
					adjs = node.getSuccessors();
				if (adjs.size()>0)
					baryCenter = calcBaryCenter(adjs);
				else
					baryCenter = 0.0;
				node.setPosInLayer(baryCenter); 
			}
			sortLayerByPos(layerNumber);
			assignPosFromLayer(layerNumber);
		}
		catch(Exception e) {
			//System.err.println("exception caught!"); 
		}
	}
	
	/**
	 * The barycenter of the list of adjacent nodes is just the average of their
	 * positions
	 * @param adjs
	 * @return the barycenter of the adjacent nodes
	 */
	private double calcBaryCenter(Collection adjs) {
		double center=0.0;
		int deg = adjs.size();
		int total =0;
		Iterator iter = adjs.iterator();
		while (iter.hasNext()) {
			GraphLayoutNode c = (GraphLayoutNode) iter.next();
			total += c.getPosInLayer(); 
		}
		center = total/deg;
		return center;
	}
	
	
	/**
	 * Sort the nodes in a layer, by position
	 * @param layerNumber the layer to be sorted.
	 */
	private void sortLayerByPos(int layerNumber) {
		try {
			int n = getLayerSize(layerNumber);
		
			for (int i = 1; i < n; i++) {
				GraphLayoutNode node = getNode(layerNumber,i);
				for (int j = i-1; j >=0; j--) {
					GraphLayoutNode prev = getNode(layerNumber,j);
					
					if (prev.getPosInLayer() >= node.getPosInLayer()) {
						setNode(layerNumber,j+1,prev);
						setNode(layerNumber,j,node);
						orderChanged = true;
					}
				}
			}
		}
		catch(Exception e) {
		}
	}
	
	/**
	 * Assign positions to each node based on their position in the  sorted 
	 * 	ordering
	 * @param layerNumber the layer in question
	 */
	private void assignPosFromLayer(int layerNumber) {
		Iterator iter = layerIterator(layerNumber);
		double pos = 0.0;
		
		try {
			while (iter.hasNext()) {
				GraphLayoutNode node = (GraphLayoutNode) iter.next();
				node.setPosInLayer(pos);
				pos +=1.0;
			}
		} 
		catch(Exception e) {
		}
	}	
	
	/**
	 * Add a node to a layer in the layering. If the layer
	 * doesn't exist, add it.
	 * 
	 * @param layerNumber the layer to which the node will be added.
	 * @param node the node to add
	 */
	public void addToLayer(int layerNumber,GraphLayoutNode node) {
		if (layerNumber > layers.size()-1) { // if we haven't created this layer yet
			for (int i = layers.size(); i <= layerNumber; i++) {
				Vector  v = new Vector();
				layers.add(v);
			}
		}
		Vector v = (Vector) layers.elementAt(layerNumber);
		v.add(node);
	}
	
	/**
	 * 
	 * @return the number of layers
	 */
	public int getLayerCount() {
		return layers.size();
	}

	/** 
	 * 
	 * @param i a layer number
	 * @return layer number {@link i}, or null if that layer does not exist
	 */
	private Vector getLayer(int i) {
		if (i < layers.size()) {
			Vector v = (Vector) layers.elementAt(i);
			return v;
		}
		else
			return null;
	}

	/**
	 *
	 * @param i a layer number
	 * @return the iterator for that layer
	 */
	public Iterator layerIterator(int i) {
		if (getLayer(i) == null)
			return null;
		return getLayer(i).iterator();
	}

	/**
	 * 
	 * @param layerNumber a layer number
	 * @return the number of nodes in layer {@link layerNumber}
	 */
	public int getLayerSize(int layerNumber) {
		return getLayer(layerNumber).size();
	}

	/**
	 * 
	 * @param layerNumber a layer number 
	 * @param n a node index
	 * @return the {@link n}th node from layer {@link layerNumber}
	 */
	public GraphLayoutNode getNode(int layerNumber,int n) {
		Vector v = getLayer(layerNumber);
		GraphLayoutNode node = (GraphLayoutNode) v.elementAt(n);
		return node;
	}

	/**
	 * Set the node in  a layer
	 * @param layerNumber the  layer number
	 * @param n position in the layer
	 * @param node node to place in thhe given layer
	 */
	public void setNode(int layerNumber,int n,GraphLayoutNode node) {
		Vector v = getLayer(layerNumber);
		v.setElementAt(node,n);
	}
	
	private void flattenLayers() {
		Vector newLayers = new Vector();
		int count = getLayerCount();
		int curLayer = 0;
		for (int i= 0; i < count; i++) {
			Vector layer = getLayer(i);
			if (layer.size() > count/2) {
				splitLayer(curLayer,layer,newLayers);
				curLayer +=2;
			}
			else {
				newLayers.add(layer);
				// set layer of nodes
				Iterator iter2 = layer.iterator();
				while (iter2.hasNext()) {
					GraphLayoutNode node = (GraphLayoutNode) iter2.next();
					node.setLayer(curLayer);
				}
				curLayer++;
			}
		}	
		layers = newLayers;
	}
	
	// split the contents of layer into two new vectors and place both in dest
	private void splitLayer(int curLayer,Vector layer,Vector dest) {
		Vector l1  = new Vector();
		Vector l2  = new Vector();
		int next = curLayer+1;
		Vector nextLayer = l1;
		Iterator iter = layer.iterator();
		while (iter.hasNext()){
			GraphLayoutNode node = (GraphLayoutNode) iter.next();
			nextLayer.add(node);
			if (nextLayer == l1) {
				nextLayer = l2;
				node.setLayer(curLayer);
			}
			else { 
				nextLayer = l1;
				node.setLayer(next);
			}
		}
		dest.add(l1);
		dest.add(l2);
	}

}


	