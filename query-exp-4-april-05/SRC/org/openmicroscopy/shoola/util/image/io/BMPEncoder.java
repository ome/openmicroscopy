/*
 * org.openmicroscopy.shoola.util.image.io.BMPEncoder
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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataOutputStream;
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies

/** 
 * A bitmap encoder.
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
public class BMPEncoder 
	implements Encoder
{
								
	private DataOutputStream	output;
	
	private BufferedImage		image;
	
	private int					imageWidth, imageHeight;
	
	//file header
	private int 				headerSize = BMPEncoderCst.FILEHEADER_SIZE + 
											BMPEncoderCst.INFOHEADER_SIZE;
  	//info header
  	private int 				infoBitCount = BMPEncoderCst.BITCOUNT;
  	private int 				infoSizeImage = BMPEncoderCst.SIZE_IMAGE;
  	private int 				infoClrUsed = 0;
  	
	/** Initializate the encoder. */
	public void initialization(BufferedImage image, DataOutputStream output)
		throws IllegalArgumentException
	{
		EncoderUtils.checkColorModel(image);
		EncoderUtils.checkOutput(output);
		this.image = image;
		this.output = output;
		init();
	}
	
	/** Write the image. */
	public void write()
		throws IOException
	{
		writeFileHeader();
		writeInfoHeader();
		writeRGB();
	}
	
	/** Initialize the values. */
	private void init()
	{
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		infoSizeImage = imageWidth*imageHeight*3
						+(4-((imageWidth*3)%4))*imageHeight;
		headerSize = infoSizeImage + BMPEncoderCst.FILEHEADER_SIZE + 
							BMPEncoderCst.INFOHEADER_SIZE;
	}

	/** 
	 * Write the plane.
	 * @throws IOException
	 */
	private void writeRGB()
		throws IOException
	{
		int pad = 4-((imageWidth*3)% 4);
		if (pad == 4) pad = 0;
		DataBufferByte 
			bufferByte = (DataBufferByte) image.getRaster().getDataBuffer();
		//model chosen			
		byte[] red = bufferByte.getData(EncoderUtils.RED_BAND);
		byte[] green = bufferByte.getData(EncoderUtils.GREEN_BAND);
		byte[] blue = bufferByte.getData(EncoderUtils.BLUE_BAND);
		
		int i, v, row, col;
	  	for (row = imageHeight; row > 0; row--) {
			for (col = 0; col < imageWidth; col++) {
				v = (row-1)*imageWidth+col;
				output.write(blue[v]);
				output.write(green[v]);
		  		output.write(red[v]);				
			}
		   	for (i = 1; i <= pad; i++) output.write(0x00);
	   }
	}
	
	/** Write the file header. */
	private void writeFileHeader()
		throws IOException
	{
		output.write(BMPEncoderCst.TYPE);
		output.write(BMPEncoderCst.intToDWord(headerSize));
		output.write(BMPEncoderCst.intToWord(BMPEncoderCst.RESERVED_1));
		output.write(BMPEncoderCst.intToWord(BMPEncoderCst.RESERVED_2));
		output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.OFFBITS));
	}
	
	/** Write the information header. */
	private void writeInfoHeader()
		throws IOException
	{
		output.write(BMPEncoderCst.intToDWord(
						BMPEncoderCst.INFOHEADER_SIZE));
		output.write(BMPEncoderCst.intToDWord(imageWidth));
		output.write(BMPEncoderCst.intToDWord(imageHeight));
		output.write(BMPEncoderCst.intToWord(BMPEncoderCst.PLANES));
		output.write(BMPEncoderCst.intToWord(infoBitCount));
		output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.COMPRESSION));
		output.write(BMPEncoderCst.intToDWord(infoSizeImage));
		output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.XPELSPERMETER));
		output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.YPELSPERMETER));
		output.write(BMPEncoderCst.intToDWord(infoClrUsed));
		output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.CLR_IMPORTANT));
	}
	
}
