/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import loci.formats.ImageReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import ome.conditions.MissingPyramidException;
import ome.conditions.ResourceError;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.messages.MissingPyramidMessage;
import ome.model.core.Pixels;
import ome.util.PixelData;

/**
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO-Beta1.0
 */
public class PixelsService extends AbstractFileSystemService
    implements ApplicationEventPublisherAware {

	/** The logger for this class. */
	private transient static Log log = LogFactory.getLog(PixelsService.class);
	
	/** Publisher interface used to publish messages concerning missing
	 * data and similar. */
	private transient ApplicationEventPublisher pub;

	/** The DeltaVision file format enumeration value */
	public static final String DV_FORMAT = "DV";

    /** Suffix for an the image pyramid of a given pixels set. */
    public static final String PYRAMID_SUFFIX = "_pyramid";

	/** Null plane size constant. */
	public static final int NULL_PLANE_SIZE = 64;

    /** Resolver of archived original file paths for pixels sets. */
    protected FilePathResolver resolver;

	/** Null plane byte array. */
	public static final byte[] nullPlane = new byte[] { -128, 127, -128, 127,
			-128, 127, -128, 127, -128, 127, // 10
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 20
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 30
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 40
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 50
			-128, 127, -128, 127, -128, 127, -128, 127, -128, 127, // 60
			-128, 127, -128, 127 }; // 64

    /**
     * Constructor.
     * @param path The root of the ROMIO proprietary pixels store. (usually
     * <code>/OMERO/Pixels</code>).
     */
    public PixelsService(String path)
    {
        super(path);
        this.resolver = null;
        if (log.isInfoEnabled())
        {
            log.info("Constructed pixel buffer with path: " + path);
        }

    }

    /**
     * Constructor.
     * @param path The root of the ROMIO proprietary pixels store. (usually
     * <code>/OMERO/Pixels</code>).
     * @param resolver Original file path resolver for pixels sets.
     */
    public PixelsService(String path, FilePathResolver resolver)
    {
        super(path);
        this.resolver = resolver;
        if (log.isInfoEnabled())
        {
            log.info("Constructed pixel buffer with path: " +
                     path + " resolver: " + resolver);
        }
    }

	public void setApplicationEventPublisher(ApplicationEventPublisher pub) {
	    if (this.pub != null) {
	        throw new FatalBeanException("Publisher already set.");
	    }
	    this.pub = pub;
	}

    public void setFilePathResolver(FilePathResolver resolver)
    {
        this.resolver = resolver;
    }

	/**
	 * Creates a PixelBuffer for a given pixels set.
	 * 
	 * @param pixels Pixels set to create a pixel buffer for.
	 * @return Allocated pixel buffer ready to be used.
	 * @throws IOException If there is an I/O error creating the pixel buffer
	 * backing file.
	 */
	public PixelBuffer createPixelBuffer(Pixels pixels) throws IOException {
		RomioPixelBuffer pixbuf = new RomioPixelBuffer(getPixelsPath(pixels
				.getId()), pixels, true);
		initPixelBuffer(pixbuf);
		return pixbuf;
	}

    /**
     * Creates a pixels pyramid for a given set of pixels.
     * @param pixels Pixels set to retrieve a pixel buffer for.
     * @param pixelsFilePath Absolute path to the pixels set. If null, this
     *      represents a {@link RomioPixelBuffer}
     * @param provider Original file metadata provider.
     * @param bypassOriginalFile Do not check for the existence of an original
     * file to back this pixel buffer.
     * @return See above.
     * @since OMERO-Beta4.3
     */
    public void makePyramid(Pixels pixels,
                            String pixelsFilePath)
    {
        final boolean useRomio = (pixelsFilePath == null);
        if (useRomio)
        {
            pixelsFilePath = getPixelsPath(pixels.getId());
        }

        final File pixelsFile = new File(pixelsFilePath);
        final String pixelsPyramidFilePath = pixelsFilePath + PYRAMID_SUFFIX;
        final File pixelsPyramidFile = new File(pixelsPyramidFilePath);

        if (!pixelsFile.exists())
        {
            log.error("FAIL -- Original pixels file does not exist: "
                    + pixelsFile.getAbsolutePath());
            return;
        }
        final PixelBuffer pixelsPyramid = createPyramidPixelBuffer(
                pixels, pixelsPyramidFilePath);
        final PixelBuffer romio = createRomioPixelBuffer(
                pixelsFilePath, pixels, true);
        // Dimension tileSize = pixelsPyramid.getTileSize();
        // FIXME: This should be configuration or service driven
        // FIXME: Also implemented in RenderingBean.getTileSize()
        Dimension tileSize = new Dimension(256, 256);
        Utils.forEachTile(new TileLoopIteration() {
            public void run(int z, int c, int t, int x, int y, int w,
                            int h, int tileCount)
            {
                try
                {
                    PixelData tile = romio.getTile(z, c, t, x, y, w, h);
                    pixelsPyramid.setTile(
                            tile.getData().array(), z, c, t, x, y, w, h);
                }
                catch (IOException e1)
                {
                    log.error("FAIL -- Error during tile population", e1);
                    try
                    {
                        pixelsPyramidFile.delete();
                    }
                    catch (Exception e2)
                    {
                        log.warn("Error deleting empty or incomplete pixel " +
                                 "buffer.", e2);
                    }
                    return;
                }
            }
        }, romio, (int) tileSize.getWidth(), (int) tileSize.getHeight());
        log.info("SUCCESS -- Pyramid created for pixels id:" + pixels.getId());
    }

   /**
     * Returns a pixel buffer for a given set of pixels. Either a proprietary
     * ROMIO pixel buffer or a specific pixel buffer implementation.
     * @param pixels Pixels set to retrieve a pixel buffer for.
     * @return See above.
     * @since OMERO-Beta4.3
     */
    public PixelBuffer getPixelBuffer(Pixels pixels)
    {
        final boolean requirePyramid = isRequirePyramid(pixels);
        final String pixelsFilePath = getPixelsPath(pixels.getId());
        final File pixelsFile = new File(pixelsFilePath);
        final String pixelsPyramidFilePath = pixelsFilePath + PYRAMID_SUFFIX;
        final File pixelsPyramidFile = new File(pixelsPyramidFilePath);

        //
        // 1. If the pixels file exists, then we know that this isn't
        // an attempt to write a new ROMIO Pixels file, therefore if
        // a pyramid is required but does not exist, then we raise a
        // message allowing other beans the chance to create the pyramid.
        // If none signal "retry", then all we can do is throw.
        //
        // Note: since big pixels stored with 4.3+ will generate only
        // a pyramid, the existence of the ROMIO Pixels file implies
        // that this is legacy data.
        //
        if (pixelsFile.exists() && requirePyramid)
        {
            while (!pixelsPyramidFile.exists()) {
                // throws if loop should exit!
                handleMissingPyramid(pixels, pixelsPyramidFilePath);
            }
        }

        //
        // 2. If the pyramid exists, regardless of whether or not it
        // is required, we should use it.
        //
        if (pixelsPyramidFile.exists())
        {
            log.info("Using Pyramid BfPixelBuffer: " + pixelsPyramidFilePath);
            return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath);
        }

        //
        // 3. Finally, this must be a ROMIO or OMERO.fs "light" (where the
        // original data has been archived. If the relevant OriginalFile can
        // be resolved use it with BfPixelBuffer, otherwise create a new
        // RomioPixelBuffer and return.
        if (!pixelsFile.exists())
        {
            if (requirePyramid) {
                log.info("Creating Pyramid BfPixelBuffer: " +
                        pixelsPyramidFilePath);
                createSubpath(pixelsPyramidFilePath);
                return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath);
            } else {
                final String originalFilePath = getOriginalFilePath(pixels);
                if (originalFilePath != null) {
                    log.info("Using BfPixelBuffer: " + pixelsFilePath);
                    return createBfPixelBuffer(originalFilePath);
                }
                log.info("Creating ROMIO Pixel buffer.");
                createSubpath(pixelsFilePath);
                return createRomioPixelBuffer(pixelsFilePath, pixels, true);
            }
        }

        log.info("Pixel buffer file exists returning read-only " +
                 "ROMIO pixel buffer.");
        return createRomioPixelBuffer(pixelsFilePath, pixels, false);
    }

    /**
     * Returns true if a pyramid should be used for the given {@link Pixels}.
     * This usually implies that this is a "Big image" and therefore will
     * need tiling.
     *
     * @param pixels
     * @return
     */
    public boolean isRequirePyramid(Pixels pixels) {
        final int sizeX = pixels.getSizeX();
        final int sizeY = pixels.getSizeY();
        final boolean requirePyramid = (sizeX * sizeY) > (1024*1024); // FIXME
        return requirePyramid;
    }

    /**
     * Retrieves the original file path for a given set of pixels.
     * @param pixels Set of pixels to return an orignal file path for.
     * @return The original file path or <code>null</code> if the original file
     * path could not be located or the <code>resolver</code> has not been set.
     */
    private String getOriginalFilePath(Pixels pixels)
    {
        if (resolver == null)
        {
            return null;
        }
        return resolver.getOriginalFilePath(this, pixels);
    }

	/**
	 * Initializes each plane of a PixelBuffer using a null plane byte array.
	 * 
	 * @param pixbuf Pixel buffer to initialize.
	 * @throws IOException If there is an I/O error during initialization.
	 */
	private void initPixelBuffer(RomioPixelBuffer pixbuf) throws IOException {
		String path = getPixelsPath(pixbuf.getId());
		createSubpath(path);
		byte[] padding = new byte[pixbuf.getPlaneSize() - NULL_PLANE_SIZE];
		FileOutputStream stream = new FileOutputStream(path);
		try {
			for (int z = 0; z < pixbuf.getSizeZ(); z++) {
				for (int c = 0; c < pixbuf.getSizeC(); c++) {
					for (int t = 0; t < pixbuf.getSizeT(); t++) {
						stream.write(nullPlane);
						stream.write(padding);
					}
				}
			}
		} finally {
			ome.util.Utils.closeQuietly(stream);
		}
	}


	/**
	 * If the outer loop should continue, this method returns successfully;
	 * otherwise it throws a MissingPyramidException.
	 *
	 * @param pixels
	 * @param pixelsPyramidFilePath
	 * @return
	 * @throws MissingPyramidException
	 */
    protected void handleMissingPyramid(Pixels pixels,
            final String pixelsPyramidFilePath) {
        MissingPyramidMessage mpm = new MissingPyramidMessage(this,
                pixels.getId());
        pub.publishEvent(mpm);
        if (mpm.isRetry()) {
            log.debug("Retrying pyramid:" + pixelsPyramidFilePath);
            return;
        }
        String msg = "Missing pyramid:" + pixelsPyramidFilePath;
        log.warn(msg);

        // FIXME make backoff configurable
        throw new MissingPyramidException(msg, 15*1000, pixels.getId());
    }

    /**
     * Helper method to properly log any exceptions raised by Bio-Formats.
     * @param filePath Non-null.
     * @param reader passed to {@link BfPixelBuffer}
     * @return
     */
    protected PixelBuffer createBfPixelBuffer(final String filePath) {
        try
        {
            return new BfPixelBuffer(filePath, new ImageReader());
        }
        catch (Exception e)
        {
            String msg = "Error instantiating pixel buffer: " + filePath;
            log.error(msg, e);
            throw new ResourceError(msg);
        }
    }

    /**
     * Helper method to properly log any exceptions raised by Bio-Formats.
     * @param filePath Non-null.
     * @param reader passed to {@link BfPixelBuffer}
     * @return
     */
    protected PixelBuffer createPyramidPixelBuffer(final Pixels pixels,
            final String filePath) {

        try
        {
            return new BfPyramidPixelBuffer(pixels, filePath);
        }
        catch (Exception e)
        {
            String msg = "Error instantiating pixel buffer: " + filePath;
            log.error(msg, e);
            throw new ResourceError(msg);
        }
    }

    /**
     * Helper method to properlty create a RomioPixelBuffer.
     *
     * @param pixelsFilePath
     * @param pixels
     * @param allowModification
     * @return
     */
    protected PixelBuffer createRomioPixelBuffer(String pixelsFilePath,
        Pixels pixels, boolean allowModification) {
        return new RomioPixelBuffer(pixelsFilePath, pixels, allowModification);
    }

    /**
	 * Removes files from data repository based on a parameterized List of Long
	 * pixels ids
	 * 
	 * @param pixelsIds Long file keys to be deleted
	 * @throws ResourceError If deletion fails.
	 */
	public void removePixels(List<Long> pixelIds) {
		File file;
		String fileName;
		boolean success = false;

		for (Iterator<Long> iter = pixelIds.iterator(); iter.hasNext();) {
			Long id = iter.next();

			String pixelPath = getPixelsPath(id);
			file = new File(pixelPath);
			fileName = file.getName();
			if (file.exists()) {
				success = file.delete();
				if (!success) {
					throw new ResourceError(
							"Pixels " + fileName + " deletion failed");
				} else {
					if (log.isInfoEnabled()) {
						log.info("INFO: Pixels " + fileName + " deleted.");
					}
				}
			}
		}
	}
}
