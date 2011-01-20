/*
 * org.openmicroscopy.shoola.env.data.views.calls.PlateWellsLoader 
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
package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Loads the wells contained within a plate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class PlateWellsLoader 	
	extends BatchCallTree
{

    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the wells-wellsample-image.
     * 
     * @param ids  Map whose keys are the plate ID and values are the 
     * 				screen acquisition ID or <code>-1</code>.
     * @param userID   The id of the user who tagged the object or 
     * 				   <code>-1</code> if the user is not specified.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadPlateWells(final Map<Long, Long> ids, 
    		final long userID)
    {
    	return new BatchCall("Loading Plate Wells") {
            public void doCall() throws Exception
            {
            	OmeroDataService os = context.getDataService();
            	Map<Long, Collection> r = new HashMap<Long, Collection>();
            	Entry entry;
            	Iterator i = ids.entrySet().iterator();
            	long key, value;
            	while (i.hasNext()) {
					entry = (Entry) i.next();
					key = (Long) entry.getKey();
					value = (Long) entry.getValue();
					r.put(key, os.loadPlateWells(key, value, userID));
				}
            	/*
            	Iterator<Long> i = plateIDs.iterator();
            	long id;
            	Map<Long, Collection> r = new HashMap<Long, Collection>();
            	while (i.hasNext()) {
					id = i.next();
					r.put(id, os.loadPlateWells(id, userID));
				}
				*/
            	result = r;
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param ids 	Map whose keys are the plate ID and values are the 
     * 				screen acquisition ID or <code>-1</code>.  
     * @param userID  	The id of the user.
     */
    public PlateWellsLoader(Map<Long, Long> ids, long userID)
    {
    	loadCall = loadPlateWells(ids, userID);
    }
    
}
