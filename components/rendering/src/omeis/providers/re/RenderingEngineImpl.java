/*
 * omeis.providers.re.RenderingEngineImpl
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

package omeis.providers.re;

// Java imports
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.meta.Experimenter;

// Third-party libraries

// Application-internal dependencies
import omeis.providers.re.codomain.CodomainChain;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.ChannelBindings;
import omeis.providers.re.metadata.PixMetadataException;
import omeis.providers.re.metadata.PixelsChannelData;
import omeis.providers.re.metadata.PixelsStats;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;
import omeis.providers.re.quantum.QuantumStrategy;
import tmp.Helper;
import tmp.RenderingDefConstants;

/**
 * Provides the {@link RenderingEngine} service. This class is an Adapter to
 * wrap the {@link Renderer} so to make it thread-safe.
 * <p>
 * The multi-threaded design of this component is based on dynamic locking and
 * confinement techiniques. All access to the component's internal parts happens
 * through a <code>RenderingEngineImpl</code> object, which is fully
 * synchronized. Internal parts are either never leaked out or given away only
 * if read-only objects. (The only exception are the {@link CodomainMapContext}
 * objects which are not read-only but are copied upon every method invocation
 * so to maintain safety.)
 * </p>
 * <p>
 * Finally the {@link RenderingEngine} component doesn't make use of constructs
 * that could compromise liveness.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.4 $ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OME2.2
 */
public class RenderingEngineImpl implements RenderingEngine
{

    /*
     * LIFECYCLE: 
     * 1. new() 
     * 2. acquireDependencies || setCallback (used to load)
     * 3. loadFromIds || loadFromObjs || ( setX, setY, set Z && load) 
     * 4. call methods 
     * 5. optionally: setX() && load() 
     * 5. release() TODO: when a
     * setXXX() method is called, when do I reload? always? or on dirty?
     * volatile boolean loaded;
     */

    /**
     * Entry point to the unsych parts of the component. As soon as Renderer is
     * not null, the Engine is ready to use.
     */
    private Renderer                       renderer;

    private Pixels                         pixelsObj;

    private ome.model.display.RenderingDef renderingDefObj;

    private Experimenter                   userObj;

    private PixelsService                  pixelsSrv;

    private Object                         serverCallback;

    ReentrantReadWriteLock                 rwl = new ReentrantReadWriteLock();

    /*
     * SETTERS for use in dependency injection. All invalidate the current
     * renderer. Only possible with the WriteLock so no possibility of
     * corrupting data.
     */

