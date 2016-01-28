/*
 *   Copyright 2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.bioformats;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.in.MinimalTiffReader;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffParser;

/**
 * File format reader for OMERO pixels pyramid files.
 * 
 * @author Chris Allan, callan at blackcat dot ca
 * @since Beta4.3
 */
public class OmeroPixelsPyramidReader extends MinimalTiffReader {

    /** Logger for this class. */
    private final static Logger log =
        LoggerFactory.getLogger(OmeroPixelsPyramidReader.class);

   /* (non-Javadoc)
    * @see loci.formats.FormatReader#isThisType(java.lang.String, boolean)
    */
    @Override
    public boolean isThisType(String name, boolean open)
    {
        boolean isThisType = super.isThisType(name, open);
        if (!isThisType && open)
        {
            RandomAccessInputStream stream = null;
            try
            {
                stream = new RandomAccessInputStream(name);
                TiffParser tiffParser = new TiffParser(stream);
                if (!tiffParser.isValidHeader())
                {
                    return false;
                }
                String imageDescription = tiffParser.getFirstIFD()
                        .getIFDTextValue(IFD.IMAGE_DESCRIPTION);
                if (imageDescription != null
                    && imageDescription.startsWith("OmeroPixelsPyramid"))
                {
                    return true;
                }
                return false;
            }
            catch (IOException e)
            {
                log.error("I/O exception during isThisType() evaluation.", e);
                return false;
            }
            finally
            {
                try
                {
                    if (stream != null)
                    {
                        stream.close();
                    }
                }
                catch (IOException e)
                {
                    log.error("I/O exception during stream closure.", e);
                }
            }
        }
        return isThisType;
    }

    /* (non-Javadoc)
     * @see loci.formats.FormatReader#setId(java.lang.String)
     */
    @Override
    public void setId(String id) throws FormatException, IOException
    {
        log.debug("setId(" + id + ")");
        super.setId(id);
    }

    /* (non-Javadoc)
     * @see loci.formats.FormatReader#close()
     */
    @Override
    public void close() throws IOException
    {
        log.debug("close(" + currentId + ")");
        super.close();
    }

    @Override
    protected void setResolutionLevel(IFD ifd)
    {
        if (ifd.get(OmeroPixelsPyramidWriter.IFD_TAG_SERIES) == null)
        {
            super.setResolutionLevel(ifd);
        }
    }
}
