/*
 * org.openmicroscopy.shoola.agents.chainbuidler.data.LayoutChainData
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainStructureError;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainStructureErrors;
import org.openmicroscopy.shoola.agents.chainbuilder.data.CircularChainError;
import org.openmicroscopy.shoola.agents.chainbuilder.data.MultiplyBoundInputError;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;


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
public class LayoutChainData  extends AnalysisChainData
	implements Comparable
{
		
	/** Static parameters for layout */
	private static final int CROSSING_ITERATIONS=4;
	
	/** has the order of children changed? */
	private boolean orderChanged = false;	
	
	/** Information about the layering of nodes */
	private Layering layering = new Layering();

	/** set of structure errors */
	private ChainStructureErrors errors = null;
	public LayoutChainData() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new LayoutChainData(); }
	

	private boolean hasCycles = false;
	
	/** lists of all of unbound inputs and outputs */
	private Vector unboundInputs = new Vector();
	private Vector unboundOutputs = new Vector();

	/** hash maps of input links and output links */
	private HashMap inputLinkMap = null;
	private HashMap outputLinkMap = null;
	public void layout() {
		validateChainStructure();
		getUnbounded();
		initNodes();
		layerNodes();
//		layering.flattenLayers();
		makeProper();
		reduceCrossings();
	
		cleanupNodes();
		//dumpChain();
		// clear out link maps so they can be garbabe collected
		inputLinkMap = outputLinkMap = null;
		
	}
	
	
	private void validateChainStructure() {
		// validate links
		validateLinks();
		// validate cycles 
		validateCycles();
	}
	
	/**
	 * Verify that no input parameter has multiple links coming in - 
	 * only one link/input is allowed.
	 * 
	 * Since multiple instances of a module are possible, we must
	 * conduct this check based on both the id of the formal input
	 * and the id of the node.
	 * 
	 *
	 */
	private void validateLinks() {
		
		if (inputLinkMap == null)
			getLinkMaps();
		
		if (inputLinkMap.size() == 0)
			return;
		
		// then, iterate over keys by input.
		// grabbing each key that has more than two links.
		Iterator iter = inputLinkMap.keySet().iterator();
		while (iter.hasNext()) {
			Integer inpID = (Integer) iter.next();
			HashMap  nodeMap = (HashMap) inputLinkMap.get(inpID);
			// if there's something for this input, look at all nodes.
			if (nodeMap != null) {
				Iterator iter2 = nodeMap.keySet().iterator();
				while (iter2.hasNext()) {
					Integer nodeID = (Integer)iter2.next();
					
					Vector inLinks = (Vector) nodeMap.get(nodeID);
					
					
					if (inLinks != null && inLinks.size() > 1) {
						// get a link
						int linkCount = inLinks.size();
						LayoutLinkData link = (LayoutLinkData) inLinks.elementAt(0);
						MultiplyBoundInputError error = 
							new MultiplyBoundInputError(link.getToInput(),
									link.getToNode(),linkCount);
						addStructureError(error);
					}
				}
			}
		}
	}
	
	/**
	 * To track the links going to each input do a hash of hashes.
	 * At the first level, index by input id. At the second level,
	 * index by nodeID.
	 * 
	 * @return
	 */
	private void getLinkMaps() {
		List links = getLinks();
		inputLinkMap = new HashMap();
		outputLinkMap = new HashMap();
		//		 build map of links by endpoint
		Iterator iter = links.iterator();
		LayoutLinkData link;
		
		while (iter.hasNext()) {
			link = (LayoutLinkData) iter.next();
			addLinkToHash(inputLinkMap,link,link.getToInput(),
					(LayoutNodeData) link.getToNode());
			addLinkToHash(outputLinkMap,link,link.getFromOutput(),
					(LayoutNodeData) link.getFromNode());
			
		}
	}
	
	private void addLinkToHash(HashMap inputs,LayoutLinkData link,
			FormalParameterData inp,LayoutNodeData node) {
	HashMap nodeMap;
		Integer nodeId;
		Vector inLinks;

		// get the map  of nodes with links to this input.
		Integer inpID = new Integer(inp.getID());
		Object obj = inputs.get(inpID);
		
		if (obj == null)
			nodeMap  = new HashMap();
		else
			nodeMap = (HashMap) obj;
		nodeId = new Integer(node.getID());
		// Get the list of links for this input & node.
		obj = nodeMap.get(nodeId);
		if (obj == null)
			inLinks = new Vector();
		else
			inLinks = (Vector) obj;
		inLinks.add(link);
		nodeMap.put(nodeId,inLinks);
		inputs.put(inpID,nodeMap);
		
	}
	
	/**
	 * Check to see if the graph has any cycles
	 * Do this by finding a node with no inlinks, and then removing it  and
	 * all of its outlinks.
	 * 
	 * Any remaining nodes are in a cycle (or multiple cycles)
	 */ 
	private void validateCycles() {
		// set hasCycles
		Vector nodes = new Vector(getNodes());
		Vector links = new Vector(getLinks());
	
		boolean foundOne = false;
		ListIterator iter;
		hasCycles = false;
		while (nodes.size() > 0 && hasCycles == false) {
			foundOne = false;
			iter = nodes.listIterator();
			while (iter.hasNext()) {
				LayoutNodeData node = (LayoutNodeData) iter.next();
				boolean inLinks = hasInLinks(node,links);
				if (inLinks == false) {
					iter.remove();	
					Vector outLinks = getOutLinks(node,links);
					if (outLinks != null) {
						links.removeAll(outLinks);
					}
				    foundOne = true;
				    break;
				}
			}
			
			// made it through list
			if (foundOne == false) {
				hasCycles = true;
				addStructureError(new CircularChainError(nodes));
				// add an error
				return;
			}
		}
		hasCycles = false;
	}
	
	private boolean hasInLinks(LayoutNodeData node,Vector links) {
		Iterator iter = links.iterator();
		LayoutLinkData link;
		while (iter.hasNext()) {
			link = (LayoutLinkData) iter.next();
			if (link.getToNode().getID() == node.getID()) {
				return true;
			}
		}
		return false;
	}
	
	private Vector getOutLinks(LayoutNodeData node,Vector links) {
		Iterator iter = links.iterator();
		LayoutLinkData link;
		Vector res = null;
		while (iter.hasNext()) {
			link = (LayoutLinkData) iter.next();
			if (link.getFromNode().getID() == node.getID()) {
				if (res == null)
					res = new Vector();
				res.add(link);
			}
		}
		return res;	
	}
	
	public boolean hasCycles() {
		return hasCycles;
	}
	
	private void addStructureError(ChainStructureError error) {
		if (errors == null) 
			errors = new ChainStructureErrors(this);
		errors.addError(error);
	}
	
	public ChainStructureErrors getStructureErrors() {
		if (errors == null)
			validateChainStructure();
		// clear out link maps so they can be garbage-collected.
		inputLinkMap = null;
		outputLinkMap = null;
		return errors;
	}
	
	/**
	 * Find the unbound inputs and outputs,
	 * as needed to draw the whole chain as a single module
	 *
	 */
	private void getUnbounded() {
		List nodes = getNodes();
		Iterator iter = nodes.iterator();
		LayoutNodeData node;
		while (iter.hasNext()) {
			node = (LayoutNodeData) iter.next();
			getUnbounded(node);
		}
	}
	
	/**
	 * Get the unbound inputs and ouputs for a single node,
	 * and add them to list.
	 *
	 */
	private void getUnbounded(LayoutNodeData node) {
		// go over formal ins and outs.
		List inputs = node.getChainModule().getFormalInputs();
		if (inputs != null)
			getUnbounded(node,inputs,inputLinkMap,unboundInputs);
		List outputs = node.getChainModule().getFormalOutputs();		
		// all outputs of all modules are potential outputs of the chain
		// even if they have outgoing links, they can be considered outputs,
		// because multiple outlinks are allowed.
		if (outputs != null)
			getUnboundedOutputs(node,outputs);
	}
	
	/**
	 * do the work of finding unbounded parameters given a node, 
	 * a list of parameters, a mapping of links, and the list 
	 * that we're building
	 *
	 */
	private void getUnbounded(LayoutNodeData node,List params,HashMap linkMap,
			Vector unbounded) {
		Iterator iter = params.iterator();
		FormalParameterData param;
		while (iter.hasNext()) {
			param = (FormalParameterData) iter.next();
			Object obj = linkMap.get(new Integer(param.getID()));
			if (obj == null) {// no entries for this param 
				unbounded.add(new UnboundedParameter(param,node));
			}
			else {
				HashMap nodeMap = (HashMap) obj;
				obj = nodeMap.get(new Integer(node.getID()));
				if (obj == null) {
					unbounded.add(new UnboundedParameter(param,node));
				}

			}
		}
	}
	
	private void getUnboundedOutputs(LayoutNodeData node,List outputs) {
		Iterator iter = outputs.iterator();
		FormalParameterData param;
		while (iter.hasNext()) {
			param = (FormalParameterData) iter.next();
			unboundOutputs.add(new UnboundedParameter(param,node));
		}
	}
	
	private void dumpUnbounded(Vector unbounded) {
		if (unbounded.size() > 5 ) {
			System.err.println("..."+unbounded.size()+" unbounded");
			return;
		}
		UnboundedParameter p;
		Iterator iter = unbounded.iterator();
		while (iter.hasNext()){
			p = (UnboundedParameter) iter.next();
			System.err.println("..param.."+p.getParam().getName()+", node.."+p.getNode().getID());
		}
	}
	
	public Vector getUnboundInputs() {
		return getParamList(unboundInputs); 
	}
	
	public Vector getUnboundOutputs() {
		return getParamList(unboundOutputs);
	}
	
	private Vector getParamList(Vector unbounds) {
		Iterator iter = unbounds.iterator();
		Vector res = new Vector();
		while (iter.hasNext()){
			UnboundedParameter p = (UnboundedParameter) iter.next();
			res.add(p.getParam());
		}
		return res;
	}
	
	/**
	 * Initalize each of the {@link LayoutNodeData}s in the chain. This is an ugly 
	 * thing to have to do, but we have to - can't call
	 * buildLinkLists in the constructor for each node...
	 * 
	 */
	private void initNodes() {
		List nodes = getNodes();
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			LayoutNodeData node = (LayoutNodeData) iter.next();
			node.buildLinkLists();
		}
	}
	
	
	
	
	/**
	 * Iterate over the nodes, cleaning up auxiliary structures that are needed
	 * for layout
	 *
	 */
	private void cleanupNodes() {
			List nodes = getNodes();
			Iterator iter = nodes.iterator();
			while (iter.hasNext()) {
				LayoutNodeData node = (LayoutNodeData) iter.next();
				node.cleanupLinkLists();
			}
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
	private void layerNodes() {
		List nodes = getNodes();
		int numAssigned = 0, currentLayer = 0;
		boolean ok= true;
		int nodeCount = nodes.size();

		LayoutNodeData node  = null;

		LayoutNodeData succ;
		
		do {
			Iterator iter = nodes.iterator();
			while (iter.hasNext()) {
				ok = true;
				node = (LayoutNodeData) iter.next();
					
				if (node.hasLayer())
					continue;
				
				//System.err.println("finding successors for "+
				//		node.getModule().getName());	
				//System.err.println("...."+node);
				Collection succs = node.getSuccessors();	
						
					
				Iterator succIter = succs.iterator();	
				while (succIter.hasNext()) {
					succ = (LayoutNodeData) succIter.next();

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
					layering.addToLayer(currentLayer,node);
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
		int count = layering.getLayerCount();
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
		//System.err.println("working on layer "+i);
		try {
			Iterator iter = layering.layerIterator(i);
			if (iter != null) {
				while (iter.hasNext()) {
					node = (GraphLayoutNode) iter.next();
					makeProperNode(node,i);		
				}
			}
		}
		catch (Exception e) { 
			//System.err.println("exception in makeProperLayer..");
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
		
		//System.err.println("making node proper: "+node.getName());
		//System.err.println("doing links..."); 
		while (iter.hasNext()) {
			//System.err.println("LINK: ");
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
		HashSet newLinks) {
		// we know node is at i.
		
		GraphLayoutNode to = link.getToNode();
		//System.err.println("..link to "+to.getName());
		int toLayer = to.getLayer();
		if (toLayer == (i-1)) {
			// layer is correct
			newLinks.add(link);
		}
		else {
			// create new dummy node
			DummyNode dummy = new DummyNode();
			LayoutLinkData semanticLayoutLinkData = link.getSemanticLink();
			//System.err.println("link is ..."+semanticLayoutLinkData.getID());
			// make this node point to "to"
			LayoutLink dummyOutLink = new LayoutLink(semanticLayoutLinkData,dummy,to);
			dummy.addSuccLink(dummyOutLink);
			
			// make node point to new node
			LayoutLink newOutLink = new LayoutLink(semanticLayoutLinkData,node,dummy);
			
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
			layering.addToLayer(i-1,dummy);
			
			// adjust the semantic link to put dummy in between "from" and "to".
			// invariant is that "from" is directly before "to", so just put it
			//after "from"
			semanticLayoutLinkData.addIntermediate(node,dummy);
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
			
			
			for (count = 1; count < layering.getLayerCount(); count++) {
				crossingReduction(count,false);
			}
			
			//for (int curLayer = layers.size()-2; curLayer >=0; curLayer--) {
			for (int curLayer = layering.getLayerCount()-2; 
				curLayer>=0;curLayer--) {
				//Vector layer  = (Vector) layers.elementAt(curLayer);
				//crossingReduction(layer,false); 11/10/03 hsh
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
			Iterator iter = layering.layerIterator(layerNumber);
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
			int n = layering.getLayerSize(layerNumber);
		
			for (int i = 1; i < n; i++) {
				GraphLayoutNode node = layering.getNode(layerNumber,i);
				for (int j = i-1; j >=0; j--) {
					GraphLayoutNode prev = layering.getNode(layerNumber,j);
					
					if (prev.getPosInLayer() >= node.getPosInLayer()) {
						layering.setNode(layerNumber,j+1,prev);
						layering.setNode(layerNumber,j,node);
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
		Iterator iter = layering.layerIterator(layerNumber);
		double pos = 0.0;
		
		try {
			//System.err.println("assigning position from layer "+layerNumber);
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
	 * 
	 * @return the object containing the layering for the Chain
	 */
	public Layering getLayering() {
		return layering;
	}
	
	
	private void dumpChain() {
		System.err.println("dumping layers for chain ..."+getName());
	 	dumpLayers();
		System.err.println("...dumping nodes..");
		dumpNodes();
		System.err.println("...dumping Links...");
		dumpLinks();
	}
	
	/**
	 * Debug code to print the layers.
	 *
	 */
	private void dumpLayers() {
		System.err.println("Chain is "+getName());
		int count = layering.getLayerCount();
		for (int i =0; i < count; i++) {
			dumpLayer(i);
		}
	}
	
	private void dumpLayer(int i) {
		System.err.println("Layer "+i);
		Iterator iter = layering.layerIterator(i);
		while (iter.hasNext()) {
			GraphLayoutNode node = (GraphLayoutNode) iter.next();
			if (node instanceof DummyNode)
				System.err.println("....Node:  dummy");
			else {
				LayoutNodeData n = (LayoutNodeData) node;
				ChainModuleData mod = (ChainModuleData) n.getModule();
				if (mod != null)
					System.err.println("....Node: "+mod.getName());
				else
					System.err.println("non dummy w/out a module");
			}
			System.err.println("... position in layer is "+
				node.getPosInLayer());
		} 
	}
	
	private void dumpNodes() {
		Collection nodes = getNodes();
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			GraphLayoutNode node = (GraphLayoutNode) iter.next();
			dumpNode(node);
	
		}
	}
	
	private void dumpLinks() {
		Collection links  = getLinks();
		Iterator iter  = links.iterator();
		while (iter.hasNext()) {
			LayoutLinkData link = (LayoutLinkData) iter.next();
			System.err.println("link.."+link);
			LayoutNodeData from = (LayoutNodeData) link.getFromNode();
			System.err.println(" from node..."+from.getModule().getName());
			LayoutNodeData to = (LayoutNodeData) link.getToNode();
			System.err.println(" to node..."+to.getModule().getName());
			
			Iterator iter2 = link.getNodeIterator();
			while (iter2.hasNext()) {
				GraphLayoutNode node = (GraphLayoutNode) iter2.next();
				dumpNode(node);
			}
		}
	}
	
	private void dumpNode(GraphLayoutNode node) {
		if (node instanceof LayoutNodeData) {
			ChainModuleData mod = (ChainModuleData) ((LayoutNodeData) node).
				getModule();
			System.err.println("...node for ..."+mod.getName());
		}
		else
			System.err.println("dummy node...");	
	}
	
	public int compareTo(Object o) {
		if (o instanceof LayoutChainData) {
			int id = ((LayoutChainData) o).getID();
			return getID()-id;
		}
		else
			return 1;
	}
	/**
	 * An auxiliary class to hold layering information
 	 */
	public class Layering {
		/**
		 *  the vector that holds layers. Each layer will be a vector
		 */
		private Vector layers = new Vector();
	
		
		// and a vector for their x positions
		
		
		Layering() {
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
		
		public void flattenLayers() {
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
	
	/**
	 * 
	 * An unbounded paramter must be a combination of a formal parameter and a node
	 */
	public class UnboundedParameter {
		
		private FormalParameterData param;
		private LayoutNodeData node;
		
		public UnboundedParameter(FormalParameterData param,LayoutNodeData node) {
			this.param = param;
			this.node = node;
		}
		
		public FormalParameterData getParam() {
			return param;
		}
		
		public LayoutNodeData getNode() {
			return node;
		}
		
	}
	
}
