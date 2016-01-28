/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.bioformats;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.in.TiffReader;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffCompression;
import ome.conditions.ApiUsageException;
import ome.conditions.LockTimeout;
import ome.conditions.ResourceError;
import ome.io.nio.ConfiguredTileSizes;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.TileSizes;
import ome.model.core.Pixels;
import ome.util.PixelData;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.primitives.PositiveInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PixelBuffer} implementation which uses Bio-Formats to
 * read pixels data directly from original files.
 *
 * @since OMERO-Beta4.3
 */
public class BfPyramidPixelBuffer implements PixelBuffer {

    private final static Logger log = LoggerFactory.getLogger(BfPyramidPixelBuffer.class);

    private BfPixelBuffer delegate;

    /** Bio-Formats implementation used to write to the backing TIFF. */
    protected OmeroPixelsPyramidWriter writer;

    /**
     * Bio-Formats implementation the delegate uses to read the backing TIFF.
     */
    protected OmeroPixelsPyramidReader reader;

    /**
     * File's who absolute path will be passed to
     * {@link TiffReader#setId(String)} for reading.
     *
     * @see {@link #writePath}
     */
    private final File readerFile;

    /** Description of tile sizes */
    private final TileSizes sizes;

    /** The OMERO pixels set we're backing. */
    private final Pixels pixels;

    /** Last IFD we used during a tile write operation. */
    private IFD lastIFD;

    /** Last z-section offset we used during a tile write operation. */
    private int lastZ = -1;

    /** Last channel offset we used during a tile write operation. */
    private int lastC = -1;

    /** Last timepoint offset  we used during a tile write operation. */
    private int lastT = -1;

    /** Metadata implementation used when writing. */
    private IMetadata metadata;

    // LOCKING. See ticket #5083

    /**
     * File whose absolute path will be given to the {@link TiffWriter}.
     *
     * This prevents that a partially written file can be accessed, if some
     * other process does not attempt to acquire the lock. On close, if this is
     * non-null, then a move from this location to the {@link #filePath} (the
     * reader path) will be attempted.
     */
    private File writerFile;

    /**
     * Lock file used both for the {@link TiffReader} and {@link TiffWriter}
     * process.
     */
    private File lockFile;

    /**
     * {@link RandomAccessFile} opened for the {@link #lockFile} path.
     */
    private RandomAccessFile lockRaf;

    /**
     * If not null, {@link FileLock} instance acquired from the {@link #lockRaf}
     */
    private FileLock fileLock;

    /** The byte order of the compressed pyramid. */
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    public static final String PYR_LOCK_EXT = ".pyr_lock";

    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object?
     * There should ultimately be some sort of check here that the
     * file is in a/the repository.
     *
     * Upon construction, the pixel buffer is available for reading or writing.
     * However, on the first read, writing will be subsequently disabled.
     *
     * @see <a href="https://trac.openmicroscopy.org/ome/ticket/5083">ticket 5083</a>
     */
    public BfPyramidPixelBuffer(Pixels pixels, String filePath, boolean write)
    throws IOException, FormatException
    {
        this(new ConfiguredTileSizes(), pixels, filePath, write);
    }

    /**
     * Full constructor taking a {@link TileSizes} implementation which defines
     * how large the pyramid tiles will be.
     *
     * @param sizes
     * @param pixels
     * @param filePath
     * @param write
     * @throws IOException
     * @throws FormatException
     */
    public BfPyramidPixelBuffer(TileSizes sizes, Pixels pixels, String filePath, boolean write)
            throws IOException, FormatException
    {
        this(sizes, pixels, filePath, write, true); // init!
    }

    protected BfPyramidPixelBuffer(TileSizes sizes, Pixels pixels, String filePath,
            boolean write, boolean init)
            throws IOException, FormatException
    {
        this.sizes = sizes;
        this.readerFile = new File(filePath);
        this.pixels = pixels;
        if (init) {
            init(filePath, write);
        }
    }

