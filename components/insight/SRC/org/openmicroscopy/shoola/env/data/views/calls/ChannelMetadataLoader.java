/*
 * org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.ChannelData;

/** 
 * Command to retrieve the channels medatada.
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
public class ChannelMetadataLoader
    extends BatchCallTree
{

	/** The result of the query. */
    private Object      results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the channels metadata.
     * 
     * @param ctx The security context.
     * @param pixelsID The ID of the pixels set.
     * @param userID   If the id is specified i.e. not <code>-1</code>, 
     * 				   load the color associated to the channel, 
     * 				   <code>false</code> otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
    		final long pixelsID, final long userID) 
    {
        return new BatchCall("Loading channel Metadata: ") {
            public void doCall() throws Exception
            {
                OmeroMetadataService os = context.getMetadataService();
                List l = os.getChannelsMetadata(ctx, pixelsID);
                if (userID >= 0) { //load the rendering settings.
                	OmeroImageService svc = context.getImageService();
                	List rnd = svc.getRenderingSettingsFor(ctx,
                			pixelsID, userID);
                	Map channels = new HashMap();
                	Iterator i = l.iterator();
                	if (rnd != null && rnd.size() > 0) {
                		RndProxyDef ref = (RndProxyDef) rnd.get(0);
                		ChannelData channel;
                		while (i.hasNext()) {
                			channel = (ChannelData) i.next();
                			channels.put(channel, 
                					ref.getChannelColor(channel.getIndex()));
						}
                	} else {
                		while (i.hasNext())
                			channels.put(i.next(), null);
                	}
                	
                	results = channels;
                } else results = l;
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
     * Loads the channel metadata linked to pixels set to render.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
	 * @param ctx The security context.
     * @param pixelsID The Id of the pixels set.
     * @param userID If the id is specified i.e. not <code>-1</code>, 
     *               load the color associated to the channel, 
     *               <code>false</code> otherwise.
     */
    public ChannelMetadataLoader(SecurityContext ctx, long pixelsID,
    		long userID)
    {
    	if (pixelsID < 0)
    		 throw new IllegalArgumentException("Pixels ID not valid.");
        loadCall = makeBatchCall(ctx, pixelsID, userID);
    }
    
}
