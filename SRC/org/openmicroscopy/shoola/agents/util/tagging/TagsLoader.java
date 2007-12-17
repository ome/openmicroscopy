/*
 * org.openmicroscopy.shoola.agents.util.tagging.TagsLoader 
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
package org.openmicroscopy.shoola.agents.util.tagging;


//Java imports
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.tagging.view.Tagger;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

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
public class TagsLoader
	extends TaggerLoader
{

	/** The id of the objects to handle. */
	private Set<Long>			ids;
	
	/** The id of the experimenter. */
	private long				expID;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The viewer this loader is for. 
	 * 					Mustn't be <code>null</code>.
	 * @param ids		The id of the objects.
	 * @param expID		The id of the experimenter.
	 */
	public TagsLoader(Tagger viewer, Set<Long>	ids, long expID)
	{
		super(viewer);
		this.ids = ids;
		this.expID = expID;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The viewer this loader is for. 
	 * 					Mustn't be <code>null</code>.
	 * @param expID		The id of the experimenter.
	 */
	public TagsLoader(Tagger viewer, long expID)
	{
		super(viewer);
		this.ids = null;
		this.expID = expID;
	}
	
	/**
	 * Retrieves the tags for the 
	 * @see TaggerLoader#load()
	 */
	public void load()
	{
		handle = dhView.loadAllTags(ids, expID, this);
	}
	
	/**
	 * Cancels the ongoing data retrieval.
	 * @see TaggerLoader#cancel()
	 */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer. 
     * @see TaggerLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
       if (viewer.getState() == Tagger.DISCARDED) return;  //Async cancel.
       List set = (List) result;
       if (set == null || set.size() != 3)
    	   viewer.setTags(null, null, null);
       else viewer.setTags((List) set.get(0), (List) set.get(1), 
    		   						(List) set.get(2));
    }
    
}
