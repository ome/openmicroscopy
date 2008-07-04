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

//Third-party libraries

//Application-internal dependencies
import java.util.List;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.DatasetData;

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
 * @since 3.0-Beta3
 */
public class ProjectionSaver 
	extends BatchCallTree
{

	/** The id of the pixels set to handle. */
	private long 			pixelsID;
	
	/** Result of the call. */
	private Object    		result;

	/** Loads the specified tree. */
	private BatchCall		loadCall;
    
    /**
     * Creates a {@link BatchCall} to render the projected image
     * 
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     * @return See above.
     */
    private BatchCall makeRenderProjectedCall(final int startZ, final int endZ, 
    		  final int stepping, final int type)
    {
    	return new BatchCall("Loading pixels dimensions: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.renderProjected(pixelsID, startZ, endZ, stepping, 
                		                    type);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to render the projected image
     * 
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     * @param channels The channels to project.
     * @param datasets The datasets to add the projected image to.
     * @param name     The name of the projected image.
     * @return See above.
     */
    private BatchCall makeProjectionCall(final int startZ, final int endZ, 
    		  final int stepping, final int type, final List<Integer> channels, 
      		  final List<DatasetData> datasets, final String name)
    {
    	return new BatchCall("Loading pixels dimensions: ") {
            public void doCall() throws Exception
            {
                OmeroImageService rds = context.getImageService();
                result = rds.projectImage(pixelsID, startZ, endZ, stepping, 
                		                type, channels, datasets, name);
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
     * @param pixelsID The id of the pixels set.
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     */
    public ProjectionSaver(long pixelsID, int startZ, int endZ, int stepping, 
    		              int type)
    {
    	if (pixelsID < 0)
    		throw new IllegalArgumentException("Pixels Id not valid.");
    	this.pixelsID = pixelsID;
    	loadCall = makeRenderProjectedCall(startZ, endZ, stepping, type);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param pixelsID The id of the pixels set.
     * @param startZ   The first optical section.
     * @param endZ     The last optical section.
     * @param stepping Stepping used while projecting. 
     *                 Default is <code>1</code>
     * @param type     The type of projection.
     * @param channels The channels to project.
     * @param datasets The datasets to add the projected image to.
     * @param name     The name of the projected image.
     */
    public ProjectionSaver(long pixelsID, int startZ, int endZ, int stepping, 
    		              int type, List<Integer> channels, 
    		      		  List<DatasetData> datasets, String name)
    {
    	if (pixelsID < 0)
    		throw new IllegalArgumentException("Pixels Id not valid.");
    	this.pixelsID = pixelsID;
    	loadCall = makeProjectionCall(startZ, endZ, stepping, type, channels,
    			                     datasets, name);
    }
    
}