    public void setCallback(Object callback)
    {
        rwl.writeLock().lock();
        {
            this.serverCallback = callback;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void setPixelsService(PixelsService service)
    {
        rwl.writeLock().lock();
        {
            this.pixelsSrv = service;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void setPixels(Pixels pixels)
    {
        rwl.writeLock().lock();
        {
            this.pixelsObj = pixels;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void setRenderDefintion(ome.model.display.RenderingDef renderingDef)
    {
        rwl.writeLock().lock();
        {
            this.renderingDefObj = renderingDef;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    /**
     * equivalent to setCallback() and setPixelsService(). For use in
     * ApplicationServer.
     * 
     * @PostConstruct
     */
    public void acquireDependencies()
    {
        rwl.writeLock().lock();
        {
            BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
            BeanFactoryReference bf = bfl.useBeanFactory("ome");

            setCallback(bf.getFactory().getBean("renderingCallback"));
            setPixelsService((PixelsService) bf.getFactory().getBean(
                    "pixelsService"));

            bf.release();
        }
        rwl.writeLock().unlock();
    }

    public void loadFromIds(long pixelsId, long renderingDefId)
    {
        rwl.writeLock().lock();
        {
            setPixels(new Pixels(Long.valueOf(pixelsId))); // TODO use callback
            setRenderDefintion(new ome.model.display.RenderingDef(Long
                    .valueOf(renderingDefId)));
            load();
        }
        rwl.writeLock().unlock();
    }

    public void loadFromObjects(Pixels pixelsObj,
            ome.model.display.RenderingDef renderingDefObj)
    {
        rwl.writeLock().lock();
        {
            setPixels(pixelsObj);
            setRenderDefintion(renderingDefObj);
            load();
        }
        rwl.writeLock().unlock();
    }

    public void load()
    {
        rwl.writeLock().lock();
        {
            /*
             * TODO we could also allow for setting of the buffer! perhaps
             * better caching, etc.
             */
            PixelBuffer buffer = pixelsSrv.getPixelBuffer(pixelsObj);
            StatsFactory sf = new StatsFactory();
            PixelsStats pixelStats = sf.compute(pixelsObj,buffer);
  
            if (renderingDefObj == null) 
            {
                setRenderDefintion(Helper.createDefaultRenderingDef(pixelsObj,pixelStats));
                pixelsObj.getSettings().add(renderingDefObj);   
            }
            
            try {
                renderer = new Renderer(pixelsObj, renderingDefObj, buffer, pixelStats);
            } catch (Exception e){
                throw new RuntimeException("Failed to initialze renderer.",e);
            }
            
        }
        rwl.writeLock().unlock();
    } 
   
    /*
     * METHODS ==================================================
     *  for all of these methods it would be good to have an
     *  TODO interceptor to check for a null renderer value. 
     */

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized RGBBuffer render(PlaneDef pd)
            throws IOException, QuantizationException
    {
        return renderer.render(pd);
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized PixelsDimensions getPixelsDims()
    {
        return renderer.getPixelsDims();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized PixelsStats getPixelsStats()
    {
        return renderer.getPixelsStats();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setModel(int model)
    {
        renderer.setModel(model);
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized int getModel()
    {
        return RenderingDefConstants.convertType(renderingDefObj.getModel());
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized int getDefaultZ()
    {
        return renderer.getRenderingDef().getDefaultZ().intValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized int getDefaultT()
    {
        return renderer.getRenderingDef().getDefaultT().intValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setDefaultZ(int z)
    {
        renderer.setDefaultZ(z);
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setDefaultT(int t)
    {
        renderer.setDefaultT(t);
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setQuantumStrategy(int bitResolution)
    {
        RenderingDef rd = renderer.getRenderingDef();
        QuantumDef qd = rd.getQuantization(), newQd;
        newQd = new QuantumDef();
        newQd.setBitResolution(Integer.valueOf(bitResolution));
        newQd.setCdStart(qd.getCdStart());
        newQd.setCdStop(qd.getCdStop());
        rd.setQuantization(newQd);
        renderer.updateQuantumManager();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setCodomainInterval(int start, int end)
    {
        CodomainChain chain = renderer.getCodomainChain();
        chain.setInterval(start, end);
        RenderingDef rd = renderer.getRenderingDef();
        QuantumDef qd = rd.getQuantization(), newQd;
        newQd = new QuantumDef();
        newQd.setBitResolution(qd.getBitResolution());
        newQd.setCdStart(Integer.valueOf(start));
        newQd.setCdStop(Integer.valueOf(end));
        rd.setQuantization(newQd);
        CodomainMapContext mapCtx;
        Iterator i = rd.getSpatialDomainEnhancement().iterator();
        while (i.hasNext())
        {
            mapCtx = (CodomainMapContext) i.next();
            mapCtx.setCodomain(start, end);
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized QuantumDef getQuantumDef()
    {
        return renderer.getRenderingDef().getQuantization();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setChannelWindow(int w, double start, double end)
    {
        QuantumStrategy qs = renderer.getQuantumManager().getStrategyFor(w);
        qs.setWindow(start, end);
        ChannelBinding[] cb = renderer.getChannelBindings();
        cb[w].setInputStart(new Float(start));
        cb[w].setInputEnd(new Float(end)); // TODO double / Float
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setQuantizationMap(int w, int family,
            double coefficient, boolean noiseReduction)
    {
        QuantumStrategy qs = renderer.getQuantumManager().getStrategyFor(w);
        qs.setQuantizationMap(family, coefficient, noiseReduction);
        ChannelBinding[] cb = renderer.getChannelBindings();
        // FIXME cb[w].setQuantizationMap(family, coefficient, noiseReduction);
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized double[] getChannelStats(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
//        FIXME
//        double[] stats = cb[w].getStats(), copy = new double[stats.length];
//        System.arraycopy(stats, 0, copy, 0, stats.length);
        return null ;// FIXME copy;
        // NOTE: These stats are supposed to be read-only; however we make a
        // copy to be on the safe side.
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized boolean getChannelNoiseReduction(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return cb[w].getNoiseReduction().booleanValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized int getChannelFamily(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return QuantumFactory.convertFamilyType(cb[w].getFamily());
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized double getChannelCurveCoefficient(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return cb[w].getCoefficient().doubleValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized double getChannelWindowStart(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return cb[w].getInputStart().intValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized double getChannelWindowEnd(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return cb[w].getInputEnd().intValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setRGBA(int w, int red, int green, int blue,
            int alpha)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        // TODO cb[w].setRGBA(red, green, blue, alpha);
        Color c = cb[w].getColor();
        c.setRed(Integer.valueOf(red));
        c.setGreen(Integer.valueOf(green));
        c.setBlue(Integer.valueOf(blue));
        c.setAlpha(Integer.valueOf(alpha));
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized int[] getRGBA(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
//        int[] rgba = cb[w].getColor, copy = new int[rgba.length];
//        System.arraycopy(rgba, 0, copy, 0, rgba.length);
//        return copy;
        // NOTE: The rgba is supposed to be read-only; however we make a
        // copy to be on the safe side.
        int[] rgba = new int[4];
        // TODO
        rgba[0] = cb[w].getColor().getRed().intValue();
        rgba[1] = cb[w].getColor().getGreen().intValue();
        rgba[2] = cb[w].getColor().getBlue().intValue();
        rgba[3] = cb[w].getColor().getAlpha().intValue();
        return rgba;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void setActive(int w, boolean active)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        cb[w].setActive(Boolean.valueOf(active));
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized boolean isActive(int w)
    {
        ChannelBinding[] cb = renderer.getChannelBindings();
        return cb[w].getActive().booleanValue();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void addCodomainMap(CodomainMapContext mapCtx)
    {
        renderer.getCodomainChain().add(mapCtx.copy());
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void updateCodomainMap(CodomainMapContext mapCtx)
    {
        renderer.getCodomainChain().update(mapCtx.copy());
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void removeCodomainMap(CodomainMapContext mapCtx)
    {
        renderer.getCodomainChain().remove(mapCtx.copy());
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void saveCurrentSettings() throws PixMetadataException
    {
        // FIXME use callback object to save.
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public synchronized void resetDefaults()
    {
        // Reset the bit resolution.
        setQuantumStrategy(QuantumFactory.DEPTH_8BIT); // NB: Java locks are
        setCodomainInterval(0, QuantumFactory.DEPTH_8BIT); // re-entrant.

        // Set the each channel's window to the channel's [min, max].
        // Make active only the first channel.
        ChannelBinding[] cb = renderer.getChannelBindings();
        PixelsStats stats = renderer.getPixelsStats();
        Map map = Helper.getPixelsChannelData(pixelsObj);
        PixelsChannelData pixData;
        boolean active = false;
        int model = RenderingDefConstants.GS;
        int[] c;
        for (int w = 0; w < cb.length; w++)
        {
            pixData = (PixelsChannelData) map
                    .get(cb[w].getIndex());
            if (pixData != null
                    && pixData.getColorDomain() == Renderer.RGB_COLOR_DOMAIN)
            {
                active = true;
                model = RenderingDefConstants.RGB;
            }
            cb[w].setActive(Boolean.valueOf(active));
            double start = stats.getGlobalEntry(w).globalMin, end = stats
                    .getGlobalEntry(w).globalMax;
            setChannelWindow(w, start, end);
            if (pixData == null) c = ColorsFactory.getColor(cb[w].getIndex().intValue(),
                    -1);
            else
                c = ColorsFactory.getColor(cb[w].getIndex().intValue(), pixData
                        .getEmWavelenght());
            setRGBA(w, c[ColorsFactory.RED], c[ColorsFactory.GREEN],
                    c[ColorsFactory.BLUE], c[ColorsFactory.ALPHA]);
        }
        cb[0].setActive(Boolean.valueOf(active));
        // Remove all the codomainMapCtx except the identity.
        renderer.getCodomainChain().remove();

        // Fall back to the default strategy.
        setModel(model);
    }

}
