/*
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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.OmeroDataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;


/** 
 * Command to retrieve the images contained in the specified containers.
 * The containers can either be <code>Dataset</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2al version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImagesLoader
    extends BatchCallTree
{
	
    /** The results of the call. */
    private Object results;
    
    /** Loads the specified tree. */
    private BatchCall loadCall;

    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @param userID The ID of the user.
     * @param orphan Pass <code>true</code> to load images not in a container,
     *  <code>false</code> otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long userID, final boolean orphan)
    {
        return new BatchCall("Loading user's images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.getExperimenterImages(ctx, userID, orphan);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the passed image
     * 
     * @param imageID The id of the image.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long imageID)
    {
        return new BatchCall("Loading user's images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                Set set = os.getImages(ctx, ImageData.class,
                		Arrays.asList(imageID), -1);
                if (set != null && set.size() == 1) {
                	Iterator i = set.iterator();
                	while (i.hasNext()) {
						results = i.next();
						break;
					}
                }
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images contained 
     * in the container specified by the set of IDs.
     * 
     * @param nodeType  	The type of the node.
     * @param nodeIDs   	A set of the IDs of top-most containers.
     * @param userID		The Id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImagesInContainerBatchCall(final Class nodeType,
                                        			final List nodeIDs,
                                        			final long userID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.getImages(ctx, nodeType, nodeIDs, userID);
            }
        };
    }
    
    /**
     * Creates a a {@link BatchCall} to retrieve images before or after
     * a given date depending on the passed parameter.
     * 
     * @param startTime The timestamp identifying the lower bound.
     * @param endTime	The timestamp identifying the date.
     * @param userID	The Id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Timestamp startTime, 
    						final Timestamp endTime, final long userID)
    {
        return new BatchCall("Loading images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
				results =  os.getImagesPeriod(ctx, startTime, endTime, userID,
						true);
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
     * Creates a new instance. 
     * 
     * @param ctx The security context.
     * @param userID The ID of the user or <code>-1</code> to load all
     *               the images within the group.
     * @param orphan Indicates to load all the images or
     * only the orphaned images.
     */
    public ImagesLoader(SecurityContext ctx, long userID, boolean orphan)
    {
    	this.ctx = ctx;
        loadCall = makeBatchCall(userID, orphan);
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
     * 
     * @param ctx The security context.
     * @param nodeType	The type of the root node. Can only be one out of:
     * 					{@link DatasetData} or {@link ImageData}.
     * @param nodeIDs	A set of the IDs of top-most containers.
     * @param userID	The Id of the user or <code>-1</code> to load all
     *               the images within the group.
     */
    public ImagesLoader(SecurityContext ctx, Class nodeType, List nodeIDs,
    		long userID)
    {
        if (nodeType == null) 
            throw new IllegalArgumentException("No node type.");
        if (nodeIDs == null || nodeIDs.size() == 0)
            throw new IllegalArgumentException("Collection of node ID" +
                                                " not valid.");
        this.ctx = ctx;
        if (nodeType.equals(DatasetData.class) || 
            nodeType.equals(ImageData.class))
            loadCall = makeImagesInContainerBatchCall(nodeType, nodeIDs,
                    									userID);
        else throw new IllegalArgumentException("Unsupported type: "+
                nodeType);
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
	 * @param ctx The security context.
     * @param startTime The timestamp identifying the start of a period.
     * @param endTime The timestamp identifying the date.
     * @param userID The Id of the user or <code>-1</code> to load all
     *               the images within the group.
     */
    public ImagesLoader(SecurityContext ctx, Timestamp startTime,
    		Timestamp endTime, long userID)
    {
    	this.ctx = ctx;
    	loadCall = makeBatchCall(startTime, endTime, userID);
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param ctx The security context.
     * @param imageID The id of the image.
     */
    public ImagesLoader(SecurityContext ctx,long imageID)
    {
    	this.ctx = ctx;
        loadCall = makeBatchCall(imageID);
    }

}
