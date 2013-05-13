/*
 * org.openmicroscopy.shoola.util.image.io.Encoder
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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;

//Third-party libraries

//Application-internal dependencies

/** 
 * Top class for encoder. Sub-classes override the {@link #write()} and 
 * {@link #init()} methods.
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
public abstract class Encoder
{
	
    /** Identifies the <code>RED</code> color band. */
    static final int    RED_BAND = 0;
    
    /** Identifies the <code>GREEN</code> color band. */
    static final int    GREEN_BAND = 1;
    
    /** Identifies the <code>BLUE</code> color band. */
    static final int    BLUE_BAND = 2;
    
    /** The output stream. */
    protected DataOutputStream      output;
    
    /** The image to encode. */
    protected BufferedImage         image;

    /** The width of the image. */
    protected int                   imageWidth;
    
    /** The height of the image. */
    protected int                   imageHeight;
    
    /** The color type of the image. */
    protected int                   colorType;
    
    /** 
     * Controls if the color model associated to the specified image is
     * supported. Note that the only supported models are Gray and RGB.
     * 
     * @param img The image to encode.
     * @return The color type.
     * */
    private int checkColorModel(BufferedImage img)
    {
        int c = img.getColorModel().getColorSpace().getType();
        if (c != ColorSpace.TYPE_RGB && 
            c != ColorSpace.TYPE_GRAY)
            throw new IllegalArgumentException("Color Type not supported");
        return c;
    }
    
    /**
     * Initializes the encoder. Sub-classes will set the constants required.
     * 
     * @param image The image to transfer.
     * @param output The stream to write the transformed image
     */
	public Encoder(BufferedImage image, DataOutputStream output)
    {
        if (output == null) 
        	throw new IllegalArgumentException("Output not valid");
        if (image == null)  
        	throw new IllegalArgumentException("Image to encode not valid");
        colorType = checkColorModel(image);
        this.image = image;
        this.imageHeight = image.getWidth();
        this.imageWidth = image.getHeight();
        this.output = output;
        init();
    }
		
    /** 
     * Returns the output stream.
     * 
     * @return See above.
     */
    public DataOutputStream getOutput() { return output; }
    
    /**
     * Writes the encoded image. Sub-classes override this method.
     * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
     */
	public abstract void write()
		throws EncoderException;
		
    /** Initializes the values. Sub-classes override this method. */
    protected abstract void init();
    
}
