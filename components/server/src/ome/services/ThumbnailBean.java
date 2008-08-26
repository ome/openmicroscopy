/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

// Java imports
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.IScale;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.api.local.Destroy;
import ome.api.local.LocalCompress;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.io.nio.ThumbnailService;
import ome.logic.AbstractLevel2Service;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import ome.util.ImageUtil;
import omeis.providers.re.Renderer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides methods for directly querying object graphs. The service is entirely
 * read/write transactionally because of the requirements of rendering engine
 * lazy object creation where rendering settings are missing.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 * 
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateful
@Remote(ThumbnailStore.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.ThumbnailStore"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.ThumbnailStore",
                   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(ThumbnailStore.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ThumbnailStore")
@Interceptors( { OmeroAroundInvoke.class })
public class ThumbnailBean extends AbstractLevel2Service implements
        ThumbnailStore, Serializable, Destroy {
    /**
     * 
     */
    private static final long serialVersionUID = 3047482880497900069L;

    /** The logger for this class. */
    private transient static Log log = LogFactory.getLog(ThumbnailBean.class);

    /** The renderer that this service uses for thumbnail creation. */
    private transient Renderer renderer;

    /** The scaling service will be used to scale buffered images. */
    private transient IScale iScale;
    
    /** The pixels service, will be used to load pixels and settings. */
    private transient IPixels iPixels;
    
    /** The service used to retrieve the pixels data. */
    private transient PixelsService pixelDataService;

    /** The ROMIO thumbnail service. */
    private transient ThumbnailService ioService;

    /** The disk space checking service. */
    private transient IRepositoryInfo iRepositoryInfo;
    
    /** The JPEG compression service. */
    private transient LocalCompress compressionService;
    
    /** The rendering settings service. */
    private transient IRenderingSettings settingsService;
    
    /** The list of all families supported by the {@link Renderer}. */
    private transient List<Family> families;
    
    /** The list of all rendering models supported by the {@link Renderer}. */
	private transient List<RenderingModel> renderingModels;

    /** If the file service checking for disk overflow. */
    private transient boolean diskSpaceChecking;
    
    /** If the renderer is dirty. */
    private Boolean dirty = true;

    /** The pixels instance that the service is currently working on. */
    private Pixels pixels;
    
    /** The rendering settings that the service is currently working with. */
    private RenderingDef settings;
    
    /** The thumbnail metadata that the service is currently working with. */
    private Thumbnail metadata;
    
    /**
     * The last time the thumbnail was updated. We're storing this as an
     * instance variable due to the possibility that an UpdateEvent will be
     * lost during an IUpdate save. This is particularly evident when two
     * Thumbnail objects have the same UpdateEvent and a Hibernate save of one
     * unloads the UpdateEvent of the other.
     */
    private Timestamp lastUpdated;

    /** The default X-width for a thumbnail. */
    public static final int DEFAULT_X_WIDTH = 48;

    /** The default Y-width for a thumbnail. */
    public static final int DEFAULT_Y_WIDTH = 48;

    /** The default compression quality in fractional percent. */
    public static final float DEFAULT_COMPRESSION_QUALITY = 0.85F;

    /** The default MIME type. */
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

    /** Notification that the bean has just returned from passivation. */
    private transient boolean wasPassivated = false;
    
    /** default constructor */
    public ThumbnailBean() {}
    
    /**
     * overriden to allow Spring to set boolean
     * @param checking
     */
    public ThumbnailBean(boolean checking) {
    	this.diskSpaceChecking = checking;
    }
    
    public Class<? extends ServiceInterface> getServiceInterface() {
        return ThumbnailStore.class;
    }

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
    }

    /** lifecycle method -- {@link PrePassivate}. */
    @PrePassivate
    public void passivate() {
    	log.info("***** Passivating... *****");
    	if (renderer != null)
    	{
    		renderer.close();
    	}
    	renderer = null;
    }
    
    @PreDestroy
    @RolesAllowed("user")
    public void destroy() {
    	// Both the pixels and rendering settings objects are being passivated.
    	if (renderer != null)
    	{
    		renderer.close();
    	}
    	renderer = null;
    	iScale = null;
    	ioService = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @RolesAllowed("user")
    @Remove
    public void close() {
        // don't need to do anything.
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#getCurrentEventContext()
     */
    public EventContext getCurrentEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#setPixelsId(long)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public boolean setPixelsId(long id)
    {
    	// If we've had a pixels set change, reset our stateful objects.
    	if (pixels != null && pixels.getId() != id)
    	{
    	    resetMetadata();
    	}

    	// Handle lookup of pixels object, we're only performing a shallow load 
    	// here to avoid unnecessary overhead.
    	if (pixels == null)
    	{
    		pixels = iQuery.get(Pixels.class, id);
    	}

    	// Handle lookup of rendering settings, again we're only performing a
    	// shallow load of the RenderingDef and its details.
    	if (settings == null)
    	{
            Long userId = getSecuritySystem().getEventContext().getCurrentUserId();
            Parameters params = new Parameters();
            params.addLong("p_id", id);
            params.addLong("o_id", userId);
            settings = iQuery.findByQuery(
                    "select r from RenderingDef as r " +
                    "join fetch r.details.updateEvent where " +
                    "r.pixels.id = :p_id and r.details.owner.id = :o_id",
                    params);
    		if (settings == null)
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Retrieves a list of the families supported by the {@link Renderer}
     * either from instance variable cache or the database.
     * @return See above.
     */
    private List<Family> getFamilies()
    {
    	if (families == null)
    	{
    		families = iPixels.getAllEnumerations(Family.class);
    	}
    	return families;
    }
    
    /**
     * Retrieves a list of the rendering models supported by the 
     * {@link Renderer} either from instance variable cache or the database.
     * @return See above.
     */
    private List<RenderingModel> getRenderingModels()
    {
    	if (renderingModels == null)
    	{
    		renderingModels = iPixels.getAllEnumerations(RenderingModel.class);
    	}
    	return renderingModels;
    }
    
    /**
     * Retrieves a deep copy of the pixels set and rendering settings as
     * required for a rendering event and creates a renderer. This method
     * should only be called if a rendering event is required.
     */
    private void load()
    {
    	if (renderer != null)
    	{
    		renderer.close();
    	}
    	pixels = iPixels.retrievePixDescription(pixels.getId());
    	settings = iPixels.retrieveRndSettings(pixels.getId());
    	PixelBuffer buffer = pixelDataService.getPixelBuffer(pixels);
    	List<Family> families = getFamilies();
    	List<RenderingModel> renderingModels = getRenderingModels();
    	QuantumFactory quantumFactory = new QuantumFactory(families);
    	renderer = new Renderer(quantumFactory, renderingModels, pixels,
    	                        settings, buffer);
    	dirty = false;
    }

    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#setRenderingDefId(java.lang.Long)
     */
    @RolesAllowed("user")
    public void setRenderingDefId(long id)
    {
        RenderingDef newSettings = iPixels.loadRndSettings(id);
        if (newSettings == null)
        {
            throw new ValidationException(
                    "No rendering definition exists with ID: " + id);
        }
        if (!settingsService.sanityCheckPixels(pixels, newSettings.getPixels()))
        {
            throw new ValidationException(
                    "The rendering definition " + id + " is incompatible " +
                    "with pixels set " + pixels.getId());
        }
        settings = newSettings;
        if (log.isDebugEnabled())
        {
            log.debug("setRenderingDefId for RenderingDef=" + id
                      + " succeeded: " + this.settings);
        }
    }

    /**
     * Pixels service Bean injector.
     * 
     * @param iPixels
     *            an <code>IPixels</code>.
     */
    public void setPixelDataService(PixelsService pixelDataService) {
        getBeanHelper().throwIfAlreadySet(this.pixelDataService, pixelDataService);
        this.pixelDataService = pixelDataService;
    }
    
    /**
     * Pixels service Bean injector.
     * 
     * @param iPixels
     *            an <code>IPixels</code>.
     */
    public void setIPixels(IPixels iPixels) {
        getBeanHelper().throwIfAlreadySet(this.iPixels, iPixels);
        this.iPixels = iPixels;
    }
    
    /**
     * Scale service Bean injector.
     * 
     * @param iScale
     *            an <code>IScale</code>.
     */
    public void setScaleService(IScale iScale) {
        getBeanHelper().throwIfAlreadySet(this.iScale, iScale);
        this.iScale = iScale;
    }

    /**
     * I/O service (ThumbnailService) Bean injector.
     * 
     * @param ioService
     *            a <code>ThumbnailService</code>.
     */
    public void setIoService(ThumbnailService ioService) {
        getBeanHelper().throwIfAlreadySet(this.ioService, ioService);
        this.ioService = ioService;
    }

    /**
     * Disk Space Usage service Bean injector
     * @param iRepositoryInfo
     *   		  	an <code>IRepositoryInfo</code>
     */
    public final void setIRepositoryInfo(IRepositoryInfo iRepositoryInfo) {
        getBeanHelper().throwIfAlreadySet(this.iRepositoryInfo, iRepositoryInfo);
        this.iRepositoryInfo = iRepositoryInfo;
    }
    
    /**
     * Compression service Bean injector.
     * 
     * @param compressionService
     *            an <code>ICompress</code>.
     */
    public void setCompressionService(LocalCompress compressionService) {
        getBeanHelper().throwIfAlreadySet(this.compressionService,
                                          compressionService);
        this.compressionService = compressionService;
    }
    
    /**
     * Rendering settings service Bean injector.
     * 
     * @param settingsService
     *            an <code>IRenderingSettings</code>.
     */
    public void setSettingsService(IRenderingSettings settingsService) {
        getBeanHelper().throwIfAlreadySet(this.settingsService,
                                          settingsService);
        this.settingsService = settingsService;
    }
    
    /**
     * Compresses a buffered image thumbnail to disk.
     * 
     * @param thumb
     *            the thumbnail metadata.
     * @param image
     *            the thumbnail's buffered image.
     * @throws IOException
     *             if there is a problem writing to disk.
     */
    private void compressThumbnailToDisk(Thumbnail thumb, BufferedImage image)
            throws IOException {

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }

        FileOutputStream stream = ioService.getThumbnailOutputStream(thumb);
        compressionService.compressToStream(image, stream);
        stream.close();
    }
    
    /**
     * Returns the Id of the currently logged in user.
     * @return See above.
     */
    private Long getCurrentUserId()
    {
        return getSecuritySystem().getEventContext().getCurrentUserId();
    }

    /**
     * Retrieves metadata for a thumbnail of the active pixels set and X-Y
     * dimensions.
     * 
     * @param dimensions The dimensions of the thumbnail.
     * @return the thumbnail metadata. <code>null</code> if the object does
     *         not exist.
     */
    private Thumbnail getThumbnailMetadata(Dimension dimensions) {
        Long userId = settings.getDetails().getOwner().getId();
        Parameters params = new Parameters();
        params.addId(pixels.getId());
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addLong("o_id", userId);

        Thumbnail thumb = iQuery.findByQuery(
        	"select t from Thumbnail as t "+
        	"join fetch t.details.updateEvent " +
        	"where t.pixels.id = :id and t.sizeX = :x and t.sizeY = :y and " +
        	"t.details.owner.id = :o_id", params);
        return thumb;
    }

    /**
     * Retrieves metadata for all thumbnails associated with the active pixels
     * set.
     * 
     * @return the thumbnail metadata. <code>null</code> if the object does
     *         not exist.
     */
    private List<Thumbnail> getThumbnailMetadata() {
        Long userId = settings.getDetails().getOwner().getId();
        List<Thumbnail> thumbs = iQuery.findAllByQuery(
                "select t from Thumbnail as t where t.pixels.id = :id and " +
                "t.details.owner.id = :ownerid", new Parameters().
                addId(pixels.getId()).addLong("ownerid", userId));
        return thumbs;
    }
    
    /**
     * Creates metadata for a thumbnail if the active pixels set and X-Y
     * dimensions.
     * 
     * @param dimensions The dimensions of the thumbnail.
     * @return the thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(Dimension dimensions)
    {
        // Unload the pixels object to avoid transactional headaches
        Pixels unloadedPixels = new Pixels(pixels.getId(), false);

        Thumbnail thumb = new Thumbnail();
        thumb.setPixels(unloadedPixels);
        thumb.setMimeType(DEFAULT_MIME_TYPE);
        thumb.setSizeX((int) dimensions.getWidth());
        thumb.setSizeY((int) dimensions.getHeight());
        return thumb;
    }

    /**
     * Checks to see if a thumbnail is in the on disk cache or not.
     * 
     * @param dimension The dimension of the thumbnail.
     * @return Whether or not the thumbnail is in the on disk cache.
     */
    private boolean isThumbnailCached(Dimension dimension)
    {
        Timestamp settingsTime = 
            settings.getDetails().getUpdateEvent().getTime();
        if (log.isDebugEnabled())
        {
        	log.debug("Thumb time: " + lastUpdated);
        	log.debug("Settings time: " + settingsTime);
        }
        
        try
        {
            if (metadata != null && lastUpdated != null
                && !settingsTime.after(lastUpdated)
                && ioService.getThumbnailExists(metadata))
            {
                return true;
            }
        }
        catch (IOException e)
        {
            String s = "Could not check if thumbnail is cached: ";
            log.error(s, e);
            throw new ResourceError(s + e.getMessage());
        }
        return false;
    }
    
    /**
     * Adds the Id of a particular set of Pixels to the correct dimension pool 
     * based on the requested longest side.
     * 
     * @param pools Map of the current dimension pools.
     * @param pixels Pixels set to add to the correct dimension pool.
     * @param size Requested longest side.
     */
    private void addToDimensionPool(Map<Dimension, Set<Long>> pools,
                                    Pixels pixels, Integer size)
    {
        // Calculate the XY widths we would use for a thumbnail of Pixels
        Dimension dimensions = calculateXYWidths(pixels, size);
        
        // If the XY widths already have a pool (an instance that only differs
        // by object reference) find it and use that as our hash key.
        Set<Long> pool;
        for (Dimension poolDimensions : pools.keySet())
        {
            if (poolDimensions.equals(dimensions))
            {
                dimensions = poolDimensions;
                break;
            }
        }
        
        // Insert the Pixels set into
        pool = pools.get(dimensions);
        if (pool == null)
        {
            pool = new HashSet<Long>();
        }
        pool.add(pixels.getId());
        pools.put(dimensions, pool);
    }
    
    /**
     * Calculates the ratio of the two sides of a Pixel set and returns the
     * X and Y widths based on the longest side maintaining aspect ratio.
     * 
     * @param size The size of the longest side of the thumbnail requested.
     * @return The calculated width (X) and height (Y).
     */
    private Dimension calculateXYWidths(int size)
    {
    	return calculateXYWidths(pixels, size);
    }
    
    /**
     * Calculates the ratio of the two sides of a Pixel set and returns the
     * X and Y widths based on the longest side maintaining aspect ratio.
     * 
     * @param pixels The Pixels set to calculate against.
     * @param size The size of the longest side of the thumbnail requested.
     * @return The calculated width (X) and height (Y).
     */
    private Dimension calculateXYWidths(Pixels pixels, int size)
    {
        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        if (sizeX > sizeY)
        {
            float ratio = (float) size / sizeX;
            return new Dimension(size, (int) (sizeY * ratio));
        }
        else
        {
            float ratio = (float) size / sizeY;
            return new Dimension((int) (sizeX * ratio), size);
        }
    }

    /**
     * Checks that sizeX and sizeY are not out of range for the active pixels
     * set and returns a set of valid dimensions.
     * 
     * @param sizeX
     *            the X-width for the requested thumbnail.
     * @param sizeY
     *            the Y-width for the requested thumbnail.
     * @return A set of valid XY dimensions.
     */
    private Dimension sanityCheckThumbnailSizes(Integer sizeX, Integer sizeY) {
        // Sanity checks
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeX < 0) {
            throw new ApiUsageException("sizeX is negative");
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        if (sizeY < 0) {
            throw new ApiUsageException("sizeY is negative");
        }
        return new Dimension(sizeX, sizeY);
    }

    /**
     * Creates a scaled buffered image from the active pixels set.
     * 
     * @param def
     *            the rendering settings to use for buffered image creation.
     * @param dimensions The X-Y dimensions of the requested scaled image.
     * @param theZ the optical section (offset across the Z-axis) requested. 
     * <pre>null</pre> signifies the rendering engine default.
     * @param theT the timepoint (offset across the T-axis) requested. 
     * <pre>null</pre> signifies the rendering engine default.
     * @return a scaled buffered image.
     */
    private BufferedImage createScaledImage(Dimension dimensions,
                                            Integer theZ, Integer theT)
    {
    	// Ensure that we have a valid state for rendering
    	errorIfInvalidState();
    	
    	// Original sizes and thumbnail metadata
    	int origSizeX = pixels.getSizeX();
    	int origSizeY = pixels.getSizeY();

    	// Retrieve our rendered data
    	if (theZ == null)
    		theZ = settings.getDefaultZ();
    	if (theT == null)
    		theT = settings.getDefaultT();
    	PlaneDef pd = new PlaneDef(PlaneDef.XY, theT);
    	pd.setZ(theZ);

    	// Render the planes and translate to a buffered image
    	BufferedImage image;
    	try
    	{
    		int[] buf = renderer.renderAsPackedInt(pd, null);
    		image = ImageUtil.createBufferedImage(buf, origSizeX, origSizeY);
    	} 
    	catch (IOException e)
    	{
    		ResourceError re = new ResourceError(
    				"IO error while rendering: " + e.getMessage());
    		re.initCause(e);
    		throw re;
    	}
    	catch (QuantizationException e)
    	{
    		InternalException ie = new InternalException(
    				"QuantizationException while rendering: " + e.getMessage());
    		ie.initCause(e);
    		throw ie;
    	}

    	// Finally, scale our image using scaling factors (percentage).
    	float xScale = (float) dimensions.getWidth() / origSizeX;
    	float yScale = (float) dimensions.getHeight() / origSizeY;
    	return iScale.scaleBufferedImage(image, xScale, yScale);
    }
    
    /**
     * Resets the current metadata state.
     */
    private void resetMetadata()
    {
        pixels = null;
        settings = null;
        dirty = true;
        metadata = null;
        lastUpdated = null;
    }
    
    protected void errorIfInvalidState()
    {
    	errorIfNullMetadata();
    	if ((renderer == null && wasPassivated) || dirty)
    	{
    		load();
    	}
    	else if (renderer == null)
    	{
    		throw new InternalException(
    			"Thumbnail service state corruption: Renderer missing.");
    	}
    }
    
    protected void errorIfNullMetadata()
    {
    	errorIfNullPixels();
    	errorIfNullRenderingDef();
    }

    protected void errorIfNullPixels()
    {
    	if (pixels == null)
    	{
    		throw new ApiUsageException(
    			"Thumbnail service not ready: Pixels not set.");
    	}
    }
    
    protected void errorIfNullRenderingDef()
    {
    	if (settings == null)
    	{
    		throw new InternalException(
    			"Thumbnail service state corruption: RenderingDef missing.");
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#createThumbnail(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer,
     *      java.lang.Integer)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void createThumbnail(Integer sizeX, Integer sizeY)
    {
        // Set defaults and sanity check thumbnail sizes
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);
    	_createThumbnail(new Dimension(sizeX, sizeY));
        
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
    }
    
    /** Actually does the work specified by {@link createThumbnail()}.*/
    private Thumbnail _createThumbnail(Dimension dimensions) {
        if (metadata == null) {
            metadata = createThumbnailMetadata(dimensions);
        } else {
            // Increment the version of the thumbnail so that its
            // update event has a timestamp equal to or after that of
            // the rendering settings. FIXME: This should be 
            // implemented using IUpdate.touch() or similar once that 
            // functionality exists.
            metadata.setVersion(metadata.getVersion() + 1);
            Pixels unloadedPixels = new Pixels(pixels.getId(), false);
            metadata.setPixels(unloadedPixels);
        }
        // We're doing this as a two step process due to the possible unloaded 
        // Pixels above. If we do not, Pixels will be unloaded and we will hit
        // IllegalStateException's when checking update events.
        metadata = iUpdate.saveAndReturnObject(metadata);
        lastUpdated = metadata.getDetails().getUpdateEvent().getTime();
        
        BufferedImage image = createScaledImage(dimensions, null, null);
        try {
            compressThumbnailToDisk(metadata, image);
            return metadata;
        } catch (IOException e) {
            log.error("Thumbnail could not be compressed.", e);
            throw new ResourceError(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#createThumbnails(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void createThumbnails() {
        List<Thumbnail> thumbnails = getThumbnailMetadata();

        for (Thumbnail t : thumbnails) {
            _createThumbnail(new Dimension(t.getSizeX(), t.getSizeY()));
        }
        
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
    }
    
    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailSet(java.lang.Integer, java.lang.Integer, java.util.Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public Map<Long, byte[]> getThumbnailSet(Integer sizeX, Integer sizeY,
                                             Set<Long> pixelsIds)
    {
        // Set defaults and sanity check thumbnail sizes
        Dimension checkedDimensions = sanityCheckThumbnailSizes(sizeX, sizeY);
        
        // Our return value HashMap
        Map<Long, byte[]> toReturn = new HashMap<Long, byte[]>();
        
        // First we try and bulk load as many rendering settings as possible,
        // loading the Pixels and Thumbnails as well to avoid extra database 
        // hits later.
        Long userId = getCurrentUserId();
        Parameters params = new Parameters();
        params.addInteger("x", (int) checkedDimensions.getWidth());
        params.addInteger("y", (int) checkedDimensions.getHeight());
        params.addLong("o_id", userId);
        params.addIds(pixelsIds);
        List<Pixels> pixelsList = iQuery.findAllByQuery(
                "from Pixels as p " +
                "join fetch p.details.updateEvent " +
                "left outer join fetch p.settings as rdef " +
                "left outer join fetch p.thumbnails as thumbnail " +
                "join fetch rdef.details.updateEvent " +
                "join fetch thumbnail.details.updateEvent " +
                "where rdef.details.owner.id = :o_id and " +
                "thumbnail.details.owner.id = :o_id and " +
                "thumbnail.sizeX = :x and thumbnail.sizeY = :y and " +
                "p.id in (:ids)", params);
        
        // Timestamp cache so that a save of an object containing an event
        // which is used elsewhere doesn't wipe out our timestamp. Also,
        // populating a pixels hash map so that we can easily determine which
        // Pixels Id's were retrieved above.
        Map<Long, Timestamp> timestampMap = new HashMap<Long, Timestamp>();
        Map<Long, Pixels> pixelsMap = new HashMap<Long, Pixels>();
        for (Pixels pixels : pixelsList)
        {
            pixelsMap.put(pixels.getId(), pixels);
            if (pixels.sizeOfThumbnails() > 0)
            {
                Details d = pixels.iterateThumbnails().next().getDetails();
                timestampMap.put(pixels.getId(), d.getUpdateEvent().getTime());
            }
        }
                
        for (Long pixelsId : pixelsIds)
    	{
    	    try
    	    {
                resetMetadata();
                if (pixelsMap.containsKey(pixelsId))
                {
                    pixels = pixelsMap.get(pixelsId);
                    settings = pixels.iterateSettings().next();
                    metadata = pixels.iterateThumbnails().next();
                    lastUpdated = timestampMap.get(pixelsId);
                }
                else
                {
                    if (!setPixelsId(pixelsId))
                    {
                        resetDefaults();
                        setPixelsId(pixelsId);
                    }
                }
    	        byte[] thumbnail = getThumbnail(sizeX, sizeY);
    	        toReturn.put(pixelsId, thumbnail);
    	    }
            catch (Throwable t)
            {
                log.warn("Retrieving thumbnail in set for Pixels ID "
                         + pixelsId + " failed.", t);
                toReturn.put(pixelsId, null);
            }
    	}
    	return toReturn;
    }
    
    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSideSet(java.lang.Integer, java.util.Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public Map<Long, byte[]> getThumbnailByLongestSideSet(Integer size,
                                                          Set<Long> pixelsIds)
    {
        // Set defaults and sanity check thumbnail sizes
        Dimension checkedDimensions = sanityCheckThumbnailSizes(size, size);
        
        // Our return value HashMap
    	Map<Long, byte[]> toReturn = new HashMap<Long, byte[]>();
    	
    	// First we try and bulk load as many rendering settings as possible,
    	// loading the Pixels as well to avoid extra database hits later.
    	Long userId = getCurrentUserId();
    	List<RenderingDef> settingsList = iQuery.findAllByQuery(
    			"select r from RenderingDef as r join fetch r.pixels " +
    			"join fetch r.details.updateEvent " +
    			"join fetch r.pixels.details.updateEvent " +
    			"where r.details.owner.id = :id and r.pixels.id in (:ids)",
    			new Parameters().addId(userId).addIds(pixelsIds));
    	
    	// We've got a set of settings, lets populate our hash maps.
    	Map<Long, RenderingDef> settingsMap = new HashMap<Long, RenderingDef>();
    	Map<Long, Thumbnail> metadataMap = new HashMap<Long, Thumbnail>();
    	Map<Dimension, Set<Long>> dimensionPools =
    		new HashMap<Dimension, Set<Long>>();
    	Map<Long, Timestamp> timestampMap = new HashMap<Long, Timestamp>();
    	for (RenderingDef def : settingsList)
    	{
    	    Pixels p = def.getPixels();
    		settingsMap.put(p.getId(), def);
    		addToDimensionPool(dimensionPools, p,
    		                   (int) checkedDimensions.getWidth());
    	}
    	
    	// Now we're going to attempt to efficiently retrieve the thumbnail
    	// metadata based on our dimension pools above. At worst, the result
    	// of maintaining the aspect ratio (calculating the new XY widths) is
    	// that we have to retrieve each thumbnail object separately.
    	for (Dimension dimensions : dimensionPools.keySet())
    	{
    		Set<Long> pool = dimensionPools.get(dimensions);
    	    Parameters params = new Parameters();
            params.addInteger("x", (int) dimensions.getWidth());
            params.addInteger("y", (int) dimensions.getHeight());
            params.addLong("o_id", userId);
            params.addIds(pool);
        	List<Thumbnail> thumbnailList = iQuery.findAllByQuery(
        			"select t from Thumbnail as t " +
        			"join fetch t.details.updateEvent where " +
        			"t.sizeX = :x and t.sizeY = :y and " + 
        			"t.details.owner.id = :o_id and t.pixels.id in (:ids)",
        			params);
        	for (Thumbnail metadata : thumbnailList)
        	{
        	    Long pixelsId = metadata.getPixels().getId();
        	    Timestamp t = metadata.getDetails().getUpdateEvent().getTime();
        		metadataMap.put(pixelsId, metadata);
        		timestampMap.put(pixelsId, t);
        	}
    	}
    	
    	// Loop through each pixels set that we've been requested to provide
    	// a thumbnail for, using the metadata that has already been retrieved
    	// if available. Thumbnail metadata will be lazily loaded if it is
    	// unavailable.
    	for (Long pixelsId : pixelsIds)
    	{
    	    try
    	    {
    	        resetMetadata();
    	        if (settingsMap.containsKey(pixelsId))
    	        {
    	            pixels = settingsMap.get(pixelsId).getPixels();
    	            settings = settingsMap.get(pixelsId);
    	            if (metadataMap.containsKey(pixelsId))
    	            {
    	                metadata = metadataMap.get(pixelsId);
    	                lastUpdated = timestampMap.get(pixelsId);
    	            }
    	        }
    	        else
    	        {
    	            if (!setPixelsId(pixelsId))
    	            {
    	                resetDefaults();
    	                setPixelsId(pixelsId);
    	            }
    	        }
    	        byte[] thumbnail = getThumbnailByLongestSide(size);
    	        toReturn.put(pixelsId, thumbnail);
    	    }
    	    catch (Throwable t)
    	    {
    	        log.warn("Retrieving thumbnail in set for " +
    	                 "Pixels ID " + pixelsId + " failed.", t);
    	        toReturn.put(pixelsId, null);
    	    }
    	}
    	return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#getThumbnail(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer,
     *      java.lang.Integer)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public byte[] getThumbnail(Integer sizeX, Integer sizeY) {
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

        if (metadata == null)
        {
            metadata = getThumbnailMetadata(dimensions);
            if (metadata != null)
            {
                lastUpdated = metadata.getDetails().getUpdateEvent().getTime();
            }
        }
        try
        {
            boolean cached = isThumbnailCached(dimensions);
            if (cached)
            {
            	if (log.isDebugEnabled())
            	{
            		log.debug("Cache hit.");
            	}
            }
            else
            {
            	if (log.isDebugEnabled())
            	{
            		log.debug("Cache miss, thumbnail missing or out of date.");
            	}
            	metadata = _createThumbnail(dimensions);
            }
            // Ensure that we do not have "dirty" pixels or rendering settings 
            // left around in the Hibernate session cache.
            iQuery.clear();
            
            byte[] thumbnail = ioService.getThumbnail(metadata);
            return thumbnail;
        }
        catch (IOException e)
        {
            log.error("Could not obtain thumbnail", e);
            throw new ResourceError(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSide(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public byte[] getThumbnailByLongestSide(Integer size) {
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(size, size);
        dimensions = calculateXYWidths((int) dimensions.getWidth());
        return getThumbnail((int) dimensions.getWidth(),
                            (int) dimensions.getHeight());
    }
    
    /** Actually does the work specified by {@link getThumbnailDirect()}.*/
    private byte[] _getThumbnailDirect(Integer sizeX, Integer sizeY,
                                       Integer theZ, Integer theT)
    {
    	// Set defaults and sanity check thumbnail sizes
    	Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

    	BufferedImage image = createScaledImage(dimensions, theZ, theT);
    	ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    	try {
    		compressionService.compressToStream(image, byteStream);
    		byte[] thumbnail = byteStream.toByteArray();
    		return thumbnail;
    	} catch (IOException e) {
    		log.error("Could not obtain thumbnail direct.", e);
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

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#getThumbnailDirect(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer,
     *      java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY)
    {
    	// Ensure that we do not have "dirty" pixels or rendering settings 
    	// left around in the Hibernate session cache.
    	iQuery.clear();
    	return _getThumbnailDirect(sizeX, sizeY, null, null);
    }
    
    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailForSectionDirect(int, int, java.lang.Integer, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailForSectionDirect(int theZ, int theT,
                                               Integer sizeX, Integer sizeY)
    {
    	// Ensure that we do not have "dirty" pixels or rendering settings 
    	// left around in the Hibernate session cache.
    	iQuery.clear();
    	return _getThumbnailDirect(sizeX, sizeY, theZ, theT);
    }
    
    /** Actually does the work specified by {@link getThumbnailByLongestSideDirect()}.*/
    private byte[] _getThumbnailByLongestSideDirect(Integer size, Integer theZ, 
                                                    Integer theT)
    {
        // Sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(size, size);

        dimensions = calculateXYWidths((int) dimensions.getWidth());
        return _getThumbnailDirect((int) dimensions.getWidth(),
                                   (int) dimensions.getHeight(), theZ, theT);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSideDirect(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailByLongestSideDirect(Integer size) {
    	// Ensure that we do not have "dirty" pixels or rendering settings 
    	// left around in the Hibernate session cache.
    	iQuery.clear();
    	return _getThumbnailByLongestSideDirect(size, null, null);
    }
    
    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailForSectionByLongestSideDirect(int, int, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailForSectionByLongestSideDirect(int theZ, int theT,
                                                            Integer size)
    {
    	// Ensure that we do not have "dirty" pixels or rendering settings 
    	// left around in the Hibernate session cache.
    	iQuery.clear();
    	return _getThumbnailByLongestSideDirect(size, theZ, theT);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#thumbnailExists(ome.model.core.Pixels,
     *      java.lang.Integer, java.lang.Integer)
     */
    @RolesAllowed("user")
    public boolean thumbnailExists(Integer sizeX, Integer sizeY) {
        // Set defaults and sanity check thumbnail sizes
        errorIfNullMetadata();
        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

        metadata = getThumbnailMetadata(dimensions);
        // Ensure that we do not have "dirty" pixels or rendering settings 
        // left around in the Hibernate session cache.
        iQuery.clear();
        if (metadata == null) {
            return false;
        }
        
        return true;
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void resetDefaults()
    {
        // Ensure that setPixelsId() has been called first.
        errorIfNullPixels();
        
        // Ensure that we haven't just been called before setPixelsId() and that
        // the rendering settings are null.
        Parameters params = new Parameters();
        params.addId(pixels.getId());
        params.addLong("o_id", getCurrentUserId());
        if (settings != null
            || iQuery.findByQuery(
                    "from RenderingDef as r where r.pixels.id = :id and " +
                    "r.details.owner.id = :o_id", params) != null)
        {
        	throw new ApiUsageException(
        		"The thumbnail service only resets **empty** rendering " +
        		"settings. Resetting of existing settings should either be " +
        		"performed using the RenderingEngine or IRenderingSettings.");
        }
        
        RenderingDef def = settingsService.createNewRenderingDef(pixels);
        settingsService.resetDefaults(def, pixels);
        iUpdate.flush();
        iUpdate.commit();
    }

	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}
}
