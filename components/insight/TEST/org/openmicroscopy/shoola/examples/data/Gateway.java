/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import omero.client;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/** 
 * Entry point to access the services. Code should be provided to keep those
 * services alive.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class Gateway 
{

	/** 
	 * The maximum number of thumbnails retrieved before restarting the
	 * thumbnails service.
	 */
	private static final int				MAX_RETRIEVAL = 100;
	
	/** Keeps the client's session alive. */
	private ScheduledThreadPoolExecutor	executor;
	
	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO Server using a secure connection. 
	 */
	private client	secureClient;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactoryPrx entryEncrypted;
	
	/**
	 * Information about the current login.
	 */
	private EventContext eventContext;

	/** Flag indicating if you are connected or not. */
	private boolean connected;
	
	/** Collection of services to keep alive. */
	private List<ServiceInterfacePrx>				services;
	
	/** Collection of services to keep alive. */
	private Map<Long, StatefulServiceInterfacePrx>	reServices;
	
	/** The container service. */
	private IContainerPrx							containerService;
	
	/** The Admin service. */
	private IAdminPrx								adminService;
	
	/** The thumbnail service. */
	private ThumbnailStorePrx						thumbnailService;
	
	
	/**
	 * The number of thumbnails already retrieved. Resets to <code>0</code>
	 * when the value equals {@link #MAX_RETRIEVAL}.
	 */
	private int										thumbRetrieval;
	
	/**
	 * Creates a <code>BufferedImage</code> from the passed array of bytes.
	 * 
	 * @param values    The array of bytes.
	 * @return See above.
	 * @throws RenderingServiceException If we cannot create an image.
	 */
	private BufferedImage createImage(byte[] values) 
	{
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(values);
			BufferedImage image = ImageIO.read(stream);
			image.setAccelerationPriority(1f);
			return image;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the {@link ThumbnailStorePrx} service.
	 *   
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private ThumbnailStorePrx getThumbService()
	{ 
		try {
			if (thumbRetrieval == MAX_RETRIEVAL) {
				thumbRetrieval = 0;
				//to be on the save side
				if (thumbnailService != null) thumbnailService.close();
				services.remove(thumbnailService);
				thumbnailService = null;
			}
			if (thumbnailService == null) {
				thumbnailService = entryEncrypted.createThumbnailStore();
				services.add(thumbnailService);
			}
			thumbRetrieval++;
			return thumbnailService; 
		} catch (Throwable e) {
			//TODO: handle exception
		}
		return null;
	}
	
	/**
	 * Returns the {@link IContainerPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IContainerPrx getContainerService()
	{ 
		try {
			if (containerService == null) {
				containerService = entryEncrypted.getContainerService();
				services.add(containerService);
			}
			return containerService; 
		} catch (Throwable e) {
			//Handle exception here.
		}
		return null;
	}
	
	/**
	 * Returns the {@link IAdminPrx} service.
	 * 
	 * @return See above.
	 */
	private IAdminPrx getAdminService()
	{ 
		try {
			if (adminService == null) {
				adminService = entryEncrypted.getAdminService(); 
				services.add(adminService);
			}
			return adminService; 
		} catch (Throwable e) {
			//Handle exception.
		}
		return null;
	}
	
	/**
	 * Returns the {@link RenderingEnginePrx Rendering service}.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RenderingEnginePrx getRenderingService()
	{
		try {
			RenderingEnginePrx engine = entryEncrypted.createRenderingEngine();
			return engine;
		} catch (Throwable e) {
			//handle exception
		}
		return null;
	}
	
	/** Creates a new instance. */
	public Gateway()
	{
		connected = false;
		services = new ArrayList<ServiceInterfacePrx>();
		reServices = new HashMap<Long, StatefulServiceInterfacePrx>();
	}
	
	/** Keeps the services alive.*/
	void keepSessionAlive()
	{
		int n = services.size()+reServices.size();
		ServiceInterfacePrx[] entries = new ServiceInterfacePrx[n];
		Iterator<ServiceInterfacePrx> i = services.iterator();
		int index = 0;
		while (i.hasNext()) {
			entries[index] = i.next();
			index++;
		}
		Iterator<Long> j = reServices.keySet().iterator();
		while (j.hasNext()) {
			entries[index] = reServices.get(j.next());
			index++;
		}
		try {
			entryEncrypted.keepAllAlive(entries);
		} catch (Exception e) {
			//Handle exception. Here
		}
	}
	
	/** 
	 * Logs in. Returns <code>true</code> if connected, <code>false</code>
	 * otherwise.
	 * 
	 * @param credentials Host the information to connect.
	 * @return
	 */
	public boolean login(LoginCredentials credentials)
	throws Exception
	{
		//read login file
		//parse
		secureClient = new client(credentials.getHostName(), 
				credentials.getPort());
		try {
			entryEncrypted = secureClient.createSession(
					credentials.getUserName(), credentials.getPassword());
			eventContext = getAdminService().getEventContext();
			connected = true;
			KeepClientAlive kca = new KeepClientAlive(this);
			executor = new ScheduledThreadPoolExecutor(1);
	        executor.scheduleWithFixedDelay(kca, 60, 60, TimeUnit.SECONDS);
			
		} catch (Exception e) {
			throw new Exception("Cannot log in");
		}
		return connected;
	}

	/** Logs out and closes the session.*/
	public void shutdDown()
	{
		if (executor != null) executor.shutdown();
        executor = null;
		connected = false;
		thumbnailService = null;
		adminService = null;
		services.clear();
		reServices.clear();
		try {
			if (secureClient != null) secureClient.__del__();
			secureClient = null;
			entryEncrypted = null;
		} catch (Exception e) {
			//session already dead.
		} finally {
			secureClient = null;
			entryEncrypted = null;
		}
	}
	
	/**
	 * Returns the images owned by the user currently logged in.
	 * We use the <code>Pojo</code> objects so we don't have to deal
	 * directly with the rtypes.
	 * 
	 * @return See above.
	 */
	public List<ImageData> getImages()
		throws Exception
	{
		List<ImageData> images = new ArrayList<ImageData>();
		try {
			ParametersI po = new ParametersI();
			po.exp(omero.rtypes.rlong(eventContext.userId));
			IContainerPrx service = getContainerService();
			List<Image> l = service.getUserImages(po);
			//stop here if you want to deal with IObject.
			if (l == null) return images;
			Iterator<Image> i = l.iterator();
			while (i.hasNext()) {
				images.add(new ImageData(i.next()));
			}
		} catch (Exception e) {
			throw new Exception("Cannot retrieve the images", e);
		}
		return images;
	}

	/**
	 * Returns the datasets owned by the user currently logged in.
	 * We use the <code>Pojo</code> objects so we don't have to deal
	 * directly with the rtypes.
	 * 
	 * @param ids
	 * @return See above.
	 * @throws Exception
	 */
	public List<DatasetData> getDatasets(List<Long> ids)
		throws Exception
	{
		List<DatasetData> datasets = new ArrayList<DatasetData>();
		ParametersI po = new ParametersI();
		po.exp(omero.rtypes.rlong(eventContext.userId));
		if (ids == null || ids.size() == 0)
			po.noLeaves();
		else po.leaves();
		IContainerPrx service = getContainerService();
		try {
			List<IObject> objects = service.loadContainerHierarchy(
					Dataset.class.getName(), ids, po);
			if (objects == null) return datasets;
			Iterator<IObject> i = objects.iterator();
			
			while (i.hasNext()) {
				datasets.add(new DatasetData((Dataset) i.next()));
			}
			return datasets;
		} catch (Exception e) {
			throw new Exception("Cannot retrieve the datasets", e);
		}
	}
	
	/**
	 * Loads the rendering control corresponding to the specified
	 * set of pixels.
	 * 
	 * @param pixelsID The identifier of the pixels set.
	 * @return See above.
	 */
	public RenderingEnginePrx loadRenderingControl(long pixelsID)
		throws Exception
	{
		try {
			RenderingEnginePrx service = 
				(RenderingEnginePrx) reServices.get(pixelsID);
			if (service != null) return service;
			service = getRenderingService();
			reServices.put(pixelsID, service);
			service.lookupPixels(pixelsID);
			if (!(service.lookupRenderingDef(pixelsID))) {
				service.resetDefaultSettings(true);
				service.lookupRenderingDef(pixelsID);
			}
			service.load();
			return service;
		} catch (Throwable t) {
			throw new Exception("Cannot load rendering engine", t);
		}
	}
	
	/**
	 * Retrieves the specified images.
	 * 
	 * @param pixelsID The identifier of the images.
	 * @param max The maximum length of a thumbnail.
	 * @return See above.
	 * @throws Exception
	 */
	public List<BufferedImage> getThumbnailSet(List pixelsID, int max)
		throws Exception
	{
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		try {
			ThumbnailStorePrx service = getThumbService();
			Map<Long, byte[]> results = service.getThumbnailByLongestSideSet(
					omero.rtypes.rint(max), pixelsID);
			
			if (results == null) return images;
			Entry entry;
			Iterator i = results.entrySet().iterator();
			BufferedImage image;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				try {
					image = createImage((byte[])entry.getValue());
					if (image != null)
						images.add(image);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			return images;
					
		} catch (Throwable t) {
			if (thumbnailService != null) {
				try {
					thumbnailService.close();
				} catch (Exception e) {
					//nothing we can do
				}
			}
			thumbnailService = null;
			
		}
		return images;
	}
	
	/**
	 * Returns the XY-plane identified by the passed z-section, time-point 
	 * and wavelength.
	 * 
	 * @param pixelsID 	The id of pixels containing the requested plane.
	 * @param z			The selected z-section.
	 * @param t			The selected time-point.
	 * @param c			The selected wavelength.
	 * @return See above.
	 */
	 public synchronized byte[] getPlane(long pixelsID, int z, int t, int c)
	 	throws Exception
	 {
		 RawPixelsStorePrx service = entryEncrypted.createRawPixelsStore();
		 try {
			 service.setPixelsId(pixelsID, false);
			 return service.getPlane(z, c, t);
		 } catch (Throwable e) {
			 throw new Exception("Cannot retrieve the plane " +
					 "(z="+z+", t="+t+", c="+c+") for pixelsID:  "+pixelsID, e);
		 } finally {
			 service.close();
		 }
	}
	 
}
