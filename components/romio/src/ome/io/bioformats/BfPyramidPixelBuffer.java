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
    private Pixels pixels;

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
        throws ServiceException, IOException, FormatException
    {
        IMetadata metadata = service.createOMEXMLMetadata();
        writer = new TiffWriter();
        TiffWriter writer = new TiffWriter();
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
        long[] rowPerStrip;
        IFD ifd;
        if (w != 256 || h != 256)
        {
            throw new UnsupportedOperationException(
                    "Only 256x256 tiles are supported.");
        }
        rowPerStrip = new long[1];
        rowPerStrip[0] = h;
        ifd = new IFD();
        ifd.put(IFD.TILE_WIDTH, w);
        ifd.put(IFD.TILE_LENGTH, h);
        ifd.put(IFD.ROWS_PER_STRIP, rowPerStrip);
        try
        {
            int sizeZ = pixels.getSizeZ();
            int sizeC = pixels.getSizeC();
            int sizeT = pixels.getSizeT();
            int planeCount = sizeZ * sizeC * sizeT;
            int planeNumber = FormatTools.getIndex(
                    "XYZCT", sizeZ, sizeC, sizeT, planeCount, z, c, t);
            writer.saveBytes(planeNumber, buffer, ifd, x, y, w, h);
        }
        catch (FormatException e)
        {
            throw new RuntimeException(e);
        }
    }
}
