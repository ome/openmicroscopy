/* 
 * org.openmicroscopy.shoola.agents.executions.ui.model.ExecutionsModel
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

package org.openmicroscopy.shoola.agents.executions.ui.model;

//Java imports
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Iterator;

import java.util.Vector;
import javax.swing.event.ChangeListener;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.ChainExecutionsLoadedEvent;
import org.openmicroscopy.shoola.agents.executions.ui.LongRangeSlider;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DatasetData;


/** 
* A model of chain executions data
*
* @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/

public class ExecutionsModel {
	
	public static final String[] modes =  {"Datasets", "Chains"};
	private static final int DATASET_ORDER=0;
	private static final int CHAIN_ORDER=1;
	
	private int currentOrder;
	
	private LinkedHashMap execsByDataset;
	private LinkedHashMap execsByChain;
	private Vector sortedExecs;
	private BoundedLongRangeModel model= null;
	
	private String datasetNames[];
	private String chainNames[];
	
	private GridModel gm;
	
	public ExecutionsModel(ChainExecutionsLoadedEvent event) {
		this.execsByDataset = event.getChainExecutionsByDatasetID();
		this.execsByChain = event.getChainExecutionsByChainID();
		
		LinkedHashMap byID = event.getExecutionsByID();
		sortedExecs = new Vector(byID.values());
		Collections.sort(sortedExecs);
		getDatasetNames();
		getChainNames();
	}
	
	// execs by dataset is a hash on dataset ids. each value is a list of
	// executions for the given dataset. Probably should be wrapped in
	// its own class. someday.
	
	private void getDatasetNames(){
		int sz = execsByDataset.keySet().size();
		datasetNames = new String[sz];
		Iterator iter = execsByDataset.keySet().iterator();
		int i = 0;
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			Collection exs = (Collection) execsByDataset.get(obj);
			Iterator iter2 = exs.iterator();
			obj = iter2.next();
			ChainExecutionData exec = (ChainExecutionData) obj;
		    DatasetData ds = exec.getDataset();
			datasetNames[i++] = ds.getName();	
			
		}
	
	}
	
	public void getChainNames() {
		int sz = execsByChain.keySet().size();
		chainNames = new String[sz];
		Iterator iter = execsByChain.keySet().iterator();
		int i = 0;
		
		while (iter.hasNext()) {
			Object obj = iter.next();
			Collection exs = (Collection) execsByChain.get(obj);
			Iterator iter2 = exs.iterator();
			ChainExecutionData exec = (ChainExecutionData) iter2.next();
		     AnalysisChainData chain = exec.getChain();
			chainNames[i++] = chain.getName();	
		}
	}
	
	public BoundedLongRangeModel getRangeModel() {
		if (model == null) {
			ChainExecutionData exec;
			exec = (ChainExecutionData) sortedExecs.firstElement();
			Date start = exec.getDate();
			exec  = (ChainExecutionData) sortedExecs.lastElement();
			Date end = exec.getDate();
			model= new BoundedLongRangeModel(start.getTime(),end.getTime());
		}
		return model;
	}
	
	public GridModel getGridModel() {
		if (model == null)
			model = getRangeModel();
		if (gm == null) {
			gm = new GridModel(this,model.getMinimum(),model.getMaximum(),
					getLastRowIndex());
			model.addChangeListener(gm);
		}
		return gm;
	}
	
	public LongRangeSlider getSlider() {
		return new LongRangeSlider(getRangeModel());
	}
	
	
	public void resetRangeProperties() {
		ChainExecutionData exec;
		exec = (ChainExecutionData) sortedExecs.firstElement();
		Date start = exec.getDate();
		exec  = (ChainExecutionData) sortedExecs.lastElement();
		Date end = exec.getDate();
		model.setRangeProperties(start.getTime(),end.getTime());
	}

	public int getChainCount() {
		return execsByChain.keySet().size();
	}
	
	public int getDatasetCount() {
		return execsByDataset.keySet().size();
	}
	// either do executions by dataset, and then by chain within datasets.
	// either way, entries go from row 1...|chains| * |datasets|
	public int getLastRowIndex() {
		int chainCount = getChainCount();
		int datasetCount = getDatasetCount();
		return chainCount*datasetCount;
	}
	
	public Iterator executionIterator() {
		return sortedExecs.iterator();
	}

	//	 eventually, this will change to look at a flag,  but for now, it's dataset 
	// major ordering and then chain within datasets.
	public int getRow(ChainExecutionData exec) {
		
		if (currentOrder == DATASET_ORDER) 
			return getDatasetMajorRow(exec);
		else 
			return getChainMajorRow(exec);
	}
	
	private int getDatasetMajorRow(ChainExecutionData exec) {
		int chain = getChainIndex(exec);
		int dataset = getDatasetIndex(exec);
		return dataset*getChainCount()+chain;
	}
	
	private int getChainMajorRow(ChainExecutionData exec) {
		int chain = getChainIndex(exec);
		int dataset = getDatasetIndex(exec);
		return chain*getDatasetCount()+dataset;
	}
	
	public String getMajorRowLabel(int count) {
		if (currentOrder == DATASET_ORDER) {
			if (count < datasetNames.length)
				return datasetNames[count];
			else
				return null;
		}
		else {
			if (count < chainNames.length) 
				return chainNames[count];
			else
				return null;
		}
	}
		
	public int getMajorRowCount() {
		if (currentOrder == DATASET_ORDER) 
			return getDatasetCount();
		else
			return getChainCount();
	}
	
	public int getMinorRowCount() {
		if (currentOrder == DATASET_ORDER)
			return getChainCount();
		else 
			return getDatasetCount();
	}
	
	
	
	public void setRenderingOrder(String choice) {
		System.err.println("changing rendering order..");
		for (int i = 0; i < modes.length; i++) {
			if (choice.compareTo(modes[i]) == 0)
				currentOrder = i;
		}
		getGridModel().clearDecorations();
	}
	
	private int getChainIndex(ChainExecutionData exec) {
		int id = exec.getChain().getID();
		return getIndex(id,execsByChain);
	}
	
	
	private int getDatasetIndex(ChainExecutionData exec) {
		int id = exec.getDataset().getID();
		return getIndex(id,execsByDataset);
	}
	
	
	
	private int getIndex(int id,LinkedHashMap execHash) {
		int index = 0;
		Iterator iter = execHash.keySet().iterator();
		while (iter.hasNext()) {
			Integer ID = (Integer) iter.next();
			if (ID.intValue() == id)
				return index;
			index++;
		}
		return -1;
	}
	
	public void addChangeListener(ChangeListener listener) {
		model.addChangeListener(listener);
	}
	
	public boolean isInRange(long time) {
		return model.isInRange(time);
	}
}

