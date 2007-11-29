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
import java.sql.Timestamp;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.ExperimenterData;

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
     * Creates a {@link BatchCall} to retrieve the data
     * 
     * @param scope		The scope of the search.
     * @param values	The terms to search for.
     * @param users		The users' name.
     * @param start		The start of a time interval.
     * @param end		The end of a time interval.
     * @return The {@link BatchCall}.
     */
    private BatchCall searchFor(final List<Class> scope, 
    							final List<String> values, 
    							final List<ExperimenterData> users, 
    							final Timestamp start,
    							final Timestamp end)
    {
        return new BatchCall("Retrieving objects") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.advancedSearchFor(scope, values, users, start, end);
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
     * @param type		The scope of the search.
     * @param expID		The id of the user.
     * @param values	The terms to search for.
     */
    public ObjectFinder(Class type, long expID, List values)
    {
    	this.expID = expID;
    	loadCall = searchFor(type, values);
    }

    /**
     * Creates a new instance.
     * 
     * @param scope		The scope of the search.
     * @param values	The terms to search for.
     * @param users		The users' data.
     * @param start		The start of a time interval.
     * @param end		The end of a time interval.
     */
    public ObjectFinder(List<Class> scope, List<String> values, 
    					List<ExperimenterData> users, Timestamp start, 
    					Timestamp end)
    {
    	loadCall = searchFor(scope, values, users, start, end);
    }
    
}
