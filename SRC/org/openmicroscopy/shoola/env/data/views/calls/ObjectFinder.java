/*
 * org.openmicroscopy.shoola.env.data.views.calls.ObjectFinder 
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Searches for objects.
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
public class ObjectFinder 
	extends BatchCallTree
{

	 /** The root nodes of the found trees. */
    private Object		result;
    
    /** The search call. */
    private BatchCall   loadCall;

	/** The id of the experimenter. */
	private long		expID;
	
	/**
     * Creates a {@link BatchCall} to retrieve the categories whose name
     * contains the passed values.
     * 
     * @param type	 The type of objects to search for.
     * @param values The values to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall searchFor(final Class type, final List values)
    {
        return new BatchCall("Retrieving objects") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.searchFor(type, expID, values);
            }
        };
    }
	
	/**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the result of the search.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param expID	
     * @param type
     * @param values
     */
    public ObjectFinder(Class type, long expID, List values)
    {
    	this.expID = expID;
    	loadCall = searchFor(type, values);
    }

}
