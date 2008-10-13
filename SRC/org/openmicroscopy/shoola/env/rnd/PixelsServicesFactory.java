/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlFactory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.model.ChannelBinding;
import omero.model.Color;
import omero.model.Pixels;
import omero.model.QuantumDef;
import omero.model.RenderingDef;
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import pojos.PixelsData;


/** 
* Factory to create the {@link RenderingControl} proxies.
* This class keeps track of all {@link RenderingControl} instances
* that have been created and are not yet shutted down. A new
* component is only created if none of the <i>tracked</i> ones is already
* active. Otherwise, the existing proxy is recycled.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
public class PixelsServicesFactory
{

	/** The percentage of memory used for caching. */
	private static final double		RATIO = 0.10;
	
	/** Values used to determine the size of a cache. */
	private static final int		FACTOR = 1024*1024;
	
	/** The sole instance. */
	private static PixelsServicesFactory 	singleton;

	/** Reference to the container registry. */
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
		RndProxyDef proxy = new RndProxyDef();
		proxy.setDefaultZ(rndDef.getDefaultZ().val);
		proxy.setDefaultT(rndDef.getDefaultT().val);
		proxy.setColorModel(rndDef.getModel().getValue().val);
		
		QuantumDef def = rndDef.getQuantization();
		proxy.setCodomain(def.getCdStart().val, def.getCdEnd().val);
		proxy.setBitResolution(def.getBitResolution().val);
		
		ChannelBinding c;
		Collection bindings = rndDef.copyWaveRendering();
		
		Color color;
		Iterator k = bindings.iterator();
		int i = 0;
		int[] rgba;
		ChannelBindingsProxy cb;
		while (k.hasNext()) {
			c = (ChannelBinding) k.next();
			cb = proxy.getChannel(i);
			if (cb == null) {
				cb = new ChannelBindingsProxy();
				proxy.setChannel(i, cb);
			}
			if (c != null) {
				rgba = new int[4];
				color = c.getColor();
				rgba[0] = color.getRed().val;
				rgba[1] = color.getGreen().val;
				rgba[2] = color.getBlue().val;
				rgba[3] = color.getAlpha().val;
				
				
				cb.setActive(c.getActive().val);
				cb.setInterval(c.getInputStart().val, c.getInputEnd().val);
				cb.setRGBA(rgba);
				cb.setQuantization(c.getFamily().getValue().val, 
						c.getCoefficient().val, c.getNoiseReduction().val);
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
	 * @param re        	The {@link RenderingEngine rendering service}.        
	 * @param pixels   		The pixels set.
	 * @param metadata  	The channel metadata.
	 * @param compression  	Pass <code>0</code> if no compression otherwise 
	 * 						pass the compression used.
	 * @param def			The rendering def linked to the rendering engine.
	 * 						This is passed to speed up the initialization 
	 * 						sequence.
	 * @return See above.
	 * @throws IllegalArgumentException If an Agent try to access the method.
	 */
	public static RenderingControl createRenderingControl(Registry context, 
			RenderingEnginePrx re, Pixels pixels, List metadata, 
			int compression, RenderingDef def)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		return singleton.makeNew(re, pixels, metadata, compression, def);
	}

	/**
	 * Reloads the rendering engine.
	 * 
	 * @param context	Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID	The ID of the pixels set.
	 * @param re		The {@link RenderingEngine rendering service}.
	 * @return See above.
	 * @throws RenderingServiceException	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static RenderingControlProxy reloadRenderingControl(Registry context, 
			long pixelsID, RenderingEnginePrx re)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(registry.equals(context)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
		singleton.rndSvcProxies.get(new Long(pixelsID));
		if (proxy != null) {
			proxy.shutDown();
			proxy.setRenderingEngine(re);
		}
		return proxy;
	}

	/**
	 * Resets the rendering engine.
	 * 
	 * @param context	Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID	The ID of the pixels set.
	 * @param re		The {@link RenderingEngine rendering service}.
	 * @param def		The rendering def linked to the rendering engine.
	 * 					This is passed to speed up the initialization 
	 * 					sequence.
	 * @return See above.
	 * @throws RenderingServiceException	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static RenderingControlProxy resetRenderingControl(Registry context, 
			long pixelsID, RenderingEnginePrx re, RenderingDef def)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(registry.equals(context)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
		singleton.rndSvcProxies.get(new Long(pixelsID));
		if (proxy != null) {
			proxy.resetRenderingEngine(re, convert(def));
		}
			
		return proxy;
	}
	
	/**
	 * Shuts downs the rendering service attached to the specified 
	 * pixels set.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The ID of the pixels set.
	 */
	public static void shutDownRenderingControl(Registry context, long pixelsID)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
			singleton.rndSvcProxies.get(new Long(pixelsID));
		if (proxy != null) {
			proxy.shutDown();
			singleton.rndSvcProxies.remove(new Long(pixelsID));
			getCacheSize();
		}
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
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		Iterator i = singleton.rndSvcProxies.keySet().iterator();
		while (i.hasNext())
			((RenderingControlProxy) 
					singleton.rndSvcProxies.get(i.next())).shutDown();

		singleton.rndSvcProxies.clear();
	}

	/**
	 * Returns the {@link RenderingControl} linked to the passed set of pixels,
	 * returns <code>null</code> if no proxy associated.
	 * 
	 * @param context   Reference to the registry. To ensure that agents cannot
	 *                  call the method. It must be a reference to the
	 *                  container's registry.
	 * @param pixelsID  The id of the pixels set.
	 * @return See above.
	 */
	public static RenderingControl getRenderingControl(Registry context,
			Long pixelsID)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		return singleton.rndSvcProxies.get(pixelsID);
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
		int size = getCacheSize();
		boolean cacheInMemory = true;
		if (size <= 0) cacheInMemory = false;
		singleton.pixelsSource = DataSink.makeNew(pixels, registry, 
												cacheInMemory);
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
	 * @return See above.
	 * 
     * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static BufferedImage render(Registry context, Long pixelsID, 
			PlaneDef pDef)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		return proxy.renderPlane(pDef);
	}

	/**
	 * Renders the prejected images.
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
     * @throws RenderingServiceException 	If an error occured while setting 
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

	/** Creates the sole instance. */
	private PixelsServicesFactory()
	{
		rndSvcProxies = new HashMap<Long, RenderingControl>();
	}
	
	/**
	 * Makes a new {@link RenderingControl}.
	 * 
	 * @param re        	The rendering control.
	 * @param pixels   		The pixels set.
	 * @param metadata		The related metadata.
	 * @param compression  	Pass <code>0</code> if no compression otherwise 
	 * 						pass the compression used.
	 * @param def			The rendering def linked to the rendering engine.
	 * 						This is passed to speed up the initialization 
	 * 						sequence.
	 * @return See above.
	 */
	private RenderingControl makeNew(RenderingEnginePrx re, Pixels pixels, 
							List metadata, int compression, RenderingDef def)
	{
		if (singleton == null) throw new NullPointerException();
		Long id = pixels.getId().val;
		RenderingControl rnd = getRenderingControl(registry, id);
		if (rnd != null) return rnd;
		RndProxyDef proxyDef = convert(def);
		rnd = new RenderingControlProxy(registry, re, pixels, metadata, 
										compression, proxyDef, getCacheSize());
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
		if (singleton.pixelsSource != null) n = 1;
		if (n == 0 && m == 0) return maxSize*FACTOR;
		else if (n == 0 && m > 0) {
			sizeCache = (maxSize/(m+1))*FACTOR;
			//reset all the image caches.
			Iterator i = singleton.rndSvcProxies.keySet().iterator();
			while (i.hasNext()) {
				proxy = (RenderingControlProxy)
							singleton.rndSvcProxies.get(i.next());
				proxy.setCacheSize(sizeCache);
			}
			return sizeCache;
		} else if (m == 0 && n > 0) {
			sizeCache = (maxSize/(n+1))*FACTOR;
			return sizeCache;
		}
		sizeCache = (maxSize/(m+n+1))*FACTOR;
		//reset all the image caches.
		Iterator i = singleton.rndSvcProxies.keySet().iterator();
		while (i.hasNext()) {
			proxy = (RenderingControlProxy)
						singleton.rndSvcProxies.get(i.next());
			proxy.setCacheSize(sizeCache);
		}
		
		return sizeCache;
	}

}
