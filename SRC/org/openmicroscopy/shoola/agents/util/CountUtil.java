/*
 * org.openmicroscopy.shoola.agents.util.CountUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util;




//Java imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * 
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
public class CountUtil
{

	/**
	 * Returns <code>true</code> it the object has been updated by the current
	 * user, <code>false</code> otherwise.
	 * 
	 * @param object	The object to handle.
	 * @param userID	The id of the current user.
	 * @return See above.
	 */
	public static boolean annotatedByCurrentUser(Object object, long userID)
	{
		if (object == null) return false;
		Map<Long, Long> counts = null;
		if (object instanceof DatasetData) 
			counts = ((DatasetData) object).getAnnotationsCounts();
			
		else if (object instanceof ProjectData) {
			counts = ((ProjectData) object).getAnnotationsCounts();
		}
		else if (object instanceof ImageData) {
			counts = ((ImageData) object).getAnnotationsCounts();
		}
			
		if (counts == null || counts.size() == 0) return false;
		return counts.keySet().contains(userID);
	}
	
	/**
	 * Returns <code>true</code> it the object has been updated by an
	 * user other than the current user, <code>false</code> otherwise.
	 * 
	 * @param object	The object to handle.
	 * @param userID	The id of the current user.
	 * @return See above.
	 */
	public static boolean annotatedByOtherUser(Object object, long userID)
	{
		if (object == null) return false;
		Map<Long, Long> counts = null;
		if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();
		else if (object instanceof DatasetData) {
			
			counts = ((DatasetData) object).getAnnotationsCounts();
		}
			
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		
		if (counts == null || counts.size() == 0) return false;
		Set set = counts.keySet();
		if (set.size() > 1) return true;
		return !set.contains(userID);
	}
	
	public static Map getAnnotations(Object object, Map userGroups)
	{
		Map<ExperimenterData, Long> 
			result = new HashMap<ExperimenterData, Long>();
		if (object == null) return result;
		Map<Long, Long> counts = null;
		if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();
		else if (object instanceof DatasetData)
			counts = ((DatasetData) object).getAnnotationsCounts();
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		
		if (counts == null) return result;
		Iterator i = userGroups.keySet().iterator(), j;
		GroupData g;
		Map<Long, ExperimenterData> 
			users = new HashMap<Long, ExperimenterData>();
		Set children;
		ExperimenterData user;
 		while (i.hasNext()) {
			g = (GroupData) i.next();
			children = g.getExperimenters();
			if (children != null) {
				j = children.iterator();
				while (j.hasNext()) {
					user = (ExperimenterData) j.next();
					users.put(user.getId(), user);
				}
			}
		}
 		i = counts.keySet().iterator();
 		Long id;
 		while (i.hasNext()) {
 			id = (Long) i.next();
 			user = users.get(id);
 			if (user != null)
 				result.put(users.get(id), counts.get(id));
		}
		return result;
	}
	
}
