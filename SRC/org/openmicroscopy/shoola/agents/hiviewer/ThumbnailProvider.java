/*
 * org.openmicroscopy.shoola.agents.hiviewer.ThumbnailProvider
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * The class hosting the thumbnail corresponding to an {@link ImageData}.
 * We first retrieve a thumbnail of dimension {@link #THUMB_MAX_WIDTH}
 * and {@link #THUMB_MAX_HEIGHT} and scale it down i.e magnification factor
 * {@link #SCALING_FACTOR}.
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
public class ThumbnailProvider
    implements Thumbnail
{
    
    /** The maximum width of the thumbnail. */
    static final int            THUMB_MAX_WIDTH = 96; 
    
    /** The maximum height of the thumbnail. */
    static final int            THUMB_MAX_HEIGHT = 96;
    
    /** The default magnification factor. */
    public static final double  SCALING_FACTOR = 0.5;
    
    /** The maximum magnification factor. */
    public static final double  MAX_SCALING_FACTOR = 1;
    
    /** The minimum magnification factor. */
    public static final double  MIN_SCALING_FACTOR = 0.25;
    
    /** The {@link ImageData} the thumbnail is for. */
    private ImageData       imgInfo;
    
    /**
     * The {@link ImageNode} corresponding to the {@link ImageData} and 
     * hosting the thumbnail.
     */
    private ImageNode       display;
    
    /** The width of the thumbnail on screen. */
    private int             width;  
    
    /** The height of the thumbnail on screen. */
    private int             height; 
    
    /** 
     * The width of the thumbnail retrieved from the server. The thumbnail
     * might not be a square.
     */
    private int             originalWidth;
    
    /** 
     * The height of the thumbnail retrieved from the server. The thumbnail
     * might not be a square.
     */
    private int             originalHeight;
    
    /** The {@link BufferedImage} representing a thumbnail of maximum size. */
    private BufferedImage   fullScaleThumb;
    
    /** The {@link BufferedImage} representing to the thumbnail displayed. */
    private BufferedImage   displayThumb;
    
    /** The magnification factor. */
    private double          scalingFactor;
    
    //TODO: this duplicates code in env.data.views.calls.ThumbnailLoader,
    //but we need size b/f img is retrieved -- b/c we vis tree need to be
    //laid out.  Sort this out.
    private void computeDims()
    {
        PixelsData pxd = imgInfo.getDefaultPixels();
        int sizeX = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
        int sizeY = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
        originalWidth = THUMB_MAX_WIDTH;
        originalHeight = THUMB_MAX_HEIGHT;
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) {
            sizeX *= ratio;
            originalWidth *= ratio;
        } else if (ratio > 1 && ratio != 0) {
            sizeY *= 1/ratio;
            originalHeight *= 1/ratio;
        }
        width = sizeX;
        height = sizeY;
    }

    /** 
     * Magnifies the specified image.
     * 
     * @param f the magnification factor.
     * @param img The image to magnify.
     * 
     * @return The magnified image.
     */
    private BufferedImage magnifyImage(double f, BufferedImage img)
    {
        if (img == null) return null;
        int width = img.getWidth(), height = img.getHeight();
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        BufferedImageOp biop = new AffineTransformOp(at, 
            AffineTransformOp.TYPE_BILINEAR); 
        BufferedImage rescaleBuff = new BufferedImage((int) (width*f), 
                        (int) (height*f), img.getType());
        biop.filter(img, rescaleBuff);
        return rescaleBuff;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param is The image data object.
     */
    public ThumbnailProvider(ImageData is)
    {
        if (is == null) throw new IllegalArgumentException("No image.");
        imgInfo = is;
        scalingFactor = SCALING_FACTOR;
        computeDims();
    }
    
    /** Sets the thumbnail retrieved from the server. */
    public void setFullScaleThumb(BufferedImage t)
    {
        if (t == null) throw new NullPointerException("No thumbnail.");
        fullScaleThumb = t;
        scale(scalingFactor);
    }
    
    /**
     * Returns the thumbnail corresponding to the selected {@link ImageNode}.
     * 
     * @param node The selected node.
     * @return See above.
     */
    public BufferedImage getImageFor(ImageNode node) 
    { 
        display = node;
        return displayThumb; 
    }

    /** 
     * Magnifies the original image. 
     * 
     * @param f The magnification factor.
     */
    public void scale(double f)
    {
        if (f < MIN_SCALING_FACTOR || f > MAX_SCALING_FACTOR) return;
        scalingFactor = f;
        int w = (int) (originalWidth*f), h = (int) (originalHeight*f);
        if (fullScaleThumb != null) {
            displayThumb = magnifyImage(f, fullScaleThumb);
            w = displayThumb.getWidth();
            h = displayThumb.getHeight();
        }  
        if (display != null) {
            display.setCanvasSize(w, h);
            display.pack();
        }
    }
     
    /** 
     * Returns the width of the displayed thumbnail.
     * 
     * @return See above.
     */
    public int getWidth() { return width; }
    
    /** 
     * Returns the height of the displayed thumbnail.
     * 
     * @return See above.
     */
    public int getHeight() { return height; }
    
    /**
     * Returns the magnification factor.
     * 
     * @return See above.
     */
    public double getScalingFactor() { return scalingFactor; }
    
    /**
     * Returns the thumbnail of maximal dimension.
     * 
     * @return See above.
     */
    public BufferedImage getFullScaleThumb() { return fullScaleThumb; }
    
}
