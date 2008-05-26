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
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import blitzgateway.service.gateway.GatewayFactory;
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
		dataService = new DataServiceImpl(gatewayFactory.getIPojoGateway(), 
										  gatewayFactory.getIQueryGateway(), 
										  gatewayFactory.getITypeGateway());
		imageService = new ImageServiceImpl(
									gatewayFactory.getRawPixelsStoreGateway(), 
									gatewayFactory.getIPixelsGateway(), 
									gatewayFactory.getIQueryGateway(), 
									gatewayFactory.getIUpdateGateway());
		fileService = new FileServiceImpl(gatewayFactory.getRawFileStoreGateway(), 
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
	 * @param ids ids of the datasets to get the projects from.
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
	 * @param imageID image id relating to the pixels.
	 * @return see above.
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels(long imageID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPixels(imageID);
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
}


