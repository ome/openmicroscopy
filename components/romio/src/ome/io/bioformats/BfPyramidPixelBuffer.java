/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.bioformats;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.in.TiffReader;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import loci.formats.tiff.IFDList;
import loci.formats.tiff.TiffCompression;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
import ome.util.PixelData;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.primitives.PositiveInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link PixelBuffer} implementation which uses Bio-Formats to
 * read pixels data directly from original files.
 *
 * @since OMERO-Beta4.3
 */
public class BfPyramidPixelBuffer implements PixelBuffer {

    private final static Log log = LogFactory.getLog(BfPyramidPixelBuffer.class);

    private final BfPixelBuffer delegate;

    /** Bio-Formats implementation used to write to the backing TIFF. */
    protected TiffWriter writer;

    /**
     * Bio-Formats implementation the delegate uses to read the backing TIFF.
     */
    protected TiffReader reader;

    /** Service to create the metadata store. */
    private OMEXMLService service;

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

    /**
     * We may want a constructor that takes the id of an imported file
     * or that takes a File object?
     * There should ultimately be some sort of check here that the
     * file is in a/the repository.
     */
    public BfPyramidPixelBuffer(Pixels pixels, String filePath)
        throws IOException, FormatException
    {
        reader = new TiffReader();
        delegate = new BfPixelBuffer(filePath, reader);
        this.pixels = pixels;
        try
        {
            loci.common.services.ServiceFactory lociServiceFactory =
                    new loci.common.services.ServiceFactory();
            service = lociServiceFactory.getInstance(OMEXMLService.class);
            initializeWriter(filePath, TiffCompression.JPEG_2000.getCodecName(),
                             false);
        }
        catch (Exception e)
        {
            throw new FormatException("Error instantiating service.", e);
        }
    }

    /**
     * Initializes the writer.
     * @param output The file where to write the compressed data.
     * @param compression The compression to use.
     * @param bigTiff Pass <code>true</code> to set the <code>bigTiff</code>
     * flag, <code>false</code> otherwise.
     * @throws Exception Thrown if an error occurred.
     */
    private void initializeWriter(String output, String compression,
                                        boolean bigTiff)
        throws ServiceException, IOException, FormatException, EnumerationException
    {
        IMetadata metadata = service.createOMEXMLMetadata();
        metadata.setImageID("Image:0", 0);
        metadata.setPixelsID("Pixels:0", 0);
        metadata.setPixelsBinDataBigEndian(true, 0, 0);
        metadata.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
        metadata.setPixelsType(ome.xml.model.enums.PixelType.fromString(
                pixels.getPixelsType().getValue()), 0);
        metadata.setPixelsSizeX(new PositiveInteger(pixels.getSizeX()), 0);
        metadata.setPixelsSizeY(new PositiveInteger(pixels.getSizeY()), 0);
        metadata.setPixelsSizeZ(new PositiveInteger(1), 0);
        metadata.setPixelsSizeC(new PositiveInteger(1), 0);
        metadata.setPixelsSizeT(new PositiveInteger(
                pixels.getSizeZ() * pixels.getSizeC() * pixels.getSizeT()), 0);
        metadata.setChannelID("Channel:0", 0, 0);
        metadata.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
        writer = new TiffWriter();
        writer.setMetadataRetrieve(metadata);
        writer.setCompression(compression);
        writer.setWriteSequentially(true);
        writer.setInterleaved(true);
        writer.setBigTiff(bigTiff);
        writer.setId(output);
    }

