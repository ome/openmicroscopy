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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.dto.UserState;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.map.DatasetMapper;
import org.openmicroscopy.shoola.env.data.map.ImageMapper;
import org.openmicroscopy.shoola.env.data.map.PixelsMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectMapper;
import org.openmicroscopy.shoola.env.data.map.UserMapper;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
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
	
	private OMEDSGateway		gateway;
	private Registry			registry;

	DMSAdapter(OMEDSGateway gateway, Registry registry)
	{
		this.gateway = gateway;
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
		UserState us = gateway.getUserState(UserMapper.getUserStateCriteria());
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
		Criteria c = DatasetMapper.buildUserDatasetsCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List datasets = (List) gateway.retrieveListData(Dataset.class, c);
	  	
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
		Criteria c = ImageMapper.buildUserImagesCriteria(uc.getUserID());

		//Load the graph defined by criteria.
		List images = (List) gateway.retrieveListData(Image.class, c);
	  	
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
		Criteria c = ProjectMapper.buildProjectCriteria(id);
		
		//Load the graph defined by criteria.
		Project project = (Project) gateway.retrieveData(Project.class, c);
		
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
		Criteria c = DatasetMapper.buildDatasetCriteria(id);
	
		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) gateway.retrieveData(Dataset.class, c);
		
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
    	Criteria c = DatasetMapper.buildImagesCriteria(datasetID);

		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) gateway.retrieveData(Dataset.class, c);
	  	
	  	//List of image summary object.
	  	List images = null;
	  	
	  	if (dataset != null)
			//Put the server data into the corresponding client object.
	  		images = DatasetMapper.fillListImages(dataset);
	  		
	  	return images;
    }
    
    /**Implemented as specified in {@link DataManagementService}. */
    public ImageData retrieveImage(int id, ImageData retVal)
		throws DSOutOfServiceException, DSAccessException
    {
		//Make a new retVal if none was provided.
    	if (retVal == null) retVal = new ImageData();

		//Define the criteria by which the object graph is pulled out.
		Criteria c = ImageMapper.buildImageCriteria(id);
				
		//Load the graph defined by criteria.
		Image image = (Image) gateway.retrieveData(Image.class, c);
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
	public PixelsDescription retrievePixels(int pixelsID, int imageID)
			throws DSOutOfServiceException, DSAccessException
	{
		PixelsDescription retVal = new PixelsDescription();
		//Define the criteria by which the object graph is pulled out.
		Criteria c = PixelsMapper.buildPixelsCriteria(imageID);
		
		Image img = (Image) gateway.retrieveData(Image.class, c);
		if (img != null)
			//Put the server data into the corresponding client object.
			PixelsMapper.fillPixelsDescription((Pixels) img.getDefaultPixels(), 
												retVal);
		return retVal;
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
		/*
  		Image i = (Image) createNewData(Image.class);
  		i.setID(retVal.getID());
  		i.setName(retVal.getName());
  		i.setDescription(retVal.getDescription());
  		updateData(i);
		*/
	}
	

}
