/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager
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
 
package org.openmicroscopy.shoola.agents.chainbuilder;


//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutLinkData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;

/**
 * A utility class for managing communications with registry and 
 * retrieving data
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */

public class ChainDataManager extends DataManager {

		
	protected HashMap chainHash=null;
		
	protected HashMap chainExecutionHash = null;
	
	/** a list of analysis nodes. populated when we get chains */
	protected HashMap analysisNodes = null;
	
	/** flags to see if we're getting  chains & executions*/
	private boolean gettingChains = false;
	private boolean gettingExecutions =false;
	
	/** flags to see if getting chains */
	
	
	public ChainDataManager(Registry registry) {
		super(registry);
	}
		
	public Registry getRegistry() {return registry; }
	
	
	
	protected synchronized void retrieveModules() {
		if (moduleHash == null ||moduleHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				ModuleCategoryData mcProto = new ModuleCategoryData();
				ChainFormalInputData finProto = new ChainFormalInputData();
				ChainFormalOutputData foutProto = new ChainFormalOutputData();
				SemanticTypeData stProto = new SemanticTypeData();
				ChainModuleData cmProto = new ChainModuleData();
				Collection modules = 
					dms.retrieveModules(cmProto,mcProto,finProto,foutProto,
						stProto);
				moduleHash = buildModuleHash(modules);
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's modules.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
	}
	
	public synchronized Collection getChains() {
		
		// if we're done, go for it.
		if (chainHash != null && chainHash.size() > 0) {
			return chainHash.values();
		}
		
		if (gettingChains == false) {
			System.err.println("loading chains..");
			gettingChains = true;
			retrieveChains();
			gettingChains = false;
			notifyAll();
			return chainHash.values();
		}
		else {// in progress
			try{ 
				wait();
				return chainHash.values();
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	
	protected synchronized void retrieveChains() {
		if (chainHash == null ||chainHash.size() == 0) {
			try {
				System.err.println("trying to load chains in chain data manager.");
				LayoutChainData acProto = new LayoutChainData();
				LayoutLinkData alProto = new LayoutLinkData();
				LayoutNodeData anProto = new LayoutNodeData(); 
				ChainFormalInputData finProto = new ChainFormalInputData();
				ChainFormalOutputData foutProto = new ChainFormalOutputData();
				SemanticTypeData stProto = new SemanticTypeData();
				ChainModuleData cmProto = new ChainModuleData();
				DataManagementService dms = registry.getDataManagementService();
				Collection chains = 
					dms.retrieveChains(acProto,alProto,anProto,cmProto,
							finProto,foutProto,stProto);
				chainHash = buildChainHash(chains);
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's chains.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
	}
	
	protected HashMap buildChainHash(Collection chains) {
		HashMap map = new HashMap();
		Iterator iter = chains.iterator();
		while (iter.hasNext()) {
			LayoutChainData p = (LayoutChainData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public LayoutChainData getChain(int id) {
		Integer ID = new Integer(id);
		if (chainHash == null)
			getChains();
		return (LayoutChainData) chainHash.get(ID);
	}
	
	public synchronized Collection getChainExecutions() {
		
		// if we're done, go for it.
		if (chainExecutionHash != null && chainExecutionHash.size() > 0) {
			return chainExecutionHash.values();
		}
		
		if (gettingChains == false) {
			System.err.println("loading chains..");
			gettingExecutions = true;
			retrieveChainExecutions();
			gettingExecutions = false;
			notifyAll();
			return chainExecutionHash.values();
		}
		else {// in progress
			try{ 
				wait();
				return chainExecutionHash.values();
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	public synchronized void retrieveChainExecutions() {
		if (chainExecutionHash == null ||chainExecutionHash.size() == 0) {
			try {
				ChainExecutionData ceProto = new ChainExecutionData();
				BrowserDatasetData dsProto = new BrowserDatasetData();
				LayoutChainData acProto = new LayoutChainData();
				NodeExecutionData neProto = new NodeExecutionData();
				LayoutNodeData anProto = new LayoutNodeData(); 
				ChainModuleData mProto = new ChainModuleData();
				ModuleExecutionData meProto = new ModuleExecutionData();
				DataManagementService dms = registry.getDataManagementService();
				Collection chainExecutions = 
					dms.retrieveChainExecutions(ceProto,dsProto,
							acProto,neProto,anProto,mProto,meProto);
				chainExecutionHash = buildExecutionHash(chainExecutions);
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's chains.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
	}
	
	protected HashMap buildExecutionHash(Collection executions) {
		HashMap map = new HashMap();
		Iterator iter = executions.iterator();
		while (iter.hasNext()) {
			ChainExecutionData p = (ChainExecutionData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public ChainExecutionData getChainExecution(int id) {
		Integer ID = new Integer(id);
		if (chainExecutionHash == null)
			getChainExecutions();
		return (ChainExecutionData) chainExecutionHash.get(ID);
	}
	
	public void setAnalysisNodes(HashMap analysisNodes) {
		this.analysisNodes = analysisNodes;
	}
	
	
	
	public LayoutNodeData getAnalysisNode(int id) {
		if (analysisNodes == null)
			return null;
		return (LayoutNodeData) analysisNodes.get(new Integer(id));
	}
}
