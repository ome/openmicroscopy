/*
 * org.openmicroscopy.shoola.env.ui.Analyser 
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
import org.openmicroscopy.shoola.env.data.ScriptCallback;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.AnalysisParam;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * Analyzes the passed objects.
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
public class Analyser 
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
    
    /** The result. */
    private FileAnnotationData		data;
    
    /** The type of analysis to perform. */
    private int						index;
    
    /** The call-back returned by the server. */
    private ScriptCallback 			callBack;
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Exception)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to analyse data", message, ex);
    }
    
    /** Notifies the user that an error occurred. */
    protected void onException() { handleNullResult(); }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param param  	The parameters used to create the movie.
     * @param ids		The selected objects.
     * @param type		The type of objects.
     * @param index		The type of analysis to perform.
     * @param activity 	The activity associated to this loader.
     */
	public Analyser(UserNotifier viewer,  Registry registry,
			Object param, List<Long> ids, Class type, int index, 
			ActivityComponent activity)
	{
		super(viewer, registry, activity);
		if (ids == null || ids.size() == 0)
			throw new IllegalArgumentException("Objects not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.ids = ids;
		this.type = type;
		this.index = index;
	}
	
	/**
     * Creates a figure of the selected images.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	switch (index) {
			case AnalysisParam.FLIM:
				break;
			case AnalysisParam.FRAP:
				 handle = ivView.analyseFRAP(ids, type, param, this);
				break;
		}
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
        		onException(MESSAGE_RUN, null); 
        	} else {
        		callBack = (ScriptCallback) o;
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
    	if (result == null) onException(MESSAGE_RESULT, null); 
    	else if (!(result instanceof Boolean)) {
    		activity.endActivity(result);
    	} 
    }
	
}
