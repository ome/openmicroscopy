/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import ome.api.local.LocalCompress;

public class CompressImpl implements LocalCompress {

	/** The default compression quality in fractional percent. */
    private float quality = 0.85F;
	
    /* (non-Javadoc)
     * @see ome.api.ICompress#compressToStream(java.awt.image.BufferedImage, java.io.OutputStream)
     */
    public void compressToStream(BufferedImage image, OutputStream outputStream)
    	throws IOException
    {
        // Get a JPEG image writer
        ImageWriter jpegWriter =
        	ImageIO.getImageWritersByFormatName("jpeg").next();

        // Setup the compression value from (0.05, 0.75 and 0.95)
        ImageWriteParam iwp = jpegWriter.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);

        // Write the JPEG to our ByteArray stream
    	ImageOutputStream imageOutputStream = null;
        try {
        	imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        	jpegWriter.setOutput(imageOutputStream);
        	jpegWriter.write(null, new IIOImage(image, null, null), iwp);
        } finally {
        	if (imageOutputStream != null)
        		imageOutputStream.close();
        }
    }

	/* (non-Javadoc)
	 * @see ome.api.ICompress#setCompressionLevel(float)
	 */
	public void setCompressionLevel(float percentage)
	{
		quality = percentage;
	}
	
	/* (non-Javadoc)
	 * @see ome.api.ICompress#getCompressionLevel()
	 */
	public float getCompressionLevel()
	{
		return quality;
	}
}
