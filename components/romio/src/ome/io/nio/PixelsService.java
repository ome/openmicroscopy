/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006-2013 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.Memoizer;
import loci.formats.MinMaxCalculator;
import loci.formats.meta.IMinMaxStore;
import ome.api.IQuery;
import ome.conditions.LockTimeout;
import ome.conditions.MissingPyramidException;
import ome.conditions.ResourceError;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.messages.MissingPyramidMessage;
import ome.io.messages.MissingStatsInfoMessage;
import ome.parameters.Parameters;
import ome.system.metrics.Metrics;
import ome.system.metrics.Timer;
import ome.model.core.Pixels;
import ome.model.stats.StatsInfo;
import ome.util.PixelData;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private transient static Logger log = LoggerFactory.getLogger(PixelsService.class);

	/** Publisher interface used to publish messages concerning missing
	 * data and similar. */
	private transient ApplicationEventPublisher pub;

	/** Suffix for an the image pyramid of a given pixels set. */
	public static final String PYRAMID_SUFFIX = "_pyramid";

	/** Null plane size constant. */
	public static final int NULL_PLANE_SIZE = 64;

	/** Default of 100 ms for {@link #memoizerWait} */
	public static final long MEMOIZER_WAIT = 100;

	/** Resolver of archived original file paths for pixels sets. */
	protected FilePathResolver resolver;

	/** BackOff implementation for calculating MissingPyramidExceptions */
	protected final BackOff backOff;

	/** TileSizes implementation for default values */
	protected final TileSizes sizes;

	/**
	 * Location where cached data from the {@link Memoizer} should be stored.
	 */
	protected final File memoizerDirectory;

	/**
	 * Time in ms. which setId must take before a file is memoized
	 */
	protected final long memoizerWait;

	private Timer tileTimes;

	private Timer minmaxTimes;
	
	private IQuery iQuery;

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
        this(path, null, new SimpleBackOff(), new ConfiguredTileSizes(), null);
    }

    /**
     * Constructor.
     * @param path The root of the ROMIO proprietary pixels store. (usually
     * <code>/OMERO/Pixels</code>).
     */
    public PixelsService(String path, FilePathResolver resolver)
    {
        this(path, resolver, new SimpleBackOff(), new ConfiguredTileSizes(), null);
    }

    /**
     * Constructor.
     * @param path The root of the ROMIO proprietary pixels store. (usually
     * <code>/OMERO/Pixels</code>).
     * @param resolver Original file path resolver for pixels sets.
     */
    public PixelsService(String path, FilePathResolver resolver, BackOff backOff, TileSizes sizes, IQuery iQuery)
    {
        this(path, new File(new File(path), "BioFormatsCache"), resolver,
                backOff, sizes, iQuery);
    }

    /**
     * Call {@link #PixelsService(String, File, long, FilePathResolver, BackOff, TileSizes)}
     * with {@link #MEMOIZER_WAIT}.
     */
    public PixelsService(String path, long memoizerWait,
            FilePathResolver resolver, BackOff backOff, TileSizes sizes, IQuery iQuery) {
        this(path, new File(new File(path), "BioFormatsCache"),
                memoizerWait, resolver, backOff, sizes, iQuery);
    }

    /**
     * Call {@link #PixelsService(String, File, long, FilePathResolver, BackOff, TileSizes)}
     * with {@link #MEMOIZER_WAIT}.
     */
    public PixelsService(String path, File memoizerDirectory,
            FilePathResolver resolver, BackOff backOff, TileSizes sizes, IQuery iQuery) {
        this(path, memoizerDirectory, MEMOIZER_WAIT, resolver, backOff, sizes, iQuery);
    }

    public PixelsService(String path, File memoizerDirectory, long memoizerWait,
            FilePathResolver resolver, BackOff backOff, TileSizes sizes, IQuery iQuery)
    {
        super(path);
        this.resolver = resolver;
        this.backOff = backOff;
        this.sizes = sizes;
        this.memoizerDirectory = memoizerDirectory;
        this.memoizerWait = memoizerWait;
        if (!this.memoizerDirectory.exists())
        {
            log.info("Creating Bio-Formats Cache: {}", memoizerDirectory);
            this.memoizerDirectory.mkdirs();
        } else {
            log.info("Using Bio-Formats Cache: {}", memoizerDirectory);
        }

        if (log.isInfoEnabled())
        {
            log.info("PixelsService(path=" +
                     path + ", resolver=" + resolver + ", backoff=" + backOff +
                     ", sizes=" + sizes + ")");
        }
        this.iQuery = iQuery;
    }

    public void setMetrics(Metrics metrics) {
        this.tileTimes = metrics.timer(this, "tileTimes");
        this.minmaxTimes = metrics.timer(this, "minmaxTimes");
    }

    public long getMemoizerWait() {
        return memoizerWait;
    }

    public File getMemoizerDirectory() {
        return memoizerDirectory;
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
        final boolean requirePyramid = requiresPixelsPyramid(pixels);

        // This was called perhaps while a pyramid was
        // being generated, and is no longer needed.
        if (pixelsPyramidFile.exists())
        {
            log.debug("Pyramid already exists: " + pixelsPyramidFilePath);
            return null; // EARLY EXIT!
        }

        if (!requirePyramid)
        {
            log.debug("Creating only StatsInfo.");
            int series = getSeries(pixels);
            final PixelsPyramidMinMaxStore minMaxStore =
                new PixelsPyramidMinMaxStore(pixels.getSizeC());
            BfPixelBuffer bfPixelBuffer = createMinMaxBfPixelBuffer(
                    originalFilePath, series, minMaxStore);

            try
            {
                for (int t = 0; t < pixels.getSizeT(); t++)
                {
                    for (int c = 0; c < pixels.getSizeC(); c++)
                    {
                        for (int z = 0; z < pixels.getSizeZ(); z++)
                        {
                            Timer.Context ctx = minmaxTimes == null ?
                                null : minmaxTimes.time();
                            try {
                                bfPixelBuffer.getPlane(z, c, t);
                            } finally {
                                if (ctx != null) {
                                    ctx.stop();
                                }
                            }
                        }
                    }
                }
                return minMaxStore.createStatsInfo();
            }
            catch (IOException e)
            {
                log.error("I/O exception while calculating min/max.", e);
                return null;
            }
        }

        final BfPyramidPixelBuffer pixelsPyramid = createPyramidPixelBuffer(
                pixels, pixelsPyramidFilePath, true);

        try
        {

            // If we don't have any data to properly generate the pyramid
            // we close the instance which will save an empty (and therefore
            // corrupt) pyramid. This is intentional since further calls will
            // get an exception rather than being told to try indefinitely.
            // (see ticket:5189)
            if (!pixelsFile.exists() && originalFilePath == null)
            {
                log.error("FAIL -- Original pixels file does not exist: "
                        + pixelsFile.getAbsolutePath());
                return null; // EARLY EXIT! closed in finally block!
            }

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
            final Pixels pixels,final File pixelsPyramidFile,
            final BfPyramidPixelBuffer pixelsPyramid, final File pixelsFile,
            final String pixelsFilePath, final String originalFilePath) {

        final PixelBuffer source;
        final Dimension tileSize;
        final PixelsPyramidMinMaxStore minMaxStore;

        if (pixelsFile.exists())
        {
            minMaxStore = null;
            source = createRomioPixelBuffer(pixelsFilePath, pixels, false);
            // FIXME: This should be configuration or service driven
            // FIXME: Also implemented in RenderingBean.getTileSize()
            tileSize = new Dimension(Math.min(pixels.getSizeX(), sizes.getTileWidth()),
                                     Math.min(pixels.getSizeY(), sizes.getTileHeight()));
        }
        else
        {
            minMaxStore = new PixelsPyramidMinMaxStore(pixels.getSizeC());
            int series = getSeries(pixels);
            BfPixelBuffer bfPixelBuffer = createMinMaxBfPixelBuffer(
                    originalFilePath, series, minMaxStore);
            pixelsPyramid.setByteOrder(
                    bfPixelBuffer.isLittleEndian()? ByteOrder.LITTLE_ENDIAN
                            : ByteOrder.BIG_ENDIAN);
            source = bfPixelBuffer;
            // If the tile sizes we've been given are completely ridiculous
            // then reset them to WIDTHxHEIGHT. Currently these conditions are:
            //  * TileWidth == ImageWidth
            //  * TileHeight == ImageHeight
            //  * Smallest tile dimension divided by the largest resolution
            //    level factor is < 1.
            // -- Chris Allan (ome:#5224).
            final Dimension sourceTileSize = source.getTileSize();
            final double tileWidth = sourceTileSize.getWidth();
            final double tileHeight = sourceTileSize.getHeight();
            final boolean tileDimensionTooSmall;
            double factor = Math.pow(2, 5);
            if (((tileWidth / factor) < 1.0)
                || ((tileHeight / factor) < 1.0))
            {
                tileDimensionTooSmall = true;
            }
            else
            {
                tileDimensionTooSmall = false;
            }
            if (tileWidth == source.getSizeX()
                || tileHeight == source.getSizeY()
                || tileDimensionTooSmall)
            {
                tileSize = new Dimension(Math.min(pixels.getSizeX(), sizes.getTileWidth()),
                                         Math.min(pixels.getSizeY(), sizes.getTileHeight()));
            }
            else
            {
                tileSize = sourceTileSize;
            }
        }
        log.info("Destination pyramid tile size: " + tileSize);

        try
        {
            final double totalTiles =
                source.getSizeZ() * source.getSizeC() * source.getSizeT() *
                (Math.ceil(source.getSizeX() / tileSize.getWidth())) *
                (Math.ceil(source.getSizeY() / tileSize.getHeight()));
            final int tenPercent = Math.max((int) totalTiles / 10, 1);
            Utils.forEachTile(new TileLoopIteration() {
                public void run(int z, int c, int t, int x, int y, int w,
                            int h, int tileCount)
            {
                if (log.isInfoEnabled()
                    && tileCount % tenPercent == 0)
                {
                    log.info(String.format(
                            "Pyramid creation for Pixels:%d %d/%d (%d%%).",
                            pixels.getId(), tileCount + 1, (int) totalTiles,
                            (int) (tileCount / totalTiles * 100)));
                }
                try
                {
                    Timer.Context ctx = tileTimes == null ? null : tileTimes.time();
                    try {
                        PixelData tile = source.getTile(z, c, t, x, y, w, h);
                        pixelsPyramid.setTile(
                            tile.getData().array(), z, c, t, x, y, w, h);
                        tile.dispose();
                    } finally {
                        if (ctx != null) {
                            ctx.stop();
                        }
                    }
                }
                catch (IOException e1)
                {
                    log.error("FAIL -- Error during tile population", e1);
                    try
                    {
                        pixelsPyramidFile.delete();
                        FileUtils.touch(pixelsPyramidFile); // ticket:5189
                    }
                    catch (Exception e2)
                    {
                        log.warn("Error clearing empty or incomplete pixel " +
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
     * @return A pixel buffer instance. <b>NOTE:</b> The pixel buffer is
     * initialized as <b>read-write</b>.
     * @deprecated In the future callers should use the more descriptive
     * {@link #getPixelBuffer(Pixels, boolean)}.
     * @since OMERO-Beta4.3
     * @see #getPixelBuffer(Pixels, boolean)
     */
    @Deprecated
    public PixelBuffer getPixelBuffer(Pixels pixels)
    {
        return getPixelBuffer(pixels, true);
    }

   /**
     * Returns a pixel buffer for a given set of pixels. Either a proprietary
     * ROMIO pixel buffer or a specific pixel buffer implementation.
     * @param pixels Pixels set to retrieve a pixel buffer for.
     * @param write Whether or not to open the pixel buffer as read-write.
     * <code>true</code> opens as read-write, <code>false</code> opens as
     * read-only.
     * @return A pixel buffer instance.
     * @since OMERO-Beta4.3
     */
    public PixelBuffer getPixelBuffer(Pixels pixels, boolean write)
    {
        PixelBuffer pb = _getPixelBuffer(pixels, write);
        if (log.isDebugEnabled()) {
            log.debug(pb +" for " + pixels);
        }
        return pb;
    }

    public PixelBuffer _getPixelBuffer(Pixels pixels, boolean write)
    {
        final String originalFilePath = getOriginalFilePath(pixels);
        final boolean requirePyramid = requiresPixelsPyramid(pixels);
        final String pixelsFilePath = getPixelsPath(pixels.getId());
        final File pixelsFile = new File(pixelsFilePath);
        final String pixelsPyramidFilePath = pixelsFilePath + PYRAMID_SUFFIX;
        final File pixelsPyramidFile = new File(pixelsPyramidFilePath);
        final boolean pixelsFileExists = pixelsFile.exists();

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
        if ((pixelsFileExists || originalFilePath != null) && requirePyramid)
        {
            while (!pixelsPyramidFile.exists()) {
                // If we are in OMERO.fs mode and the source original file
                // is already a pyramid don't try and create one.
                if (originalFilePath != null) {
                    int series = getSeries(pixels);
                    PixelBuffer bfPixelBuffer = createBfPixelBuffer(
                            originalFilePath, series);
                    if (bfPixelBuffer.getResolutionLevels() > 1) {
                        return bfPixelBuffer;
                    }
                }
                // throws if loop should exit!
                handleMissingPyramid(pixels, pixelsPyramidFilePath);
            }
        }
        // Note: since the OMERO.fs work, a pixels pyramid is only required
        // when the pixels set meets big image criteria.
        if (!pixelsFileExists && requirePyramid && originalFilePath != null)
        {
            handleMissingStatsInfo(pixels);
        }

        //
        // 2. If the pyramid exists, regardless of whether or not it
        // is required, we should use it.
        //
        if (pixelsPyramidFile.exists())
        {
            log.info("Using Pyramid BfPixelBuffer: " + pixelsPyramidFilePath);
            return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath, write);
        }

        //
        // 3. Finally, this must be a ROMIO, direct writing to the
        // BfPyramidPixelBuffer or OMERO.fs "light" (where the original data
        // has been archived). If the relevant OriginalFile path can be
        // resolved use it with BfPixelBuffer, otherwise create a new
        // RomioPixelBuffer and return.
        if (!pixelsFileExists)
        {
            if (requirePyramid) {
                if (!write) {
                    throw new LockTimeout(
                            "Pixels pyramid missing, being created or " +
                            "import in progress.", 15*1000, 0);
                }
                log.info("Creating Pyramid BfPixelBuffer: " +
                        pixelsPyramidFilePath);
                return createPyramidPixelBuffer(pixels, pixelsPyramidFilePath, write);
            } else {
                if (originalFilePath != null) {
                    int series = getSeries(pixels);
                    return createBfPixelBuffer(originalFilePath, series);
                }
                if (!write) {
                    throw new LockTimeout("Import in progress.", 15*1000, 0);
                }
                log.info("Creating ROMIO Pixel buffer.");
                createSubpath(pixelsFilePath);
                return createRomioPixelBuffer(pixelsFilePath, pixels, write);
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
    public boolean requiresPixelsPyramid(Pixels pixels) {
        String type = pixels.getPixelsType().getValue();
        if ("float".equals(type) || "double".equals(type))
            return false;
        final long sizeX = pixels.getSizeX();
        final long sizeY = pixels.getSizeY();
        final boolean requirePyramid = (sizeX * sizeY) > (sizes.getMaxPlaneWidth()*sizes.getMaxPlaneHeight());
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
     * Retrieves the series for a given set of pixels.
     * @param pixels Set of pixels to return the series for.
     * @return The series as specified by the pixels parameters or
     * <code>0</code> (the first series).
     */
    protected int getSeries(Pixels pixels)
    {
        try
        {
            final String query = "SELECT image.series FROM Pixels WHERE id = :id";
            final List<Object[]> results = iQuery.projection(query, new Parameters().addId(pixels.getId()));
            return (Integer) results.get(0)[0];
        }
        catch (Exception e)  // NumberFormatException, NullPointerException
        {
            return 0;
        }
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
        Integer size = RomioPixelBuffer.safeLongToInteger(pixbuf.getPlaneSize());
		byte[] padding = new byte[size - NULL_PLANE_SIZE];
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
     * @param pixels
     */
    protected void handleMissingStatsInfo(Pixels pixels) {
        for (int channel = 0; channel < pixels.sizeOfChannels(); channel++)
        {
            if (pixels.getChannel(channel).getStatsInfo() != null)
            {
                return;
            }
        }
        long pixelsId = pixels.getId();
        MissingStatsInfoMessage m = new MissingStatsInfoMessage(this, pixelsId);
        pub.publishEvent(m);
        if (m.isRetry()) {
            log.debug("Retrying stats info for Pixels:" + pixelsId);
            return;
        }
        String msg = "Missing stats info for Pixels:" + pixelsId;
        log.info(msg);

        backOff.throwMissingPyramidException(msg, pixels);
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
        log.info(msg);

        backOff.throwMissingPyramidException(msg, pixels);
    }

    /**
     * Helper method to properly log any exceptions raised by Bio-Formats and
     * add a min/max calculator wrapper to the reader stack.
     * @param filePath Non-null.
     * @param store Min/max store to use with the min/max calculator.
     * @param series series to use
     * @param reader passed to {@link BfPixelBuffer}
     * @return
     */
    protected BfPixelBuffer createMinMaxBfPixelBuffer(final String filePath,
                                                      final int series,
                                                      final IMinMaxStore store)
    {
        try
        {
            IFormatReader reader = createBfReader();
            MinMaxCalculator calculator = new MinMaxCalculator(reader);
            calculator.setMinMaxStore(store);
            BfPixelBuffer pixelBuffer = new BfPixelBuffer(filePath, calculator);
            pixelBuffer.setSeries(series);
            log.info(String.format("Creating BfPixelBuffer: %s Series: %d",
                    filePath, series));
            return pixelBuffer;
        }
        catch (Exception e)
        {
            String msg = "Error instantiating pixel buffer: " + filePath;
            log.error(msg, e);
            throw new ResourceError(msg);
        }
    }


    /**
     * Short-cut in the FS case where we know that we are dealing with a FS-lite
     * file, and want to retrieve the actual file as opposed to a pyramid or anything
     * else. This may be used to access the original metadata.
     * @throws FormatException
     * @throws IOException
     */
    public IFormatReader getBfReader(Pixels pixels) throws FormatException, IOException {
        // from getPixelBuffer
        final String originalFilePath = getOriginalFilePath(pixels);
        final int series = getSeries(pixels);
        final IFormatReader reader = createBfReader();
        reader.setId(originalFilePath); // Called by BfPixelsBuffer elsewhere.
        reader.setSeries(series);
        return reader;
    }

    /**
     * Create an {@link IFormatReader} with the appropriate {@link loci.formats.ReaderWrapper}
     * instances and {@link IFormatReader#setFlattenedResolutions(boolean)} set to false.
     */
    protected IFormatReader createBfReader() {
        IFormatReader reader = new ImageReader();
        reader = new ChannelFiller(reader);
        reader = new ChannelSeparator(reader);
        reader = new Memoizer(reader, getMemoizerWait(), getMemoizerDirectory());
        reader.setFlattenedResolutions(false);
        reader.setMetadataFiltered(true);
        return reader;
    }

    /**
     * Helper method to properly log any exceptions raised by Bio-Formats.
     * @param filePath Non-null.
     * @param reader passed to {@link BfPixelBuffer}
     * @param series series to use
     * @return
     */
    protected BfPixelBuffer createBfPixelBuffer(final String filePath,
                                              final int series) {
        try
        {
            IFormatReader reader = createBfReader();
            BfPixelBuffer pixelBuffer = new BfPixelBuffer(filePath, reader);
            pixelBuffer.setSeries(series);
            log.info(String.format("Creating BfPixelBuffer: %s Series: %d",
                    filePath, series));
            return pixelBuffer;
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
    protected BfPyramidPixelBuffer createPyramidPixelBuffer(final Pixels pixels,
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
