/*
 * org.openmicroscopy.shoola.util.image.io.TIFFEncoder
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.image.io;

//Java imports
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataOutputStream;
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies

/** 
 * Save a buffered image as an uncompressed, big-Endian TIFF.
 * We use the band interleaving model to retrieve the pixel value.

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
public class TIFFEncoder
{
    
	public static final String	FORMAT_EXTENSION = "tiff";
								
	private DataOutputStream	output;
	
	private BufferedImage		image;
	private int					colorType;
	
	private int 				bitsPerSample, samplesPerPixel , nEntries , 
								photoInterp, ifdSize, imageSize;
	private int					imageWidth, imageHeight;
	
	public TIFFEncoder(BufferedImage image, DataOutputStream output)
		throws IllegalArgumentException
	{
		checkColorModel(image);
		checkOutput(output);
		this.image = image;
		this.output = output;
		init();
	}
	
	/** Save the image as an uncompressed big-endian TIFF. */
	public void write()
		throws IOException
	{
		writeHeader();
		writeIFD();
		int bpsSize = 0, scaleSize;
		if (colorType == ColorSpace.TYPE_RGB) {
			writeBitsPerPixel();
			bpsSize = TIFFEncoderCst.BPS_DATA_SIZE;
		}
		scaleSize = TIFFEncoderCst.SCALE_DATA_SIZE;
		int size = TIFFEncoderCst.IMAGE_START-
					(TIFFEncoderCst.HDR_SIZE+ifdSize+bpsSize+scaleSize);
		output.write(new byte[size]); // force image to start at offset 768
		if (colorType == ColorSpace.TYPE_RGB)	writeRGBPixels();
		else  writeGrayPixels();	
	}
	
	/** Initialize the values. */
	private void init()
	{
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		bitsPerSample = 8;
		samplesPerPixel = 1;
		nEntries = 12;
		photoInterp = 1;
		ifdSize = 6 + nEntries*12;
		int bytesPerPixel = 1;
		
		if (colorType  == ColorSpace.TYPE_RGB) {
			photoInterp = 2;
			samplesPerPixel = 3;
			bytesPerPixel = 3;
		}
		imageSize = imageWidth*imageHeight*bytesPerPixel;
	}
	
	/** Check if the output is valid. */
	private void checkOutput(DataOutputStream output)
		throws IllegalArgumentException
	{
		if (output == null) 
			new IllegalArgumentException("Output not valid");
	}
	
	/** Check if we support the model, we only support Gray and RGB. */
	private void checkColorModel(BufferedImage img)
		throws IllegalArgumentException
	{
		colorType = img.getColorModel().getColorSpace().getType();
		if (colorType != ColorSpace.TYPE_RGB && 
			colorType != ColorSpace.TYPE_GRAY)
			throw new IllegalArgumentException("Color Type not supported");
	}
	
	/** Write the 6 bytes of data required by RGB BitsPerSample tag. */
	private void writeBitsPerPixel()
		throws IOException 
	{
		output.writeShort(8);
		output.writeShort(8);
		output.writeShort(8);
	}
	
	/** 
	 * Writes the 16 bytes of data required by the XResolution and 
	 * YResolution tags.
	 */
	private void  writeScale(DataOutputStream out) 
		throws IOException
	{
		//TODO: should be modified if the pixel.width and height != 1
		int scale = 1000000;
		out.writeInt(scale);
		out.writeInt(scale);
		out.writeInt(scale);
		out.writeInt(scale);
	}

	/** Write the header. */
	private void writeHeader()
		throws IOException
	{
		output.write(TIFFEncoderCst.header);
	}
     
	/** Write one Image File Directory. */
	private void writeIFD()
		throws IOException
	{
		int tagDataOffset = TIFFEncoderCst.HDR_SIZE + ifdSize;
		output.writeShort(nEntries);
		writeEntry(TIFFEncoderCst.NEW_SUBFILE_TYPE, 4, 1, 0);
		writeEntry(TIFFEncoderCst.IMAGE_WIDTH, 3, 1, imageWidth);
		writeEntry(TIFFEncoderCst.IMAGE_LENGTH, 3, 1, imageHeight);
		if (colorType == ColorSpace.TYPE_RGB) {
			writeEntry(TIFFEncoderCst.BITS_PER_SAMPLE,  3, 3, tagDataOffset);
			tagDataOffset += TIFFEncoderCst.BPS_DATA_SIZE;
		} else {
			writeEntry(TIFFEncoderCst.BITS_PER_SAMPLE,  3, 1, bitsPerSample);
		}
		writeEntry(TIFFEncoderCst.PHOTO_INTERP, 3, 1, photoInterp);
		writeEntry(TIFFEncoderCst.STRIP_OFFSETS, 4, 1,
					TIFFEncoderCst.IMAGE_START);
		writeEntry(TIFFEncoderCst.SAMPLES_PER_PIXEL, 3, 1, samplesPerPixel);
		writeEntry(TIFFEncoderCst.ROWS_PER_STRIP,   3, 1, imageHeight);
		writeEntry(TIFFEncoderCst.STRIP_BYTE_COUNT, 4, 1, imageSize);
		
		/** X resolution info, Y resolution info.. */
		writeEntry(TIFFEncoderCst.X_RESOLUTION, 5, 1, tagDataOffset);
		writeEntry(TIFFEncoderCst.Y_RESOLUTION, 5, 1, tagDataOffset+8);
		int unit = 2;	//TODO: support all unit
		writeEntry(TIFFEncoderCst.RESOLUTION_UNIT, 3, 1, unit);
		output.writeInt(0);	// only one image
	}
	
	/** Writes one 12-byte IFD entry. */
	void writeEntry(int tag, int fieldType, int count, int value) 
		throws IOException
	{
		output.writeShort(tag);
		output.writeShort(fieldType);
		output.writeInt(count);
		if (count == 1 && fieldType == TIFFEncoderCst.SHORT)
			value <<= 16; //left justify 16-bit values
		output.writeInt(value); // may be an offset
	}

	/** Write the pixel value, band model. */
	private void writeGrayPixels()
		throws IOException
	{
		int bytesWritten = 0;
		int size = imageWidth*imageHeight;
		int count = 8192;
		DataBufferByte buffer = 
						(DataBufferByte) image.getRaster().getDataBuffer();
		byte[] pixels = buffer.getData();
		while (bytesWritten<size) {
			if ((bytesWritten + count) > size)
				count = size - bytesWritten;
			output.write(pixels, bytesWritten, count);
			bytesWritten += count;
		}
	}
	
	/** Write the pixel value, band model. */
	private void writeRGBPixels()
		throws IOException
	{
		int bytesWritten = 0;
		int size = imageWidth*imageHeight*3;
		int count = imageWidth*24;		//3*8
		byte[] buffer = new byte[count];
		int i, j;
		DataBufferByte bufferByte = 
						(DataBufferByte) image.getRaster().getDataBuffer();
		//model chosen			
		byte[] red = bufferByte.getData(TIFFEncoderCst.RED_BAND);
		byte[] green = bufferByte.getData(TIFFEncoderCst.GREEN_BAND);
		byte[] blue = bufferByte.getData(TIFFEncoderCst.BLUE_BAND);
		while (bytesWritten < size) {
			if ((bytesWritten + count) > size)
				count = size - bytesWritten;
			j = bytesWritten/3;
			//TIFF save as BRG and not RGB.
			for (i = 0; i < count; i += 3) {
				buffer[i]   = (byte) blue[j];	//blue
				buffer[i+1] = (byte) red[j];	//red
				buffer[i+2] = (byte) green[j];	//green
				j++;
			}
			output.write(buffer, 0, count);
			bytesWritten += count;
		}
	}

}
