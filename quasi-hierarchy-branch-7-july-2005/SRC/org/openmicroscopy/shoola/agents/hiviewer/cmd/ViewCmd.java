/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;


/** 
 * TODO: add comments
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
public class ViewCmd
    implements ActionCmd
{
    
    private HiViewer    model;
    private DataObject  hierarchyObject;
    
    
    public ViewCmd(DataObject hierarchyObject)
    {
        if (hierarchyObject == null)
            throw new NullPointerException("No hierarchy object.");
        this.hierarchyObject = hierarchyObject;
    }
    
    /** Creates a new instance.*/
    public ViewCmd(HiViewer model)
    {
        if (model == null)
            throw new IllegalArgumentException("no model");
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        if (model != null) {
            ImageDisplay selectedDisplay = model.getBrowser().
                                                    getSelectedDisplay();
            hierarchyObject = (DataObject) selectedDisplay.getHierarchyObject();
        }
        if (hierarchyObject == null) return;
        if (hierarchyObject instanceof DatasetSummary)
            HiViewerAgent.browse(Browse.DATASET, 
                    ((DatasetSummaryLinked) hierarchyObject).getID());
        else if (hierarchyObject instanceof ProjectSummary)
            HiViewerAgent.browse(Browse.PROJECT, 
                    ((ProjectSummary) hierarchyObject).getID());
        else if (hierarchyObject instanceof CategoryGroupData)
            HiViewerAgent.browse(Browse.CATEGORY_GROUP, 
                    ((CategoryGroupData) hierarchyObject).getID());
        else if (hierarchyObject instanceof CategoryData)
            HiViewerAgent.browse(Browse.CATEGORY, 
                    ((CategoryData) hierarchyObject).getID());
        else if (hierarchyObject instanceof ImageSummary) {
            EventBus eventBus = HiViewerAgent.getRegistry().getEventBus();
            ImageSummary is = (ImageSummary) hierarchyObject;
            int[] pxSets = is.getPixelsIDs();
            eventBus.post(new LoadImage(is.getID(), pxSets[0], is.getName()));   
        }
    }

}
