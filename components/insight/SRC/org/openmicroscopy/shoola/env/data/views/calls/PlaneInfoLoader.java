/*
 * org.openmicroscopy.shoola.env.data.views.calls.PlaneInfoLoader 
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
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Loads the plane information related to a given pixels set.
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
public class PlaneInfoLoader 
	extends BatchCallTree
{

	/** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to load the plane information.
     * 
     * @param pixelsID  The id of the pixels set.
     * @param z 		The selected z-section or <code>-1</code>.
     * @param t 		The selected timepoint or <code>-1</code>.
     * @param channel 	The selected timepoint or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadPlaneInfo(final long pixelsID, final int z, 
    		final int t, final int channel)
    {
        return new BatchCall("Loading Plane Info for"+pixelsID) {
            public void doCall() throws Exception
            {
            	OmeroImageService os = context.getImageService();
            	result = os.loadPlaneInfo(ctx, pixelsID, z, t, channel);
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
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param z The selected z-section or <code>-1</code>.
     * @param t The selected timepoint or <code>-1</code>.
     * @param channel The selected channel or <code>-1</code>.
     */
    public PlaneInfoLoader(SecurityContext ctx, long pixelsID, int z, int t,
    		int channel)
    {
    	this.ctx = ctx;
    	loadCall = loadPlaneInfo(pixelsID, z, t, channel);
    }
    
}
