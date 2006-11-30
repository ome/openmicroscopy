/*
 * org.openmicroscopy.shoola.util.image.io.ImageWriter
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
import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;


//Third-party libraries

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
     * a <code>JPEG</code> or <code>PNG</code> image is created.
     * 
     * @param f The file used to create the outpu stream.
     * @param img The image to encode.
     * @param format The file format.
     * @throws EncoderException Exception thrown if an error occured during the
     * encoding process.
     */
    public static void saveImage(File f, BufferedImage img, String format)
        throws EncoderException
    {
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
     * @param encoder The encoder to use.
     * @throws EncoderException Exception thrown if an error occured during the
     * encoding process.
     */
    public static void saveImage(Encoder encoder)
        throws EncoderException
    {   
        try {
            encoder.write();
            encoder.getOutput().close();   
        } catch (Exception e) {
            throw new EncoderException("Cannot encode the image.", e);
        }      
    }  
    
}
