/*
 * org.openmicroscopy.shoola.agents.util.tagging.util.TagSaverDef 
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
package org.openmicroscopy.shoola.agents.util.tagging.util;



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
public class TagSaverDef
{

	/** Collection of tags to create. */
	private Set<CategoryData>	tagsToCreate;
	
	/** Collection of tags to add the image(s) to. */
	private Set<CategoryData>	tagsToUpdate;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param tagsToCreate 	The collection of tags to create.
	 * @param tagsToUpdate 	The collection of tags to add to the image(s).
	 */
	public TagSaverDef(Set<CategoryData> tagsToCreate, 
			Set<CategoryData> tagsToUpdate)
	{
		this.tagsToCreate = tagsToCreate;
		this.tagsToUpdate = tagsToUpdate;
	}
	
	/**
	 * Returns the collection of tags to create.
	 * 
	 * @return See above.
	 */
	public Set<CategoryData> getTagsToCreate() { return tagsToCreate; }
	
	/**
	 * Returns collection of tags to add to the image(s).
	 * 
	 * @return See above.
	 */
	public Set<CategoryData> getTagsToUpdate() { return tagsToUpdate; }
	
}
