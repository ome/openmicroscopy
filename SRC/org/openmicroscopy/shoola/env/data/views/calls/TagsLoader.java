/*
 * org.openmicroscopy.shoola.env.data.views.calls.TagsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies
import java.util.Set;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

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
	extends BatchCallTree
{
	
	/** Indicates to retrieves the tags. */
	public static final int LEVEL_TAG = 0;
	
	/** Indicates to retrieves the tag sets. */
	public static final int LEVEL_TAG_SET = 1;

	/** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the tagSets owned by the passed
     * user.
     * 
     * @param id
     * @param images	Pass <code>true</code> to load the images related 
     * 					to the tags, <code>false</code> otherwise.
     * @param userID	The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadTagsCall(final Long id, final boolean images, 
    								final long userID)
    {
        return new BatchCall("Loading tags.") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	result = os.loadTagsContainer(id, images, userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the tagSets owned by the passed
     * user.
     * 
     * @param id
     * @param images	Pass <code>true</code> to load the images related 
     * 					to the tags, <code>false</code> otherwise.
     * @param userID	The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadTagSetsCall(final Long id, final boolean images,
    								final long userID)
    {
        return new BatchCall("Loading tagSets.") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	result = os.loadTagSetsContainer(id, images, userID);
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
     * @param id
     * @param index 	One of the constants defined by this class.
     * @param images	Pass <code>true</code> to load the images related 
     * 					to the tags, <code>false</code> otherwise.
     * @param userID	The id of the user who owns the tags or tag sets.
     */
	public TagsLoader(int index, Long id, boolean images , long userID)
	{
		switch (index) {
			case LEVEL_TAG:
				loadCall = loadTagsCall(id, images, userID);
				break;
			case LEVEL_TAG_SET:
				loadCall = loadTagSetsCall(id, images, userID);
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
}
