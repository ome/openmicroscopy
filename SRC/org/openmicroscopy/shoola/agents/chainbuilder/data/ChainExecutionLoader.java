/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutionLoader
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
import java.util.List;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ContentLoader;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ContentGroup;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;

/** 
 * A {@link ContentLoader} subclass for loading chain executions.
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
public class ChainExecutionLoader extends ContentLoader
{
	private List chainExecutions = null;
	
	public ChainExecutionLoader(final ChainDataManager dataManager,
			final ContentGroup group) {
		super(dataManager,group);
		start();
	}	
	
	/**
	 * Do the work
	 */
	public Object getContents() {
		if (chainExecutions == null)  {
			System.err.println("calling get chains..");
			chainExecutions = ((ChainDataManager) dataManager).getChainExecutions();
			
		}
		// reconcile
		reconcileExecutions();
		dumpExecutions();
		return chainExecutions;
	}
	
	private void reconcileExecutions() {
		ChainExecutionData chainExecution;
		BrowserDatasetSummary dataset;
		LayoutChainData  chain;
		int id;
		Iterator iter = chainExecutions.iterator();
		ChainDataManager chainDataManager = (ChainDataManager) dataManager;
		
		while (iter.hasNext()) {
			chainExecution = (ChainExecutionData) iter.next();
			chain = (LayoutChainData) chainExecution.getChain();
			id = chain.getID();
			chainExecution.setChain(chainDataManager.getChain(id));
			dataset = (BrowserDatasetSummary) chainExecution.getDataset();
			id = dataset.getID();
			chainExecution.setDataset(chainDataManager.getDataset(id));
			reconcileNodeExecutions(chainExecution);
		}
	}
	
	public void reconcileNodeExecutions(ChainExecutionData chainExecution) {
		List nodeExecs = chainExecution.getNodeExecutions();
		if (nodeExecs == null || nodeExecs.size() == 0)
			return;
		NodeExecutionData ne;
		Iterator iter = nodeExecs.iterator();
		while (iter.hasNext()) {
			ne = (NodeExecutionData) iter.next();
			reconcileNodeExecution(ne);
		}		
	}
	
	public void reconcileNodeExecution(NodeExecutionData ne) {
		LayoutNodeData n = (LayoutNodeData) ne.getAnalysisNode();
		ChainDataManager chainDataManager = (ChainDataManager) dataManager;
		int id = n.getID();
		ne.setAnalysisNode(chainDataManager.getAnalysisNode(id));
	}
	
	private void dumpExecutions() {
		Iterator iter = chainExecutions.iterator();
		ChainExecutionData exec;
		while (iter.hasNext()) {
			exec = (ChainExecutionData) iter.next();
			if (exec.getID() == 1)
				dumpExecution(exec);
		}
	}
		
	private void dumpExecution(ChainExecutionData exec) {
		System.err.println("\n\nChain excution: "+exec.getID());
		LayoutChainData chain = (LayoutChainData) exec.getChain();
		System.err.println(" .. chain "+chain.getID()+", "+chain.getName());
		
		BrowserDatasetSummary ds = (BrowserDatasetSummary) exec.getDataset();
		System.err.println(".. dataset "+ds.getID()+", "+ds.getName());
		System.err.println(".. time: "+exec.getTimestamp());
		dumpNodeExecutions(exec);
	}
	
	private void dumpNodeExecutions(ChainExecutionData exec) {
		List nodeExecs = exec.getNodeExecutions();
		if (nodeExecs == null || nodeExecs.size() == 0)
			return;
		NodeExecutionData ne;
		System.err.println("Nod executions...");
		Iterator iter = nodeExecs.iterator();
		while (iter.hasNext()) {
			ne = (NodeExecutionData) iter.next();
			dumpNodeExecution(ne);
		}
	}
	
	private void dumpNodeExecution(NodeExecutionData ne) {
		System.err.println("node execution..."+ne.getID());
		LayoutNodeData node = (LayoutNodeData) ne.getAnalysisNode();
		System.err.println("Node is "+ node.getID());
		ChainModuleData module = (ChainModuleData) node.getModule();
		System.err.println(" .. module .."+module.getID()+", "+module.getName());
		ModuleExecutionData mex = ne.getModuleExecution();
		System.err.println(" .. mex id is "+mex.getID());
		System.err.println(" .. mex status is "+mex.getStatus());
		System.err.println("... mex time is "+mex.getTimestamp());
	}
}