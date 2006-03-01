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
 * A <code>bitmap</code> encoder.
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
	extends Encoder
{

	/** The size of the header. */
	private int 				headerSize = BMPEncoderCst.FILEHEADER_SIZE + 
											BMPEncoderCst.INFOHEADER_SIZE;
  	/** The number of bits for the info. */
  	private int 				infoBitCount = BMPEncoderCst.BITCOUNT;
    
    /** The size image's information.*/
  	private int 				infoSizeImage = BMPEncoderCst.SIZE_IMAGE;

    /** 
     * Writes the plane.
     * @throws IOException Exception thrown if an error occured during the
     * encoding process.
     */
    private void writeRGB()
        throws IOException
    {
        int pad = 4-((imageWidth*3)% 4);
        if (pad == 4) pad = 0;
        DataBufferByte 
            bufferByte = (DataBufferByte) image.getRaster().getDataBuffer();
        //model chosen          
        byte[] red = bufferByte.getData(Encoder.RED_BAND);
        byte[] green = bufferByte.getData(Encoder.GREEN_BAND);
        byte[] blue = bufferByte.getData(Encoder.BLUE_BAND);
        
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
    
    /** 
     * Writes the file header.
     * @throws IOException Exception thrown if an error occured during the
     * encoding process.
     */
    private void writeFileHeader()
        throws IOException
    {
        output.write(BMPEncoderCst.TYPE);
        output.write(BMPEncoderCst.intToDWord(headerSize));
        output.write(BMPEncoderCst.intToWord(BMPEncoderCst.RESERVED_1));
        output.write(BMPEncoderCst.intToWord(BMPEncoderCst.RESERVED_2));
        output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.OFFBITS));
    }
    
    /** 
     * Writes the info header.
     * @throws IOException Exception thrown if an error occured during the
     * encoding process.
     */
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
        output.write(BMPEncoderCst.intToDWord(0));
        output.write(BMPEncoderCst.intToDWord(BMPEncoderCst.CLR_IMPORTANT));
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param image The image to encode. Mustn't be <code>null</code>.
     * @param output The output stream. Mustn't be <code>null</code>.
     */
	public BMPEncoder(BufferedImage image, DataOutputStream output)
    {
        super(image, output);
    }
    
	/** 
     * Writes the image. 
     * @see Encoder#write()
     */
	public void write()
		throws EncoderException
	{
        try {
            writeFileHeader();
            writeInfoHeader();
            writeRGB();
        } catch (IOException e) {
            throw new EncoderException("Cannot encode the image.", e);
        }
	}
    
    /**
     * Initializes the values needed by this encoder
     * @see Encoder#init()
     */
    public void init()
    {
        infoSizeImage = imageWidth*imageHeight*3
                        +(4-((imageWidth*3)%4))*imageHeight;
        headerSize = infoSizeImage + BMPEncoderCst.FILEHEADER_SIZE + 
                            BMPEncoderCst.INFOHEADER_SIZE;
    }
    
}
