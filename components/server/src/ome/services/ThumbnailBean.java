/*
 *   Copyright 2006-2013 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import ome.conditions.ConcurrencyException;
import ome.conditions.InternalException;
import ome.conditions.ReadOnlyGroupSecurityViolation;
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
import ome.services.ThumbnailCtx.NoThumbnail;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import ome.util.ImageUtil;
import omeis.providers.re.Renderer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;
import omeis.providers.re.quantum.QuantumFactory;

import org.apache.batik.transcoder.TranscoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.springframework.core.io.Resource;
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
public class ThumbnailBean extends AbstractLevel2Service
    implements ThumbnailStore, Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 3047482880497900069L;

    /**
     * Version integer which will be used when a thumbnail is saved that is
     * currently having its pyramid generated, i.e. isInProgress.
     */
    private static final Integer PROGRESS_VERSION = -1;

    /** The logger for this class. */
    private transient static Logger log = LoggerFactory.getLogger(ThumbnailBean.class);

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

    /** ID of the pixels instance that the service is currently working on. */
    private Long pixelsId;

    /** In progress marker; set to true when no data is available the pixel */
    private boolean inProgress;

    /** The rendering settings that the service is currently working with. */
    private RenderingDef settings;

    /** The thumbnail metadata that the service is currently working with. */
    private Thumbnail thumbnailMetadata;

    /** The thumbnail metadata context. */
    private ThumbnailCtx ctx;

    /** The in-progress image resource we'll use for in progress images. */
    private Resource inProgressImageResource;

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
     * overridden to allow Spring to set boolean
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
        log.debug("Closing thumbnail bean");
        try {
            if (renderer != null) {
                renderer.close();
            }
            ctx = null;
            settings = null;
            pixels = null;
            thumbnailMetadata = null;
            renderer = null;
            iScale = null;
            ioService = null;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    @RolesAllowed("user")
    public long getRenderingDefId() {
        if (settings == null || settings.getId() == null) {
            throw new ApiUsageException("No rendering def");
        }
        return settings.getId();
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
        if ((pixels != null && pixels.getId() != id) || pixels == null)
        {
            newContext();
        }
        Set<Long> pixelsIds = new HashSet<Long>();
        pixelsIds.add(id);
        ctx.loadAndPrepareRenderingSettings(pixelsIds);
        pixels = ctx.getPixels(id);
        pixelsId = pixels.getId();
        settings = ctx.getSettings(id);
        return (ctx.hasSettings(id));
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.ThumbnailStore#isInProgress()
     */
    @RolesAllowed("user")
    public boolean isInProgress()
    {
        return inProgress;
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
        List<Family> families = getFamilies();
        List<RenderingModel> renderingModels = getRenderingModels();
        QuantumFactory quantumFactory = new QuantumFactory(families);
        // Loading last to try to ensure that the buffer will get closed.
        PixelBuffer buffer = pixelDataService.getPixelBuffer(pixels, false);
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
        errorIfNullPixels();
        ctx.loadAndPrepareRenderingSettings(pixelsId, id);
        settings = ctx.getSettings(pixelsId);
        // Handle cases where this new settings is not owned by us so that
        // retrieval of thumbnail metadata is done based on the owner of the
        // settings not the owner of the session. (#2274 Part I)
        ctx.setUserId(settings.getDetails().getOwner().getId());
    }

    /**
     * In-progress image resource Bean injector.
     * @param inProgressImageResource The in-progress image resource we'll be
     * using for in progress images.
     */
    public void setInProgressImageResource(Resource inProgressImageResource) {
        getBeanHelper().throwIfAlreadySet(
                this.inProgressImageResource, inProgressImageResource);
        this.inProgressImageResource = inProgressImageResource;
    }

    /**
     * Pixels data service Bean injector.
     *
     * @param pixelDataService
     *            a <code>PixelsService</code>.
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
        try {
            if (inProgress) {
                compressInProgressImageToStream(thumb, stream);
            } else {
                compressionService.compressToStream(image, stream);
            }
        } finally {
            stream.close();
        }
    }

    /**
     * Compresses the <i>in progress</i> image to a stream.
     * @param thumb The thumbnail metadata.
     * @param outputStream Stream to compress the data to.
     */
    private void compressInProgressImageToStream(
            Thumbnail thumb, OutputStream outputStream) {
        int x = thumb.getSizeX();
        int y = thumb.getSizeY();
        StopWatch s1 = new Slf4JStopWatch("omero.transcodeSVG");
        try
        {
            SVGRasterizer rasterizer = new SVGRasterizer(
                    inProgressImageResource.getInputStream());
            // Batik will automatically maintain the aspect ratio of the
            // resulting image if we only specify the width or height.
            if (x > y)
            {
                rasterizer.setImageWidth(x);
            }
            else
            {
                rasterizer.setImageHeight(y);
            }
            rasterizer.setQuality(compressionService.getCompressionLevel());
            rasterizer.createJPEG(outputStream);
            s1.stop();
        }
        catch (IOException e1)
        {
            String s = "Error loading in-progress image from Spring resource.";
            log.error(s, e1);
            throw new ResourceError(s);
        }
        catch (TranscoderException e2)
        {
            String s = "Error transcoding in progress SVG.";
            log.error(s, e2);
            throw new ResourceError(s);
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
     * @param theZ the optical section (offset across the Z-axis) requested.
     * <pre>null</pre> signifies the rendering engine default.
     * @param theT the timepoint (offset across the T-axis) requested.
     * <pre>null</pre> signifies the rendering engine default.
     * @return a scaled buffered image.
     */
    private BufferedImage createScaledImage(Integer theZ, Integer theT)
    {
        // Ensure that we have a valid state for rendering
        errorIfInvalidState();

        if (inProgress)
        {
            return null;
        }

        // Retrieve our rendered data
        if (theZ == null)
            theZ = settings.getDefaultZ();
        if (theT == null)
            theT = settings.getDefaultT();
        PlaneDef pd = new PlaneDef(PlaneDef.XY, theT);
        pd.setZ(theZ);
        // Use a resolution level that matches our requested size if we can
        PixelBuffer pixelBuffer = renderer.getPixels();
        int originalSizeX = pixels.getSizeX();
        int originalSizeY = pixels.getSizeY();
        int pixelBufferSizeX = pixelBuffer.getSizeX();
        int pixelBufferSizeY = pixelBuffer.getSizeY();
        if (pixelBuffer.getResolutionLevels() > 1)
        {
            int resolutionLevel = pixelBuffer.getResolutionLevels();
            while (resolutionLevel > 0)
            {
                resolutionLevel--;
                renderer.setResolutionLevel(resolutionLevel);
                pixelBufferSizeX = pixelBuffer.getSizeX();
                pixelBufferSizeY = pixelBuffer.getSizeY();
                if (pixelBufferSizeX <= thumbnailMetadata.getSizeX()
                    || pixelBufferSizeY <= thumbnailMetadata.getSizeY())
                {
                    break;
                }
            }
            log.debug(String.format("Using resolution level %d -- %dx%d",
                    resolutionLevel, pixelBufferSizeX, pixelBufferSizeY));
            renderer.setResolutionLevel(resolutionLevel);
        }

        // Render the planes and translate to a buffered image
        Pixels rendererPixels = renderer.getMetadata();
        try
        {
            log.debug(String.format("Setting renderer Pixel sizeX:%d sizeY:%d",
                    pixelBufferSizeX, pixelBufferSizeY));
            rendererPixels.setSizeX(pixelBufferSizeX);
            rendererPixels.setSizeY(pixelBufferSizeY);
            int[] buf = renderer.renderAsPackedInt(pd, null);
            BufferedImage image = ImageUtil.createBufferedImage(
                    buf, pixelBufferSizeX, pixelBufferSizeY);

            // Finally, scale our image using scaling factors (percentage).
            float xScale = (float)
                    thumbnailMetadata.getSizeX() / pixelBufferSizeX;
            float yScale = (float)
                    thumbnailMetadata.getSizeY() / pixelBufferSizeY;
            log.debug(String.format("Using scaling factors x:%f y:%f",
                    xScale, yScale));
            return iScale.scaleBufferedImage(image, xScale, yScale);
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
        finally
        {
            // Reset to our original dimensions (#5075)
            log.debug(String.format(
                    "Setting original renderer Pixel sizeX:%d sizeY:%d",
                    originalSizeX, originalSizeY));
            rendererPixels.setSizeX(originalSizeX);
            rendererPixels.setSizeY(originalSizeY);
        }

    }

    /**
     * Creates a new thumbnail context.
     */
    private void newContext()
    {
        resetMetadata();
        ctx = new ThumbnailCtx(
                iQuery, iUpdate, iPixels, settingsService, ioService,
                sec, sec.getEffectiveUID());
    }

    /**
     * Resets the current metadata state.
     */
    private void resetMetadata()
    {
        inProgress = false;
        pixels = null;
        pixelsId = null;
        settings = null;
        dirty = true;
        dirtyMetadata = false;
        thumbnailMetadata = null;
        // Be as explicit as possible when closing the renderer to try and
        // avoid re-use where we don't want it. (#2075 and #2274 Part II)
        if (renderer != null)
        {
            renderer.close();
        }
        renderer = null;
    }

    protected void errorIfInvalidState()
    {
        errorIfNullPixelsAndRenderingDef();
        if (inProgress)
        {
            return; // No-op #5191
        }
        if ((renderer == null && wasPassivated) || dirty)
        {
            try
            {
                load();
            }
            catch (ConcurrencyException e)
            {
                inProgress = true;
                log.info("ConcurrencyException on load()");
            }
        }
        else if (renderer == null)
        {
            throw new InternalException(
            "Thumbnail service state corruption: Renderer missing.");
        }
    }

    protected void errorIfNullPixelsAndRenderingDef()
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
        errorIfNullPixels();
        if (inProgress)
        {
            // pass. Do nothing.
        }
        else if (settings == null &&
            ctx.isExtendedGraphCritical(Collections.singleton(pixelsId)))
        {
            long ownerId = pixels.getDetails().getOwner().getId();
            throw new ResourceError(String.format(
                    "The owner id:%d has not viewed the Pixels set id:%d, " +
                    "rendering settings are missing.", ownerId, pixelsId));
        }
        else if (settings == null)
        {
            throw new ome.conditions.InternalException(
                    "Fatal error retrieving rendering settings or settings " +
                    "not loaded for Pixels set id:" + pixelsId);
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
        if (inProgress)
        {
            return;
        }

        try
        {
            // Set defaults and sanity check thumbnail sizes
            if (sizeX == null) {
                sizeX = DEFAULT_X_WIDTH;
            }
            if (sizeY == null) {
                sizeY = DEFAULT_Y_WIDTH;
            }
            Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);
            Set<Long> pixelsIds = new HashSet<Long>();
            pixelsIds.add(pixelsId);
            ctx.loadAndPrepareMetadata(pixelsIds, dimensions);
            try {
                thumbnailMetadata = ctx.getMetadata(pixels.getId());
            } catch (NoThumbnail e) {
                // See #10618
                // Since the creation of the thumbnail was explicitly requested,
                // we'll throw an exception instead.
                throw new ValidationException(e.getMessage());
            }
            thumbnailMetadata = _createThumbnail();
            if (dirtyMetadata)
            {
                thumbnailMetadata = iUpdate.saveAndReturnObject(thumbnailMetadata);
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
    private Thumbnail _createThumbnail() {
        StopWatch s1 = new Slf4JStopWatch("omero._createThumbnail");
        if (thumbnailMetadata == null) {
            throw new ValidationException("Missing thumbnail metadata.");
        } else if (ctx.dirtyMetadata(pixels.getId())) {
            // Increment the version of the thumbnail so that its
            // update event has a timestamp equal to or after that of
            // the rendering settings. FIXME: This should be
            // implemented using IUpdate.touch() or similar once that
            // functionality exists.
            
            //Check first if the thumbnail is the one of the settings owner
            Long ownerId = thumbnailMetadata.getDetails().getOwner().getId();
            Long rndOwnerId = settings.getDetails().getOwner().getId();
            if (rndOwnerId.equals(ownerId)) {
                Pixels unloadedPixels = new Pixels(pixels.getId(), false);
                thumbnailMetadata.setPixels(unloadedPixels);
                _setMetadataVersion(thumbnailMetadata, inProgress);
                dirtyMetadata = true;
            } else {
                //new one for owner of the settings.
                Dimension d = new Dimension(thumbnailMetadata.getSizeX(),
                        thumbnailMetadata.getSizeY());
                thumbnailMetadata = ctx.createThumbnailMetadata(pixels, d);
                _setMetadataVersion(thumbnailMetadata, inProgress);
                thumbnailMetadata = iUpdate.saveAndReturnObject(thumbnailMetadata);
                dirtyMetadata = false;
            }
        }
        // dirtyMetadata is left false here because we may be creating a
        // thumbnail for the first time and the Thumbnail object has just been
        // created upstream of us.

        BufferedImage image = createScaledImage(null, null);
        try {
            compressThumbnailToDisk(thumbnailMetadata, image);
            s1.stop();
            return thumbnailMetadata;
        } catch (IOException e) {
            log.error("Thumbnail could not be compressed.", e);
            throw new ResourceError(e.getMessage());
        }
    }

    private static void _setMetadataVersion(Thumbnail tb, boolean inProgress) {
        Integer version = tb.getVersion();
        if (version == null) {
            version = inProgress ? PROGRESS_VERSION : 0;
        } else {
            version++;
        }
        tb.setVersion(version);
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
            List<Thumbnail> thumbnails = ctx.loadAllMetadata(pixelsId);
            for (Thumbnail thumbnail : thumbnails) {
                thumbnailMetadata = thumbnail;
                _createThumbnail();
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
        // Set defaults and sanity check thumbnail sizes
        Dimension checkedDimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

        // Prepare our thumbnail context
        newContext();
        ctx.loadAndPrepareRenderingSettings(pixelsIds);
        ctx.createAndPrepareMissingRenderingSettings(pixelsIds);
        ctx.loadAndPrepareMetadata(pixelsIds, checkedDimensions);
        Map<Long, byte[]> values = retrieveThumbnailSet(pixelsIds);
        iQuery.clear();
        return values;
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public Map<Long, byte[]> getThumbnailByLongestSideSet(Integer size,
            Set<Long> pixelsIds)
    {
        // Set defaults and sanity check thumbnail sizes
        Dimension checkedDimensions = sanityCheckThumbnailSizes(size, size);
        size = (int) checkedDimensions.getWidth();

        // Prepare our thumbnail context
        newContext();
        ctx.loadAndPrepareRenderingSettings(pixelsIds);
        ctx.createAndPrepareMissingRenderingSettings(pixelsIds);
        ctx.loadAndPrepareMetadata(pixelsIds, size);
        Map<Long, byte[]> values = retrieveThumbnailSet(pixelsIds);
        iQuery.clear();
        return values;
    }

    /**
     * Performs the logic of retrieving a set of thumbnails.
     * @param pixelsIds The Pixels IDs to retrieve thumbnails for.
     * @return Map of Pixels ID vs. thumbnail bytes.
     */
    private Map<Long, byte[]> retrieveThumbnailSet(Set<Long> pixelsIds)
    {
        // Our return value HashMap
        Map<Long, byte[]> toReturn = new HashMap<Long, byte[]>();

        List<Thumbnail> toSave = new ArrayList<Thumbnail>();
        for (Long pixelsId : pixelsIds)
        {
            // Ensure that the renderer has been made dirty otherwise the
            // same renderer will be used to return all thumbnails with dirty
            // metadata. (See #2075).
            resetMetadata();
            try
            {
                if (!ctx.hasSettings(pixelsId))
                {
                    try
                    {
                        pixelDataService.getPixelBuffer(
                                ctx.getPixels(pixelsId), false);
                        continue;  // No exception, not an in progress image
                    }
                    catch (ConcurrencyException e)
                    {
                        log.debug("ConcurrencyException on " +
                                 "retrieveThumbnailSet.ctx.hasSettings: " +
                                 "pyramid in progress");
                        inProgress = true;
                    }
                }
                pixels = ctx.getPixels(pixelsId);
                pixelsId = pixels.getId();
                settings = ctx.getSettings(pixelsId);
                thumbnailMetadata = ctx.getMetadata(pixelsId);
                if (!PROGRESS_VERSION.equals(thumbnailMetadata.getVersion())) {
                    thumbnailMetadata.setVersion(PROGRESS_VERSION);
                    dirtyMetadata = true;
                }
                try
                {
                    // At this point, we're sure that we have a thumbnail obj
                    // that we want to use, but retrieveThumbnail likes to
                    // re-generate. For the moment, we're saving and restoring
                    // that value to prevent creating a new one.
                    byte[] thumbnail = retrieveThumbnail(false);
                    toReturn.put(pixelsId, thumbnail);
                    if (dirtyMetadata)
                    {
                        toSave.add(thumbnailMetadata);
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
        errorIfNullPixelsAndRenderingDef();
        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);
        // Reloading thumbnail metadata because we don't know what may have
        // happened in the database since our last method call.
        Set<Long> pixelsIds = new HashSet<Long>();
        pixelsIds.add(pixelsId);
        byte[] value = null;
        try {
            ctx.loadAndPrepareMetadata(pixelsIds, dimensions);
            thumbnailMetadata = ctx.getMetadata(pixelsId);
            value = retrieveThumbnailAndUpdateMetadata(false);
        } catch (Throwable t) {
            value = handleNoThumbnail(t, dimensions);
        }
        iQuery.clear();//see #11072
        return value;
    }

    /**
     * Creates the thumbnail or retrieves it from cache and updates the
     * thumbnail metadata.
     * @return Thumbnail bytes.
     */
    private byte[] retrieveThumbnailAndUpdateMetadata(boolean rewriteMetadata)
    {
        byte[] thumbnail = retrieveThumbnail(rewriteMetadata);
        if (inProgress && !PROGRESS_VERSION.equals(thumbnailMetadata)) {
            thumbnailMetadata.setVersion(PROGRESS_VERSION);
            dirtyMetadata = true;
        }
        if (dirtyMetadata)
        {
            try
            {
                iUpdate.saveObject(thumbnailMetadata);
            }
            finally
            {
                dirtyMetadata = false;
            }
        }
        return thumbnail;
    }

    /**
     * Creates the thumbnail or retrieves it from cache.
     * @return Thumbnail bytes.
     */
    private byte[] retrieveThumbnail(boolean rewriteMetadata)
    {
        if (inProgress)
        {
            return retrieveThumbnailDirect(
                    thumbnailMetadata.getSizeX(),
                    thumbnailMetadata.getSizeY(),
                    0, 0, rewriteMetadata);
        }

        try
        {
            boolean cached = ctx.isThumbnailCached(pixels.getId());
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
                _createThumbnail();
            }
            byte[] thumbnail = ioService.getThumbnail(thumbnailMetadata);
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
        errorIfNullPixelsAndRenderingDef();
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(size, size);
        size = (int) dimensions.getWidth();
        // Resetting thumbnail metadata because we don't know what may have
        // happened in the database since or if sizeX and sizeY have changed.
        Set<Long> pixelsIds = new HashSet<Long>();
        pixelsIds.add(pixelsId);
        byte[] value = null;
        try {
            ctx.loadAndPrepareMetadata(pixelsIds, size);
            thumbnailMetadata = ctx.getMetadata(pixelsId);
            value = retrieveThumbnailAndUpdateMetadata(false);
        } catch (Throwable t) {
            value = handleNoThumbnail(t, dimensions);
        }
        iQuery.clear();//see #11072
        return value;
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
        // Leaving rewriteMetadata here true since it's unclear of what the
        // expected state of the bean should be.
        byte[] value = retrieveThumbnailDirect(sizeX, sizeY, null, null, true);
    	// Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
    	iQuery.clear();
        return value;
    }

    /**
     * Retrieves a thumbnail directly, not inspecting or interacting with the
     * thumbnail cache.
     * @param sizeX Width of the thumbnail.
     * @param sizeY Height of the thumbnail.
     * @param theZ Optical section to retrieve a thumbnail for.
     * @param theT Timepoint to retrieve a thumbnail for.
     * @return
     */
    private byte[] retrieveThumbnailDirect(Integer sizeX, Integer sizeY,
            Integer theZ, Integer theT, boolean rewriteMetadata)
    {
        errorIfNullPixelsAndRenderingDef();
        // Set defaults and sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);
        Thumbnail local = ctx.createThumbnailMetadata(pixels, dimensions);
        if (rewriteMetadata) {
            thumbnailMetadata = local;
        }

        BufferedImage image = createScaledImage(theZ, theT);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            if (inProgress) {
                compressInProgressImageToStream(local, byteStream);
            } else {
                compressionService.compressToStream(image, byteStream);
            }
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
        // As getThumbnailDirect
        byte[] value = retrieveThumbnailDirect(sizeX, sizeY, theZ, theT, true);
     // Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
        iQuery.clear();
        return value;
    }

    /** Actually does the work specified by {@link getThumbnailByLongestSideDirect()}.*/
    private byte[] _getThumbnailByLongestSideDirect(Integer size, Integer theZ,
            Integer theT, boolean rewriteMetadata)
    {
        // Sanity check thumbnail sizes
        Dimension dimensions = sanityCheckThumbnailSizes(size, size);

        dimensions = ctx.calculateXYWidths(pixels, (int) dimensions.getWidth());
        byte[] value = retrieveThumbnailDirect((int) dimensions.getWidth(),
                (int) dimensions.getHeight(), theZ, theT, rewriteMetadata);
        iQuery.clear();
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSideDirect(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailByLongestSideDirect(Integer size) {
        errorIfNullPixelsAndRenderingDef();
        // As getThumbnailDirect
        byte[] value = _getThumbnailByLongestSideDirect(size, null, null, true);
        // Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
        iQuery.clear();//see #11072
        return value;
    }

    /* (non-Javadoc)
     * @see ome.api.ThumbnailStore#getThumbnailForSectionByLongestSideDirect(int, int, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailForSectionByLongestSideDirect(int theZ, int theT,
            Integer size)
    {
        errorIfNullPixelsAndRenderingDef();
        // Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
        iQuery.clear();
        // As getThumbnailDirect
        byte[] value = _getThumbnailByLongestSideDirect(size, theZ, theT, true);
        // Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
        iQuery.clear();
        return value;
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
        errorIfNullPixelsAndRenderingDef();
        if (inProgress)
        {
            return false;
        }

        Dimension dimensions = sanityCheckThumbnailSizes(sizeX, sizeY);

        Set<Long> pixelsIds = new HashSet<Long>();
        pixelsIds.add(pixelsId);
        ctx.loadAndPrepareMetadata(pixelsIds, dimensions, false);
        // Ensure that we do not have "dirty" pixels or rendering settings
        // left around in the Hibernate session cache.
        iQuery.clear();
        return ctx.isThumbnailCached(pixelsId);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void resetDefaults()
    {
        if (settings == null
            && ctx.isExtendedGraphCritical(Collections.singleton(pixelsId)))
        {
            throw new ApiUsageException(
                    "Unable to reset rendering settings in a read-only group " +
                    "for Pixels set id:" + pixelsId);
        }
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
        params.addLong("o_id", sec.getEffectiveUID());
        if (settings != null
            || iQuery.findByQuery(
                    "from RenderingDef as r where r.pixels.id = :id and " +
                    "r.details.owner.id = :o_id", params) != null)
        {
            throw new ApiUsageException(
                    "The thumbnail service only resets **empty** rendering " +
                    "settings. Resetting of existing settings should either " +
                    "be performed using the RenderingEngine or " +
                    "IRenderingSettings.");
        }

        RenderingDef def = settingsService.createNewRenderingDef(pixels);
        try
        {
            settingsService.resetDefaults(def, pixels);
        }
        catch (ConcurrencyException mpe)
        {
            inProgress = true;
            log.info("ConcurrencyException on settingsSerice.resetDefaults");
        }
    }

    public boolean isDiskSpaceChecking() {
        return diskSpaceChecking;
    }

    public void setDiskSpaceChecking(boolean diskSpaceChecking) {
        this.diskSpaceChecking = diskSpaceChecking;
    }

    /**
     * If a known exception is thrown, then fallback to using a direct
     * thumbnail generation method. Otherwise, re-throw the exception,
     * wrapping it as necessary.
     */
    private byte[] handleNoThumbnail(Throwable t, Dimension dimensions) {
        if (t instanceof NoThumbnail ||
                t instanceof ReadOnlyGroupSecurityViolation) {
            log.debug("Calling retrieveThumbnailDirect on missing thumbnail");
            // As getThumbnailDirect
            return retrieveThumbnailDirect((int) dimensions.getWidth(),
                (int) dimensions.getHeight(), null, null, true);
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            // This is unexpected. The only checked exception that
            // should be throwable by the invoking methods should be
            // NoThumbnail.
            InternalException ie = new InternalException("No thumbnail available!");
            ie.initCause(t);
            throw ie;
        }
    }
}
