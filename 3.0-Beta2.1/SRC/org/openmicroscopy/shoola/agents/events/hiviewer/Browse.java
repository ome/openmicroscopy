/*
 * org.openmicroscopy.shoola.agents.events.hiviewer.Browse
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.events.hiviewer;


//Java imports
import java.awt.Rectangle;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.ExperimenterData;

/** 
 * Event to browse a given <code>Data Object</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class Browse
    extends RequestEvent
{
    
    /** Event ID corresponding to a browse Project event. */
    public static final int PROJECT = 0;
    
    /** Event ID corresponding to a browse Dataset event. */
    public static final int DATASET = 1;
    
    /** Event ID corresponding to a browse categoryGroup event. */
    public static final int CATEGORY_GROUP = 2;
    
    /** Event ID corresponding to a browse category event. */
    public static final int CATEGORY = 3;
    
    /** Event ID corresponding to a browse images event. */
    public static final int IMAGES = 4;
    
    /** Event ID corresponding to a browse datasets event. */
    public static final int DATASETS = 5;
    
    /** Event ID corresponding to a browse datasets event. */
    public static final int CATEGORIES = 6;
    
    /** Event ID corresponding to a browse datasets event. */
    public static final int PROJECTS = 7;
    
    /** Event ID corresponding to a browse datasets event. */
    public static final int CATEGORY_GROUPS = 8;
    
    /** ID of the top element in the hierarchy. */
    private long        		hierarchyObjectID;
    
    /** List of IDs object to browse. */
    private Set<Long>			objectsIDs;
    
    /** 
     * Index of the top element in the hierarchy e.g.
     * if eventIndex = PROJECT, this means that we want to browse the selected
     * project.
     */
    private int         		eventIndex;
    
    /** The ID of the selected group for the current user. */
    private long				userGroupID;
    
    /** The bounds of the component posting the event. */
    private Rectangle   		requesterBounds;
    
    /** The currently selected user. */
    private ExperimenterData	experimenter;
    
    /**
     * Controls if the specified index is supported.
     * 
     * @param index The index to control.
     */
    private void checkEventIndex(int index)
    {
        switch (index) {
            case PROJECT:
            case DATASET:
            case CATEGORY_GROUP:
            case CATEGORY:
                return; 
            default:
                throw new IllegalArgumentException("Event index not valid.");
        }
    }
    
    /**
     * Controls if the specified index is supported.
     * 
     * @param index The index to control.
     */
    private void checkMultiNodesIndex(int index)
    {
        switch (index) {
            case IMAGES:
            case DATASETS:
            case CATEGORIES:
            case PROJECTS:
            case CATEGORY_GROUPS:
                return; 
            default:
                throw new IllegalArgumentException("Event index not valid.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param hierarchyObjectID The Id of the <code>Data Object</code> to 
     *                          browse.
     * @param index             The index of the browser. One of the constants
     *                          defined by this class.
     * @param experimenter		The currently selected experimenter. 
     * 							Mustn't be <code>null</code>.
     * @param userGroupID		The ID of the selected group for the current 
     * 							user.		
     * @param bounds            The bounds of the component posting the event.
     */
    public Browse(long hierarchyObjectID, int index, 
    			ExperimenterData experimenter, long userGroupID, 
    			Rectangle bounds)
    {
        checkEventIndex(index);
        if (experimenter == null) 
        	throw new IllegalArgumentException("No experimenter.");
        this.hierarchyObjectID = hierarchyObjectID;
        eventIndex = index;
        this.experimenter = experimenter;
        this.userGroupID = userGroupID;
        requesterBounds = bounds;
    }

    /**
     * Creates a new instance. This contructor should only be invoked to 
     * browse a list of images.
     * 
     * @param ids       	The list of objects ids.
     * @param index     	The index of the browser. One of the constants
     *                  	defined by this class.
     * @param experimenter	The currently selected experimenter. 
     * 						Mustn't be <code>null</code>.
     * @param userGroupID	The ID of the selected group for the current 
     * 						user.                 
     * @param bounds    	The bounds of the component posting the event.                 
     */
    public Browse(Set<Long> ids, int index, ExperimenterData experimenter, 
    			long userGroupID, Rectangle bounds)
    {
    	checkMultiNodesIndex(index); 
    	 if (experimenter == null) 
         	throw new IllegalArgumentException("No experimenter.");
        eventIndex = index;
        this.experimenter = experimenter;
        this.userGroupID = userGroupID;
        objectsIDs = ids;
        requesterBounds = bounds;
    }
    
    /**
     * Returns the browse event index. 
     * 
     * @return See above.
     */
    public int getEventIndex() { return eventIndex; }
    
    /** 
     * Returns the id of the <code>dataObject</code>.
     * 
     * @return See above.
     */
    public long getHierarchyObjectID() { return hierarchyObjectID; }
    
    /**
     * Returns the root level.
     * 
     * @return See above.
     */
    public ExperimenterData getExperimenter() { return experimenter; }
    
    /**
     * Returns the ID of the selected group for the current user.
     * 
     * @return See above.
     */
    public long getUserGroupID() { return userGroupID; }
    
    /**
     * Returns the list of the objects to browse.
     * 
     * @return See above.
     */
    public Set<Long> getObjectIDs() { return objectsIDs; }
    
    /**
     * Returns the bounds of the component posting the event. 
     * Returns <code>null</code> if not available.
     * 
     * @return See above.
     */
    public Rectangle getRequesterBounds() { return requesterBounds; }
    
}