    /* (non-Javadoc)
     * @see ome.io.bioformats.BfPixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        try
        {
            int planeCount = getSizeZ() * getSizeC() * getSizeT();
            int planeNumber = FormatTools.getIndex(
                    "XYZCT", getSizeZ(), getSizeC(), getSizeT(), planeCount,
                    z, c, t);
            writer.saveBytes(planeNumber, buffer, getIFD(z, c, t, w, h),
                             x, y, w, h);
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
    private IFD getIFD(int z, int c, int t, int w, int h)
    {
        if (lastT != t || lastC != c || lastZ != z)
        {
            lastIFD = new IFD();
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
    private void checkTileParameters(int x, int y, int w, int h)
        throws IOException
    {
        // Ensure the reader has been initialized
        delegate.reader();
        IFDList ifds = reader.getIFDs();
        if (ifds.size() == 0)
        {
            throw new IOException("Backing reader has no IFDs!");
        }
        IFD firstIFD = ifds.get(0);
        int tileWidth, tileHeight;
        try
        {
            tileWidth = (int) firstIFD.getTileWidth();
            tileHeight = (int) firstIFD.getTileLength();
        }
        catch (FormatException e)
        {
            String message = "Error retrieving tile width and height!";
            log.error(message, e);
            throw new IOException(message);
        }
        if (x % tileWidth != 0)
        {
            throw new IOException(String.format(
                    "Tile X offset %d not a multiple of tile width %d",
                    x, tileWidth));
        }
        if (y % tileHeight != 0)
        {
            throw new IOException(String.format(
                    "Tile Y offset %d not a multiple of tile width %d",
                    x, tileWidth));
        }
        if (w > tileWidth)
        {
            throw new IOException(String.format(
                    "Requested tile width %d larger than tile width %d",
                    w, tileWidth));
        }
        if (h > tileHeight)
        {
            throw new IOException(String.format(
                    "Requested tile height %d larger than tile height %d",
                    w, tileHeight));
        }
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#calculateMessageDigest()
     */
    public byte[] calculateMessageDigest() throws IOException
    {
        return delegate.calculateMessageDigest();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#checkBounds(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public void checkBounds(Integer x, Integer y, Integer z, Integer c,
            Integer t) throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        delegate.checkBounds(x, y, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#close()
     */
    public void close() throws IOException
    {
        try
        {
            delegate.close();
        }
        finally
        {
            writer.close();
        }
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getByteWidth()
     */
    public int getByteWidth()
    {
        return delegate.getByteWidth();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getCol(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getCol(Integer x, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getCol(x, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getColDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getColDirect(Integer x, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getColDirect(x, z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getColSize()
     */
    public Integer getColSize()
    {
        return delegate.getColSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getHypercube(java.util.List, java.util.List, java.util.List)
     */
    public PixelData getHypercube(List<Integer> offset, List<Integer> size,
            List<Integer> step) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getHypercubeDirect(java.util.List, java.util.List, java.util.List, byte[])
     */
    public byte[] getHypercubeDirect(List<Integer> offset, List<Integer> size,
            List<Integer> step, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getId()
     */
    public long getId()
    {
        return delegate.getId();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPath()
     */
    public String getPath()
    {
        return delegate.getPath();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlane(java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getPlane(Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getPlane(z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getPlaneDirect(Integer z, Integer c, Integer t, byte[] buffer)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getPlaneDirect(z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public Long getPlaneOffset(Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getPlaneOffset(z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneRegion(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getPlaneRegion(Integer x, Integer y, Integer width,
            Integer height, Integer z, Integer c, Integer t, Integer stride)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getPlaneRegion(x, y, width, height, z, c, t, stride);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneRegionDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getPlaneRegionDirect(Integer z, Integer c, Integer t,
            Integer count, Integer offset, byte[] buffer) throws IOException,
            DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return getPlaneRegionDirect(z, c, t, count, offset, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getPlaneSize()
     */
    public Integer getPlaneSize()
    {
        return delegate.getPlaneSize();
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
    public PixelData getRow(Integer y, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getRow(y, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getRowDirect(Integer y, Integer z, Integer c, Integer t,
            byte[] buffer) throws IOException, DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getRowDirect(y, z, c, t, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowOffset(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public Long getRowOffset(Integer y, Integer z, Integer c, Integer t)
            throws DimensionsOutOfBoundsException
    {
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getRowOffset(y, z, c, t);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getRowSize()
     */
    public Integer getRowSize()
    {
        return delegate.getRowSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeC()
     */
    public int getSizeC()
    {
        return pixels.getSizeC();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeT()
     */
    public int getSizeT()
    {
        return pixels.getSizeT();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeX()
     */
    public int getSizeX()
    {
        return pixels.getSizeX();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeY()
     */
    public int getSizeY()
    {
        return pixels.getSizeY();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getSizeZ()
     */
    public int getSizeZ()
    {
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
    public Integer getStackSize()
    {
        return delegate.getStackSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTile(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    public PixelData getTile(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h) throws IOException
    {
        checkTileParameters(x, y, w, h);
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getTile(z, c, t, x, y, w, h);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTileDirect(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, byte[])
     */
    public byte[] getTileDirect(Integer z, Integer c, Integer t, Integer x,
            Integer y, Integer w, Integer h, byte[] buffer) throws IOException
    {
        checkTileParameters(x, y, w, h);
        t = getRasterizedT(z, c, t);
        c = 0;
        z = 0;
        return delegate.getTileDirect(z, c, t, x, y, w, h, buffer);
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepoint(java.lang.Integer)
     */
    public PixelData getTimepoint(Integer t) throws IOException,
            DimensionsOutOfBoundsException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTimepointDirect(java.lang.Integer, byte[])
     */
    public byte[] getTimepointDirect(Integer t, byte[] buffer)
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
    public Integer getTimepointSize()
    {
        return delegate.getTimepointSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#getTotalSize()
     */
    public Integer getTotalSize()
    {
        return delegate.getTotalSize();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#isFloat()
     */
    public boolean isFloat()
    {
        return delegate.isFloat();
    }

    /* (non-Javadoc)
     * @see ome.io.nio.PixelBuffer#isSigned()
     */
    public boolean isSigned()
    {
        return delegate.isSigned();
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
}
