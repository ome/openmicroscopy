/*
 * org.openmicroscopy.shoola.env.data.model.ModuleData
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.Module;

/** 
 * A module object
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
public class ModuleData
	extends OMEDataObject
{

	private String				moduleType;
	private String 				location;
	private ModuleCategoryData	moduleCategory;
	private String 				defaultIterator;
	private String 				newFeatureTag;
	private List				formalInputs;
	private List 				formalOutputs;
	private List 				executions;
	private String				executionInstructions;
	
	private Module 				moduleDTO;
	
	public ModuleData() {}
	
	public ModuleData(int id, String name, String description,String
		moduleType,String location,ModuleCategoryData moduleCategory,
		String defaultIterator,String newFeatureTag,List formalInputs,
		List formalOutputs,List executions,String executionInstructions) 
	{
		super(id,name,description);
		this.moduleType = moduleType;
		this.location = location;
		this.moduleCategory = moduleCategory;
		this.defaultIterator  = defaultIterator;
		this.newFeatureTag = newFeatureTag;
		this.formalInputs = formalInputs;
		this.formalOutputs = formalOutputs;
		this.executions = executions;
		this.executionInstructions = executionInstructions;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ModuleData(); }

	
	
	public String getModuletype() { return moduleType; }
	
	public String getLocation() { return location; }
	
	public ModuleCategoryData getModuleCategory() { return moduleCategory; }
	
	public String getNewFeatureTag() {
		return newFeatureTag;
	}

	public String getDefaultIterator() {
		return defaultIterator;
	}

	public List getFormalInputs() {
		return formalInputs;
	}

	public List getFormalOutputs() {
		return formalOutputs;
	}

	public List getExecutions() {
		return executions;
	}

	public String getExecutionInstructions() {
		return executionInstructions;
	}

	public Module getModuleDTO() {
		return moduleDTO;
	}
	
	public void setModuleType(String moduleType) { 
		this.moduleType = moduleType; 
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setModuleCategory(ModuleCategoryData moduleCategory) {
		this.moduleCategory = moduleCategory;	
	}
	
	public void setDefaultIterator(String defaultIterator) {
		this.defaultIterator = defaultIterator;
	}
	
	public void setNewFeatureTag(String newFeatureTag) {
		this.newFeatureTag = newFeatureTag;
	}
	
	public void setFormalInputs(List formalInputs) {
		this.formalInputs = formalInputs;
	}

	public void setFormalOutputs(List formalOutputs) {
		this.formalOutputs = formalOutputs;
	}

	public void setExecutions(List executions) {
		this.executions = executions;
	}

	public void setExecutionInstructions(String executionInstructions) {
		this.executionInstructions = executionInstructions;
	}

	public void setModuleDTO(Module moduleDTO) {
		this.moduleDTO=moduleDTO;
	}
}
