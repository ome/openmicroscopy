/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

import java.awt.image.BufferedImage;

import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
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
     * @param pixelsID The id of the pixels set the plane belongs to.
     * @param pd The plane to render.
	 * @param largeImage Pass <code>true</code> to render a large image,
	 *                  <code>false</code> otherwise.
	 * @param compression The compression level.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final PlaneDef pd,
    		final boolean largeImage, final int compression)
    {
        return new BatchCall("rendering image: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.renderImage(ctx, pixelsID, pd,
                		largeImage, compression);
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
	 * @param largeImae Pass <code>true</code> to render a large image,
	 * 					<code>false</code> otherwise.
	 * @param compression The compression level.
     */
    public ImageRenderer(SecurityContext ctx, long pixelsID, PlaneDef pd,
    	boolean largeImage, int compression)
    {
    	this.ctx = ctx;
        if (pixelsID < 0)
            throw new IllegalArgumentException("ID not valid.");
       loadCall = makeBatchCall(pixelsID, pd, largeImage, compression);
    }
    
}
