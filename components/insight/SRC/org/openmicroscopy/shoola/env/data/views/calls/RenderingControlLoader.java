/*
 * org.openmicroscopy.shoola.env.data.views.calls.RenderingControlLoader
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

//Third-party libraries

//Application-internal dependencies
import omero.gateway.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * Command to retrieve the {@link RenderingControl}.
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
public class RenderingControlLoader
  	extends BatchCallTree
{
	
	/** Indicates to load the rendering engine. */
	public static final int LOAD = 0;
	
	/** Indicates to reload the rendering engine. */
	public static final int RELOAD = 1;
	
	/** Indicates to reload the rendering engine. */
	public static final int RESET = 2;
	
	/** Indicates to reload the rendering engine. */
	public static final int SHUTDOWN = 3;
	
	/** Result of the call. */
	private Object result;

	/** Loads the specified tree. */
	private BatchCall loadCall;

	/** The security context.*/
    private SecurityContext ctx;
    
	/**
	 * Creates a {@link BatchCall} to retrieve rendering control.
	 * 
	 * @param pixelsID  The id of the pixels set the rendering control is for.
	 * @param index		Pass <code>true</code> to reload the rendering engine,
	 * 					<code>false</code> otherwise.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final long pixelsID, final int index)
	{
		return new BatchCall("Loading rendering control: ") {
			public void doCall() throws Exception
			{
				OmeroImageService rds = context.getImageService();
				switch (index) {
					default:
					case LOAD:
						result = rds.loadRenderingControl(ctx, pixelsID);
                                                if (result == null)
                                                    throw new DSOutOfServiceException(
                                                            "Cannot start the "
                                                                    + "rendering engine for pixelsID "
                                                                    + pixelsID);
						break;
					case RELOAD:
						result = rds.reloadRenderingService(ctx, pixelsID);
                                                if (result == null)
                                                    throw new DSOutOfServiceException(
                                                            "Cannot start the "
                                                                    + "rendering engine for pixelsID "
                                                                    + pixelsID);
						break;
					case RESET:
						result = rds.resetRenderingService(ctx, pixelsID);
						break;
					case SHUTDOWN:
						rds.shutDown(ctx, pixelsID);
				}
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
	 * @param index		One of the constants defined by this class.
	 */
	public RenderingControlLoader(SecurityContext ctx, long pixelsID, int index)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("ID not valid.");
		this.ctx = ctx;
		loadCall = makeBatchCall(pixelsID, index);
	}

}
