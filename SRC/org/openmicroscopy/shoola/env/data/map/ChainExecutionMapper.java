/*
 * org.openmicroscopy.shoola.env.data.map.ChainExecutionMapper
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

package org.openmicroscopy.shoola.env.data.map;



//Java imports
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.AnalysisChain;
import org.openmicroscopy.ds.dto.AnalysisNode;
import org.openmicroscopy.ds.dto.ChainExecution;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Module;
import org.openmicroscopy.ds.dto.ModuleExecution;
import org.openmicroscopy.ds.dto.NodeExecution;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;

/** 
 * Mapper for analysis chain executions
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 *
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ChainExecutionMapper 
{
		
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieving chain executions
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildChainExecutionCriteria(int userID)
	{
		Criteria criteria = new Criteria();
	
		//Specify which fields we want for the chain.
		criteria.addWantedField("id");
		criteria.addWantedField("analysis_chain");
		criteria.addWantedField("dataset");
		criteria.addWantedField("node_executions");
		criteria.addWantedField("timestamp");
		
		// stuff for dataset
		criteria.addWantedField("dataset","id");
		criteria.addWantedField("dataset","name");
		criteria.addWantedField("dataset","owner_id");
		
		// stuff for chains
		criteria.addWantedField("analysis_chain","id");
		criteria.addWantedField("analysis_chain","name");
		
		// stuff for node executions
		criteria.addWantedField("node_executions","id");
		
		criteria.addWantedField("node_executions","module_execution");
		criteria.addWantedField("node_executions.module_execution","id");
		criteria.addWantedField("node_executions.module_execution","timestamp");
		
		criteria.addWantedField("node_executions.module_execution","module");
		
		criteria.addWantedField("node_executions.module_execution.module","id");
		criteria.addWantedField("node_executions.module_execution.module","name");
	
		
		criteria.addFilter("experimenter_id",new Integer(userID));
		criteria.addFilter("dataset.owner_id",new Integer(userID));
		criteria.addFilter("dataset.name", "NOT LIKE", "ImportSet");
		return criteria;
	}


	
	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillChainExecutions(List execs,ChainExecutionData ceProto,
		DatasetData dsProto,AnalysisChainData acProto,NodeExecutionData 
		neProto,ModuleData mProto,ModuleExecutionData
		meProto)
	{
		List execList= new ArrayList();  //The returned summary list.
		Iterator i = execs.iterator();
		
		ChainExecution e;
		ChainExecutionData exec;
		Dataset d;
		DatasetData ds;
		AnalysisChain c;
		AnalysisChainData chain;
		
		
	
		

		while (i.hasNext()) {
			e = (ChainExecution) i.next();
			
			//Make a new DataObject and fill it up.
			exec = (ChainExecutionData) ceProto.makeNew();
			exec.setID(e.getID());
			exec.setTimestamp(e.getTimestamp());
			exec.setDate(getDate(e.getTimestamp()));
			// dataset
			d = e.getDataset();
			ds = (DatasetData) dsProto.makeNew();
			ds.setID(d.getID());
			ds.setName(d.getName());
			exec.setDataset(ds);
			
			//chain
			c = e.getChain();
			chain = (AnalysisChainData) acProto.makeNew();
			chain.setID(c.getID());
			chain.setName(c.getName());
			exec.setChain(chain);
			
			//node executions
			getNodeExecutions(exec,e,neProto,mProto,meProto,chain);
			execList.add(exec);
		}
		
		return execList;
	}
	
	private static void getNodeExecutions(ChainExecutionData exec,ChainExecution e,
			NodeExecutionData neProto,ModuleData mProto,
			ModuleExecutionData meProto,AnalysisChainData chain) {
		ArrayList nodeExecutionList = new ArrayList();
		
		List executions = e.getNodeExecutions();
		NodeExecution ne;
		NodeExecutionData nodeExecution;
		AnalysisNode n;
		AnalysisNodeData analysisNode;
		ModuleExecution me;
		ModuleExecutionData moduleExecution;
		Module m;
		ModuleData module;
		
		Iterator i = executions.iterator();
		
		while (i.hasNext()) {
			ne = (NodeExecution) i.next();
			nodeExecution = (NodeExecutionData) neProto.makeNew();
			nodeExecution.setID(ne.getID());
		
			// get the module execution
			me = ne.getModuleExecution();
			moduleExecution = (ModuleExecutionData) meProto.makeNew();
			moduleExecution.setID(me.getID());
			//moduleExecution.setStatus(me.getStatus());
			moduleExecution.setTimestamp(me.getTimestamp());
			moduleExecution.setDate(getDate(me.getTimestamp()));
			nodeExecution.setModuleExecution(moduleExecution);
			
			m = me.getModule();
			module = (ModuleData) mProto.makeNew();
			module.setID(m.getID());
			module.setName(m.getName());
			moduleExecution.setModule(module);
	
			nodeExecutionList.add(nodeExecution);
		}
		
		exec.setNodeExecutions(nodeExecutionList);
	}
	
	private static Date getDate(String time) {
		Date date=null;
		String parsed =null;
		ParsePosition pos = new ParsePosition(0);
	
		// this code is a bit sketchy. might be postgresql dependent.
		// based in experience (not postgres docs), times can either look like
		// 2004-01-29 21:49:45.50726 or 
		// 2004-05-25 18:10:56-04
		// for first case, throw out everything including and after last period
		// otherwise, throw out everything including and after the dash.
		int index = time.lastIndexOf(".");
		if (index != -1) {
			parsed = time.substring(0,index);
		}
		else {
			index = time.lastIndexOf("-");
			if (index != -1)
				parsed = time.substring(0,index);
		}
		if (parsed != null) {
			SimpleDateFormat format = new 
				SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setLenient(true);
			date = format.parse(time,pos);
		}
		return date;
	}
	

}
