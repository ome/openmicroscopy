/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.IOException;

import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public interface ImportReader extends IFormatReader {

    /**
     * Obtains an object which represents a given plane within the file.
     * 
     * @param id
     *            The path to the file.
     * @param planeNumber
     *            The plane or section within the file to obtain.
     * @param buf
     *            Pre-allocated buffer which has a <i>length</i> that can fit
     *            the byte count of an entire plane.
     * @return an object which represents the plane.
     * @throws FormatException
     *             if there is an error parsing the file.
     * @throws IOException
     *             if there is an error reading from the file or acquiring
     *             permissions to read the file.
     */
    public Plane2D openPlane2D(String id, int planeNumber, byte[] buf)
            throws FormatException, IOException;

    /**
     * Returns whether or not the reader for a given file is a screening format
     * or not.
     * 
     * @param string
     *            Absolute path to the image file to check.
     * @return <code>true</code> if the reader is an <i>SPW</i> reader and
     *         <code>false</code> otherwise.
     */
    public boolean isSPWReader(String string);

    public boolean isMinMaxSet() throws FormatException, IOException;

    /**
     * Return the base image reader
     * 
     * @return See above.
     */
    public ImageReader getImageReader();

    public void setMinMaxStore(MinMaxCalculator store);

}
