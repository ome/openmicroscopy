/*
 * org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

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
 * @since OME3.0
 */
public class RenderingSettingsLoader  
	extends BatchCallTree
{

	/** Result of the call. */
	private Object result;

	/** Loads the specified tree. */
	private BatchCall loadCall;

	/** The security context.*/
    private SecurityContext ctx;
    
	/**
	 * Creates a {@link BatchCall} to retrieve rendering settings.
	 * 
	 * @param pixelsID  The id of the pixels set the rendering control is for.
	 * @param userID	The id of the user the settings are related to or 	
	 * 					<code>-1</code>.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final long pixelsID, final long userID)
	{
		return new BatchCall("Loading rendering settings: ") {
			public void doCall() throws Exception
			{
				OmeroImageService rds = context.getImageService();
				result = rds.getRenderingSettings(ctx, pixelsID, userID);
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
	 * Returns the {@link RenderingControl}.
	 * 
	 * @see BatchCallTree#getResult()
	 */
	protected Object getResult() { return result; }

	/**
	 * Creates a new instance.
	 * If bad arguments are passed, we throw a runtime exception so to fail
	 * early and in the caller's thread.
	 * 
	 * @param ctx The security context.
	 * @param pixelsID  The id of the pixels set the rendering control is for.
	 * @param userID	The id of the user.
	 */
	public RenderingSettingsLoader(SecurityContext ctx, long pixelsID,
			long userID)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("ID not valid.");
		this.ctx = ctx;
		loadCall = makeBatchCall(pixelsID, userID);
	}

}
