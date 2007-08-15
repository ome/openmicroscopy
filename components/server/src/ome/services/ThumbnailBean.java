/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

// Java imports
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

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

import ome.api.ICompress;
import ome.api.IRepositoryInfo;
import ome.api.IScale;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.io.nio.ThumbnailService;
import ome.logic.AbstractLevel2Service;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import ome.util.ImageUtil;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

import sun.awt.image.IntegerInterleavedRaster;

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
@RemoteBinding(jndiBinding = "omero/remote/ome.api.ThumbnailStore")
@Local(ThumbnailStore.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ThumbnailStore")
@Interceptors( { OmeroAroundInvoke.class })
@SecurityDomain("OmeroSecurity")
public class ThumbnailBean extends AbstractLevel2Service implements
        ThumbnailStore, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3047482880497900069L;

    /** The logger for this class. */
    private transient static Log log = LogFactory.getLog(ThumbnailBean.class);

    /** The rendering engine that this service uses for thumbnail creation. */
    private transient RenderingEngine re;

    /** The scaling service that will be used to scale buffered images. */
    private transient IScale iScale;

    /** The ROMIO thumbnail service. */
    private transient ThumbnailService ioService;

    /** The disk space checking service. */
    private transient IRepositoryInfo iRepositoryInfo;
    
    /** The JPEG compression service. */
    private transient ICompress compressionService;

    /** is file service checking for disk overflow */
    private transient boolean diskSpaceChecking;

    /** The pixels instance that the service is currently working on. */
    private Pixels pixels;

    /** The id of the pixels instance. */
    private Long pixelsId;
    
    /** Only set after a passivated bean is activated. */
    private transient Long resetPix = null;

    /** Currently unused. */
    private transient Long resetRE = null;
    
    /** The id of the rendering definition instance. */
    private Long renderingDefId;

    /** The default X-width for a thumbnail. */
    public static final int DEFAULT_X_WIDTH = 48;

    /** The default Y-width for a thumbnail. */
    public static final int DEFAULT_Y_WIDTH = 48;

    /** The default compression quality in fractional percent. */
    public static final float DEFAULT_COMPRESSION_QUALITY = 0.85F;

    /** The default MIME type. */
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

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
    @PostActivate
    public void create() {
        selfConfigure();
        if (pixelsId != null) {
            resetPix = pixelsId;
            pixelsId = null;
        }
        // FIXME resetRE will need to be managed here too.
    }

    @PrePassivate
    @PreDestroy
    public void destroy() {
        // id is the only thing passivated.
   		re.close();
        re = null;
        iScale = null;
        ioService = null;
        pixels = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
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
    public boolean setPixelsId(long id) {
        if (pixelsId == null || pixelsId.longValue() != id) {
            pixelsId = new Long(id);
            resetPix = null;
            // FIXME resetRE will need to be managed here too.
            pixels = iQuery.get(Pixels.class, pixelsId);

            if (pixels == null) {
                throw new ApiUsageException(
                        "Unable to locate pixels set with ID: " + id);
            }

            re.lookupPixels(id);
            if (re.lookupRenderingDef(id) == false) {
            	pixelsId = null;
            	return false;
            }
            re.load();
        }
        return true;
    }

    @RolesAllowed("user")
    public void setRenderingDefId(Long id) {
        // FIXME: This currently is useless as the rendering engine does
        // not accept arbitrary rendering definitions.
        // FIXME resetRE will need to be managed here too.
        return;
    }

    /**
     * Rendering Engine Bean injector.
     * 
     * @param re
     *            a <code>RenderingEngine</code>.
     */
    public void setRenderingEngine(RenderingEngine re) {
        getBeanHelper().throwIfAlreadySet(this.re, re);
        this.re = re;
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
    public void setCompressionService(ICompress compressionService) {
        getBeanHelper().throwIfAlreadySet(this.compressionService,
                                          compressionService);
        this.compressionService = compressionService;
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
        Parameters param = new Parameters();
        param.addId(pixelsId);
        param.addInteger("x", sizeX);
        param.addInteger("y", sizeY);

        Thumbnail thumb = iQuery.findByQuery(
                "select t from Thumbnail as t where t.pixels.id = :id and "
                        + "t.sizeX = :x and t.sizeY = :y", param);
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
                "select t from Thumbnail as t where t.pixels.id = :id",
                new Parameters().addId(pixelsId));
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
    private Thumbnail createThumbnailMetadata(int sizeX, int sizeY) {
        Thumbnail thumb = new Thumbnail();
        thumb.setPixels(pixels);
        thumb.setMimeType(DEFAULT_MIME_TYPE);
        thumb.setSizeX(sizeX);
        thumb.setSizeY(sizeY);
        return iUpdate.saveAndReturnObject(thumb);
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
     * @return a scaled buffered image.
     */
    private BufferedImage createScaledImage(Integer sizeX, Integer sizeY) {
        // Original sizes and thumbnail metadata
        int origSizeX = pixels.getSizeX();
        int origSizeY = pixels.getSizeY();

        // Retrieve our rendered data and translate to a buffered image
        PlaneDef pd = new PlaneDef(PlaneDef.XY, re.getDefaultT());
        pd.setZ(re.getDefaultZ());
        int[] buf = re.renderAsPackedInt(pd);
        BufferedImage image = 
        	ImageUtil.createBufferedImage(buf, origSizeX, origSizeY);

        // Finally, scale our image using scaling factors (percentage).
        log.info("Setting xScale factor: " + sizeX + "/" + origSizeX);
        float xScale = (float) sizeX / origSizeX;
        log.info("Setting yScale factor: " + sizeX + "/" + origSizeX);
        float yScale = (float) sizeY / origSizeY;
        return iScale.scaleBufferedImage(image, xScale, yScale);
    }

    protected void errorIfInvalidState() {
        errorIfNullPixels();
        // FIXME resetRE will need to be managed here too.
    }

    protected void errorIfNullPixels() {
        if (resetPix != null) {
            long reset = resetPix.longValue();
            setPixelsId(reset);
        } else if (pixelsId == null) {
            throw new ApiUsageException(
                    "Thumbnail service not ready: Pixels object not set.");
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
    public void createThumbnail(Integer sizeX, Integer sizeY) {
        // Set defaults and sanity check thumbnail sizes
        errorIfInvalidState();
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);

        Thumbnail metadata = getThumbnailMetadata(sizeX, sizeY);
        if (metadata == null) {
            metadata = createThumbnailMetadata(sizeX, sizeY);
        }

        BufferedImage image = createScaledImage(sizeX, sizeY);
        try {
            compressThumbnailToDisk(metadata, image);
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
            createThumbnail(t.getSizeX(), t.getSizeY());
        }
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
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);

        try {
            Thumbnail metadata = getThumbnailMetadata(sizeX, sizeY);
            if (metadata == null) {
                // First create a scaled buffered image
                BufferedImage image = createScaledImage(sizeX, sizeY);

                // Now write it to the disk cache and return what we've written
                metadata = createThumbnailMetadata(sizeX, sizeY);
                compressThumbnailToDisk(metadata, image);
                return ioService.getThumbnail(metadata);
            }
            return ioService.getThumbnail(metadata);
        } catch (IOException e) {
            log.error("Could not obtain thumbnail metadata", e);
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

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.ThumbnailStore#getThumbnailDirect(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer,
     *      java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY) {
        // Set defaults and sanity check thumbnail sizes
        errorIfInvalidState();
        if (sizeX == null) {
            sizeX = DEFAULT_X_WIDTH;
        }
        if (sizeY == null) {
            sizeY = DEFAULT_Y_WIDTH;
        }
        sanityCheckThumbnailSizes(sizeX, sizeY);

        BufferedImage image = createScaledImage(sizeX, sizeY);
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
     * @see ome.api.ThumbnailStore#getThumbnailByLongestSideDirect(ome.model.core.Pixels,
     *      ome.model.display.RenderingDef, java.lang.Integer)
     */
    @RolesAllowed("user")
    public byte[] getThumbnailByLongestSideDirect(Integer size) {
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
            return getThumbnailDirect(size, (int) (sizeY * ratio));
        } else {
            float ratio = (float) size / sizeY;
            return getThumbnailDirect((int) (sizeX * ratio), size);
        }
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
        if (thumb == null) {
            return false;
        }
        return true;
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void resetDefaults() {
    	re.resetDefaults();
    }

	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}
}
