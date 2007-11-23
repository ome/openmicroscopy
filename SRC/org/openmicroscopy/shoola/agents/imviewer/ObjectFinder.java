/*
 * org.openmicroscopy.shoola.agents.imviewer.ObjectFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer;

import java.util.List;
import java.util.Set;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.ImageData;

/** 
 * Searches for tags, images etc.
 * This class calls <code>searchFor</code> method in the
 * <code>DataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ObjectFinder 
	extends DataLoader
{
	
	/** Indicates to search for tags. */
	public static final int TAGS = 0;
	
	/** Indicates to search for images. */
	public static final int IMAGES = 1;
	
	/** Indicates to search for annotations. */
	public static final int ANNOTATIONS = 2;

	/** The id of the experimenter. */
	private long		expID;
	
	/** One of the constants defined by this class.  */
	private int			type;
	
	/** Collection of terms to search for. */
	private List		values;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /** 
     * Checks if the passed type is supported.
     * 
     * @param value The value to handle.
     */
    private void checkType(int value)
    {
    	switch (value) {
			case TAGS:
			case IMAGES:
			case ANNOTATIONS:
				return;
			default:
				throw new IllegalArgumentException("Type not supported.");
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The view this loader is for.
     *                  Mustn't be <code>null</code>.
     * @param expID		The id of the experimenter.
     * @param index		One of the constants defined by this class. 
     * @param values	Collection of terms to search for.
     * @param type		The type of data to search, One of the constants 
     * 					defined by this class.
     */
    public ObjectFinder(ImViewer viewer, long expID, List values, int type)
    {
    	super(viewer);
    	checkType(type);
    	if (values == null || values.size() == 0) return;
    	this.expID = expID;
    	this.type = type;
    	this.values = values;
    }
    
    /**
     * Searches for values.
     * @see DataLoader#load()
     */
    public void load()
    {
    	Class klass = null;
    	switch (type) {
	    	case TAGS:
	    		klass = CategoryData.class;
	    		break;
			case IMAGES:
				klass = ImageData.class;
	    		break;
			case ANNOTATIONS:
				klass = AnnotationData.class;
		}
    	handle = dhView.searchFor(klass, expID, values, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
       if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
       EventBus bus = ImViewerAgent.getRegistry().getEventBus();
       Set set = (Set) result;
       if (set == null || set.size() == 0) {
	       	UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
	       	un.notifyInfo("Search", "No results matching your criteria.");
	       	return;
       }
       switch (type) {
	       	case TAGS:
	       		bus.post(new Browse(set, Browse.CATEGORIES, 
	   			viewer.getUserDetails(), viewer.getUI().getBounds()));  
	       		break;
	       	case IMAGES:
	    	case ANNOTATIONS:
	       		bus.post(new Browse(set, Browse.IMAGES, 
	   			viewer.getUserDetails(), viewer.getUI().getBounds()));  
	       		break;
       }
    }
    
}
