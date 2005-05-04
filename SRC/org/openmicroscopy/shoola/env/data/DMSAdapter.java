/*
 * org.openmicroscopy.shoola.env.data.DMSAdapter
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.AnalysisChain;
import org.openmicroscopy.ds.dto.AnalysisLink;
import org.openmicroscopy.ds.dto.AnalysisNode;
import org.openmicroscopy.ds.dto.ChainExecution;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.ModuleCategory;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.Dimensions;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.map.AnalysisChainMapper;
import org.openmicroscopy.shoola.env.data.map.AnnotationMapper;
import org.openmicroscopy.shoola.env.data.map.ChainExecutionMapper;
import org.openmicroscopy.shoola.env.data.map.DatasetMapper;
import org.openmicroscopy.shoola.env.data.map.HierarchyMapper;
import org.openmicroscopy.shoola.env.data.map.ImageMapper;
import org.openmicroscopy.shoola.env.data.map.ModuleMapper;
import org.openmicroscopy.shoola.env.data.map.ModuleCategoryMapper;
import org.openmicroscopy.shoola.env.data.map.ModuleExecutionMapper;
import org.openmicroscopy.shoola.env.data.map.PixelsMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectMapper;
import org.openmicroscopy.shoola.env.data.map.STSMapper;
import org.openmicroscopy.shoola.env.data.map.UserMapper;
import org.openmicroscopy.shoola.env.data.model.ActualInputData;
import org.openmicroscopy.shoola.env.data.model.AnalysisChainData;
import org.openmicroscopy.shoola.env.data.model.AnalysisLinkData;
import org.openmicroscopy.shoola.env.data.model.AnalysisNodeData;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.FormalInputData;
import org.openmicroscopy.shoola.env.data.model.FormalOutputData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.env.data.model.UserDetails;
import org.openmicroscopy.shoola.env.ui.UserCredentials;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Implements the {@link DataManagementService} interface.
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
class DMSAdapter
	implements DataManagementService
{
	
    //ISSUE: Don't use the IN clause for filtering results.
    // e.g. filtering by imageID, don't pass a list of imageID with 
    //size > limit. The server may crash otherwise.
    static final int    LIMIT_FOR_IN = 1000;
    
	private OMEDSGateway		gateway;
	private Registry			registry;

	DMSAdapter(OMEDSGateway gateway, Registry registry)
	{
		this.gateway = gateway;
		this.registry = registry;
	}
    
    private List getDatasetAnnotations(List ids, int uID)
        throws DSOutOfServiceException, DSAccessException
    {
        if (ids!= null && ids.size() == 0) return null;
        Criteria c = AnnotationMapper.buildDatasetAnnotationCriteria(ids, uID);
        return (List) gateway.retrieveListSTSData("DatasetAnnotation", c);
    }

    /** Retrieve list of imageAnnotations. */
    private List getImageAnnotations(List ids, int uID)
        throws DSOutOfServiceException, DSAccessException
    {
        if (ids != null && ids.size() == 0) return null;
        Criteria c= AnnotationMapper.buildImageAnnotationCriteria(ids, uID);
        return 
            (List) gateway.retrieveListSTSData("ImageAnnotation", c);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public UserDetails getUserDetails()
        throws DSOutOfServiceException, DSAccessException
    {
        UserDetails ud = (UserDetails) 
                    registry.lookup(LookupNames.USER_DETAILS);
        if (ud == null) {
            Criteria c = UserMapper.getUserStateCriteria();
            Experimenter exp = gateway.getCurrentUser(c);
            List groups = new ArrayList();
            groups.add(new Integer(exp.getGroup().getID()));
            ud = new UserDetails(exp.getID(), exp.getFirstName(), 
                                exp.getLastName(), groups);
            registry.bind(LookupNames.USER_DETAILS, ud);
            UserCredentials uc = (UserCredentials)
                registry.lookup(LookupNames.USER_CREDENTIALS);
            uc.setUserID(ud.getUserID());
        }
        return ud;
    }
    
    
    /** Implemented as specified in {@link DataManagementService}. */
    public String getSessionKey() { return gateway.getSessionKey(); }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjects(ProjectSummary pProto, 
                                    DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
		//Make new protos if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		if (dProto == null) dProto = new DatasetSummary();
		
		//Retrieve the user ID.
		//UserCredentials uc = (UserCredentials)
		//					registry.lookup(LookupNames.USER_CREDENTIALS);
		
        UserDetails uc = getUserDetails();
        
		//Define the criteria by which the object graph is pulled out.
		Criteria c = ProjectMapper.buildUserProjectsCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List projects = (List) gateway.retrieveListData(Project.class, c);
	  	
		//List of project summary objects.
		List projectsDS = null;
		if (projects != null) 
			//Put the server data into the corresponding client object.
    		projectsDS = ProjectMapper.fillUserProjects(projects, pProto, 
    													dProto);
		//can be null
    	return projectsDS;
	}
	
    /** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserProjects()
		throws DSOutOfServiceException, DSAccessException
    {
    	return retrieveUserProjects(null, null);
    }
    	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserProjectsWithDatasetData(ProjectSummary pProto,
			DatasetData dProto)
		throws DSOutOfServiceException, DSAccessException
	{
		//Make new protos if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		if (dProto == null) dProto = new DatasetData();
		
		//Retrieve the user ID.
		//UserCredentials uc = (UserCredentials)
		//					registry.lookup(LookupNames.USER_CREDENTIALS);
        UserDetails uc = getUserDetails();
		//Define the criteria by which the object graph is pulled out.
		Criteria c = ProjectMapper.buildUserProjectsCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List projects = (List) gateway.retrieveListData(Project.class, c);
	  	
        if (projects == null || projects.size() == 0) return new ArrayList();
		return ProjectMapper.fillUserProjectsWithDatasetData(projects, pProto, 
                                                            dProto);
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserProjectsWithDatasetData()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserProjectsWithDatasetData(null, null);
	}

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjectsWithDAnnotations()
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveUserProjectsWithDAnnotations(null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjectsWithDAnnotations(ProjectSummary pProto, 
                                                    DatasetSummary dProto)
        throws DSOutOfServiceException, DSAccessException
    {
        //Make new protos if none was provided.
        if (pProto == null) pProto = new ProjectSummary();
        if (dProto == null) dProto = new DatasetSummary();
        
        //Retrieve the user ID.
        //UserCredentials uc = (UserCredentials)
        //                   registry.lookup(LookupNames.USER_CREDENTIALS);
        UserDetails uc = getUserDetails();
        //Define the criteria by which the object graph is pulled out.
        Criteria c = ProjectMapper.buildUserProjectsCriteria(uc.getUserID());

        //Load the graph defined by criteria.
        List projects = (List) gateway.retrieveListData(Project.class, c);
        
        //Put the server data into the corresponding client object.
        if (projects == null || projects.size() == 0) return new ArrayList();
        List ids = ProjectMapper.prepareListDatasetsID(projects);
        if (ids != null && ids.size() != 0) {
            //if (ids.size() > LIMIT_FOR_IN) ids = null;
            List dsAnnotations = getDatasetAnnotations(ids, uc.getUserID());
            return ProjectMapper.fillListAnnotatedDatasets(projects, pProto, 
                            dProto, dsAnnotations);
        }
        //Projects contain no datasets, this is the case for the
        //initial project for example.
        return ProjectMapper.fillUserProjects(projects, pProto, dProto);
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserDatasets(DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
		//Make a new proto if none was provided.
		if (dProto == null) dProto = new DatasetSummary();
		
		//Retrieve the user ID.
		//UserCredentials uc = (UserCredentials)
		//					registry.lookup(LookupNames.USER_CREDENTIALS);
        UserDetails uc = getUserDetails();
		//Define the criteria by which the object graph is pulled out.
		Criteria c = DatasetMapper.buildUserDatasetsCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List datasets = (List) gateway.retrieveListData(Dataset.class, c);
	  	
        if (datasets == null || datasets.size() == 0) return new ArrayList();
		return DatasetMapper.fillUserDatasets(datasets, dProto);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserDatasets()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserDatasets(null);
	}

    /** Implemented as specified in {@link DataManagementService}. */
    public List fullRetrieveUserDatasets(DatasetData dProto, 
                                        ImageSummary iProto)
		throws DSOutOfServiceException, DSAccessException
	{
    	//Make a new proto if none was provided.
		if (dProto == null) dProto = new DatasetData();
		if (iProto == null) iProto = new ImageSummary();
		
		//Retrieve the user ID.
		//UserCredentials uc = (UserCredentials)
		//					registry.lookup(LookupNames.USER_CREDENTIALS);
        UserDetails uc = getUserDetails();
		//Define the criteria by which the object graph is pulled out.
		Criteria c = 
			DatasetMapper.buildFullUserDatasetsCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List datasets = (List) gateway.retrieveListData(Dataset.class, c);
	  	
        if (datasets == null || datasets.size() == 0) return new ArrayList();
		return DatasetMapper.fillFullUserDatasets(datasets, dProto, iProto);
	}

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserImages(Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveUserImages(null, filters, complexFilters);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserImages(ImageSummary iProto, Map filters, 
                                    Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        //Make a new proto if none was provided.
        if (iProto == null) iProto = new ImageSummary();
        
        //Retrieve the user ID.
        //UserCredentials uc = (UserCredentials)
        //                    registry.lookup(LookupNames.USER_CREDENTIALS);
        UserDetails uc = getUserDetails();
        //Define the criteria by which the object graph is pulled out.
        Criteria c = ImageMapper.buildUserImagesCriteria(uc.getUserID(), 
                            filters, complexFilters);

        //Load the graph defined by criteria.
        List images = (List) gateway.retrieveListData(Image.class, c);
        if (images == null || images.size() == 0)  return new ArrayList();
        
        Boolean b = null;
        if (filters != null)
            b = (Boolean) filters.get(DataManagementService.FILTER_ANNOTATED);
        
        if (b != null && b.booleanValue()) {
            List ids = new ArrayList();
            Iterator i = images.iterator();
            while (i.hasNext()) 
                ids.add(new Integer(((Image) i.next()).getID()));
            List imgAnnotations = getImageAnnotations(ids, uc.getUserID());
            return ImageMapper.fillListImages(images, iProto, imgAnnotations);
        }
        return ImageMapper.fillListImages(images, iProto, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserImages()
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveUserImages(null, null, null);
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserImages(ImageSummary iProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
        return retrieveUserImages(iProto, null, null);
	}
       
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserGroup(Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserGroup(null, filters, complexFilters);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserGroup(ImageSummary iProto, Map filters, 
                                            Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        //Make a new proto if none was provided.
        if (iProto == null) iProto = new ImageSummary();

        //Define the criteria by which the object graph is pulled out.
        UserDetails uc = getUserDetails();
        Criteria c = ImageMapper.buildUserImagesCriteria(
                    uc.getGroupIDs(), filters, complexFilters);

        //Load the graph defined by criteria.
        List images = (List) gateway.retrieveListData(Image.class, c);
        if (images == null) return new ArrayList(); 
        //Check if annotation filter is specified.
        Boolean b = null;
        if (filters != null)
            b = (Boolean) filters.get(DataManagementService.FILTER_ANNOTATED);
        
        if (b != null && b.booleanValue()) {
            List ids = new ArrayList();
            Iterator i = images.iterator();
            //Retrieve the user ID.
            while (i.hasNext()) 
                ids.add(new Integer(((Image) i.next()).getID()));
            List imgAnnotations = getImageAnnotations(ids, uc.getUserID());
            return ImageMapper.fillListImages(images, iProto, imgAnnotations);
        }
        return ImageMapper.fillListImages(images, iProto, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserGroup()
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserGroup(null, null, null);
    }

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserGroup(ImageSummary iProto)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserGroup(iProto, null, null);
    }
	
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets(List datasetsIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserDatasets(datasetsIDs, null, null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets(List datasetsIDs, Map filters, 
                                            Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserDatasets(datasetsIDs, null, filters, 
                                            complexFilters);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets(List datasetsIDs, 
            ImageSummary iProto, Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        if (datasetsIDs == null || datasetsIDs.size() == 0)
            return new ArrayList();
        //Create a new dataObject if none provided.
        if (iProto == null) iProto = new ImageSummary();
        //Define the criteria by which the object graph is pulled out.
        Criteria c;
        if (datasetsIDs.size() > LIMIT_FOR_IN)
            c = DatasetMapper.buildImagesCriteria(null, filters, 
                        complexFilters);
        else c = DatasetMapper.buildImagesCriteria(datasetsIDs, filters, 
                                        complexFilters);

        //Load the graph defined by criteria.
        List datasets = (List) gateway.retrieveListData(Dataset.class, c);
        if (datasets == null || datasets.size() == 0) return new ArrayList(); 
        Boolean b = null;
        if (filters != null)
            b = (Boolean) filters.get(DataManagementService.FILTER_ANNOTATED);
        
        if (b != null && b.booleanValue()) {
            UserDetails uc = getUserDetails();
            List ids = DatasetMapper.prepareListImagesID(datasets);
            List l = getImageAnnotations(ids, uc.getUserID());     
            return DatasetMapper.fillImagesInUserDatasets(datasets, iProto, l, 
                                        datasetsIDs);
        }
        return DatasetMapper.fillImagesInUserDatasets(datasets, iProto, null,
                                            datasetsIDs);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets(List datasetsIDs, 
                                            ImageSummary iProto)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInUserDatasets(datasetsIDs, iProto, null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets()
        throws DSOutOfServiceException, DSAccessException
    {
        List projects = retrieveUserProjects();
        Iterator i = projects.iterator();
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
        return retrieveImagesInUserDatasets(datasetIDs, null, null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInUserDatasets(Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        List projects = retrieveUserProjects();
        Iterator i = projects.iterator();
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
        return retrieveImagesInUserDatasets(datasetIDs, null, filters, 
                                            complexFilters);
    }

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInSystem(Map filters, Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInSystem(null, filters, complexFilters);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInSystem(ImageSummary iProto, Map filters, 
                                    Map complexFilters)
        throws DSOutOfServiceException, DSAccessException
    {
        //Make a new proto if none was provided.
        if (iProto == null) iProto = new ImageSummary();

        //Define the criteria by which the object graph is pulled out.
        Criteria c = ImageMapper.buildUserImagesCriteria(-1, filters, 
                                    complexFilters);

        //Load the graph defined by criteria.
        List images = (List) gateway.retrieveListData(Image.class, c);
        if (images == null || images.size() == 0) return new ArrayList();
        Boolean b = null;
        if (filters != null)
            b = (Boolean) filters.get(DataManagementService.FILTER_ANNOTATED);
        
        if (b != null && b.booleanValue()) {
            List ids = new ArrayList();
            Iterator i = images.iterator();
            //Retrieve the user ID.
            UserDetails uc = getUserDetails();
            while (i.hasNext()) 
                ids.add(new Integer(((Image) i.next()).getID()));
            if (ids.size() > LIMIT_FOR_IN) ids = null;
            c = AnnotationMapper.buildImageAnnotationCriteria(ids, 
                    uc.getUserID());
            List l = getImageAnnotations(ids, uc.getUserID());
            return ImageMapper.fillListImages(images, iProto, l);
        }
        return ImageMapper.fillListImages(images, iProto, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInSystem()
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInSystem(null, null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesInSystem(ImageSummary iProto)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesInSystem(iProto, null, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public ProjectData retrieveProject(int id, ProjectData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new ProjectData();
		
		//Define the criteria by which the object graph is pulled out.
		Criteria c = ProjectMapper.buildProjectCriteria(id);
		
		//Load the graph defined by criteria.
		Project project = (Project) gateway.retrieveData(Project.class, c);
		//Put the server data into the corresponding client object.
		if (project != null) ProjectMapper.fillProject(project, retVal);
			
		//Can be an empty data object.
    	return retVal;
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public ProjectData retrieveProject(int id)
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveProject(id, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
    public DatasetData retrieveDataset(int id, DatasetData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new DatasetData();
	
		//Define the criteria by which the object graph is pulled out.
		Criteria c = DatasetMapper.buildDatasetCriteria(id);
	
		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) gateway.retrieveData(Dataset.class, c);
		//Put the server data into the corresponding client object.
		if (dataset != null) DatasetMapper.fillDataset(dataset, retVal);
			
		//Can be an empty data object.	
    	return retVal;
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public DatasetData retrieveDataset(int id)
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveDataset(id, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveImages(int datasetID, ImageSummary retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		//Create a new dataObject if none provided.
		//Object used as prototype.
		if (retVal == null) retVal = new ImageSummary();
		//Define the criteria by which the object graph is pulled out.
        List ids = new ArrayList();
        ids.add(new Integer(datasetID));
		Criteria c = DatasetMapper.buildImagesCriteria(ids, null, null);

		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) gateway.retrieveData(Dataset.class, c);
		//Put the server data into the corresponding client object.
		if (dataset == null) return new ArrayList();
        return DatasetMapper.fillListImages(dataset, retVal);
	}
	
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImages(int datasetID)
		throws DSOutOfServiceException, DSAccessException
    {
		return retrieveImages(datasetID, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesWithAnnotations(int datasetID, 
                                            ImageSummary retVal)
        throws DSOutOfServiceException, DSAccessException
    {
        //Create a new dataObject if none provided.
        //Object used as prototype.
        if (retVal == null) retVal = new ImageSummary();
        //Define the criteria by which the object graph is pulled out.
        List datasetIDs = new ArrayList();
        datasetIDs.add(new Integer(datasetID));
        Criteria c = DatasetMapper.buildImagesCriteria(datasetIDs, null, null);

        //Load the graph defined by criteria.
        Dataset dataset = (Dataset) gateway.retrieveData(Dataset.class, c);

        //List of image summary object.
        List images = new ArrayList();
        //Put the server data into the corresponding client object.
        if (dataset == null) return images;
        //Retrieve the user ID.
        UserCredentials uc = (UserCredentials)
                            registry.lookup(LookupNames.USER_CREDENTIALS);
        List ids = DatasetMapper.prepareListImagesID(dataset);
        List l = getImageAnnotations(ids, uc.getUserID());
        DatasetMapper.fillListAnnotatedImages(dataset, retVal, l, images);
        return images;
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImagesWithAnnotations(int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImagesWithAnnotations(datasetID, null);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public ImageData retrieveImage(int id, ImageData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
    	if (retVal == null) retVal = new ImageData();

		//Define the criteria by which the object graph is pulled out.
		Criteria c = ImageMapper.buildImageCriteria(id);
				
		//Load the graph defined by criteria.
		Image image = (Image) gateway.retrieveData(Image.class, c);
		//Put the server data into the corresponding client object.
        if (image == null) return retVal;
        ImageMapper.fillImage(image, retVal);
        //retrieve the realSize of a pixels.
        c = PixelsMapper.buildPixelsDimensionCriteria(id);
        Dimensions pixelDim = 
            (Dimensions) gateway.retrieveSTSData("Dimensions", c);
        if (pixelDim != null)
            PixelsMapper.fillPixelsDimensions(pixelDim, 
                    retVal.getDefaultPixels());
        c = PixelsMapper.buildLogicalChannelCriteria(
                STSMapper.IMAGE_GRANULARITY, id);
        List lc = (List) gateway.retrieveListSTSData("LogicalChannel", c);
        //need to fix problem if no logical channel(shouldn't happen)
        if (lc != null && lc.size() == retVal.getDefaultPixels().getSizeC()) 
            retVal.setChannels(ImageMapper.fillImageChannels(lc));
        else 
            retVal.setChannels(ImageMapper.fillDefaultImageChannels(
                    retVal.getDefaultPixels().getSizeC()));
	
  		//Can be an empty data object.
  		return retVal;	  
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public ImageData retrieveImage(int id) 
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveImage(id, null);
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveModules(ModuleData mProto, ModuleCategoryData mcProto,
					FormalInputData finProto, FormalOutputData foutProto,
					SemanticTypeData stProto)
		throws DSOutOfServiceException, DSAccessException 
	{
		if (mProto == null)    mProto = new ModuleData();
		if (mcProto == null)   mcProto = new ModuleCategoryData();		
		if (finProto == null)  finProto = new FormalInputData();
		if (foutProto == null) foutProto = new FormalOutputData();
		if (stProto == null)   stProto = new SemanticTypeData();
		
		// Load the graph defined by the criteria
		List modules = gateway.retrieveModules();
		
		List moduleDS = null;
		if (modules != null) 
			moduleDS = ModuleMapper.fillModules(modules, mProto, mcProto,
			                                    finProto, foutProto, stProto);
		return moduleDS;
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveModules()
		throws DSOutOfServiceException, DSAccessException 
	{
		return retrieveModules(null, null, null, null, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveModuleCategories(ModuleCategoryData mcProto)
		throws DSOutOfServiceException, DSAccessException 
	{
		if (mcProto == null)   mcProto = new ModuleCategoryData();
				
		//Define the criteria by which the object graph is pulled out
		Criteria c = ModuleCategoryMapper.buildModuleCategoriesCriteria();
	
		//Load the graph defined by the criteria
		List categories = 
			(List) gateway.retrieveListData(ModuleCategory.class, c);
	
		List categoryDS = null;
		if (categories != null) 
			categoryDS = 
				ModuleCategoryMapper.fillModuleCategories(categories, mcProto);
		return categoryDS;
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveModuleCategories()
		throws DSOutOfServiceException, DSAccessException 
	{
		return retrieveModuleCategories(null);
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveChains(AnalysisChainData acProto, AnalysisLinkData
			alProto, AnalysisNodeData anProto, ModuleData modProto,
			FormalInputData finProto, FormalOutputData foutProto,
			SemanticTypeData stProto) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (acProto == null)    acProto = new AnalysisChainData();
		if (alProto == null)    alProto = new AnalysisLinkData();		
		if (anProto == null)    anProto = new AnalysisNodeData();
		if (modProto == null) 	modProto = new ModuleData();
		if (finProto == null)   finProto = new FormalInputData();
		if (foutProto == null)  foutProto = new FormalOutputData();
		if (stProto == null)    stProto = new SemanticTypeData();

	
		// Load the graph defined by the criteria
		List chains = gateway.retrieveChains();
	
		List chainsDS = null;
		if (chains != null) 
			chainsDS = AnalysisChainMapper.fillChains(chains, acProto,
			        alProto, anProto, modProto, finProto, foutProto, stProto);
		return chainsDS;
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveChains() 
		throws DSOutOfServiceException, DSAccessException 
	{
		return retrieveChains(null, null, null, null, null, null, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public AnalysisChainData retrieveChain(int id, AnalysisChainData acProto,
            AnalysisLinkData alProto, AnalysisNodeData anProto, 
            ModuleData modProto, FormalInputData finProto, 
            FormalOutputData foutProto, SemanticTypeData stProto) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (acProto == null)    acProto = new AnalysisChainData();
		if (alProto == null)    alProto = new AnalysisLinkData();		
		if (anProto == null)    anProto = new AnalysisNodeData();
		if (modProto == null) 	modProto = new ModuleData();
		if (finProto == null)   finProto = new FormalInputData();
		if (foutProto == null)  foutProto = new FormalOutputData();
		if (stProto == null)    stProto = new SemanticTypeData();
		
		// Define the criteria by which the object graph is pulled out
		Criteria c = AnalysisChainMapper.buildChainCriteria(id);
	
		// Load the graph defined by the criteria
		AnalysisChain chain = (AnalysisChain) 
			gateway.retrieveData(AnalysisChain.class, c);

		if (chain != null) 
			AnalysisChainMapper.fillChain(chain, acProto, alProto, anProto,
                        modProto, finProto, foutProto, stProto);
		return acProto;
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public AnalysisChainData retrieveChain(int id) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return retrieveChain(id, null, null, null, null, null, null, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveChainExecutions(ChainExecutionData ceProto,
			                DatasetData dsProto, AnalysisChainData acProto,
			                NodeExecutionData neProto, ModuleData mProto, 
							ModuleExecutionData meProto) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (ceProto == null)    ceProto = new ChainExecutionData();
		if (dsProto == null)    dsProto = new DatasetData();		
		if (acProto == null)    acProto = new AnalysisChainData();
		if (neProto == null) 	neProto = new NodeExecutionData();
		if (mProto == null)     mProto = new ModuleData();
		if (meProto == null)    meProto = new ModuleExecutionData();
		
		//Retrieve the user ID.
		UserDetails uc = getUserDetails();
		//Define the criteria by which the object graph is pulled out
		Criteria c = ChainExecutionMapper.
			buildChainExecutionCriteria(uc.getUserID());
		// Load the graph defined by the criteria
		List execs = (List) gateway.retrieveListData(ChainExecution.class, c);
		
		List execDS = null;
		if (execs != null) 
			execDS = ChainExecutionMapper.fillChainExecutions(execs, ceProto, 
                     dsProto, acProto, neProto, mProto, meProto);
		return execDS;
	}	
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveChainExecutions()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveChainExecutions(null, null, null, null, null, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public ProjectSummary createProject(ProjectData retVal, 
                                        ProjectSummary pProto)
		throws DSOutOfServiceException, DSAccessException
	{
		//Make a new proto if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		
		//Create a new project Object
		Criteria c = UserMapper.getUserStateCriteria(); 
		Project p = (Project) gateway.createNewData(Project.class);
		p.setName(retVal.getName());
		p.setDescription(retVal.getDescription());
		p.setOwner(gateway.getCurrentUser(c));
		gateway.markForUpdate(p);
		gateway.updateMarkedData();
		List ids = ProjectMapper.fillNewProject(p, retVal.getDatasets(), 
												pProto);
		if (ids.size() != 0) gateway.addDatasetsToProject(p.getID(), ids);
		return pProto;
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public DatasetSummary createDataset(List projectSummaries,
										List imageSummaries,
										DatasetData retVal, 
										DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException
	{
		//Make a new proto if none was provided.
		if (dProto == null) dProto = new DatasetSummary();
		
		//Create a new project Object
		Criteria c = UserMapper.getUserStateCriteria(); 
		Dataset d = (Dataset) gateway.createNewData(Dataset.class);
		d.setName(retVal.getName());
		d.setDescription(retVal.getDescription());
		d.setOwner(gateway.getCurrentUser(c));
		gateway.markForUpdate(d);
		gateway.updateMarkedData();
		
		//prepare list for Manager.
		List pIds = new ArrayList(), iIds = new ArrayList();		
		if (projectSummaries != null) {
			Iterator i =  projectSummaries.iterator();
			while (i.hasNext())	
				pIds.add(new Integer(((ProjectSummary) i.next()).getID()));
		}
		
		if (imageSummaries != null) {
			Iterator j = imageSummaries.iterator();
			while (j.hasNext())
				iIds.add(new Integer(((ImageSummary) j.next()).getID()));
		}
		
		if (pIds.size() != 0) gateway.addDatasetToProjects(d.getID(), pIds);
		if (iIds.size() != 0) gateway.addImagesToDataset(d.getID(), iIds);
		
		//fill up the proto
		dProto.setID(d.getID());
		dProto.setName(d.getName());
		return dProto;
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public ProjectSummary createProject(ProjectData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		return createProject(retVal, null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public DatasetSummary createDataset(List projectSummaries,
										List imageSummaries, 
										DatasetData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		return createDataset(projectSummaries, imageSummaries, retVal, null);
	}

	/** Implemented as specified in {@link DataManagementService}. */
    public void updateProject(ProjectData retVal, List dsToRemove,
    								 List dsToAdd)
		throws DSOutOfServiceException, DSAccessException
    {
    	Criteria c = ProjectMapper.buildUpdateCriteria(retVal.getID());
		Project p = (Project) gateway.retrieveData(Project.class, c);
        if (p == null) return;
		p.setName(retVal.getName());
		p.setDescription(retVal.getDescription());
		gateway.markForUpdate(p);
		gateway.updateMarkedData();
		
		//prepare list for Manager.
		List toRemoveIds = new ArrayList(), toAddIds = new ArrayList();
		List datasets = retVal.getDatasets();	
		DatasetSummary ds;	
	  	if (dsToRemove != null) {
			Iterator i =  dsToRemove.iterator();
			while (i.hasNext())	{
				ds = (DatasetSummary) i.next();
				toRemoveIds.add(new Integer(ds.getID()));
				datasets.remove(ds);
		  	}	  
		}
		if (dsToAdd != null) {
			Iterator j = dsToAdd.iterator();
			while (j.hasNext()) {
				ds = (DatasetSummary) j.next();
				toAddIds.add(new Integer(ds.getID()));	
				if (!datasets.contains(ds)) datasets.add(ds);
			}
		}
		//Remove the specified datasets.
		if (toRemoveIds.size() != 0)
			gateway.removeDatasetsFromProject(retVal.getID(), toRemoveIds);
		//Add the specified datasets.
		if (toAddIds.size() != 0)
			gateway.addDatasetsToProject(retVal.getID(), toAddIds);
		retVal.setDatasets(datasets);
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public void updateDataset(DatasetData retVal, List isToRemove, 
									List isToAdd)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = DatasetMapper.buildUpdateCriteria(retVal.getID());
		Dataset d = (Dataset) gateway.retrieveData(Dataset.class, c);
		if (d == null) return;
		d.setName(retVal.getName());
		d.setDescription(retVal.getDescription());
		gateway.markForUpdate(d);
		gateway.updateMarkedData();
		//prepare list for Manager.
		List toRemoveIds = new ArrayList(), toAddIds = new ArrayList();
		List images = retVal.getImages();	
		ImageSummary is;	
		if (isToRemove != null) {
			Iterator i =  isToRemove.iterator();
			while (i.hasNext())	{
				is = (ImageSummary) i.next();
				toRemoveIds.add(new Integer(is.getID()));
				images.remove(is);
			}	  
		}
		if (isToAdd != null) {
			Iterator j = isToAdd.iterator();
			while (j.hasNext()) {
				is = (ImageSummary) j.next();
				toAddIds.add(new Integer(is.getID()));	
				if (!images.contains(is)) images.add(is);
			}
		}

		//Remove the specified datasets.
		if (toRemoveIds.size() != 0)
			gateway.removeImagesFromDataset(retVal.getID(), toRemoveIds);
		//Add the specified datasets.
		if (toAddIds.size() != 0)
			gateway.addImagesToDataset(retVal.getID(), toAddIds);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public void updateImage(DataObject retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		if (retVal instanceof ImageSummary) 
			updateImage((ImageSummary) retVal);
		if (retVal instanceof ImageData) 
			updateImage((ImageData) retVal);
	}

	/** Implemented as specified in {@link DataManagementService}. */
	private void updateImage(ImageData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = ImageMapper.buildUpdateCriteria(retVal.getID());
		Image i = (Image) gateway.retrieveData(Image.class, c);
		if (i != null) {
			gateway.markForUpdate(i);
			i.setName(retVal.getName());
			i.setDescription(retVal.getDescription());
			gateway.updateMarkedData();
		}
	}
	
	/** Update the image using an image summary object. */
	private void updateImage(ImageSummary retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = ImageMapper.buildUpdateCriteria(retVal.getID());
		Image i = (Image) gateway.retrieveData(Image.class, c);
		if (i != null)	{
			gateway.markForUpdate(i);
			i.setName(retVal.getName());
			gateway.updateMarkedData();
		} 
	}

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrievePDIHierarchy(List imageSummaries, boolean annotated)
        throws DSOutOfServiceException, DSAccessException
    {
        if (imageSummaries == null)
            throw new NullPointerException("List of imageSummaries " +
                "cannot be null");
        if (imageSummaries.size() == 0)
            throw new IllegalArgumentException("List of imageSummaries " +
                    "cannot be of length 0");
        Iterator i = imageSummaries.iterator();
        ImageSummary is;
        Map map = new HashMap();
        List ids = new ArrayList();
        Integer id;
        //Not sure this is useful in that case.
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            id = new Integer(is.getID());
            map.put(id, is);
            ids.add(id);
        }
        //TODO: use subLits
        //if (ids.size() > LIMIT_FOR_IN) ids = null;
        Criteria c = HierarchyMapper.buildIDPHierarchyCriteria(ids);
        //Load the graph defined by criteria.
        List images = (List) gateway.retrieveListData(Image.class, c);
        if (images == null || images.size() == 0) return new ArrayList();
        
        if (!annotated)
            return HierarchyMapper.fillIDPHierarchy(images, map, null);
        
        UserDetails uc = getUserDetails();
        List dsAnnotations = getDatasetAnnotations(null, uc.getUserID());
        return HierarchyMapper.fillIDPHierarchy(images, map, dsAnnotations);
    }

    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveProjectsTree(List projectIDs, boolean annotated)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c;
        //TODO NEED SubList
        //if (projectIDs.size() > LIMIT_FOR_IN) 
        //    c = ProjectMapper.buildProjectsTreeCriteria(null);
        //else 
        c = ProjectMapper.buildProjectsTreeCriteria(projectIDs);
        List projects = (List) gateway.retrieveListData(Project.class, c);

        List results = new ArrayList();
        //Put the server data into the corresponding client object.
        if (projects == null || projects.size() == 0) return results;
        
        if (!annotated) {
            ProjectMapper.fillProjectsTree(projects, results, projectIDs, null, 
                    null);
            return results;
        }
        //Retrieve the user ID.
        UserDetails uc = getUserDetails();
        int uID = uc.getUserID();
       
        List datasetIDs = ProjectMapper.prepareListDatasetsID(projects);
        List imageIDs = ProjectMapper.prepareListImagesID(projects);
        List dsAnnotations = getDatasetAnnotations(datasetIDs, uID);
        List isAnnotations = getImageAnnotations(imageIDs, uID);
        ProjectMapper.fillProjectsTree(projects, results, projectIDs, 
                                    dsAnnotations, isAnnotations);
        return results;
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public ProjectSummary retrieveProjectTree(int projectID, boolean annotated)
        throws DSOutOfServiceException, DSAccessException
    {
        List ids = new ArrayList();
        ids.add(new Integer(projectID));
        List results = retrieveProjectsTree(ids, annotated); 
        return (ProjectSummary) results.get(0);
    }

    /** Implemented as specified in {@link DataManagementService}. */
    public DatasetSummaryLinked retrieveDatasetTree(int datasetID, 
                                boolean annotated)
        throws DSOutOfServiceException, DSAccessException
    {
        List ids = new ArrayList();
        ids.add(new Integer(datasetID));
        List results = retrieveDatasetsTree(ids, annotated);
        return (DatasetSummaryLinked) results.get(0);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveDatasetsTree(List datasetIDs, boolean annotated)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c;
        //if (datasetIDs.size() > LIMIT_FOR_IN) 
        //    c = DatasetMapper.buildDatasetsTree(null);
        //else 
        c = DatasetMapper.buildDatasetsTree(datasetIDs);
        List datasets = (List) gateway.retrieveListData(Dataset.class, c);
        List results = new ArrayList();
        if (datasets == null || datasets.size() == 0) return results;
        if (!annotated) {
            DatasetMapper.fillDatasetsTree(datasets, results, datasetIDs);
            return results;
        }
        //Retrieve the user ID.
        UserDetails uc = getUserDetails();
        int uID = uc.getUserID();
        List ids = DatasetMapper.prepareListImagesID(datasets);
        List isAnnotations = getImageAnnotations(ids, uID);
        List dsAnnotations = getDatasetAnnotations(datasetIDs, uID);
        DatasetMapper.fillDatasetsTree(datasets, results, datasetIDs, 
                dsAnnotations, isAnnotations);
        return results;
    }
    
    private void markChainNodes(AnalysisChain chain, Collection nodes)
        throws DSOutOfServiceException, DSAccessException
    {
        Iterator iter = nodes.iterator();
        AnalysisNodeData node;
        AnalysisNode n;
        while (iter.hasNext()) {
            node = (AnalysisNodeData) iter.next();
            n  = (AnalysisNode) gateway.createNewData(AnalysisNode.class);
            node.setAnalysisNodeDTO(n);
            n.setModule(node.getModule().getModuleDTO());
            n.setChain(chain);
            gateway.markForUpdate(n);
        }   
    }
    
    private void markChainLinks(AnalysisChain chain, Collection links) 
        throws DSOutOfServiceException, DSAccessException
    {   
        AnalysisNodeData node;
        AnalysisNode n;
        AnalysisLinkData link;
        AnalysisLink  lnk;
        Iterator iter = links.iterator();
        FormalOutputData output;
        FormalInputData input;
        while (iter.hasNext()) {
            link = (AnalysisLinkData) iter.next();
            lnk =   (AnalysisLink) gateway.createNewData(AnalysisLink.class);
            // set output module
            // get the analysis Node data that it came from.
            
            node = link.getFromNode();
            // get the DTO for this node
            n = node.getAnalysisNodeDTO();
            // set this in the dto for the link going out
            lnk.setFromNode(n);
            
            // set output param
            output = link.getFromOutput();
            lnk.setFromOutput(output.getFormalOutputDTO());
            
            // set input node
            // as per above with output node
            node = link.getToNode();
            n = node.getAnalysisNodeDTO();
            lnk.setToNode(n);

            // set input param.
            input = link.getToInput();
            lnk.setToInput(input.getFormalInputDTO());
            
            //set chain
            lnk.setChain(chain);    
            gateway.markForUpdate(lnk);
        }
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List getMexExecutionHistory(int mexID, ModuleExecutionData mexProto,
    			ModuleData modData, ActualInputData inpData, 
                FormalInputData finData, FormalOutputData foutData,
                SemanticTypeData stData)
    		throws DSOutOfServiceException, DSAccessException
    {
    		if (mexProto == null) mexProto = new ModuleExecutionData();
    		
    		List mexes = gateway.getModuleExecutionHistory(mexID);
    		List mexDS = null;
    		if (mexes != null)
    				mexDS = ModuleExecutionMapper.fillHistoryMexes(mexes, 
                      mexProto, modData, inpData, finData, foutData, stData); 
    		return mexDS;
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List getMexExecutionHistory(int mexID) 
		throws DSOutOfServiceException, DSAccessException 
	{
    		return getMexExecutionHistory(mexID, null, null, null, null, null,
                                            null);
	}

    /** Implemented as specified in {@link DataManagementService}. */
    public List getChainExecutionHistory(int chexID, 
                ModuleExecutionData mexProto, ModuleData modData, 
                ActualInputData inpData, FormalInputData finData,
				FormalOutputData foutData, SemanticTypeData stData) 
	    throws DSOutOfServiceException, DSAccessException
	{	
    		if (mexProto == null) mexProto = new ModuleExecutionData();

    		List mexes = gateway.getChainExecutionHistory(chexID);
    		List mexDS = null;
    		if (mexes != null)
    				mexDS = ModuleExecutionMapper.fillHistoryMexes(mexes, 
    				mexProto, modData, inpData, finData, foutData, stData);
    		return mexDS;
	}
    
    /** Implemented as specified in {@link DataManagementService}. */
    public List getChainExecutionHistory(int chexID) 
		throws DSOutOfServiceException, DSAccessException 
	{
    		return getChainExecutionHistory(chexID, null, null, null, null, 
                                            null, null);
	}
    
    /** Implemented as specified in {@link DataManagementService}. */
    public void createAnalysisChain(AnalysisChainData retVal)
        throws DSOutOfServiceException, DSAccessException
    {
        //Create a new chain Object
        Criteria c = UserMapper.getUserStateCriteria(); 
        AnalysisChain chain = 
                (AnalysisChain) gateway.createNewData(AnalysisChain.class);
        chain.setName(retVal.getName());
        chain.setDescription(retVal.getDescription());
        chain.setOwner(gateway.getCurrentUser(c));
        gateway.markForUpdate(chain);
        markChainNodes(chain,retVal.getNodes());        
        markChainLinks(chain,retVal.getLinks());
        gateway.updateMarkedData();
        retVal.setID(chain.getID());
    }

}

