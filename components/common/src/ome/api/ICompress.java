/*
 *   $Id: ICompres.java 1167 2006-12-15 10:39:34Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides methods for performing scaling (change of the image size through
 * interpolation or other means) on BufferedImages.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev: 1167 $ $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $) </small>
 * @since 3.0
 */
public interface ICompress extends ServiceInterface {

    /**
     * Compresses a buffered image thumbnail to an output stream.
     * 
     * @param image
     *            the thumbnail's buffered image.
     * @param outputStream
     *            the stream to write to.
     * @throws IOException
     *             if there is a problem when writing to <i>stream<i>.
     */
	void compressThumbnailToStream(BufferedImage image, OutputStream outputStream) throws IOException ;
	
}
