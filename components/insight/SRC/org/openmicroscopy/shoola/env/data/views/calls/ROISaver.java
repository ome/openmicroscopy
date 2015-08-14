/*
* org.openmicroscopy.shoola.env.data.views.calls.ROISaver
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
import java.util.Collection;
import java.util.List;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.ROIData;

/**
 * Saves the region of interest related to a given image back to the server.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ROISaver 
	extends BatchCallTree
{

	/** Call to save the ROIs. */
	private BatchCall	saveCall;
	
	/** Was the save successful. */
	private Collection<ROIData> 	result;
	
	/**
	 * Creates a {@link BatchCall} to load the ROIs.
	 * 
	 * @param ctx The security context.
	 * @param imageID The id of the image.
	 * @param fileID  The id of the file.
	 * @param userID  The id of the user. 
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeSaveCall(final SecurityContext ctx,
		final long imageID, final long userID, final List<ROIData> roiList)
	{
		return new BatchCall("save ROI") {
			            public void doCall() throws Exception
	        {
			    OmeroImageService svc = context.getImageService();
			    result = svc.saveROI(ctx, imageID, userID, roiList);
	        }
	    };
	}
	
	/**
	 * Adds the {@link #loadCall} to the computation tree.
	 * @see BatchCallTree#buildTree()
	 */
	protected void buildTree() { add(saveCall); }
		
    /**
     * Returns the result of the save.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param imageID The image's ID.
	 * @param userID The user's ID.
	 * @param roiList The rois to handle.
	 */
	public ROISaver(SecurityContext ctx, long imageID,long userID,
			List<ROIData> roiList)
	{
		saveCall = makeSaveCall(ctx, imageID, userID, roiList);
	}

}
