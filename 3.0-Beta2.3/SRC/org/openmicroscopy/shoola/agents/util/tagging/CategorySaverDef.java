/*
 * org.openmicroscopy.shoola.agents.util.tagging.CategorySaverDef 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util.tagging;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.CategoryData;

/** 
 * Helper class used to store information while sending property changes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CategorySaverDef
{

	/** Collection of categories to create. */
	private Set<CategoryData>	categoriesToCreate;
	
	/** Collection of categories to add the image(s) to. */
	private Set<CategoryData>	categoriesToUpdate;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param categoriesToCreate 	The collection of categories to create.
	 * @param categoriesToUpdate 	The collection of categories to add the 
	 * 								image(s) to.
	 */
	public CategorySaverDef(Set<CategoryData> categoriesToCreate, 
			Set<CategoryData> categoriesToUpdate)
	{
		this.categoriesToCreate = categoriesToCreate;
		this.categoriesToUpdate = categoriesToUpdate;
	}
	
	/**
	 * Returns the collection of categories to create.
	 * 
	 * @return See above.
	 */
	public Set<CategoryData> getCategoriesToCreate()
	{
		return categoriesToCreate;
	}
	
	/**
	 * Returns collection of categories to add the image(s) to.
	 * 
	 * @return See above.
	 */
	public Set<CategoryData> getCategoriesToUpdate()
	{
		return categoriesToUpdate;
	}
	
}
