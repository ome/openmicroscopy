/*
 * org.openmicroscopy.shoola.agents.hiviewer.CategoryGroupLoader
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryGroupData;

/** 
 * Loads a Category Group/Category/Image hierarchy rooted by a given Category 
 * Group.
 * This class calls the <code>loadHierarchy</code> method in the
 * <code>HierarchyBrowsingView</code>.
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
public class CategoryGroupLoader
    extends DataLoader
{

    /** The id of the root Category Group. */
    private long         cgID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param cgID  The id of the root Category Group.
     */
    public CategoryGroupLoader(HiViewer viewer, long cgID)
    {
        super(viewer);
        this.cgID = cgID;
    }
    
    /**
     * Retrieves the Category Group tree.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = hiBrwView.loadHierarchy(CategoryGroupData.class, cgID, 
                            viewer.getRootLevel(), getRootID(), this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the viewer of progress. 
     * @see #update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();
        if (status == null) 
            status = (percDone == 100) ? HiViewer.PAINTING_TEXT :  //Else
                                       ""; //Description wasn't available.   
        if (percDone != 100) //We've only got one call and don't know how long
            percDone = -1;   //it'll take.  Set to indeterminate.
        viewer.setStatus(status, percDone);
    }
    
    /**
     * Feeds the result back to the viewer.
     * @see #handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == HiViewer.DISCARDED) return;  //Async cancel.
        viewer.setHierarchyRoots((Set) result);
    }
    
}
