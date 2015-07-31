/*
 * org.openmicroscopy.shoola.env.data.views.calls.PixelsDataLoader 
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;


/** 
 * Command to retrieve data related to the specified set of pixels.
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
public class PixelsDataLoader
	extends BatchCallTree
{
	
	/** Flag indicating to load the pixels set. */
	public static final int SET = 1;
	
	/** Flag indicating to load the pixels' size. */
	public static final int SIZE = 2;
	
	
	 /** The id of the pixels set this loader is for. */
    private long pixelsID;
    
    /** Result of the call. */
    private Object result;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;

    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to load the pixels set.
     * 
     * @return See above.
     */
    private BatchCall makePixelsBatchCall()
    {
    	return new BatchCall("Loading pixels: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.loadPixels(ctx, pixelsID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to check if the pixels is a big image.
     * 
     * @return See above.
     */
    private BatchCall makePixelsSizeBatchCall()
    {
    	return new BatchCall("Loading pixels: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.isLargeImage(ctx, pixelsID);
            }
        };
    }
    
    /**
     * Adds a {@link BatchCall} to the tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }
    
    /**
     * Returns the result.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates the call corresponding to the passed index.
     * 
     * @param ctx The security context.
     * @param pixelsID	The id of the pixels set.
     * @param index		One of the constants defined by this class.
     */
    public PixelsDataLoader(SecurityContext ctx, long pixelsID, int index)
    {
    	this.pixelsID = pixelsID;
    	this.ctx = ctx;
    	switch (index) {
			case SET:
				loadCall = makePixelsBatchCall();
				break;
			case SIZE:
				loadCall = makePixelsSizeBatchCall();
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
}
