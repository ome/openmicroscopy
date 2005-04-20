/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiViewer
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
import java.awt.Component;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateImage;
import org.openmicroscopy.shoola.agents.events.datamng.ClassifyImage;
import org.openmicroscopy.shoola.agents.events.datamng.ShowProperties;
import org.openmicroscopy.shoola.agents.events.hiviewer.BrowseCategory;
import org.openmicroscopy.shoola.agents.events.hiviewer.BrowseCategoryGroup;
import org.openmicroscopy.shoola.agents.events.hiviewer.BrowseDataset;
import org.openmicroscopy.shoola.agents.events.hiviewer.BrowseProject;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;

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
public class HiViewerAgent
    implements Agent, AgentEventListener
{

    /** Reference to the registry. */
    private Registry                registry;
    
    /** Reference to the control component. */
    private HiViewerCtrl            control;
    
    /** Creates a new instance. */
    public HiViewerAgent() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}

    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx)
    {
        registry = ctx;
        control = new HiViewerCtrl(this);
        EventBus bus = registry.getEventBus();
        bus.register(this, BrowseProject.class);
        bus.register(this, BrowseDataset.class);
        bus.register(this, BrowseCategory.class);
        bus.register(this, BrowseCategoryGroup.class);
    }

    /** Implemented as specified by {@link Agent}. */
    public boolean canTerminate() { return true; }

    /**
     * Responds to an event fired trigger on the bus.
     * Listens to BrowseProject, BrowseDataset, BrowseCategoryGroup, 
     * BrowseCategory.
     * @see AgentEventListener#eventFired
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof BrowseProject)
            handleBrowseProject((BrowseProject) e);
        else if (e instanceof BrowseDataset)
            handleBrowseDataset((BrowseDataset) e);
        else if (e instanceof BrowseCategoryGroup)
            handleBrowseCategoryGroup((BrowseCategoryGroup) e);
        else if (e instanceof BrowseCategory)
            handleBrowseCategory((BrowseCategory) e);
    }
    
    /** Handle browse project event. */
    private void handleBrowseProject(BrowseProject evt)
    {
        if (evt == null) return;
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        ProjectLoader loader = new ProjectLoader(this, hiViewer, 
                                                 evt.getProjectID());
        loader.load();
    }

    /** Handle browse dataset event. */
    private void handleBrowseDataset(BrowseDataset evt)
    {
        if (evt == null) return;
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        DatasetLoader loader = new DatasetLoader(this, hiViewer, 
                                                 evt.getDatasetID());
        loader.load();
    }
    
    /** Handle browse categoryGroup event. */
    private void handleBrowseCategoryGroup(BrowseCategoryGroup response)
    {
        if (response == null) return;
        browseCategoryGroup(response.getHierarchyObject());
    }
    
    /** Handle browse category event. */
    private void handleBrowseCategory(BrowseCategory response)
    {
        if (response == null) return;
        browseCategory(response.getHierarchyObject());
    }

    Registry getRegistry() { return registry; }
    
    HiViewerCtrl getControl() { return control; }
    
    /** Classify the selected image. */
    void classify(ImageSummary target)
    {
        registry.getEventBus().post(new ClassifyImage(target));
    }
    
    /** Post an event to annotate the specified DataObject. */
    void annotate(Object target)
    {
        EventBus eventBus = registry.getEventBus();
        if (target instanceof DatasetSummary) {
            DatasetSummary uO = (DatasetSummary) target;
            eventBus.post(new AnnotateDataset(uO.getID(), uO.getName()));
        } else if (target instanceof ImageSummary) {
            ImageSummary uO = (ImageSummary) target;
            eventBus.post(new AnnotateImage(uO.getID(), uO.getName(), 
                        (uO.getPixelsIDs())[0]));
        }    
    }
    
    /** Post an event to show the properties of the specified DataObject. */
    void showProperties(Object object, Component parent)
    {
        registry.getEventBus().post(new ShowProperties((DataObject) object, 
                                        parent));
    }
    
    /** 
     * Browse the specified dataset. This method is called when a view action
     * is posted by the HiViewer.
     * 
     * @param target    The selected dataset.
     */
    void browseDataset(DatasetSummaryLinked target)
    {
        Set topNodes = HiTranslator.transform(target);
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        presentation.createBrowserFor(topNodes, hiViewer);
    }
    
    /** 
     * Browse the specified project. This method is called when a view action
     * is posted by the HiViewer.
     * 
     * @param target    The selected project.
     */
    void browseProject(ProjectSummary target)
    {
        Set topNodes = HiTranslator.transform(target);
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        presentation.createBrowserFor(topNodes, hiViewer);
    }
    
    /** 
     * Browse the specified category
     * 
     * @param target    The selected category.
     */
    void browseCategory(CategoryData target)
    {
        Set topNodes = HiTranslator.transform(target);
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        presentation.createBrowserFor(topNodes, hiViewer);
    }
    
    /** 
     * Browse the specified categoryGroup
     * 
     * @param target    The selected categoryGroup.
     */
    void browseCategoryGroup(CategoryGroupData target)
    {
        Set topNodes = HiTranslator.transform(target);
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        Browser brw = presentation.createBrowserFor(topNodes, hiViewer);
    }
    
    /** Post an event to bring up the viewer. */
    void viewImage(ImageSummary target)
    {
        int[] pxSets = target.getPixelsIDs();
        LoadImage request = new LoadImage(target.getID(), pxSets[0], 
                            target.getName());
        registry.getEventBus().post(request);   
    }
    
    /** 
     * Retrieve how the set of images is organized 
     * in the CategoryGroup-Category-Images hierarchy
     * 
     * @param images    set of images.
     */
    void viewInCGCI(Set images)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        CGCILoader loader = new CGCILoader(this, hiViewer, images);
        loader.load();
    }
    
    /** 
     * Retrieve how the set of images is organized 
     * in the Project-Dataset-Images hierarchy
     * 
     * @param images    set of images.
     */
    void viewInPDI(Set images)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        PDILoader loader = new PDILoader(this, hiViewer, images);
        loader.load();
    }

}
