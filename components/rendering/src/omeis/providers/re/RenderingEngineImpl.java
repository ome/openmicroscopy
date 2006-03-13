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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

// Third-party libraries

// Application-internal dependencies
import ome.api.IPixels;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.system.OmeroContext;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.Renderer;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.PixelsStats;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;
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
     * 1. new() || new(Services[]) 
     * 2. (lookupX || useX)+ && load  
     * 3. call methods 
     * 4. optionally: return to 2.
     * 5. destroy() 
     *
     * TODO: when a
     * setXXX() method is called, when do I reload? always? or on dirty?
     * volatile boolean loaded;
     */

    /**
     * Entry point to the unsych parts of the component. As soon as Renderer is
     * not null, the Engine is ready to use.
     */
    private OmeroContext                   ctx;
    private Renderer                       renderer;
    private Pixels                         pixelsObj;
    private RenderingDef                   rendDefObj;
    private PixelsService                  pixDataSrv;
    private IPixels                        pixMetaSrv;
    private ReentrantReadWriteLock         rwl = new ReentrantReadWriteLock();


    // ~ CREATION/CONFIGURATION/DESTRUCTION
    // =========================================================================
    public  RenderingEngineImpl()
    { }

    public void setPixelsMetadata(IPixels metaService)
    {
        pixMetaSrv = metaService;
    }
    
    public void setPixelsData(PixelsService dataService)
    {
        pixDataSrv = dataService;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) 
    throws BeansException
    {
        this.ctx = (OmeroContext) applicationContext;
    }
    
    public void selfConfigure()
    {
        this.ctx = OmeroContext.getInternalServerContext();
        this.ctx.applyBeanPropertyValues(this,RenderingEngine.class.getName());
    }
    
    public void destroy()
    {
        rwl.writeLock().lock();
        {
            renderer = null;
        }
        rwl.writeLock().unlock();
    }
    
    
    /*
     * SETTERS for use in dependency injection. All invalidate the current
     * renderer. Only possible with the WriteLock so no possibility of
     * corrupting data.
     * 
     * a.k.a. STATE-MANAGEMENT
     */
    public void usePixels(Pixels pixels)
    {
        rwl.writeLock().lock();
        {
            this.pixelsObj = pixels;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void useRenderDefintion(ome.model.display.RenderingDef renderingDef)
    {
        rwl.writeLock().lock();
        {
            this.rendDefObj = renderingDef;
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void lookupPixels(long pixelsId)
    {
        rwl.writeLock().lock();
        {
            this.pixelsObj 
                = pixMetaSrv.retrievePixDescription(pixelsId);
            this.renderer = null;
        }
        rwl.writeLock().unlock();
    }

    public void lookupRenderingDef(long renderingDefId)
    {
        rwl.writeLock().lock();
        {
            this.rendDefObj 
                = pixMetaSrv.retrieveRndSettings(renderingDefId);
            this.renderer = null;
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
            PixelBuffer buffer = pixDataSrv.getPixelBuffer(pixelsObj);
            StatsFactory sf = new StatsFactory();
            PixelsStats pixelStats = sf.compute(pixelsObj,buffer);

            if (pixelsObj == null)
            {
                throw new IllegalStateException("Pixels object not set.");
            }
            
            // FIXME: This should be stripped out
            /*
            if (rendDefObj == null) 
            {
                this.rendDefObj = 
                        Helper.createDefaultRenderingDef(pixelsObj,pixelStats);
                pixelsObj.getSettings().add(rendDefObj);   
            }
            */
            
            try {
                renderer 
                = new Renderer(pixelsObj, rendDefObj, buffer, pixelStats);
            } catch (Exception e){
                throw new RuntimeException("Failed to initialze renderer.",e);
            }
            
        }
        rwl.writeLock().unlock();
    } 

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void saveCurrentSettings()
    {
        rwl.writeLock().lock();
        {
            // TODO vararg save method.
            pixMetaSrv.saveRndSettings(rendDefObj);
        }
        rwl.writeLock().unlock();
    }

    
    /*
     * DELEGATING METHODS ==================================================
     *  
     *  All following methods follow the pattern:
     *   1) get read or write lock
     *      {
     *   2)   error on null renderer
     *   3)   delegate method to renderer
     *      }
     *   4) release lock
     *  
     *  TODO for all of these methods it would be good to have an
     *  interceptor to check for a null renderer value. 
     *      would have to be JBoss specific 
     *      or apply at the Renderer level
     *      or this would have to be a delegate to REngine to Renderer!
     *      
     *  @Write || @Read on each. 
     *  for example 
     *  see http://www.onjava.com/pub/a/onjava/2004/08/25/aoa.html?page=3
     *  for asynch. annotations (also @TxSynchronized)
     *   
     */

    public final static String NULL_RENDERER = 
        "Renderer is null."+ 
        "This method can only be called " +
        "after the renderer is properly " +
        "initialized (not-null).";
    
    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public RGBBuffer render(PlaneDef pd)
            throws IOException, QuantizationException
    {
        RGBBuffer result = null;
        rwl.readLock().lock(); 
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = renderer.render(pd);
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public PixelsDimensions getPixelsDims()
    {
        PixelsDimensions result = null;
        rwl.readLock().lock(); 
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = renderer.getPixelsDims();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public PixelsStats getPixelsStats()
    {
        PixelsStats result = null;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = renderer.getPixelsStats();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setModel(int model)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setModel(model);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getModel()
    {
        int result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            // TODO doesn't need render !!! other cases?
            result = 
                RenderingDefConstants.convertType(rendDefObj.getModel());
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getDefaultZ()
    {
        int result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = 
                renderer.getRenderingDef().getDefaultZ().intValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getDefaultT()
    {
        int result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = 
                renderer.getRenderingDef().getDefaultT().intValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setDefaultZ(int z)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setDefaultZ(z);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setDefaultT(int t)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setDefaultT(t);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setQuantumStrategy(int bitResolution)
    {
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setQuantumStrategy(bitResolution);
        }
        rwl.readLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setCodomainInterval(int start, int end)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);

            renderer.setCodomainInterval(start, end);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public QuantumDef getQuantumDef()
    {
        QuantumDef result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            result = renderer.getRenderingDef().getQuantization();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setChannelWindow(int w, double start, double end)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setChannelWindow(w, start, end);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setQuantizationMap(int w, int family,
            double coefficient, boolean noiseReduction)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.setQuantizationMap(w, family, coefficient, noiseReduction);
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double[] getChannelStats(int w)
    {
        double[] result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
//        FIXME
//        double[] stats = cb[w].getStats(), copy = new double[stats.length];
//        System.arraycopy(stats, 0, copy, 0, stats.length);
        }
        rwl.readLock().unlock();
            return null ;// FIXME copy;
        // NOTE: These stats are supposed to be read-only; however we make a
        // copy to be on the safe side.
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public boolean getChannelNoiseReduction(int w)
    {
        boolean result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = cb[w].getNoiseReduction().booleanValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getChannelFamily(int w)
    {
        int result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = QuantumFactory.convertFamilyType(cb[w].getFamily());
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelCurveCoefficient(int w)
    {
        double result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = cb[w].getCoefficient().doubleValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelWindowStart(int w)
    {
        double result;
        rwl.readLock().unlock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = cb[w].getInputStart().intValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelWindowEnd(int w)
    {
        double result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = cb[w].getInputEnd().intValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setRGBA(int w, int red, int green, int blue, int alpha)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);

            renderer.setRGBA(w, red, green, blue, alpha);

        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int[] getRGBA(int w)
    {

        int[] rgba = new int[4];
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
    //        int[] rgba = cb[w].getColor, copy = new int[rgba.length];
    //        System.arraycopy(rgba, 0, copy, 0, rgba.length);
    //        return copy;
            // NOTE: The rgba is supposed to be read-only; however we make a
            // copy to be on the safe side.
            // TODO
            rgba[0] = cb[w].getColor().getRed().intValue();
            rgba[1] = cb[w].getColor().getGreen().intValue();
            rgba[2] = cb[w].getColor().getBlue().intValue();
            rgba[3] = cb[w].getColor().getAlpha().intValue();
        }
        rwl.readLock().unlock();
        return rgba;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setActive(int w, boolean active)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            cb[w].setActive(Boolean.valueOf(active));
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public boolean isActive(int w)
    {
        boolean result;
        rwl.readLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            ChannelBinding[] cb = renderer.getChannelBindings();
            result = cb[w].getActive().booleanValue();
        }
        rwl.readLock().unlock();
        return result;
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void addCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.getCodomainChain().add(mapCtx.copy());
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void updateCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.getCodomainChain().update(mapCtx.copy());
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void removeCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
            
            renderer.getCodomainChain().remove(mapCtx.copy());
        }
        rwl.writeLock().unlock();
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void resetDefaults()
    {
        rwl.writeLock().lock();
        {
            if (renderer == null)
                throw new IllegalStateException(NULL_RENDERER);
 
            renderer.resetDefaults();
 
        }
        rwl.writeLock().unlock();
    }

}
