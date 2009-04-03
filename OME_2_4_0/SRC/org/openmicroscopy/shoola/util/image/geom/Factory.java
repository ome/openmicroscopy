/*
 * org.openmicroscopy.shoola.util.image.geom.Factory
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

package org.openmicroscopy.shoola.util.image.geom;

//Java imports
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class Factory
{
    
    
    /** Sharpen filter. */
    public static final float[] SHARPEN = {
            0.f, -1.f,  0.f,
            -1.f,  5.f, -1.f,
            0.f, -1.f,  0.f};
    
    /** Low pass filter. */
    public static final float[] LOW_PASS = {
            0.1f, 0.1f, 0.1f,   
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f};
    
    /** 
     * Magnify the selected {@link BufferedImage}.
     * 
     * @param img       BufferedImage to zoom in or out.
     * @param level     magnification factor.
     * @param at        Affine transform
     * @param w         extra space, necessary b/c of the lens option.         
     * @return          The zoomed bufferedImage.
     */
    public static BufferedImage magnifyImage(BufferedImage img, double level, 
                                        AffineTransform at, int w)
    {
        int width = img.getWidth(), height = img.getHeight();
        BufferedImage bimg = new BufferedImage(width, height, 
                                                BufferedImage.TYPE_INT_RGB);
        RescaleOp rop = new RescaleOp(1, 0.0f, null);
        rop.filter(img, bimg);
        BufferedImageOp biop = new AffineTransformOp(at, 
                                AffineTransformOp.TYPE_BILINEAR); 
        BufferedImage rescaleBuff = new BufferedImage((int) (width*level)+w, 
                                            (int) (height*level)+w,
                                            BufferedImage.TYPE_INT_RGB);
        Graphics2D bigGc = rescaleBuff.createGraphics();
        bigGc.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
        bigGc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        bigGc.drawImage(bimg, biop, 0, 0);
        return rescaleBuff;
    } 
    
    /** Apply a sharpen filter or a low_pass filter. */
    public static BufferedImage convolveImage(BufferedImage img, float[] filter)
    {
        int width = img.getWidth(), height = img.getHeight();
        BufferedImage bimg = new BufferedImage(width, height, 
                                                BufferedImage.TYPE_INT_RGB);
        RescaleOp rop = new RescaleOp(1, 0.0f, null);
        rop.filter(img, bimg);  // copy the original one
        Kernel kernel = new Kernel(3, 3, filter);
        ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP,
                                        null);
        BufferedImage finalImg = new BufferedImage(width, height,
                                    BufferedImage.TYPE_INT_RGB);
        cop.filter(bimg, finalImg);
        return finalImg;
    }
    
}
