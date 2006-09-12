/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerFactory
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.TaskBar;

//Third-party libraries

//Application-internal dependencies

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
     * Returns the <code>windows</code> menu. 
     * 
     * @return See above.
     */
    static JMenu getWindowsMenu() { return singleton.windowsMenu; }
    
    /**
     * Returns a viewer to display the images.
     * 
     * @param ids       The images' ids.
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The Id of the root.
     * @return A {@link HiViewer} component for the collection of images.
     */
    public static HiViewer getImagesViewer(Set ids, Class rootLevel,
                                            long rootID)
    {
        HiViewerModel model = new ImagesModel(ids);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Project/Dataset/Image hierarchy
     * rooted by the specified Project.
     * 
     * @param projectID The id of the Project root node.
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The Id of the root.
     * @return A {@link HiViewer} component for the specified Project.
     */
    public static HiViewer getProjectViewer(long projectID, Class rootLevel,
                                            long rootID)
    {
        HiViewerModel model = new ProjectModel(projectID);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Dataset/Image hierarchy
     * rooted by the specified Dataset.
     * 
     * @param datasetID The id of the Dataset root node.
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The Id of the root.
     * @return A {@link HiViewer} component for the specified Dataset.
     */
    public static HiViewer getDatasetViewer(long datasetID, Class rootLevel,
                                            long rootID)
    {
        HiViewerModel model = new DatasetModel(datasetID);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Category Group/Category/Image hierarchy
     * rooted by the specified Category Group.
     * 
     * @param cgID      The id of the Category Group root node.
     * @param rootLevel The level of the hierarchy either 
     *                  <code>GroupData</code> or 
     *                  <code>ExperimenterData</code>.
     * @param rootID    The Id of the root.
     * @return A {@link HiViewer} component for the specified Category Group.
     */
    public static HiViewer getCategoryGroupViewer(long cgID, Class rootLevel,
                                                long rootID)
    {
        HiViewerModel model = new CategoryGroupModel(cgID);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display the Category/Image hierarchy
     * rooted by the specified Category.
     * 
     * @param categoryID    The id of the Category root node.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootID        The Id of the root.
     * @return A {@link HiViewer} component for the specified Category.
     */
    public static HiViewer getCategoryViewer(long categoryID, Class rootLevel,
                                            long rootID)
    {
        HiViewerModel model = new CategoryModel(categoryID);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display data trees in the Project/Dataset/Image 
     * hierarchy that contain the specified images.
     * 
     * @param images        The <code>ImageData</code> objects for the images
     *                      that are at the bottom of the tree.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootID        The Id of the root.        
     * @return A {@link HiViewer} component for the specified images.
     */
    public static HiViewer getPDIViewer(Set images, Class rootLevel,
                                        long rootID)
    {
        HiViewerModel model = new HierarchyModel(images, 
                								HiViewer.PDI_HIERARCHY);
        model.setRootLevel(rootLevel, rootID);
        return singleton.getViewer(model);
    }
    
    /**
     * Returns a viewer to display data trees in the 
     * Category Group/Category/Image hierarchy that contain
     * the specified images.
     * 
     * @param images        The <code>ImageData</code> objects for the images
     *                      that are at the bottom of the tree.
     * @param rootLevel     The level of the hierarchy either 
     *                      <code>GroupData</code> or 
     *                      <code>ExperimenterData</code>.
     * @param rootID        The Id of the root.             
     * @return A {@link HiViewer} component for the specified images.
     */
    public static HiViewer getCGCIViewer(Set images, Class rootLevel, 
                                         long rootID)
    {
        HiViewerModel model = new HierarchyModel(images,
                                            HiViewer.CGCI_HIERARCHY);
        model.setRootLevel(rootLevel, rootID);
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
    private Set     viewers;
    
    
    /** The windows menu. */
    private JMenu   windowsMenu;
    
    /** Creates a new instance. */
    private HiViewerFactory() 
    {
        viewers = new HashSet();
        windowsMenu = new JMenu("HIViewer Window");
        TaskBar tb = ImViewerAgent.getRegistry().getTaskBar();
        tb.addToMenu(TaskBar.WINDOW_MENU, windowsMenu);
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
            if (model.isSameDisplay(comp.getModel())) return comp;
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
    }

}
