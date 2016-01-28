/*
 * org.openmicroscopy.shoola.env.data.views.calls.FilesLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.util.Target;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/**
 * Exports the image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ExportLoader 
	extends BatchCallTree
{

	/** Indicates to export the image as OME TIFF. */
	public static final int	EXPORT_AS_OMETIFF = 
		OmeroImageService.EXPORT_AS_OMETIFF;
	
	/** Indicates to export the image as OME XML. */
	public static final int	EXPORT_AS_OME_XML = 
		OmeroImageService.EXPORT_AS_OME_XML;
	
	/** Loads the specified annotations. */
    private BatchCall loadCall;
    
    /** The result of the call. */
    private Object result;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to export the image as XML.
     * 
	 * @param file    The file where to export the image.
	 * @param imageID The id of the image to export.
	 * @param target The selected schema.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeAsOMETiffBatchCall(final int index, final File file, 
    						final long imageID, final Target target)
    {
        return new BatchCall("Export image as OME-TIFF or OME-XML.") {
            public void doCall() throws Exception
            {
                OmeroImageService service = context.getImageService();
                result = service.exportImageAsOMEFormat(ctx, index, imageID,
                		file, target);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the collection of archives files.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
	 * @param imageID The id of the image to export.
	 * @param file    The file where to store the exported file.
	 * @param index	  One of the constants defined by this class.
	 * @param target The selected schema.
     */
    public ExportLoader(SecurityContext ctx, long imageID, File file, int index,
    		Target target)
    {
    	this.ctx = ctx;
    	loadCall = makeAsOMETiffBatchCall(index, file, imageID, target);
    }

}
