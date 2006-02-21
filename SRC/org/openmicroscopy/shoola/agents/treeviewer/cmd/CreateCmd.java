/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.CreateCmd
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.treeviewer.ShowProperties;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.event.EventBus;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class CreateCmd
    implements ActionCmd
{
    
    /** Indicates to create a <code>Project</code>. */
    public static final int PROJECT = 0;
    
    /** Indicates to create a <code>Dataset</code>. */
    public static final int DATASET = 1;
    
    /** Indicates to create a <code>CategoryGroup</code>. */
    public static final int CATEGORY_GROUP = 2;
    
    /** Indicates to create a <code>Category</code>. */
    public static final int CATEGORY = 3;
    
    /** Reference to the model. */
    private TreeViewer  model;
    
    /**
     * The <code>DataObject</code> corresponding to a constant
     * defined by this class.
     */
    private DataObject  userObject;
    
    /**
     * Checks that the specified type is currently supported
     * and returns the corresponding <code>DataObject</code>.
     * 
     * @param type The type to check.
     * @return See above.
     */
    private DataObject checkNodeType(int type)
    {
        switch (type) {
            case PROJECT: return new ProjectData();
            case DATASET: return new DatasetData(); 
            case CATEGORY_GROUP: return new CategoryGroupData(); 
            case CATEGORY: return new CategoryData(); 
            default:
                throw new IllegalArgumentException("Type not supported");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the model. Mustn't be <code>null</code>.
     * @param type      One of the constants defined by this class.
     */
    public CreateCmd(TreeViewer model, int type)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        userObject = checkNodeType(type);
        this.model = model;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Browser browser = model.getSelectedBrowser();
        if (browser == null) return;
        if (userObject == null) return; //shouldn't happen.
        EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
        bus.post(new ShowProperties(userObject, ShowProperties.CREATE));
    }
    
}
