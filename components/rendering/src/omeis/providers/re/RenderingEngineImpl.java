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
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

// Application-internal dependencies
import ome.api.IPixels;
import ome.api.IQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.system.OmeroContext;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.Renderer;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

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

    private static Log log = LogFactory.getLog(RenderingEngineImpl.class);
    
    /*
     * LIFECYCLE: 
     * 1. new() || new(Services[]) 
     * 2. (lookupX || useX)+ && load  
     * 3. call methods 
     * 4. optionally: return to 2.
     * 5. destroy() 
     *
     * TODO: when a setXXX() method is called, when do I reload? always?
     * or ondirty?
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

    public void create() 
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

        try {
            pixelsObj = null;
            rendDefObj = null;
            renderer = null;
        } finally {
            rwl.writeLock().unlock();
        }
    }
    
    
    /*
     * SETTERS for use in dependency injection. All invalidate the current
     * renderer. Only possible with the WriteLock so no possibility of
     * corrupting data.
     * 
     * a.k.a. STATE-MANAGEMENT
     */
    public void lookupPixels(long pixelsId)
    {
        rwl.writeLock().lock();

        try {

            this.pixelsObj = pixMetaSrv.retrievePixDescription(pixelsId); 
            this.renderer = null;

            if ( pixelsObj == null )
                throw new ValidationException(
                        "Pixels object with id "+pixelsId+" not found.");
            
        } finally {
            rwl.writeLock().unlock();
        }
        
        if (log.isDebugEnabled())
            log.debug("lookupPixels for id "+pixelsId+" succeeded: "+this.pixelsObj);
        
    }
    
    public void usePixels(Pixels pixels)
    {
        rwl.writeLock().lock();

        try {
            this.pixelsObj = pixels;
            this.renderer = null;
            
            if ( pixelsObj == null )
                throw new ValidationException("Null pixels not allowed.");
            
        } finally {
            rwl.writeLock().unlock();
        }
        
        if (log.isDebugEnabled())
            log.debug("Using pixels: "+this.pixelsObj);

    }


    public void lookupRenderingDef(long pixelsId)
    {
        rwl.writeLock().lock();
        
        try {
            this.rendDefObj = pixMetaSrv.retrieveRndSettings(pixelsId);
            this.renderer = null;
            
            if ( rendDefObj == null )
                throw new ValidationException(
                        "RenderingDef with id "+pixelsId+" not found.");
        } finally {
            rwl.writeLock().unlock();
        }
        
        if (log.isDebugEnabled())
            log.debug("lookupRenderingDef for id "+pixelsId+" succeeded: "+this.rendDefObj);
        
    }

    public void load()
    {
        rwl.writeLock().lock();

        try {

            errorIfNullPixels();

            /*
             * TODO we could also allow for setting of the buffer! perhaps
             * better caching, etc.
             */
            PixelBuffer buffer = pixDataSrv.getPixelBuffer(pixelsObj);
            
            renderer = new Renderer(pixMetaSrv, pixelsObj, rendDefObj, buffer);
        } finally {
            rwl.writeLock().unlock();
        }
    } 

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void saveCurrentSettings()
    {
        rwl.writeLock().lock();

        try {
            errorIfNullRenderingDef();
            pixMetaSrv.saveRndSettings(rendDefObj);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    
    /*
     * DELEGATING METHODS ==================================================
     *  
     *  All following methods follow the pattern:
     *   1) get read or write lock
     *      try {
     *   2)   error on null 
     *   3)   delegate method to renderer/renderingObj/pixelsObj
     *      } finally {
     *   4)   release lock
     *      }
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

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public RGBBuffer render(PlaneDef pd)
            throws ResourceError, ValidationException
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            return renderer.render(pd);
        } catch (IOException e) {
            ResourceError re = new ResourceError(
                    "IO error while rendering:\n"+e.getMessage());
            re.initCause(e);
            throw re;
        } catch (QuantizationException e) {
            InternalException ie = new InternalException(
                    "QuantizationException while rendering:\n"+e.getMessage());
            ie.initCause(e);
            throw ie;
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setModel(RenderingModel model)
    {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setModel(model);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setDefaultZ(int z)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setDefaultZ(z);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setDefaultT(int t)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setDefaultT(t);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setQuantumStrategy(int bitResolution)
    {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setQuantumStrategy(bitResolution);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setCodomainInterval(int start, int end)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setCodomainInterval(start, end);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setChannelWindow(int w, double start, double end)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setChannelWindow(w, start, end);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setQuantizationMap(int w, Family family,
            double coefficient, boolean noiseReduction)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setQuantizationMap(w, family, coefficient, noiseReduction);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double[] getChannelStats(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
//        FIXME
//        double[] stats = cb[w].getStats(), copy = new double[stats.length];
//        System.arraycopy(stats, 0, copy, 0, stats.length);
            return null;
        } finally {
            rwl.readLock().unlock();
        }// FIXME copy;
        // NOTE: These stats are supposed to be read-only; however we make a
        // copy to be on the safe side.
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public boolean getChannelNoiseReduction(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getNoiseReduction().booleanValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public Family getChannelFamily(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getFamily();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelCurveCoefficient(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getCoefficient().doubleValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelWindowStart(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getInputStart().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public double getChannelWindowEnd(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getInputEnd().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setRGBA(int w, int red, int green, int blue, int alpha)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.setRGBA(w, red, green, blue, alpha);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int[] getRGBA(int w)
    {

        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            int[] rgba = new int[4];
            ChannelBinding[] cb = renderer.getChannelBindings();
            // NOTE: The rgba is supposed to be read-only; however we make a
            // copy to be on the safe side.
            rgba[0] = cb[w].getColor().getRed().intValue();
            rgba[1] = cb[w].getColor().getGreen().intValue();
            rgba[2] = cb[w].getColor().getBlue().intValue();
            rgba[3] = cb[w].getColor().getAlpha().intValue();
            return rgba;
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void setActive(int w, boolean active)
    {
        try {
            rwl.writeLock().lock();
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            cb[w].setActive(Boolean.valueOf(active));
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public boolean isActive(int w)
    {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getActive().booleanValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void addCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.getCodomainChain().add(mapCtx.copy());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void updateCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.getCodomainChain().update(mapCtx.copy());
        } finally { 
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void removeCodomainMap(CodomainMapContext mapCtx)
    {
        rwl.writeLock().lock();
        try {
            errorIfInvalidState();
            renderer.getCodomainChain().remove(mapCtx.copy());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public void resetDefaults()
    {
        rwl.writeLock().lock();
        
        try {
            errorIfInvalidState();
            renderer.resetDefaults();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    //  ~ RendDefObj Delegation
    // =========================================================================
    
    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public RenderingModel getModel()
    {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getModel();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getDefaultZ()
    {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getDefaultZ().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public int getDefaultT()
    {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getDefaultT().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    
    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public QuantumDef getQuantumDef()
    {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getQuantization();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public Pixels getPixels()
    {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return pixelsObj;
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public List getAvailableModels()
    {
        rwl.readLock().lock();

        try {
        	return pixMetaSrv.getAllEnumerations(RenderingModel.class);
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    /** Implemented as specified by the {@link RenderingEngine} interface. */
    public List getAvailableFamilies()
    {
        rwl.readLock().lock();

        try {
        	return pixMetaSrv.getAllEnumerations(Family.class);
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    // ~ Error checking methods
    // =========================================================================
    
    protected final static String NULL_RENDERER = 
        "RenderingEngine not ready: renderer is null."+ 
        "This method can only be called " +
        "after the renderer is properly " +
        "initialized (not-null). \n" +
        "Try lookup and/or use methods.";
 
    // TODO ObjectUnreadyException
    protected void errorIfInvalidState()
    {
        errorIfNullPixels();
        errorIfNullRenderingDef();
        errorIfNullRenderer();
    }
        
    protected void errorIfNullPixels()
    {
        if (pixelsObj == null)
            throw new ApiUsageException(
                    "RenderingEngine not ready: Pixels object not set.");
    }
    
    protected void errorIfNullRenderingDef()
    {
        if (rendDefObj == null)
            throw new ApiUsageException(
                    "RenderingEngine not ready: RenderingDef object not set.");

    }
    
    protected void errorIfNullRenderer()
    {
        if ( renderer == null )
            throw new ApiUsageException(NULL_RENDERER);

    }

}
