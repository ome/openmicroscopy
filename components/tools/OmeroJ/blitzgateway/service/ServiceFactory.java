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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.service.gateway.GatewayFactory;
import blitzgateway.util.OMEROClass;
import omero.model.Dataset;
import omero.model.Format;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;

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
	
	/** The dataservice object. */
	private DataService 	dataService;
	
	/** The Image service object. */
	private ImageService	imageService;

	/** The File service object. */
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
		fileService = new FileServiceImpl(
									gatewayFactory.getRawFileStoreGateway(), 
									gatewayFactory.getIQueryGateway());
	}
	
	/**
	 * Get the projects in the OMERO.Blitz server in the user account.
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
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param withLeaves get the datasets too.
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
	 * Get the projects in the OMERO.Blitz server in the user account.
	 * @param withLeaves get the datasets too.
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
	 * Get the rawplane for the image id imageId.
	 * @param imageID id of the image to retrieve.
	 * @param c the channel of the image to retrieve.
	 * @param t the time point to retrieve.
	 * @param z the z section to retrieve.
	 * @return The rawplane in 2-d array of doubles. 
	 * @throws DSAccessException 
	 * @throws DSOutOfServiceException 
	 */
	public double[][] getPlane(long imageID, int c, int t, int z) 
		throws DSOutOfServiceException, DSAccessException
	{
		return imageService.getPlane(imageID, c, t, z);
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
	 * Test method to make sure the client converts it's data to the server data
	 * correctly. 
	 * @param pixelsId pixels id to upload to .  
	 * @param c channel.
	 * @param t time point.
	 * @param z z section.
	 * @return the converted data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException 
	 */
	public double[][] testVal(long pixelsId, int z, int c, int t) 
	throws DSOutOfServiceException, DSAccessException
	{
		return imageService.testVal(pixelsId, z, c, t);
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
	 * @param c channel.
	 * @param t time point.
	 * @param z z section. 
	 * @param data plane data. 
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void uploadPlane(long pixelsId, int c, int t, int z, 
			double [][] data) throws DSOutOfServiceException, DSAccessException
	{
		imageService.uploadPlane(pixelsId, c, t, z, data);
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
	 * Get the pixelsTypes.
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
	 * Get the original file with id.
	 * @param id see above.
	 * @return the original file.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public OriginalFile getOriginalFile(long id)
	throws DSOutOfServiceException, DSAccessException
	{
		return fileService.getOriginalFile(id);
	}
	
	/**
	 * Get the file as String with id.
	 * @param id see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public String getFileAsString(long id)
	throws DSOutOfServiceException, DSAccessException
	{
		return fileService.getFileAsString(id);
	}
	
	/**
	 * Get the file as raw bytes with id.
	 * @param id see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] getRawFile(long id)
	throws DSOutOfServiceException, DSAccessException
	{
		return fileService.getRawFile(id);
	}
	
	/**
	 * Does an original file with id exist.
	 * @param id see above.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public boolean fileExists(long id)
	throws DSOutOfServiceException, DSAccessException
	{
		return fileService.fileExists(id);
	}
	
	/**
	 * Get all file id's for filename with format fmt.
	 * @param fileName see above.
	 * @param fmt see above.
	 * @return list of file ids.
	 * @throws DSAccessException
	 * @throws DSOutOfServiceException
	 */
	public List<Long> findFile(String fileName, Format fmt) 
	throws DSAccessException, DSOutOfServiceException
	{
		return fileService.findFile(fileName, fmt);
	}
	
	/**
	 * Get all the formats in the system.
	 * @return see above.
	 * @throws DSAccessException
	 * @throws DSOutOfServiceException
	 */
	public List<Format> getAllFormats() 
	throws DSAccessException, DSOutOfServiceException
	{
		return fileService.getAllFormats();
	}
	
	/**
	 * Get the format with id.
	 * @param id see above.
	 * @return see above.
	 * @throws DSAccessException
	 * @throws DSOutOfServiceException
	 */
	public Format getFormat(long id)
	throws DSAccessException, DSOutOfServiceException
	{
		return fileService.getFileFormat(id);
	}
	
	/**
	 * Get the format with name fmt.
	 * @param fmt see above.
	 * @return see above.
	 * @throws DSAccessException
	 * @throws DSOutOfServiceException
	 */
	public Format getFormat(String fmt)
	throws DSAccessException, DSOutOfServiceException
	{
		return fileService.getFormat(fmt);
	}
}


