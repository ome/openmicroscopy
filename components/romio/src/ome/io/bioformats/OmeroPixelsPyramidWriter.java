/*
 *   $Id$
 *
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.bioformats;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loci.formats.FormatException;
import loci.formats.in.TiffReader;
import loci.formats.out.TiffWriter;
import loci.formats.tiff.IFD;

/**
 * File format writer for OMERO pixels pyramid files.
 * 
 * @author Chris Allan, callan at blackcat dot ca
 * @since Beta4.3
 */
public class OmeroPixelsPyramidWriter extends TiffWriter {

    /** Logger for this class. */
    private final static Logger log =
        LoggerFactory.getLogger(OmeroPixelsPyramidWriter.class);

    /** Current TIFF image comment for OMERO pixels pyramid TIFFs. */
    public static final String IMAGE_DESCRIPTION = "OmeroPixelsPyramid v1.0.0";

    /** TIFF tag we're using to store the Bio-Formats series. */
    public static final int IFD_TAG_SERIES = 65000;

    /** TIFF tag we're using to store the Bio-Formats plane number. */
    public static final int IFD_TAG_PLANE_NUMBER = 65001;

    /* (non-Javadoc)
     * @see loci.formats.out.TiffWriter#close()
     */
    @Override
    public void close() throws IOException
    {
        log.debug("close(" + currentId + ")");
        try
        {
            if (currentId != null)
            {
                postProcess();
            }
        } catch (FormatException e)
        {
            String m = "Error during process processing!";
            log.error(m, e);
            throw new IOException(m);
        } finally
        {
            super.close();
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.FormatWriter#setId(java.lang.String)
     */
    @Override
    public void setId(String id) throws FormatException, IOException
    {
        log.debug("setId(" + id + ")");
        super.setId(id);
    }

    /**
     * Performs re-compression post processing on the pixel pyramid.
     * @throws IOException
     * @throws FormatException
     */
    protected void postProcess() throws IOException, FormatException
    {
        TiffReader reader = new TiffReader();
        try
        {
            reader.setId(currentId);
            // First we want to re-compress resolution level 0 (the source series,
            // with resolution levels exposed, are in reverse order).
            recompressSeries(reader, 1);
            // Second we want to re-compress resolution level 1 (the source series,
            // with resolution levels exposed, are in reverse order).
            recompressSeries(reader, 2);
        } finally {
            reader.close();
        }
    }

    /**
     * Re-compresses a source series, that is JPEG 2000 compressed, via its
     * resolution level.
     * @param source Reader created of ourselves.
     * @param series Target series for the re-compressed data which is the
     * inverse of the source resolution level.
     * @throws FormatException
     * @throws IOException
     */
    protected void recompressSeries(TiffReader source, int series)
        throws FormatException, IOException
    {
        int sourceSeries = source.getSeriesCount() - series;
        source.setSeries(sourceSeries);
        int imageCount = source.getImageCount();
        setSeries(series);
        for (int i = 0; i < imageCount; i++)
        {
            byte[] plane = source.openBytes(i);
            IFD ifd = new IFD();
            // Ensure that we're compressing all rows of the image in a single
            // JPEG 2000 block.
            ifd.put(IFD.ROWS_PER_STRIP, new long[] { source.getSizeY() });
            // Set the TIFF image description so that we are able to
            // differentiate ourselves from basic TIFFs.
            ifd.put(IFD.IMAGE_DESCRIPTION, IMAGE_DESCRIPTION);
            // First re-usable TIFF IFD (series)
            ifd.put(IFD_TAG_SERIES, sourceSeries - 1);
            // Second re-usable TIFF IFD (plane number)
            ifd.put(IFD_TAG_PLANE_NUMBER, i);
            saveBytes(i, plane, ifd);
        }
    }
}
