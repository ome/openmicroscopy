/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerFactory
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.ui.TaskBar;

import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Factory to create {@link HiViewer} components.
 * This class keeps track of all {@link HiViewer} instances that have been
 * created and are not yet {@link HiViewer#DISCARDED discarded}. A new
 * component is only created if none of the <i>tracked</i> ones is already
 * displaying the given hierarchy.  Otherwise, the existing component is
 * recycled.
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
public class HiViewerFactory
    implements ChangeListener
{

    /** The sole instance. */
    private static final HiViewerFactory  singleton = new HiViewerFactory();
    
    
    /** 
     * Returns all the {@link HiViewer} components that this factory is
     * currently tracking.
     * 
     * @return The set of currently tracked viewers. 
     */
    static Set getViewers() { return singleton.viewers; }
    
    /** 
     * Returns the <code>window</code> menu. 
     * 
     * @return See above.
     */
    static JMenu getWindowMenu() { return singleton.windowMenu; }
    
    /**
     * Returns <code>true</code> is the {@link #windowMenu} is attached 
     * to the <code>TaskBar</code>, <code>false</code> otherwise.
     *
     * @return See above.
     */
    static boolean isWindowMenuAttachedToTaskBar()
    {
        return singleton.isAttached;
    }
    
    /** Attaches the {@link #windowMenu} to the <code>TaskBar</code>. */
    static void attachWindowMenuToTaskBar()
    {
        if (isWindowMenuAttachedToTaskBar()) return;
        TaskBar tb = HiViewerAgent.getRegistry().getTaskBar();
        tb.addToMenu(TaskBar.WINDOW_MENU, singleton.windowMenu);
        singleton.isAttached = true;
    }
    
    /**
     * Returns a viewer to display the images.
     * 
     * @param ids       	The images' ids.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getImagesViewer(Set ids, ExperimenterData exp)
    {
        HiViewerModel model = new ImagesModel(ids);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the datasets.
     * 
     * @param ids       	The datasets' ids.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getDatasetsViewer(Set<Long> ids, ExperimenterData 
    										exp)
    {
        HiViewerModel model = new DatasetModel(ids);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the categories.
     * 
     * @param ids       	The categories' ids.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getCategoriesViewer(Set<Long> ids, ExperimenterData 
												exp)
    {
        HiViewerModel model = new CategoryModel(ids);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the projects.
     * 
     * @param ids       	The projects' ids.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getProjectsViewer(Set<Long> ids, ExperimenterData 
												exp)
    {
        HiViewerModel model = new ProjectModel(ids);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the category groups.
     * 
     * @param ids       	The projects' ids.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getCategoryGroupsViewer(Set<Long> ids, 
    									ExperimenterData exp)
    {
        HiViewerModel model = new CategoryGroupModel(ids);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Project/Dataset/Image hierarchy
     * rooted by the specified Project.
     * 
     * @param projectID 	The id of the Project root node.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the specified Project.
     */
    public static HiViewer getProjectViewer(long projectID, ExperimenterData 
											exp)
    {
        HiViewerModel model = new ProjectModel(projectID);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Dataset/Image hierarchy
     * rooted by the specified Dataset.
     * 
     * @param datasetID 	The id of the Dataset root node.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the specified Dataset.
     */
    public static HiViewer getDatasetViewer(long datasetID, ExperimenterData 
											exp)
    {
        HiViewerModel model = new DatasetModel(datasetID);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Category Group/Category/Image hierarchy
     * rooted by the specified Category Group.
     * 
     * @param cgID      	The id of the Category Group root node.
     * @param exp 			The selected experimenter.
     * @param userGroupID 	The ID of the selected group for the current user.
     * @return A {@link HiViewer} component for the specified Category Group.
     */
    public static HiViewer getCategoryGroupViewer(long cgID, ExperimenterData 
												exp)
    {
        HiViewerModel model = new CategoryGroupModel(cgID);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Category/Image hierarchy
     * rooted by the specified Category.
     * 
     * @param categoryID    The id of the Category root node.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the specified Category.
     */
    public static HiViewer getCategoryViewer(long categoryID, ExperimenterData 
											exp)
    {
        HiViewerModel model = new CategoryModel(categoryID);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Category/Image hierarchy
     * rooted by the specified image.
     * 
     * @param imageID   The id of the image.
     * @param exp		The selected experimenter.
     * @return A {@link HiViewer} component for the specified Category.
     */
    public static HiViewer getImageToCategoriesViewer(long imageID, 
    											ExperimenterData  exp)
    {
        HiViewerModel model = new ImageToCategoriesModel(imageID);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the images acquired during a period
     * of time.
     * 
     * @param timeRef   The object storing time information.
     * @param exp		The selected experimenter.
     * @return A {@link HiViewer} component for the specified Category.
     */
    public static HiViewer getImagePerDateViewer(TimeRefObject timeRef, 
    											ExperimenterData  exp)
    {
        HiViewerModel model = new ImagePerDateModel(timeRef);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display data trees in the Project/Dataset/Image 
     * hierarchy that contain the specified images.
     * 
     * @param images        The <code>ImageData</code> objects for the images
     *                      that are at the bottom of the tree.
     * @param exp 			The selected experimenter.
     * @return A {@link HiViewer} component for the specified images.
     */
    public static HiViewer getPDIViewer(Set<ImageData> images, ExperimenterData 
											exp)
    {
        HiViewerModel model = new HierarchyModel(images, 
                								HiViewer.PDI_HIERARCHY);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display data trees in the 
     * Category Group/Category/Image hierarchy that contain
     * the specified images.
     * 
     * @param images        The <code>ImageData</code> objects for the images
     *                      that are at the bottom of the tree.
     * @param exp 			The selected experimenter.     
     * @return A {@link HiViewer} component for the specified images.
     */
    public static HiViewer getCGCIViewer(Set<ImageData> images, ExperimenterData 
										exp)
    {
        HiViewerModel model = new HierarchyModel(images,
                                            HiViewer.CGCI_HIERARCHY);
        model.setRootLevel(exp);
        return singleton.getViewer(model);
    }
    
    /**
     * Creates a new {@link HiViewer} component after the given 
     * <code>master</code>.
     * The new component will be handling the same hierarchy as the
     * <code>master</code>, but the data will be reloaded.
     * 
     * @param master The viewer to use for creating a new one of this kind.
     *               Mustn't be <code>null</code>.
     * @return A new viewer created after the <code>master</code>.
     */
    public static HiViewer reinstantiate(HiViewer master)
    {
        return singleton.copy(master);
    }
    
    /** All the tracked components. */
    private Set<HiViewer>     	viewers;
     
    /** The windows menu. */
    private JMenu   			windowMenu;
    
    /** 
     * Indicates if the {@link #windowMenu} is attached to the 
     * <code>TaskBar</code>.
     */
    private boolean isAttached;
    
    /** Creates a new instance. */
    private HiViewerFactory() 
    {
        viewers = new HashSet<HiViewer>();
        isAttached = false;
        windowMenu = new JMenu("HiViewers");
    }
    
    /**
     * Creates or recycles a viewer component for the specified 
     * <code>model</code>.
     * 
     * @param model The component's Model.
     * @return A {@link HiViewer} for the specified <code>model</code>.  
     */
    private HiViewer getViewer(HiViewerModel model)
    {
        Iterator v = viewers.iterator();
        HiViewerComponent comp;
        while (v.hasNext()) {
            comp = (HiViewerComponent) v.next();
            if (model.isSameDisplay(comp.getModel())) {
            	comp.refresh(); //refresh the view.
            	return comp;
            }
        }
        comp = new HiViewerComponent(model);
        comp.initialize();
        comp.addChangeListener(this);
        viewers.add(comp);
        return comp;
    }
    
    /**
     * Replaces the <code>master</code> with a new component instantiated
     * after the <code>master</code>.
     * 
     * @param master The viewer to use for creating a new one of this kind.
     *               Mustn't be <code>null</code>.
     * @return A new viewer created after the <code>master</code>.
     */
    private HiViewer copy(HiViewer master)
    {
        if (master == null) throw new NullPointerException("No master.");
        HiViewerComponent comp = (HiViewerComponent) master, newComp;
        HiViewerModel model = comp.getModel();
        newComp = new HiViewerComponent(model.reinstantiate());
        newComp.initialize();
        newComp.addChangeListener(this);
        viewers.add(comp);
        return newComp;
    }
    
    /**
     * Removes a viewer from the {@link #viewers} set when it is
     * {@link HiViewer#DISCARDED discarded}. 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */ 
    public void stateChanged(ChangeEvent ce)
    {
        HiViewerComponent comp = (HiViewerComponent) ce.getSource(); 
        if (comp.getState() == HiViewer.DISCARDED) viewers.remove(comp);
        if (viewers.size() == 0) {
        	TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
            tb.removeFromMenu(TaskBar.WINDOW_MENU, windowMenu);
            isAttached = false;
        }
    }

}
