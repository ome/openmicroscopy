/*
 * org.openmicroscopy.shoola.agents.util.finder.Finder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import omero.gateway.SecurityContext;

/** 
 * Interface that every finder should implement
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
public interface Finder
{

	/** 
	 * Bound property indicating that some results matching 
	 * the passed criteria have been found.
	 */
	public static final String RESULTS_FOUND_PROPERTY = "resultsFound";
	
	/** Identified the <code>DISCARD</code> state. */
	public static final int DISCARDED = 100;
	
	/** Identified the <code>SEARCH</code> state. */
	public static final int SEARCH = 101;
	
	/** Cancels any ongoing search. */
	public void cancel();
	
	/** 
	 * Returns the state.
	 * 
	 * @return See above.
	 */
	public int getState();
	
	/** Disposes of the finder when the results are found. */
	public void dispose();
	
	/**
	 * Sets the status.
	 * 
	 * @param text		The text to display.
	 * @param status 	Pass <code>true</code> to hide the progress bar,
	 * 					<code>false</code> otherwise.
	 */
	public void setStatus(String text, boolean status);

	/**
	 * Sets the results of the search.
	 * 
	 * @param ctx The security context.
	 * @param result The value to set.
	 */
	public void setResult(AdvancedSearchResultCollection result);

	/** 
	 * Sets the collection of tags. 
	 * 
	 * @param tags The collection of tags to set.
	 */
	public void setExistingTags(Collection tags);
	
}
