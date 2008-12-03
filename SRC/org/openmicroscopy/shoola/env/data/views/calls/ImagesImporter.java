/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImagesImporter 
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 * Command to import images in a container if specified.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImagesImporter 
	extends BatchCallTree
{

    /** The results of the call. */
    private Object 		results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a a {@link BatchCall} to import the images.
     * 
     * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param images	The images to import. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final DataObject container, 
    						final List<Object> images, final long userID, 
    						final long groupID)
    {
        return new BatchCall("Importing images: ") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
				results = os.importImages(container, images, userID, groupID);
            }
        };
    }
    
	 /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
     * @param container The container where to import the images into or 
	 * 					<code>null</code>.
	 * @param images	The images to import. Mustn't be <code>null</code>.
	 * @param userID	The id of the user.
	 * @param groupID	The id of the group.
     */
    public ImagesImporter(DataObject container, List<Object> images, 
    					long userID, long groupID)
    {
    	if (images == null || images.size() == 0)
    		throw new IllegalArgumentException("No images to import.");
    	loadCall = makeBatchCall(container, images, userID, groupID);
    }
    
}
