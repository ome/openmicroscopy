/*
 * ome.services.RenderingBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

// Java imports
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IPixels;
import ome.api.ServiceInterface;
import ome.api.local.LocalCompress;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.logic.AbstractLevel2Service;
import ome.model.IObject;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import ome.util.ImageUtil;
import ome.util.ShallowCopy;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.Renderer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.cache.NoPassivationCache;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides the {@link RenderingEngine} service. This class is an Adapter to
 * wrap the {@link Renderer} so to make it thread-safe.
 * <p>
 * The multi-threaded design of this component is based on dynamic locking and
 * confinement techniques. All access to the component's internal parts happens
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
 * @author Andrea Falconi, a.falconi at dundee.ac.uk
 * @author Chris Allan, callan at blackcat.ca
 * @author Jean-Marie Burel, j.burel at dundee.ac.uk
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see RenderingEngine
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Stateful
@Remote(RenderingEngine.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/omeis.providers.re.RenderingEngine"),
    @RemoteBinding(jndiBinding = "omero/secure/omeis.providers.re.RenderingEngine",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(RenderingEngine.class)
@LocalBinding(jndiBinding = "omero/local/omeis.providers.re.RenderingEngine")
@SecurityDomain("OmeroSecurity")
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Interceptors( { OmeroAroundInvoke.class })
// TODO previously not here. examine the difference.
public class RenderingBean extends AbstractLevel2Service implements
        RenderingEngine, Serializable {

    private static final long serialVersionUID = -4383698215540637039L;

    private static final Log log = LogFactory.getLog(RenderingBean.class);

    public Class<? extends ServiceInterface> getServiceInterface() {
        return RenderingEngine.class;
    }

    // ~ State
    // =========================================================================

    /*
     * LIFECYCLE: 1. new() || new(Services[]) 2. (lookupX || useX)+ && load 3.
     * call methods 4. optionally: return to 2. 5. destroy()
     * 
     * TODO: when a setXXX() method is called, when do I reload? always? or
     * ondirty?
     */

    /**
     * Transforms the raw data. Entry point to the unsych parts of the
     * component. As soon as Renderer is not null, the Engine is ready to use.
     */
    private transient Renderer renderer;

    /** The pixels set to the rendering engine is for. */
    private Pixels pixelsObj;

    /** The rendering settings associated to the pixels set. */
    private RenderingDef rendDefObj;

    /** Reference to the service used to retrieve the pixels data. */
    private transient PixelsService pixDataSrv;

    /** Reference to the service used to retrieve the pixels metadata. */
    private transient IPixels pixMetaSrv;

    /** Reference to the service used to compress pixel data. */
    private transient LocalCompress compressionSrv;

    /**
     * read-write lock to prevent READ-calls during WRITE operations. Unneeded
     * for remote invocations (EJB synchronizes).
     */
    private transient final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /** Notification that the bean has just returned from passivation. */
    private transient boolean wasPassivated = false;

    /**
     * Compression service Bean injector.
     *
     * @param compressionService
     *            an <code>ICompress</code>.
     */
    public void setCompressionService(LocalCompress compressionSrv) {
        getBeanHelper().throwIfAlreadySet(this.compressionSrv, compressionSrv);
        this.compressionSrv = compressionSrv;
    }

    /** set injector. For use during configuration. Can only be called once. */
    public void setPixelsMetadata(IPixels metaService) {
        getBeanHelper().throwIfAlreadySet(this.pixMetaSrv, metaService);
        pixMetaSrv = metaService;
    }

    /** set injector. For use during configuration. Can only be called once. */
    public void setPixelsData(PixelsService dataService) {
        getBeanHelper().throwIfAlreadySet(this.pixDataSrv, dataService);
        pixDataSrv = dataService;
    }

    // ~ Lifecycle methods
    // =========================================================================

    /**
     * lifecycle method -- {@link PostActivate} and {@link PostConstruct}.
     * Delegates to super class
     */
    @PostConstruct
    public void create() {
        selfConfigure();
    }

    /** lifecycle method -- {@link PostPassivate}. */
    @PostActivate
    public void postPassivate() {
    	log.info("***** Returning from passivation... ******");
    	create();
    	wasPassivated = true;
    	rwl = new ReentrantReadWriteLock();
    }

    /** lifecycle method -- {@link PrePassivate}. */
    @PrePassivate
    public void passivate() {
    	log.info("***** Passivating... ******");
    	closeRenderer();
    	renderer = null;
    }

    /**
     * lifecycle method -- {@link PreDestroy}. Nulls {@link Renderer} instance
     * and delegates to super class with
     * {@link ReentrantReadWriteLock write lock}
     */
    @PreDestroy
    public void destroy() {
        rwl.writeLock().lock();

        try {
            // Mark us unready. All other state is marked transient.
            closeRenderer();
            renderer = null;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    @RolesAllowed("user")
    @Remove
    public void close() {
        // don't need to do anything.
    }

    /*
     * SETTERS for use in dependency injection. All invalidate the current
     * renderer. Only possible with the WriteLock so no possibility of
     * corrupting data.
     * 
     * a.k.a. STATE-MANAGEMENT
     */

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#lookupPixels(long)
     */
    @RolesAllowed("user")
    public void lookupPixels(long pixelsId) {
    	rwl.writeLock().lock();

    	try {
    		pixelsObj = pixMetaSrv.retrievePixDescription(pixelsId);
    		closeRenderer();
    		renderer = null;

    		if (pixelsObj == null) {
    			throw new ValidationException("Pixels object with id "
    					+ pixelsId + " not found.");
    		}
    	} finally {
    		rwl.writeLock().unlock();
    	}

    	if (log.isDebugEnabled()) {
    		log.debug("lookupPixels for id " + pixelsId + " succeeded: "
    				+ this.pixelsObj);
    	}
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#lookupRenderingDef(long)
     */
    @RolesAllowed("user")
    public boolean lookupRenderingDef(long pixelsId)
    {
    	rwl.writeLock().lock();

    	try
    	{
    		rendDefObj = pixMetaSrv.retrieveRndSettings(pixelsId);
    		closeRenderer();
    		renderer = null;

    		if (rendDefObj == null)
    		{
    			// We've been initialized on a pixels set that has no rendering
    			// definition for the given user. In order to maintain the
    			// proper state and ensure that we avoid transactional problems
    			// we're going to notify the caller instead of performing *any*
    			// magic that would require a database update.
    			// *** Ticket #564 -- Chris Allan <callan@blackcat.ca> ***
    			return false;
    		}
    		
    		// Ensure that the pixels object is unloaded to avoid transactional
    		// headaches later due to Hibernate object caching techniques. If
    		// this is not performed, rendDefObj.pixels will be the same
    		// instance as pixelsObj; which if passed to IUpdate will be
    		// set unloaded by the service. We *really* don't want this.
    		// *** Ticket #848 -- Chris Allan <callan@blackcat.ca> ***
    		Pixels unloadedPixels = new Pixels(pixelsId);
    		unloadedPixels.unload();
    		rendDefObj.setPixels(unloadedPixels);
    		
    		// Ensure that we do not have "dirty" pixels or rendering settings
    		// left around in the Hibernate session cache.
    		iQuery.clear();
    	}
    	finally
    	{
    		rwl.writeLock().unlock();
    	}

    	if (log.isDebugEnabled())
    	{
    		log.debug("lookupRenderingDef for Pixels=" + pixelsId
    				+ " succeeded: " + this.rendDefObj);
    	}
    	return true;
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#load()
     */
    @RolesAllowed("user")
    public void load() {
        rwl.writeLock().lock();
        
        try {
            errorIfNullPixels();
            /*
             * TODO we could also allow for setting of the buffer! perhaps
             * better caching, etc.
             */
            PixelBuffer buffer = pixDataSrv.getPixelBuffer(pixelsObj);
            closeRenderer();
            List<Family> families = pixMetaSrv.getAllEnumerations(Family.class);
            List<RenderingModel> renderingModels = pixMetaSrv
                    .getAllEnumerations(RenderingModel.class);
            QuantumFactory quantumFactory = new QuantumFactory(families);
            renderer = new Renderer(quantumFactory, renderingModels, pixelsObj,
                    rendDefObj, buffer);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface. TODO
     * 
     * @see RenderingEngine#getCurrentEventContext()
     */
    @RolesAllowed("user")
    public EventContext getCurrentEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext());
    }

    /*
     * DELEGATING METHODS ==================================================
     * 
     * All following methods follow the pattern: 1) get read or write lock try {
     * 2) error on null 3) delegate method to renderer/renderingObj/pixelsObj }
     * finally { 4) release lock }
     * 
     * TODO for all of these methods it would be good to have an interceptor to
     * check for a null renderer value. would have to be JBoss specific or apply
     * at the Renderer level or this would have to be a delegate to REngine to
     * Renderer!
     * 
     * @Write || @Read on each. for example see
     * http://www.onjava.com/pub/a/onjava/2004/08/25/aoa.html?page=3 for asynch.
     * annotations (also @TxSynchronized)
     * 
     */

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#render(PlaneDef)
     */
    @RolesAllowed("user")
    public RGBBuffer render(PlaneDef pd) throws ResourceError,
            ValidationException {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            return renderer.render(pd);
        } catch (IOException e) {
            ResourceError re = new ResourceError("IO error while rendering:\n"
                    + e.getMessage());
            re.initCause(e);
            throw re;
        } catch (QuantizationException e) {
            InternalException ie = new InternalException(
                    "QuantizationException while rendering:\n" + e.getMessage());
            ie.initCause(e);
            throw ie;
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#render(PlaneDef)
     */
    @RolesAllowed("user")
    public int[] renderAsPackedInt(PlaneDef pd)
    	throws ResourceError, ValidationException {
    	rwl.writeLock().lock();

    	try {
    		errorIfInvalidState();
    		return renderer.renderAsPackedInt(pd);
    	} catch (IOException e) {
    		ResourceError re = new ResourceError("IO error while rendering:\n"
    				+ e.getMessage());
    		re.initCause(e);
    		throw re;
    	} catch (QuantizationException e) {
    		InternalException ie = new InternalException(
    				"QuantizationException while rendering:\n" + e.getMessage());
    		ie.initCause(e);
    		throw ie;
    	} finally {
    		rwl.writeLock().unlock();
    	}
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#renderCompressed()
     */
    @RolesAllowed("user")
    public byte[] renderCompressed(PlaneDef pd) throws ResourceError,
            ValidationException {
        int[] buf = renderAsPackedInt(pd);
        int sizeX = pixelsObj.getSizeX();
        int sizeY = pixelsObj.getSizeY();
        BufferedImage image = ImageUtil.createBufferedImage(buf, sizeX, sizeY);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            compressionSrv.compressToStream(image, byteStream);
            return byteStream.toByteArray();
        } catch (IOException e) {
            log.error("Could not compress rendered image.", e);
            throw new ResourceError(e.getMessage());
        } finally {
            try {
                byteStream.close();
            } catch (IOException e) {
                log.error("Could not close byte stream.", e);
                throw new ResourceError(e.getMessage());
            }
        }
    }

    // ~ Settings
    // =========================================================================

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#resetDefaults()
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void resetDefaults() {
        rwl.writeLock().lock();

        try {
            errorIfNullPixels();
            long pixelsId = pixelsObj.getId();

            // Ensure that we haven't just been called before
            // lookupRenderingDef().
            if (rendDefObj == null)
            {
				RenderingDef def = pixMetaSrv.retrieveRndSettings(pixelsId); 
				if (def != null)
				{
					errorIfInvalidState();
				}
                List<Family> families =
                	pixMetaSrv.getAllEnumerations(Family.class);
                List<RenderingModel> renderingModels =
                	pixMetaSrv.getAllEnumerations(RenderingModel.class);
                QuantumFactory quantumFactory = new QuantumFactory(families);
           		PixelBuffer buffer = pixDataSrv.getPixelBuffer(pixelsObj);
           		def = Renderer.createNewRenderingDef(pixelsObj);
           		Renderer.resetDefaults(def, pixelsObj, quantumFactory,
           		                       renderingModels, buffer);
           		buffer.close();
            	pixMetaSrv.saveRndSettings(def);
            }
            else
            {
            	errorIfInvalidState();
            	renderer.resetDefaults();
            	
            	// Increment the version of the rendering settings so that we 
            	// can have some notification that either the RenderingDef 
            	// object itself or one of its children in the object graph has 
            	// been updated. FIXME: This should be implemented using 
            	// IUpdate.touch() or similar once that functionality exists.
                rendDefObj.setVersion(rendDefObj.getVersion() + 1);
                
                // Actually save the rendering settings
                pixMetaSrv.saveRndSettings(rendDefObj);
                rendDefObj = reload(rendDefObj);
            	
            	// The above save step sets the rendDefObj instance (for which 
            	// the renderer hold a reference) unloaded, which *will* cause 
            	// IllegalStateExceptions if we're not careful. To compensate
            	// we will now reload the renderer.
            	// *** Ticket #848 -- Chris Allan <callan@blackcat.ca> ***
                load();
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("An I/O error occurred.", e);
            }
            throw new ResourceError(e.getMessage()
                    + " Please check server log.");
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#resetDefaults()
     */
    @RolesAllowed("user")
    public void resetDefaultsNoSave() {
        rwl.writeLock().lock();
        try {
            errorIfInvalidState();
            renderer.resetDefaults();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setCompressionLevel()
     */
	@RolesAllowed("user")
	public void setCompressionLevel(float percentage)
	{
		compressionSrv.setCompressionLevel(percentage);
	}

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getCompressionLevel()
     */
	@RolesAllowed("user")
	public float getCompressionLevel()
	{
		return compressionSrv.getCompressionLevel();
	}

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#saveCurrentSettings()
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void saveCurrentSettings() {
        rwl.writeLock().lock();

        try {
            errorIfNullRenderingDef();
            
            // Increment the version of the rendering settings so that we can
            // have some notification that either the RenderingDef object
            // itself or one of its children in the object graph has been
            // updated. FIXME: This should be implemented using IUpdate.touch()
            // or similar once that functionality exists.
            rendDefObj.setVersion(rendDefObj.getVersion() + 1);
            
    	    // Unload the model object to avoid transactional headaches
    	    RenderingModel unloadedModel = 
    	    	new RenderingModel(rendDefObj.getModel().getId());
    	    unloadedModel.unload();
    	    rendDefObj.setModel(unloadedModel);
            
    	    // Unload the family of each channel binding to avoid transactional 
    	    // headaches.
    	    for (Object object : rendDefObj.getWaveRendering())
    	    {
    	    	ChannelBinding binding = (ChannelBinding) object;
    	    	Family unloadedFamily = new Family(binding.getFamily().getId());
    	    	unloadedFamily.unload();
    	    	binding.setFamily(unloadedFamily);
    	    }
            
            // Actually save and reload the rendering settings
            pixMetaSrv.saveRndSettings(rendDefObj);
            rendDefObj = reload(rendDefObj);
            
            // The above save and reload step sets the rendDefObj instance
            // (for which the renderer hold a reference) unloaded, which *will* 
            // cause IllegalStateExceptions if we're not careful. To compensate
            // we will now reload the renderer.
            // *** Ticket #848 -- Chris Allan <callan@blackcat.ca> ***
            load();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    // ~ Renderer Delegation (READ)
    // =========================================================================

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getChannelCurveCoefficient(int)
     */
    @RolesAllowed("user")
    public double getChannelCurveCoefficient(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getCoefficient().doubleValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getChannelFamily(int)
     */
    @RolesAllowed("user")
    public Family getChannelFamily(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            Family family = cb[w].getFamily();
            return copyFamily(family);
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getChannelNoiseReduction(int)
     */
    @RolesAllowed("user")
    public boolean getChannelNoiseReduction(int w) {
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
    @RolesAllowed("user")
    public double[] getChannelStats(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            // ChannelBinding[] cb = renderer.getChannelBindings();
            // FIXME
            // double[] stats = cb[w].getStats(), copy = new
            // double[stats.length];
            // System.arraycopy(stats, 0, copy, 0, stats.length);
            return null;
        } finally {
            rwl.readLock().unlock();
        }// FIXME copy;
        // NOTE: These stats are supposed to be read-only; however we make a
        // copy to be on the safe side.
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getChannelWindowEnd(int)
     */
    @RolesAllowed("user")
    public double getChannelWindowEnd(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getInputEnd().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getChannelWindowStart(int)
     */
    @RolesAllowed("user")
    public double getChannelWindowStart(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getInputStart().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getRGBA(int)
     */
    @RolesAllowed("user")
    public int[] getRGBA(int w) {

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

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#isActive(int)
     */
    @RolesAllowed("user")
    public boolean isActive(int w) {
        rwl.readLock().lock();

        try {
            errorIfInvalidState();
            ChannelBinding[] cb = renderer.getChannelBindings();
            return cb[w].getActive().booleanValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    // ~ RendDefObj Delegation
    // =========================================================================

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getDefaultT()
     */
    @RolesAllowed("user")
    public int getDefaultT() {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getDefaultT().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getDefaultZ()
     */
    @RolesAllowed("user")
    public int getDefaultZ() {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return rendDefObj.getDefaultZ().intValue();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getModel()
     */
    @RolesAllowed("user")
    public RenderingModel getModel() {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return copyRenderingModel(rendDefObj.getModel());
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getQuantumDef()
     */
    @RolesAllowed("user")
    public QuantumDef getQuantumDef() {
        rwl.readLock().lock();

        try {
            errorIfNullRenderingDef();
            return new ShallowCopy().copy(rendDefObj.getQuantization());
        } finally {
            rwl.readLock().unlock();
        }
    }

    // ~ Pixels Delegation
    // =========================================================================

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getPixels()
     */
    @RolesAllowed("user")
    public Pixels getPixels() {
        rwl.readLock().lock();

        try {
            errorIfNullPixels();
            return copyPixels(pixelsObj);
        } finally {
            rwl.readLock().unlock();
        }
    }

    // ~ Service Delegation
    // =========================================================================

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getAvailableModels()
     */
    @RolesAllowed("user")
    public List getAvailableModels() {
        rwl.readLock().lock();

        try {
            List<RenderingModel> models = pixMetaSrv
                    .getAllEnumerations(RenderingModel.class);
            List<RenderingModel> result = new ArrayList<RenderingModel>();
            for (RenderingModel model : models) {
                result.add(copyRenderingModel(model));
            }
            return result;
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getAvailableFamilies()
     */
    @RolesAllowed("user")
    public List getAvailableFamilies() {
        rwl.readLock().lock();

        try {
            List<Family> families = pixMetaSrv.getAllEnumerations(Family.class);
            List<Family> result = new ArrayList<Family>();
            for (Family family : families) {
                result.add(copyFamily(family));
            }
            return result;
        } finally {
            rwl.readLock().unlock();
        }
    }

    // ~ Renderer Delegation (WRITE)
    // =========================================================================

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    @RolesAllowed("user")
    public void addCodomainMap(CodomainMapContext mapCtx) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.getCodomainChain().add(mapCtx.copy());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    @RolesAllowed("user")
    public void removeCodomainMap(CodomainMapContext mapCtx) {
        rwl.writeLock().lock();
        try {
            errorIfInvalidState();
            renderer.getCodomainChain().remove(mapCtx.copy());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /** Implemented as specified by the {@link RenderingEngine} interface. */
    @RolesAllowed("user")
    public void updateCodomainMap(CodomainMapContext mapCtx) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.getCodomainChain().update(mapCtx.copy());
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setActive(int, boolean)
     */
    @RolesAllowed("user")
    public void setActive(int w, boolean active) {
        try {
            rwl.writeLock().lock();
            errorIfInvalidState();
            renderer.setActive(w, active);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setChannelWindow(int, double, double)
     */
    @RolesAllowed("user")
    public void setChannelWindow(int w, double start, double end) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setChannelWindow(w, start, end);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setCodomainInterval(int, int)
     */
    @RolesAllowed("user")
    public void setCodomainInterval(int start, int end) {
        rwl.writeLock().lock();
        try {
            errorIfInvalidState();
            renderer.setCodomainInterval(start, end);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setDefaultT(int)
     */
    @RolesAllowed("user")
    public void setDefaultT(int t) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setDefaultT(t);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setDefaultZ(int)
     */
    @RolesAllowed("user")
    public void setDefaultZ(int z) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setDefaultZ(z);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setModel(RenderingModel)
     */
    @RolesAllowed("user")
    public void setModel(RenderingModel model) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            RenderingModel m = lookup(model);
            renderer.setModel(m);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setQuantizationMap(int, Family, double, boolean)
     */
    @RolesAllowed("user")
    public void setQuantizationMap(int w, Family family, double coefficient,
            boolean noiseReduction) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            Family f = lookup(family);
            renderer.setQuantizationMap(w, f, coefficient, noiseReduction);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setQuantumStrategy(int)
     */
    @RolesAllowed("user")
    public void setQuantumStrategy(int bitResolution) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setQuantumStrategy(bitResolution);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#setRGBA(int, int, int, int, int)
     */
    @RolesAllowed("user")
    public void setRGBA(int w, int red, int green, int blue, int alpha) {
        rwl.writeLock().lock();

        try {
            errorIfInvalidState();
            renderer.setRGBA(w, red, green, blue, alpha);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#isPixelsTypeSigned()
     */
    @RolesAllowed("user")
    public boolean isPixelsTypeSigned() {
        rwl.readLock().lock();
        try {
            errorIfInvalidState();
            return renderer.isPixelsTypeSigned();
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getPixelsTypeLowerBound(int)
     */
    @RolesAllowed("user")
    public double getPixelsTypeLowerBound(int w) {
        rwl.readLock().lock();
        try {
            errorIfInvalidState();
            return renderer.getPixelsTypeLowerBound(w);
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Implemented as specified by the {@link RenderingEngine} interface.
     * 
     * @see RenderingEngine#getPixelsTypeUpperBound(int)
     */
    @RolesAllowed("user")
    public double getPixelsTypeUpperBound(int w) {
        rwl.readLock().lock();
        try {
            errorIfInvalidState();
            return renderer.getPixelsTypeUpperBound(w);
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Close the active renderer, cleaning up any potential messes left by the
     * included pixel buffer.
     */
    private void closeRenderer() {
        if (renderer != null) {
            renderer.close();
        }
    }

    // ~ Error checking methods
    // =========================================================================

    protected final static String NULL_RENDERER = "RenderingEngine not ready: renderer is null.\n"
            + "This method can only be called "
            + "after the renderer is properly "
            + "initialized (not-null).\n"
            + "Try lookup and/or use methods.";

    // TODO ObjectUnreadyException
    protected void errorIfInvalidState() {
        errorIfNullPixels();
        errorIfNullRenderingDef();
        errorIfNullRenderer();
    }

    protected void errorIfNullPixels() {
        if (pixelsObj == null) {
            throw new ApiUsageException(
                    "RenderingEngine not ready: Pixels object not set.");
        }
    }

    protected void errorIfNullRenderingDef() {
    	if (rendDefObj == null) {
    		throw new ApiUsageException(
    				"RenderingEngine not ready: RenderingDef object not set.");
    	}
    }

    protected void errorIfNullRenderer()
    {
    	if (renderer == null && wasPassivated)
    	{
    		load();
    	}
    	else if (renderer == null)
    	{
    		throw new ApiUsageException(NULL_RENDERER);
    	}
    }

    // ~ Lookups & copies
    // =========================================================================

    private <T extends IObject> T lookup(T argument) {
        if (argument == null) {
            return null;
        }
        if (argument.getId() == null) {
            return argument;
        }
        return (T) iQuery
                .get(argument.getClass(), argument.getId().longValue());
    }

    @SuppressWarnings("unchecked")
    private Pixels copyPixels(Pixels pixels) {
        if (pixels == null) {
            return null;
        }
        Pixels newPixels = new ShallowCopy().copy(pixels);
        copyChannels(pixels, newPixels);
        newPixels.setPixelsDimensions(new ShallowCopy().copy(pixels
                .getPixelsDimensions()));
        newPixels.setPixelsType(new ShallowCopy().copy(pixels.getPixelsType()));
        return newPixels;
    }

    private void copyChannels(Pixels from, Pixels to) {
        java.util.Iterator<Channel> it = from.iterateChannels();
        while (it.hasNext()) {
            to.addChannel(it.next());
        }
    }

    private Channel copyChannel(Channel channel) {
        Channel newChannel = new ShallowCopy().copy(channel);
        newChannel.setLogicalChannel(new ShallowCopy().copy(channel
                .getLogicalChannel()));
        newChannel.setStatsInfo(new ShallowCopy().copy(channel.getStatsInfo()));
        return newChannel;
    }

    private RenderingModel copyRenderingModel(RenderingModel model) {
        if (model == null) {
            return null;
        }
        RenderingModel newModel = new RenderingModel();
        newModel.setId(model.getId());
        newModel.setValue(model.getValue());
        newModel.getDetails().copy(
                model.getDetails() == null ? null : model.getDetails()
                        .shallowCopy());
        return newModel;
    }

    private Family copyFamily(Family family) {
        if (family == null) {
            return null;
        }
        Family newFamily = new Family();
        newFamily.setId(family.getId());
        newFamily.setValue(family.getValue());
        newFamily.getDetails().copy(
                family.getDetails() == null ? null : family.getDetails()
                        .shallowCopy());
        return newFamily;
    }

    /**
     * Reloads an {@link Pixels} or {@link RenderingDef} object following a
     * <pre>clear()</pre> being called on the Hibernate session.
     * @param t The object to reload.
     * @return A reloaded object of the same type as that passed in.
     */
    @SuppressWarnings("unchecked")
    private <T extends IObject> T reload(T t)
    {
    	if (t != null)
    	{
    		if (t instanceof RenderingDef)
    		{
    			return (T) pixMetaSrv.retrieveRndSettings(pixelsObj.getId());
    		}
    		else if (t instanceof Pixels)
    		{
    			return (T) pixMetaSrv.retrievePixDescription(t.getId());
    		}
    		else
    		{
    			throw new RuntimeException(
    					"Unknown reload class: " + t.getClass());
    		}
    	}
    	return t;
    }
}
