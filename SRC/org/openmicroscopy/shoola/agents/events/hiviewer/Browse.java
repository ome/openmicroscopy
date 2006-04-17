/*
 * org.openmicroscopy.shoola.agents.events.hiviewer.Browse
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.events.hiviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.ExperimenterData;
import pojos.GroupData;

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
    

    /** ID of the top element in the hierarchy. */
    private long    hierarchyObjectID;
    
    /** 
     * Index of the top element in the hierarchy e.g.
     * if eventIndex = PROJECT, this means that we want to browse the selected
     * project.
     */
    private int     eventIndex;
    
    /** 
     * The level of the root, either {@link ExperimenterData} or
     * {@link  GroupData}.
     */
    private Class   rootLevel;
    
    /** The Id of the root. */
    private long    rootID;
    
    /**
     * Checks if the specified level is supported.
     * 
     * @param level The level to control.
     */
    private void checkRootLevel(Class level)
    {
        if (level.equals(ExperimenterData.class) ||
                level.equals(GroupData.class)) return;
        throw new IllegalArgumentException("Root level not supported");
    }
    
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
     * Creates a new instance.
     * 
     * @param hierarchyObjectID The Id of the <code>Data Object</code> to 
     *                          browse.
     * @param index             The index of the browser. One of the constants
     *                          defined by this class.
     * @param rootLevel         The level of the hierarchy either 
     *                          <code>GroupData</code> or 
     *                          <code>ExperimenterData</code>.
     * @param rootID            The id of the root level. The value is taken
     *                          into account if only if the root level is a 
     *                          group.
     */
    public Browse(long hierarchyObjectID, int index, Class rootLevel,
                  long rootID)
    {
        checkEventIndex(index); 
        checkRootLevel(rootLevel);
        this.hierarchyObjectID = hierarchyObjectID;
        eventIndex = index;
        this.rootLevel = rootLevel;
        this.rootID = rootID;
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
    public Class getRootLevel() { return rootLevel; }
    
    /**
     * Returns the root Id.
     * 
     * @return See above.
     */
    public long getRootID() { return rootID; }
    
}
