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
import java.util.Iterator;
import java.util.List;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalInputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainFormalOutputData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutLinkData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
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

		
	protected List chains;
	
	protected List chainExecutions;
	
	/** a list of analysis nodes. populated when we get chains */
	protected List analysisNodes = null;
	
	public ChainDataManager(Registry registry) {
		super(registry);
	}
		
	public Registry getRegistry() {return registry; }
	
	private boolean gettingModules = false;
	
	public synchronized List getModules() {
		
		// if we're done, go for it.
		
		if (modules != null && modules.size() > 0)
			return modules;
		
		if (gettingModules == false) {
			gettingModules = true;
			retrieveModules();
			gettingModules = false;
			notifyAll();
			return modules;
		}
		else {// in progress
			try{ 
				wait();
				return modules;
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	
	
	private synchronized List retrieveModules() {
		if (modules == null ||modules.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				ModuleCategoryData mcProto = new ModuleCategoryData();
				ChainFormalInputData finProto = new ChainFormalInputData();
				ChainFormalOutputData foutProto = new ChainFormalOutputData();
				SemanticTypeData stProto = new SemanticTypeData();
				ChainModuleData cmProto = new ChainModuleData();
				modules = 
					dms.retrieveModules(cmProto,mcProto,finProto,foutProto,
						stProto);
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
		return modules;
	}
	
	public List getChains() {
		if (chains == null ||chains.size() == 0) {
			try {
				LayoutChainData acProto = new LayoutChainData();
				LayoutLinkData alProto = new LayoutLinkData();
				LayoutNodeData anProto = new LayoutNodeData(); 
				ChainFormalInputData finProto = new ChainFormalInputData();
				ChainFormalOutputData foutProto = new ChainFormalOutputData();
				SemanticTypeData stProto = new SemanticTypeData();
				ChainModuleData cmProto = new ChainModuleData();
				DataManagementService dms = registry.getDataManagementService();
				chains = 
					dms.retrieveChains(acProto,alProto,anProto,cmProto,
							finProto,foutProto,stProto);
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
		return chains;
	}
	
	public List getChainExecutions() {
		if (chainExecutions == null ||chainExecutions.size() == 0) {
			try {
				ChainExecutionData ceProto = new ChainExecutionData();
				BrowserDatasetSummary dsProto = new BrowserDatasetSummary();
				LayoutChainData acProto = new LayoutChainData();
				NodeExecutionData neProto = new NodeExecutionData();
				LayoutNodeData anProto = new LayoutNodeData(); 
				ChainModuleData mProto = new ChainModuleData();
				ModuleExecutionData meProto = new ModuleExecutionData();
				DataManagementService dms = registry.getDataManagementService();
				chainExecutions = 
					dms.retrieveChainExecutions(ceProto,dsProto,
							acProto,neProto,anProto,mProto,meProto);
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
		return chainExecutions;
	}
	
	public void setAnalysisNodes(List analysisNodes) {
		this.analysisNodes = analysisNodes;
	}
	
	public ChainModuleData getModule(int id) {
		List mods = getModules();
		if (mods == null)
			return null;
		Iterator iter = mods.iterator();
		while (iter.hasNext()) {
			ChainModuleData m = (ChainModuleData) iter.next();
			if (m.getID() == id)
				return m;
		}
		return null;
	}
	
	public LayoutChainData getChain(int id) {
		List chains = getChains();
		if (chains == null)
			return null;
		Iterator iter = chains.iterator();
		while (iter.hasNext()) {
			LayoutChainData c = (LayoutChainData) iter.next();
			if (c.getID() == id)
				return c;
		}
		return null;
	}
	
	public BrowserDatasetSummary getDataset(int id ) {
		List datasets = getDatasets();
		if (datasets == null)
			return null;
		Iterator iter = datasets.iterator();
		while (iter.hasNext()) {
			BrowserDatasetSummary d = (BrowserDatasetSummary) iter.next();
			if (d.getID() == id) 
				return d;
		}
		return null;
	}
	
	public LayoutNodeData getAnalysisNode(int id) {
		if (analysisNodes == null)
			return null;
		Iterator iter = analysisNodes.iterator();
		while (iter.hasNext()) {
			LayoutNodeData n = (LayoutNodeData) iter.next();
			if (n.getID() ==id)
				return n;
		}
		return null;
	}
}
