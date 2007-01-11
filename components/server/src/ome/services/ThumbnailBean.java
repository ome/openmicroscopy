/*
 * ome.logic.QueryImpl
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
import java.io.OutputStream;
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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
// Third-party libraries
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import ome.api.IScale;
import ome.api.ThumbnailStore;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.io.nio.ThumbnailService;
import ome.logic.AbstractLevel2Service;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.parameters.Parameters;
import ome.system.EventContext;
import ome.system.SimpleEventContext;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;
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

    /** The pixels instance that the service is currently working on. */
    private Pixels pixels;

    /** The id of the pixels instance. */
    private Long pixelsId;

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

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#getServiceInterface()
     */
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface() {
        return ThumbnailStore.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#create()
     */
    @Override
    @PostConstruct
    @PostActivate
    public void create() {
        super.create();
        if (pixelsId != null) {
            long reset = pixelsId.longValue();
            pixelsId = null;
            setPixelsId(reset);
        }

        if (renderingDefId != null) {
            long reset = renderingDefId.longValue();
            renderingDefId = null;
            setPixelsId(reset);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#destroy()
     */
    @Override
    @PrePassivate
    @PreDestroy
    public void destroy() {
        super.destroy();
        // id is the only thing passivated.
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
    public void setPixelsId(long id) {
        if (pixelsId == null || pixelsId.longValue() != id) {
            pixelsId = new Long(id);
            pixels = iQuery.get(Pixels.class, pixelsId);

            if (pixels == null) {
                throw new ApiUsageException(
                        "Unable to locate pixels set with ID: " + id);
            }

            re.lookupPixels(id);
            re.lookupRenderingDef(id);
            re.load();
        }
    }

    @RolesAllowed("user")
    public void setRenderingDefId(Long id) {
        // FIXME: This currently is useless as the rendering engine does
        // not accept arbitrary rendering definitions.
        return;
    }

    /**
     * Rendering Engine Bean injector.
     * 
     * @param re
     *            a <code>RenderingEngine</code>.
     */
    public void setRenderingEngine(RenderingEngine re) {
        throwIfAlreadySet(this.re, re);
        this.re = re;
    }

    /**
     * Scale service Bean injector.
     * 
     * @param iScale
     *            an <code>IScale</code>.
     */
    public void setScaleService(IScale iScale) {
        throwIfAlreadySet(this.iScale, iScale);
        this.iScale = iScale;
    }

    /**
     * I/O service (ThumbnailService) Bean injector.
     * 
     * @param ioService
     *            a <code>ThumbnailService</code>.
     */
    public void setIoService(ThumbnailService ioService) {
        throwIfAlreadySet(this.ioService, ioService);
        this.ioService = ioService;
    }

    /**
     * Creates a buffered image from a rendering engine RGB buffer without data
     * copying.
     * 
     * @param buf
     *            the rendering engine packed integer buffer.
     * @param sizeX
     *            the X-width of the image rendered.
     * @param sizeY
     *            the Y-width of the image rendered.
     * @return a buffered image wrapping <i>buf</i> with the X-Y dimensions
     *         provided.
     */
    private BufferedImage createBufferedImage(int[] buf, int sizeX, int sizeY) {
        // First wrap the packed integer array with a Java2D buffer
        DataBuffer j2DBuf = new DataBufferInt(buf, sizeX * sizeY, 0);

        // Create a sample model which supplies the bit masks for each colour
        // component.
        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, new int[] {
                        0x00ff0000, // Red
                        0x0000ff00, // Green
                        0x000000ff, // Blue
                // 0xff000000 // Alpha
                });

        // Now create a compatible raster which wraps the Java2D buffer and is
        // told how to get to the pixel data by the sample model.
        WritableRaster raster = new IntegerInterleavedRaster(sampleModel,
                j2DBuf, new Point(0, 0));

        // Finally create a screen accessible colour model and wrap the raster
        // in a buffered image.
        ColorModel colorModel = new DirectColorModel(24, 0x00ff0000, // Red
                0x0000ff00, // Green
                0x000000ff // Blue
        // 0xff000000 // Alpha
        );
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        return image;
    }

    /**
     * Compresses a buffered image thumbnail to an output stream.
     * 
     * @param image
     *            the thumbnail's buffered image.
     * @param stream
     *            the stream to write to.
     * @throws IOException
     *             if there is a problem when writing to <i>stream<i>.
     */
    private void compressThumbnailToStream(BufferedImage image,
            OutputStream stream) throws IOException {
        // Get a JPEG image writer
        ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpeg")
                .next();

        // Setup the compression value from (0.05, 0.75 and 0.95)
        ImageWriteParam iwp = jpegWriter.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(DEFAULT_COMPRESSION_QUALITY);

        // Write the JPEG to our ByteArray stream
        ImageOutputStream imageOutputStream = ImageIO
                .createImageOutputStream(stream);
        jpegWriter.setOutput(imageOutputStream);
        jpegWriter.write(null, new IIOImage(image, null, null), iwp);
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
        FileOutputStream stream = ioService.getThumbnailOutputStream(thumb);
        compressThumbnailToStream(image, stream);
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
        if (sizeX > pixels.getSizeX()) {
            throw new ApiUsageException("sizeX > pixels.sizeX");
        }
        if (sizeX < 0) {
            throw new ApiUsageException("sizeX is negative");
        }
        if (sizeY > pixels.getSizeY()) {
            throw new ApiUsageException("sizeY > pixels.sizeY");
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
        BufferedImage image = createBufferedImage(buf, origSizeX, origSizeY);

        // Finally, scale our image using scaling factors (percentage).
        log.info("Setting xScale factor: " + sizeX + "/" + origSizeX);
        float xScale = (float) sizeX / origSizeX;
        log.info("Setting yScale factor: " + sizeX + "/" + origSizeX);
        float yScale = (float) sizeY / origSizeY;
        return iScale.scaleBufferedImage(image, xScale, yScale);
    }

    protected void errorIfInvalidState() {
        errorIfNullPixels();
    }

    protected void errorIfNullPixels() {
        if (pixelsId == null) {
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            compressThumbnailToStream(image, byteStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceError(e.getMessage());
        }
        return byteStream.toByteArray();
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
}
