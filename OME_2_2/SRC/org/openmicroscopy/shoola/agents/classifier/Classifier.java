/*
 * org.openmicroscopy.shoola.agents.classifier.Classifier
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.shoola.agents.classifier.events.ClassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.ClassifyImages;
import org.openmicroscopy.shoola.agents.classifier.events.ImagesClassified;
import org.openmicroscopy.shoola.agents.classifier.events.LoadCategories;
import org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImages;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.ResponseEvent;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/**
 * The base agent for the Classification module.  The module handles the
 * creation of Classification attributes and Category types.  Other agents
 * can have their own UIs for representing the categories.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Classifier implements Agent, AgentEventListener
{
    private static final String CATEGORY = "Category";
    private static final String CATEGORY_GROUP = "CategoryGroup";
    private static final String CLASSIFICATION = "Classification";
    
    private Registry registry;
    
    private TopFrame topFrame;
    
    private List activeControls;
    
    public Classifier()
    {
        activeControls = new ArrayList();
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        // TODO Auto-generated method stub

    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.Agent#canTerminate()
     */
    public boolean canTerminate()
    {
        for(Iterator iter = activeControls.iterator(); iter.hasNext();)
        {
            CategoryCtrl ctrl = (CategoryCtrl)iter.next();
            if(!ctrl.canExit()) return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.Agent#setContext(org.openmicroscopy.shoola.env.config.Registry)
     */
    public void setContext(Registry ctx)
    {
        this.registry = ctx;
        topFrame = registry.getTopFrame();
        registry.getEventBus().register(this,LoadCategories.class);
        registry.getEventBus().register(this,ClassifyImage.class);
        registry.getEventBus().register(this,ClassifyImages.class);
        registry.getEventBus().register(this,ReclassifyImage.class);
        registry.getEventBus().register(this,ReclassifyImages.class);
    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.Agent#terminate()
     */
    public void terminate()
    {
        // TODO Auto-generated method stub

    }
    
    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.env.event.AgentEventListener#eventFired(org.openmicroscopy.shoola.env.event.AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if(e instanceof LoadCategories)
        {
            showCategoryDialog((LoadCategories)e);
        }
        else if(e instanceof ClassifyImage)
        {
            System.err.println("received ClassifyImage");
            ClassifyImage ci = (ClassifyImage)e;
            classifyImageNew(ci);
        }
        else if(e instanceof ClassifyImages)
        {
            System.err.println("received ClassifyImages");
            ClassifyImages ci = (ClassifyImages)e;
            classifyImageNew(ci);
        }
        else if(e instanceof ReclassifyImage)
        {
            System.err.println("received ReclassifyImage");
            ReclassifyImage ri = (ReclassifyImage)e;
            reclassify(ri);
        }
        else if(e instanceof ReclassifyImages)
        {
            System.err.println("received ReclassifyImages");
            ReclassifyImages ri = (ReclassifyImages)e;
            reclassify(ri);
        }
    }
    
    /**
     * Creates a new category group (set of phenotypes)
     * @param groupName
     * @param description
     * @param datasetID
     * @return
     */
    CategoryGroup createCategoryGroup(String groupName,
                                      String description,
                                      int datasetID)
    {
        CategoryGroup group = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            group = (CategoryGroup)sts.createAttribute(CATEGORY_GROUP,datasetID);
            group.setName(groupName);
            group.setDescription(description);
        }
        catch(DSAccessException dsae)
        {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Phenotype creation error",
                           "Unable to create the semantic type "+CATEGORY_GROUP,
                           dsae);
        }
        catch(DSOutOfServiceException dsoe)
        {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return group;
    }
    
    /**
     * Creates a new category within a CategoryGroup.
     * @param categoryName
     * @param description
     * @param datasetID
     * @return
     */
    Category createCategory(CategoryGroup parent,
                            String categoryName,
                            String description,
                            int datasetID)
    {
        Category category = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            category = (Category)sts.createAttribute(CATEGORY,datasetID);
            category.setCategoryGroup(parent);
            category.setName(categoryName);
            category.setDescription(description);
        }
        catch(DSAccessException dsae)
        {
            dsae.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Phenotype creation error",
                           "Unable to create the semantic type "+CATEGORY,
                           dsae);
        }
        catch(DSOutOfServiceException dsoe)
        {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return category;
    }
    
    Classification createClassification(Category category, int imageID)
    {
        Classification classification = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            classification =
                (Classification)sts.createAttribute(CLASSIFICATION,imageID);
            classification.setCategory(category);
            classification.setConfidence(new Float(1.0f));
        }
        catch(DSAccessException dsae)
        {
            dsae.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Phenotype creation error",
                           "Unable to create the semantic type "+CATEGORY,
                           dsae);
        }
        catch(DSOutOfServiceException dsoe)
        {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return classification;
    }
    
    boolean commitNewAttributes(List attributes)
    {
        if(attributes == null || attributes.size() == 0)
        {
            return false;
        }
        try
        {    
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateUserInputAttributes(attributes);
            return true;
        } catch(DSAccessException dsae) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Data Creation Failure", 
                "Unable to update the semantic type ", dsae);
            return false;
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            dsose.printStackTrace();
            registry.getEventBus().post(request);
            return false;
        }
    }
    
    boolean updateAttributes(List attributes)
    {
        if(attributes == null || attributes.size() == 0 ) return false;
        try {
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateAttributes(attributes);
            return true;
        } catch(DSAccessException dsae) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Data Creation Failure", 
                "Unable to update the semantic type ", dsae);
            dsae.printStackTrace();
            return false;
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            dsose.printStackTrace();
            registry.getEventBus().post(request);
            return false;
        }
    }
    
    public List getCategoryGroups(int datasetID)
    {
        List categoryGroups = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            categoryGroups =
                sts.retrieveDatasetAttributes(CATEGORY_GROUP,datasetID);
        }
        catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Could not retrieve category groups",
                           dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return categoryGroups;
    }
    
    // likely much faster to piece together who belongs to whom locally (with
    // all categories available) than over wicked expensive (currently) DB call
    public List getCategories(int datasetID)
    {
        List categories = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            categories =
                sts.retrieveDatasetAttributes(CATEGORY,datasetID);
        }
        catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Could not retrieve category groups",
                           dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return categories;
    }
    
    public List getClassifications(int imageID)
    {
        List classifications = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            classifications =
                sts.retrieveImageAttributes(CLASSIFICATION,imageID);
        }
        catch(DSAccessException dsa) {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Could not retrieve category groups",
                           dsa.getMessage(), dsa);
        } catch(DSOutOfServiceException dso) {
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return classifications;
    }
    
    public void classifyImageNew(ClassifyImage event)
    {
        if(event == null || event.getCategory() == null) return;
        Category category = event.getCategory();
        int imageID = event.getImageID();
        Classification c = createClassification(category,imageID);
        List newList = new ArrayList();
        newList.add(c);
        commitNewAttributes(newList);
        ImagesClassified response = new ImagesClassified(event);
        response.addClassification(c);
        registry.getEventBus().post(response);
    }
    
    public void classifyImageNew(ClassifyImages event)
    {
        if(event == null || event.getCategory() == null ||
           event.getImageIDs() == null)
        {
            return;
        }
        Category category = event.getCategory();
        int[] imageIDs = event.getImageIDs(); 
        List newList = new ArrayList();
        for(int i=0;i<imageIDs.length;i++)
        {
            System.err.println("adding classification ("+category.getName()+
                               ", "+imageIDs[i]+")");
            Classification c = createClassification(category,imageIDs[i]);
            newList.add(c);
            
            // temp workaround
            List tempList = new ArrayList();
            tempList.add(c);
            commitNewAttributes(tempList);
        }
        // see if this needs to be fixed commitNewAttributes(newList);
        ImagesClassified response = new ImagesClassified(event);
        for(int i=0;i<newList.size();i++)
        {
            response.addClassification((Classification)newList.get(i));
        }
        registry.getEventBus().post(response);
    }
    
    public void reclassify(ReclassifyImage event)
    {
        if(event == null || event.getClassification() == null) return;
        Classification updatedClassification = event.getClassification();
        List newList = new ArrayList();
        newList.add(updatedClassification);
        updateAttributes(newList);
        ImagesClassified response = new ImagesClassified(event);
        response.addClassification(updatedClassification);
        registry.getEventBus().post(response);
    }
    
    public void reclassify(ReclassifyImages event)
    {
        if(event == null || event.getClassifications() == null ||
           event.getClassifications().size() == 0)
        {
            return;
        }
        List updatedClassifications = event.getClassifications();
        List updatedList = new ArrayList();
        for(int i=0;i<updatedClassifications.size();i++)
        {
            updatedList.add(updatedClassifications.get(i));
        }
        updateAttributes(updatedList);
        ImagesClassified response = new ImagesClassified(event);
        for(int i=0;i<updatedClassifications.size();i++)
        {
            response.addClassification((Classification)updatedClassifications.get(i));
        }
        registry.getEventBus().post(response);
    }
    
    public void showCategoryDialog(LoadCategories requestEvent)
    {
        CategoryCtrl cc = new CategoryCtrl(this,requestEvent);
        activeControls.add(cc);
        CategoryUI ui = new CategoryUI(cc,registry);
        ui.show();
    }
    
    public void respondWithEvent(ResponseEvent re)
    {
        if(re != null)
        {
            registry.getEventBus().post(re);
        }
    }
    
    public void close(CategoryCtrl control)
    {
        activeControls.remove(control);
    }
}
