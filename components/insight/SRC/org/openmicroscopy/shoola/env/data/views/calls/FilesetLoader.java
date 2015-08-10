/*
 * org.openmicroscopy.shoola.env.data.views.calls.FilesetLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Loads the file set associated to an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class FilesetLoader
	extends BatchCallTree
{
	/** The result of the query. */
    private Object      result;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to load the file set for the 
     * specified image.
     * 
     * @param ctx The security context.
     * @param imageId The ID of the image.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final long imageId) 
    {
        return new BatchCall("Load the file set.") {
            public void doCall() throws Exception
            {
                OmeroImageService os = context.getImageService();
                result = os.getFileSet(ctx, imageId);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param imageId The Id of the image.
     */
	public FilesetLoader(SecurityContext ctx, long imageId)
	{
		if (imageId < 0)
			throw new IllegalArgumentException("Image's ID not valid.");
		loadCall = makeBatchCall(ctx, imageId);
	}

}
