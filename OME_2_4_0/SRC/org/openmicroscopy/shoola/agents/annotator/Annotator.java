/*
 * org.openmicroscopy.shoola.agents.annotator.Annotator
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
 
package org.openmicroscopy.shoola.agents.annotator;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateImage;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Model and agent for the Annotator set of widgets.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * after code by 
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">
 *              jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2
 */
public class Annotator 
	implements Agent, AgentEventListener
{
	
    public static final int             SAVE = 0, SAVEWITHRS = 1;
    
    /** Annotation constants. */
    public static final int             DATASET = 0, IMAGE = 1;
	
    private static final String         MSG_CREATED = 
                                        "The annotation has been created.";
    
    private static final String         MSG_UPDATE = 
                                        "The annotation has been updated.";
    
    private static final String         MSG_DELETE = 
                                        "The annotation has been deleted.";
    
    /** Reference to the {@link RenderingControl}. */
    private RenderingControl            renderingControl;
    
    /** Reference to the {@link Registry}. */
    private Registry                    registry;
    
    /** Reference to the control of this agent. */
    private AnnotatorCtrl               control;
    
    /** Reference to the view of this agent. */
    private AnnotatorUIF                presentation;  
    
    private int                         viewImageID;
    
    /** ID of the pixels set of the image we annotate. */
    private int                         pixelsID;
    
    /** One the Annotation contants defined above. */
    private int                         annotationIndex;
    
    /** 
     * Map of annotations, key: ownerID, value: list of annotations made
     * by the owner.
     */
    private Map                         annotationsMap;
    
    /** Utility map to link index and owner ID. */
    private Map                         ownersMap;
    
    /** ID of the image or dataset to annotate. */
    private int                         annotatedImageID, annotatedDatasetID;
    
    /** Name of the image or dataset to annotate. */
    private String                      objectName;
    
    private int                         maxOwner;
    
    private int                         userIndex;
    
    /** Implemented as specified by {@link Agent}. */
    public void activate() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void terminate() {}
    
    /** Implemented as specified by {@link Agent}. */
    public void setContext(Registry ctx)
    {
        this.registry = ctx;
        pixelsID = -1;
        control = new AnnotatorCtrl(this);
        EventBus bus = registry.getEventBus();
        bus.register(this, AnnotateImage.class);
        bus.register(this, AnnotateDataset.class);
        bus.register(this, ImageLoaded.class);
    }

    /** Implemented as specified by {@link Agent}. */
    public boolean canTerminate() { return true; }

    /** Implemented as specified by {@link AgentListener}. */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof AnnotateImage) 
            handleAnnotateImage((AnnotateImage) e);
        else if (e instanceof AnnotateDataset)
            handleAnnotateDataset((AnnotateDataset) e);
        else if (e instanceof ImageLoaded) 
            handleImageLoaded((ImageLoaded) e);
    }

    Registry getRegistry() { return registry; }
    
    /** 
     * View the image at the specified z-section and timepoint. 
     * 
     * @param z     z-section.
     * @param t     timepoint
     */
    void viewImage(int z, int t)
    {
        if (annotationIndex == IMAGE) {
            loadImage();
            if (z != AnnotationData.DEFAULT && t != AnnotationData.DEFAULT)
                renderImage(z, t);   
            else renderImage();
        }
    }
    
    /** 
     * Invoke when we want to view the image and 
     * the annotation hasn't been created. 
     */
    void viewImage()
    {
        if (annotationIndex == IMAGE) {
            loadImage();
            //Agent listens to ImageLoaded event, this implies that 
            //the renderingContol != null iff not modal dialog
            renderImage(); 
        }
    }
    
    /**
     * Posts a request to view all images in the specified dataset.
     */   
    void viewDataset()
    {
        LoadDataset request = new LoadDataset(annotatedDatasetID);
        registry.getEventBus().post(request);   
    }
    
    /**
     * Update the specified annotation.
     * 
     * @param data  annotation to update.
     */
    void update(AnnotationData data, int saveIndex)
    {
        String title = "";
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            switch (annotationIndex) {
                case DATASET:
                    title = "Update dataset annotation";
                    sts.updateDatasetAnnotation(data, annotatedDatasetID);
                    //Eventually post a DatasetAnnotation event.
                    break;
                case IMAGE:
                    title = "Update image annotation";
                    setDataToSave(data, saveIndex);
                    sts.updateImageAnnotation(data, annotatedImageID);
                    break;
            }
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo(title, MSG_UPDATE, 
                        im.getIcon(IconManager.SEND_TO_DB));
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        close();
    }
    
    /** 
     * Create a new annotation. 
     * 
     * @param annotation    text of the annotation.
     */
    void create(String annotation, int saveIndex)
    {
        String title = "";
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            switch (annotationIndex) {
                case DATASET:
                    title = "Create dataset annotation";
                    sts.createDatasetAnnotation(annotatedDatasetID, annotation);
                    //Eventually post a ImageAnnotation event.
                    break;
                case IMAGE:
                    title = "Create image annotation";
                    int theZ = AnnotationData.DEFAULT;
                    int theT = AnnotationData.DEFAULT;
                    if (renderingControl != null && saveIndex == SAVEWITHRS &&
                        annotatedImageID == viewImageID) {
                        theT = renderingControl.getDefaultT();
                        theZ = renderingControl.getDefaultZ();
                        renderingControl.saveCurrentSettings();
                    }
                    sts.createImageAnnotation(annotatedImageID, annotation, 
                                                theZ, theT);;
                    break;
            }
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo(title, MSG_CREATED, 
                            im.getIcon(IconManager.SEND_TO_DB));
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        close();
    }
    
    /** 
     * Delete the specified annotation.
     * 
     * @param data  annotation to delete.
     */
    void delete(AnnotationData data)
    {
        String title = "";
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            switch (annotationIndex) {
                case DATASET:
                    title = "Delete dataset annotation";
                    sts.removeDatasetAnnotation(data);
                    break;
                case IMAGE:
                    title = "Delete image annotation";
                    sts.removeImageAnnotation(data);
                    break;
            }
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo(title, MSG_DELETE, 
                            im.getIcon(IconManager.SEND_TO_DB));
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        close();
    }

    /** Close the widget and reset the map.*/
    void close()
    {
        if (presentation != null) {
            presentation.dispose();
            presentation.setVisible(false);
            presentation = null;
            control.setPresentation(null);
        }
        annotationsMap = null;
        maxOwner = -1;
        annotatedImageID = -1;
        annotatedDatasetID = -1;
    }
    
    void bringToFront()
    {
        if (presentation != null) presentation.toFront();
    }
    
    /** Return the annotations made by the specified user. */
    List getOwnerAnnotation(int index)
    { 
        if (index == maxOwner) {
            Iterator i = annotationsMap.keySet().iterator();
            ArrayList all = new ArrayList();
            List l;
            while (i.hasNext()) {
                l = (List) annotationsMap.get(i.next());
                all.addAll(l);
            }
            return all;
        }
        Integer ownerID = (Integer) ownersMap.get(new Integer(index));
        if (ownerID == null) return new ArrayList();    //empty list
        return (List) annotationsMap.get(ownerID);
    }
    
    int getUserIndex() { return userIndex; }
    
    int getAnnotationIndex() { return annotationIndex; }
    
    /** Handle the event @see ImageLoaded. */
    private void handleImageLoaded(ImageLoaded response)
    {
        renderingControl = response.getProxy();  
        LoadImage request = (LoadImage) response.getACT();
        viewImageID = request.getImageID();
        if (viewImageID != annotatedImageID) close();
    }
    
    /** Handle the event @see AnnotateImage. */
    private void handleAnnotateImage(AnnotateImage response)
    {
        if (annotatedImageID == response.getID()) {
            bringToFront();
            return;
        }
        close();
        annotationIndex = IMAGE;
        annotatedImageID = response.getID();
        pixelsID = response.getPixelsID();
        objectName = response.getName();
        //retrieve the annotation associated to the image.
        if (getImageAnnotations(response.getID()) != null)
            showAnnotationDialog();
    }
    
    /** Handle the event @see AnnotateDataset. */
    private void handleAnnotateDataset(AnnotateDataset response)
    {
        if (annotatedDatasetID == response.getID()) {
            bringToFront();
            return;
        }
        close();
        annotationIndex = DATASET;
        annotatedDatasetID = response.getID();
        objectName = response.getName();
        //retrieve the annotation associated to the dataset
        if (getDatasetAnnotations(response.getID()) != null) 
            showAnnotationDialog();
    }

    /**
     * Retrieve all the annotations of the specified image.
     * 
     * @param imageID     ID of the image.
     * @return 
     */
    private Map getImageAnnotations(int imageID)
    {
        annotationsMap = null;
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            annotationsMap = sts.getImageAnnotations(imageID);  
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return annotationsMap;
    }
      
    /**
     * Retrieve all the annotations of the specified dataset.
     * 
     * @param datasetID     ID of the dataset.
     * @return 
     */
    private Map getDatasetAnnotations(int datasetID)
    {
        annotationsMap = null;
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            annotationsMap = sts.getDatasetAnnotations(datasetID);  
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return annotationsMap;
    }
    
    /** Retrieve the user's id. */
    private UserDetails retrieveUserDetails()
    {
        UserDetails ud = null;
        try {
            DataManagementService dms = registry.getDataManagementService();
            ud = dms.getUserDetails();  
        } catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error", dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return ud;
    }

    /** Bring up the dialog. */
    private void showAnnotationDialog()
    {
        //Retrieve user ID
        UserDetails ud = retrieveUserDetails();
        if (ud == null) return;
        String[] owners = new String[annotationsMap.size()];
        Iterator i = (annotationsMap.keySet().iterator());
        Integer id;
        int index = 0;
        ownersMap = new HashMap();
        int selectedIndex = -1;
        String name = "";
        while (i.hasNext()) {
            id = (Integer) i.next();
            name = ((AnnotationData) 
                ((List) annotationsMap.get(id)).get(0)).getOwnerLastName();
            if (ud.getUserID() == id.intValue()) {
                selectedIndex = index;
                name = ud.getUserLastName();
            }
            owners[index] = name;
            ownersMap.put(new Integer(index), id);
            index++;
        }
        //Determine the list.
        String[] annotators = null;
        if (selectedIndex == -1) {  //user not in the list.
            if (owners.length >= 1) {   //at least one
                annotators = new String[owners.length+2];
                for (int j = 0; j < owners.length; j++) 
                    annotators[j] = owners[j];
                annotators[owners.length] = ud.getUserLastName();
                annotators[owners.length+1] = "All";
                selectedIndex = owners.length;
                maxOwner = owners.length+1;
            } else {
                annotators = new String[1];
                annotators[0] = ud.getUserLastName();
                selectedIndex = 0;
            }
        } else {    // user in the list
            if (owners.length >= 2) {   //at least two
                annotators = new String[owners.length+1];
                for (int j = 0; j < owners.length; j++) 
                    annotators[j] = owners[j];
                annotators[owners.length] = "All";
                maxOwner = owners.length;
            } else {
                annotators = new String[owners.length];
                for (int j = 0; j < owners.length; j++) 
                    annotators[j] = owners[j];
            }
        }
        userIndex = selectedIndex;
        presentation = new AnnotatorUIF(control, objectName, annotators, 
                            selectedIndex, getOwnerAnnotation(selectedIndex));
        control.setPresentation(presentation);
        UIUtilities.centerAndShow(presentation);
    }

    
    /** Post an event to load the specific event. */
    private void loadImage()
    {
        LoadImage request = new LoadImage(annotatedImageID, pixelsID, 
                                            objectName);
        registry.getEventBus().post(request);   
    }
    
    /** 
     * Post an event to render the image at the specified section and timepoint.
     * 
     * @param z     z-section.
     * @param t     timepoint.
     */
    private void renderImage(int z, int t)
    {
        PlaneDef def = new PlaneDef(PlaneDef.XY, t);
        def.setZ(z);
        renderingControl.setDefaultZ(z);
        renderingControl.setDefaultT(t);
        registry.getEventBus().post(new RenderImage(pixelsID, def));  
    }
    
    /** 
     * Post an event to render the image at the current section and timepoint. 
     */
    private void renderImage()
    {
        PlaneDef def = new PlaneDef(PlaneDef.XY, 
                            renderingControl.getDefaultT());
        def.setZ(renderingControl.getDefaultZ());
        registry.getEventBus().post(new RenderImage(pixelsID, def));  
    }
    
    
    /** 
     * Create an ImageAnnotationData object to save in the DB. 
     * Note that if we previously save theZ and theT, and we only want 
     * to update the annotation, we have to press the save button.
     */
    private void setDataToSave(AnnotationData data, int saveIndex)
    {
        if (saveIndex == SAVEWITHRS && renderingControl != null && 
                annotatedImageID == viewImageID)
        {
            data.setTheT(renderingControl.getDefaultT());
            data.setTheZ(renderingControl.getDefaultZ());
            renderingControl.saveCurrentSettings();
        }
    }
}
