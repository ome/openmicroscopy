/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import omero.api.RenderingEnginePrx;
import omero.model.ChannelBinding;
import omero.model.Pixels;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.romio.PlaneDef;

import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;
import omero.log.Logger;

import org.openmicroscopy.shoola.env.rnd.data.DataSink;

import pojos.ChannelData;
import pojos.PixelsData;


/** 
* Factory to create the {@link RenderingControl} proxies.
* This class keeps track of all {@link RenderingControl} instances
* that have been created and are not yet closed. A new
* component is only created if none of the <i>tracked</i> ones is already
* active. Otherwise, the existing proxy is recycled.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @since OME2.2
*/
public class PixelsServicesFactory
{

	/** The percentage of memory used for caching. */
	private static final double		RATIO = 0.10;
	
	/** Values used to determine the size of a cache. */
	private static final int		FACTOR = 
		RenderingControl.MAX_SIZE*RenderingControl.MAX_SIZE;
	
	/** The sole instance. */
	private static PixelsServicesFactory 	singleton;

	/** Reference to the container Registry. */
	private static Registry                 registry;

	/** The maximum amount of memory in bytes used for caching. */
	private static int						maxSize;

	/**
	 * Converts the {@link RenderingDef} into a {@link RndProxyDef}.
	 * 
	 * @param rndDef The object to convert.
	 * @return See above.
	 */
	public static RndProxyDef convert(RenderingDef rndDef)
	{
		if (rndDef == null) return null;
		
		RndProxyDef proxy = new RndProxyDef(rndDef);

		try {
			long v = rndDef.getDetails().getUpdateEvent().getTime().getValue();
			proxy.setLastModified(new Timestamp(v));
		} catch (Exception e) {
			//ignore;
		}
		if (rndDef.getName() != null)
			proxy.setName(rndDef.getName().getValue());
		
		proxy.setDefaultZ(rndDef.getDefaultZ().getValue());
		proxy.setDefaultT(rndDef.getDefaultT().getValue());
		proxy.setColorModel(rndDef.getModel().getValue().getValue());
		
		QuantumDef def = rndDef.getQuantization();
		proxy.setCodomain(def.getCdStart().getValue(), 
				def.getCdEnd().getValue());
		proxy.setBitResolution(def.getBitResolution().getValue());
		
		ChannelBinding c;
		Collection<ChannelBinding> bindings = rndDef.copyWaveRendering();
		Iterator<ChannelBinding> k = bindings.iterator();
		int i = 0;
		int[] rgba;
		ChannelBindingsProxy cb;
		while (k.hasNext()) {
			c = k.next();
			cb = proxy.getChannel(i);
			if (cb == null) {
				cb = new ChannelBindingsProxy();
				proxy.setChannel(i, cb);
			}
			if (c != null) {
				rgba = new int[4];
				rgba[0] = c.getRed().getValue();
				rgba[1] = c.getGreen().getValue();
				rgba[2] = c.getBlue().getValue();
				rgba[3] = c.getAlpha().getValue();
				
				cb.setActive(c.getActive().getValue());
				cb.setInterval(c.getInputStart().getValue(), 
						c.getInputEnd().getValue());
				cb.setRGBA(rgba);
				cb.setQuantization(c.getFamily().getValue().getValue(), 
						c.getCoefficient().getValue(), 
						c.getNoiseReduction().getValue());
			}		
			i++;
		}
		return proxy;
	}
	
