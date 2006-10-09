/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlFactory
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

package org.openmicroscopy.shoola.env.rnd;


//Java imports
import java.util.HashMap;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import ome.model.core.PixelsDimensions;
import omeis.providers.re.RenderingEngine;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;


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
public class RenderingServicesFactory
{

    /** The default size in MB of the general cache. */
    private static final int                DEFAULT_CACHE_SIZE = 40;
    
    /** The sole instance. */
    private static RenderingServicesFactory singleton;
    
    /** Reference to the container registry. */
    private static Registry                 registry;
    
    /** The per image cache size. */
    private static int                      cacheSize;
    
    /**
     * Creates a new instance.  This can't be called outside of container b/c 
     * agents have no refs to the singleton container. So we can be sure this
     * method is going to create services just once.
     * 
     * @param c Reference to the container. 
     * @return The sole instance.
     * @throws NullPointerException If the reference to the {@link Container}
     *                              is <code>null</code>.
     */
    public static RenderingServicesFactory getInstance(Container c)
    {
        if (c == null)
            throw new NullPointerException();  //An agent called this method?
        if (singleton == null)  {
            registry = c.getRegistry();
            singleton = new RenderingServicesFactory();
        }
        return singleton;
    }

    /**
     * Creates a new {@link RenderingControl}. We pass a reference to the 
     * the registry to ensure that agents don't call the method.
     * 
     * @param context   Reference to the registry. To ensure that agents cannot
     *                  call the method. It must be a reference to the
     *                  container's registry.
     * @param re        The {@link RenderingEngine rendering service}.        
     * @param pixDims   The dimension of the pixels set.
     * @return See above.
     * @throws IllegalArgumentException If an Agent try to access the method.
     */
    public static RenderingControl createRenderingControl(Registry context, 
                                RenderingEngine re, PixelsDimensions pixDims)
    {
        if (!(context.equals(registry)))
            throw new IllegalArgumentException("Not allow to access method.");
        return singleton.makeNew(re, pixDims);
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
        RenderingControl proxy = (RenderingControl) 
            singleton.rndSvcProxies.get(new Long(pixelsID));
        if (proxy != null)
            singleton.rndSvcProxies.remove(new Long(pixelsID));
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
          ((RenderingControl) singleton.rndSvcProxies.get(i.next())).shutDown();

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
    
    /** Keep track of all the rendering service already initialized. */
    private HashMap                 rndSvcProxies;
    
    /** Creates the sole instance. */
    private RenderingServicesFactory()
    {
        cacheSize = DEFAULT_CACHE_SIZE;
        rndSvcProxies = new HashMap();
        Integer size = (Integer) registry.lookup(LookupNames.RE_CACHE_SZ);
        if (size != null) cacheSize = size.intValue();
    }
 
    /**
     * Makes a new {@link RenderingControl}.
     * 
     * @param re        The rendering control.
     * @param pixDims   The dimensions of the pixels array.
     * @return See above.
     */
    private RenderingControl makeNew(RenderingEngine re,
                                    PixelsDimensions pixDims)
    {
        if (singleton == null) throw new NullPointerException();
        Long id = re.getPixels().getId();
        RenderingControl rnd = getRenderingControl(registry, id);
        if (rnd != null) return rnd;
        int l = singleton.rndSvcProxies.size();
        int size = cacheSize;
        if (l != 0) size = cacheSize/l;
        rnd = new RenderingControlProxy(re, pixDims, size);
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
