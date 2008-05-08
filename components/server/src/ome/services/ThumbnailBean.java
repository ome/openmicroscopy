/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

// Java imports
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
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

    /** is file service checking for disk overflow */
    private transient boolean diskSpaceChecking;

    /** The pixels instance that the service is currently working on. */
    private Pixels pixels;
    
    /** The rendering settings that the service is currently working with. */
    private RenderingDef settings;

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
    		pixels = null;
    		settings = null;
    	}

    	// Handle lookup of pixels object
    	if (pixels == null)
    	{
    		pixels = iPixels.retrievePixDescription(id);
    		if (pixels == null)
    		{
    			throw new ValidationException(
    					"Pixels object with id '" + id + "' not found.");
    		}
    	}

    	// Handle lookup of rendering settings
   		settings = iPixels.retrieveRndSettings(id);
   		if (settings == null)
   		{
   			return false;
   		}
    	
    	// Rebuild the renderer.
    	load();
    	return true;
    }
    
    /**
     * Creates a renderer for the active pixels set and rendering settings.
     */
    private void load()
    {
    	errorIfNullMetadata();
    	if (renderer != null)
    	{
    		renderer.close();
    	}
    	PixelBuffer buffer = pixelDataService.getPixelBuffer(pixels);
    	List<Family> families =
    		iPixels.getAllEnumerations(Family.class);
    	List<RenderingModel> renderingModels =
    		iPixels.getAllEnumerations(RenderingModel.class);
    	QuantumFactory quantumFactory = new QuantumFactory(families);
    	renderer = new Renderer(quantumFactory, renderingModels, pixels,
    	                        settings, buffer);
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
        load();
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
     * Retrieves metadata for a thumbnail of the active pixels set and X-Y
     * dimensions.
     * 
     * @param sizeX
     *            the X-width of the thumbnail.
     * @param sizeY
     *            the Y-width of the thumbnail.
     * @return the thumbnail metadata. <code>null</code> if the object does
     *         not exist.
     */
    private Thumbnail getThumbnailMetadata(int sizeX, int sizeY) {
        Long userId = getSecuritySystem().getEventContext().getCurrentUserId();
        Parameters param = new Parameters();
        param.addId(pixels.getId());
        param.addInteger("x", sizeX);
        param.addInteger("y", sizeY);
        param.addLong("ownerid", userId);

        Thumbnail thumb = iQuery.findByQuery(
        	"select t from Thumbnail as t join fetch t.details.updateEvent " +
        	"where t.pixels.id = :id and t.sizeX = :x and t.sizeY = :y and " +
        	"t.details.owner.id = :ownerid", param);
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
        Long userId = getSecuritySystem().getEventContext().getCurrentUserId();
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
     * @param sizeX
     *            the X-width of the thumbnail.
     * @param sizeY
     *            the Y-width of the thumbnail.
     * @return the thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(int sizeX, int sizeY)
    {
        // Unload the pixels object to avoid transactional headaches
        Pixels unloadedPixels = new Pixels(pixels.getId(), false);

        Thumbnail thumb = new Thumbnail();
        thumb.setPixels(unloadedPixels);
        thumb.setMimeType(DEFAULT_MIME_TYPE);
        thumb.setSizeX(sizeX);
        thumb.setSizeY(sizeY);
        return thumb;
    }

    /**
     * Checks that sizeX and sizeY are not out of range for the active pixels
     * set.
     * 
     * @param sizeX
     *            the X-width for the requested thumbnail.
     * @param sizeY
     *            the Y-width for the requested thumbnail.
     */
    private void sanityCheckThumbnailSizes(int sizeX, int sizeY) {
        // Sanity checks
        if (sizeX < 0) {
            throw new ApiUsageException("sizeX is negative");
        }
        if (sizeY < 0) {
            throw new ApiUsageException("sizeY is negative");
        }
    }

    /**
     * Creates a scaled buffered image from the active pixels set.
     * 
     * @param def
     *            the rendering settings to use for buffered image creation.
     * @param sizeX
     *            the X-width of the requested, scaled image.
     * @param sizeY
     *            the Y-width of the requested, scaled image.
     * @param theZ the optical section (offset across the Z-axis) requested. 
     * <pre>null</pre> signifies the rendering engine default.
     * @param theT the timepoint (offset across the T-axis) requested. 
     * <pre>null</pre> signifies the rendering engine default.
     * @return a scaled buffered image.
     */
    private BufferedImage createScaledImage(Integer sizeX, Integer sizeY,
                                            Integer theZ, Integer theT)
    {
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
    		int[] buf = renderer.renderAsPackedInt(pd);
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
    	log.info("Setting xScale factor: " + sizeX + "/" + origSizeX);
    	float xScale = (float) sizeX / origSizeX;
    	log.info("Setting yScale factor: " + sizeX + "/" + origSizeX);
    	float yScale = (float) sizeY / origSizeY;
    	return iScale.scaleBufferedImage(image, xScale, yScale);
    }
    
    protected void errorIfInvalidState()
    {
    	errorIfNullMetadata();
    	if (renderer == null && wasPassivated)
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
        errorIfInvalidState();
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);
    	_createThumbnail(sizeX, sizeY);
        
    	// Ensure that we do not have "dirty" pixels or rendering settings left
    	// around in the Hibernate session cache.
    	iQuery.clear();
    }
    
    /** Actually does the work specified by {@link createThumbnail()}.*/
    private Thumbnail _createThumbnail(Integer sizeX, Integer sizeY) {
        Thumbnail metadata = getThumbnailMetadata(sizeX, sizeY);
        if (metadata == null) {
            metadata = createThumbnailMetadata(sizeX, sizeY);
        } else {
            // Increment the version of the thumbnail so that its
            // update event has a timestamp equal to or after that of
            // the rendering settings. FIXME: This should be 
            // implemented using IUpdate.touch() or similar once that 
            // functionality exists.
            metadata.setVersion(metadata.getVersion() + 1);
        }
        iUpdate.saveAndReturnObject(metadata);
        
        BufferedImage image = createScaledImage(sizeX, sizeY, null, null);
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
            _createThumbnail(t.getSizeX(), t.getSizeY());
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
    	Map<Long, byte[]> toReturn = new HashMap<Long, byte[]>();
    	for (Long pixelsId : pixelsIds)
    	{
    	    try
    	    {
    	        if (!setPixelsId(pixelsId))
    	        {
    	            resetDefaults();
    	            setPixelsId(pixelsId);
    	        }
    	        byte[] thumbnail = getThumbnail(sizeX, sizeY);
    	        toReturn.put(pixelsId, thumbnail);
    	    }
            catch (Throwable t)
            {
                log.warn("WARNING: Retrieving thumbnail in set for Pixels ID "
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
    	Map<Long, byte[]> toReturn = new HashMap<Long, byte[]>();
    	for (Long pixelsId : pixelsIds)
    	{
    	    try
    	    {
    	        if (!setPixelsId(pixelsId))
    	        {
    	            resetDefaults();
    	            setPixelsId(pixelsId);
    	        }
    	        byte[] thumbnail = getThumbnailByLongestSide(size);
    	        toReturn.put(pixelsId, thumbnail);
    	    }
    	    catch (Throwable t)
    	    {
    	        log.warn("WARNING: Retrieving thumbnail in set for Pixels ID "
    	                 + pixelsId + " failed.", t);
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
        errorIfInvalidState();
        if (sizeX == null)
        {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null)
        {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);

        try
        {
            Thumbnail metadata = getThumbnailMetadata(sizeX, sizeY);
        	Timestamp thumbTime = metadata != null?
        		metadata.getDetails().getUpdateEvent().getTime() : null;
        	Timestamp settingsTime = 
        		settings.getDetails().getUpdateEvent().getTime();
        	log.info("Thumb time: " + thumbTime);
        	log.info("Settings time: " + settingsTime);
            if (metadata == null
            	|| (thumbTime != null && settingsTime.after(thumbTime)))
            {
            	log.info("Cache miss, thumbnail missing or out of date.");
            	metadata = _createThumbnail(sizeX, sizeY);
            }
            else
            {
            	log.info("Cache hit.");
            }
            // Ensure that we do not have "dirty" pixels or rendering settings 
            // left around in the Hibernate session cache.
            iQuery.clear();

            return ioService.getThumbnail(metadata);
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
        errorIfInvalidState();
        if (size == null) {
            size = DEFAULT_X_WIDTH;
        }
        sanityCheckThumbnailSizes(size, size);

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        if (sizeX > sizeY) {
            float ratio = (float) size / sizeX;
            return getThumbnail(size, (int) (sizeY * ratio));
        } else {
            float ratio = (float) size / sizeY;
            return getThumbnail((int) (sizeX * ratio), size);
        }
    }
    
    /** Actually does the work specified by {@link getThumbnailDirect()}.*/
    private byte[] _getThumbnailDirect(Integer sizeX, Integer sizeY,
                                       Integer theZ, Integer theT)
    {
    	// Set defaults and sanity check thumbnail sizes
    	errorIfInvalidState();
    	if (sizeX == null) {
    		sizeX = DEFAULT_X_WIDTH;
    	}
    	if (sizeY == null) {
    		sizeY = DEFAULT_Y_WIDTH;
    	}
    	sanityCheckThumbnailSizes(sizeX, sizeY);

    	BufferedImage image = createScaledImage(sizeX, sizeY, theZ, theT);
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
        // Set defaults and sanity check thumbnail sizes
        errorIfInvalidState();
        if (size == null) {
            size = DEFAULT_X_WIDTH;
        }
        sanityCheckThumbnailSizes(size, size);

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        if (sizeX > sizeY) {
            float ratio = (float) size / sizeX;
            return _getThumbnailDirect(size, (int) (sizeY * ratio), theZ, theT);
        } else {
            float ratio = (float) size / sizeY;
            return _getThumbnailDirect((int) (sizeX * ratio), size, theZ, theT);
        }
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
        errorIfInvalidState();
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);

        Thumbnail thumb = getThumbnailMetadata(sizeX, sizeY);
        // Ensure that we do not have "dirty" pixels or rendering settings 
        // left around in the Hibernate session cache.
        iQuery.clear();
        if (thumb == null) {
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
        if (settings != null
            || iPixels.retrieveRndSettings(pixels.getId()) != null)
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
