/*
 * org.openmicroscopy.shoola.env.ui.DataObjectRemover
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ProcessCallback;

import pojos.DataObject;

/** 
 * Deletes a collection of objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataObjectRemover 
	extends UserNotifierLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The call-back returned by the server. */
    private List<ProcessCallback> callBacks;
    
    /** The collection of objects to delete. */
    private Map<SecurityContext, Collection<DeletableObject>> objects;

    /** The number of callbacks.*/
    private int number;
    
	/**
     * Returns the SecurityContext if already added or <code>null</code>.
     * 
     * @param map The map to check.
     * @param id The group's identifier.
     * @return See above.
     */
    private SecurityContext getKey(
    	Map<SecurityContext, Collection<DeletableObject>> map, long id)
    {
    	Iterator<SecurityContext> i = map.keySet().iterator();
    	SecurityContext ctx;
    	while (i.hasNext()) {
			ctx = i.next();
			if (ctx.getGroupID() == id)
				return ctx;
		}
    	return null;
    }
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String)
     */
    protected void onException(String message, Throwable ex)
    { 
    	if (activity != null)
    		activity.notifyError("Unable to delete the object", message, ex);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param objects  	The collection of objects to delete.
     * @param activity  The activity associated to this loader.
     */
	public DataObjectRemover(UserNotifier viewer, Registry registry,
		List<DeletableObject> objects, 
		ActivityComponent activity)
	{
		super(viewer, registry, null, activity);
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No Objects to delete.");
		callBacks = new ArrayList<ProcessCallback>();
		Map<SecurityContext, Collection<DeletableObject>>
    	map = new HashMap<SecurityContext, Collection<DeletableObject>>();
    	Iterator<DeletableObject> i = objects.iterator();
    	DeletableObject o;
    	DataObject ho;
    	long groupId;
    	SecurityContext ctx;
    	Collection<DeletableObject> l;
    	while (i.hasNext()) {
			o = i.next();
			ho = o.getObjectToDelete();
			ctx = o.getSecurityContext();
			if (ctx == null) {
				groupId = ho.getGroupId();
				ctx = getKey(map, groupId);
				if (ctx == null) {
					ctx = new SecurityContext(groupId);
					map.put(ctx, new ArrayList<DeletableObject>());
				}
			} else {
				if (getKey(map, ctx.getGroupID()) == null) {
					map.put(ctx, new ArrayList<DeletableObject>());
				}
			}
			l = map.get(ctx);
			l.add(o);
		}
    	this.objects = map;
	}
	
	/**
     * Deletes the object.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = dmView.delete(objects, this);
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
    		if (activity != null)
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
            	if (number == objects.size() && activity != null)
            		activity.onCallBackSet();
        	}
        }
    }
 
    /**Does nothing since the result will return <code>null</code>.*/
    public void handleNullResult() {}
    
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (activity != null) {
	    	if (result instanceof Boolean) {
	    		boolean b = ((Boolean) result).booleanValue();
	    		if (b) activity.endActivity(DeleteActivity.DELETE_COMPLETE);
	    		else onException(MESSAGE_RESULT, null);
	    	} else activity.endActivity(result);
    	}
    }

}
