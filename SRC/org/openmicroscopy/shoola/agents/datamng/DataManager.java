/*
 * org.openmicroscopy.shoola.agents.datamng.DataManager
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.ds.st.PixelsDTO;
import org.openmicroscopy.ds.st.RepositoryDTO;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.agents.annotator.IconManager;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateImage;
import org.openmicroscopy.shoola.agents.events.datamng.ClassifyImage;
import org.openmicroscopy.shoola.agents.events.datamng.ShowProperties;
import org.openmicroscopy.shoola.agents.events.hiviewer.Browse;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

import pojos.ExperimenterData;
import pojos.PixelsData;

/** 
 * The data manager agent.
 * This agent manages and presents user's data.
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
public class DataManager
	implements Agent, AgentEventListener
{

    public static final String      FILTER_NAME = 
                    DataManagementService.FILTER_NAME;
    public static final String      FILTER_DATE = 
                    DataManagementService.FILTER_DATE;
    public static final String      FILTER_ANNOTATED = 
                    DataManagementService.FILTER_ANNOTATED;
    public static final String      FILTER_LIMIT = 
                    DataManagementService.FILTER_LIMIT;
    public static final String      GREATER = 
                    DataManagementService.FILTER_GREATER;
    public static final String      LESS = DataManagementService.FILTER_LESS;
    public static final String      CONTAIN = 
                    DataManagementService.FILTER_CONTAIN;
    public static final String      NOT_CONTAIN = 
                    DataManagementService.FILTER_NOT_CONTAIN;
    
    private static final int        THUMBNAIL_SIZE=100;
    
	/** Reference to the registry. */
	private Registry				registry;
	
	/** Reference to the GUI. */
	private DataManagerUIF			presentation;
	
	/** Reference to the control component. */
	private DataManagerCtrl     	control;
	
	/** 
	 * All user's projects. 
	 * That represents all user's data rooted by project objects. 
	 * That is, the whole project-dataset hierarchy for the current user.
	 */
	private Set           			projectSummaries;

	/** All user's datasets. */
	private Set					datasetSummaries;
    
	/** Creates a new instance. */
	public DataManager() {}
    
	/** Implemented as specified by {@link Agent}. */
	public void activate() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void terminate() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		control = new DataManagerCtrl(this);
		presentation = new DataManagerUIF(control, registry);
		datasetSummaries = new HashSet();
		projectSummaries = new HashSet();
        
        EventBus bus = registry.getEventBus();
        bus.register(this, ServiceActivationResponse.class);
        bus.register(this, ShowProperties.class);
        bus.register(this, ClassifyImage.class);
	}
	
	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate() { return true; }
    
    /** Return the GUI of this agent. */
    public DataManagerUIF getPresentation() { return presentation; }
    
    /**
     * Responds to an event fired trigger on the bus.
     * Listens to ViewDatasetInfo, ViewImageInfo events.
     * @see AgentEventListener#eventFired
     */
    public void eventFired(AgentEvent e)
    {
        if (e instanceof ServiceActivationResponse)
        	handleSAR((ServiceActivationResponse) e);
        else if (e instanceof ShowProperties) 
            handleShowProperties((ShowProperties) e);
        else if (e instanceof ClassifyImage)
            handleClassifyImage((ClassifyImage) e);
    }

	Registry getRegistry() { return registry; }
	
     /** Handle the classify Image event. */
    private void handleClassifyImage(ClassifyImage response)
    {
        if (response == null) return;
        //For now we bring the dataManager, no the best strategy.
        if (presentation == null) 
            presentation = new DataManagerUIF(control, registry);
        presentation.setFocusableWindowState(true);
        presentation.deIconify();
        control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
    }
    
    /** Handle the show properties event. */
    private void handleShowProperties(ShowProperties response)
    {
        if (response == null) return;
        if (presentation != null) {
            presentation.setFocusableWindowState(true);
            presentation.deIconify();
        }
        //Data retrieval now via OMERO: need to modify the code.
        pojos.DataObject object = response.getUserObject();
        if (object == null) return;
        pojos.DataObject target = retrieveData(object);
        control.showProperties(target, response.getParent());
    }
    
    
	/** Rebuild the Tree if the connection is succesfull. */
	private void handleSAR(ServiceActivationResponse response)
	{
		if (response.isActivationSuccessful() && presentation != null) 
			presentation.rebuildTree();
	}
    
    private pojos.DataObject retrieveData(pojos.DataObject target)
    {
        if (target instanceof pojos.DatasetData) {
            pojos.DatasetData dataset = (pojos.DatasetData) target;
            Set set = dataset.getImages();
            if (set == null) {
                HashSet id = new HashSet(1);
                id.add(new Integer(dataset.getId())); 
                try {
                    set = registry.getOmeroService().loadContainerHierarchy(
                            pojos.DatasetData.class, id, true);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                if (set != null) {
                    Iterator i = set.iterator();
                    while (i.hasNext()) {
                        dataset = (pojos.DatasetData) i.next();
                        break;
                    }
                }
                return dataset;
            }
        } else if (target instanceof pojos.CategoryData) {
            pojos.CategoryData category = (pojos.CategoryData) target;
            Set set = category.getImages();
            if (set == null) {
                HashSet id = new HashSet(1);
                id.add(new Integer(category.getId())); 
                try {
                    set = registry.getOmeroService().loadContainerHierarchy(
                            pojos.CategoryData.class, id, true);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                if (set != null) {
                    Iterator i = set.iterator();
                    while (i.hasNext()) {
                        category = (pojos.CategoryData) i.next();
                        break;
                    }
                }
                return category;
            }
        }
        return target;
    }
    
    //Tempo
    void showProperties(pojos.DataObject object, int index)
    {
        if (object == null) return;
        pojos.DataObject target = retrieveData(object);
        control.showPropertiesEditor(target, index);
    }
    
	/**
	 * Import a list of images into the specified dataset.
	 * 
	 * @param images			list of files to import.
	 * @param datasetID			id of the dataset to import into.
	 */
	void importImages(List images, int datasetID)
	{
		//EventBus eventBus = registry.getEventBus();
		//eventBus.post(new ImportImages(datasetID, images));
		registry.getUserNotifier().notifyInfo("Importer", 
			"not yet implemented"+images+" "+datasetID);
	}
	
    /** Refresh the classifications tree. */
    void refreshCategoryGroups()
    {
        if (presentation != null) presentation.rebuildCategoryGroupTree();
    }
    
    /** Refresh the categorySummary. */
    void refreshCategory(pojos.CategoryData data)
    {
        if (presentation != null) {
            try {
                presentation.refreshCategory(getImagesInCategory(data));
            } catch(DSAccessException dsae) {
                String s = "Can't retrieve the specified category.";
                registry.getLogger().error(this, s+" Error: "+dsae);
                registry.getUserNotifier().notifyError("Data Retrieval " +
                        "Failure", s, dsae);
            }
        }
    }
    
	/** Refresh the all tree. */
	void refresh()
	{
		projectSummaries.removeAll(projectSummaries);
        datasetSummaries.removeAll(datasetSummaries);
		if (presentation != null) presentation.rebuildTree();
	}
    
    /** Refresh the all tree. */
    void refresh(pojos.DatasetData ds)
    {
        if (presentation != null) presentation.refreshDataset(ds);
    }
    
	/**
	 * Return a list of {@link ImageSummary} objects owned by the 
     * current user but not contained in the specified dataset.
	 * 
	 * @param data    {@link DatasetData} object.
     * 
	 * @return See above.
	 */
	Set getImagesDiff(pojos.DatasetData data, Map filters, Map complexFilters)
	{
        Set imagesDiff = new HashSet();
        try {
            imagesDiff = getImportedImages(filters, complexFilters); 
            pojos.ImageData isg;
            Iterator i = imagesDiff.iterator();
            Set images = data.getImages();
            Iterator j;
            while (i.hasNext()) {
                isg = (pojos.ImageData) i.next();   
                j = images.iterator();
                while (j.hasNext()) {
                    if (((pojos.ImageData) j.next()).getId() == isg.getId())  
                        imagesDiff.remove(isg); 
                }
                
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        /*
		List imagesDiff = new ArrayList();
        try {
            imagesDiff = getImportedImages(filters, complexFilters);   
            Set images = data.getImages();
            pojos.ImageData isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (pojos.ImageData) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((pojos.ImageData) i.next()).getId() == isg.getId())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
		return imagesDiff;
        */
	}
	
    /**
     * Return a list of {@link ImageSummary} objects currently in the user's 
     * datasets but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    Set getImagesInUserDatasetsDiff(pojos.DatasetData data, List datasets,
            Map filters, Map complexFilters)
    {
        Set imagesDiff = new HashSet();
        try {
            if (datasets == null || datasets.size() == 0) return imagesDiff;
            imagesDiff = getImagesInDatasets(datasets, filters, complexFilters);   
            Set images = data.getImages();
            pojos.ImageData isg;
            Iterator i;
            Iterator j = imagesDiff.iterator();
            while (j.hasNext()) {
                isg = (pojos.ImageData) j.next();
                i = images.iterator();
                while (i.hasNext()) {
                    if (((pojos.ImageData) i.next()).getId() == isg.getId())  
                        imagesDiff.remove(isg); 
                }
            } 
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        
        return imagesDiff;
        /*
        Set imagesDiff = new HashSet();
        if (datasets == null || datasets.size() == 0) return HashSet();
        try {
            imagesDiff = getImagesInDatasets(datasets, filters, complexFilters);   
            Set images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
    /**
     * Return a list of {@link ImageSummary} objects owned by member of the 
     * user's group but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    Set getImagesInUserGroupDiff(pojos.DatasetData data, Map filters, 
                                Map complexFilters)
    {
        Set imagesDiff = new HashSet();
        try {
            imagesDiff = getGroupImages(filters, complexFilters);
            Set images = data.getImages();
            pojos.ImageData isg;
            Iterator i;
            Iterator j = imagesDiff.iterator();
            while (j.hasNext()) {
                isg = (pojos.ImageData) j.next();
                i = images.iterator();
                while (i.hasNext()) {
                    if (((pojos.ImageData) i.next()).getId() == isg.getId())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 

        return imagesDiff;
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getGroupImages(filters, complexFilters);   
            Set images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
    /**
     * Return a list of all {@link ImageSummary} objects 
     * but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    Set getImagesInSystemDiff(pojos.DatasetData data, Map filters, 
                                Map complexFilters)
    {
        Set imagesDiff = new HashSet();
        try {
            imagesDiff = getSystemImages(filters, complexFilters); 
            Set images = data.getImages();
            pojos.ImageData isg;
            Iterator i;
            Iterator j = imagesDiff.iterator();
            while (j.hasNext()) {
                isg = (pojos.ImageData) j.next();
                i = images.iterator();
                while (i.hasNext()) {
                    if (((pojos.ImageData) i.next()).getId() == isg.getId())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getSystemImages(filters, complexFilters);   
            Set images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
	/**
	 * Return the list of {@link DatasetSummary} objects that belong to the user
	 * but which are not linked to the specified project.
	 * 
	 * @param data		if of the project.
	 * @return See below.
	 */
	Set getDatasetsDiff(pojos.ProjectData data)
	{
        Set datasetsDiff = new HashSet();
        try {
            Set datasetsAll = getUserDatasets();
            Set datasets = data.getDatasets();
            pojos.DatasetData dsg;
            Iterator j = datasetsAll.iterator();
            Iterator i;
            while (j.hasNext()) {
                dsg = (pojos.DatasetData) j.next();
                i = datasets.iterator();
                datasetsDiff.add(dsg);
                while (i.hasNext()) {
                    if (((pojos.DatasetData) i.next()).getId() == dsg.getId()) {
                        datasetsDiff.remove(dsg);
                        break;
                    } 
                }   
            }  

        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's projects or images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return datasetsDiff;
        /*
        try {
            Set datasetsAll = getUserDatasets();
            Set datasets = data.getDatasets();
            pojos.DatasetData dsg;
            Iterator j = datasetsAll.iterator();
            Iterator i;
            while (j.hasNext()) {
                dsg = (pojos.DatasetData) j.next();
                i = datasets.iterator();
                datasetsDiff.add(dsg);
                while (i.hasNext()) {
                    if (((pojos.DatasetData) i.next()).getId() == dsg.getId()) {
                        datasetsDiff.remove(dsg);
                        break;
                    } 
                }   
            }  
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's projects or images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
		return datasetsDiff;
        */
	}
	
	/**
	 * Returns all projects which belong to the current user.
	 * Each project is linked to its datasets.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of {@link ProjectSummary} objects. 
	 */
	Set getUserProjects()
        throws DSAccessException
	{
        if (projectSummaries.size() == 0) {
            try { 
                OmeroPojoService os = registry.getOmeroService();
                projectSummaries = os.loadContainerHierarchy(
                        pojos.ProjectData.class, null, false);
            } catch(DSOutOfServiceException dsose) {
                ServiceActivationRequest 
                request = new ServiceActivationRequest(
                                    ServiceActivationRequest.DATA_SERVICES);
                registry.getEventBus().post(request);
            }

        }
        return projectSummaries;
        /*
		if (projectSummaries.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				//projectSummaries = dms.retrieveUserProjects();
                projectSummaries = dms.retrieveUserProjectsWithDAnnotations();
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
		return projectSummaries;
        */
	}
	
    /** 
     * Return the list of datasets used by the current user.
     * @return
     * @throws DSAccessException
     */
    Set getUsedDatasets()
        throws DSAccessException
    {
        Set projects = getUserProjects();
        Set datasets = new HashSet();
        if (projects == null || projects.size() == 0) return datasets;
        Map map = new HashMap();
        Iterator i = projects.iterator(), j, k;
        Set listDS;
        pojos.DatasetData ds;
        while (i.hasNext()) {
            listDS = ((pojos.ProjectData) i.next()).getDatasets();
            j = listDS.iterator();
            while (j.hasNext()) {
                ds = (pojos.DatasetData) j.next();
                map.put(new Integer(ds.getId()), ds);
            }
        }
        k = map.keySet().iterator();
        while (k.hasNext())
            datasets.add(map.get(k.next()));
        return datasets;
    }
    
	/**
	 * Returns all datasets which belong to the current user.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of {@link DatasetSummary} objects. 
	 */
	Set getUserDatasets()
        throws DSAccessException
	{
        if (datasetSummaries.size() == 0) {
            try { 
                OmeroPojoService os = registry.getOmeroService();
                return os.loadContainerHierarchy(pojos.DatasetData.class, null,
                                                    false);
            } catch(DSOutOfServiceException dsose) {    
                ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
                registry.getEventBus().post(request);
            } 
        }
        return datasetSummaries;
	}
	
	/**
	 * Returns the images imported by the current user..
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of {@link ImageSummary} objects. 
	 */
	Set getImportedImages(Map filters, Map complexFilters)
        throws DSAccessException
	{
        try { 
            OmeroPojoService os = registry.getOmeroService();
            return os.getUserImages();
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new HashSet();

        /*
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveUserImages(filters, complexFilters);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
		return new ArrayList();
        */
	}

    /**
     * Returns the images used by the current user.
     * <p>If an error occurs while trying to retrieve the user's data from 
     * OMEDS, the user gets notified and this method returns <code>null</code>.
     * </p>
     *
     * @return  A list of {@link ImageSummary} objects. 
     */
    Set getUsedImages()
        throws DSAccessException
    {
        try { 
            OmeroPojoService os = registry.getOmeroService();
            if (projectSummaries.size() == 0)  
                return os.getUserImages();
            Iterator i = projectSummaries.iterator();
            Set datasets;
            Iterator j;
            HashMap ids = new HashMap();
            Integer id;
            while (i.hasNext()) {
                datasets = ((pojos.ProjectData) i.next()).getDatasets();
                j = datasets.iterator();
                while (j.hasNext()) {
                    id = new Integer(((pojos.DatasetData) j.next()).getId());
                    ids.put(id, id);
                }
            }
            Iterator key = ids.keySet().iterator();
            Set datasetIDs = new HashSet(ids.size());
            while (key.hasNext())
                datasetIDs.add(key.next());
            return os.getImages(pojos.DatasetData.class, datasetIDs);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new HashSet();
        
        
        /*
        try { 
            DataManagementService dms = registry.getDataManagementService();
            //we didn't previously retrieve the Hierarchy data
            if (projectSummaries.size() == 0)  
                return dms.retrieveImagesInUserDatasets();
        
            Iterator i = projectSummaries.iterator();
            List datasets;
            Iterator j;
            HashMap ids = new HashMap();
            Integer id;
            while (i.hasNext()) {
                datasets = ((ProjectSummary) i.next()).getDatasets();
                j = datasets.iterator();
                while (j.hasNext()) {
                    id = new Integer(((DatasetSummary) j.next()).getID());
                    ids.put(id, id);
                }
            }
            Iterator key = ids.keySet().iterator();
            List datasetIDs = new ArrayList();
            while (key.hasNext())
                datasetIDs.add(key.next());
            return dms.retrieveImagesInUserDatasets(datasetIDs);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
        */
    }
    
    /** 
     * Retrieve the images contained in the list of datasets
     * 
     * @param datasets    list of datasetSummary.
     * @return list of {@link ImageSummary} objects contained in the datasets.
     */
    Set getImagesInDatasets(List datasets, Map filters, Map complexFilters)
        throws DSAccessException
    {
        try { 
            if (datasets == null || datasets.size() == 0) return new HashSet();
            OmeroPojoService os = registry.getOmeroService();
            Iterator i = datasets.iterator();
            HashSet datasetIDs = new HashSet(datasets.size());
            while (i.hasNext()) 
                datasetIDs.add(
                        new Integer(((pojos.DatasetData) i.next()).getId()));  
            return os.getImages(pojos.DatasetData.class, datasetIDs);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new HashSet();
        
        /*
        try { 
            DataManagementService dms = registry.getDataManagementService();
            Iterator i = datasets.iterator();
            List datasetIDs = new ArrayList();
            while (i.hasNext()) 
                datasetIDs.add(
                        new Integer(((DatasetSummary) i.next()).getID()));  
            return dms.retrieveImagesInUserDatasets(datasetIDs, filters,
                        complexFilters);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
        */
    }
    
    /**
     * Returns the images imported by the users who belong to the same group
     * as the current user.
     * <p>If an error occurs while trying to retrieve the user's data from 
     * OMEDS, the user gets notified and this method returns <code>null</code>.
     * </p>
     *
     * @return  A list of {@link ImageSummary} objects. 
     */
    Set getGroupImages(Map filters, Map complexFilters)
        throws DSAccessException
    {
        try { 
            OmeroPojoService os = registry.getOmeroService();
            return os.getUserImages();
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new HashSet();

        /*
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInUserGroup(filters, complexFilters);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
        */
    }
    
    /**
     * Returns the images currently in the system.
     * <p>If an error occurs while trying to retrieve the user's data from 
     * OMEDS, the user gets notified and this method returns <code>null</code>.
     * </p>
     *
     * @return  A list of {@link ImageSummary} objects. 
     */
    Set getSystemImages(Map filters, Map complexFilters)
        throws DSAccessException
    {
        try { 
            OmeroPojoService os = registry.getOmeroService();
            return os.getUserImages();
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new HashSet();

        /*
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInSystem(filters, complexFilters);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
        */
    }
    
	/**
	 * Retrieve a list of images in the specified dataset.
	 * 
	 * @param datasetID		Specified dataset id.
	 * @return list of image summary objects.
	 */
	Set getImages(int datasetID) 
        throws DSAccessException
	{
        try { 
            OmeroPojoService os = registry.getOmeroService();
            HashSet set = new HashSet(1);
            set.add(new Integer(datasetID));
            return os.getImages(pojos.DatasetData.class, set);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new HashSet();
        /*
		try { 
			DataManagementService dms = registry.getDataManagementService();
			//images = dms.retrieveImages(datasetID);
            return dms.retrieveImagesWithAnnotations(datasetID);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		}
		return new HashSet();	
        */
	}

    Image getThumbnail(pojos.ImageData data) 
    {
        BufferedImage thumbnail = null;
        PixelsData pxd = data.getDefaultPixels();
        int sizeX = THUMBNAIL_SIZE;
        int sizeY = THUMBNAIL_SIZE;
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;

        //TO REMOVE ASAP.
        Map map = new HashMap();
        map.put("id", new Integer(2));
        map.put("ImageServerURL", pxd.getImageServerURL());
        RepositoryDTO rep = new RepositoryDTO(map);
        map = new HashMap();
        map.put("Repository", rep);
        map.put("ImageServerID", new Long(pxd.getImageServerID()));
        PixelsDTO pixels = new PixelsDTO(map);
        try {
            thumbnail = registry.getPixelsService().getThumbnail(
                    pixels, sizeX, sizeY);
        } catch (Exception e) {} 
        return thumbnail;
    }
    
    private pojos.ProjectData transformPStoPojosPD(ProjectSummary ps)
    {
        OmeroPojoService os = registry.getOmeroService();
        pojos.ProjectData data = null;
        HashSet ids = new HashSet(1);
        ids.add(new Integer(ps.getID()));
        Set set = null;
        try {
            set = os.loadContainerHierarchy(pojos.ProjectData.class, ids,
                    false);
        } catch (Exception e) {
            
        }

        if (set == null || set.size() == 0) return data;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            data = (pojos.ProjectData) i.next();
            break;
        }
        return data;
    }
    
    private DatasetSummary transformDDToDs(pojos.DatasetData data)
    {
        DatasetSummary ds = new DatasetSummary();
        ds.setID(data.getId());
        ds.setName(data.getName());
        return ds;
    }
    
	/**
	 * Create a new project.
	 * 
	 * @param pd		project data object.
	 */
	void createProject(ProjectData pd)
	{
        try { 
            DataManagementService dms = registry.getDataManagementService();
            ProjectSummary ps = dms.createProject(pd); 
            pojos.ProjectData data = transformPStoPojosPD(ps);
            System.out.println("data " +data);
            if (projectSummaries.size() != 0) {
                if (data != null) {
                    projectSummaries.add(ps);   //local copy
                    presentation.addNewProjectToTree(data);   //update tree
                }
            } else {
                getUserProjects(); 
                presentation.rebuildTree();
            } 
            presentation.rebuildTree();
            //if everything went smoothly we removed the creation panel 
            //and display the property panel
            control.showProperties(data, DataManagerCtrl.FOR_HIERARCHY);
        } catch(DSAccessException dsae) {
            String s = "Can't create the project: "+pd.getName()+".";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
            control.showComponent(null, DataManagerCtrl.FOR_HIERARCHY);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 

        /*
		try { 
			DataManagementService dms = registry.getDataManagementService();
			ProjectSummary ps = dms.createProject(pd);
			if (projectSummaries.size() != 0) {
				projectSummaries.add(ps);	//local copy
				presentation.addNewProjectToTree(ps);	//update tree
			} else {
				getUserProjects(); 
				presentation.rebuildTree();
			} 
            //if everything went smoothly we removed the creation panel 
            //and display the property panel
            control.showProperties(ps, DataManagerCtrl.FOR_HIERARCHY);
		} catch(DSAccessException dsae) {
			String s = "Can't create the project: "+pd.getName()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
            control.showComponent(null, DataManagerCtrl.FOR_HIERARCHY);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
        */
	}
	
    private pojos.DatasetData transformDStoPojosDD(DatasetSummary ds)
    {
        OmeroPojoService os = registry.getOmeroService();
        pojos.DatasetData data = null;
        HashSet ids = new HashSet(1);
        ids.add(new Integer(ds.getID()));
        Set set = null;
        try {
            set = os.loadContainerHierarchy(pojos.DatasetData.class, ids,
                    false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        if (set == null || set.size() == 0) return data;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            data = (pojos.DatasetData) i.next();
            break;
        }
        return data;
    }
    
    private ProjectSummary transformPojosProjectDataToPS(pojos.ProjectData data)
    {
        ProjectSummary ps = new ProjectSummary();
        ps.setID(data.getId());
        ps.setName(data.getName());
        return ps;
    }
    
	/**
	 * Create a new dataset.
	 * 
	 * @param projects	list of project summaries, projects to which 
	 * 					the dataset will be added.
	 * @param images	list of image summaries, images in the new dataset.
	 * @param dd		dataset data object.
	 */
	void createDataset(List projects, List images, DatasetData dd)
	{
        try { 
            DataManagementService dms = registry.getDataManagementService();
            
            List p = new ArrayList();
            if (projects != null) {
                Iterator i = projects.iterator();
                while (i.hasNext()) {
                    p.add(transformPojosProjectDataToPS(
                            (pojos.ProjectData) i.next()));
                }
            }
            List imgs = new ArrayList();
            if (images != null) {
                Iterator i = images.iterator();
                while (i.hasNext()) {
                    p.add(transformPojosIDToIs(
                            (pojos.ImageData) i.next()));
                }
            }
            DatasetSummary ds = dms.createDataset(p, imgs, dd);
            pojos.DatasetData data = transformDStoPojosDD(ds);
            if (datasetSummaries.size() !=0 ) getUserDatasets();
            else datasetSummaries.add(data); //local copy.
            pojos.ProjectData pData;
            Set set = new HashSet(projects.size());
            for (int i = 0; i < p.size(); i++) {
                pData = (pojos.ProjectData) projects.get(i);
                pData.getDatasets().add(data);
                set.add(pData);
            }
            if (presentation.isTreeLoaded()) {
                if (projects != null ) 
                    presentation.addNewDatasetToTree(set); 
            } else  
                presentation.rebuildTree();
            //if everything went smoothly we removed the creation panel 
            //and display the property panel
            control.showProperties(data, DataManagerCtrl.FOR_HIERARCHY);
        } catch(DSAccessException dsae) {
            String s = "Can't create the dataset: "+dd.getName()+".";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
            control.showComponent(null, DataManagerCtrl.FOR_HIERARCHY);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        
        /*
		try { 
			DataManagementService dms = registry.getDataManagementService();
			DatasetSummary ds = dms.createDataset(projects, images, dd);
			if (datasetSummaries.size() !=0) getUserDatasets();
			else datasetSummaries.add(ds); //local copy.
			ProjectSummary ps;
			for (int i = 0; i < projects.size(); i++) {
				ps = (ProjectSummary) projects.get(i);
				ps.getDatasets().add(ds);	
			}
			if (presentation.isTreeLoaded()) {
				if (projects != null ) 
					presentation.addNewDatasetToTree(projects);	
			} else  
                presentation.rebuildTree();
			//if everything went smoothly we removed the creation panel 
            //and display the property panel
            control.showProperties(ds, DataManagerCtrl.FOR_HIERARCHY);
		} catch(DSAccessException dsae) {
			String s = "Can't create the dataset: "+dd.getName()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
            control.showComponent(null, DataManagerCtrl.FOR_HIERARCHY);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		}
        */
	}
	
	/**
	 * Update a specified project.
	 * 
	 * @param pd			data object to update.
	 * @param dsToRemove	List of datasetIds to remove from the project.
	 * @param dsToAdd		List of datasetIds to add to the project.
	 */
	void updateProject(pojos.ProjectData pd, List dsToRemove, List dsToAdd,
						boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
            List toRemove = new ArrayList(dsToRemove.size());
            Iterator i = dsToRemove.iterator();
            while (i.hasNext())
                toRemove.add(transformDDToDs((pojos.DatasetData) i.next()));
            
            List toAdd = new ArrayList(dsToAdd.size());
            i = dsToAdd.iterator();
            while (i.hasNext())
                toAdd.add(transformDDToDs((pojos.DatasetData) i.next()));
			dms.updateProject(transformPojosProjectData(pd), dsToRemove,
                                dsToAdd);
			//update the presentation and the project summary contained in the 
			//projectSummaries list accordingly
			updatePSList(pd);
			if (nameChange) presentation.updateProjectInTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update project", "The project has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the project: "+pd.getId()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 	
	}
	
    private ProjectData transformPojosProjectData(pojos.ProjectData pojo)
    {
        ProjectData project = new ProjectData();
        project.setID(pojo.getId());
        project.setName(pojo.getName());
        project.setDescription(pojo.getDescription());
        Set datasets = pojo.getDatasets();
        if (datasets == null) {
            Iterator i = datasets.iterator();
            List d = new ArrayList(datasets.size());
            while (i.hasNext()) {
                d.add(transformPojosDatasetDataToDS((pojos.DatasetData) i.next()));
            }
            project.setDatasets(d);
        }
        return project;
    }
    
    /**
	 * Update a specified dataset.
	 * 
	 * @param dd			data object to update.
	 * @param isToRemove	List of imageIds to remove from the dataset.
	 * @param isToAdd		List of imageIds to add to the dataset.
	 */
	void updateDataset(pojos.DatasetData dd, List isToRemove, List isToAdd,
					 boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
            List toRemove = new ArrayList(isToRemove.size());
            Iterator i = isToRemove.iterator();
            while (i.hasNext())
                toRemove.add(transformPojosIDToIs((pojos.ImageData) i.next()));
            
            List toAdd = new ArrayList(isToAdd.size());
            i = isToAdd.iterator();
            while (i.hasNext())
                toAdd.add(transformPojosIDToIs((pojos.ImageData) i.next()));
            
			dms.updateDataset(transformPojosDatasetData(dd), toRemove,
                                toAdd);
			//update the presentation and the dataset summary contained in the 
			//datasetSummaries list accordingly.
			if (datasetSummaries.size() != 0) updateDSList(dd);
			else updateDatasetInPS(dd);
			if (nameChange) presentation.updateDatasetInTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update dataset", "The dataset has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the dataset: "+dd.getId()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
	}
	
    private DatasetSummary transformPojosDatasetDataToDS(pojos.DatasetData pojo)
    {
        DatasetSummary dataset = new DatasetSummary();
        dataset.setID(pojo.getId());
        dataset.setName(pojo.getName());
        return dataset;
    }
    
    private DatasetData transformPojosDatasetData(pojos.DatasetData pojo)
    {
        DatasetData dataset = new DatasetData();
        dataset.setID(pojo.getId());
        dataset.setDescription(pojo.getDescription());
        dataset.setName(pojo.getName());
        Set images = pojo.getImages();
        if (images != null) {
            Iterator i = images.iterator();
            List imgs = new ArrayList(images.size());
            while (i.hasNext()) {
                imgs.add(transformPojosIDToIs((pojos.ImageData) i.next()));
            }
            dataset.setImages(imgs);
        }
        return dataset;
    }
    
	/**
	 * Update a specified image.
	 * 
	 * @param dd	Image data object.
	 */
	void updateImage(pojos.ImageData image, boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateImage(transformPojosImageData(image));
			if (nameChange) {
				synchImagesView(image);
			} 
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update image", "The image has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the image: "+image.getId()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 	
	}
	 
    private ImageSummary transformPojosIDToIs(pojos.ImageData pojo)
    {
        ImageSummary image = new ImageSummary();
        image.setID(pojo.getId());
        image.setDate(pojo.getCreated());
        image.setName(pojo.getName());
        return image;
    }
    
    private ImageData transformPojosImageData(pojos.ImageData pojo)
    {
        ImageData image = new ImageData();
        image.setID(pojo.getId());
        image.setCreated(pojo.getCreated());
        image.setName(pojo.getName());
        image.setDescription(pojo.getDescription());
        return image;
    }

    /** 
     * Post an event to browse the specified dataset.
     * 
     * @param object    DataObject corresponding either to a 
     *                  DatasetSummary, DatasetData.
     *                  
     */
	void browseDataset(pojos.DatasetData data)
	{
        if (data != null)
            registry.getEventBus().post(new Browse(data.getId(),
                                        Browse.DATASET)); 
	}

    /** 
     * Post an event to browse the specified project.
     * 
     * @param object    DataObject corresponding either to a 
     *                  ProjectSummary or ProjectData.
     *                  
     */
    void browseProject(pojos.ProjectData data)
    {
        if (data != null)
            registry.getEventBus().post(new Browse(data.getId(),
                                        Browse.PROJECT));
    }

    /** 
     * Post an event to browse the specified categoryGroup.
     * 
     * @param object    DataObject to browse.              
     */
    void browseCategoryGroup(pojos.CategoryGroupData data)
    {
        if (data != null)
            registry.getEventBus().post(
                    new Browse(data.getId(), Browse.CATEGORY_GROUP));
    }
    
    /** 
     * Post an event to browse the specified category.
     * 
     * @param object    DataObject to browse.
     *                  
     */
    void browseCategory(pojos.CategoryData data)
    {
        if (data != null)
            registry.getEventBus().post(
                    new Browse(data.getId(), Browse.CATEGORY));
    }
    
    //Post an event to bring the ZoomBrowser. ???
    void browseRoot()
    {
        
    }
    
	/**
	 * Posts a request to view the given pixels set within the image.
	 * 
	 * @param imageID	The id of the image containing the pixels set.
	 * @param pixelsID	The id of the pixels set.
	 * @param imageName	The name of the image to load.
	 */
	void viewImage(int imageID, int pixelsID, String imageName)
	{
		LoadImage request = new LoadImage(imageID, pixelsID, imageName);
		registry.getEventBus().post(request);	
	}

	/** Select the menuItem. */
	//void setMenuSelection(boolean b) { viewItem.setSelected(b); }
	
    void annotate(pojos.DataObject target)
    {
        EventBus eventBus = registry.getEventBus();
        if (target instanceof pojos.DatasetData) {
            pojos.DatasetData uO = (pojos.DatasetData) target;
            eventBus.post(new AnnotateDataset(uO.getId(), uO.getName()));
        } else if (target instanceof pojos.ImageData) {
            pojos.ImageData uO = (pojos.ImageData) target;
            eventBus.post(new AnnotateImage(uO.getId(), uO.getName(),
                    uO.getDefaultPixels().getId()));
        }      
    }

    /** 
     * Retrieves the CategoryGroup/Category without the images.
     * 
     * @return  List of CategoryData objects.
     * @throws DSAccessException
     */
    Set getAvailableGroups()
        throws DSAccessException
    {
        try { 
            OmeroPojoService os = registry.getOmeroService();
            return os.loadContainerHierarchy(pojos.CategoryGroupData.class, null,
                                            false); 
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new HashSet();
        
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveAvailableGroups();  
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /** Retrieve all categoryGroups. */
    Set getCategoryGroups()
        throws DSAccessException
    {
        try { 
            OmeroPojoService os = registry.getOmeroService();
            return os.loadContainerHierarchy(pojos.CategoryGroupData.class, null,
                                            true);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new HashSet();

        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveCategoryGroups(true, false);  
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /**
     * Retrieve the images classified in the specified category. 
     * 
     * @param data      The {@link CategoryData} object.
     * @return List     
     * @throws DSAccessException
     */
    pojos.CategoryData getImagesInCategory(pojos.CategoryData data)
        throws DSAccessException
    {
        return data;
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            CategoryData cd = sts.retrieveCategoryTree(data.getId(), true);
            //data.setClassifications(cd.getClassifications());
            return data;    
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return data;
        */
    }

    /** 
     * Return the images owned by the user but not contained in the
     * specified CategoryGroup.
     * 
     * @param group     corresponding data object.
     * @return  list of {@link ImageSummary}s.
     */
    Set retrieveImagesNotInCategoryGroup(pojos.CategoryGroupData group, Map filters, 
                    Map complexFilters)
        //throws DSAccessException
    {
        return new HashSet();
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesNotInCategoryGroup(group.getID(), filters, 
                                                complexFilters);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /** 
     * Retrieve all images not classified in the specified group. 
     * and contained in the specified datasets.
     * 
     * @param CategoryGroupData the specified group.
     * @param List  List of {@link DatasetSummary} objects.
     */
    Set retrieveImagesInUserDatasetsNotInCategoryGroup(pojos.CategoryGroupData group,
            List datasets, Map filters, Map complexFilters)
        //throws DSAccessException
    {
        return new HashSet();
        /*
        if (datasets == null || datasets.size() == 0) return new HashSet();
        try { 
            List ids = new ArrayList();
            Iterator i = datasets.iterator();
            while (i.hasNext())
                ids.add(new Integer(((DatasetSummary) i.next()).getID()));
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInUserDatasetsNotInCategoryGroup(group, 
                    ids, filters, complexFilters);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /** Retrieve all images not classified in the specified group. */
    Set retrieveImagesInUserGroupNotInCategoryGroup(pojos.CategoryGroupData group, 
            Map filters, Map complexFilters)
        //throws DSAccessException
    {
        return new HashSet();
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInUserGroupNotInCategoryGroup(group, 
                    filters, complexFilters);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /** Retrieve all images not classified in the specified group. */
    Set retrieveImagesInSystemNotInCategoryGroup(pojos.CategoryGroupData group, 
            Map filters, Map complexFilters)
        //throws DSAccessException
    {
        return new HashSet();
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInSystemNotInCategoryGroup(group, filters, 
                    complexFilters);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }
    
    /**
     * Retrieve images owned by the user but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    Set retrieveImagesDiffNotInCategoryGroup(pojos.CategoryData data,
                Map filters, Map complexFilters)
    {
        return new HashSet();
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesNotInCategoryGroup(
                        data.getCategoryGroup(), filters, complexFilters);   
            List images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
    /**
     * Retrieve images used by the current user but not already in the specified 
     * {@link CategoryData category}.
     * 
     * @param data  specified category.
     * @return See above.
     */
    Set retrieveImagesDiffInUserDatasetsNotInCategoryGroup(pojos.CategoryData data, 
            List datasets, Map filters, Map complexFilters)
    {
        return new HashSet();
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInUserDatasetsNotInCategoryGroup(
                        data.getCategoryGroup(), datasets, filters, 
                        complexFilters);   
            List images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
    /**
     * Retrieve images owned by members of the user's group
     * but not already in the specified {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    Set retrieveImagesDiffInUserGroupNotInCategoryGroup(pojos.CategoryData data, 
              Map filters, Map complexFilters)
    {
        return new HashSet();
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInUserGroupNotInCategoryGroup(
                        data.getCategoryGroup(), filters, complexFilters);   
            List images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }
    
    /**
     * Retrieve all images in system but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    Set retrieveImagesDiffInSystemNotInCategoryGroup(pojos.CategoryData data, 
              Map filters, Map complexFilters)
    {
        return new HashSet();
        /*
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInSystemNotInCategoryGroup(
                        data.getCategoryGroup(), filters, complexFilters);   
            List images = data.getImages();
            ImageSummary isg;
            Iterator i;
            for (int j = 0; j < imagesDiff.size(); j++) {
                isg = (ImageSummary) imagesDiff.get(j);
                i = images.iterator();
                while (i.hasNext()) {
                    if (((ImageSummary) i.next()).getID() == isg.getID())  
                        imagesDiff.remove(isg); 
                }
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
        return imagesDiff;
        */
    }   
    
    /** 
     * Retrieve all categories in the specified group, the categories
     * don't contain images already in the group. 
     */
    Set retrieveCategoriesNotInGroup(pojos.CategoryGroupData group)
        //throws DSAccessException
    {
        return new HashSet();
        /*
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveCategoriesNotInGroup(group);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
        */
    }

    private pojos.CategoryGroupData transformCGtoPojosCGD(CategoryGroupData
                                                            cg)
    {
        OmeroPojoService os = registry.getOmeroService();
        pojos.CategoryGroupData data = null;
        HashSet ids = new HashSet(1);
        ids.add(new Integer(cg.getID()));
        Set set = null;
        try {
            set = os.loadContainerHierarchy(pojos.CategoryGroupData.class, ids,
                    false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (set == null || set.size() == 0) return data;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            data = (pojos.CategoryGroupData) i.next();
            break;
        }
        return data;
    }
    
    private pojos.CategoryData transformGtoPojosCD(CategoryData c)
    {
        OmeroPojoService os = registry.getOmeroService();
        pojos.CategoryData data = null;
        HashSet ids = new HashSet(1);
        ids.add(new Integer(c.getID()));
        Set set = null;
        try {
            set = os.loadContainerHierarchy(pojos.CategoryData.class, ids,
                    false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (set == null || set.size() == 0) return data;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            data = (pojos.CategoryData) i.next();
            break;
        }
        return data;
    }
    
    /** Create a new category group. */
    void createCategoryGroup(CategoryGroupData data)
    {
        if (data == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            CategoryGroupData group = sts.createCategoryGroup(data);
            presentation.rebuildClassificationTree();
            if (group != null)
                control.showProperties(transformCGtoPojosCGD(group), 
                        DataManagerCtrl.FOR_CLASSIFICATION);
            else 
                control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
            
        } catch(DSAccessException dsae) {
            String s = "Can't create a new categoryGroup.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
            control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
    }
    
    /** Create a new category group. */
    void createCategory(CategoryData data, List images)
    {
        if (data == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            CategoryData category = sts.createCategory(data, images);
            presentation.rebuildClassificationTree();
            if (category != null) control.showProperties(
                    transformGtoPojosCD(category), 
                            DataManagerCtrl.FOR_CLASSIFICATION);
            else 
                control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
        } catch(DSAccessException dsae) {
            String s = "Can't create a new category.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
            control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
    }
     
    private CategoryGroupData transformPojoCGToCGroup(pojos.CategoryGroupData data)
    {
        CategoryGroupData group = new CategoryGroupData();
        group.setID(data.getId());
        group.setName(data.getName());
        group.setDescription(data.getDescription());
        return group;
    }
    
    /**Update an existing category group. */
    void updateCategoryGroup(pojos.CategoryGroupData data, List categoriesToAdd, 
            boolean nameChange)
    {
        if (data == null || categoriesToAdd == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateCategoryGroup(transformPojoCGToCGroup(data),
                            categoriesToAdd);
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update category group", "The specified group has " +
                    "now been updated.", im.getIcon(IconManager.SEND_TO_DB));
            if (nameChange) presentation.rebuildClassificationTree();
        } catch(DSAccessException dsae) {
            String s = "Can't create a new category.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        /*
        if (data == null || categoriesToAdd == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateCategoryGroup(data, categoriesToAdd);
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update category group", "The specified group has " +
                    "now been updated.", im.getIcon(IconManager.SEND_TO_DB));
            if (nameChange) presentation.rebuildClassificationTree();
        } catch(DSAccessException dsae) {
            String s = "Can't create a new category.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        */
    }
    
    private CategoryData transformPojosCToCategory(pojos.CategoryData data)
    {
        CategoryData category = new CategoryData();
        category.setID(data.getId());
        category.setName(data.getName());
        category.setDescription(data.getDescription());
        return category;
    }
    
    /**
     * Update the specified category.
     * 
     * @param data          The DataObject to update
     * @param imgsToRemove  List of image's ID to declassify.
     * @param imgsToAdd     List of image's ID to declassify.
     * @param nameChange
     */
    void updateCategory(pojos.CategoryData data, List imgsToRemove, List imgsToAdd, 
                        boolean nameChange)
    {
        if (data == null || imgsToRemove == null || imgsToAdd == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateCategory(transformPojosCToCategory(data), imgsToRemove, imgsToAdd);
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update category", "The specified category has " +
                    "now been updated.", im.getIcon(IconManager.SEND_TO_DB));
            if (nameChange) presentation.rebuildClassificationTree();
        } catch(DSAccessException dsae) {
            String s = "Can't update the specified category.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        /*
        if (data == null || imgsToRemove == null || imgsToAdd == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.updateCategory(data, imgsToRemove, imgsToAdd);
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update category", "The specified category has " +
                    "now been updated.", im.getIcon(IconManager.SEND_TO_DB));
            if (nameChange) presentation.rebuildClassificationTree();
        } catch(DSAccessException dsae) {
            String s = "Can't update the specified category.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        */
    }
    
    /** 
     * Update datasetSummary object in projectSummaries.
     * Method called when a dataset has been updated.
     * 
     * @param dd    Modified dataset data object.
     */
    private void updateDatasetInPS(pojos.DatasetData dd)
    {
        Iterator i = projectSummaries.iterator();
        Iterator j;
        pojos.ProjectData ps;
        pojos.DatasetData ds;
        while (i.hasNext()) {
            ps = (pojos.ProjectData) i.next();
            j = ps.getDatasets().iterator();
            while (j.hasNext()) {
                ds = (pojos.DatasetData) j.next();
                if (ds.getId() == dd.getId()) {
                    ds.setName(dd.getName());
                    break;
                }       
            }
        }
    }
    
    /** 
     * Update the corresponding dataset summary object contained in the 
     * datasetSummaries list. The method is called when a dataset has been 
     * updated.
     * @param dd    Modified dataset data object.
     * 
     */
    private void updateDSList(pojos.DatasetData dd)
    {
        Iterator i = datasetSummaries.iterator();
        pojos.DatasetData ds;
        while (i.hasNext()) {
            ds = (pojos.DatasetData) i.next();
            if (ds.getId() ==  dd.getId()) {
                ds.setName(dd.getName());
                break;
            }   
        }
    }
    
    /** Synchronize the 2 views displaying image data. */
    private void synchImagesView(pojos.ImageData is) 
    {
        presentation.updateImageInTree(is);
        presentation.updateImageInTable(is);
    }
    
    /** 
     * Update the corresponding project summary object contained in the 
     * projectSummaries list. The method is called when a project has been 
     * updated.
     * 
     * @param pd    Modified project data object.
     */
    private void updatePSList(pojos.ProjectData pd)
    {
        //TODO need to be modified.
        Iterator i = projectSummaries.iterator();
        pojos.ProjectData ps;
        while (i.hasNext()) {
            ps = (pojos.ProjectData) i.next();
            if (ps.getId() ==  pd.getId()) {
                ps.setName(pd.getName());
                ps.setDatasets(pd.getDatasets());
                break;
            }   
        }
    }

}
