/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutions
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
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
//import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
//import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutNodeData;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
//import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;


/** 
 * A container class for information about chain executions
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
public class ChainExecutions {
	
	/* we iterate over key sets on these, so treemaps are good */
	private Map byExecId = new TreeMap();
	private Map byChainId = new TreeMap();
	private Map byDatasetId = new TreeMap();
	
	private String datasetNames[];
	private String chainNames[];
	
	private long firstExecTime = Long.MAX_VALUE;
	private long lastExecTime = Long.MIN_VALUE;

	/*private transient Map nodeCounts = new HashMap();
	
	private transient Map nexesByMex = new HashMap();
	private transient Map nexesByModule = new HashMap();
	
	private int maxNodeExecutionCount = 0; */
	
	public ChainExecutions(Collection executions) {
		Iterator iter = executions.iterator();
		ChainExecutionData exec;
		
		if (executions.size() == 0) {
			firstExecTime = lastExecTime;
			return;
		}
		
		while (iter.hasNext()) {
			exec = (ChainExecutionData) iter.next();
			addChainExecution(exec);
			Date date = exec.getDate();
			long time = date.getTime();
			if (time < firstExecTime)
				firstExecTime = time;
			if (time >lastExecTime)
				lastExecTime = time;
		}
		
		// build list of chain names
		getChainNames();
		
		// build list of dataset names.
		getDatasetNames();
	}
	
	private void addChainExecution(ChainExecutionData exec) {
		
		// add it to hash by exec id.
		addToMap(byExecId,exec.getID(),exec);
		
		// add it to hash by chain id
		addToMappedList(byChainId,exec.getChain().getID(),exec);
		
		// add it to hash by dataset id.
		addToMappedList(byDatasetId,exec.getDataset().getID(),exec);
		
		// find module with most executions
		//addNodeExecution(exec);
	}
	
	/*private void addNodeExecution(ChainExecutionData exec) {
		// look at all of the node executions
		Collection nodeExecs = exec.getNodeExecutions();
		Iterator iter = nodeExecs.iterator();
		ChainNodeExecutionData nodeExec;
		LayoutNodeData node;
		
		while (iter.hasNext()) {
			nodeExec = (ChainNodeExecutionData) iter.next();
			// add nex by mex
			addNodeExecution(nodeExec);
			node = (LayoutNodeData) nodeExec.getAnalysisNode();
			addExecutionForNode(node);
		}
	}
	
	private void addNodeExecution(ChainNodeExecutionData nodeExec) {
		ModuleExecutionData mex = nodeExec.getModuleExecution();
		ChainModuleData mod = (ChainModuleData) mex.getModule();
		addToMexNexHash(mex,nodeExec);
		addToModuleNexHash(mod,nodeExec);
	}
	
	private void addToMexNexHash(ModuleExecutionData mex,ChainNodeExecutionData nodeExec) {
		addToMappedList(nexesByMex,mex.getID(),nodeExec);
	}
	
	private void addToModuleNexHash(ChainModuleData mod,ChainNodeExecutionData nodeExec) {
		addToMappedList(nexesByModule,mod.getID(),nodeExec);
	}
	
	private void addExecutionForNode(LayoutNodeData node) {
		int count;
		Integer c;
		Integer hashIndex = new Integer(node.getID());
		Object obj  = nodeCounts.get(hashIndex);
		if (obj == null) 
			count = 0;
		else {
			c = (Integer) obj;
			count = c.intValue();
		}
		count++; // add one for current
		if (count > maxNodeExecutionCount)
			maxNodeExecutionCount = count;
		c = new Integer(count);
		nodeCounts.put(hashIndex,c);
	}*/

	private void addToMap(Map map,int id,Object newObj) {
		Integer ID = new Integer(id);
		map.put(ID,newObj);
	}
	
	private void addToMappedList(Map map,int id,Object newObj) {
		Integer ID = new Integer(id);
		Object obj = map.get(ID);
		Vector items;
		if (obj == null) {
			items = new Vector();
		}
		else 
			items = (Vector) obj;
		items.add(newObj);
		map.put(ID,items);
	}
	
	/*public int getMaxNodeExecutionCount() {
		return maxNodeExecutionCount;
	}*/
	
	public Collection getExecutions() {
		
		Vector sortedExecs = new Vector(byExecId.values());
		Collections.sort(sortedExecs);
		return sortedExecs;
	}
	
	private void getDatasetNames(){
		int sz = byDatasetId.keySet().size();
		datasetNames = new String[sz];
		Iterator iter = byDatasetId.keySet().iterator();
		int i = 0;
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			Collection exs = (Collection) byDatasetId.get(obj);
			Iterator iter2 = exs.iterator();
			obj = iter2.next();
			ChainExecutionData exec = (ChainExecutionData) obj;
		    DatasetData ds = exec.getDataset();
			datasetNames[i++] = new String(ds.getID()+". "+ds.getName());	
		}
	}
	
	public int getDatasetCount() {
		return byDatasetId.keySet().size();
	}
	
	private void getChainNames() {
		int sz = byChainId.keySet().size();
		chainNames = new String[sz];
		Iterator iter = byChainId.keySet().iterator();
		int i = 0;
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			Collection exs = (Collection) byChainId.get(obj);
			Iterator iter2 = exs.iterator();
			ChainExecutionData exec = (ChainExecutionData) iter2.next();
		    AnalysisChainData chain = exec.getChain();
			chainNames[i++] = chain.getName();	
		}
	}
	
	public int getChainCount() {
		return byChainId.keySet().size();
	}
	
	// to be revised.
	public ChainExecutionsByNodeID getChainExecutionsByChainID(int id) {
		Integer ID = new Integer(id);
		return new ChainExecutionsByNodeID((Collection) byChainId.get(ID));
	}
	
	public Collection getChainExecutionsByDatasetID(int id) {
		Integer ID = new Integer(id);
		return (Collection) byDatasetId.get(ID);
	}
	
	public String getDatasetName(int i) {
		if (i < datasetNames.length)
			return datasetNames[i];
		return null;
	}
	
	public String getChainName(int i) {
		if (i < chainNames.length)
			return chainNames[i];
		return null;
	}
	
	public int getChainIndex(ChainExecutionData exec) {
		int id = exec.getChain().getID();
		return getIndex(id,byChainId);
	}
	
	public int getDatasetIndex(ChainExecutionData exec) {
		int id = exec.getDataset().getID();
		return getIndex(id,byDatasetId);
	}
	
	private int getIndex(int id,Map map) {
		int index = 0;
		Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Integer ID = (Integer) iter.next();
			if (ID.intValue() == id)
				return index;
			index++;
		}
		return -1;
	}
	
	public boolean chainHasExecutionsForDataset(int chainID,int datasetID) {
		Collection chainExecs = (Collection) 
			byChainId.get(new Integer(chainID));
		 
		// we might be tempted to get the execs for the dataset id 
		// and then check to see if the intersection of the two-lists is non-null
		// but this won't work - we can't just look at object equality,
		// we have to check ids. so, iterate.
		
		if (chainExecs == null)
			return false;
		
		Iterator iter = chainExecs.iterator();
		ChainExecutionData exec;
		while (iter.hasNext()) {
			exec = (ChainExecutionData) iter.next();
			if (exec.getDataset().getID() == datasetID)
				return true;
		}
		return false; 
	}
	
	public long getStartTime() {
		return firstExecTime;
	}
	
	public long getEndTime() {
		return lastExecTime;
	}

	/*public Collection getNexesForMex(int mexid) {
		Integer ID = new Integer(mexid);
		return (Collection) nexesByMex.get(ID);
	}
	
	public Collection getNexesForModule(int modid) {
		Integer ID = new Integer(modid);
		return (Collection) nexesByModule.get(ID);
	}*/
	
}