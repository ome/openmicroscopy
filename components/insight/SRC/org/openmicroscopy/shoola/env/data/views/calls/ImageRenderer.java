/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Renders an image.s
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImageRenderer
    extends BatchCallTree
{

    /** The rendered image. */
    private Object result;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @param pixelsID  The id of the pixels set the plane belongs to.
     * @param pd        The plane to render.
     * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param largeImae Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
     * @return          The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final PlaneDef pd, 
    		final boolean asTexture, final boolean largeImage)
    {
        return new BatchCall("rendering image: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.renderImage(ctx, pixelsID, pd, asTexture,
                		largeImage);
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
     * Returns the {@link BufferedImage rendered image}.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param pixelsID  The id of the pixels set the plane belongs to.
     * @param pd        The plane to render.
     * @param asTexture	Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
	 * @param largeImae Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
     */
    public ImageRenderer(SecurityContext ctx, long pixelsID, PlaneDef pd,
    	boolean asTexture, boolean largeImage)
    {
    	this.ctx = ctx;
        if (pixelsID < 0)
            throw new IllegalArgumentException("ID not valid.");
       loadCall = makeBatchCall(pixelsID, pd, asTexture, largeImage);
    }
    
}
