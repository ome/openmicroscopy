/*
 * org.openmicroscopy.shoola.util.image.io.Encoder
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
    protected DataOutputStream    output;
    
    /** The image to encode. */
    protected BufferedImage       image;

    /** The width of the image. */
    protected int                 imageWidth;
    
    /** The height of the image. */
    protected int                 imageHeight;
    
    /** 
     * Controls if the color model associated to the specified image is
     * supported. Note that the only supported models are Gray and RGB.
     * 
     * @param img The image to encode.
     * @return The color type.
     * */
    private int checkColorModel(BufferedImage img)
    {
        int colorType = img.getColorModel().getColorSpace().getType();
        if (colorType != ColorSpace.TYPE_RGB && 
            colorType != ColorSpace.TYPE_GRAY)
            throw new IllegalArgumentException("Color Type not supported");
        return colorType;
    }
    
    /**
     * Initializes the encoder. Sub-classes will set the constants required.
     * 
     * @param image The image to transfer.
     * @param output The stream to write the transformed image
     */
	public Encoder(BufferedImage image, DataOutputStream output)
    {
        if (output == null) new IllegalArgumentException("Output not valid");
        if (image == null)  
            new IllegalArgumentException("Image to encode not valid");
        checkColorModel(image);
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
     * @throws EncoderException Exception thrown if an error occured during the
     * encoding process.
     */
	public abstract void write()
		throws EncoderException;
		
    /** Initializes the values. Sub-classes override this method. */
    protected abstract void init();
    
}
