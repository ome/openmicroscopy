/*
 * org.openmicroscopy.shoola.agents.util.tagging.TagsSaver 
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.tagging.view.Tagger;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import pojos.CategoryData;

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
public class TagsSaver 
	extends TaggerLoader
{

	/** Indicates to tag the selected objects. */
	public static final int 	LEVEL_ZERO = 0;//DataHandlerView.TAG_LEVEL_ZERO;
	
	/** Indicates to tag the children of the selected objects. */
	public static final int 	LEVEL_ONE = 1;//DataHandlerView.TAG_LEVEL_ONE;
	
	/** The id of the objects to handle. */
	private Set<Long> 			ids;
	
	/** The time object hosting the time interval. */
	private TimeRefObject 		timeRef;
	
	/** Identifies the type of objects to tag. */
	private Class				rootType;
	
	/** 
     * Identifies the tagging level, One of the constants defined by this class.
     */
	private int					tagLevel;
	
	/** 
	 * Collection containing the categories to create or to update depending 
	 * on the index. 
	 */
	private Set<CategoryData> 	data;
	
	/** Collection containing the categories to update.*/
	private Set<CategoryData> 	toUpdate;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
 
	/**
     * Creates a new instance to create a new category and 
     * add the passed image to the newly created category.
     * 
     * @param viewer		The view this loader is for.
     *                  	Mustn't be <code>null</code>.
     * @param ids			The id of the images.
     * @param rootType
     * @param data			Collection containing the categories to create.
     * @param toUpdate		Collection containing the categories to update.
     * @param tagLevel
     */
	public TagsSaver(Tagger viewer, Set<Long> ids, Class rootType, 
						Set<CategoryData> data, Set<CategoryData> toUpdate, 
						int tagLevel)
	{
		super(viewer);
		this.ids = ids;
		this.data = data;
		this.toUpdate = toUpdate;
		this.rootType = rootType;
		this.tagLevel = tagLevel;
	}
	
	/**
     * Creates a new instance to create a new category and 
     * add the passed image to the newly created category.
     * 
     * @param viewer		The view this loader is for.
     *                  	Mustn't be <code>null</code>.
     * @param timeRef		The time ref containing the data objects to tag.
     * @param rootType
     * @param data			Collection containing the categories to create.
     * @param toUpdate		Collection containing the categories to update.
     * @param tagLevel
     */
	public TagsSaver(Tagger viewer, TimeRefObject timeRef, Class rootType,
						Set<CategoryData> data, Set<CategoryData> toUpdate,
						int tagLevel)
	{
		super(viewer);
		this.timeRef = timeRef;
		this.tagLevel = tagLevel;
		this.data = data;
		this.toUpdate = toUpdate;
		this.rootType = rootType;
	}
	
	/**
	 * Saves the tags back to the server.
	 * @see TaggerLoader#load()
	 */
	public void load()
	{
		/*
		if (timeRef != null)
			handle = dhView.tag(timeRef, rootType, tagLevel, data, toUpdate, 
								this);
		else 
			handle = dhView.tag(ids, rootType, tagLevel, data, toUpdate, this);
			*/
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
       viewer.setImageTagged();
    }
    
}
