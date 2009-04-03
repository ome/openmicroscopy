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
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.LogicalChannel;
import org.openmicroscopy.ds.st.Repository;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.map.DatasetMapper;
import org.openmicroscopy.shoola.env.data.map.ImageMapper;
import org.openmicroscopy.shoola.env.data.map.PixelsMapper;
import org.openmicroscopy.shoola.env.data.map.ProjectMapper;
import org.openmicroscopy.shoola.env.data.map.STSMapper;
import org.openmicroscopy.shoola.env.data.map.UserMapper;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
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
		Criteria c = UserMapper.getUserStateCriteria();
		return gateway.getCurrentUser(c).getID();
	}
    
     /** Implemented as specified in {@link DataManagementService}. */
    public String getSessionKey()
    {
        return gateway.getSessionKey();
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
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
	
	/** Implemented as specified in {@link DataManagementService}. */
    public List retrieveUserProjects()
		throws DSOutOfServiceException, DSAccessException
    {
    	return retrieveUserProjects(null, null);
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
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
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserDatasets()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserDatasets(null);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
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
	
	/** Implemented as specified in {@link DataManagementService}. */
	public List retrieveUserImages()
		throws DSOutOfServiceException, DSAccessException
	{
		return retrieveUserImages(null);
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
		
		if (project != null)
			//Put the server data into the corresponding client object.
			ProjectMapper.fillProject(project, retVal);
			
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
		
		if (dataset != null)
			//Put the server data into the corresponding client object.
			DatasetMapper.fillDataset(dataset, retVal);
			
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
		Criteria c = DatasetMapper.buildImagesCriteria(datasetID);

		//Load the graph defined by criteria.
		Dataset	dataset = (Dataset) gateway.retrieveData(Dataset.class, c);

		//List of image summary object.
		List images = null;

		if (dataset != null)
		//Put the server data into the corresponding client object.
		images = DatasetMapper.fillListImages(dataset, retVal);

		return images;
	}
	
    /** Implemented as specified in {@link DataManagementService}. */
    public List retrieveImages(int datasetID)
		throws DSOutOfServiceException, DSAccessException
    {
		return retrieveImages(datasetID, null);
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
  		if (image != null)
  			//Put the server data into the corresponding client object.
  			ImageMapper.fillImage(image, retVal);
  			
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
	public PixelsDescription retrievePixels(int pixelsID, int imageID)
			throws DSOutOfServiceException, DSAccessException
	{
		PixelsDescription retVal = new PixelsDescription();
		//Define the criteria by which the object graph is pulled out.
		Criteria c = PixelsMapper.buildPixelsCriteria(imageID);
		
		Image img = (Image) gateway.retrieveData(Image.class, c);
		if (img != null)
			//Put the server data into the corresponding client object.
			PixelsMapper.fillPixelsDescription(img.getDefaultPixels(), retVal);
		return retVal;
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
		if (p != null) {
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
    }
    
	/** Implemented as specified in {@link DataManagementService}. */
	public void updateDataset(DatasetData retVal, List isToRemove, 
									List isToAdd)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = DatasetMapper.buildUpdateCriteria(retVal.getID());
		Dataset d = (Dataset) gateway.retrieveData(Dataset.class, c);
		if (d != null) {
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
	public void importImages(int datasetID, List images)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = DatasetMapper.buildUpdateCriteria(datasetID);
		Dataset d = (Dataset) gateway.retrieveData(Dataset.class, c);
		Repository rep = gateway.getRepository();
		List filesToImport = new ArrayList();
		Iterator i = images.iterator();
		Long ID;
		while (i.hasNext()) {
			ID = gateway.uploadFile(rep, (File) i.next());
			if (ID != null) filesToImport.add(ID);
		}
		if (d != null && filesToImport.size() != 0) 
			gateway.startImport(d, filesToImport);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public ChannelData[] getChannelData(int imageID)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = STSMapper.buildDefaultRetrieveCriteria(
								STSMapper.IMAGE_GRANULARITY, imageID);
		List ciList = 
			(List) gateway.retrieveListSTSData("PixelChannelComponent", c);
		
		List lcList = (List) gateway.retrieveListSTSData("LogicalChannel", c);
		if (ciList == null || lcList == null) return null;
        return ImageMapper.fillImageChannelData(ciList, lcList);
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public void updateChannelData(ChannelData retVal)
		throws DSOutOfServiceException, DSAccessException
	{
		Criteria c = STSMapper.buildDefaultRetrieveCriteria(
								STSMapper.GLOBAL_GRANULARITY, retVal.getID());
		LogicalChannel lc = 
				(LogicalChannel) gateway.retrieveSTSData("LogicalChannel", c);
				
		//update the LogicalChannel object
		lc.setExcitationWavelength(new Integer(retVal.getExcitation()));
		lc.setFluor(retVal.getFluor());
		lc.setPhotometricInterpretation(retVal.getInterpretation());
		List l = new ArrayList();
		l.add(lc);
		gateway.updateAttributes(l);
	}

	/** Implemented as specified in {@link DataManagementService}. */
	public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID, 
											int pixelType)
		throws DSOutOfServiceException, DSAccessException
	{
		
		RenderingDef displayOptions;
		displayOptions = null;
		/*
		HAVE TO WRITE CRITERIA criteria with imageID +userID+ pixels
		Criteria c = STSMapper.buildDefaultRetrieveCriteria(
										STSMapper.IMAGE_GRANULARITY, imageID);
		List rsList = 
			(List) gateway.retrieveListSTSData("RenderingSettings", c);
		// build new criteria
		if (rsList != null && rsList.size() == 0)
			displayOptions = ImageMapper.fillInRenderingDef(qs, pixelType);									
		*/
		return displayOptions;
	}
	
	/** Implemented as specified in {@link DataManagementService}. */
	public void saveRenderingSettings(int pixelsID, int imageID,
		RenderingDef rDef)
		throws DSOutOfServiceException, DSAccessException
	{ 
		/*
	
		//HAVE TO WRITE CRITERIA criteria with imageID +userID+ pixels
		Criteria c = STSMapper.buildDefaultRetrieveCriteria(
										STSMapper.IMAGE_GRANULARITY, imageID);
		List rsList = 
			(List) gateway.retrieveListSTSData("RenderingSettings", c);
		//List to save	
		List l = new ArrayList();
		RenderingSettings rs;
		ChannelBindings[] channelBindings = rDef.getChannelBindings();
		int z = rDef.getDefaultZ();
		int t = rDef.getDefaultT();
		int model = rDef.getModel();
		QuantumDef qDef = rDef.getQuantumDef();
		int family = qDef.family;
		double coeff = qDef.curveCoefficient;
		int cdStart = qDef.cdStart;
		int cdEnd = qDef.cdEnd;
		int bitResolution = qDef.bitResolution;
		if (rsList == null && rsList.size() == 0) { // nothing previously saved
			for (int i = 0; i < channelBindings.length; i++) {
				rs = (RenderingSettings) 
					gateway.createNewData("RenderingSettings");
				ImageMapper.fillInRenderingSettings(z, t, model, family, 
								coeff, cdStart, cdEnd, bitResolution, 
								ChannelBindings[i], RenderingSettings rs);
				l.add(rs);
			}
		} else {
			Iterator j = rsList.iterator();
			int k;
			if (channelBindings.length != rsList.size()) 
				throw new DSAccessException("Data retrieved from DB don't " +
					"match the parameters passed.");
			while (j.hasNext()) {
				rs = (type) j.next();
				k = rs.getTheC();
				ImageMapper.fillInRenderingSettings(z, t, model, family, 
							coeff, cdStart, cdEnd, bitResolution, 
							ChannelBindings[k], RenderingSettings rs);
				l.add(rs);
			}
		}
		gateway.updateAttributes(l);
		*/
	}

}
