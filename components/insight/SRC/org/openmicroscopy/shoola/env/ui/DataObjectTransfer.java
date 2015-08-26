/*
 * org.openmicroscopy.shoola.env.ui.DataObjectTransfer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.TransferableObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;


/** 
 * Moves data between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DataObjectTransfer
	extends UserNotifierLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The call-back returned by the server. */
    private List<ProcessCallback> callBacks;
    
    /** The object to transfer. */
    private TransferableObject object;

    /** The number of callbacks.*/
    private int number;
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to transfer the objects", message, ex);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param object  	The objects to transfer.
     * @param activity  The activity associated to this loader.
     */
	public DataObjectTransfer(UserNotifier viewer, Registry registry,
			TransferableObject object, ActivityComponent activity)
	{
		super(viewer, registry, null, activity);
		if (object == null)
			throw new IllegalArgumentException("No Objects to transfer.");
		callBacks = new ArrayList<ProcessCallback>();
    	this.object = object;
	}
	
	/**
     * Transfers the objects.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = dmView.changeGroup(object, this);
    }
    
    /**
     * Cancels the on-going data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    { 
    	try {
    		Iterator<ProcessCallback> i = callBacks.iterator();
    		ProcessCallback callback;
    		while (i.hasNext()) {
    			callback = i.next();
    			if (callback != null)
    				callback.cancel();
			}
    		activity.onActivityCancelled();
		} catch (Exception e) {
			handleException(e);
		}
    	handle.cancel();
	}
    
    /** 
     * Stores the call-back.
     * @see UserNotifierLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        Object o = fe.getPartialResult();
        if (o != null) {
        	if (o instanceof Boolean) {
        		Boolean b = (Boolean) o;
        		if (!b.booleanValue())
        			onException("", null); 
        	} else {
        		ProcessCallback callBack = (ProcessCallback) o;
            	callBack.setAdapter(this);
            	callBacks.add(callBack);
            	number++;
            	if (number == object.getSource().size())
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
    		boolean b = ((Boolean) result).booleanValue();
    		if (b) activity.endActivity("Transfer complete");
    		else onException(MESSAGE_RESULT, null); 
    	} else activity.endActivity(result);
    }

}
