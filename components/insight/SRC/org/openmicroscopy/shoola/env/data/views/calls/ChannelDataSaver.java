/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;

/**
 * Command to save the channels.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ChannelDataSaver
	extends BatchCallTree
{

	/** The result of the query. */
    private Object      results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to save the channels
     * 
     * @param ctx The security context.
     * @param channels The channels to handle.
     * @param objects The objects to apply the changes to. If the objects are
	 * datasets, then all the images within the datasets will be updated.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final List<ChannelData> channels, final List<DataObject> objects)
    {
        return new BatchCall("Saving channel: ") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                results = os.saveChannelData(ctx, channels, objects);
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
    protected Object getResult() { return results; }

    /**
     * Saves the channels. Applies the changes to all the images contained in
	 * the specified objects. This could be datasets, plates or images.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param ctx The security context.
     * @param channels The channels to handle.
     * @param objects The objects to apply the changes to. If the objects are
	 * datasets, then all the images within the datasets will be updated.
     */
    public ChannelDataSaver(SecurityContext ctx, 
    		List<ChannelData> channels, List<DataObject> objects)
    {
    	if (CollectionUtils.isEmpty(channels))
    		 throw new IllegalArgumentException("No Channels specified.");
        loadCall = makeBatchCall(ctx, channels, objects);
    }

}
