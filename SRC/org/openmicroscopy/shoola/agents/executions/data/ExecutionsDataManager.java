/*
 * org.openmicroscopy.shoola.agents.executions.ExecutionsDataManager
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
 
package org.openmicroscopy.shoola.agents.executions.data;


//Java imports
import java.util.Collection;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;

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

public class ExecutionsDataManager extends DataManager {

		

	private boolean gettingExecutions =false;
	
	private ExecutionsData executionsData; 
	
	
	public ExecutionsDataManager(Registry registry) {
		super(registry);
	}
		
	public Registry getRegistry() {return registry; }
	
	
	
	
	public ExecutionsData getChainExecutions() {
		
		long start;
		// if we're done, go for it.
		if (executionsData != null) {
			return executionsData;
		}
		
		if (gettingExecutions == false) {
			gettingExecutions = true;
			retrieveExecutionsData();
			gettingExecutions = false;
		//	notifyAll();
			return executionsData;
		}
		return null;
	}
	
	
	
	public synchronized void retrieveExecutionsData() {
		if (executionsData== null) {
			try {
				ChainExecutionData ceProto = new ChainExecutionData();
				DatasetData dsProto = new DatasetData();
				AnalysisChainData acProto = new AnalysisChainData();
				NodeExecutionData neProto = new NodeExecutionData();
				AnalysisNodeData anProto = new AnalysisNodeData(); 
				ModuleData mProto = new ModuleData();
				ModuleExecutionData meProto = new ModuleExecutionData();
				DataManagementService dms = registry.getDataManagementService();
				Collection execs = 
					dms.retrieveChainExecutions(ceProto,dsProto,
							acProto,neProto,anProto,mProto,meProto);
				executionsData = new ExecutionsData(execs);

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
		if (executionsData!= null)
			return executionsData.chainHasExecutionsForDataset(chainID,datasetID);
		return false;
	}
}
