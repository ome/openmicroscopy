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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
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
		
	
	/** Information about the layering of nodes */
	private Layering layering;

	/** set of structure errors */
	private ChainStructureErrors errors = null;
	public LayoutChainData() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new LayoutChainData(); }
	

	private boolean hasCycles = false;
	
	/** lists of all of unbound inputs and outputs */
	private Collection unboundInputs = new Vector();
	private Collection unboundOutputs = new Vector();

	/** hash maps of input links and output links */
	private Map inputLinkMap = null;
	private Map outputLinkMap = null;
	
	public void layout() {
		validateChainStructure();
		getUnbounded();
		initNodes();
		layering = new Layering(getNodes());	
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
	
	private void addLinkToHash(Map inputs,LayoutLinkData link,
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
	
	private boolean hasInLinks(LayoutNodeData node,Collection links) {
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
	
	private Vector getOutLinks(LayoutNodeData node,Collection links) {
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
	private void getUnbounded(LayoutNodeData node,List params,Map linkMap,
			Collection unbounded) {
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
	
	/*private void dumpUnbounded(Vector unbounded) {
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
	}*/
	
	public List getUnboundInputs() {
		return getParamList(unboundInputs); 
	}
	
	public List getUnboundOutputs() {
		return getParamList(unboundOutputs);
	}
	
	private List getParamList(Collection unbounds) {
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
	 * 
	 * @return the object containing the layering for the Chain
	 */
	public Layering getLayering() {
		return layering;
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
