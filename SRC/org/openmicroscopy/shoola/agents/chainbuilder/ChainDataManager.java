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
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutions;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutionsByNodeID;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainNodeExecutionData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutLinkData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.agents.events.LoadChainExecutionsEvent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
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

		
	protected Map chainHash=null;
	
	
	/** flags to see if we're getting  chains & executions*/
	private boolean gettingChains = false;
	private boolean gettingExecutions =false;
	
	private ChainExecutions chainExecutions; 
	
	/** hash map of modules */
	protected Map moduleHash = null;

	/** are we loading modules? */
	protected boolean loadingModules = false;
	
	/** cached hash of module categories */
	protected Map moduleCategoryHash = null;		

	/** are we loading modules categories? */
	protected boolean loadingModuleCategories = false;
	
	public ChainDataManager(Registry registry) {
		super(registry);
	}
		
	public Registry getRegistry() {return registry; }
	
	
	/* Retrieve the modules. As with other similar calls in this file, 
	 * this may be a bit reckless: a second request coming in while this one is in 
	 * progress will get null instead of waiting. However, second calls are not 
	 * frequent, and this approach seems to be faster in general.
	 */
	public Collection getModules() {
		
		// if we're done, go for it.
		
		if (moduleHash != null && moduleHash.size() > 0)
			return moduleHash.values();
		
		if (loadingModules == false) {
			long start = System.currentTimeMillis();
			loadingModules = true;
			retrieveModules();
		//	retrieveCategories();
			loadingModules = false;
			if (ChainBuilderAgent.DEBUG_TIMING) {
				long end = System.currentTimeMillis()-start;
				System.err.println("time spent in getModules is "+end);
			}
			return moduleHash.values();
		}
		return null;
	}
	
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
	
	protected Map buildModuleHash(Collection modules) {
		HashMap map = new HashMap();
		Iterator iter = modules.iterator();
		while (iter.hasNext()) {
			ModuleData p = (ModuleData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public ModuleData getModule(int id) {
		Integer ID = new Integer(id);
		if (moduleHash == null)
			getModules();
		return (ModuleData) moduleHash.get(ID);
	}
	
	public Collection getModuleCategories() {
		
		// if we're done, go for it.
		
		if (moduleCategoryHash != null && moduleCategoryHash.size() > 0)
			return moduleCategoryHash.values();
		
		if (loadingModuleCategories == false) {
			long start = System.currentTimeMillis();
			loadingModuleCategories = true;
			retrieveModuleCategories();
			loadingModuleCategories = false;
			if (ChainBuilderAgent.DEBUG_TIMING) {
				long end = System.currentTimeMillis()-start;
				System.err.println("time spent in getModuleCategories.."+end);
			}
			return moduleCategoryHash.values();
		}
		/* shouldn't get here */
		return null;
	}
	
	public synchronized void retrieveModuleCategories() {
		if (moduleCategoryHash== null ||moduleCategoryHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				Collection moduleCategories = 
					dms.retrieveModuleCategories();
				moduleCategoryHash = buildModuleCategoryHash(moduleCategories); 
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
	
	protected Map buildModuleCategoryHash(Collection moduleCategories) {
		HashMap map = new HashMap();
		Iterator iter = moduleCategories.iterator();
		while (iter.hasNext()) {
			ModuleCategoryData p = (ModuleCategoryData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public ModuleCategoryData getModuleCategory(int id) {
		Integer ID = new Integer(id);
		if (moduleCategoryHash == null)
			getModuleCategories();
		return (ModuleCategoryData) moduleHash.get(ID);
	}
	public Collection getChains() {
		
		Collection res = null;
		long start;
		// if we're done, go for it.
		if (chainHash != null && chainHash.size() > 0) {
			res = chainHash.values();
		}
		else {
			if (gettingChains == false) {
				if (ChainBuilderAgent.DEBUG_TIMING) {
					start = System.currentTimeMillis();
				}
				gettingChains = true;
				retrieveChains();
				gettingChains = false;
				if (ChainBuilderAgent.DEBUG_TIMING) {
					long end = System.currentTimeMillis()-start;
					System.err.println("time for retrieving chains.. "+end);
				}
				res = chainHash.values();
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
	
	protected synchronized LayoutChainData retrieveChain(int id) {
		try {
			LayoutChainData acProto = new LayoutChainData();
			LayoutLinkData alProto = new LayoutLinkData();
			LayoutNodeData anProto = new LayoutNodeData(); 
			ChainFormalInputData finProto = new ChainFormalInputData();
			ChainFormalOutputData foutProto = new ChainFormalOutputData();
			SemanticTypeData stProto = new SemanticTypeData();
			ChainModuleData cmProto = new ChainModuleData();
			DataManagementService dms = registry.getDataManagementService();
			LayoutChainData chain =  (LayoutChainData)
				dms.retrieveChain(id,acProto,alProto,anProto,cmProto,
						finProto,foutProto,stProto);
			return chain;
		} catch(DSAccessException dsae) {
			String s = "Can't retrieve user's chains.";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure",
													s, dsae);
			return null;
		} catch(DSOutOfServiceException dsose) {
			ServiceActivationRequest 
			request = new ServiceActivationRequest(
								ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
			return null;
		}
	}
	
	protected Map buildChainHash(Collection chains) {
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
		LayoutChainData res = (LayoutChainData) chainHash.get(ID);
		return res;
	}
	
	public void addChain(LayoutChainData chain) {
		if (chain != null) {
			Integer ID = new Integer(chain.getID());
			chainHash.put(ID,chain);
		}
	}
	
	public LayoutChainData loadChain(int id) {
		LayoutChainData chain = retrieveChain(id);
		
		// reconcile the chain
		Collection nodes = chain.getNodes();
		Iterator iter = nodes.iterator();
		while (iter.hasNext()) {
			LayoutNodeData node = (LayoutNodeData) iter.next();
			int nodeId = node.getModule().getID();
			ChainModuleData mod = (ChainModuleData) getModule(nodeId);
			node.setModule(mod);	
		}
		addChain(chain);
		return chain;
	}
	
	public Collection getChainExecutions() {
		
		long start;
		// if we're done, go for it.
		if (chainExecutions != null) {
			return chainExecutions.getExecutions();
		}
		
		if (gettingExecutions == false) {
			if (ChainBuilderAgent.DEBUG_TIMING) {
				start = System.currentTimeMillis();
			}
			gettingExecutions = true;
			retrieveChainExecutions();
			gettingExecutions = false;
		//	notifyAll();
			if (ChainBuilderAgent.DEBUG_TIMING) {
				long getTime = System.currentTimeMillis()-start;
				System.err.println("time for executions is "+getTime);
			}
			return chainExecutions.getExecutions();
		}
		/*else {// in progress
			try{ 
				wait();
				return chainExecutions.getExecutions();
			}
			catch (InterruptedException e) {
				return null;
			}
		}*/
		return null;
	}
	
	public synchronized void retrieveChainExecutions() {
		if (chainExecutions== null) {
			try {
				ChainExecutionData ceProto = new ChainExecutionData();
				BrowserDatasetData dsProto = new BrowserDatasetData();
				LayoutChainData acProto = new LayoutChainData();
				ChainNodeExecutionData neProto = new ChainNodeExecutionData();
				LayoutNodeData anProto = new LayoutNodeData(); 
				ChainModuleData mProto = new ChainModuleData();
				ModuleExecutionData meProto = new ModuleExecutionData();
				DataManagementService dms = registry.getDataManagementService();
				Collection execs = 
					dms.retrieveChainExecutions(ceProto,dsProto,
							acProto,neProto,anProto,mProto,meProto);
				chainExecutions = new ChainExecutions(execs);
	
				
				LoadChainExecutionsEvent event = new
					LoadChainExecutionsEvent(chainExecutions);
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
	
	public boolean chainHasExecutionsForDataset(int chainID,int datasetID) {
		if (chainExecutions!= null)
			return chainExecutions.chainHasExecutionsForDataset(chainID,datasetID);
		return false;
	}
	
	// to be revised
	public ChainExecutionsByNodeID getChainExecutionsByChainID(int id) {
		if (chainExecutions != null)
			return chainExecutions.getChainExecutionsByChainID(id);
		return null;
	}
	
	public int getMaxNodeExecutionCount() {
		if (chainExecutions == null)
			return 0;
		return chainExecutions.getMaxNodeExecutionCount();
	}
	
	public Collection getNexesForMex(int id) {
		if (chainExecutions == null)
			return null;
		return chainExecutions.getNexesForMex(id);
	}

	public Collection getNexesForModule(int id) {
		if (chainExecutions == null)
			return null;
		return chainExecutions.getNexesForModule(id);
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
			if (name.compareToIgnoreCase(chain.getName()) == 0)
				return true;
		}
		return false;
	}
}
