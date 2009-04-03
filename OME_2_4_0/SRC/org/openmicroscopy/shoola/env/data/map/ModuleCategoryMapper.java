/*
 * org.openmicroscopy.shoola.env.data.map.ModuleCategoryMapper
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.ModuleCategory;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;

/** 
 * Mapper for module catgegories
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
public class ModuleCategoryMapper
{
		
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieving module categories
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildModuleCategoriesCriteria()
	{
		Criteria criteria = new Criteria();
	
		//Specify which fields we want for the project.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("description");
		criteria.addWantedField("parent_category");
	
		//Specify which fields we want for the categories
		criteria.addWantedField("parent_category","name");
		criteria.addWantedField("parent_category","id");
		
		criteria.addOrderBy("name");
	
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
	public static List fillModuleCategories(List categories,
		ModuleCategoryData mcProto,ModuleData mProto)
	{
		List categoriesList = new ArrayList();  //The returned summary list.
		Iterator i = categories.iterator();
		ModuleCategoryData mcd;
		ModuleCategoryData mcdParent;
		
		ModuleCategory mc;
		ModuleCategory parent;
		//For each c in categories
		while (i.hasNext()) {
			mc = (ModuleCategory) i.next();
			
			//Make a new DataObject and fill it up.
			mcd = (ModuleCategoryData) mcProto.makeNew();
			mcd.setID(mc.getID());
			mcd.setName(mc.getName());
			
			parent = mc.getParentCategory();
			if (parent != null) {
				mcdParent = (ModuleCategoryData) mcProto.makeNew();
				mcdParent.setName(parent.getName());
				mcdParent.setID(parent.getID());
				mcd.setParentCategory(mcdParent);
			}
			else
				mcd.setParentCategory(null);
			categoriesList.add(mcd);
		}
		
		return categoriesList;
	}
	
}
