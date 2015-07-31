/*
 * org.openmicroscopy.shoola.env.data.views.calls.ScriptsLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * Loads the scripts.
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
public class ScriptsLoader 
	extends BatchCallTree
{

	/** Indicates to load the specified script. */
	public static final int	SINGLE_SCRIPT = 0;
	
	/** Indicates to load all the scripts. */
	public static final int	ALL_SCRIPTS = 1;
	
	/** Indicates to load all the scripts. */
	public static final int	DEFAULT_SCRIPTS = 2;
	
	/** Result of the call. */
	private Object result;

	/** Loads the specified tree. */
	private BatchCall loadCall;
	
	/** The security context.*/
    private SecurityContext ctx;
    
	/**
	 * Creates a {@link BatchCall} to load the scripts.
	 * 
	 * @param userID The id of the experimenter or <code>-1</code>.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeBatchCall(final long userID)
	{
		return new BatchCall("Load scripts ") {
			public void doCall() throws Exception
			{
				OmeroImageService svc = context.getImageService();
				result = svc.loadAvailableScripts(ctx, userID);
			}
		};
	}
	
	/**
	 * Creates a {@link BatchCall} to load the scripts.
	 * 
	 * @param scriptID The id of the script to load.
	 * @return The {@link BatchCall}.
	 */
	private BatchCall makeLoadScriptBatchCall(final long scriptID)
	{
		return new BatchCall("Load script. ") {
			public void doCall() throws Exception
			{
				OmeroImageService svc = context.getImageService();
				result = svc.loadScript(ctx, scriptID);
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
	 * @param id The id of the experimenter, the id of the script 
	 *           or <code>-1</code>.
	 * @param index One of the constants defined by this class.
	 */
	public ScriptsLoader(SecurityContext ctx, long id, int index)
	{
		this.ctx = ctx;
		switch (index) {
			case ALL_SCRIPTS:
			case DEFAULT_SCRIPTS:
				loadCall = makeBatchCall(id);
				break;
			case SINGLE_SCRIPT:
				loadCall = makeLoadScriptBatchCall(id);
		}
	}
	
}
