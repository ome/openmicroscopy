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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
 
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
import org.openmicroscopy.shoola.agents.events.ChainExecutionsLoadedEvent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
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

		
	protected LinkedHashMap chainHash=null;
	
	/** hash executions by id */
	protected LinkedHashMap chainExecutionHashesByID = null;
	
	/** hash executions by dataset id */
	protected LinkedHashMap executionsByDatasetID = null;
	
	/** hash executions by chain id */
	protected LinkedHashMap executionsByChainID = null;
	
	/** a list of analysis nodes. populated when we get chains */
	protected LinkedHashMap analysisNodes = null;
	
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
		
		Collection res = null;
		// if we're done, go for it.
		if (chainHash != null && chainHash.size() > 0) {
			res = chainHash.values();
		}
		else {
			if (gettingChains == false) {
				gettingChains = true;
				retrieveChains();
				gettingChains = false;
				notifyAll();
				res = chainHash.values();
			}
			else {// in progress
				try{ 
					wait();
					res = chainHash.values();
				}
				catch (InterruptedException e) {
					res = null;
				}
			}
		}
		return res;
	}
	
	protected synchronized void retrieveChains() {
		if (chainHash == null ||chainHash.size() == 0) {
			try {
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
	
	protected LinkedHashMap buildChainHash(Collection chains) {
		LinkedHashMap map = new LinkedHashMap();
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
	
	private void addChain(LayoutChainData chain) {
		Integer ID = new Integer(chain.getID());
		chainHash.put(ID,chain);
	}
	
	public synchronized Collection getChainExecutions() {
		
		// if we're done, go for it.
		if (chainExecutionHashesByID != null && chainExecutionHashesByID.size() > 0) {
			return chainExecutionHashesByID.values();
		}
		
		if (gettingChains == false) {
			gettingExecutions = true;
			retrieveChainExecutions();
			gettingExecutions = false;
			notifyAll();
			return chainExecutionHashesByID.values();
		}
		else {// in progress
			try{ 
				wait();
				return chainExecutionHashesByID.values();
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	public synchronized void retrieveChainExecutions() {
		if (chainExecutionHashesByID == null ||chainExecutionHashesByID.size() == 0) {
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
				buildExecutionHashes(chainExecutions);
				ChainExecutionsLoadedEvent event = new
					ChainExecutionsLoadedEvent(executionsByDatasetID,
							executionsByChainID,chainExecutionHashesByID);
				registry.getEventBus().post(event);
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
	
	protected void buildExecutionHashes(Collection executions) {
		chainExecutionHashesByID = new LinkedHashMap();
		executionsByDatasetID = new LinkedHashMap();
		executionsByChainID = new LinkedHashMap();
		Iterator iter = executions.iterator();
		while (iter.hasNext()) {
			
			// hash by execution id
			ChainExecutionData p = (ChainExecutionData) iter.next();
			Integer id = new Integer(p.getID());
			chainExecutionHashesByID.put(id,p);
			
			// by dataset id
			
			DatasetData d = p.getDataset();
			id = new Integer(d.getID());
			updateExecutionHash(executionsByDatasetID,id,p);
			
			LayoutChainData c = (LayoutChainData) p.getChain();
			id = new Integer(c.getID());
			updateExecutionHash(executionsByChainID,id,p);
		}
	}
	
	private void updateExecutionHash(LinkedHashMap map, Integer id,ChainExecutionData p) {
		ArrayList execs = null;
		Object obj = map.get(id);
		if (obj == null)
			execs = new ArrayList();
		else
			execs = (ArrayList) obj;
		execs.add(p);
		map.put(id,execs);
	}
	
	public ChainExecutionData getChainExecutionByID(int id) {
		Integer ID = new Integer(id);
		if (chainExecutionHashesByID == null)
			getChainExecutions();
		return (ChainExecutionData) chainExecutionHashesByID.get(ID);
	}
	
	public Collection getChainExecutionsByDatasetID(int id) {
		Integer ID = new Integer(id);
		if (executionsByDatasetID == null)
			getChainExecutions();
		return (Collection) executionsByDatasetID.get(ID);
	}
	
	public Collection getChainExecutionsByChainID(int id) {
		Integer ID = new Integer(id);
		if (executionsByChainID == null)
			getChainExecutions();
		return (Collection) executionsByChainID.get(ID);
	}
	
	public void setAnalysisNodes(LinkedHashMap analysisNodes) {
		this.analysisNodes = analysisNodes;
	}
	
	
	
	public LayoutNodeData getAnalysisNode(int id) {
		if (analysisNodes == null)
			return null;
		return (LayoutNodeData) analysisNodes.get(new Integer(id));
	}
	
	public void saveChain(LayoutChainData chain) {
		if (chain != null) {
			try {
				DataManagementService dms = registry.getDataManagementService();
				dms.createAnalysisChain(chain);
			} catch(DSAccessException dsae) {
				String s = "Can't save new chain.";
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
	
	public boolean hasChainWithName(String name) {
		Collection chains = getChains();
		Iterator iter = chains.iterator();
		LayoutChainData chain;
		while (iter.hasNext()) {
			chain = (LayoutChainData) iter.next();
			if (name.compareTo(chain.getName()) == 0)
				return true;
		}
		return false;
	}
}
