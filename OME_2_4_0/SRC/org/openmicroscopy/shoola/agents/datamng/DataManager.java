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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.IconManager;
import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateDataset;
import org.openmicroscopy.shoola.agents.events.annotator.AnnotateImage;
import org.openmicroscopy.shoola.agents.events.datamng.ViewImageInfo;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationResponse;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

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
	private List           			projectSummaries;

	/** All user's datasets. */
	private List					datasetSummaries;
    
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
		datasetSummaries = new ArrayList();
		projectSummaries = new ArrayList();
        
        EventBus bus = registry.getEventBus();
        bus.register(this, ViewImageInfo.class);
        bus.register(this, ServiceActivationResponse.class);
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
        if (e instanceof ViewImageInfo)
            control.showProperties(((ViewImageInfo) e).getImageInfo());
        else if (e instanceof ServiceActivationResponse)
        	handleSAR((ServiceActivationResponse) e);
    }

	Registry getRegistry() { return registry; }
	
	/** Rebuild the Tree if the connection is succesfull. */
	void handleSAR(ServiceActivationResponse response)
	{
		if (response.isActivationSuccessful() && presentation != null) 
			presentation.rebuildTree();
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
    void refreshCategory(CategorySummary cs)
    {
        if (presentation != null) presentation.refreshCategory(cs);
    }
    
	/** Refresh the all tree. */
	void refresh()
	{
		projectSummaries.removeAll(projectSummaries);
        datasetSummaries.removeAll(datasetSummaries);
		if (presentation != null) presentation.rebuildTree();
	}
    
    /** Refresh the all tree. */
    void refresh(DatasetSummary ds)
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
	List getImagesDiff(DatasetData data)
	{
		List imagesDiff = new ArrayList();
        try {
            imagesDiff = getImportedImages();   
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
	}
	
    /**
     * Return a list of {@link ImageSummary} objects currently in the user's 
     * datasets but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    List getImagesInUserDatasetsDiff(DatasetData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getUsedImages();   
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
    }
    
    /**
     * Return a list of {@link ImageSummary} objects owned by member of the 
     * user's group but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    List getImagesInUserGroupDiff(DatasetData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getGroupImages();   
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
    }
    
    /**
     * Return a list of all {@link ImageSummary} objects 
     * but not contained in the specified dataset.
     * 
     * @param data    {@link DatasetData} object.
     * 
     * @return See above.
     */
    List getImagesInSystemDiff(DatasetData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getSystemImages();   
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
    }
    
	/**
	 * Return the list of {@link DatasetSummary} objects that belong to the user
	 * but which are not linked to the specified project.
	 * 
	 * @param projectID		if of the project.
	 * @return
	 */
	List getDatasetsDiff(ProjectData data)
	{
		List datasetsDiff = new ArrayList();
        try {
            List datasetsAll = getUserDatasets();
            List datasets = data.getDatasets();
            DatasetSummary dsg;
            Iterator j = datasetsAll.iterator();
            Iterator i;
            while (j.hasNext()) {
                dsg = (DatasetSummary) j.next();
                i = datasets.iterator();
                datasetsDiff.add(dsg);
                while (i.hasNext()) {
                    if (((DatasetSummary) i.next()).getID() == dsg.getID()) {
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
	List getUserProjects()
        throws DSAccessException
	{
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
	}
	
	/**
	 * Returns all datasets which belong to the current user.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of {@link DatasetSummary} objects. 
	 */
	List getUserDatasets()
        throws DSAccessException
	{
		if (datasetSummaries.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				datasetSummaries = dms.retrieveUserDatasets();
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
	List getImportedImages()
        throws DSAccessException
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveUserImages();
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
		return new ArrayList();
	}

    /**
     * Returns the images used by the current user.
     * <p>If an error occurs while trying to retrieve the user's data from 
     * OMEDS, the user gets notified and this method returns <code>null</code>.
     * </p>
     *
     * @return  A list of {@link ImageSummary} objects. 
     */
    List getUsedImages()
        throws DSAccessException
    {
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
    List getGroupImages()
        throws DSAccessException
    {
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInUserGroup();
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
    }
    
    /**
     * Returns the images currently in the system.
     * <p>If an error occurs while trying to retrieve the user's data from 
     * OMEDS, the user gets notified and this method returns <code>null</code>.
     * </p>
     *
     * @return  A list of {@link ImageSummary} objects. 
     */
    List getSystemImages()
        throws DSAccessException
    {
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInSystem();
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new ArrayList();
    }
    
	/**
	 * Retrieve a list of images in the specified dataset.
	 * 
	 * @param datasetID		Specified dataset id.
	 * @return list of image summary objects.
	 */
	List getImages(int datasetID) 
        throws DSAccessException
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			//images = dms.retrieveImages(datasetID);
            return dms.retrieveImagesWithAnnotations(datasetID);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		}
		return new ArrayList();	
	}
	
	/**
	 * Retrieve a specified project.
	 * 
	 * @param projectID	Specified project id.
	 * @return projectData object.
	 */
	ProjectData getProject(int projectID)
	    throws DSAccessException
    {
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveProject(projectID);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
		return new ProjectData();
	}
	
	/**
	 * Retrieve a specified dataset.
	 * 
	 * @param datasetID		Specified dataset id.
	 * @return projectData object.
	 */
	DatasetData getDataset(int datasetID)
        throws DSAccessException
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveDataset(datasetID);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
		return new DatasetData();
	}
	
	/**
	 * Retrieve a specified image.
	 * 
	 * @param imageID		Specified image id.
	 * @return imageData object.
	 */
	ImageData getImage(int imageID)
        throws DSAccessException
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveImage(imageID);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
		return new ImageData();
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
			ProjectSummary project = dms.createProject(pd);
			if (projectSummaries.size() != 0) {
				projectSummaries.add(project);	//local copy
				presentation.addNewProjectToTree(project);	//update tree
			} else {
				getUserProjects(); 
				presentation.rebuildTree();
			} 
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Create project", "A new project "+pd.getName()+
                        " has been created.", 
                        im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't create the project: "+pd.getName()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
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
			DatasetSummary dataset = dms.createDataset(projects, images, dd);
			if (datasetSummaries.size() !=0) getUserDatasets();
			else datasetSummaries.add(dataset); //local copy.
			ProjectSummary ps;
			for (int i = 0; i < projects.size(); i++) {
				ps = (ProjectSummary) projects.get(i);
				ps.getDatasets().add(dataset);	
			}
			if (presentation.isTreeLoaded()) {
				if (projects != null ) 
					presentation.addNewDatasetToTree(projects);	
			} else  presentation.rebuildTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Create dataset", "A new dataset "+dd.getName()+
                        " has been created.", 
                        im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't create the dataset: "+dd.getName()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		}
	}
	
	/**
	 * Update a specified project.
	 * 
	 * @param pd			data object to update.
	 * @param dsToRemove	List of datasetIds to remove from the project.
	 * @param dsToAdd		List of datasetIds to add to the project.
	 */
	void updateProject(ProjectData pd, List dsToRemove, List dsToAdd,
						boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateProject(pd, dsToRemove, dsToAdd);
			//update the presentation and the project summary contained in the 
			//projectSummaries list accordingly
			updatePSList(pd);
			if (nameChange) presentation.updateProjectInTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update project", "The project has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the project: "+pd.getID()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 	
	}
	
	/**
	 * Update a specified dataset.
	 * 
	 * @param dd			data object to update.
	 * @param isToRemove	List of imageIds to remove from the dataset.
	 * @param isToAdd		List of imageIds to add to the dataset.
	 */
	void updateDataset(DatasetData dd, List isToRemove, List isToAdd,
					 boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateDataset(dd, isToRemove, isToAdd);
			//update the presentation and the dataset summary contained in the 
			//datasetSummaries list accordingly.
			if (datasetSummaries.size() != 0) updateDSList(dd);
			updateDatasetInPS(dd);
			if (nameChange) presentation.updateDatasetInTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update dataset", "The dataset has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the dataset: "+dd.getID()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
	}
		
	/**
	 * Update a specified image.
	 * 
	 * @param dd	Image data object.
	 */
	void updateImage(ImageData id, boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateImage(id);
			if (nameChange) {
				ImageSummary is = new ImageSummary();
				is.setDate(id.getCreated());
				is.setName(id.getName());
				is.setID(id.getID());
				synchImagesView(is);
			} 
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Update image", "The image has been updated.", 
                    im.getIcon(IconManager.SEND_TO_DB));
		} catch(DSAccessException dsae) {
			String s = "Can't update the image: "+id.getID()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 	
	}
	
	/**
	 * Update a specified image.
	 * 
	 * @param is	Image summary object.
	 */
	void updateImage(ImageSummary is)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateImage(is);
			synchImagesView(is);
		} catch(DSAccessException dsae) {
			String s = "Can't update the image: "+is.getID()+".";
			registry.getLogger().error(this, s+" Error: "+dsae);
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
										ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 	
	}
	
    /** Retrieve the current user last name. */
    String getUserName()
    {
        String name = "";
        try { 
            DataManagementService dms = registry.getDataManagementService();
            UserDetails details = dms.getUserDetails();
            name = details.getUserFirstName()+" "+details.getUserLastName();
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user details.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }  
        return name;
    }
	
	/**
	 * Posts a request to view all images in the specified dataset.
	 * 
	 * @param datasetID		The id of the dataset.
	 */
	void viewDataset(int datasetID)
	{
		LoadDataset request = new LoadDataset(datasetID);
		registry.getEventBus().post(request);	
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
	
	/** Post an {@link AnnotateDataset} event. */
	void annotateDataset(int id, String name)
	{
		registry.getEventBus().post(new AnnotateDataset(id, name));
	}
	
	/** Post an {@link AnnotateImage} event. */
	void annotateImage(int id, String name, int pixelsID) 
	{
		registry.getEventBus().post(new AnnotateImage(id, name, pixelsID));
	}
    
    /** Retrieve all categoryGroups. */
    List getCategoryGroups()
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveCategoryGroups();  
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
    }

    /** 
     * Return the images owned by the user but not contained in the
     * specified CategoryGroup.
     * 
     * @param group     corresponding data object.
     * @return  list of {@link ImageSummary}s.
     */
    List retrieveImagesNotInCategoryGroup(CategoryGroupData group)
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            //return sts.retrieveImagesNotInGroup(group);
            return sts.retrieveImagesNotInCategoryGroup(group.getID());
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
    }
    
    /** Retrieve all images not classified in the specified group. */
    List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group)
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInUserDatasetsNotInCategoryGroup(group);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
    }
    
    /** Retrieve all images not classified in the specified group. */
    List retrieveImagesInUserGroupNotInCategoryGroup(CategoryGroupData group)
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInUserGroupNotInCategoryGroup(group);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
    }
    
    /** Retrieve all images not classified in the specified group. */
    List retrieveImagesInSystemNotInCategoryGroup(CategoryGroupData group)
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveImagesInSystemNotInCategoryGroup(group);
        } catch(DSOutOfServiceException dsose) {
            ServiceActivationRequest 
            request = new ServiceActivationRequest(
                                ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        }
        return new ArrayList();
    }
    
    /**
     * Retrieve images owned by the user but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffNotInCategoryGroup(CategoryData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesNotInCategoryGroup(
                        data.getCategoryGroup());   
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
    }
    
    /**
     * Retrieve images used by the current user but not already in the specified 
     * {@link CategoryData category}.
     * 
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInUserDatasetsNotInCategoryGroup(CategoryData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInUserDatasetsNotInCategoryGroup(
                        data.getCategoryGroup());   
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
    }
    
    /**
     * Retrieve images owned by members of the user's group
     * but not already in the specified {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInUserGroupNotInCategoryGroup(CategoryData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInUserGroupNotInCategoryGroup(
                        data.getCategoryGroup());   
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
    }
    
    /**
     * Retrieve all images in system but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInSystemNotInCategoryGroup(CategoryData data)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = retrieveImagesInSystemNotInCategoryGroup(
                        data.getCategoryGroup());   
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
    }   
    
    /** 
     * Retrieve all categories in the specified group, the categories
     * don't contain images already in the group. 
     */
    List retrieveCategoriesNotInGroup(CategoryGroupData group)
        throws DSAccessException
    {
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
    }
    
    CategoryData getCategoryData(int id)
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            //cd = sts.retrieveCategory(id);
            return sts.retrieveCategoryWithIAnnotations(id);
        } catch(DSOutOfServiceException dsose) {    
            ServiceActivationRequest request = new ServiceActivationRequest(
                                        ServiceActivationRequest.DATA_SERVICES);
            registry.getEventBus().post(request);
        } 
        return new CategoryData();
    }

    /** Create a new category group. */
    void createCategoryGroup(CategoryGroupData data)
    {
        if (data == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            sts.createCategoryGroup(data);
            presentation.rebuildClassificationTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Create categoryGroup", 
                    "A new categoryGroup has now been created.", 
                    im.getIcon(IconManager.SEND_TO_DB));
        } catch(DSAccessException dsae) {
            String s = "Can't create a new categoryGroup.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
                                                    dsae);
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
            sts.createCategory(data, images);
            presentation.rebuildClassificationTree();
            UserNotifier un = registry.getUserNotifier();
            IconManager im = IconManager.getInstance(registry);
            un.notifyInfo("Create category", "A new category has now been " +
                    "created.", im.getIcon(IconManager.SEND_TO_DB));
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
    }
     
    /**Update an existing category group. */
    void updateCategoryGroup(CategoryGroupData data, List categoriesToAdd, 
            boolean nameChange)
    {
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
    }
    
    /**Update an existing category group. */
    void updateCategory(CategoryData data, List imgsToRemove, List imgsToAdd, 
                        boolean nameChange)
    {
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
    }
    
    /** 
     * Update datasetSummary object in projectSummaries.
     * Method called when a dataset has been updated.
     * 
     * @param dd    Modified dataset data object.
     */
    private void updateDatasetInPS(DatasetData dd)
    {
        Iterator i = projectSummaries.iterator();
        Iterator j;
        ProjectSummary ps;
        DatasetSummary ds;
        while (i.hasNext()) {
            ps = (ProjectSummary) i.next();
            j = ps.getDatasets().iterator();
            while (j.hasNext()) {
                ds = (DatasetSummary) j.next();
                if (ds.getID() == dd.getID()) {
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
    private void updateDSList(DatasetData dd)
    {
        Iterator i = datasetSummaries.iterator();
        DatasetSummary ds;
        while (i.hasNext()) {
            ds = (DatasetSummary) i.next();
            if (ds.getID() ==  dd.getID()) {
                ds.setName(dd.getName());
                break;
            }   
        }
    }
    
    /** Synchronize the 2 views displaying image data. */
    private void synchImagesView(ImageSummary is) 
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
    private void updatePSList(ProjectData pd)
    {
        //TODO need to be modified.
        Iterator i = projectSummaries.iterator();
        ProjectSummary ps;
        while (i.hasNext()) {
            ps = (ProjectSummary) i.next();
            if (ps.getID() ==  pd.getID()) {
                ps.setName(pd.getName());
                ps.setDatasets(pd.getDatasets());
                break;
            }   
        }
    }

}
