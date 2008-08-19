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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;


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

	/** The sole instance. */
	private static PixelsServicesFactory 	singleton;

	/** Reference to the container registry. */
	private static Registry                 registry;

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
		proxy.setDefaultZ(rndDef.getDefaultZ());
		proxy.setDefaultT(rndDef.getDefaultT());
		proxy.setColorModel(rndDef.getModel().getValue());
		
		QuantumDef def = rndDef.getQuantization();
		proxy.setCodomain(def.getCdStart(), def.getCdEnd());
		proxy.setBitResolution(def.getBitResolution());
		
		ChannelBinding c;
		Collection bindings = rndDef.unmodifiableWaveRendering();
		
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
				rgba[0] = color.getRed();
				rgba[1] = color.getGreen();
				rgba[2] = color.getBlue();
				rgba[3] = color.getAlpha();
				
				
				cb.setActive(c.getActive());
				cb.setInterval(c.getInputStart(), c.getInputEnd());
				cb.setRGBA(rgba);
				cb.setQuantization(c.getFamily().getValue(), 
						c.getCoefficient().doubleValue(), 
						c.getNoiseReduction().booleanValue());
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
			RenderingEngine re, Pixels pixels, List metadata, int compression, 
			RenderingDef def)
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
	 */
	public static RenderingControlProxy reloadRenderingControl(Registry context, 
			long pixelsID, RenderingEngine re)
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
	 */
	public static RenderingControlProxy resetRenderingControl(Registry context, 
			long pixelsID, RenderingEngine re, RenderingDef def)
	{
		if (!(registry.equals(context)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = (RenderingControlProxy) 
		singleton.rndSvcProxies.get(new Long(pixelsID));
		if (proxy != null) {
			proxy.shutDown();
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
		return (RenderingControl) singleton.rndSvcProxies.get(pixelsID);
	}

	/**
	 * Creates a new data sink for the specified set of pixels.
	 * 
	 * @param pixels The pixels set the data sink is for.
	 * @return See above.
	 */
	public static DataSink createDataSink(Pixels pixels)
	{
		if (pixels == null)
			throw new IllegalArgumentException("Pixels cannot be null.");
		if (singleton.pixelsSource != null && 
				singleton.pixelsSource.isSame(pixels.getId().longValue()))
			return singleton.pixelsSource;
		singleton.pixelsSource = DataSink.makeNew(pixels, registry);
		return singleton.pixelsSource;
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
	 */
	public static BufferedImage render(Registry context, Long pixelsID, 
			PlaneDef pDef)
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		try {
			return proxy.renderPlane(pDef);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
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
     * @return See above.
     * @throws RenderingServiceException 	If an error occured while setting 
     * 										the value.
     * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	public static BufferedImage renderProjected(Registry context, Long pixelsID, 
			int startZ, int endZ, int type, int stepping)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (!(context.equals(registry)))
			throw new IllegalArgumentException("Not allow to access method.");
		RenderingControlProxy proxy = 
			(RenderingControlProxy) singleton.rndSvcProxies.get(pixelsID);
		if (proxy == null) 
			throw new RuntimeException("No rendering service " +
			"initialized for the specified pixels set.");
		return proxy.renderProjected(startZ, endZ, stepping, type);
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
	private HashMap                 rndSvcProxies;

	/** Access to the raw data. */
	private DataSink				pixelsSource;

	/** Creates the sole instance. */
	private PixelsServicesFactory()
	{
		rndSvcProxies = new HashMap();
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
	private RenderingControl makeNew(RenderingEngine re, Pixels pixels, 
							List metadata, int compression, RenderingDef def)
	{
		if (singleton == null) throw new NullPointerException();
		Long id = pixels.getId();//re.getPixels().getId();
		RenderingControl rnd = getRenderingControl(registry, id);
		if (rnd != null) return rnd;
		int l = singleton.rndSvcProxies.size();
		RndProxyDef proxyDef = convert(def);
		rnd = new RenderingControlProxy(registry, re, pixels, metadata, 
										compression, proxyDef);
		//reset the size of the caches.
		Iterator i = singleton.rndSvcProxies.keySet().iterator();
		RenderingControlProxy proxy;
		while (i.hasNext()) {
			proxy = (RenderingControlProxy) singleton.rndSvcProxies.get(
					i.next());
			proxy.resetCacheSize(l);
		}
		singleton.rndSvcProxies.put(id, rnd);
		return rnd;
	}

}
