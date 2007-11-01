/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.PropertiesCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import pojos.DataObject;

/** 
 * Posts an event to bring up the Property widget.
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
public class PropertiesCmd
    implements ActionCmd
{
    
    /** Reference to the model */
    private HiViewer    model;
    
    /** The selected hierarchy object. */
    private DataObject  hierarchyObject;

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public PropertiesCmd(HiViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        this.model = model;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model             Reference to the model.
     *                          Mustn't be <code>null</code>.
     * @param hierarchyObject   The selected hierarchy object.
     */
    public PropertiesCmd(HiViewer model, DataObject hierarchyObject)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        this.model = model;
        this.hierarchyObject = hierarchyObject;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        if (model != null && hierarchyObject == null) {
        	Browser browser = model.getBrowser();
        	if (browser != null) {
        		ImageDisplay d = browser.getLastSelectedDisplay();
                hierarchyObject = (DataObject) d.getHierarchyObject();
        	}
            
        }
        if (hierarchyObject == null) return;
        //NEED to review that code.
        //post a show properties event.
        //if (model != null) model.moveToBack(); //move the window to the back.
        //EventBus eventBus = HiViewerAgent.getRegistry().getEventBus();
        //eventBus.post(new ShowProperties(hierarchyObject, ShowProperties.EDIT));
        model.showProperties(hierarchyObject);
    }

}