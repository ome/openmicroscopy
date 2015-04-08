/*
 * org.openmicroscopy.shoola.env.ui.FigureCreator 
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
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;

/** 
 * Creates a figure of the passed images.
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
public class FigureCreator 
	extends UserNotifierLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The parameters to use.*/
    private Object 					param;
    
    /** The select objects. */
    private List<Long>				ids;
    
    /** The type of object to handle. */
    private Class					type;
    
    /** The call-back returned by the server. */
    private ProcessCallback 		callBack;
   
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to create figure", message, ex);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param ctx The security context.
     * @param param  	The parameters used to create the movie.
     * @param ids		The selected objects.
     * @param type		The type of objects.
     * @param activity 	The activity associated to this loader.
     */
	public FigureCreator(UserNotifier viewer,  Registry registry,
			SecurityContext ctx, Object param, List<Long> ids, Class type,
			ActivityComponent activity)
	{
		super(viewer, registry, ctx, activity);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("Objects not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.ids = ids;
		this.type = type;
	}
	
	/**
     * Creates a figure of the selected images.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
        handle = ivView.createFigure(ctx, ids, type, param, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    { 
    	try {
    		if (callBack != null) {
    			callBack.cancel();
        		activity.onActivityCancelled();
    		}
		} catch (Exception e) {
			handleException(e);
		}
    	handle.cancel();
    }
    
    /** 
     * Sets the call-back. 
     * @see UserNotifierLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        //if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        Object o = fe.getPartialResult();
        if (o != null) {
        	if (o instanceof Boolean) {
        		Boolean b = (Boolean) o;
        		if (!b.booleanValue())
        			onException(MESSAGE_RUN, null); 
        	} else {
        		callBack = (ProcessCallback) o;
            	callBack.setAdapter(this);
            	activity.onCallBackSet();
        	}
        }
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    { 
    	if (result instanceof Boolean) {
    		Boolean b = (Boolean) result;
    		if (!b.booleanValue())
    			onException(MESSAGE_RUN, null); 
    	} else if (!(result instanceof Boolean)) {
    		activity.endActivity(result);
    	} 
    }
	
}
