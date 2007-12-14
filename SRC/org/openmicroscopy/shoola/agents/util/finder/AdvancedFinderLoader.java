/*
 * org.openmicroscopy.shoola.agents.util.finder.AdvancedFinderLoader 
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
package org.openmicroscopy.shoola.agents.util.finder;

//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.ExperimenterData;

/** 
 * Searches for data
 * This class calls <code>advancedSearchFor</code> method in the
 * <code>DataHandlerView</code>.
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
public class AdvancedFinderLoader
	extends FinderLoader
{

	/** Collection of terms to search for. */
	private List<String>			values;
	
	/** Collection of terms to search for. */
	private List<ExperimenterData>	users;
	
	/** Collection of terms to search for. */
	private List<Class>				scope;
	
	/** The start of a time interval. */
	private Timestamp 				start;
	
	/** The end of a time interval. */
	private Timestamp 				end;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  			handle;

    /**
     * Converts all the elements from the passed collection.
     * 
     * @param context The collection to handle.
     */
    private void convertContext(List<Integer> context)
    {
    	scope = new ArrayList<Class>();
    	Iterator i = context.iterator();
    	Integer index;
    	while (i.hasNext()) {
    		index = (Integer) i.next();
    		scope.add(checkType(index));
		}
    }
    
	/**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param values	Collection of terms to search for.
     * @param users		Collection of users' data.
     * @param context	Collection of constants defined by this class.
     * @param start		The start of a time interval.
     * @param end		The end of a time interval.
     */
    public AdvancedFinderLoader(Finder viewer, List<String> values,
    							List<ExperimenterData> users,
    							List<Integer> context, Timestamp start,
    							Timestamp end)
    {
    	super(viewer);
    	if (values == null || values.size() == 0) 
    		throw new IllegalArgumentException("No terms to search for.");
    	if (context == null || context.size() == 0) 
    		throw new IllegalArgumentException("No scope defined.");
    	convertContext(context);
    	this.values = values;
    	this.users = users;
    	this.start = start;
    	this.end = end;
    }
    
    /**
     * Searches for values.
     * @see FinderLoader#load()
     */
    public void load()
    {
    	handle = dhView.advancedSearchFor(scope, values, users, start, end, 
    										this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see FinderLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see FinderLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (viewer.getState() == Finder.DISCARDED) return;  //Async cancel.
        EventBus bus = registry.getEventBus();
        Set set = (Set) result;
        if (set == null || set.size() == 0) {
        	
        	UserNotifier un = registry.getUserNotifier();
        	un.notifyInfo("Search", "No results matching your criteria.");
        	viewer.setStatus(false);
        	return;
        }
       
        Browse event = new Browse(set, Browse.IMAGES, getUserDetails(), null); 
        Iterator i = values.iterator();
        String s = " for \"";
        while (i.hasNext()) {
			s += (String) i.next();
			s += " ";
		}
        s = s.substring(0, s.length()-1);
        s += "\"";
        //if (CategoryData.class.equals(type)) s += "Tags";
       // else if (ImageData.class.equals(type)) s += "Images";
       // else if (AnnotationData.class.equals(type)) s += "Annotations";
        
        event.setSearchContext(s);
		bus.post(event); 
		viewer.dispose();
    }

}
