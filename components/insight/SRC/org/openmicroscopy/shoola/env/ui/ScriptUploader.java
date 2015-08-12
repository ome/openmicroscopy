/*
 * org.openmicroscopy.shoola.env.ui.ScriptUploader
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Uploads the specified script.
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
public class ScriptUploader 
	extends UserNotifierLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** Reference to the script to run. */
    private ScriptObject 			script;
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to upload the script", message, ex);
    }

    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param ctx The security context.
     * @param script  	The script to run.
     * @param activity  The activity associated to this loader.
     */
	public ScriptUploader(UserNotifier viewer,  Registry registry,
		SecurityContext ctx, ScriptObject script, ActivityComponent activity)
	{
		super(viewer, registry, ctx, activity);
		if (script == null)
			throw new IllegalArgumentException("No script to run.");
		this.script = script;
	}
	
	/**
     * Uploads the script.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = ivView.uploadScript(ctx, script, this);
    }
    
    /**
     * Cancels the on-going data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    {
    	if (handle != null) handle.cancel();
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    { 
    	if (result == null) onException(MESSAGE_RESULT, null);
    	else if (result instanceof Long) {
			Long value = (Long) result;
			if (value < 0) onException(MESSAGE_RESULT, null);
			else activity.endActivity(result); 
		}
    }
	
}
