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

import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.in.TiffReader;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffCompression;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.model.core.Pixels;
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
public class BfPyramidPixelBuffer extends BfPixelBuffer {

    private final static Log log = LogFactory.getLog(BfPyramidPixelBuffer.class);

    /** Bio-Formats implementation used to write to the backing TIFF. */
    protected TiffWriter writer;

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
        super(filePath, new TiffReader());
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

    @Override
    public void setPlane(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setPlane(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setRegion(Integer size, Long offset, byte[] buffer)
            throws IOException, BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setRegion(Integer size, Long offset, ByteBuffer buffer)
            throws IOException, BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setRow(ByteBuffer buffer, Integer y, Integer z, Integer c,
            Integer t) throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setStack(ByteBuffer buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setStack(byte[] buffer, Integer z, Integer c, Integer t)
            throws IOException, DimensionsOutOfBoundsException,
            BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setTimepoint(ByteBuffer buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    @Override
    public void setTimepoint(byte[] buffer, Integer t) throws IOException,
            DimensionsOutOfBoundsException, BufferOverflowException {
        throw new UnsupportedOperationException(
                "Non-tile based writing unsupported.");
    }

    /* (non-Javadoc)
     * @see ome.io.bioformats.BfPixelBuffer#setTile(byte[], java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public void setTile(byte[] buffer, Integer z, Integer c, Integer t, Integer x, Integer y,
            Integer w, Integer h) throws IOException,
            BufferOverflowException
    {
        try
        {
            int sizeZ = pixels.getSizeZ();
            int sizeC = pixels.getSizeC();
            int sizeT = pixels.getSizeT();
            int planeCount = sizeZ * sizeC * sizeT;
            int planeNumber = FormatTools.getIndex(
                    "XYZCT", sizeZ, sizeC, sizeT, planeCount, z, c, t);
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
}
