/*
 * org.openmicroscopy.shoola.util.image.io.ImageWriter
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.image.io;




//Java imports
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


//Third-party libraries
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

//Application-internal dependencies

/** 
 * Utility class to encode images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class WriterImage
{
    
    /**
     * Encodes the specified image. Depending on the specified format
     * a <code>JPEG</code>, <code>PNG</code>, <code>BMP</code> image is created.
     * 
     * @param f The file used to create the output stream.
     * @param img The image to encode.
     * @param format The file format.
     * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
     */
    public static void saveImage(File f, BufferedImage img, String format)
        throws EncoderException
    {
    	if (f == null) 
    		throw new IllegalArgumentException("No file specified.");
    	if (img == null) 
    		throw new IllegalArgumentException("No image specified.");
        try {
            Iterator writers = ImageIO.getImageWritersByFormatName(format);
            ImageWriter writer = (ImageWriter) writers.next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(img);
            ios.close();
        } catch (Exception e) {
            throw new EncoderException("Cannot encode the image.", e);
        }
    }

    /** 
     * Encodes the specified image.
     * 
     * @param encoder The encoder to use. Mustn't be <code>null</code>.
     * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
     */
    public static void saveImage(Encoder encoder)
        throws EncoderException
    {   
    	if (encoder == null)
    		throw new IllegalArgumentException("No encoder specified.");
        try {
            encoder.write();
            encoder.getOutput().close();   
        } catch (Exception e) {
            throw new EncoderException("Cannot encode the image.", e);
        }      
    }  
    
    /**
	 * Converts the BufferImage to <code>JPEG</code> image.
	 * 
	 * @param image The image to convert.
	 * @return See above.
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static byte[] imageToByteStreamAsJPEG(BufferedImage image) 
		throws EncoderException
	{
		if (image == null) 
    		throw new IllegalArgumentException("No image specified.");
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos); 
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
			param.setQuality(1.0f, false); 
			encoder.setJPEGEncodeParam(param); 
			encoder.encode(image); 
			bos.close(); 
			return bos.toByteArray(); 
		} catch (Exception e) {
			throw new EncoderException("Cannot encode the image.", e);
		}  
	}
	
	/**
	 * Converts the passed byte array to a buffered image.
	 * 
	 * @param values The values to convert.
	 * @return See above.
	 *  @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static BufferedImage bytesToImageJPEG(byte[] values)
		throws EncoderException
	{
		if (values == null) 
    		throw new IllegalArgumentException("No array specified.");
		JPEGImageDecoder decoder = 
			JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(values));
		try {
			return decoder.decodeAsBufferedImage();
		} catch (Exception e) {
			throw new EncoderException("Cannot decode the image.", e);
		}
	}
	
	/**
	 * Converts the passed byte array to a buffered image.
	 * 
	 * @param values The values to convert.
	 * @return See above.
	 *  @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static int[] bytesToDataBufferJPEG(byte[] values)
		throws EncoderException
	{
		if (values == null) 
    		throw new IllegalArgumentException("No array specified.");
		
		try {
			BufferedImage img = bytesToImageJPEG(values);
			if (img == null) return null;
			DataBufferInt buf = (DataBufferInt) img.getData().getDataBuffer();
			return buf.getData();
		} catch (Exception e) {
			throw new EncoderException("Cannot decode the image.", e);
		}
	}
	
}
