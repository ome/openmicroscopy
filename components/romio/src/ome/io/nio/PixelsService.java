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

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;
import loci.formats.meta.IMinMaxStore;
import ome.conditions.LockTimeout;
import ome.conditions.MissingPyramidException;
import ome.conditions.ResourceError;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.messages.MissingPyramidMessage;
import ome.model.core.Pixels;
import ome.model.stats.StatsInfo;
import ome.util.PixelData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

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
     * Creates a pixels pyramid for a given set of pixels. If the pyramid file
     * already exists, then a DEBUG message is logged and this method returns.
     *
     * @param pixels Pixels set to retrieve a pixel buffer for.
     * @since OMERO-Beta4.3
     */
    public StatsInfo[] makePyramid(Pixels pixels)
    {
        final String pixelsFilePath = getPixelsPath(pixels.getId());
        final File pixelsFile = new File(pixelsFilePath);
        final String pixelsPyramidFilePath = pixelsFilePath + PYRAMID_SUFFIX;
        final File pixelsPyramidFile = new File(pixelsPyramidFilePath);
        final String originalFilePath = getOriginalFilePath(pixels);
        
        if (!pixelsFile.exists() && originalFilePath == null)
        {
            log.error("FAIL -- Original pixels file does not exist: "
                    + pixelsFile.getAbsolutePath());
            return null; // EARLY EXIT!
        }

        if (pixelsPyramidFile.exists())
        {
            log.debug("Pyramid already exists: " + pixelsPyramidFilePath);
            return null; // EARLY EXIT!
        }

        final PixelBuffer pixelsPyramid = createPyramidPixelBuffer(
                pixels, pixelsPyramidFilePath, true);

        try
        {
            PixelsPyramidMinMaxStore minMaxStore = performWrite(
                    pixels, pixelsPyramidFile, pixelsPyramid,
                    pixelsFile, pixelsFilePath, originalFilePath);
            if (minMaxStore != null)
            {
                return minMaxStore.createStatsInfo();
            }
            return null;
        }

        finally
        {
            if (pixelsPyramid != null)
            {
                try
                {
                    pixelsPyramid.close();
                }
                catch (IOException e)
                {
                    log.error("Error closing pixel pyramid.", e);
                }
            }
        }
    }

    private PixelsPyramidMinMaxStore performWrite(
            Pixels pixels,final File pixelsPyramidFile,
            final PixelBuffer pixelsPyramid, final File pixelsFile,
            final String pixelsFilePath, final String originalFilePath) {

        final PixelBuffer source;
        final Dimension tileSize;
        final PixelsPyramidMinMaxStore minMaxStore;
        if (pixelsFile.exists())
        {
            minMaxStore = null;
            source = createRomioPixelBuffer(pixelsFilePath, pixels, true);
            // FIXME: This should be configuration or service driven
            // FIXME: Also implemented in RenderingBean.getTileSize()
            tileSize = new Dimension(256, 256);
        }
        else
        {
            minMaxStore = new PixelsPyramidMinMaxStore(pixels.getSizeC());
            source = createMinMaxBfPixelBuffer(originalFilePath, minMaxStore);
            tileSize = source.getTileSize();
        }

        try
        {
            Utils.forEachTile(new TileLoopIteration() {
                public void run(int z, int c, int t, int x, int y, int w,
                            int h, int tileCount)
            {
                try
                {
                    PixelData tile = source.getTile(z, c, t, x, y, w, h);
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
            }, source, (int) tileSize.getWidth(), (int) tileSize.getHeight());

            log.info("SUCCESS -- Pyramid created for pixels id:" + pixels.getId());

        }

        finally
        {
            if (source != null)
            {
                try
                {
                    source.close();
                }
                catch (IOException e)
                {
                    log.error("Error closing pixel pyramid.", e);
                }
            }
        }
        return minMaxStore;
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
        final String originalFilePath = getOriginalFilePath(pixels);

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
        if ((pixelsFile.exists() || originalFilePath != null) && requirePyramid)
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
            return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath, false);
        }

        //
        // 3. Finally, this must be a ROMIO, direct writing to the
        // BfPyramidPixelBuffer or OMERO.fs "light" (where the original data
        // has been archived). If the relevant OriginalFile path can be
        // resolved use it with BfPixelBuffer, otherwise create a new
        // RomioPixelBuffer and return.
        if (!pixelsFile.exists())
        {
            if (requirePyramid) {
                log.info("Creating Pyramid BfPixelBuffer: " +
                        pixelsPyramidFilePath);
                return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath, true);
            } else {
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
    protected String getOriginalFilePath(Pixels pixels)
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
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                    log.error("Error closing stream.", e);
                }
            }
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
     * Helper method to properly log any exceptions raised by Bio-Formats and
     * add a min/max calculator wrapper to the reader stack.
     * @param filePath Non-null.
     * @param store Min/max store to use with the min/max calculator.
     * @param reader passed to {@link BfPixelBuffer}
     * @return
     */
    protected PixelBuffer createMinMaxBfPixelBuffer(final String filePath,
                                                    IMinMaxStore store) {
        try
        {
            IFormatReader reader = new ImageReader();
            reader = new ChannelFiller(reader);
            reader = new ChannelSeparator(reader);
            MinMaxCalculator calculator = new MinMaxCalculator(reader);
            calculator.setMinMaxStore(store);
            return new BfPixelBuffer(filePath, calculator);
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
    protected PixelBuffer createBfPixelBuffer(final String filePath) {
        try
        {
            IFormatReader reader = new ImageReader();
            reader = new ChannelFiller(reader);
            reader = new ChannelSeparator(reader);
            return new BfPixelBuffer(filePath, reader);
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
            final String filePath, boolean write) {

        try
        {
            if (write) {
                // #5159. Creating the path if we need to write.
                createSubpath(filePath);
            }
            return new BfPyramidPixelBuffer(pixels, filePath, write);
        }
        catch (Exception e)
        {
            if (e instanceof LockTimeout) {
                throw (LockTimeout) e;
            }
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

    class PixelsPyramidMinMaxStore implements IMinMaxStore
    {
        final double[][] channelGlobalMinMax;

        final int sizeC;

        public PixelsPyramidMinMaxStore(int sizeC)
        {
            this.sizeC = sizeC;
            channelGlobalMinMax = new double[sizeC][2];
        }

        /* (non-Javadoc)
         * @see loci.formats.meta.IMinMaxStore#setChannelGlobalMinMax(int, double, double, int)
         */
        public void setChannelGlobalMinMax(int channel, double minimum,
                                           double maximum, int series)
        {
            channelGlobalMinMax[channel][0] = minimum;
            channelGlobalMinMax[channel][1] = maximum;
        }

        public StatsInfo[] createStatsInfo()
        {
            StatsInfo[] statsInfo = new StatsInfo[sizeC];
            for (int c = 0; c < sizeC; c++)
            {
                statsInfo[c] = new StatsInfo();
                statsInfo[c].setGlobalMin(channelGlobalMinMax[c][0]);
                statsInfo[c].setGlobalMax(channelGlobalMinMax[c][1]);
            }
            return statsInfo;
        }
    }
}
