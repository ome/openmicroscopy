/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ome.annotations.RolesAllowed;
import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.IScale;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.api.local.LocalCompress;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.OriginalFileMetadataProvider;
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
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import ome.util.ImageUtil;
import omeis.providers.re.Renderer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
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
@Transactional(readOnly = true)
public class ThumbnailBean extends AbstractLevel2Service implements
        ThumbnailStore, Serializable {
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
    
    /** If the settings {@link metadata} is dirty. */
    private Boolean dirtyMetadata = false;

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
    private Timestamp metadataLastUpdated;
    
    /**
     * The last time the rendering settings were updated. We're storing this as 
     * an instance variable due to the possibility that an UpdateEvent will be
     * lost during an IUpdate save. This is particularly evident when two
     * Thumbnail objects have the same UpdateEvent and a Hibernate save of one
     * unloads the UpdateEvent of the other.
     */
    private Timestamp settingsLastUpdated;
    
    /**
     * The user Id of the current set of settings. We're storing this as 
     * an instance variable due to the possibility that an UpdateEvent will be
     * lost during an IUpdate save. This is particularly evident when two
     * Thumbnail objects have the same UpdateEvent and a Hibernate save of one
     * unloads the UpdateEvent of the other.
     */
    private Long settingsUserId;

    /** The default X-width for a thumbnail. */
    public static final int DEFAULT_X_WIDTH = 48;

    /** The default Y-width for a thumbnail. */
    public static final int DEFAULT_Y_WIDTH = 48;

    /** The default compression quality in fractional percent. */
    public static final float DEFAULT_COMPRESSION_QUALITY = 0.85F;

    /** The default MIME type. */
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

    /**
     * read-write lock to prevent READ-calls during WRITE operations.
     *
     * It is safe for the lock to be serialized. On deserialization, it will
     * be in the unlocked state.
     */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

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

    // ~ Lifecycle methods
    // =========================================================================

    // See documentation on JobBean#passivate
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public void passivate() {
	log.debug("***** Passivating... ******");

        rwl.writeLock().lock();
	try {
	    if (renderer != null) {
		renderer.close();
	    }
	    renderer = null;
	} finally {
	    rwl.writeLock().unlock();
	}
    }

    // See documentation on JobBean#activate
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public void activate() {
	log.debug("***** Returning from passivation... ******");

	rwl.writeLock().lock();
	try {
	    wasPassivated = true;
	} finally {
	    rwl.writeLock().unlock();
	}
    }

    @RolesAllowed("user")
    public void close() {
        rwl.writeLock().lock();

        try {
	    if (renderer != null) {
		renderer.close();
	    }
	    renderer = null;
	    iScale = null;
	    ioService = null;
        } finally {
            rwl.writeLock().unlock();
        }
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
            Long userId = getCurrentUserId();
            settings = getSettingsForUser(id, userId);
            if (settings == null) {
                // ticket:1434 and shoola:ticket:1157
                if (getSecuritySystem().isGraphCritical()) {
                    long ownerId = pixels.getDetails().getOwner().getId();
                    settings = getSettingsForUser(id, ownerId);
                }
                if (settings == null) {
                    return false;
                }
            }
    		settingsLastUpdated = 
    			settings.getDetails().getUpdateEvent().getTime();
    		settingsUserId = settings.getDetails().getOwner().getId();
    	}
    	return true;
    }

    private RenderingDef getSettingsForUser(long id, Long userId) {
        Parameters params = new Parameters();
        params.addLong("p_id", id);
        params.addLong("o_id", userId);
        return iQuery.findByQuery(
                "select r from RenderingDef as r " +
                "join fetch r.details.updateEvent where " +
                "r.pixels.id = :p_id and r.details.owner.id = :o_id",
                params);
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
    	settings = iPixels.loadRndSettings(settings.getId());
    	OriginalFileMetadataProvider metadataProvider =
    		new OmeroOriginalFileMetadataProvider(iQuery);
    	PixelBuffer buffer = 
    		pixelDataService.getPixelBuffer(pixels, metadataProvider, false);
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
        Long userId = getCurrentUserId();
        RenderingDef newSettings = getSettingsForUser(id, userId);
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
        settingsLastUpdated = 
                settings.getDetails().getUpdateEvent().getTime();
        settingsUserId = settings.getDetails().getOwner().getId();
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
     * Bulk loads a set of rendering sets for a group of pixels sets.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @return Loaded rendering settings for <code>pixelsIds</code>.
     */
    private List<RenderingDef> bulkLoadRenderingSettings(Set<Long> pixelsIds)
    {
    	StopWatch s1 = new CommonsLogStopWatch(
    			"omero.bulkLoadRenderingSettings");
    	List<RenderingDef> toReturn = iQuery.findAllByQuery(
    			"select r from RenderingDef as r join fetch r.pixels " +
    			"join fetch r.details.updateEvent " +
    			"join fetch r.pixels.details.updateEvent " +
    			"where r.details.owner.id = :id and r.pixels.id in (:ids)",
    			new Parameters().addId(getCurrentUserId()).addIds(pixelsIds));
    	s1.stop();
    	return toReturn;
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
     * Returns owner of the share while in share
     * @return See above.
     */
    private Long getCurrentUserId()
    {
        Long shareId = getSecuritySystem().getEventContext().getCurrentShareId();
        if (shareId != null) {
            Session s = iQuery.get(Session.class, shareId);
            return s.getOwner().getId();
        } 
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
        Parameters params = new Parameters();
        params.addId(pixels.getId());
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addLong("o_id", settingsUserId);

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
        List<Thumbnail> thumbs = iQuery.findAllByQuery(
                "select t from Thumbnail as t where t.pixels.id = :id and " +
                "t.details.owner.id = :ownerid", new Parameters().
                addId(pixels.getId()).addLong("ownerid", getCurrentUserId()));
        return thumbs;
    }
    
    /**
     * Creates metadata for a thumbnail of the active pixels set.
     * 
     * @param dimensions The dimensions of the thumbnail.
     * @return The thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(Dimension dimensions)
    {
    	return createThumbnailMetadata(pixels, dimensions);
    }
    
    /**
     * Creates metadata for a thumbnail of a given set of pixels set and X-Y
     * dimensions.
     * 
     * @param pixels The Pixels set to create thumbnail metadata for.
     * @param dimensions The dimensions of the thumbnail.
     * 
     * @return the thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(Pixels pixels,
    		                                  Dimension dimensions)
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
        if (log.isDebugEnabled())
        {
        	log.debug("Thumb time: " + metadataLastUpdated);
        	log.debug("Settings time: " + settingsLastUpdated);
        }
        
        try
        {
            if (metadata != null && metadataLastUpdated != null
                && !settingsLastUpdated.after(metadataLastUpdated)
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
        for (Dimension poolDimensions : pools.keySet())
        {
            if (poolDimensions.equals(dimensions))
            {
                dimensions = poolDimensions;
                break;
            }
        }
        
        // Insert the Pixels set into
        Set<Long> pool = pools.get(dimensions);
        if (pool == null)
        {
            pool = new HashSet<Long>();
        }
        pool.add(pixels.getId());
        pools.put(dimensions, pool);
    }
    
    /**
     * Attempts to efficiently retrieve the thumbnail metadata based on a set
     * of dimension pools. At worst, the result of maintaining the aspect ratio
     * (calculating the new XY widths) is that we have to retrieve each 
     * thumbnail object separately.
     * @param dimensionPools Dimension pools to query based upon.
     * @param metadataMap Dictionary of Pixels ID vs. thumbnail metadata. Will
     * be updated by this method.
     * @param metadataTimeMap Dictionary of Pixels ID vs. thumbnail metadata
     * last modification time. Will be updated by this method.
     */
    private void loadMetadataByDimensionPool(
    		Map<Dimension, Set<Long>> dimensionPools,
    		Map<Long, Thumbnail> metadataMap,
    		Map<Long, Timestamp> metadataTimeMap)
    {
    	StopWatch s1 = new CommonsLogStopWatch(
    			"omero.loadMetadataByDimensionPool");
    	Long userId = getCurrentUserId();
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
        			"join t.pixels " +
        			"join fetch t.details.updateEvent " +
        			"where t.sizeX = :x and t.sizeY = :y " + 
        			"and t.details.owner.id = :o_id " +
        			"and t.pixels.id in (:ids)", params);
        	for (Thumbnail metadata : thumbnailList)
        	{
        	    Long pixelsId = metadata.getPixels().getId();
        	    Timestamp t = metadata.getDetails().getUpdateEvent().getTime();
        		metadataMap.put(pixelsId, metadata);
        		metadataTimeMap.put(pixelsId, t);
        	}
    	}
    	s1.stop();
    }
    
    private void createMissingThumbnailMetadata(
    		Map<Long, Pixels> pixelsMap,
    		Map<Dimension, Set<Long>> dimensionPools,
    		Map<Long, Thumbnail> metadataMap,
    		Map<Long, Timestamp> metadataTimeMap,
    		Integer size)
    {
    	StopWatch s1 = new CommonsLogStopWatch(
			"omero.createMissingThumbnailMetadata");
    	List<Thumbnail> toSave = new ArrayList<Thumbnail>();
    	Map<Dimension, Set<Long>> temporaryDimensionPools = 
    		new HashMap<Dimension, Set<Long>>();
    	for (Long pixelsId : pixelsMap.keySet())
    	{
    		Pixels pixels = pixelsMap.get(pixelsId);
    		if (!metadataMap.containsKey(pixelsId))
    		{
    			for (Dimension dimension : dimensionPools.keySet())
    			{
    				Set<Long> pool = dimensionPools.get(dimension);
    				if (pool.contains(pixelsId))
    				{
    					toSave.add(createThumbnailMetadata(
    							pixelsMap.get(pixelsId), dimension));
    					addToDimensionPool(temporaryDimensionPools, pixels, size);
    				}
    			}
    		}
    	}
    	iUpdate.saveAndReturnIds(toSave.toArray(new Thumbnail[toSave.size()]));
    	loadMetadataByDimensionPool(temporaryDimensionPools, metadataMap,
    			                    metadataTimeMap);
    	s1.stop();
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
        dirtyMetadata = false;
        metadata = null;
        metadataLastUpdated = null;
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
    	try
    	{
    		// Set defaults and sanity check thumbnail sizes
    		if (sizeX == null) {
    			sizeX = DEFAULT_X_WIDTH;
    		}
    		if (sizeY == null) {
    			sizeY = DEFAULT_Y_WIDTH;
    		}
    		sanityCheckThumbnailSizes(sizeX, sizeY);
    		Dimension dimensions = new Dimension(sizeX, sizeY);
    		metadata = getThumbnailMetadata(dimensions);
    		if (metadata == null)
    		{
    			metadata = iUpdate.saveAndReturnObject(
    					createThumbnailMetadata(dimensions));
        		metadataLastUpdated = 
        			metadata.getDetails().getUpdateEvent().getTime();
    		}
    		metadata = _createThumbnail(new Dimension(sizeX, sizeY));
    		if (dirtyMetadata)
    		{
    			metadata = iUpdate.saveAndReturnObject(metadata);
        		metadataLastUpdated = 
        			metadata.getDetails().getUpdateEvent().getTime();
    		}

    		// Ensure that we do not have "dirty" pixels or rendering settings 
    		// left around in the Hibernate session cache.
    		iQuery.clear();
    	}
    	finally
    	{
    		dirtyMetadata = false;
    	}
    }
    
    /** Actually does the work specified by {@link createThumbnail()}.*/
    private Thumbnail _createThumbnail(Dimension dimensions) {
    	StopWatch s1 = new CommonsLogStopWatch("omero._createThumbnail");
        if (metadata == null) {
        	throw new ValidationException("Missing thumbnail metadata.");
        } else if (settingsLastUpdated.after(metadataLastUpdated)){
            // Increment the version of the thumbnail so that its
            // update event has a timestamp equal to or after that of
            // the rendering settings. FIXME: This should be 
            // implemented using IUpdate.touch() or similar once that 
            // functionality exists.
            metadata.setVersion(metadata.getVersion() + 1);
            Pixels unloadedPixels = new Pixels(pixels.getId(), false);
            metadata.setPixels(unloadedPixels);
            dirtyMetadata = true;
        }
        // dirtyMetadata is left false here because we may be creating a
        // thumbnail for the first time and the Thumbnail object has just been
        // created upstream of us.
        
        BufferedImage image = createScaledImage(dimensions, null, null);
        try {
            compressThumbnailToDisk(metadata, image);
            s1.stop();
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
    	try
    	{
    		List<Thumbnail> thumbnails = getThumbnailMetadata();

    		for (Thumbnail t : thumbnails) {
    			metadata = t;
    			_createThumbnail(new Dimension(t.getSizeX(), t.getSizeY()));
    		}
    		// We're doing the update or creation and save as a two step 
    		// process due to the possible unloaded Pixels. If we do not, 
    		// Pixels will be unloaded and we will hit 
    		// IllegalStateException's when checking update events.
    		iUpdate.saveArray(thumbnails.toArray(
    				new Thumbnail[thumbnails.size()]));

    		// Ensure that we do not have "dirty" pixels or rendering settings
    		// left around in the Hibernate session cache.
    		iQuery.clear();
    	}
    	finally
    	{
    		dirtyMetadata = false;
    	}
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void createThumbnailsByLongestSideSet(Integer size,
                                                 Set<Long> pixelsIds)
    {
    	getThumbnailByLongestSideSet(size, pixelsIds);
    }
    
    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailSet(java.lang.Integer, java.lang.Integer, java.util.Set)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public Map<Long, byte[]> getThumbnailSet(Integer sizeX, Integer sizeY,
                                             Set<Long> pixelsIds)
    {
    	// FIXME: This method is broken if called when thumbnails do not exist
    	// needs to be resolved.
    	
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
    	StopWatch s1 = new CommonsLogStopWatch(
			"omero.getThumbnailSet.loadSettingsAndThumbnails");
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
        s1.stop();
        
        // Timestamp cache so that a save of an object containing an event
        // which is used elsewhere doesn't wipe out our timestamp. Also,
        // populating a pixels hash map so that we can easily determine which
        // Pixels Id's were retrieved above.
        Map<Long, Timestamp> metadataTimeMap = new HashMap<Long, Timestamp>();
        Map<Long, Timestamp> settingsTimeMap = new HashMap<Long, Timestamp>();
        Map<Long, Long> settingsUserMap = new HashMap<Long, Long>();
        Map<Long, Pixels> pixelsMap = new HashMap<Long, Pixels>();
        Details details;
        for (Pixels pixels : pixelsList)
        {
        	Long pId = pixels.getId();
            pixelsMap.put(pId, pixels);
            if (pixels.sizeOfThumbnails() > 0)
            {
                details = pixels.iterateThumbnails().next().getDetails();
                metadataTimeMap.put(pId, details.getUpdateEvent().getTime());
                details = pixels.iterateSettings().next().getDetails();
                settingsTimeMap.put(pId, details.getUpdateEvent().getTime());
                settingsUserMap.put(pId, details.getOwner().getId());
            }
        }
        
        List<Thumbnail> toSave = new ArrayList<Thumbnail>(pixelsIds.size());
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
                    metadataLastUpdated = metadataTimeMap.get(pixelsId);
                    settingsLastUpdated = settingsTimeMap.get(pixelsId);
                    settingsUserId = settingsUserMap.get(pixelsId);
                }
                else
                {
                    if (!setPixelsId(pixelsId))
                    {
                        _resetDefaults();
                        setPixelsId(pixelsId);
                    }
                }
                try
                {
                	byte[] thumbnail = _getThumbnail(sizeX, sizeY);
                	toReturn.put(pixelsId, thumbnail);
                	if (dirtyMetadata)
                	{
                		toSave.add(metadata);
                	}
                }
    	        finally
    	        {
    	        	dirtyMetadata = false;
    	        }
    	    }
            catch (Throwable t)
            {
                log.warn("Retrieving thumbnail in set for Pixels ID "
                         + pixelsId + " failed.", t);
                toReturn.put(pixelsId, null);
            }
    	}
    	// We're doing the update or creation and save as a two step 
    	// process due to the possible unloaded Pixels. If we do not, 
    	// Pixels will be unloaded and we will hit 
    	// IllegalStateException's when checking update events.
    	iUpdate.saveArray(toSave.toArray(new Thumbnail[toSave.size()]));
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
        iUpdate.flush();
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
    	List<RenderingDef> settingsList = bulkLoadRenderingSettings(pixelsIds);
    	
    	// We've got a set of settings, lets populate our hash maps.
    	Map<Long, RenderingDef> settingsMap = new HashMap<Long, RenderingDef>();
    	Map<Long, Thumbnail> metadataMap = new HashMap<Long, Thumbnail>();
    	Map<Dimension, Set<Long>> dimensionPools =
    		new HashMap<Dimension, Set<Long>>();
    	Map<Long, Timestamp> settingsTimeMap = new HashMap<Long, Timestamp>();
    	Map<Long, Long> settingsUserMap = new HashMap<Long, Long>();
    	Map<Long, Pixels> pixelsMap = 
    		new HashMap<Long, Pixels>(pixelsIds.size());
    	for (RenderingDef settings : settingsList)
    	{
    	    Pixels pixels = settings.getPixels();
    	    Long pixelsId = pixels.getId();
    	    pixelsMap.put(pixelsId, pixels);
    	    Details details = settings.getDetails();
    	    Timestamp timestemp = details.getUpdateEvent().getTime();
    		settingsMap.put(pixelsId, settings);
    		settingsTimeMap.put(pixelsId, timestemp);
    		settingsUserMap.put(pixelsId, details.getOwner().getId());
    		addToDimensionPool(dimensionPools, pixels,
    		                   (int) checkedDimensions.getWidth());
    	}
    	
    	// For dimension pooling to work correctly for the purpose of thumbnail
    	// metadata creation we now need to load the Pixels sets that had no
    	// rendering settings.
    	Set<Long> pixelsIdsWithoutSettings = new HashSet<Long>();
    	Set<Long> pixelsIdsWithSettings = pixelsMap.keySet(); 
    	for (Long pixelsId : pixelsIds)
    	{
    		if (!pixelsIdsWithSettings.contains(pixelsId))
    		{
    			pixelsIdsWithoutSettings.add(pixelsId);
    		}
    	}

    	if (pixelsIdsWithoutSettings.size() > 0)
    	{
        	Parameters parameters = new Parameters();
        	parameters.addIds(pixelsIdsWithoutSettings);
        	List<Pixels> pixelsWithoutSettings = iQuery.findAllByQuery(
        			"select p from Pixels as p where id in (:ids)", parameters);
        	for (Pixels pixels : pixelsWithoutSettings)
        	{
        		pixelsMap.put(pixels.getId(), pixels);
        		addToDimensionPool(dimensionPools, pixels,
        				           (int) checkedDimensions.getWidth());
        	}
    	}

    	// Now we're going to attempt to efficiently retrieve the thumbnail
    	// metadata based on our dimension pools above. To save significant
    	// time later we're also going to pre-create thumbnail metadata where
    	// it is missing.
    	Map<Long, Timestamp> metadataTimeMap = new HashMap<Long, Timestamp>();
    	loadMetadataByDimensionPool(dimensionPools, metadataMap,
    			                    metadataTimeMap);
    	createMissingThumbnailMetadata(pixelsMap, dimensionPools, metadataMap,
    			                       metadataTimeMap,
    			                       (int) checkedDimensions.getWidth());
    	
    	// Loop through each pixels set that we've been requested to provide
    	// a thumbnail for, using the metadata that has already been retrieved
    	// if available. Thumbnail metadata will be lazily loaded if it is
    	// unavailable.
    	List<Thumbnail> toSave = new ArrayList<Thumbnail>();
    	for (Long pixelsId : pixelsIds)
    	{
    	    try
    	    {
    	        resetMetadata();
    	        if (settingsMap.containsKey(pixelsId))
    	        {
    	            pixels = settingsMap.get(pixelsId).getPixels();
    	            settings = settingsMap.get(pixelsId);
    	            settingsLastUpdated = settingsTimeMap.get(pixelsId);
    	            settingsUserId = settingsUserMap.get(pixelsId);
    	            if (metadataMap.containsKey(pixelsId))
    	            {
    	                metadata = metadataMap.get(pixelsId);
    	                metadataLastUpdated = metadataTimeMap.get(pixelsId);
    	            }
    	        }
    	        else
    	        {
    	        	// Prime a shallowly loaded Pixels object that we've
    	        	// already queried for.
    	        	pixels = pixelsMap.get(pixelsId);
    	            if (!setPixelsId(pixelsId))
    	            {
    	                _resetDefaults();
    	                setPixelsId(pixelsId);
    	            }
    	        }
    	        try
    	        {
    	        	byte[] thumbnail = _getThumbnailByLongestSide(size);
    	        	toReturn.put(pixelsId, thumbnail);
    	        	if (dirtyMetadata)
    	        	{
    	        		toSave.add(metadata);
    	        	}
    	        }
    	        finally
    	        {
    	        	dirtyMetadata = false;
    	        }
    	    }
    	    catch (Throwable t)
    	    {
    	        log.warn("Retrieving thumbnail in set for " +
    	                 "Pixels ID " + pixelsId + " failed.", t);
    	        toReturn.put(pixelsId, null);
    	    }
    	}
    	// We're doing the update or creation and save as a two step 
    	// process due to the possible unloaded Pixels. If we do not, 
    	// Pixels will be unloaded and we will hit 
    	// IllegalStateException's when checking update events.
    	iUpdate.saveArray(toSave.toArray(new Thumbnail[toSave.size()]));
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
        iUpdate.flush();
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
    	Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);
        // Ensure that we do not have "dirty" pixels or rendering settings 
        // left around in the Hibernate session cache.
        iQuery.clear();
        // Reloading thumbnail metadata because we don't know what may have
        // happened in the database since our last method call.
        metadata = getThumbnailMetadata(dimensions);
        if (metadata == null)
        {
        	metadata = createThumbnailMetadata(dimensions);
        	metadata = iUpdate.saveAndReturnObject(metadata);
    		metadataLastUpdated = 
    			metadata.getDetails().getUpdateEvent().getTime();
        }
        else
        {
        	metadataLastUpdated =
        		metadata.getDetails().getUpdateEvent().getTime();
        }
        byte[] thumbnail = _getThumbnail(sizeX, sizeY);
        if (dirtyMetadata)
        {
        	try
        	{
        		iUpdate.saveObject(metadata);
        	}
        	finally
        	{
        		dirtyMetadata = false;
        	}
        }
        return thumbnail; 
    }
    
    /** Actually does the work specified by {@link getThumbnail()}.*/
    private byte[] _getThumbnail(Integer sizeX, Integer sizeY) {
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

        if (metadata == null)
        {
            metadata = getThumbnailMetadata(dimensions);
            if (metadata != null)
            {
                metadataLastUpdated = 
                	metadata.getDetails().getUpdateEvent().getTime();
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
        
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
        // Resetting thumbnail metadata because we don't know what may have
        // happened in the database since or if sizeX and sizeY have changed.
    	metadata = getThumbnailMetadata(dimensions);
    	if (metadata == null)
    	{
    		metadata = createThumbnailMetadata(dimensions);
    		metadata = iUpdate.saveAndReturnObject(metadata);
    		metadataLastUpdated = 
    			metadata.getDetails().getUpdateEvent().getTime();
    	}
        else
        {
        	metadataLastUpdated =
        		metadata.getDetails().getUpdateEvent().getTime();
        }
    	byte[] thumbnail = _getThumbnailByLongestSide(size);
    	if (dirtyMetadata)
    	{
    		try
    		{
    			iUpdate.saveObject(metadata);
    		}
    		finally
    		{
    			dirtyMetadata = false;
    		}
    	}
    	return thumbnail;
    }
    
    /** Actually does the work specified by {@link _getThumbnailByLongestSide()}.*/
    private byte[] _getThumbnailByLongestSide(Integer size) {
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(size, size);
        dimensions = calculateXYWidths((int) dimensions.getWidth());
        byte[] thumbnail = 
        	_getThumbnail((int) dimensions.getWidth(),
                          (int) dimensions.getHeight());
    	return thumbnail;
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
    	_resetDefaults();
        iUpdate.flush();
    }
    
    /** Actually does the work specified by {@link resetDefaults()}.*/
    private void _resetDefaults()
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
    }

	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}
}
