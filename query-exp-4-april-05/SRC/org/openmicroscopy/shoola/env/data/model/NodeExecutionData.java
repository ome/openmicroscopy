/*
 * org.openmicroscopy.shoola.env.data.model.NodeExecutionData
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

package org.openmicroscopy.shoola.env.data.model;

//Java imports
import java.util.Date;

//Third-party libraries

//Application-internal dependencies

/** 
 * An analysis chain node execution object
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
public class NodeExecutionData implements DataObject
{

	private int id;
	private ChainExecutionData chainExecution;
	private AnalysisNodeData analysisNode;
	private ModuleExecutionData moduleExecution;
	
	private Date date;
	
	public NodeExecutionData() {}
	
	public NodeExecutionData(int id,ChainExecutionData chainExecution,
			AnalysisNodeData analysisNode,ModuleExecutionData moduleExecution) 
	{	
		this.id = id;
		this.chainExecution = chainExecution;
		this.analysisNode = analysisNode;
		this.moduleExecution = moduleExecution;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new NodeExecutionData(); }
	
	public int getID() {
		return id;
	}

	public AnalysisNodeData getAnalysisNode() {
		return analysisNode;
	}
	
	public ChainExecutionData getChainExecution() {
		return chainExecution;
	}
	
	public ModuleExecutionData getModuleExecution() {
		return moduleExecution;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setID(int i) {
		id = i;
	}
	
	public void setAnalysisNode(AnalysisNodeData analysisNode) {
		this.analysisNode = analysisNode;
	}
	
	public void setChainExecution(ChainExecutionData chainExecution) {
		this.chainExecution = chainExecution;
	}
	
	public void setModuleExecution(ModuleExecutionData moduleExecution) {
		this.moduleExecution = moduleExecution;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
}
