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
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
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
        bus.register(this, Browse.class);
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
        if (e instanceof Browse)
            handleBrowse((Browse) e);
    }
    
    /** Handle browse project event. */
    private void handleBrowse(Browse evt)
    {
        if (evt == null) return;
        browse(evt.getEventIndex(), evt.getHierarchyObjectID());
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
     * Browse the specified element.
     * 
     * @param eventIndex one of the constant defined by the {@link Browser}
     * class event.
     * 
     * @param id    id of the dataObject to browse.
     */
    void browse(int eventIndex, int id)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        HiLoader loader = null;
        switch (eventIndex) {
            case Browse.PROJECT:
                loader = new ProjectLoader(this, hiViewer, id);
                break;
            case Browse.DATASET:
                loader = new DatasetLoader(this, hiViewer, id);
                break;
            case Browse.CATEGORY_GROUP:
                loader = new CategoryGroupLoader(this, hiViewer, id);
                break;
            case Browse.CATEGORY:
                loader = new CategoryLoader(this, hiViewer, id);
                break;             
        }
        if (loader != null) loader.load();
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
     * 1.   In the CategoryGroup/Category/Images hierarchy 
     *      if index = HiViewerCtrl.VIEW_IN_CGI.
     * 2.   in the Project/Dataset/Images hierarchy 
     *      if index = HiViewerCtrl.VIEW_IN_PDI.
     * 
     * @param images    set of images.
     * @param index     hierarch index.
     */
    void viewHierarchy(Set images, int index)
    {
        HiViewerUIF presentation = HiViewerUIF.getInstance(control);
        HiViewer hiViewer = presentation.createHiViewer();
        HiLoader loader = null;
        switch (index) {
            case HiViewerCtrl.VIEW_IN_PDI:
                loader = new CGCILoader(this, hiViewer, images); break;
            case HiViewerCtrl.VIEW_IN_CGCI:
                loader = new PDILoader(this, hiViewer, images);
        }
        if (loader != null) loader.load();
    }

}
