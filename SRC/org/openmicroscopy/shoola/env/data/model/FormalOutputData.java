/*
 * org.openmicroscopy.shoola.env.data.model.FormalOutputData
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
 * Module Formal outputs
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
public class FormalOutputData
	extends FormalParameterData
{

	
	private String featureTag;
						
	
	public FormalOutputData() {};
	
	public FormalOutputData(int id, String name, String description,
			ModuleData module,boolean isOptional,boolean isList,
			SemanticTypeData semanticType,String featureTag) 
	{
		super(id,name,description,module,isOptional,isList,semanticType);
		this.featureTag = featureTag;
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new FormalOutputData(); }

	
	public String getFeatureTag() {
		return featureTag;
	}

	public void setFeatureTag(String featureTag) {
		this.featureTag = featureTag;
	}
}
