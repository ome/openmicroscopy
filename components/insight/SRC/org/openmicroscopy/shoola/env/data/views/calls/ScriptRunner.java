/*
 * org.openmicroscopy.shoola.env.data.views.calls.ScriptRunner 
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
import org.openmicroscopy.shoola.env.data.ScriptCallback;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.env.data.views.ProcessBatchCall;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;

/** 
 * Creates a batch call to run a script.
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
public class ScriptRunner 
	extends BatchCallTree
{
    
    /** The server call-handle to the computation. */
    private Object callBack;
    
    /** Calls to run the script. */
    private BatchCall loadCall;
    
    /**
     * Creates a {@link BatchCall} to run the script.
     * 
     * @param ctx The security context.
     * @param script The script to run. 
     * @return The {@link BatchCall}.
     */
    private BatchCall makeCall(final SecurityContext ctx,
    		final ScriptObject script)
    {
    	return new ProcessBatchCall("Run the script") {
    		public ProcessCallback initialize() throws Exception
    		{
    			OmeroImageService os = context.getImageService();
    			ScriptCallback cb = os.runScript(ctx, script);
    			if (cb == null) {
                	callBack = Boolean.valueOf(false);
                	return null;
                } else {
                	callBack = new ProcessCallback(cb);
                    return (ProcessCallback) callBack;
                }
    		}
    	};
    }
    
    /**
     * Returns the server call-handle to the computation.
     * 
     * @return See above.
     */
    protected Object getPartialResult() { return callBack; }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the result.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return Boolean.valueOf(true); }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param script The script to run.
     */
    public ScriptRunner(SecurityContext ctx, ScriptObject script)
    {
		loadCall = makeCall(ctx, script);
    }

}
