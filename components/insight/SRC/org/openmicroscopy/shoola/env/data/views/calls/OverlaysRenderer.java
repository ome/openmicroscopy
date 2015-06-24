/*
 * org.openmicroscopy.shoola.env.data.views.calls.OverlaysRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Renders the image with out without the overlays.
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
public class OverlaysRenderer
	extends BatchCallTree
{

	/** The result of the call. */
	private Object 		result;
	
	/** Loads the specified tree. */
    private BatchCall	loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param pd        The plane to render. 
     * @param tableID	The id of the table.
     * @param overlays	The overlays to render.
     * @return          The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final PlaneDef pd,
    		final long tableID, final Map<Long, Integer> overlays)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.renderOverLays(ctx, pixelsID, pd, tableID, overlays);
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
     * @param pixelsID The id of the pixels set.
     * @param pd The plane to render. 
     * @param tableID The id of the table.
     * @param overlays The overlays to render.
     * @param asTexture Pass <code>true</code> to return a texture,
	 * 					<code>false</code> to return a buffered image.
     */
    public OverlaysRenderer(SecurityContext ctx, long pixelsID, PlaneDef pd,
    	long tableID, Map<Long, Integer> overlays)
    {
    	if (pixelsID < 0)
    		throw new IllegalArgumentException("ID not valid.");
		this.ctx = ctx;
    	loadCall = makeBatchCall(pixelsID, pd, tableID, overlays);
    }
    
}
