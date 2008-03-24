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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.util.SearchResult;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
	private SearchDataContext		searchContext;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  			handle;

	/**
     * Creates a new instance.
     * 
     * @param viewer 		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param values		Collection of terms to search for.
     * @param users			Collection of users' data.
     * @param context		Collection of constants defined by this class.
     * @param start			The start of a time interval.
     * @param end			The end of a time interval.
     * @param separator		The term used between the terms to search.
     * @param caseSensitive	Pass <code>true</code> to be case sensitive,
     * 						<code>false</code> otherwise.
     */
    public AdvancedFinderLoader(Finder viewer, SearchDataContext context)
    {
    	super(viewer);
    	if (context == null) 
    		throw new IllegalArgumentException("No scope defined.");
    	searchContext = context;
    }
    
    /**
     * Searches for values.
     * @see FinderLoader#load()
     */
    public void load()
    {
    	handle = dhView.advancedSearchFor(searchContext, this);
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
        SearchResult r = (SearchResult) result;
        if (r == null) {
        	UserNotifier un = registry.getUserNotifier();
        	un.notifyInfo("Search", "No results matching your criteria.");
        	viewer.setStatus("", false);
        	return;
        }
        Set<Long> set = r.getNodeIDs();
        if (set == null || set.size() == 0) {
        	UserNotifier un = registry.getUserNotifier();
        	un.notifyInfo("Search", "No results matching your criteria.");
        	viewer.setStatus("", false);
        	return;
        }
        int size = set.size();
        if (size > Finder.MAX_RESULTS) {
        	viewer.setStatus("Found: "+size+" results. " +
        					"Please refine your search", false);
        	return;
        }
        Browse event = new Browse(set, Browse.IMAGES, getUserDetails(), null); 
        /*
        Iterator i = values.iterator();
        String s = " for \"";
        while (i.hasNext()) {
			s += (String) i.next();
			s += " ";
		}
        s = s.substring(0, s.length()-1);
        s += "\" in ";
        i = scope.iterator();
        
        int index = 0;
        int l = scope.size()-1;
        while (i.hasNext()) {
        	s += convertType((Class) i.next());
        	if (index != l) s +=", ";
        	index++;
		}
        */
        String s = "Results";
        //event.setSearchContext(s);
		bus.post(event); 
		viewer.dispose();
    }

}