	/**
	 * Creates a new instance. This can't be called outside of container b/c 
	 * agents have no refs to the singleton container. So we can be sure this
	 * method is going to create services just once.
	 * 
	 * @param c Reference to the container. 
	 * @return The sole instance.
	 * @throws NullPointerException If the reference to the {@link Container}
	 *                              is <code>null</code>.
	 */
	public static PixelsServicesFactory getInstance(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)  {
			registry = c.getRegistry();
			singleton = new PixelsServicesFactory();
			//Retrieve the maximum heap size.
			MemoryUsage usage = 
				ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			String message = "Heap memory usage: max "+usage.getMax();
			registry.getLogger().info(singleton, message);
			//percentage of memory used for caching.
			maxSize = (int) (RATIO*usage.getMax())/FACTOR; 
		}
		return singleton;
	}

	/**
	 * Creates a new {@link RenderingControl}. We pass a reference to the 
	 * the registry to ensure that agents don't call the method.
	 * 
	 * @param context   	Reference to the registry. To ensure that agents
	 * 						cannot call the method. 
	 * 						It must be a reference to the
	 *                  	container's registry.
	 * @param reList        The {@link RenderingEngine}s.
	 * @param pixels   		The pixels set.
	 * @param metadata  	The channel metadata.
	 * @param compression  	Pass <code>0</code> if no compression otherwise 
	 * 						pass the compression used.
	 * @param defs			The rendering defs linked to the rendering engine.
	 * 						This is passed to speed up the initialization 
	 * 						sequence.
	 * @return See above.
	 * @throws IllegalArgumentException If an Agent try to access the method.
	 */
	public static RenderingControl createRenderingControl(Registry context,
			SecurityContext ctx, List<RenderingEnginePrx> reList, Pixels pixels,
			List<ChannelData> metadata, int compression, List<RndProxyDef> defs)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		return singleton.makeNew(ctx, reList, pixels, metadata, compression,
				defs);
	}

	/**
	 * Reloads the rendering engine.
	 * 
	 * @param context Reference to the registry. To ensure that agents cannot
	 *                call the method. It must be a reference to the
	 *                container's registry.
	 * @param pixelsID	The ID of the pixels set.
	 * @param reList The {@link RenderingEngine}s.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while setting 
	 * the value.
     * @throws DSOutOfServiceException If the connection is broken.
	 */
	public static RenderingControlProxy reloadRenderingControl(Registry context,
			long pixelsID, List<RenderingEnginePrx> reList)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(registry.equals(context)))
			throw new IllegalArgumentException("Not allow to access method.");
		if (reList == null || reList.size() == 0)
			throw new IllegalArgumentException("No RE specified.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
		singleton.rndSvcProxies.get(pixelsID);
		if (proxy != null) {
			proxy.shutDown();
			proxy.setRenderingEngine(reList.get(0));
			reList.remove(0);
			List<RenderingControl> slaves = proxy.getSlaves();
			if (slaves.size() == reList.size()) {
				Iterator<RenderingControl> i = slaves.iterator();
				int index = 0;
				while (i.hasNext()) {
					proxy = (RenderingControlProxy) i.next();
					proxy.shutDown();
					proxy.setRenderingEngine(reList.get(index));
				}
				index++;
			}
		}
		return proxy;
	}

	/**
	 * Resets the rendering engine.
	 * 
	 * @param context Reference to the registry. To ensure that agents cannot
	 *                call the method. It must be a reference to the
	 *                container's registry.
	 * @param pixelsID The ID of the pixels set.
	 * @param reList The {@link RenderingEngine}s.
	 * @param def The rendering def linked to the rendering engine.
	 * This is passed to speed up the initialization sequence.
	 * @return See above.
	 * @throws RenderingServiceException	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static RenderingControlProxy resetRenderingControl(Registry context,
			long pixelsID, List<RenderingEnginePrx> reList, RenderingDef def)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(registry.equals(context)))
			throw new IllegalArgumentException("Not allow to access method.");
		if (reList == null || reList.size() == 0)
			throw new IllegalArgumentException("No RE specified.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
		singleton.rndSvcProxies.get(pixelsID);
		if (proxy != null) {
			RndProxyDef converted = convert(def);
			proxy.resetRenderingEngine(reList.get(0), converted);
			reList.remove(0);
			List<RenderingControl> slaves = proxy.getSlaves();
			if (slaves.size() == reList.size()) {
				Iterator<RenderingControl> i = slaves.iterator();
				int index = 0;
				while (i.hasNext()) {
					proxy = (RenderingControlProxy) i.next();
					proxy.resetRenderingEngine(reList.get(index), converted);
				}
				index++;
			}
		}
		return proxy;
	}
	
	/**
	 * Shuts downs the rendering service attached to the specified 
	 * pixels set. Returns <code>true</code> if the rendering control is shared,
	 * <code>false</code> otherwise.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The ID of the pixels set.
	 * @return See above.
	 */
	public static boolean shutDownRenderingControl(Registry context, 
			long pixelsID)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
			singleton.rndSvcProxies.get(pixelsID);
		Integer count = singleton.rndSvcProxiesCount.get(pixelsID);
		if (proxy != null) {
			if (count == 1) {
				proxy.shutDown();
				singleton.rndSvcProxies.remove(pixelsID);
				singleton.rndSvcProxiesCount.remove(pixelsID);
				getCacheSize();
			} else {
				count--;
				singleton.rndSvcProxiesCount.put(pixelsID, count);
			}
			return (singleton.rndSvcProxiesCount.containsKey(pixelsID));
		}
		return false;
	}
	
	/** 
	 * Shuts downs all running rendering services.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * */
	public static void shutDownRenderingControls(Registry context)
	{
		//Note that the class should be deleted.
		singleton.rndSvcProxies.clear();
		singleton.rndSvcProxiesCount.clear();
	}


	/** 
	 * Checks if the rendering controls are still active. Shuts the inactive
	 * ones.
	 * 
	 * @param context Reference to the registry. To ensure that agents cannot
	 *                call the method. It must be a reference to the
	 *                container's registry.
	 */
	public static void checkRenderingControls(Registry context)
	{
		// TODO Auto-generated method stub
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy;
		Entry<Long, RenderingControl> e;
		Iterator<Entry<Long, RenderingControl>> i =
				singleton.rndSvcProxies.entrySet().iterator();
		Long value = (Long) context.lookup(LookupNames.RE_TIMEOUT);
		 
		long timeout = 60000; //1min
		if (value != null && value.longValue() > timeout)
			timeout = value.longValue();
			
		Logger logger = context.getLogger();
		while (i.hasNext()) {
			e = i.next();
			proxy = (RenderingControlProxy) e.getValue();
			if (!proxy.isProxyActive(timeout)) {
				if (!proxy.shutDown(true))
					logger.info(singleton,
							"Rendering Engine shut down: PixelsID "+e.getKey());
			}
		}
	}
	
	/**
	 * Returns the {@link RenderingControl} linked to the passed set of pixels,
	 * returns <code>null</code> if no proxy associated.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @param init		Pass <code>true</code> if the rendering control
	 * 					is going to be initialized, <code>false</code> 
	 * 					otherwise.
	 * @return See above.
	 */
	public static RenderingControl getRenderingControl(Registry context,
			Long pixelsID, boolean init)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		if (init && singleton.rndSvcProxiesCount.containsKey(pixelsID)) {
			Integer count = singleton.rndSvcProxiesCount.get(pixelsID);
			count++;
			singleton.rndSvcProxiesCount.put(pixelsID, count);
		}
		return singleton.rndSvcProxies.get(pixelsID);
	}
	
	/**
	 * Returns <code>true</code> if the proxy is used elsewhere.
	 * 
	 * @param context Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @return See above.
	 */
	public static boolean isRenderingControlShared(Registry context,
			Long pixelsID)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		if (!singleton.rndSvcProxiesCount.containsKey(pixelsID)) return false;
		Integer count = singleton.rndSvcProxiesCount.get(pixelsID);
		return (count > 1);
	}
	
	/**
	 * Creates a new data sink for the specified set of pixels.
	 * 
	 * @param pixels The pixels set the data sink is for.
	 * @return See above.
	 */
	public static DataSink createDataSink(PixelsData pixels)
	{
		if (pixels == null)
			throw new IllegalArgumentException("Pixels cannot be null.");
		if (singleton.pixelsSource != null && 
				singleton.pixelsSource.isSame(pixels.getId()))
			return singleton.pixelsSource;
		registry.getCacheService().clearAllCaches(); 
		int size = getCacheSize();
		if (size <= 0) size = 0;
		singleton.pixelsSource = DataSink.makeNew(pixels, registry, size);
		return singleton.pixelsSource;
	}

	/**
	 * Shuts downs the data sink attached to the specified pixels set.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The ID of the pixels set.
	 */
	public static void shutDownDataSink(Registry context, long pixelsID)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		if (singleton.pixelsSource != null && 
				singleton.pixelsSource.isSame(pixelsID)) {
			int size = getCacheSize();
			boolean cacheInMemory = true;
			if (size <= 0) cacheInMemory = false;
			singleton.pixelsSource.clearCache();
			singleton.pixelsSource.setCacheInMemory(cacheInMemory);
		}
	}
	
	/**
	 * Renders the specified {@link PlaneDef 2D-plane}.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @param pDef      The plane to render.
	 * @param largeImage Indicates to set the resolution to <code>0</code>.
	 * @param compression The compression level.
	 * @return          The image representing the plane.
	 * @return See above.
	 * 
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static Object render(Registry context, SecurityContext ctx,
			Long pixelsID, PlaneDef pDef, boolean largeImage,
			int compression)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		int level = proxy.getSelectedResolutionLevel();
		if (compression < RenderingControl.UNCOMPRESSED)
		    compression = RenderingControl.UNCOMPRESSED;
		if (compression > RenderingControl.LOW) 
		    compression = RenderingControl.LOW;
		proxy.setCompression(compression);
		if (largeImage) {
			proxy.setSelectedResolutionLevel(0);
		}
		Object o = proxy.render(pDef);
		if (largeImage) proxy.setSelectedResolutionLevel(level);
		return o;
	}

	/**
	 * Renders the specified {@link PlaneDef 2D-plane}.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @param pDef      The plane to render.
	 * @param tableID	The id of the table hosting the mask.
	 * @param overlays	The overlays to render or <code>null</code>.
	 * @return See above.
	 * 
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static Object renderOverlays(Registry context, long pixelsID, 
			PlaneDef pd, long tableID, Map<Long, Integer> overlays)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		proxy.setOverlays(tableID, overlays);
		return proxy.render(pd);
	}
	
	/**
	 * Renders the projected images.
	 * 
	 * @param context	Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @param startZ	The first optical section.
     * @param endZ     	The last optical section.
     * @param stepping 	Stepping value to use while calculating the projection.
     * @param type 	   	One of the projection type defined by this class.
     * @param channels The collection of channels to project.
     * @return See above.
     * @throws RenderingServiceException 	If an error occurred while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static BufferedImage renderProjected(Registry context, Long pixelsID, 
			int startZ, int endZ, int type, int stepping, 
			List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		
		return proxy.renderProjected(startZ, endZ, stepping, type, channels);
	}

	/**
	 * Returns the compression quality related to the passed level.
	 * 
	 * @param compressionLevel The level to handle.
	 * @return See above.
	 */
	static final float getCompressionQuality(int compressionLevel)
	{
		Float value;
		switch (compressionLevel) {
			default:
			case RenderingControl.UNCOMPRESSED:
			case RenderingControl.MEDIUM:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_MEDIUM_QUALITY);
				return value.floatValue();
			case RenderingControl.LOW:
				value = (Float) registry.lookup(
						LookupNames.COMPRESSIOM_LOW_QUALITY);
				return value.floatValue();
		}
	}
	
	/** Keep track of all the rendering service already initialized. */
	private Map<Long, RenderingControl>	rndSvcProxies;

	/** Access to the raw data. */
	private DataSink					pixelsSource;

	/**
	 * Keep track of the number of times the rendering proxy has been requested
	 * to be initialized.
	 */
	private Map<Long, Integer>			rndSvcProxiesCount;
	
	/** Creates the sole instance. */
	private PixelsServicesFactory()
	{
		rndSvcProxies = new HashMap<Long, RenderingControl>();
		rndSvcProxiesCount = new HashMap<Long, Integer>();
	}
	
	/**
	 * Makes a new {@link RenderingControl}.
	 * 
	 * @param reList The rendering engines.
	 * @param pixels The pixels set.
	 * @param metadata The related metadata.
	 * @param compression Pass <code>0</code> if no compression otherwise 
	 * 					 pass the compression used.
	 * @param defs The rendering definitions linked to the 
	 * rendering engine.This is passed to speed up the initialization sequence.
	 * @return See above.
	 */
	private RenderingControl makeNew(SecurityContext ctx, 
			List<RenderingEnginePrx> reList,
			Pixels pixels, List<ChannelData> metadata, int compression,
			List<RndProxyDef> defs)
	{
		if (singleton == null) throw new NullPointerException();
		Long id = pixels.getId().getValue();
		RenderingControl rnd = 
			(RenderingControl) singleton.rndSvcProxies.get(id);
		if (rnd != null) return rnd;
		singleton.rndSvcProxiesCount.put(id, 1);
		RenderingEnginePrx master = reList.get(0);
		int size = getCacheSize();
		reList.remove(0);
		rnd = new RenderingControlProxy(registry, ctx, master, pixels, metadata,
										compression, defs, size);
		Iterator<RenderingEnginePrx> i = reList.iterator();
		if (reList.size() > 0) {
			List<RenderingControl> 
			slaves = new ArrayList<RenderingControl>(reList.size());
			while (i.hasNext()) {
				slaves.add(new RenderingControlProxy(registry, ctx, 
						i.next(), pixels, metadata, compression, defs, size));
			}
			((RenderingControlProxy) rnd).setSlaves(slaves);
		}
		
		singleton.rndSvcProxies.put(id, rnd);
		return rnd;
	}
	
	/**
	 * Returns the size of the cache.
	 * 
	 * @return See above.
	 */
	private static int getCacheSize()
	{
		MemoryUsage usage = 
			ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		//percentage of memory used for caching.
		maxSize = (int) (RATIO*(usage.getMax()-usage.getUsed()))/FACTOR; 
		int m = singleton.rndSvcProxies.size();
		int n = 0;
		int sizeCache = 0;
		RenderingControlProxy proxy;
		Entry<Long, RenderingControl> entry;
		Iterator<Entry<Long, RenderingControl>> i;
		if (singleton.pixelsSource != null) n = 1;
		if (n == 0 && m == 0) return maxSize*FACTOR;
		else if (n == 0 && m > 0) {
			sizeCache = (maxSize/(m+1))*FACTOR;
			//reset all the image caches.
			i = singleton.rndSvcProxies.entrySet().iterator();
			while (i.hasNext()) {
				entry = i.next();
				proxy = (RenderingControlProxy) entry.getValue();
				proxy.setCacheSize(sizeCache);
			}
			return sizeCache;
		} else if (m == 0 && n > 0) {
			sizeCache = (maxSize/(n+1))*FACTOR;
			return sizeCache;
		}
		sizeCache = (maxSize/(m+n+1))*FACTOR;
		//reset all the image caches.
		i = singleton.rndSvcProxies.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			proxy = (RenderingControlProxy) entry.getValue();
			proxy.setCacheSize(sizeCache);
		}
		
		return sizeCache;
	}

}
