/*
 * org.openmicroscopy.shoola.env.ui.ScriptHandler
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
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Runs the specified script.
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
public class ScriptHandler 
	extends UserNotifierLoader
{

	/** Indicates to run the script. */
	public static final int	RUN = ScriptActivity.RUN;
	
	/** Indicates to upload the script. */
	public static final int	UPLOAD = ScriptActivity.UPLOAD;
	
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** Reference to the script to run. */
    private ScriptObject 			script;

    /** One of constants defined by this class. */
    private int 					index;
    
    /** Notifies the user that an error occurred. */
    protected void onException() { handleNullResult(); }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param script  	The script to run
     */
	public ScriptHandler(UserNotifier viewer,  Registry registry,
			ScriptObject script, int index, ActivityComponent activity)
	{
		super(viewer, registry, activity);
		if (script == null)
			throw new IllegalArgumentException("No script to run.");
		this.script = script;
		this.index = index;
	}
	
	/**
     * Runs the script.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case UPLOAD:
				handle = ivView.uploadScript(script, this);
				break;
			case RUN:
				handle = ivView.runScript(script, this);
		}
    }
    
    /**
     * Cancels the on-going data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it wasn't possible to create the figure.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    { 
    	activity.notifyError("Unable to run the script");
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result) { activity.endActivity(result); }
	
}
