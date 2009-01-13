/*
 * ome.services.blitz.omerogateway.OmeroGateway 
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
package ome.services.blitz.gateway;


//Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Third-party libraries
import Ice.Current;

//Application-internal dependencies
import ome.services.blitz.gateway.services.DataService;
import ome.services.blitz.gateway.services.GatewayFactory;
import ome.services.blitz.gateway.services.ImageService;
import ome.services.blitz.gateway.services.impl.ImageServiceImpl;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import omero.RInt;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api.BufferedImage;
import omero.api.ContainerClass;
import omero.api.IScriptPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.api._GatewayOperations;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
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
public class OmeroGateway
	implements ServiceFactoryAware, _GatewayOperations, BlitzOnly
{
	
	/** The service factory to create different service. */
	private ServiceFactoryPrx 	serviceFactory;

	/** The image service provides methods to manipulate the image structures.*/
	private ImageService 		imageService;
	
	/** The dataservice provides methods for manipulating data objects. */
	private DataService 		dataService;
	
	/** The gateway factory which controls access to the basic services. */
	private GatewayFactory		gatewayFactory;
	
	/**
	 * Initialize the service factory which creates the gateway and services
	 * and links the different services together.  
	 * 
	 * @param client an already existing client object.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	public void setServiceFactory(ServiceFactoryI sf) throws ServerError
	{
	    Ice.Identity id = sf.sessionId();
	    serviceFactory = ServiceFactoryPrxHelper.checkedCast(sf.getAdapter().createProxy(id));
	    createServices();
	}
	
	/**
	 * Create all the base services which provide additional functionality to 
	 * manipulate omero objects.
	 * @throws ServerError 
	 */
	private void createServices() throws ServerError
	{
		gatewayFactory = new GatewayFactory(serviceFactory);
		imageService = gatewayFactory.getImageService();
		dataService = gatewayFactory.getDataService();
	}

	private void close() throws ServerError
	{
		imageService = null;
		dataService = null;
		gatewayFactory.closeService();
	}
	

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#attachImageToDataset(omero.model.Dataset, omero.model.Image, Ice.Current)
	 */
	public void attachImageToDataset(Dataset dataset, Image image,
			Current __current) throws ServerError
	{
		dataService.attachImageToDataset(dataset, image);
	}


	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#copyPixels(long, java.util.List, java.lang.String, Ice.Current)
	 */
	public long copyPixels(long pixelsID, List<Integer> channelList,
			String methodology, Current __current) throws ServerError
	{
		return imageService.copyPixels(pixelsID, channelList, methodology);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#copyPixelsXYTZ(long, int, int, int, int, java.util.List, java.lang.String, Ice.Current)
	 */
	public long copyPixelsXYTZ(long pixelsID, int x, int y, int t, int z,
			List<Integer> channelList, String methodology, Current __current)
			throws ServerError
	{
		return imageService.copyPixels(pixelsID, x, y, t, z, channelList, 
			methodology);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#createImage(int, int, int, int, java.util.List, omero.model.PixelsType, java.lang.String, java.lang.String, Ice.Current)
	 */
	public long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
			List<Integer> channelList, PixelsType pixelsType, String name,
			String description, Current __current) throws ServerError
	{
		return imageService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, 
			pixelsType, name, description);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#deleteObject(omero.model.IObject, Ice.Current)
	 */
	public void deleteObject(IObject row, Current __current) throws ServerError
	{
		dataService.deleteObject(row);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#findAllByQuery(java.lang.String, Ice.Current)
	 */
	public List<IObject> findAllByQuery(String myQuery, Current __current)
			throws ServerError
	{
		return dataService.findAllByQuery(myQuery);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#findByQuery(java.lang.String, Ice.Current)
	 */
	public IObject findByQuery(String myQuery, Current __current)
			throws ServerError
	{
		return dataService.findByQuery(myQuery);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getDataset(long, boolean, Ice.Current)
	 */
	public Dataset getDataset(long datasetId, boolean leaves, Current __current)
			throws ServerError
	{
		List<Long> datasetIdList = new ArrayList();
		datasetIdList.add(datasetId);
		List<Dataset> datasets = getDatasets(datasetIdList, leaves, __current);
		if(datasets.size()==1)
			return datasets.get(0);
		return null;	
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getDatasets(java.util.List, boolean, Ice.Current)
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean withLeaves,
			Current __current) throws ServerError
	{
		return dataService.getDatasets(ids, withLeaves);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getImage(long, Ice.Current)
	 */
	public Image getImage(long id, Current __current) throws ServerError
	{
		return imageService.getImage(id);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getImageByName(java.lang.String, Ice.Current)
	 */
	public List<Image> getImageByName(String imageName, Current __current)
			throws ServerError
	{
		return dataService.getImageByName(imageName);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getImageFromDatasetByName(long, java.lang.String, Ice.Current)
	 */
	public List<Image> getImageFromDatasetByName(long datasetId,
			String imageName, Current __current) throws ServerError
	{
		return dataService.getImageFromDatasetByName(datasetId, imageName);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getImages(omero.api.ContainerClass, java.util.List, Ice.Current)
	 */
	public List<Image> getImages(ContainerClass parentType, List<Long> ids,
			Current __current) throws ServerError
	{
		return dataService.getImages(parentType, ids);
	}


	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getPixelType(java.lang.String, Ice.Current)
	 */
	public PixelsType getPixelType(String type, Current __current)
			throws ServerError
	{
		return dataService.getPixelType(type);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getPixelTypes(Ice.Current)
	 */
	public List<PixelsType> getPixelTypes(Current __current) throws ServerError
	{
		return dataService.getPixelTypes();
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getPixels(long, Ice.Current)
	 */
	public Pixels getPixels(long pixelsId, Current __current)
			throws ServerError
	{
		return imageService.getPixels(pixelsId);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getPixelsFromImage(long, Ice.Current)
	 */
	public List<Pixels> getPixelsFromImage(long imageId, Current __current)
			throws ServerError
	{
		return dataService.getPixelsFromImage(imageId);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getProjects(java.util.List, boolean, Ice.Current)
	 */
	public List<Project> getProjects(List<Long> ids, boolean withLeaves,
			Current __current) throws ServerError
	{
		return dataService.getProjects(ids, withLeaves);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getRenderedImage(long, int, int, Ice.Current)
	 */
	public int[] getRenderedImage(long pixelsId, int z, int t,
			Current __current) throws ServerError
	{
		return imageService.getRenderedImage(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getRenderedImageMatrix(long, int, int, Ice.Current)
	 */
	public int[][][] getRenderedImageMatrix(long pixelsId, int z, int t,
			Current __current) throws ServerError
	{
		return imageService.getRenderedImageMatrix(pixelsId, z, t);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getThumbnail(long, omero.RInt, omero.RInt, Ice.Current)
	 */
	public byte[] getThumbnail(long pixelsId, RInt sizeX, RInt sizeY,
			Current __current) throws ServerError
	{
		return imageService.getThumbnail(pixelsId, sizeX, sizeY);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#getThumbnailSet(omero.RInt, omero.RInt, java.util.List, Ice.Current)
	 */
	public Map<Long, byte[]> getThumbnailSet(RInt sizeX, RInt sizeY,
			List<Long> pixelsIds, Current __current) throws ServerError
	{
		return imageService.getThumbnailSet(sizeX, sizeY, pixelsIds);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#saveAndReturnArray(java.util.List, Ice.Current)
	 */
	public List<IObject> saveAndReturnArray(List<IObject> graph,
			Current __current) throws ServerError
	{
		return dataService.saveAndReturnArray(graph);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#saveAndReturnObject(omero.model.IObject, Ice.Current)
	 */
	public IObject saveAndReturnObject(IObject obj, Current __current)
			throws ServerError
	{
		return dataService.saveAndReturnObject(obj);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#saveArray(java.util.List, Ice.Current)
	 */
	public void saveArray(List<IObject> graph, Current __current)
			throws ServerError
	{
		dataService.saveArray(graph);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#saveObject(omero.model.IObject, Ice.Current)
	 */
	public void saveObject(IObject obj, Current __current) throws ServerError
	{
		dataService.saveObject(obj);
	}

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#setActive(long, int, boolean, Ice.Current)
	 */
	public void setActive(long pixelsId, int w, boolean active,
			Current __current) throws ServerError
	{
		imageService.setActive(pixelsId, w, active);
	}

	
	/* (non-Javadoc)
	 * @see omero.api._StatefulServiceInterfaceOperations#close_async(omero.api.AMD_StatefulServiceInterface_close, Ice.Current)
	 */
	public void close_async(AMD_StatefulServiceInterface_close __cb,
			Current __current) throws ServerError
	{
		  close();
		  __cb.ice_response();
	}

	/* (non-Javadoc)
	 * @see omero.api._StatefulServiceInterfaceOperations#getCurrentEventContext_async(omero.api.AMD_StatefulServiceInterface_getCurrentEventContext, Ice.Current)
	 */
	public void getCurrentEventContext_async(
			AMD_StatefulServiceInterface_getCurrentEventContext __cb,
			Current __current) throws ServerError
	{
		__cb.ice_exception(new omero.InternalException(null,null,"NYI"));
	}	

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#copyImage(long, int, int, int, int, java.util.List, java.lang.String, Ice.Current)
	 */
	public long copyImage(long imageId, int x, int y, int t, int z,
			List<Integer> channelList, String imageName, Current __current)
			throws ServerError
	{
		return imageService.copyImage(imageId, x, y, t, z, channelList,"");
	}
	 
	/**
	 * Keep service alive.
	 * @throws DSOutOfServiceException
	 * @throws omero.ServerError
	 */
	
	public void keepAlive(Ice.Current current) throws omero.ServerError
	{
		gatewayFactory.keepAlive();
	}
	

	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#uploadPlane(long, int, int, int, double[][], Ice.Current)
	 */
	public void uploadPlane(long pixelsId, int z, int c, int t,
			byte[] data, Current __current) throws ServerError
	{
		imageService.uploadPlane(pixelsId, z, c, t, data);
	}
	
	/* (non-Javadoc)
	 * @see omero.api._GatewayOperations#updatePixels(omero.model.Pixels, Ice.Current)
	 */
	public Pixels updatePixels(Pixels pixels, Current __current)
			throws ServerError
	{
		return dataService.updatePixels(pixels);
	}

	/**
	 * Get the raw plane for the pixels pixelsId, this returns a 2d array 
	 * representing the plane, it returns doubles but will not lose data.
	 * @param pixelsId id of the pixels to retrieve.
	 * @param c the channel of the pixels to retrieve.
	 * @param t the time point to retrieve.
	 * @param z the z section to retrieve.
	 * @return The raw plane in 2-d array. 
	 * @throws omero.ServerError 
	 */
	public byte[] getPlane(long pixelsId, int z, int c, int t, Ice.Current current)
		throws omero.ServerError
	{
		return imageService.getRawPlane(pixelsId, z, c, t);
	}
	
	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#deleteScript(long)
	 */
	public void deleteScript(long id, Current __current) throws ServerError 
	{
		dataService.deleteScript(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getParams(long)
	 */
	public Map<String, RType> getParams(long id, Current __current) throws ServerError 
	{
		return dataService.getParams(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScript(long)
	 */
	public String getScript(long id, Current __current) throws ServerError 
	{
		return dataService.getScript(id);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScriptID(String)
	 */
	public long getScriptID(String name, Current __current) throws ServerError 
	{
		return dataService.getScriptID(name);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#getScripts()
	 */
	public Map<Long, String> getScripts(Current __current) throws ServerError 
	{
		return dataService.getScripts();
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#runScript(long, Map<String, RType>)
	 */
	public Map<String, RType> runScript(long id, Map<String, RType> map,
			Current __current) throws ServerError 
	{
		return dataService.runScript(id, map);
	}

	/* (non-Javadoc)
	 * @see ome.services.blitz.omerogateway.services.DataService#uploadScript(String)
	 */
	public long uploadScript(String script, Current __current) throws ServerError 
	{
		return dataService.uploadScript(script);
	}

}


