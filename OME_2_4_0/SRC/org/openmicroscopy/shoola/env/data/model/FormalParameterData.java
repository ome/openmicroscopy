/*
 * org.openmicroscopy.shoola.env.data.model.FormalParameterData
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

//Third-party libraries

//Application-internal dependencies

/** 
 * Module Formal Inputs
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
public class FormalParameterData extends OMEDataObject
{

	private ModuleData			module;
	private boolean 			isOptional;
	private boolean 			isList;
	private SemanticTypeData	semanticType;
	
						
	
	public FormalParameterData() {}
	
	public FormalParameterData(int id, String name, String description,ModuleData
			module,boolean isOptional,boolean isList,SemanticTypeData 
			semanticType) 
	{
		super(id,name,description);
		this.module = module;
		this.isOptional = isOptional;
		this.isList = isList;
		this.semanticType = semanticType;
	
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new FormalParameterData(); }
	
	
	public boolean isOptional() {
		return isOptional;
	}

	public ModuleData getModule() {
		return module;
	}

	public SemanticTypeData getSemanticType() {
		return semanticType;
	}
	
	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	public void setModule(ModuleData module) {
		this.module = module;
	}

	public void setSemanticType(SemanticTypeData semanticType) {
		this.semanticType = semanticType;
	}

}
