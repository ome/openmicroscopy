/*
 * org.openmicroscopy.shoola.util.image.io.ImageWriter
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.filter.file.GIFFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;

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
    
	/** Indicates to create a <code>JPEG</code>. */
	public static final int JPEG = 0;
	
	/** Indicates to create a <code>PNG</code>. */
	public static final int PNG = 1;
	
	/** Indicates to create a <code>GIF</code>. */
	public static final int GIF = 2;
	
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
    	ImageOutputStream ios = null;
    	try {
    		Iterator<ImageWriter> writers =
    				ImageIO.getImageWritersByFormatName(format);
    		ImageWriter writer = writers.next();
    		ios = ImageIO.createImageOutputStream(f);
			if (ios == null) {
				throw new IOException("Can't access file");
			}
    		writer.setOutput(ios);
    		writer.write(img);
    	} catch (IOException e) {
    		throw new EncoderException("Cannot encode the image.", e);
    	} finally {
			if (ios != null) {
				try {
					ios.close();
				} catch (Exception ex) {}
			}
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
	public static byte[] imageToByteStream(BufferedImage image) 
		throws EncoderException
	{
		return imageToByteStream(image, JPEG);
	}
	
    /**
	 * Converts the BufferImage.
	 * 
	 * @param image The image to convert.
	 * @param type  One of the constants defined by this class.
	 * @return See above.
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static byte[] imageToByteStream(BufferedImage image, int type) 
		throws EncoderException
	{
		if (image == null) 
    		throw new IllegalArgumentException("No image specified.");
		ByteArrayOutputStream stream = null;
		try {
			stream = new ByteArrayOutputStream();
			switch (type) {
				case PNG:
					ImageIO.write(image, PNGFilter.PNG, stream);
					break;
				case GIF:
					ImageIO.write(image, GIFFilter.GIF, stream);
					break;
				case JPEG:
				default:
					ImageIO.write(image, JPEGFilter.JPEG, stream);
			}
			return stream.toByteArray();
		} catch (Throwable e) {
			throw new EncoderException("Cannot encode the image.", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {}
			}
		}
	}
	
	/**
	 * Converts the passed byte array to a buffered image.
	 * 
	 * @param values The values to convert.
	 * @return See above.
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static BufferedImage bytesToImage(byte[] values)
		throws EncoderException
	{
		if (values == null) 
    		throw new IllegalArgumentException("No array specified.");
		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(values);
			BufferedImage image = ImageIO.read(stream);
			if (image != null) image.setAccelerationPriority(1f);
			return image;
		} catch (Exception e) {
			throw new EncoderException("Cannot create buffered image", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {}
			}
		}
	}
	
	/**
	 * Converts the passed byte array to a byte array used to texture.
	 * 
	 * @param values The values to convert.
	 * @return See above.
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static byte[] bytesToBytes(byte[] values)
		throws EncoderException
	{
		if (values == null) 
    		throw new IllegalArgumentException("No array specified.");
		ByteArrayInputStream stream = null;
		try {
			stream = new ByteArrayInputStream(values);
			BufferedImage image = ImageIO.read(stream);
			if (image == null) return null;
			image.setAccelerationPriority(1f);
			int w = image.getWidth();
			int h = image.getHeight();
			WritableRaster raster = Raster.createInterleavedRaster(
					DataBuffer.TYPE_BYTE, w, h, 4, null);
			ComponentColorModel cm =
				new ComponentColorModel (ColorSpace.getInstance(
						ColorSpace.CS_sRGB), new int[] {8, 8, 8, 8}, true,
						false, ComponentColorModel.TRANSLUCENT,
						DataBuffer.TYPE_BYTE);
			BufferedImage img = new BufferedImage(cm, raster, false, null);
 
			Graphics2D g = img.createGraphics();
			g.drawImage(image, null, null);
			DataBufferByte buf = (DataBufferByte) raster.getDataBuffer();
			return buf.getData();
		} catch (Exception e) {
			throw new EncoderException("Cannot create buffered image", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {}
			}
		}
	}
	
	/**
	 * Converts the passed byte array to a buffered image.
	 * 
	 * @param values The values to convert.
	 * @return See above.
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public static int[] bytesToDataBuffer(byte[] values)
		throws EncoderException
	{
		if (values == null) 
    		throw new IllegalArgumentException("No array specified.");
		
		try {
			BufferedImage img = bytesToImage(values);
			if (img == null) return null;
			DataBufferInt buf = (DataBufferInt) img.getData().getDataBuffer();
			return buf.getData();
		} catch (Exception e) {
			throw new EncoderException("Cannot decode the image.", e);
		}
	}

}
