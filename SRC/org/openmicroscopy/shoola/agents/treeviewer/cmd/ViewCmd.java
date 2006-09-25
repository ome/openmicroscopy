/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd
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
import java.util.HashSet;
import java.util.Set;

import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Views the selected image or browses the selected container.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
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
    private TreeViewer model;
    
    /** The <code>DataObject</code> to browse or view depending on the type. */
    private DataObject  hierarchyObject;
    
    /**
     * Converts the specified UI rootLevel into its corresponding 
     * constant defined by the {@link OmeroDataService}.
     * 
     * @param level The level to convert.
     * @return See above.
     */
    private Class convertRootLevel(int level)
    {
        switch (level) {
            case TreeViewer.USER_ROOT: return ExperimenterData.class;
            case TreeViewer.GROUP_ROOT: return GroupData.class;
            default:
                throw new IllegalArgumentException("Level not supported");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public ViewCmd(TreeViewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model             Reference to the model.
     *                          Mustn't be <code>null</code>.
     * @param hierarchyObject   The object to browse or view.
     *                          Mustn't be <code>null</code>.
     *                          
     */
    public ViewCmd(TreeViewer model, DataObject hierarchyObject)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        if (hierarchyObject == null) 
            throw new IllegalArgumentException("No hierarchyObject.");
        this.hierarchyObject = hierarchyObject;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Browser browser = model.getSelectedBrowser();
        if (browser == null) return;
        Object ho = null;
        EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
        Class root = convertRootLevel(model.getRootLevel());
        if (hierarchyObject != null) ho = hierarchyObject;
        else {
            TreeImageDisplay[] nodes = browser.getSelectedDisplays();
            if (nodes.length > 1) {
                TreeImageDisplay n = nodes[0];
                if (n.getUserObject() instanceof ImageData) {
                    Set ids = new HashSet(nodes.length);
                    ImageData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (ImageData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.IMAGES, root, 
                            model.getRootGroupID()));   
                    return;
                }
                UserNotifier un = 
                    TreeViewerAgent.getRegistry().getUserNotifier();  
                un.notifyInfo("Browse", "Can only browse images.");
            } else {
                TreeImageDisplay display = browser.getLastSelectedDisplay();
                if (display == null) return;
                if (display.getParentDisplay() == null &&
                    browser.getBrowserType() == Browser.IMAGES_EXPLORER) {
                    bus.post(new Browse(browser.getLeaves(), Browse.IMAGES, 
                            root, model.getRootGroupID()));   
                    return;
                }
                ho = display.getUserObject(); 
            }
        }

        if (ho instanceof ImageData) {
            ImageData data = (ImageData) ho;
            bus.post(new ViewImage(data.getId(), 
                    data.getDefaultPixels().getId() , data.getName()));
        } else if (ho instanceof DatasetData)
            bus.post(new Browse(((DatasetData) ho).getId(), Browse.DATASET, 
                     root, model.getRootGroupID())); 
        else if (ho instanceof ProjectData)
            bus.post(new Browse(((ProjectData) ho).getId(), Browse.PROJECT,
                    root, model.getRootGroupID())); 
        else if (ho instanceof CategoryData)
            bus.post(new Browse(((CategoryData) ho).getId(), Browse.CATEGORY, 
                    root, model.getRootGroupID())); 
        else if (ho instanceof CategoryGroupData)
            bus.post(new Browse(((CategoryGroupData) ho).getId(),
                          Browse.CATEGORY_GROUP, root, model.getRootGroupID())); 
    }
    
}
