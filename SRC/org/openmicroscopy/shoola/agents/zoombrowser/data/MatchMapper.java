/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.MatchMapper
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

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports
import java.util.HashMap;
import java.util.Vector;
//Third-party libraries

//Application-internal dependencies

/** 
 * 
 * Generic code for managing the correspondence between objects.
 * Since Shoola & OME-Java don't have any guarantee of object uniqueness, 
 * we might have multiple Java objects corresponding to a given OME ID. 
 * Thus, for example, we might have multiple instances of 
 * {@link BrowserImageSummary} for a single OME image. 
 * 
 * This presents a challenge for interactions that involve acting on all of the
 * objects that interact with an instance of an ome object. For example, 
 * all of the {@link Thumbnail} objects associated with an image. We can't just
 * keep the list in the {@link BrowserImageSummary} instance - there may be
 * several of these for each OME Image. Instead, we use this class to
 * store a hash that is static for BrowserImageSummary. In this hash, we store
 * lists of thumbnails, indexed by image id.We can examine this list to 
 * retrieve the list of thumbnails that go with a given image. Similar use of 
 * this class will be made for {@link ModuleData}, {@link FormalInputData},
 * {@link FormalOutputData}, and other classes.
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class MatchMapper 
{
	private HashMap map = new HashMap();
	 
	
	public MatchMapper() {}
	

	public void addMatch(int id,Object object) {
	
		Vector items;
		Integer ID = new Integer(id);
		Object obj = map.get(ID);
		if (obj == null) {
			 items = new Vector();
		}
		else
			items = (Vector) obj;
		items.add(object);
		map.put(ID,items);
	}
	
	public Vector getMatches(int id) {
		Vector items = (Vector) map.get(new Integer(id));
		return items;	
	}
	
	public void removeMatch(int id,Object object) {
		Integer ID = new Integer(id);
		Object obj = map.get(ID);
		if (obj != null) {
			Vector items = (Vector) obj;
			items.remove(object);
			map.put(ID,items);
		}
	}
}
