/*
 * org.openmicroscopy.shoola.env.data.views.calls.ExperimenterImagesCounter 
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
package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
* Retrieves the images imported by the specified user during various
* periods of time.
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
public class ExperimenterImagesCounter 
	extends BatchCallTree
{

	/** The id of the user the count is for. */
	private long 						userID;

	/** The lastly retrieved count along side the index. */
	private Map<Integer, Object>		result;

	/** The nodes to handle. */
	private Map<Integer, TimeRefObject> nodes;

	/** Helper reference to the image service. */
	private OmeroDataService 			os;

	/**
	 * Counts the number of items imported during a period of time.
	 * 
	 * @param index	The index identifying the period.
	 * @param ref	The object containing period information.
	 */
	private void countItems(Integer index, TimeRefObject ref)
	{
		try {
			int number = -1;
			List l;
			result = new HashMap<Integer, Object>(1);
			Timestamp start, end;
			start = ref.getStartTime();
			end = ref.getEndTime();
			if (start == null || end == null) {
				l = os.getImagesPeriodIObject(start, end, userID);
				if (l != null) number = l.size();
				result.put(index, number);
			} else {
				l = os.getImagesAllPeriodCount(start, end, userID);
				result.put(index, l);
			}
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.print("Cannot count the number of items imported during the " +
					"specified period");
			msg.print(e);
			context.getLogger().error(this, msg);
		}
	}
	
	/**
	 * Adds a {@link BatchCall} to the tree for each container.
	 * The batch call simply invokes {@link #loadThumbail(int)}.
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree()
	{
		String description;
		Iterator i = nodes.keySet().iterator();
		while (i.hasNext()) {
			final Integer index = (Integer) i.next();
			description = "Count items";
			final TimeRefObject ref = nodes.get(index);
			add(new BatchCall(description) {
				public void doCall() { countItems(index, ref); }
			});   
		}
	}

	/**
	 * Returns the lastly retrieved count.
	 * This will be packed by the framework into a feedback event and
	 * sent to the provided call observer, if any.
	 * 
	 * @return 	A Map whose key is the index of the time node and the value
	 * 			the number of items.
	 */
	protected Object getPartialResult() { return result; }

	/**
	 * Returns <code>null</code> as there's no final result.
	 * In fact, thumbnails are progressively delivered with 
	 * feedback events. 
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return null; }

	/**
	 * Creates a new instance.
	 * 
	 * @param userID	The id of the user the count is for.
	 * @param m		The elements to handle.
	 */
	public ExperimenterImagesCounter(long userID, Map<Integer, TimeRefObject> m)
	{
		if (m == null || m.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		this.userID = userID;
		nodes = m;
		os = context.getDataService();
	}
 
}