    protected void init(String filePath, boolean write)
            throws IOException, FormatException
    {
        if (!write || readerFile.exists())
        {
            if (write) {
                log.debug("Initialized in a write-context; setting read-only for " + filePath);
            }

            if (!readerFile.exists() && !readerFile.canRead()) {
                throw new IOException("Cannot access " + filePath);
            }
            initializeReader();
        }

        else
        {
            final File readerDir = readerFile.getParentFile();
            writerFile = File.createTempFile("." + readerFile.getName(), ".tmp", readerDir);
            writerFile.deleteOnExit();
            acquireLock();
        }
    }

    /**
     * If the pyramid file exists (which the constructor guarantees) then we
     * assume that even if a lock file is present, that it's no longer valid.
     */
    protected synchronized void initializeReader() throws IOException, FormatException
    {
        File lockFile = lockFile();
        if (readerFile.exists() && lockFile.exists()) {
            // note: we double checked readerFile exists just in case.
            lockFile.delete();
        }
        reader = new OmeroPixelsPyramidReader();
        delegate = new BfPixelBuffer(readerFile.getAbsolutePath(), reader);
        byteOrder = delegate.isLittleEndian()? ByteOrder.LITTLE_ENDIAN
                : ByteOrder.BIG_ENDIAN;
    }

    /**
     * Initializes the writer. Since the reader location is not present until
     * this instance is closed, other {@link BfPyramidPixelBuffer} instances
     * may try to also call this method in which case {@link #acquireLock()}
     * will throw a {@link LockTimeout}.
     *
     * @param output The file where to write the compressed data.
     * @param compression The compression to use.
     * @param bigTiff Pass <code>true</code> to set the <code>bigTiff</code>
     * flag, <code>false</code> otherwise.
     * @throws Exception Thrown if an error occurred.
     */
    protected synchronized void initializeWriter(String output,
                                               String compression,
                                               boolean bigTiff,
                                               int tileWidth, int tileLength)
        throws FormatException
    {
        try
        {
            if (readerFile.exists()) {
                throw new ResourceError(" exists. Pyramid is read-only");
            }
            loci.common.services.ServiceFactory lociServiceFactory =
                new loci.common.services.ServiceFactory();
            OMEXMLService service =
                lociServiceFactory.getInstance(OMEXMLService.class);
            metadata = service.createOMEXMLMetadata();
            addSeries(tileWidth, tileLength);
            writer = new OmeroPixelsPyramidWriter();
            writer.setMetadataRetrieve(metadata);
            writer.setCompression(compression);
            writer.setWriteSequentially(true);
            writer.setInterleaved(true);
            writer.setBigTiff(bigTiff);
            writer.setId(output);
        }
        catch (Exception e)
        {
            throw new FormatException("Error instantiating service.", e);
        }
    }

