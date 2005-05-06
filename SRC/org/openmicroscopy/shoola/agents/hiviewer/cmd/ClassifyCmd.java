/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd
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
import org.openmicroscopy.shoola.agents.events.datamng.ClassifyImage;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.event.EventBus;

/** 
 * 
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
public class ClassifyCmd
    implements ActionCmd
{
    
    private HiViewer    model;
    private DataObject  hierarchyObject;
    
    private boolean     classify;
    
    public ClassifyCmd(DataObject hierarchyObject, boolean classify)
    {
        if (hierarchyObject == null)
            throw new NullPointerException("No hierarchy object.");
        this.hierarchyObject = hierarchyObject;
        this.classify = classify;
    }
    
    /** Creates a new instance.*/
    public ClassifyCmd(HiViewer model, boolean classify)
    {
        if (model == null)
            throw new IllegalArgumentException("no model");
        this.model = model;
        this.classify = classify;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        /*
        if (model != null) {
            ImageDisplay selectedDisplay = model.getBrowser().
                                                    getSelectedDisplay();
            hierarchyObject = (DataObject) selectedDisplay.getHierarchyObject();
        }
        if (hierarchyObject == null) return;
        if (hierarchyObject instanceof ImageSummary) {
            EventBus eventBus = HiViewerAgent.getRegistry().getEventBus();
            eventBus.post(new ClassifyImage((ImageSummary) hierarchyObject));
        }
        */
    }

}
