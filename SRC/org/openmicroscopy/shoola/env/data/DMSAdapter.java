/*
 * org.openmicroscopy.shoola.env.data.DMSProxy
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.RemoteAuthenticationException;
import org.openmicroscopy.ds.RemoteConnectionException;
import org.openmicroscopy.ds.RemoteServerErrorException;
import org.openmicroscopy.ds.dto.DataInterface;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.dto.UserState;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.map.DatasetMapper;
import org.openmicroscopy.shoola.env.data.map.ImageMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectMapper;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
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
	
	private DataFactory		proxy;
	private Registry		registry;

	DMSAdapter(DataFactory proxy, Registry registry)
	{
		this.proxy = proxy;
		this.registry = registry;
	}
	
	/** 
	 * Retrieves the user's ID. 
	 * This method is called when we connect cf. 
	 * {@link DataServicesFactory#connect() connect}.
	 * The userID is then retrieved using the {@link UserCredentials}.
	 */
	int getUserID() 
		throws DSOutOfServiceException, DSAccessException
	{
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = new Criteria();
		criteria.addWantedField("experimenter");
		criteria.addWantedField("experimenter", "id");
		
		//Load the graph defined by criteria
		UserState us = null;
		
		try {
			us = (UserState) proxy.getUserState(criteria);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
					throw new DSAccessException("Can't retrieve the user id",
												rsee);
		} 
		
		return us.getExperimenter().getID();
	}
    
    /**Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjects(ProjectSummary pProto, 
    								DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
		//Make new protos if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		if (dProto == null) dProto = new DatasetSummary();
		
		//Retrieve the user ID.
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);

		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ProjectMapper.buildUserProjectsCriteria(
															uc.getUserID());

		//Load the graph defined by criteria.
		List projects = (List) retrieveListData(Project.class, criteria);
	  	
		//List of project summary objects.
		List projectsDS = null;
		if (projects != null) 
			//Put the server data into the corresponding client object.
    		projectsDS = ProjectMapper.fillUserProjects(projects, pProto, 
    													dProto);
    
    	//can be null
    	return projectsDS;
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjects()
		throws DSOutOfServiceException, DSAccessException
    {
    	return retrieveUserProjects(null, null);
    }
    
	/**Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserDatasets(DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
		//Make a new proto if none was provided.
		if (dProto == null) dProto = new DatasetSummary();
		
		//Retrieve the user ID.
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);

		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = DatasetMapper.buildUserDatasetsCriteria(
															uc.getUserID());

		//Load the graph defined by criteria.
		List datasets = (List) retrieveListData(Dataset.class, criteria);
	  	
		//List of dataset summary objects.
		List datasetsDS = null;
		if (datasets != null) 
			//Put the server data into the corresponding client object.
			datasetsDS = DatasetMapper.fillUserDatasets(datasets, dProto);
    
		//can be null
		return datasetsDS;
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserDatasets()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserDatasets(null);
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserImages(ImageSummary iProto)
		throws DSOutOfServiceException, DSAccessException								
	{	
		//Make a new proto if none was provided.
		if (iProto == null) iProto = new ImageSummary();
		
		//Retrieve the user ID.
		UserCredentials uc = (UserCredentials)
							registry.lookup(LookupNames.USER_CREDENTIALS);

		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ImageMapper.buildUserImagesCriteria(
															uc.getUserID());

		//Load the graph defined by criteria.
		List images = (List) retrieveListData(Image.class, criteria);
	  	
		//List of image summary objects.
		List imagesDS = null;
		if (images != null) 
			//Put the server data into the corresponding client object.
			imagesDS = ImageMapper.fillUserImages(images, iProto);
    
		//can be null
		return imagesDS;
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserImages()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserImages(null);
	}
    /**Implemented as specified in {@link DataManagementService}. */
    public ProjectData retrieveProject(int id, ProjectData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new ProjectData();
		
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ProjectMapper.buildProjectCriteria(id);
		
		//Load the graph defined by criteria.
		Project project = (Project) retrieveData(Project.class, criteria);
		
		if (project != null)
			//Put the server data into the corresponding client object.
			ProjectMapper.fillProject(project, retVal);
			
		//Can be an empty data object.
    	return retVal;
    }
    
	/**Implemented as specified in {@link DataManagementService}. */
	public ProjectData retrieveProject(int id)
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveProject(id, null);
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
    public DatasetData retrieveDataset(int id, DatasetData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
		if (retVal == null) retVal = new DatasetData();
	
		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = DatasetMapper.buildDatasetCriteria(id);
	
		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) retrieveData(Dataset.class, criteria);
		
		if (dataset != null)
			//Put the server data into the corresponding client object.
			DatasetMapper.fillDataset(dataset, retVal);
			
		//Can be an empty data object.	
    	return retVal;
    }
    
	/**Implemented as specified in {@link DataManagementService}. */
	public DatasetData retrieveDataset(int id)
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveDataset(id, null);
	}
	
    /**Implemented as specified in {@link DataManagementService}. */
    public List retrieveImages(int datasetID)
		throws DSOutOfServiceException, DSAccessException
    {
		//Define the criteria by which the object graph is pulled out.
    	Criteria criteria = DatasetMapper.buildImagesCriteria();

		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) loadData(Dataset.class, datasetID, 
											criteria);
	  	
	  	//List of image summary object.
	  	List images = null;
	  	
	  	if (dataset != null)
			//Put the server data into the corresponding client object.
	  		images = DatasetMapper.fillListImages(dataset);
	  		
	  	//can be null.
	  	/*
	  	try {
			Thread.sleep(1500);
		} catch (Exception e) {
			// TODO: handle exception
		}
		*/
	  	return images;
    }
    
    /**Implemented as specified in {@link DataManagementService}. */
    public ImageData retrieveImage(int id, ImageData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
    	if (retVal == null) retVal = new ImageData();

		//Define the criteria by which the object graph is pulled out.
		Criteria criteria = ImageMapper.buildImageCriteria(id);
				
		//Load the graph defined by criteria.
		Image image = (Image) retrieveData(Image.class, criteria);
  		if (image != null)
  			//Put the server data into the corresponding client object.
  			ImageMapper.fillImage(image, retVal);
  			
  		//Can be an empty data object.
  		return retVal;	  
    }
    
	/**Implemented as specified in {@link DataManagementService}. */
	public ImageData retrieveImage(int id) 
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveImage(id, null);
	}
	
	
	/**Implemented as specified in {@link DataManagementService}. */
	public ProjectSummary createProject(ProjectData retVal, 
										ProjectSummary pProto)
		throws DSOutOfServiceException, DSAccessException
	{
		//Make a new proto if none was provided.
		if (pProto == null) pProto = new ProjectSummary();
		/*
		Project p = (Project) createNewData(Project.class);
		p.setName(retVal.getName());
		p.setDescription(retVal.getDescription());
		updateData(p);
		ProjectMapper.fillNewProject(p, pProto);
		*/
		pProto.setID(104);
		pProto.setName(retVal.getName());
		pProto.setDatasets(retVal.getDatasets());
		return pProto;
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public DatasetSummary createDataset(List projectSummaries,
										List imageSummaries,
										DatasetData retVal, 
										DatasetSummary dProto)
		throws DSOutOfServiceException, DSAccessException
	{
		//Make a new proto if none was provided.
		if (dProto == null) dProto = new DatasetSummary();
		/*
		Dataset d = (Dataset) createNewData(Dataset.class);
		d.setName(retVal.getName());
		d.setDescription(retVal.getDescription());
		updateData(d);
		DatasetMapper.fillNewDataset(d, dProto);
		*/
		dProto.setID(104);
		dProto.setName(retVal.getName());
		return dProto;
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public ProjectSummary createProject(ProjectData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		return createProject(retVal, null);
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public DatasetSummary createDataset(List projectSummaries,
										List imageSummaries, 
										DatasetData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		return createDataset(projectSummaries, imageSummaries, retVal, null);
	}

	/**Implemented as specified in {@link DataManagementService}. */
    public void updateProject(ProjectData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Comments b/c of OMEDS status
		/*
		Project p = (Project) createNewData(Project.class);
		p.setID(retVal.getID());
		p.setName(retVal.getName());
		p.setDescription(retVal.getDescription());
		updateData(p);
		*/
    }
    
	/**Implemented as specified in {@link DataManagementService}. */
	public void updateDataset(DatasetData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		//Comments b/c of OMEDS status
		/*
		Dataset d = (Dataset) createNewData(Dataset.class);
		d.setID(retVal.getID());
		d.setName(retVal.getName());
		d.setDescription(retVal.getDescription());
		updateData(d);
		*/
	}
	
	/**Implemented as specified in {@link DataManagementService}. */
	public void updateImage(ImageData retVal)
	{
		//Comments b/c of OMEDS status
  		/*
  		Image i = (Image) createNewData(Image.class);
  		i.setID(retVal.getID());
  		i.setName(retVal.getName());
  		i.setDescription(retVal.getDescription());
  		updateData(i);
  		*/
	}
	
	/**
	 * Create a new Data Interface object
	 * Wrap the call to the {@link DataFactory#createNew(Class) create}
     * method.
	 * @param dto 	targetClass, the core data type to count.
	 * @return DataInterface.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * create a DataInterface from OMEDS service. 
	 */
	private DataInterface createNewData(Class dto)
			throws DSOutOfServiceException, DSAccessException 
	{
		DataInterface retVal = null;
		try {
			retVal = proxy.createNew(dto);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
		return retVal; 
	}
	
	/**
	 * Wrap the call to the {@link DataFactory#update(DataInterface) update}
     * method.
     * 
	 * @param di		dataInterface.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * update data from OMEDS service. 
	 */
	private void updateData(DataInterface di)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			proxy.update(di);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		}
	}
	
    /**
     * Load the graph defined by the criteria.
     * Wrap the call to the {@link DataFactory#load(Class, int, Criteria) load}
     * method.
     * 
     * @param dto		targetClass, the core data type to count.
     * @param id		filter by id .
     * @param c			criteria by which the object graph is pulled out.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.  
     */
	private Object loadData(Class dto, int id, Criteria c) 
		throws DSOutOfServiceException, DSAccessException 
	{
		Object retVal = null;
		try {
			retVal = proxy.load(dto, id, c);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't load data", rsee);
		} 
		return retVal;
	}
	
	/**
	* Load the graph defined by the criteria.
	* Wrap the call to the 
	* {@link DataFactory#retrieve(Class, Criteria) retrieve} method.
	* 
	* @param dto		targetClass, the core data type to count.
	* @param c			criteria by which the object graph is pulled out. 
	* @throws DSOutOfServiceException If the connection is broken, or logged in
	* @throws DSAccessException If an error occured while trying to 
	* retrieve data from OMEDS service. 
	*/
   private Object retrieveData(Class dto, Criteria c) 
	   throws DSOutOfServiceException, DSAccessException 
   {
	   Object retVal = null;
	   try {
		   retVal = proxy.retrieve(dto, c);
	   } catch (RemoteConnectionException rce) {
		   throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
	   } catch (RemoteAuthenticationException rae) {
		   throw new DSOutOfServiceException("Not logged in", rae);
	   } catch (RemoteServerErrorException rsee) {
		   throw new DSAccessException("Can't load data", rsee);
	   } 
	   return retVal;
   }

	/**
     * Retrieve the graph defined by the criteria.
     * Wrap the call to the 
     * {@link DataFactory#retrieveList(Class, int, Criteria) retrieve}
     * method.
     *  
	 * @param dto		targetClass, the core data type to count.
	 * @param c			criteria by which the object graph is pulled out.
	 * @return
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service. 
	 */
	private Object retrieveListData(Class dto, Criteria c) 
		throws DSOutOfServiceException, DSAccessException
	{
		Object retVal = null;
		try {
			retVal = proxy.retrieveList(dto, c);
		} catch (RemoteConnectionException rce) {
			throw new DSOutOfServiceException("Can't connect to OMEDS", rce);
		} catch (RemoteAuthenticationException rae) {
			throw new DSOutOfServiceException("Not logged in", rae);
		} catch (RemoteServerErrorException rsee) {
			throw new DSAccessException("Can't retrieve data", rsee);
		} 
		return retVal;
	}
}
