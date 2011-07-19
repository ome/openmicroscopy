/*
 * org.openmicroscopy.shoola.env.data.views.calls.ArchivedImageLoader 
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



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

/** 
 * Command to load the archived image.
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
public class ArchivedImageLoader 
	extends BatchCallTree
{

	/** The result of the query. */
    private Object      result;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the channels metadata.
     * 
     * @param pixelsID The ID of the pixels set.
     * @param userID   If the id is specified i.e. not <code>-1</code>, 
     * 				   load the color associated to the channel, 
     * 				   <code>false</code> otherwise.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long pixelsID, final String folder) 
    {
        return new BatchCall("Download the archived files. ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                result = os.getArchivedImage(folder, pixelsID);
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
    protected Object getResult() { return result; }

    /**
     * Loads the archived images.
     * If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the caller's thread.
	 * 
     * @param pixelsID  The Id of the pixels set.
     * @param folderPath The location where to download the archived image.
     */
    public ArchivedImageLoader(long pixelsID, String folderPath)
    {
    	if (pixelsID < 0)
    		 throw new IllegalArgumentException("Pixels ID not valid.");
        loadCall = makeBatchCall(pixelsID, folderPath);
    }
}
