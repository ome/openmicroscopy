/*
 * blitzgateway.service.ServiceFactory 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package blitzgateway.service;

//Java imports
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import blitzgateway.service.gateway.GatewayFactory;
import blitzgateway.service.stateful.RawFileStoreService;
import blitzgateway.service.stateful.RawFileStoreServiceImpl;
import blitzgateway.service.stateful.RawPixelsStoreService;
import blitzgateway.service.stateful.RawPixelsStoreServiceImpl;
import blitzgateway.service.stateful.RenderingService;
import blitzgateway.service.stateful.RenderingServiceImpl;
import blitzgateway.service.stateful.ThumbnailService;
import blitzgateway.service.stateful.ThumbnailServiceImpl;
import omero.RType;
import omero.model.Dataset;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.util.OMEROClass;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ServiceFactory
{		
	/** The gateway factory to create make connection, create and access 
	 *  services .
	 */
	private GatewayFactory 	gatewayFactory;
	
	/** The Data service object. */
	private DataService 	dataService;
	
	/** The Image service object. */
	private ImageService	imageService;

	/** The FileService object. */
	private FileService		fileService;
	
	/** The rendering service object. */
	private RenderingService renderingService;
	
	/** The rawFile service object. */
	private RawFileStoreService rawFileStoreService;
	
	/** The raw pixels service object. */
	private RawPixelsStoreService rawPixelsStoreService;
	
	/** The thumbnail service. */
	private ThumbnailService 	thumbnailService;
	
	/**
	 * Create the service factory which creates the gateway and services
	 * and links the different services together.  
	 * 
	 * @param iceConfig path to the ice config file.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public ServiceFactory(String iceConfig) 
		throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory = new GatewayFactory(iceConfig);
	}
	
	/**
	 * Is the session closed?
	 * @return true if closed.
	 */
	public boolean isClosed()
	{
		return gatewayFactory.isClosed();
	}
	
	/**
	 * Close the session with the server.
	 */
	public void close()
	{
		gatewayFactory.close();
		dataService = null;
		imageService = null;
		fileService = null;
		renderingService = null;
		rawFileStoreService = null;
		rawPixelsStoreService = null;
		thumbnailService = null;
	}
	
	/**
	 * Open a session to the server with username and password.
	 * @param username see above.
	 * @param password see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void createSession(String username, String password) 
				throws DSOutOfServiceException, DSAccessException
	{
		gatewayFactory.createSession(username, password);
		renderingService = new RenderingServiceImpl(gatewayFactory);
		thumbnailService = new ThumbnailServiceImpl(gatewayFactory);
		rawFileStoreService = new RawFileStoreServiceImpl(gatewayFactory);
		rawPixelsStoreService = new RawPixelsStoreServiceImpl(gatewayFactory);
		dataService = new DataServiceImpl(gatewayFactory.getIPojoGateway(), 
										  gatewayFactory.getIQueryGateway(), 
										  gatewayFactory.getITypeGateway(),
										  gatewayFactory.getIUpdateGateway()
										  );
		
		imageService = new ImageServiceImpl(
									rawPixelsStoreService,
									renderingService,
									thumbnailService,
									gatewayFactory.getIPixelsGateway(), 
									gatewayFactory.getIQueryGateway(), 
									gatewayFactory.getIUpdateGateway());
		fileService = new FileServiceImpl(rawFileStoreService, 
										gatewayFactory.getIScriptGateway(), 
										gatewayFactory.getIQueryGateway());
	}
	
	/**
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param ids user ids to get the projects from.
	 * @param withLeaves get the datasets too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Project> getProjects(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getProjects(ids, withLeaves);
	}

	/**
	 * Get the datasets in the OMERO.Blitz server in the projects ids.
	 * @param ids ids of the projets to get the datasets from.
	 * @param withLeaves get the images too.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean withLeaves) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getDatasets(ids, withLeaves);
	}

	/**
	 * Get the pixels associated with the image.
	 * @param imageId
	 * @return the list of pixels.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
		throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromImage(imageId);
	}

	
	/**
	 * Get the image with id
	 * @param id see above
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Image getImage(Long id) 
			throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getImage(id);
	}
	
	/**
	 * Get the images in the OMERO.Blitz server from the object parentType with
	 * id's in list ids.
	 * @param parentType see above.
	 * @param ids see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImages(OMEROClass parentType, List<Long> ids ) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImages(parentType, ids);
	}

	/**
	 * Run the query passed as a string in the iQuery interface.
	 * @param myQuery string containing the query.
	 * @return the result.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Object findAllByQuery(String myQuery) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.findAllByQuery(myQuery);
	}
	
	/**
	 * Run the query passed as a string in the iQuery interface.
	 * @param myQuery string containing the query.
	 * @return the result.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Object findByQuery(String myQuery) 
			throws DSOutOfServiceException, DSAccessException
	{
		return dataService.findByQuery(myQuery);
	}
	
	/**
	 * Get the raw plane for the image id imageId.
	 * @param imageID id of the image to retrieve.
	 * @param c the channel of the image to retrieve.
	 * @param t the time point to retrieve.
	 * @param z the z section to retrieve.
	 * @return The raw plane in 2-d array of doubles. 
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public double[][] getPlane(long imageID, int z, int c, int t) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPlane(imageID, z, c, t);
	}
	
	
	/**
	 * Get the pixels information for an image.
	 * @param pixelsId image id relating to the pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long pixelsId) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPixels(pixelsId);
	}
	
	/**
	 * Get the name of the user.
	 * @return see above.
	 *
	 */
	public String getUserName()
	{
		return gatewayFactory.getUserName();
	}
	
	/**
	 * Copy the pixels set from pixels to a new set.
	 * @param pixelsID pixels id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyPixels(long pixelsID, int x, int y,
		int t, int z, List<Integer> channelList, String methodology) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyPixels(pixelsID, x, y, t, z, channelList, methodology);
	}
	
	/**
	 * Copy the image and pixels set from image to a new set.
	 * @param imageId image id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long copyImage(long imageId, int x, int y,
		int t, int z, List<Integer> channelList, String methodology) throws 
		DSOutOfServiceException, DSAccessException
	{
		return imageService.copyImage(imageId, x, y, t, z, channelList, methodology);
	}
	
	/**
	 * Upload the plane to the server, on pixels id with channel and the 
	 * time, + z section. the data is the client 2d data values. This will
	 * be converted to the raw server bytes.
	 * @param pixelsId pixels id to upload to .  
	 * @param z z section. 
	 * @param c channel.
	 * @param t time point.
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t, 
			double [][] data) throws DSOutOfServiceException, DSAccessException
	{
		imageService.uploadPlane(pixelsId, z, c, t, data);
	}

	/**
	 * Update the pixels object to the new object.
	 * @param object see above.
	 * @return the new updated pixels.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels updatePixels(Pixels object) 
	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.updatePixels(object);
	}

	/**
	 * Get the pixelsTypes.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<PixelsType> getPixelTypes() 
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelTypes();
	}

	/**
	 * Get the pixelsType for type of name type.
	 * @param type see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public PixelsType getPixelType(String type) 
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelType(type);
	}
	
	/**
	 * Get the scripts from the iScript Service.
	 * @return all the available scripts, mapped by id, name
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<Long, String> getScripts() throws   DSOutOfServiceException, 
											DSAccessException
	{
		return fileService.getScripts();
	}
	
	/**
	 * Get the id of the script with name 
	 * @param name name of the script.
	 * @return the id of the script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long getScriptID(String name) throws DSOutOfServiceException, 
										 DSAccessException
	{
		return fileService.getScriptID(name);
	}
	
	/**
	 * Upload the script to the server.
	 * @param script script to upload
	 * @return id of the new script.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public long uploadScript(String script) throws DSOutOfServiceException, 
											DSAccessException	
	{
		return fileService.uploadScript(script);
	}

	/**
	 * Get the script with id, this returns the actual script as a string.
	 * @param id id of the script to retrieve.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public String getScript(long id) throws DSOutOfServiceException, 
									 DSAccessException
	{
		return fileService.getScript(id);
	}
	
	/**
	 * Get the params the script takes, this is the name and type. 
	 * @param id id of the script.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<String, RType> getParams(long id) throws DSOutOfServiceException, 
												 DSAccessException
	{
		return fileService.getParams(id);
	}
	
	/**
	 * Run the script and get the results returned as a name , value map.
	 * @param id id of the script to run.
	 * @param map the map of params, values for inputs.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Map<String, RType> runScript(long id, Map<String, RType> map) 
						throws DSOutOfServiceException, DSAccessException
	{
		return fileService.runScript(id, map);
	}
	
	/**
	 * Delete the script from the server.
	 * @param id id of the script to delete.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void deleteScript(long id) throws 	DSOutOfServiceException, 
										DSAccessException
	{
		fileService.deleteScript(id);
	}	
	
	/**
	 * Render image as Buffered image. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public BufferedImage getRenderedImage(long pixelsId, int z, int t)	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getRenderedImage(pixelsId, z, t);
	}

	/**
	 * Render image as 3d matrix. 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t)	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getRenderedImageMatrix(pixelsId, z, t);
	}
	
	/**
	 * Render as a packedInt 
	 * @param pixelsId pixels id of the plane to render
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] renderAsPackedInt(Long pixelsId, int z, int t) throws DSOutOfServiceException, DSAccessException
	{
		return imageService.renderAsPackedInt(pixelsId, z, t);
	}
	
	/**
	 * Set the active channels in the pixels.
	 * @param pixelsId the pixels id.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setActive(Long pixelsId, int w, boolean active) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setActive(pixelsId, w, active);
	}

	/**
	 * Is the channel active.
	 * @param pixelsId the pixels id.
	 * @param w channel
	 * @return true if the channel active.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	boolean isActive(Long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.isActive(pixelsId, w);
	}

	/**
	 * Get the default Z section of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	int getDefaultZ(Long pixelsId) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getDefaultT(pixelsId);
	}
	
	/**
	 * Get the default T point of the image
	 * @param pixelsId the pixelsId of the image.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int getDefaultT(Long pixelsId) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getDefaultT(pixelsId);
	}
	
	/**
	 * Set the default Z section of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param z see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultZ(Long pixelsId, int z) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setDefaultZ(pixelsId, z);
	}
	
	/**
	 * Set the default timepoint of the image.
	 * @param pixelsId the pixelsId of the image.
	 * @param t see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultT(Long pixelsId, int t) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setDefaultT(pixelsId, t);
	}
		
	/**
	 * Set the channel min, max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @param start min.
	 * @param end max.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setChannelWindow(Long pixelsId, int w, double start, double end) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setChannelWindow(pixelsId, w, start, end);
	}
	
	/**
	 * Get the channel min.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowStart(Long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getChannelWindowStart(pixelsId, w);
	}
	
	/**
	 * Get the channel max.
	 * @param pixelsId the pixelsId of the image.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowEnd(Long pixelsId, int w) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getChannelWindowEnd(pixelsId, w);
	}
	
	/**
	 * Set the rendering def from the default to another.
	 * @param pixelsId for pixelsId 
	 * @param renderingDefId see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setRenderingDefId(long pixelsId, long renderingDefId) throws  DSOutOfServiceException, DSAccessException
	{
		imageService.setRenderingDefId(pixelsId, renderingDefId);
	}
	
	/**
	 * Get the thumbnail of the image.
	 * @param pixelsId for pixelsId 
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnail(long pixelsId, omero.RInt sizeX, omero.RInt sizeY) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnail(pixelsId, sizeX, sizeY);
	}
	
	/**
	 * Get a set of thumbnails.
	 * @param sizeX size of thumbnail.
	 * @param sizeY size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailSet(omero.RInt sizeX, omero.RInt sizeY, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}
	
	/**
	 * Get a set of thumbnails, maintaining aspect ratio.
	 * @param size size of thumbnail.
	 * @param pixelsIds list of ids.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map<Long, byte[]>getThumbnailByLongestSideSet(omero.RInt size, List<Long> pixelsIds) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailByLongestSideSet(size, pixelsIds);
	}
	
	/**
	 * Get the thumbnail of the image, maintain aspect ratio.
	 * @param pixelsId for pixelsId 
	 * @param size size of thumbnail.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	byte[] getThumbnailByLongestSide(long pixelsId, omero.RInt size) throws  DSOutOfServiceException, DSAccessException
	{
		return imageService.getThumbnailByLongestSide(pixelsId, size);
	}
	
	/**
	 * Attach an image to a dataset.
	 * @param dataset 
	 * @param image 
	 * @throws DSOutOfServiceException 
	 * @throws DSAccessException 
	 * 
	 */
	public void attachImageToDataset(Dataset dataset, Image image) throws  DSOutOfServiceException, DSAccessException
	{
		dataService.attachImageToDataset(dataset, image);
	}

	/**
	 * Copy the image and pixels from image.
	 * @param imageId image id to copy.
	 * @param x width of plane.
	 * @param y height of plane.
	 * @param t num timepoints
	 * @param z num zsections.
	 * @param channelList the list of channels to copy.
	 * @param methodology what created the pixels.
	 * @return new id.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description) throws DSOutOfServiceException,
			DSAccessException
	{
		return imageService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, name, description);
	}
	
	public List<Image> getImagesFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImagesFromDataset(dataset);
	}
	
	/**
	 * Get the plane from the image with imageId.
	 * @param imageId see above.
	 * @param z zSection of the plane.
	 * @param c channel of the plane.
	 * @param t timepoint of the plane.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double[][] getPlaneFromImage(long imageId, int z, int c, int t) throws  DSOutOfServiceException, DSAccessException
	{
		return null;
	}
	
	/**
	 * Get the datasets from a project.
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Dataset> getDatasetsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getDatasetsFromProject(project);
	}
	
	/**
	 * Get the Pixels list from the dataset.
	 * @param dataset see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromDataset(Dataset dataset)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromDataset(dataset);
	}
	
	/**
	 * Get the Pixels list from the project.
	 * @param project see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Pixels> getPixelsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getPixelsFromProject(project);
	}
	
	/**
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return map of the pixels-->imageId.
	 */
	public Map<Long, Pixels> getPixelsImageMap(List<Image> images)
	{
		return dataService.getPixelsImageMap(images);
	}


	/**
	 * Get the pixels from the images in the list.
	 * @param images see above.
	 * @return list of the pixels.
	 */
	public List<Pixels> getPixelsFromImageList(List<Image> images)
	{
		return getPixelsFromImageList(images);
	}
	
	/**
	 * Get the images from the dataset with name, this can use wild cards.
	 * @param datasetId see above.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageFromDatasetByName(Long datasetId, String imageName)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImageFromDatasetByName(datasetId, imageName);
	}

	/**
	 * Get the list of images with name containing imageName.
	 * @param imageName see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public List<Image> getImageByName(String imageName)
	throws DSOutOfServiceException, DSAccessException
	{
		return dataService.getImageByName(imageName);
	}
	
}


