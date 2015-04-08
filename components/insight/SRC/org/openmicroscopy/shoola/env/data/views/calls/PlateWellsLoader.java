/*
 * org.openmicroscopy.shoola.env.data.views.calls.PlateWellsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import omero.gateway.SecurityContext;
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
    private Object result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
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
            	Entry<Long, Long> entry;
            	Iterator<Entry<Long, Long>> i = ids.entrySet().iterator();
            	long key, value;
            	while (i.hasNext()) {
					entry = i.next();
					key = entry.getKey();
					value = entry.getValue();
					r.put(key, os.loadPlateWells(ctx, key, value, userID));
				}
            	result = r;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the plate(s) hosting the
     * specified images.
     * 
     * @param ids The identifiers of the images linked to the plate.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadPlateFromImage(final Collection<Long> ids)
    {
        return new BatchCall("Loading Plate From Image") {
            public void doCall() throws Exception
            {
            	OmeroDataService os = context.getDataService();
            	result = os.loadPlateFromImage(ctx, ids);
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
     * @param ctx The security context.
     * @param ids 	Map whose keys are the plate ID and values are the 
     * 				screen acquisition ID or <code>-1</code>.  
     * @param userID  	The id of the user.
     */
    public PlateWellsLoader(SecurityContext ctx, Map<Long, Long> ids,
    		long userID)
    {
    	this.ctx = ctx;
    	loadCall = loadPlateWells(ids, userID);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param imageIDs The identifiers of the images linked to the plate.
     */
    public PlateWellsLoader(SecurityContext ctx, Collection<Long> imageIDs)
    {
    	this.ctx = ctx;
    	loadCall = loadPlateFromImage(imageIDs);
    }
    
}
