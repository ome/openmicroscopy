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
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;


/** 
 * Views the selected image or browses the selected container.
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
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** The hierarchy object hosting by the selected {@link ImageDisplay}. */
    private DataObject  hierarchyObject;
    
    
    /**
     * Creates a new instance.
     * 
     * @param hierarchyObject The hierarchy object hosting by the selected
     * {@link ImageDisplay}.
     */
    public ViewCmd(DataObject hierarchyObject)
    {
        if (hierarchyObject == null)
            throw new IllegalArgumentException("No hierarchy object.");
        if (!(hierarchyObject instanceof ImageData))
            throw new IllegalArgumentException("Object must be an ImageData " +
                    "object.");
        this.hierarchyObject = hierarchyObject;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model             Reference to the model.
     *                          Mustn't be <code>null</code>.
     */
    public ViewCmd(HiViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model             Reference to the model.
     *                          Mustn't be <code>null</code>.
     * @param hierarchyObject   The hierarchy object hosting by the selected
     *                          {@link ImageDisplay}.
     */
    public ViewCmd(HiViewer model, DataObject hierarchyObject)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.hierarchyObject = hierarchyObject;
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        if (model != null && hierarchyObject == null) {
            ImageDisplay selectedDisplay = model.getBrowser().
                                                    getSelectedDisplay();
            hierarchyObject = (DataObject) selectedDisplay.getHierarchyObject();
        }
        if (hierarchyObject == null) return;
        if (hierarchyObject instanceof DatasetData)
            HiViewerAgent.browse(Browse.DATASET, 
                    ((DatasetData) hierarchyObject).getId(), 
                    model.getRootLevel(), model.getRootID());
        else if (hierarchyObject instanceof ProjectData)
            HiViewerAgent.browse(Browse.PROJECT, 
                    ((ProjectData) hierarchyObject).getId(),
                    model.getRootLevel(), model.getRootID());
        else if (hierarchyObject instanceof CategoryGroupData)
            HiViewerAgent.browse(Browse.CATEGORY_GROUP, 
                    ((CategoryGroupData) hierarchyObject).getId(),
                    model.getRootLevel(), model.getRootID());
        else if (hierarchyObject instanceof CategoryData)
            HiViewerAgent.browse(Browse.CATEGORY, 
                    ((CategoryData) hierarchyObject).getId(),
                    model.getRootLevel(), model.getRootID());
        else if (hierarchyObject instanceof ImageData) {
            EventBus eventBus = HiViewerAgent.getRegistry().getEventBus();
            ImageData is = (ImageData) hierarchyObject;
            eventBus.post(new LoadImage(is.getId(), 
                    is.getDefaultPixels().getId(), is.getName()));   
        }
    }

}