    /**
     * Creates a new series for the destination metadata store.
     * @param metadata Metadata store and retrieve implementation.
     * @param pixels Source pixels set.
     * @param series Destination series.
     * @param sizeX Destination X width. Not necessarily
     * <code>Pixels.SizeX</code>.
     * @param sizeY Destination Y height. Not necessarily
     * <code>Pixels.SizeY</code>.
     * @throws EnumerationException
     */
    private void createSeries(int series, int sizeX, int sizeY)
        throws EnumerationException
    {
        metadata.setImageID("Image:" + series, series);
        metadata.setPixelsID("Pixels: " + series, series);
        metadata.setPixelsBinDataBigEndian(
                byteOrder == ByteOrder.BIG_ENDIAN? true : false, series, 0);
        metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, series);
        metadata.setPixelsType(ome.xml.model.enums.PixelType.fromString(
                pixels.getPixelsType().getValue()), series);
        metadata.setPixelsSizeX(new PositiveInteger(sizeX), series);
        metadata.setPixelsSizeY(new PositiveInteger(sizeY), series);
        metadata.setPixelsSizeZ(new PositiveInteger(1), series);
        metadata.setPixelsSizeC(new PositiveInteger(1), series);
        int totalPlanes =
            pixels.getSizeZ() * pixels.getSizeC() * pixels.getSizeT();
        metadata.setPixelsSizeT(new PositiveInteger(totalPlanes), series);
        metadata.setChannelID("Channel:" + series, series, 0);
        metadata.setChannelSamplesPerPixel(new PositiveInteger(1), series, 0);
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Added series %d %dx%dx%d",
                    series, sizeX, sizeY, totalPlanes));
        }
    }

    /**
     * During tile writing, adds additional all series.
     * @param tileWidth Tile width of full resolution tiles.
     * @param tileLength Tile length of full resolution tiles.
     * @throws EnumerationException
     */
    private void addSeries(int tileWidth, int tileLength)
        throws EnumerationException
    {
        int series = 0;
        for (int level : new int[] { 0, 5, 4 })
        {
            long imageWidth = pixels.getSizeX();
            long imageLength = pixels.getSizeY();
            long factor = (long) Math.pow(2, level);
            long newTileWidth = Math.round((double) tileWidth / factor);
            newTileWidth = newTileWidth < 1? 1 : newTileWidth;
            long newTileLength = Math.round((double) tileLength / factor);
            newTileLength = newTileLength < 1? 1: newTileLength;
            long evenTilesPerRow = imageWidth / tileWidth;
            long evenTilesPerColumn = imageLength / tileLength;
            double remainingWidth =
                    ((double) (imageWidth - (evenTilesPerRow * tileWidth))) /
                    factor;
            remainingWidth = remainingWidth < 1? Math.ceil(remainingWidth) :
                Math.round(remainingWidth);
            double remainingLength =
              ((double) imageLength - (evenTilesPerColumn * tileLength)) /
              factor;
            remainingLength = remainingLength < 1? Math.ceil(remainingLength) :
                Math.round(remainingLength);
            int newImageWidth = (int) ((evenTilesPerRow * newTileWidth) +
                remainingWidth);
            int newImageLength = (int) ((evenTilesPerColumn * newTileLength) +
                remainingLength);

            createSeries(series, newImageWidth, newImageLength);
            series++;
        }
    }

    protected void acquireLock()
    {
        try {
            lockFile = lockFile();
            lockRaf = new RandomAccessFile(lockFile, "rw");
            fileLock = lockRaf.getChannel().lock(); // THROWS!
        } catch (OverlappingFileLockException overlap) {
            closeRaf();
            throw new LockTimeout("Already locked! " +
                    lockFile.getAbsolutePath(), 15*1000, 0);
        } catch (IOException e) {
            closeRaf();
            throw new LockTimeout("IOException while locking " +
                    lockFile.getAbsolutePath(), 15*1000, 0);
        }
    }

    protected void closeRaf()
    {
        if (lockRaf != null)
        {
            try
            {
                lockRaf.close();
            } catch (Exception e)
            {
                log.warn("Failed to close " + lockFile, e);
            } finally
            {
                    lockRaf = null;
            }
        }
    }

    protected boolean isLockedByOthers()
    {

        if (fileLock != null) {
            return false; // We control the lock.
        }

        // Since we don't control the lock here, we will try to
        // obtain it and release it immediately.

        try {
            lockFile = lockFile();
            lockRaf = new RandomAccessFile(lockFile, "rw");
            try {
                fileLock = lockRaf.getChannel().tryLock();
            } catch (OverlappingFileLockException ofle) {
                // Another object in this JVM controls the lock.
                log.debug("Overlapping file lock exception: " + readerFile);
            }
            if (fileLock == null) {
                // If we don't control the fileLock, then we
                // also don't have the right to delete the
                // lockFile. #5655
                lockFile = null;
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            lockFile = null;
            throw new RuntimeException(e);
        } finally {
            releaseLock();
        }
    }

    private File lockFile() {
        File parent = readerFile.getParentFile();
        String name = "." + readerFile.getName() + PYR_LOCK_EXT;
        File lock = new File(parent, name);
        return lock;
    }

    private void releaseLock()
    {
        try {
            if (fileLock != null) {
                fileLock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            fileLock = null;
            closeRaf();
            if (lockFile != null) {
                lockFile.delete();
                lockFile = null;
            }
        }
    }

    /**
     * This method should never exit without releasing the lock.
     */
    protected void closeWriter() throws IOException
    {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } finally {
            try {
                if (writerFile != null) {
                    try {
                        FileUtils.moveFile(writerFile, readerFile);
                    } finally {
                        writerFile = null;
                    }
                }
            } finally {
                releaseLock();
            }
        }
    }

    /**
     * Whether or not this instance is in writing-mode. Any of the calls to reader
     * methods called while this method returns true will close the writer,
     * saving it to disk and preventing any further write methods.
     */
    public boolean isWrite()
    {
        return writerFile != null;
    }

    private BfPixelBuffer delegate()
    {
        if (isWrite())
        {
            try {
                closeWriter();
                try {
                    initializeReader();
                } catch (FormatException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
        else if (delegate == null)
        {
            try {
                initializeReader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (FormatException e) {
                throw new RuntimeException(e);
            }
        }
        return delegate;
    }

    /* (non-Javadoc)
     * @see ome.io.bioformats.BfPixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized void setTile(byte[] buffer, Integer z, Integer c,
            Integer t, Integer x, Integer y, Integer w, Integer h)
        throws IOException, BufferOverflowException
    {
        if (!isWrite())
        {
            throw new ApiUsageException("In read-only mode!");
        }
        try
        {
            int planeCount = getSizeZ() * getSizeC() * getSizeT();
            int planeNumber = FormatTools.getIndex(
                    "XYZCT", getSizeZ(), getSizeC(), getSizeT(), planeCount,
                    z, c, t);
            IFD ifd = getIFD(z, c, t, w, h);
            if (log.isDebugEnabled())
            {
                log.debug(String.format(
                        "Writing tile planeNumber:%d bufferSize:%d ifd:%s " +
                        "x:%d y:%d w:%d h:%d", planeNumber, buffer.length,
                        ifd.toString(), x, y, w, h));
            }
            writer.saveBytes(planeNumber, buffer, ifd, x, y, w, h);
        }
        catch (FormatException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the IFD that should be used for a given planar offset.
     * @param z Z-section offset requested.
     * @param c Channel offset requested.
     * @param t Timepoint offset requested.
     * @param w Tile width requested.
     * @param h Tile height requested.
     * @return A new or already allocated IFD for use when writing tiles.
     */
    private synchronized IFD getIFD(int z, int c, int t, int w, int h)
    {
        if (lastT == -1 && lastC == -1 && lastZ == -1)
        {
            try
            {
                initializeWriter(writerFile.getAbsolutePath(),
                        TiffCompression.JPEG_2000.getCodecName(), true, w, h);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        if (lastT != t || lastC != c || lastZ != z)
        {
            lastIFD = new IFD();
            lastIFD.put(IFD.IMAGE_DESCRIPTION,
                        OmeroPixelsPyramidWriter.IMAGE_DESCRIPTION);
            lastIFD.put(IFD.TILE_WIDTH, w);
            lastIFD.put(IFD.TILE_LENGTH, h);
            if (log.isDebugEnabled())
            {
                log.debug(String.format(
                        "Creating new IFD z:%d c:%d t:%d w:%d: h:%d -- %s",
                        z, c, t, w, h, lastIFD));
            }
        }
        lastT = t;
        lastC = c;
        lastZ = z;
        return lastIFD;
    }

    /**
     * Retrieves the rasterized timepoint offset based on the linearization of
     * the z-section, channel and timepoint offsets.
     * @param z Z-section offset requested.
     * @param c Channel offset requested.
     * @param t Timepoint offset requested.
     * @return
     */
    private int getRasterizedT(int z, int c, int t)
    {
        int rasterizedT = (t * pixels.getSizeC() * pixels.getSizeZ())  // T
             + (c * pixels.getSizeZ())  // C
             + z;  // Z
        if (log.isDebugEnabled())
        {
            log.debug(String.format("Rasterizing z:%d c:%d t:%d to t:%d",
                    z, c, t, rasterizedT));
        }
        return rasterizedT;
    }

    /**
     * Checks that the tile parameters are not weirdly offset and do not have
     * odd sizes.
     * @param x X offset to the tile request.
     * @param y Y offset to the tile request.
     * @param w Width of the tile request.
     * @param h Height of the tile request.
     * @throws IOException If there is a problem with the parameters or a
     * problem checking them.
     */
    private synchronized void checkTileParameters(int x, int y, int w, int h)
        throws IOException
    {
        // No-op.
    }

    /**
     * Returns the current pixel byte order.
     * @return See above.
     */
    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    /**
     * Sets the pixel byte order.
     * @param byteOrder The pixel byte order to set.
     */
    public void setByteOrder(ByteOrder byteOrder)
    {
        this.byteOrder = byteOrder;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#calculateMessageDigest()
     */
    public synchronized byte[] calculateMessageDigest() throws IOException
    {
        return delegate().calculateMessageDigest();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#checkBounds(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized void checkBounds(Integer x, Integer y, Integer z,
            Integer c, Integer t) throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        delegate().checkBounds(x, y, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#close()
     */
    public synchronized void close() throws IOException
    {
        try
        {
            if (delegate != null)
            {
                delegate.close();
            }
        }
        catch (IOException e)
        {
            log.error("Failure to close delegate.", e);
        }
        delegate = null;

        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                log.warn("Failed to close reader", e);
            } finally {
                reader = null;
            }
        }

        closeWriter();

    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getByteWidth()
     */
    public synchronized int getByteWidth()
    {
        return delegate().getByteWidth();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getCol(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized PixelData getCol(Integer x, Integer z, Integer c,
                                         Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        PixelData data = delegate().getCol(x, z, c, t);
        data.setOrder(byteOrder);
        return data;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getColDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public synchronized byte[] getColDirect(Integer x, Integer z, Integer c,
            Integer t, byte[] buffer)
        throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getColDirect(x, z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getColSize()
     */
    public synchronized Integer getColSize()
    {
        return delegate().getColSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getHypercube(java.util.List, java.util.List, java.util.List)
     */
    public synchronized PixelData getHypercube(List<Integer> offset,
            List<Integer> size, List<Integer> step)
        throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getHypercubeDirect(java.util.List, java.util.List, java.util.List, byte[])
     */
    public synchronized byte[] getHypercubeDirect(List<Integer> offset,
            List<Integer> size, List<Integer> step, byte[] buffer)
        throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getHypercubeSize(java.util.List, java.util.List, java.util.List)
     */
    public synchronized Long getHypercubeSize(List<Integer> offset,
            List<Integer> size, List<Integer> step)
        throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getId()
     */
    public synchronized long getId()
    {
        return delegate().getId();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPath()
     */
    public synchronized String getPath()
    {
        return delegate().getPath();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlane(java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        PixelData data = delegate().getPlane(z, c, t);
        data.setOrder(byteOrder);
        return data;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public synchronized byte[] getPlaneDirect(Integer z, Integer c, Integer t,
                                              byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getPlaneDirect(z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getPlaneOffset(z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneRegion(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized PixelData getPlaneRegion(Integer x, Integer y,
            Integer width, Integer height, Integer z, Integer c, Integer t,
            Integer stride)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        PixelData data =
            delegate().getPlaneRegion(x, y, width, height, z, c, t, stride);
        data.setOrder(byteOrder);
        return data;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneRegionDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public synchronized byte[] getPlaneRegionDirect(Integer z, Integer c,
            Integer t, Integer count, Integer offset, byte[] buffer)
        throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return getPlaneRegionDirect(z, c, t, count, offset, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneSize()
     */
    public synchronized Long getPlaneSize()
    {
        return delegate().getPlaneSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRegion(java.lang.Integer, java.lang.Long)
     */
    public PixelData getRegion(Integer size, Long offset) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRegionDirect(java.lang.Integer, java.lang.Long, byte[])
     */
    public byte[] getRegionDirect(Integer size, Long offset, byte[] buffer)
            throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRow(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized PixelData getRow(Integer y, Integer z, Integer c,
                                         Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        PixelData data = delegate().getRow(y, z, c, t);
        data.setOrder(byteOrder);
        return data;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public synchronized byte[] getRowDirect(Integer y, Integer z, Integer c,
            Integer t, byte[] buffer)
        throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getRowDirect(y, z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized Long getRowOffset(Integer y, Integer z, Integer c,
                                          Integer t)
            throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getRowOffset(y, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowSize()
     */
    public synchronized Integer getRowSize()
    {
        return delegate().getRowSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeC()
     */
    public int getSizeC()
    {
        // Not delegating due to the timepoint rasterization of dimensions
        // that's happening below us.
        return pixels.getSizeC();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeT()
     */
    public int getSizeT()
    {
        // Not delegating due to the timepoint rasterization of dimensions
        // that's happening below us.
        return pixels.getSizeT();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeX()
     */
    public synchronized int getSizeX()
    {
        if (delegate == null || delegate.reader.get() == null)
        {
            // The downstream reader has not been initialized, we don't need to
            // delegate and can't even if we wanted to because no data has
            // actually been written yet.
            return pixels.getSizeX();
        }
        return delegate.getSizeX();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeY()
     */
    public synchronized int getSizeY()
    {
        if (delegate == null || delegate.reader.get() == null)
        {
            // The downstream reader has not been initialized, we don't need to
            // delegate and can't even if we wanted to because no data has
            // actually been written yet.
            return pixels.getSizeY();
        }
        return delegate.getSizeY();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeZ()
     */
    public int getSizeZ()
    {
        // Not delegating due to the timepoint rasterization of dimensions
        // that's happening below us.
        return pixels.getSizeZ();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getStack(java.lang.Integer, java.lang.Integer)
     */
    public PixelData getStack(Integer c, Integer t) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getStackDirect(java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getStackDirect(Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getStackOffset(java.lang.Integer, java.lang.Integer)
     */
    public Long getStackOffset(Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getStackSize()
     */
    public synchronized Long getStackSize()
    {
        return delegate().getStackSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public synchronized PixelData getTile(Integer z, Integer c, Integer t,
            Integer x, Integer y, Integer w, Integer h) throws IOException
    {
        checkTileParameters(x, y, w, h);
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        PixelData data = delegate().getTile(z, c, t, x, y, w, h);
        data.setOrder(byteOrder);
        return data;
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public synchronized byte[] getTileDirect(Integer z, Integer c, Integer t,
            Integer x, Integer y, Integer w, Integer h, byte[] buffer)
        throws IOException
    {
        checkTileParameters(x, y, w, h);
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate().getTileDirect(z, c, t, x, y, w, h, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepoint(java.lang.Integer)
     */
    public synchronized PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepointDirect(java.lang.Integer, byte[])
     */
    public synchronized byte[] getTimepointDirect(Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepointOffset(java.lang.Integer)
     */
    public Long getTimepointOffset(Integer t)
            throws DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepointSize()
     */
    public synchronized Long getTimepointSize()
    {
        return delegate().getTimepointSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTotalSize()
     */
    public synchronized Long getTotalSize()
    {
        return delegate().getTotalSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#isFloat()
     */
    public synchronized boolean isFloat()
    {
        return delegate().isFloat();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#isSigned()
     */
    public synchronized boolean isSigned()
    {
        return delegate().isSigned();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setPlane(java.nio.ByteBuffer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setPlane(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setRegion(java.lang.Integer, java.lang.Long, byte[])
     */
    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setRegion(java.lang.Integer, java.lang.Long, java.nio.ByteBuffer)
     */
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setRow(java.nio.ByteBuffer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setStack(java.nio.ByteBuffer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setStack(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTimepoint(java.nio.ByteBuffer, java.lang.Integer)
     */
    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setTimepoint(byte[], java.lang.Integer)
     */
    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException
    {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevel()
     */
    public synchronized int getResolutionLevel()
    {
        if (isWrite())
        {
            throw new ApiUsageException("In write mode!");
        }
        return delegate().getResolutionLevel();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getResolutionLevels()
     */
    public synchronized int getResolutionLevels()
    {
        if (isWrite())
        {
            throw new ApiUsageException("In write mode!");
        }
        return delegate().getResolutionLevels();
    }

    public synchronized List<List<Integer>> getResolutionDescriptions()
    {
        if (isWrite())
        {
            throw new ApiUsageException("In write mode!");
        }
        return delegate().getResolutionDescriptions();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileSize()
     */
    public synchronized Dimension getTileSize()
    {
        if (isWrite())
        {
            return new Dimension(sizes.getTileWidth(), sizes.getTileHeight());
        }
        return delegate().getTileSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#setResolutionLevel(int)
     */
    public synchronized void setResolutionLevel(int resolutionLevel)
    {
        if (isWrite())
        {
            throw new ApiUsageException("In write mode!");
        }
        delegate().setResolutionLevel(resolutionLevel);
    }
}
