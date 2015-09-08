/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.image.geom.Factory;

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;

/** 
 * The class hosting the thumbnail corresponding to an {@link ImageData}.
 * We first retrieve a thumbnail of dimension {@link #THUMB_MAX_WIDTH}
 * and {@link #THUMB_MAX_HEIGHT} and scale it down i.e magnification factor
 * {@link #SCALING_FACTOR}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ThumbnailProvider
	implements Thumbnail
{

    /** 
     * The magnification factor used when the thumbnail of max size 
     * is magnified. 
     */
    private static final double	ZOOM_FACTOR = 1.5;
    
    /** The thickness of the border added to the icon. */
    private static final int    BORDER = 1;
    
    /** The color of the border. */
    private static final Color  BORDER_COLOR = Color.WHITE;
    
    /** The {@link DataObject} the thumbnail is for. */
    private DataObject       imgInfo;
    
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
     
    /** Flag indicating if the thumbnail is valid or not. */
    private boolean			valid;
    
    //TODO: this duplicates code in env.data.views.calls.ThumbnailLoader,
    //but we need size b/f img is retrieved -- b/c we vis tree need to be
    //laid out.  Sort this out.
    private void computeDims()
    {
        PixelsData pxd = null;
        width = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
        height = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
        originalWidth = THUMB_MAX_WIDTH;
        originalHeight = THUMB_MAX_HEIGHT;
        try {
        	if (imgInfo instanceof ImageData)
        		pxd = ((ImageData) imgInfo).getDefaultPixels();
        	else return;
		} catch (Exception e) { //no pixels linked to it.
			width = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
	        height = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
	        originalWidth = THUMB_MAX_WIDTH;
	        originalHeight = THUMB_MAX_HEIGHT;
	        return;
		}
		if (pxd == null) {
			width = (int) (THUMB_MAX_WIDTH*SCALING_FACTOR);
	        height = (int) (THUMB_MAX_HEIGHT*SCALING_FACTOR);
	        originalWidth = THUMB_MAX_WIDTH;
	        originalHeight = THUMB_MAX_HEIGHT;
	        return;
		}
        double pixSizeX = pxd.getSizeX();
        double pixSizeY = pxd.getSizeY();
        Dimension size = Factory.computeThumbnailSize(width, height, pixSizeX, 
        		pixSizeY);
        width = size.width;//sizeX;
        height = size.height;//sizeY;
        size = Factory.computeThumbnailSize(originalWidth, originalHeight, 
        		pixSizeX, pixSizeY);
        originalWidth = size.width;//sizeX;
        originalHeight = size.height;//sizeY;
    }

    /**
     * Creates a new instance.
     * 
     * @param is The image data object.
     */
    public ThumbnailProvider(DataObject is)
    {
        if (is == null) throw new IllegalArgumentException("No image.");
        if (!(is instanceof ImageData || is instanceof ExperimenterData ||
        	is instanceof FileData))
        	throw new IllegalArgumentException("Objet to supported.");
        imgInfo = is;
        scalingFactor = SCALING_FACTOR;
        computeDims();
        valid = true;
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
    	flush();
    	fullScaleThumb = t;
        if (fullScaleThumb != null) scale(scalingFactor);
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#setFullScaleThumb(BufferedImage)
     */
    public void setValid(boolean valid)
    {
    	this.valid = valid;
    	if (!valid && display != null) {
    		Registry reg = DataBrowserAgent.getRegistry();
    		Boolean b = (Boolean) reg.lookup("/views/DisplayNonValidImage");
    		//display.setVisible(b);
    	}
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
            displayThumb = Factory.magnifyImage(f, fullScaleThumb);
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
    	return Factory.magnifyImage(ZOOM_FACTOR, fullScaleThumb);
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getIcon()
     */
    public Icon getIcon() 
    {
        if (iconThumb != null) return iconThumb;
        if (fullScaleThumb == null) return null;
        BufferedImage img = Factory.magnifyImage(ICON_ZOOM, fullScaleThumb);
        BufferedImage newImg = new BufferedImage(img.getWidth()+2*BORDER, 
                img.getHeight()+2*BORDER, img.getType());
        Graphics g = newImg.getGraphics();
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(BORDER_COLOR);
        g2D.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());
        g2D.drawImage(img, null, BORDER, BORDER);
        iconThumb = new ImageIcon(newImg);
        img.flush();
        return iconThumb;
    }
    
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getIcon()
     */
    public Icon getIcon(double magnification)
    {
    	if (magnification <= 0) magnification = ICON_ZOOM;
    	if (fullScaleThumb == null) return null;
        BufferedImage img = Factory.magnifyImage(magnification, fullScaleThumb);
        BufferedImage newImg = new BufferedImage(img.getWidth()+2*BORDER, 
                img.getHeight()+2*BORDER, img.getType());
        Graphics g = newImg.getGraphics();
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(BORDER_COLOR);
        g2D.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());
        g2D.drawImage(img, null, BORDER, BORDER);
        img.flush();
        return new ImageIcon(newImg);
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

    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#getOriginalSize()
     */
	public Dimension getOriginalSize()
	{ 
		return new Dimension(originalWidth, originalHeight);
	}
	
    /**
     * Implemented as specified by the {@link Thumbnail} I/F.
     * @see Thumbnail#flush()
     */
	public void flush()
	{
		fullSizeImage = null;
		displayThumb = null;
		fullScaleThumb = null;
	}
}
