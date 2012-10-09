/*
 * org.openmicroscopy.shoola.util.roi.model.ROIShapeRelationshipMap 
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

//Java imports
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIShapeRelationshipMap
{
	Map <Long, ROIShapeRelationshipList> roiShapeRelationshipMap;
	Map <Long, ROIShapeRelationship> relationshipMap;
	
	public ROIShapeRelationshipMap()
	{
		roiShapeRelationshipMap = new HashMap<Long, ROIShapeRelationshipList>();
		relationshipMap = new HashMap<Long, ROIShapeRelationship>();
	}
	
	public boolean contains(long relationship)
	{
		return relationshipMap.containsKey(relationship);
	}
	
	public void add(ROIShapeRelationship relationship)
	{
		long parentID = relationship.getParent().getID();
		long childID = relationship.getChild().getID();
		
		relationshipMap.put(relationship.getID(), relationship);
		
		ROIShapeRelationshipList list;
		if(!roiShapeRelationshipMap.containsKey(parentID))
		{
			list = new ROIShapeRelationshipList(parentID);
			roiShapeRelationshipMap.put(parentID, list);
		}
		list = roiShapeRelationshipMap.get(parentID);
		list.add(relationship.getID());
		if(!roiShapeRelationshipMap.containsKey(childID))
		{
			list = new ROIShapeRelationshipList(childID);
			roiShapeRelationshipMap.put(childID, list);
		}
		list = roiShapeRelationshipMap.get(childID);
		list.add(relationship.getID());
	}
	
	public void remove(long relationshipID)
	{
		ROIShapeRelationship relationship = relationshipMap.get(relationshipID);
		ROIShapeRelationshipList list;
		list = roiShapeRelationshipMap.get(relationship.getParent().getID());
		list.remove(relationshipID);
		list = roiShapeRelationshipMap.get(relationship.getChild().getID());
		list.remove(relationshipID);
		relationshipMap.remove(relationshipID);
		
	}
	
	public ROIShapeRelationship getRelationship(long id)
	{
		return relationshipMap.get(id);
	}
	
	public ROIShapeRelationshipList getRelationshipList(long id)
	{
		return roiShapeRelationshipMap.get(id);
	}
}

