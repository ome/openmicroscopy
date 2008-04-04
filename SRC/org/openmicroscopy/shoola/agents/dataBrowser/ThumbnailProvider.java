/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailProvider 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ThumbnailProvider     
	implements Thumbnail
{
    
    /** The maximum width of the thumbnail. */
    static final int            THUMB_MAX_WIDTH = Factory.THUMB_DEFAULT_WIDTH; 
    
    /** The maximum height of the thumbnail. */
    static final int            THUMB_MAX_HEIGHT = Factory.THUMB_DEFAULT_HEIGHT;
    
    /** The thickness of the border added to the icon. */
    private static final int    BORDER = 1;
    
    /** The color of the border. */
    private static final Color  BORDER_COLOR = Color.WHITE;
    
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
    
    /** The {@link BufferedImage} representing the thumbnail displayed. */
    private BufferedImage   displayThumb;
    
    /** 
     * The {@link BufferedImage} representing the full size image for 
     * the slide show. 
     */
    private BufferedImage   fullSizeImage;
    
    /** The magnification factor. */
    private double          scalingFactor;
    
    /** The {@link Icon} representing the thumbnail. */
    private Icon            iconThumb;
     
    //TODO: this duplicates code in env.data.views.calls.ThumbnailLoader,
    //but we need size b/f img is retrieved -- b/c we vis tree need to be
    //laid out.  Sort this out.
    private void computeDims()
    {
        PixelsData pxd = imgInfo.getDefaultPixels();
        int w = THUMB_MAX_WIDTH;
        int h = THUMB_MAX_HEIGHT;
        double pixSizeX = pxd.getSizeX();
        double pixSizeY = pxd.getSizeY();
        //if (pixSizeX < THUMB_MAX_WIDTH) w = (int) pixSizeX;
       // if (pixSizeY < THUMB_MAX_HEIGHT) h = (int) pixSizeY;
        int sizeX = (int) (w*SCALING_FACTOR);
        int sizeY = (int) (h*SCALING_FACTOR);
        originalWidth = w;
        originalHeight = h;
        double ratio = 1;
        if (pxd != null) 
            ratio = pixSizeX/pixSizeY;
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
        if (width <= 0 || height <= 0) return null;
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
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#setImageNode(ImageNode)
     */
    public void setImageNode(ImageNode node)
    {
        if (node == null) throw new IllegalArgumentException("No Image node");
        display = node;
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#setFullScaleThumb(BufferedImage)
     */
    public void setFullScaleThumb(BufferedImage t)
    {
        if (t == null) throw new NullPointerException("No thumbnail.");
        fullScaleThumb = t;
        scale(scalingFactor);
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getDisplayedImage()
     */
    public BufferedImage getDisplayedImage() { return displayThumb; }

    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#scale(double)
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
        if (display != null) {  //Shouldn't happen.
            display.setCanvasSize(w, h);
            display.pack();
        }
    }
     
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getWidth()
     */
    public int getWidth() { return width; }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getHeight()
     */
    public int getHeight() { return height; }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getScalingFactor()
     */
    public double getScalingFactor() { return scalingFactor; }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getFullScaleThumb()
     */
    public BufferedImage getFullScaleThumb() { return fullScaleThumb; }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getZoomedFullScaleThumb()
     */
    public BufferedImage getZoomedFullScaleThumb()
    {
    	return magnifyImage(1.5, fullScaleThumb);
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getIcon()
     */
    public Icon getIcon() 
    {
        if (iconThumb != null) return iconThumb;
        if (fullScaleThumb == null) return null;
        BufferedImage img = magnifyImage(0.16, fullScaleThumb);
        BufferedImage newImg = new BufferedImage(img.getWidth()+2*BORDER, 
                img.getHeight()+2*BORDER, img.getType());
        Graphics g = newImg.getGraphics();
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(BORDER_COLOR);
        g2D.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());
        g2D.drawImage(img, null, BORDER, BORDER);
        iconThumb = new ImageIcon(newImg);
        return iconThumb;
    }

	/**
	 * Implemented as specified by the {@link Thumbnail} I/F.
	 * @see Thumbnail#isThumbnailLoaded()
	 */
	public boolean isThumbnailLoaded() { return fullScaleThumb != null; }

	/**
	 * Implemented as specified by the {@link Thumbnail} I/F.
	 * @see Thumbnail#setFullSizeImage(BufferedImage)
	 */
	public void setFullSizeImage(BufferedImage image)
	{ 
		fullSizeImage = image;
	}

    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getFullSizeImage()
     */
	public BufferedImage getFullSizeImage() { return fullSizeImage; }

}
