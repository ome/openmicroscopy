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
//import java.util.LinkedHashMap;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutions;
import org.openmicroscopy.shoola.agents.events.LoadChainExecutionsEvent;
import org.openmicroscopy.shoola.agents.executions.ui.LongRangeSlider;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;



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
	
	//private LinkedHashMap execsByDataset;
	//private LinkedHashMap execsByChain;
	private Collection sortedExecs;
	private BoundedLongRangeModel model= null;
	
	
	private GridModel gm;
	
	private ChainExecutions chainExecutions;
	
	public ExecutionsModel(LoadChainExecutionsEvent event) {
		chainExecutions = event.getChainExecutions();
		//this.execsByDataset = event.getChainExecutionsByDatasetID();
		//this.execsByChain = event.getChainExecutionsByChainID();		
		sortedExecs = chainExecutions.getExecutions();
	}
	
	public int size() {
		return sortedExecs.size();
	}
	
	public BoundedLongRangeModel getRangeModel() {
		if (model == null) {
			long start = chainExecutions.getStartTime();
			long end = chainExecutions.getEndTime();
			model= new BoundedLongRangeModel(start,end);
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
		long start = chainExecutions.getStartTime();
		long end = chainExecutions.getEndTime();
		model.setProperties(start,end);
	}

	public int getChainCount() {
		return chainExecutions.getChainCount();
	}
	
	public int getDatasetCount() {
		return chainExecutions.getDatasetCount();
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
			return chainExecutions.getDatasetName(count);
		}
		else {
			return chainExecutions.getChainName(count);
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
		for (int i = 0; i < modes.length; i++) {
			if (choice.compareTo(modes[i]) == 0)
				currentOrder = i;
		}
		getGridModel().clearDecorations();
	}
	
	private int getChainIndex(ChainExecutionData exec) {
		return chainExecutions.getChainIndex(exec);
	}
	
	
	private int getDatasetIndex(ChainExecutionData exec) {
		return chainExecutions.getDatasetIndex(exec);
	}

	
	public void addChangeListener(ChangeListener listener) {
		model.addChangeListener(listener);
	}
	
	public boolean isInRange(long time) {
		return model.isInRange(time);
	}
}

