/*
 * org.openmicroscopy.shoola.env.data.views.calls.ProjectionSaver 
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

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to project an image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ProjectionSaver 
	extends BatchCallTree
{

	/** The id of the pixels set to handle. */
	private long 			pixelsID;
	
	/** Result of the call. */
	private Object result;

	/** Loads the specified tree. */
	private BatchCall loadCall;
    
	/** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to render the projected image
     * 
     * @param startZ   	The first optical section.
     * @param endZ     	The last optical section.
     * @param stepping 	Stepping used while projecting. 
     *                 	Default is <code>1</code>
     * @param algorithm	The type of projection.
     * @param channels  The collection of channels to project.
     * @return See above.
     */
    private BatchCall makeRenderProjectedCall(final int startZ, final int endZ, 
    		  final int stepping, final int algorithm,
    		  final List<Integer> channels)
    {
    	return new BatchCall("Preview the projected image.") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.renderProjected(ctx, pixelsID, startZ, 
            			endZ, stepping, algorithm, channels);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to render the projected image
     * 
     * @param ref The object hosting the projection's parameters.
     * @return See above.
     */
    private BatchCall makeProjectionCall(final ProjectionParam ref)
    {
    	return new BatchCall("Project the image") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.projectImage(ctx, ref);
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
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     * @param channels The collection of channels to project.
     */
    public ProjectionSaver(SecurityContext ctx, long pixelsID, int startZ,
    	int endZ, int stepping, int type, List<Integer> channels)
    {
    	if (pixelsID < 0)
    		throw new IllegalArgumentException("Pixels Id not valid.");
    	this.ctx = ctx;
    	this.pixelsID = pixelsID;
    	loadCall = makeRenderProjectedCall(startZ, endZ, stepping, type,
    			channels);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param ref The object hosting the projection's parameters.
     */
    public ProjectionSaver(SecurityContext ctx, ProjectionParam ref)
    {
    	this.ctx = ctx;
    	loadCall = makeProjectionCall(ref);
    }
    
}
