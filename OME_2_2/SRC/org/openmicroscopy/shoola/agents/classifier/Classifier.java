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
import org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImage;
import org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImages;
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
 * @version 2.2.1
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
    
    /**
     * Creates a Classifier agent.
     */
    public Classifier()
    {
        activeControls = new ArrayList();
    }
    
    /**
     * Component does not require activation at this time.
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        // nothing necessary
    }
    
    /**
     * Queries the agent to determine if the application can exit.  If there
     * is an operation in the classifier in progress, this method will return
     * false.  Otherwise, it will return true, and the agent can be shut down.
     * 
     * @return Whether or not the classifier is in a state of safe, expected exit.
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
    
    /**
     * Sets the application context of this agent and registers the agent to
     * listen for certain application events.
     * 
     * @param ctx The Registry class containing application context.
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
        
        // BUG 117 FIX
        registry.getEventBus().register(this,DeclassifyImage.class);
        registry.getEventBus().register(this,DeclassifyImages.class);
    }
    
    /**
     * A non-saving termination condition (which will only be triggered if
     * canTerminate() evaluates to true-- which will be false if there is
     * pending unsaved information)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#terminate()
     */
    public void terminate()
    {
        for(Iterator iter = activeControls.iterator(); iter.hasNext();)
        {
            CategoryCtrl ctrl = (CategoryCtrl)iter.next();
            ctrl.close();
        }
    }
    
    /**
     * Responds to events posted to the event bus.  The classifier should respond
     * to the following events:
     * 
     * LoadCategories
     * ClassifyImage
     * ClassifyImages
     * ReclassifyImage
     * ReclassifyImages
     * DeclassifyImage
     * DeclassifyImages
     * 
     * @param e The event to respond to.
     * @see org.openmicroscopy.shoola.env.event.AgentEventListener#eventFired(org.openmicroscopy.shoola.env.event.AgentEvent)
     * @see org.openmicroscopy.shoola.agents.classifier.events.LoadCategories
     * @see org.openmicroscopy.shoola.agents.classifier.events.ClassifyImage
     * @see org.openmicroscopy.shoola.agents.classifier.events.ClassifyImages
     * @see org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImage
     * @see org.openmicroscopy.shoola.agents.classifier.events.ReclassifyImages
     * @see org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImage
     * @see org.openmicroscopy.shoola.agents.classifier.events.DeclassifyImages
     */
    public void eventFired(AgentEvent e)
    {
        if(e instanceof LoadCategories)
        {
            showCategoryDialog((LoadCategories)e);
        }
        else if(e instanceof ClassifyImage)
        {
            ClassifyImage ci = (ClassifyImage)e;
            classifyImageNew(ci);
        }
        else if(e instanceof ClassifyImages)
        {
            ClassifyImages ci = (ClassifyImages)e;
            classifyImageNew(ci);
        }
        else if(e instanceof ReclassifyImage)
        {
            ReclassifyImage ri = (ReclassifyImage)e;
            reclassify(ri);
        }
        else if(e instanceof ReclassifyImages)
        {
            ReclassifyImages ri = (ReclassifyImages)e;
            reclassify(ri);
        }
        else if(e instanceof DeclassifyImage)
        {
            DeclassifyImage di = (DeclassifyImage)e;
            declassify(di);
        }
        else if(e instanceof DeclassifyImages)
        {
            DeclassifyImages di = (DeclassifyImages)e;
            declassify(di);
        }
    }
    
    /**
     * Creates a new category group (set of phenotypes) inside the database, and
     * returns a Java data object corresponding to the new record.
     * 
     * @param groupName The name of the category group.
     * @param description The description of the category group.
     * @param datasetID The ID of the dataset to bind the group to.
     * @return The CategoryGroup object (to be committed to the database)
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
     * Creates a new category within a CategoryGroup, issues a create in the
     * remote database, and returns a Java data transfer object corresponding to
     * the new record.
     * 
     * @param parent The parent CategoryGroup of the desired category.
     * @param categoryName The name of the category.
     * @param description A description of which images should be classified as
     *                    members of that category.
     * @param datasetID The dataset ID to bind the category to.
     * @return A newly-created Category object (to be committed to the database)
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
    
    /**
     * Creates a new Classification object, creates the record in the database, and
     * returns a Java data object corresponding to the new record.
     * 
     * @param category The category the classification binds the image to.
     * @param imageID The ID of the image to classify.
     * @return A new Classification record (to be committed to the DB)
     */
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
            classification.setValid(Boolean.TRUE);
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
    
    /**
     * Commits a list of newly created category groups, categories and
     * classifications to the database.  The create family of methods trigger
     * module executions at the database level but do not physically store the
     * semantic type objects as records; this operation seals the deal.
     * 
     * @param attributes The list of new attributes to commit to the database.
     * @return Whether or not the commit was successful.
     */
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
    
    /**
     * Updates the attributes in the database.  Every attribute in the list should
     * already have a database record; otherwise, the server will throw a remote
     * error.
     * 
     * @param attributes A list of updated attributes to commit.
     * @return Whether or not the update was successful.
     */
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
    
    /**
     * Returns a list of category groups that bound to the dataset with the
     * specified ID.
     * @param datasetID The ID of the dataset to filter category groups by.
     * @return A list of category groups associated with the specified dataset.
     */
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
    /**
     * Gets the categories bound to a specific dataset.
     * 
     * @param datasetID The ID of the dataset to retrieve categories from.
     * @return A list of Category object bound to the specified dataset.
     */
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
    
    /**
     * Gets all the classifications pertaining to a specified image, with
     * categories that are bound to the specified dataset.
     * @param imageID The ID of the image to retrieve classifications from.
     * @param datasetID The dataset used to filter classifications.
     * @return See above.
     */
    public List getClassifications(int imageID, int datasetID)
    {
        List classifications = null;
        try
        {
            SemanticTypesService sts = registry.getSemanticTypesService();
            List dummyList = new ArrayList();
            dummyList.add(new Integer(imageID));
            classifications =
                sts.retrieveImageClassifications(dummyList,datasetID);
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
    
    /**
     * Create a new classification in response to a ClassifyImage event.
     * @param event The parameters of the new classification.
     */
    void classifyImageNew(ClassifyImage event)
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
        respondWithEvent(response);
    }
    
    /**
     * Create a set of new classifications in response to a ClassifyImages event.
     * @param event The collection of new classifications.
     */
    void classifyImageNew(ClassifyImages event)
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
        respondWithEvent(response);
    }
    
    /**
     * Reclassifies a particular image in response to a ReclassifyImage event.
     * @param event The event to respond to.
     */
    public void reclassify(ReclassifyImage event)
    {
        if(event == null || event.getClassification() == null) return;
        Classification updatedClassification = event.getClassification();
        // if it was invalid before, set to valid (to be safe)
        updatedClassification.setValid(Boolean.TRUE);
        List newList = new ArrayList();
        newList.add(updatedClassification);
        updateAttributes(newList);
        ImagesClassified response = new ImagesClassified(event);
        response.addClassification(updatedClassification);
        respondWithEvent(response);
    }
    
    /**
     * Reclassifies several images in response to a ReclassifyImages event.
     * @param event The event to respond to.
     */
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
            Classification classification =
                (Classification)updatedClassifications.get(i);
            // if it was invalid before, set to valid (to be safe)
            classification.setValid(Boolean.TRUE);
            updatedList.add(classification);
        }
        updateAttributes(updatedList);
        ImagesClassified response = new ImagesClassified(event);
        for(int i=0;i<updatedList.size();i++)
        {
            response.addClassification((Classification)updatedList.get(i));
        }
        respondWithEvent(response);
    }
    
    /**
     * Declassifies an image in response to a DeclassifyImage event.
     * @param event The event to respond to.
     */
    public void declassify(DeclassifyImage event)
    {
        if(event == null || event.getClassification() == null)
        {
            return;
        }
        Classification invalidClassification = event.getClassification();
        invalidClassification.setValid(Boolean.FALSE);
        List newList = new ArrayList();
        newList.add(invalidClassification);
        updateAttributes(newList);
        ImagesClassified response = new ImagesClassified(event);
        response.addClassification(invalidClassification);
        respondWithEvent(response);
    }
    
    /**
     * Declassifies an image in response to a DeclassifyImages event.
     * @param event The event to respond to.
     */
    public void declassify(DeclassifyImages event)
    {
        if(event == null || event.getClassifications() == null ||
           event.getClassifications().size() == 0)
        {
            return;
        }
        List invalidClassifications = event.getClassifications();
        List updatedList = new ArrayList();
        for(int i=0;i<invalidClassifications.size();i++)
        {
            Classification classification =
                (Classification)invalidClassifications.get(i);
            classification.setValid(Boolean.FALSE);
            updatedList.add(classification);
        }
        updateAttributes(updatedList);
        ImagesClassified response = new ImagesClassified(event);
        for(int i=0;i<updatedList.size();i++)
        {
            response.addClassification((Classification)updatedList.get(i));
        }
        respondWithEvent(response);
    }
    
    /**
     * Prompt the edit/view categories dialog box in response to an event.
     * @param requestEvent The event to respond to.
     */
    void showCategoryDialog(LoadCategories requestEvent)
    {
        CategoryCtrl cc = new CategoryCtrl(this,requestEvent);
        activeControls.add(cc);
        CategoryUI ui = new CategoryUI(cc,registry);
        ui.show();
    }
    
    /**
     * Post a response event to the event bus.
     * @param re The event to place on the bus.
     */
    void respondWithEvent(ResponseEvent re)
    {
        if(re != null)
        {
            registry.getEventBus().post(re);
        }
    }
    
    /**
     * Close the edit/load categories control box.
     * @param control
     */
    void close(CategoryCtrl control)
    {
        activeControls.remove(control);
    }
}
