/*
 * org.openmicroscopy.shoola.env.data.model.ModuleCategoryData
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

/** 
 * A module category object
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
public class ModuleCategoryData
	implements DataObject
{

	private int 				id;
	private String 				name;
	private String 				description;
	private ModuleCategoryData 	parentCategory;
	private List 				childCategories;
	private List				modules;
	
	
	
	public ModuleCategoryData() {}
	
	public ModuleCategoryData(int id, String name, String description, 
						ModuleCategoryData parentCategory,
						List childCategories,List modules) 
						
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentCategory = parentCategory;
		this.childCategories = childCategories;
		this.modules = modules
	}
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ModuleCategoryData(); }

	public String getDescription() { return description; }

	public int getID() { return id; }

	public String getName() { return name; }
	
	public ModuleCategoryData getParentCategory() {
		return parentCategory;
	}
	
	public List getChildCategories() {
		return childCategories;
	}
	
	public List getModules() { return modules; }

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setID(int id) { this.id = id; }

	public void setName(String name) { this.name = name; }

	public void setParentCategory(ModuleCategoryData parentCategory) {
		this.parentCategory = parentCategory;
	}
	
	public void setChildCategories(List childCategories){
		this.childCategories=childCategories;
	}
	
	public void setModules(List modules) {
		this.modules = modules;
	}

}
