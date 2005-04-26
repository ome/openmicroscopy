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
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;

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
public class ThumbnailProvider
    implements Thumbnail
{
    
    static final int            THUMB_MAX_WIDTH = 96; 
    static final int            THUMB_MAX_HEIGHT = 96;
    
    public static final double  SCALING_FACTOR = 0.5;
    
    public static final double  MAX_SCALING_FACTOR = 1;
    public static final double  MIN_SCALING_FACTOR = 0.25;
    
    private ImageSummary    imgInfo;
    private int             width;  //of displayThumb. 
    private int             height; //of displayThumb.
    private BufferedImage   fullScaleThumb;
    private BufferedImage   displayThumb;
    private ImageNode       display;
    private double          scalingFactor;
    
    //TODO: this duplicates code in env.data.views.calls.ThumbnailLoader,
    //but we need size b/f img is retrieved -- b/c we vis tree need to be
    //laid out.  Sort this out.
    private void computeDims()
    {
        PixelsDescription pxd = imgInfo.getDefaultPixels();
        int sizeX = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
        int sizeY = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
        double ratio = (double) pxd.getSizeX()/pxd.getSizeY();
        if (ratio < 1) sizeX *= ratio;
        else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
        width = sizeX;
        height = sizeY;
    }
    
    public void setFullScaleThumb(BufferedImage t)
    {
        if (t == null) throw new NullPointerException("No thumbnail.");
        fullScaleThumb = t;
        //Scale down to 48x48.
        displayThumb = magnifyImage(SCALING_FACTOR, fullScaleThumb);
        if (display != null) display.repaint();
    }
    
    /** Scale the original thumbnail. */
    private BufferedImage magnifyImage(double f, BufferedImage img)
    {
        if (img == null) return null;
        scalingFactor = f;
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
    
    public ThumbnailProvider(ImageSummary is)
    {
        imgInfo = is;
        computeDims();
    }
    
    public int getWidth() { return width; }
    
    public int getHeight() { return height; }
    
    public BufferedImage getImageFor(ImageNode node) 
    { 
        display = node;
        return displayThumb; 
    }

    public void scale(double f)
    {
        if (fullScaleThumb == null) return;
        if (f < MIN_SCALING_FACTOR || f > MAX_SCALING_FACTOR) return;
        displayThumb = magnifyImage(f, fullScaleThumb);
        if (display != null) {
            int w = displayThumb.getWidth();
            int h = displayThumb.getHeight();
            display.setCanvasSize(w, h);
            display.pack();
        }
    }
    
    public double getScalingFactor() { return scalingFactor; }
    
    public BufferedImage getFullScaleThumb() { return fullScaleThumb; }
    
}
