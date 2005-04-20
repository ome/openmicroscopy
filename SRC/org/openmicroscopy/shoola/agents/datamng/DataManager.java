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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
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
        presentation.deIconify();
        control.showComponent(null, DataManagerCtrl.FOR_CLASSIFICATION);
    }
    
    /** Handle the show properties event. */
    private void handleShowProperties(ShowProperties response)
    {
        control.showProperties(response.getUserObject(), response.getParent());
    }
    
	/** Rebuild the Tree if the connection is succesfull. */
	private void handleSAR(ServiceActivationResponse response)
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
    void refreshCategory(CategoryData data)
    {
        if (presentation != null) presentation.refreshCategory(data);
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
	List getImagesDiff(DatasetData data, Map filters, Map complexFilters)
	{
		List imagesDiff = new ArrayList();
        try {
            imagesDiff = getImportedImages(filters, complexFilters);   
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
    List getImagesInUserDatasetsDiff(DatasetData data, List datasets,
            Map filters, Map complexFilters)
    {
        List imagesDiff = new ArrayList();
        if (datasets == null || datasets.size() == 0) return imagesDiff;
        try {
            imagesDiff = getImagesInDatasets(datasets, filters, complexFilters);   
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
    List getImagesInUserGroupDiff(DatasetData data, Map filters, 
                                Map complexFilters)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getGroupImages(filters, complexFilters);   
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
    List getImagesInSystemDiff(DatasetData data, Map filters, 
                                Map complexFilters)
    {
        List imagesDiff = new ArrayList();
        try {
            imagesDiff = getSystemImages(filters, complexFilters);   
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
     * Return the list of datasets used by the current user.
     * @return
     * @throws DSAccessException
     */
    List getUsedDatasets()
        throws DSAccessException
    {
        List projects = getUserProjects();
        List datasets = new ArrayList();
        if (projects == null || projects.size() == 0) return datasets;
        Map map = new HashMap();
        Iterator i = projects.iterator(), j, k;
        List listDS;
        DatasetSummary ds;
        while (i.hasNext()) {
            listDS = ((ProjectSummary) i.next()).getDatasets();
            j = listDS.iterator();
            while (j.hasNext()) {
                ds = (DatasetSummary) j.next();
                map.put(new Integer(ds.getID()), ds);
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
	List getImportedImages(Map filters, Map complexFilters)
        throws DSAccessException
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			return dms.retrieveUserImages(filters, complexFilters);
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
     * Retrieve the images contained in the list of datasets
     * 
     * @param datasets    list of datasetSummary.
     * @return list of {@link ImageSummary} objects contained in the datasets.
     */
    List getImagesInDatasets(List datasets, Map filters, Map complexFilters)
        throws DSAccessException
    {
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
    List getGroupImages(Map filters, Map complexFilters)
        throws DSAccessException
    {
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInUserGroup(filters, complexFilters);
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
    List getSystemImages(Map filters, Map complexFilters)
        throws DSAccessException
    {
        try { 
            DataManagementService dms = registry.getDataManagementService();
            return dms.retrieveImagesInSystem(filters, complexFilters);
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
	
    Image getThumbnail(ImageData data) 
    {
        BufferedImage thumbnail = null;
        PixelsService ps = registry.getPixelsService();
        PixelsDescription pxd = data.getDefaultPixels();
        int sizeX = THUMBNAIL_SIZE;
        int sizeY = THUMBNAIL_SIZE;
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
        try {
            Pixels pix = pxd.getPixels();
            thumbnail = ps.getThumbnail(pix, sizeX, sizeY);
        } catch(ImageServerException ise) {}
        return thumbnail;
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
			} else  presentation.rebuildTree();
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
     * Post an event to browse the specified dataset.
     * 
     * @param object    DataObject corresponding either to a 
     *                  DatasetSummary, DatasetData.
     *                  
     */
	void browseDataset(DataObject object)
	{
        int id = -1;
        if (object instanceof DatasetData) 
            id = ((DatasetData) object).getID();
        else if (object instanceof DatasetSummary) 
            id = ((DatasetSummary) object).getID();
        if (id != -1)
            registry.getEventBus().post(new Browse(id, Browse.DATASET));
	}

    /** 
     * Post an event to browse the specified project.
     * 
     * @param object    DataObject corresponding either to a 
     *                  ProjectSummary or ProjectData.
     *                  
     */
    void browseProject(DataObject object)
    {
        int id = -1;
        if (object instanceof ProjectData) 
            id = ((ProjectData) object).getID();
        else if (object instanceof ProjectSummary) 
            id = ((ProjectSummary) object).getID();
        if (id != -1)
            registry.getEventBus().post(new Browse(id, Browse.PROJECT));
    }

    /** 
     * Post an event to browse the specified categoryGroup.
     * 
     * @param object    DataObject to browse.              
     */
    void browseCategoryGroup(CategoryGroupData data)
    {
        if (data != null)
            registry.getEventBus().post(
                    new Browse(data.getID(), Browse.CATEGORY_GROUP));
    }
    
    /** 
     * Post an event to browse the specified category.
     * 
     * @param object    DataObject to browse.
     *                  
     */
    void browseCategory(CategoryData data)
    {
        if (data != null)
            registry.getEventBus().post(
                    new Browse(data.getID(), Browse.CATEGORY));
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
	
    void annotate(DataObject target)
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

    /** Retrieve all categoryGroups. */
    List getCategoryGroups()
        throws DSAccessException
    {
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            return sts.retrieveCategoryGroups(true);  
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
    List retrieveImagesNotInCategoryGroup(CategoryGroupData group, Map filters, 
                    Map complexFilters)
        throws DSAccessException
    {
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
    }
    
    /** 
     * Retrieve all images not classified in the specified group. 
     * and contained in the specified datasets.
     * 
     * @param CategoryGroupData the specified group.
     * @param List  List of {@link DatasetSummary} objects.
     */
    List retrieveImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData group,
            List datasets, Map filters, Map complexFilters)
        throws DSAccessException
    {
        if (datasets == null || datasets.size() == 0) return new ArrayList();
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
    }
    
    /** Retrieve all images not classified in the specified group. */
    List retrieveImagesInUserGroupNotInCategoryGroup(CategoryGroupData group, 
            Map filters, Map complexFilters)
        throws DSAccessException
    {
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
    }
    
    /** Retrieve all images not classified in the specified group. */
    List retrieveImagesInSystemNotInCategoryGroup(CategoryGroupData group, 
            Map filters, Map complexFilters)
        throws DSAccessException
    {
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
    }
    
    /**
     * Retrieve images owned by the user but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffNotInCategoryGroup(CategoryData data,
                Map filters, Map complexFilters)
    {
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
    }
    
    /**
     * Retrieve images used by the current user but not already in the specified 
     * {@link CategoryData category}.
     * 
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInUserDatasetsNotInCategoryGroup(CategoryData data, 
            List datasets, Map filters, Map complexFilters)
    {
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
    }
    
    /**
     * Retrieve images owned by members of the user's group
     * but not already in the specified {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInUserGroupNotInCategoryGroup(CategoryData data, 
              Map filters, Map complexFilters)
    {
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
    }
    
    /**
     * Retrieve all images in system but not already in the specified 
     * {@link CategoryData category}.
     * @param data  specified category.
     * @return See above.
     */
    List retrieveImagesDiffInSystemNotInCategoryGroup(CategoryData data, 
              Map filters, Map complexFilters)
    {
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

    /** Create a new category group. */
    void createCategoryGroup(CategoryGroupData data)
    {
        if (data == null) return;
        try { 
            SemanticTypesService sts = registry.getSemanticTypesService();
            CategoryGroupData group = sts.createCategoryGroup(data);
            presentation.rebuildClassificationTree();
            if (group != null)
                control.showProperties(group, 
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
            if (category != null) control.showProperties(category, 
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
