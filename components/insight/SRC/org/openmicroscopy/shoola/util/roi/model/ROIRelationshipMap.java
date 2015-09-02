/*
 * org.openmicroscopy.shoola.util.roi.model.ROIRelationshipMap 
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

package org.openmicroscopy.shoola.util.roi.model;

import java.util.HashMap;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since OME3.0
 */
public class ROIRelationshipMap
{
	HashMap <Long, ROIRelationshipList> roiRelationshipMap;
	HashMap <Long, ROIRelationship> relationshipMap;
	
	public ROIRelationshipMap()
	{
		roiRelationshipMap = new HashMap<Long, ROIRelationshipList>();
		relationshipMap = new HashMap<Long, ROIRelationship>();
	}
	
	public boolean contains(long relationship)
	{
		return relationshipMap.containsKey(relationship);
	}
	
	public void add(ROIRelationship relationship)
	{
		long parentID = relationship.getParent().getID();
		long childID = relationship.getChild().getID();
		
		relationshipMap.put(relationship.getID(), relationship);
		
		ROIRelationshipList list;
		if(!roiRelationshipMap.containsKey(parentID))
		{
			list = new ROIRelationshipList(parentID);
			roiRelationshipMap.put(parentID, list);
		}
		list = roiRelationshipMap.get(parentID);
		list.add(relationship.getID());
		if(!roiRelationshipMap.containsKey(childID))
		{
			list = new ROIRelationshipList(childID);
			roiRelationshipMap.put(childID, list);
		}
		list = roiRelationshipMap.get(childID);
		list.add(relationship.getID());
	}
	
	public void remove(long relationshipID)
	{
		ROIRelationship relationship = relationshipMap.get(relationshipID);
		ROIRelationshipList list;
		list = roiRelationshipMap.get(relationship.getParent().getID());
		list.remove(relationshipID);
		list = roiRelationshipMap.get(relationship.getChild().getID());
		list.remove(relationshipID);
		relationshipMap.remove(relationshipID);
		
	}
	
	public ROIRelationship getRelationship(long id)
	{
		return relationshipMap.get(id);
	}
	
	public ROIRelationshipList getRelationshipList(long id)
	{
		return roiRelationshipMap.get(id);
	}
}


