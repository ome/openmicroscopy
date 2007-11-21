/*
 * org.openmicroscopy.shoola.agents.imviewer.CategoryLoader 
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
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * Retrieves the categories the image is categorised into.
 * This class calls <code>loadAllClassifications</code> method in the
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
public class CategoryLoader
	extends DataLoader
{

	/** Indicates to load categories to work with them. */
	public static final int LOAD = 0;
	
	/** Indicates to retrieve the categories with the specified tags. */
	public static final int SEARCH = 1;
	
	/** The id of the image to handle. */
	private long 		imageID;
	
	/** The id of the experimenter. */
	private long		expID;
	
	/** One of the constants defined by this class.  */
	private int			index;
	
	/** Collection of terms to search for. */
	private List		values;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The view this loader is for.
     *                  Mustn't be <code>null</code>.
     * @param imageID	The id of the image.
     * @param expID		The id of the experimenter.
     * @param index		One of the constants defined by this class. 
     */
    public CategoryLoader(ImViewer viewer, long imageID, long expID)
    {
    	super(viewer);
    	this.imageID = imageID;
    	this.expID = expID;
    	this.index = LOAD;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The view this loader is for.
     *                  Mustn't be <code>null</code>.
     * @param expID		The id of the experimenter.
     * @param index		One of the constants defined by this class. 
     * @param values	Collection of terms to search for.
     */
    public CategoryLoader(ImViewer viewer, long expID, List values)
    {
    	super(viewer);
    	if (values == null || values.size() == 0) return;
    	this.expID = expID;
    	this.index = SEARCH;
    	this.values = values;
    }
    
    /**
     * Retrieves the categories the images is categorised into.
     * @see DataLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case LOAD:
				handle = dhView.loadAllClassifications(imageID, expID, this);
				break;
			case SEARCH:
				handle = dhView.searchForClassifications(expID, values, this);
		}
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
       if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
       switch (index) {
			case LOAD:
				 List set = (List) result;
			       if (set == null || set.size() != 3)
			    	   viewer.setClassification(null, null, null);
			       else viewer.setClassification((List) set.get(0),
			    		   						(List) set.get(1), 
			    		   						(List) set.get(2));
				break;
			case SEARCH:
				viewer.browse((Set) result);
				break;
       }
    }
    
}
