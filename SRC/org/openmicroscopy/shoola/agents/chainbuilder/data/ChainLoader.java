/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ChainLoader
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

package org.openmicroscopy.shoola.agents.chainbuilder.data;

//Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ComponentContentLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ContentGroup;
import org.openmicroscopy.shoola.agents.zoombrowser.piccolo.ContentComponent;

import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;

/** 
 * A {@link ComponentContentLoader} subclass for loading chains.
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
public class ChainLoader extends ComponentContentLoader
{
	private List chains = null;
	
	/** a list storing the chain nodes that we find */
	private ArrayList analysisNodes = new ArrayList();
	
	public ChainLoader(final ChainDataManager dataManager,
			ContentComponent component,final ContentGroup group) {
		super(dataManager,component,group);
	}	
	
	/**
	 * Do the work
	 */
	public Object getContents() {
		ChainDataManager chainDataManager = (ChainDataManager) dataManager;
		if (chains == null)  {
			System.err.println("calling get chains..");
			chains = chainDataManager.getChains();
			
		}
		Iterator iter = chains.iterator();
		while (iter.hasNext()) {
			LayoutChainData chain = (LayoutChainData) iter.next();
			reconcileChain(chain);
			chain.layout();
		}
		// set the list of analysiNodes..
		chainDataManager.setAnalysisNodes(analysisNodes);
		return chains;
	}
	
	/**
	 * Since the modules that come from the chain mapper are not fully populated
	 * we need to replace these modules with the fully populated versions
	 * returned by retreiveModules().
	 *
	 */
	private void reconcileChain(LayoutChainData chain) {
		List nodes = chain.getNodes();
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			LayoutNodeData node = (LayoutNodeData) iter.next();
			reconcileNode(node);
			analysisNodes.add(node);
		}
	}
	
	private void reconcileNode(LayoutNodeData node) {
	
		ChainDataManager chainDataManager = (ChainDataManager) dataManager;
		int id = node.getModule().getID();
		ChainModuleData mod = chainDataManager.getModule(id);
		node.setModule(mod);
	}
	
	
	
	private void dumpChains() {
		LayoutChainData chain;
		if (chains == null)
			return;
	  	Iterator iter = chains.iterator();
	  	while (iter.hasNext()) {
	  		chain = (LayoutChainData) iter.next();
	  		dumpChain(chain);
	  	}
	}
	
	private static void dumpChain(LayoutChainData chain) {
		System.err.println("\n\nanalysis chain: "+chain.getID()+") "
			+chain.getName());
		System.err.println("Owner: "+chain.getOwner());
		System.err.println("Nodes: ");
		dumpNodes(chain);
		
		System.err.println("all links");
		dumpLinks(chain.getLinks());		
	}	
	
	private static void dumpNodes(LayoutChainData chain) {
		List nodes = chain.getNodes();
		Iterator iter = nodes.iterator();
		AnalysisNodeData analysisNode;
		while (iter.hasNext()) {
			analysisNode = (AnalysisNodeData) iter.next();
			dumpNode(analysisNode);	
		}
	}
	
	private static void dumpNode(AnalysisNodeData analysisNode) {
		System.err.println("Node.."+analysisNode.getID());
		System.err.println("... module "+analysisNode.getModule().getID()+
			") "+analysisNode.getModule().getName());
		
		List inputs = analysisNode.getInputLinks();
		System.err.println("Input links...");
		dumpLinks(inputs);
		
		List outputs = analysisNode.getOutputLinks();
		System.err.println("Output links...");
		dumpLinks(outputs);
	}
	
	private static void dumpLinks(List links) {
		if (links == null)
			return;
		Iterator iter = links.iterator();
		AnalysisLinkData link;
		while (iter.hasNext()) {
			link = (AnalysisLinkData) iter.next();
			dumpLink(link);
		}
	}
	
	private static void dumpLink(AnalysisLinkData link) {
		System.err.println("link... "+link.getID());
		System.err.println("from node ..."+link.getFromNode().getID()+
			" to node .."+link.getToNode().getID());
		System.err.println(".... from output"+link.getFromOutput().getID()+
			", st .."+link.getFromOutput().getSemanticType().getID());
		System.err.println(".... to input"+link.getToInput().getID()+
			", st .."+link.getToInput().getSemanticType().getID());
		
	}
}