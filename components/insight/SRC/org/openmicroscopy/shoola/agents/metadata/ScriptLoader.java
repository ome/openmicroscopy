/*
 * org.openmicroscopy.shoola.agents.metadata.ScriptLoader 
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
package org.openmicroscopy.shoola.agents.metadata;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.ProcessException;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.log.LogMessage;

/** 
 * Loads the specified script. This class calls the <code>loadScript</code> 
 * method in the <code>MetadataHandlerView</code>.
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
public class ScriptLoader 
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** The script's identifier. */
    private long scriptID;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param scriptID	The script's identifier.
     */
    public ScriptLoader(Editor viewer, SecurityContext ctx, long scriptID)
    {
    	super(viewer, ctx);
    	if (scriptID < 0)
    		throw new IllegalArgumentException("No script specified.");
    	this.scriptID = scriptID;
    }
    
    /** 
     * Loads the script.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	handle = mhView.loadScript(ctx, scriptID, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	//viewer.setScripts((List) result);
    	viewer.setScript((ScriptObject) result);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	viewer.setStatus(false);
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        if (exc instanceof ProcessException) {
            ProcessException se = (ProcessException) exc;
            s = se.getMessage();

            Throwable cause = se.getCause();
            if (cause instanceof omero.ResourceError) {
                omero.ResourceError re = (omero.ResourceError) cause;
                s += String.format("\nError: \"%s\"", re.message);
            } else if (cause != null && cause.getMessage() != null) {
                s += (":" + cause.getMessage());
            }
            registry.getUserNotifier().notifyInfo("Running Script",
                    s+"\nPlease contact your administrator.");
        } else {
            registry.getUserNotifier().notifyError("Data Retrieval Failure",
                    s, exc);
        }
    }
    
}
