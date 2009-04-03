/*
 * org.openmicroscopy.shoola.agents.treeviewer.TagLoader 
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
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
public class TagLoader 
	extends EditorLoader
{

	/** Indicates to retrieve the tags attached to the image. */
	public static final int TAGS_USED = 0;
	
	/** 
	 * Indicates to retrieve the tags available and not yet linked to 
	 * the image. 
	 */
	public static final int TAGS_AVAILABLE = 1;
	
	/** The id of the image to handle. */
	private long 		imageID;
	
	/** The id of the experimenter currently logged in. */
	private long		expID;
	
	/** One of the constants defined by this class. */
	private int			index;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The Editor this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param imageID  	The id of the image.
     * @param expID		The id of the experimenter currently logged in.
     * @param index		One of the constants defined by this class.
     */
	public TagLoader(Editor viewer, long imageID, long expID, int index)
	{
		super(viewer);
		this.imageID = imageID;
		this.expID = expID;
		this.index = index;
	}
	 
    /** 
     * Retrieves the emission wavelengths for the specified pixels set.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	Set<Long> images = new HashSet<Long>(1);
    	images.add(imageID);
    	switch (index) {
			case TAGS_USED:
				handle = dhView.loadLinkedTags(images, expID, this);
				break;
			case TAGS_AVAILABLE:
				handle = dhView.loadUnlinkedTags(images, expID, this);
				break;
		}
    }
    
    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Overridden so that we don't notify the user. 
     * @see EditorLoader#handleCancellation()
     */
    public void handleCancellation() {}
    
    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        if (viewer.getState() == Editor.DISCARDED) return;  //Async cancel.
        switch (index) {
			case TAGS_USED:
				List set = (List) result;
		        if (set == null || set.size() != 2)
		     	   viewer.setTags(null, null);
		        else viewer.setTags((List) set.get(0), (List) set.get(1));
				break;
			case TAGS_AVAILABLE:
				viewer.setAvailableTags((List) result);
        }
    }

}
