/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;




//Java imports
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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
     * Returns the images contained in the passed node.
     * 
     * @param node The node to handle.
     * @return See above.
     */
    private Set getImageNodes(TreeImageDisplay node) 
    {
    	 LeavesVisitor visitor = new LeavesVisitor(model.getSelectedBrowser());
         node.accept(visitor);
         return visitor.getNodeIDs();
    }
    
    /**
     * Returns the constrain indicating to retrieve the values after
     * or before the time of reference.
     * 
     * @param index The index to control.
     * @return See above.
     */
    private int getTimeConstrain(int index)
    {
    	switch (index) {
			case TreeImageTimeSet.OTHER:
				return DataManagerView.BEFORE;
			case TreeImageTimeSet.MONTH:
			case TreeImageTimeSet.YEAR_BEFORE:
			//case TreeImageTimeSet.YEAR:
				return DataManagerView.PERIOD;
			default:
				return DataManagerView.AFTER;
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
        Rectangle bounds = model.getUI().getBounds();
        ExperimenterData exp = model.getSelectedExperimenter();
        if (hierarchyObject != null) ho = hierarchyObject;
        else {
            TreeImageDisplay[] nodes = browser.getSelectedDisplays();
            int length = nodes.length;
            if (browser.getBrowserType() == Browser.IMAGES_EXPLORER) {
            	if (length == 1) {
            		TreeImageDisplay display = browser.getLastSelectedDisplay();
                    if (display == null) return;
                    if (display instanceof TreeImageTimeSet) {
                    	if (display.containsImages()) {
                    		bus.post(new Browse(getImageNodes(display), 
                        			Browse.IMAGES, exp, bounds));   
                    	} else {
                    		TreeImageTimeSet time = (TreeImageTimeSet) display;
                    		int c = getTimeConstrain(time.getType());
                    		exp = browser.getNodeOwner(time);
                    		TimeRefObject ref = new TimeRefObject(exp.getId(), 
                    				time.getLowerTime(), time.getTime(), c);
                    		
                    		bus.post(new Browse(ref, exp, bounds));   
                    	}
                    	return;
                    } 
                    ho = display.getUserObject(); 
                    
            	} else { //more than one node.
            		TreeImageDisplay n = nodes[0];
            		if (n instanceof TreeImageTimeSet) {
            			Set ids = new HashSet();
            			for (int i = 0; i < nodes.length; i++) {
							ids.add(getImageNodes(nodes[i]));
						}
               		 bus.post(new Browse(ids, Browse.IMAGES, exp, bounds));   
                        return;
            		} else if (n.getUserObject() instanceof ImageData) {
                        Set<Long> ids = new HashSet<Long>(nodes.length);
                        ImageData data;
                        for (int i = 0; i < nodes.length; i++) {
                            data = (ImageData) nodes[i].getUserObject();
                            ids.add(new Long(data.getId()));
                        }
                        bus.post(new Browse(ids, Browse.IMAGES, exp, bounds));   
                        return;
                    }
            	}
            	
            }
            if (length > 1) {
                TreeImageDisplay n = nodes[0];
                if (n.getUserObject() instanceof ImageData) {
                    Set<Long> ids = new HashSet<Long>(nodes.length);
                    ImageData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (ImageData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.IMAGES, exp, bounds));   
                    return;
                } else if (n.getUserObject() instanceof DatasetData) {
                    Set<Long> ids = new HashSet<Long>(nodes.length);
                    DatasetData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (DatasetData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.DATASETS, exp, bounds));   
                    return;
                } else if (n.getUserObject() instanceof CategoryData) {
                    Set<Long> ids = new HashSet<Long>(nodes.length);
                    CategoryData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (CategoryData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.CATEGORIES, exp, bounds));   
                    return;
                } else if (n.getUserObject() instanceof ProjectData) {
                    Set<Long> ids = new HashSet<Long>(nodes.length);
                    ProjectData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (ProjectData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.PROJECTS, exp, bounds));   
                    return;
                } else if (n.getUserObject() instanceof CategoryGroupData) {
                    Set<Long> ids = new HashSet<Long>(nodes.length);
                    CategoryGroupData data;
                    for (int i = 0; i < nodes.length; i++) {
                        data = (CategoryGroupData) nodes[i].getUserObject();
                        ids.add(new Long(data.getId()));
                    }
                    bus.post(new Browse(ids, Browse.CATEGORY_GROUPS, exp, 
                    					bounds));   
                    return;
                }
            } else { // only one node
            	TreeImageDisplay display = browser.getLastSelectedDisplay();
                if (display == null) return;
                ho = display.getUserObject(); 
            }
        }

        if (ho instanceof ImageData) {
            ImageData data = (ImageData) ho;
            long pixelsID = -1;
            try {
            	pixelsID = data.getDefaultPixels().getId();
			} catch (Exception e) {
				UserNotifier un = 
						TreeViewerAgent.getRegistry().getUserNotifier();
				un.notifyInfo("View Image", "No pixels set associated to " +
											"this image.");
				return;
			}
            bus.post(new ViewImage(data.getId(), pixelsID, data.getName(), 
            				bounds));
        } else if (ho instanceof DatasetData)
            bus.post(new Browse(((DatasetData) ho).getId(), Browse.DATASET, 
                     exp, bounds)); 
        else if (ho instanceof ProjectData)
            bus.post(new Browse(((ProjectData) ho).getId(), Browse.PROJECT,
                    exp, bounds)); 
        else if (ho instanceof CategoryData)
            bus.post(new Browse(((CategoryData) ho).getId(), Browse.CATEGORY, 
                    exp, bounds)); 
        else if (ho instanceof CategoryGroupData)
            bus.post(new Browse(((CategoryGroupData) ho).getId(),
                          Browse.CATEGORY_GROUP, exp, bounds)); 
    }
    
}
