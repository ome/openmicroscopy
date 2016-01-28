/*
 * org.openmicroscopy.shoola.env.data.views.calls.EnumerationLoader 
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to retrieve existing the enumerations related to the image
 * acquisition metadata, and to the channel metadata.
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
public class EnumerationLoader 
	extends BatchCallTree
{

	/** Indicates to load the enumeration related to the image metadata. */
	public static final int IMAGE = 0;
	
	/** Indicates to load the enumeration related to the channel metadata. */
	public static final int CHANNEL = 1;
	
	/** The nodes of the existing objects. */
    private Map         results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;

    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to enumerations related to the image
     * metadata.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCallForImage()
    {
        return new BatchCall("Loading image metadata enumeration: ") {
            public void doCall() throws Exception
            {
                OmeroMetadataService service = context.getMetadataService();
                results.put(OmeroMetadataService.IMMERSION, 
                		service.getEnumeration(ctx, 
                				OmeroMetadataService.IMMERSION));
                results.put(OmeroMetadataService.CORRECTION, 
                		service.getEnumeration(ctx, 
                				OmeroMetadataService.CORRECTION));
                results.put(OmeroMetadataService.MEDIUM, 
                		service.getEnumeration(ctx,
                				OmeroMetadataService.MEDIUM));
                results.put(OmeroMetadataService.FORMAT, 
                		service.getEnumeration(ctx,
                				OmeroMetadataService.FORMAT));
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to enumerations related to the channel
     * metadata.
     * 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCallForChannel()
    {
        return new BatchCall("Loading channel metadata enumeration: ") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService service = context.getMetadataService();
            	results.put(OmeroMetadataService.ILLUMINATION_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.ILLUMINATION_TYPE));
            	results.put(OmeroMetadataService.CONTRAST_METHOD, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.CONTRAST_METHOD));
            	results.put(OmeroMetadataService.ACQUISITION_MODE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.ACQUISITION_MODE));
            	results.put(OmeroMetadataService.BINNING, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.BINNING));
            	results.put(OmeroMetadataService.DETECTOR_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.DETECTOR_TYPE));
            	results.put(OmeroMetadataService.LASER_MEDIUM, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.LASER_MEDIUM));
            	results.put(OmeroMetadataService.LASER_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.LASER_TYPE));
            	results.put(OmeroMetadataService.LASER_PULSE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.LASER_PULSE));
            	results.put(OmeroMetadataService.ARC_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.ARC_TYPE));
            	results.put(OmeroMetadataService.FILAMENT_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.FILAMENT_TYPE));
            	results.put(OmeroMetadataService.FILTER_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.FILTER_TYPE));
            	results.put(OmeroMetadataService.MICROSCOPE_TYPE, 
            			service.getEnumeration(ctx,
            					OmeroMetadataService.MICROSCOPE_TYPE));
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
     * Returns the found objects.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
     * 
     * @param ctx The security context
     * @param index One of the constants defined by this class.
     */
    public EnumerationLoader(SecurityContext ctx, int index)
    {
    	results = new HashMap<String, Collection>();
    	this.ctx = ctx;
    	if (index == IMAGE) loadCall = makeBatchCallForImage();
    	else loadCall = makeBatchCallForChannel();
    }
    
}
