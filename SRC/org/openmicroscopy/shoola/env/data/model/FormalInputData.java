/*
 * org.openmicroscopy.shoola.env.data.model.FormalInputData
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
import org.openmicroscopy.ds.dto.FormalInput;
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
public class FormalInputData
	extends FormalParameterData
{

	
	private LookupTableData		lookupTable;
	private boolean				isUserDefined;
	private List				actualInputs;
						
	private FormalInput 		formalInputDTO;
	
	public FormalInputData() {};
	
	public FormalInputData(int id, String name, String description,
			ModuleData module,boolean isOptional,boolean isList,
			SemanticTypeData semanticType,LookupTableData lookupTable, 
			boolean isUserDefined,List actualInputs) 
	{
		super(id,name,description,module,isOptional,isList,semanticType);
		this.lookupTable = lookupTable;
		this.isUserDefined=  isUserDefined;
		this.actualInputs = actualInputs;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new FormalInputData(); }

	
	public List getActualInputs() {
		return actualInputs;
	}

	public boolean isUserDefined() {
		return isUserDefined;
	}

	public LookupTableData getLookupTable() {
		return lookupTable;
	}
	
	public FormalInput getFormalInputDTO() {
		return formalInputDTO;
	}
	

	public void setActualInputs(List actualInputs) {
		this.actualInputs = actualInputs;
	}
	
	public void setUserDefined(boolean isUserDefined) {
		this.isUserDefined = isUserDefined;
	}

	public void setLookupTable(LookupTableData lookupTable) {
		this.lookupTable = lookupTable;
	}
	
	public void setFormalInputDTO(FormalInput formalInputDTO) {
		this.formalInputDTO = formalInputDTO;
	}
}
