/*
 * org.openmicroscopy.shoola.util.ui.lens.LensModel.java
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
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

//Third-party libraries

//Application-internal dependencies

/** 
 * The Lens model controls the manipulation of the lens, creating the zoomed 
 * verison of the image to be displayed in the zoomPanel. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME2.2
 */
class LensModel 
{

	/** Dark lens colour, when the background of the image is light. */
    static final Color 		LENS_DARK_COLOUR = new Color(96, 96, 96, 255);

	/** Light lens colour, when the background of the image is Dark. */
	static final Color 		LENS_LIGHT_COLOUR = 
											new Color(196, 196, 196, 255);

	/** Minimum zoom allowed. */
	final static int		MINIMUM_ZOOM	= 1;

	/** Maximum zoom allowed. */
	final static int		MAXIMUM_ZOOM	= 10;
	
	/** The default size of the pre-allocated buffer used to store the 
	 * zoomed image. 
	 */
	final static int		DEFAULT_SIZE	= 562500;
		
	/** x co-ordinate of the lens. */
	private int		      x;

	/** y co-ordinate of the lens. */
	private int		      y;
	
	/** Width of the lens. */
	private int  	      width;
	
	/** Height of the lens. */
	private int		      height;
	
	/** 
	 * ZoomFactor which will be used to convvert the original image 
	 * represented by the viewport of the lens component to the image
	 * shown by the zoomWindow.
	 */
	private float          zoomFactor;
	
	/** The amount of zooming in the original image. */
	private float          imageZoomFactor;
	
	/** plane image. */
	private BufferedImage  planeImage;
	
	/** pre-allocated buffer storing image data */
	private DataBuffer     zoomedDataBuffer = 
									new DataBufferInt(DEFAULT_SIZE, 1);
	
	/** Size of the zoomedDatabuffer, used to see if a new Buffer 
	 * will exceed the size of the current buffer. 
	 */
	private int            zoomedDataBufferSize = DEFAULT_SIZE;
	

	/**
	 * Returns a writable raster to the {@link #scaleBufferedImage} method.
	 * This method will allocate a new databuffer only if the new raster is
	 * larger than the pre-allocated one. 
	 * 
	 * @param w width of new image
	 * @param h height of new image
	 * @return see above.
	 */
	private WritableRaster getZoomedRaster(int w, int h)
	{
		if (zoomedDataBufferSize < height*zoomFactor*width*zoomFactor)
			zoomedDataBuffer = 
				new DataBufferInt((int)(150*150*zoomFactor*zoomFactor), 1);
		ColorModel colourModel = planeImage.getColorModel();
		SampleModel sm = colourModel.createCompatibleSampleModel(w, h);
        return Raster.createWritableRaster(sm, zoomedDataBuffer, null);			
 	}
	
	/**
	 * Scales the image to the new size xScale, yScale.
	 * 
	 * @param image 	Buffered Image to be scaled.
	 * @param xScale	x scale factor for image, as a percent. 
	 * @param yScale	y scale factor for image, as a percent.
	 * @return thumbImage scaled image.
	 * @see ome.api.IScale#scaleBufferedImage(BufferedImage, float, float)
	 */
    private BufferedImage scaleBufferedImage(BufferedImage image,
                                            float xScale, float yScale)
    {
     	int thumbHeight = (int) (image.getHeight()*yScale);
    	int thumbWidth  = (int) (image.getWidth()*xScale);
    	
    	// Create the required compatible (thumbnail) buffered image to  
    	// avoid potential errors from Java's ImagingLib.
    	ColorModel cm = image.getColorModel();
    	WritableRaster r = getZoomedRaster(thumbWidth, thumbHeight);
    	BufferedImage thumbImage = new BufferedImage(cm, r, false, null);
 
    	// Do the actual scaling and return the result
        Graphics2D graphics2D = thumbImage.createGraphics();
   
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
        return thumbImage;
    }
    
	/**
	 * Constructor of the lens Model.
	 * 
	 * @param planeImage 
	 *
	 */
	LensModel(BufferedImage planeImage)
	{
		this.planeImage = planeImage;
	}

	/** 
	 * Set the plane image to a new image.
	 *  
	 * @param img new PlaneImage.
	 */
	void setPlaneImage(BufferedImage img) { planeImage = img; }
	
	/**
	 * Gets the width of the plane Image.
	 * 
	 * @return see above.
	 */
	int	getImageWidth()
	{
		if (planeImage != null ) return planeImage.getWidth();
		return 0;
	}
	
	/**
	 * Gets the width of the plane Image.
	 * 
	 * @return see above.
	 */
	int	getImageScaledWidth()
	{
		if (planeImage != null )
			return (int)(planeImage.getWidth()*imageZoomFactor);
		return 0;
	}

	/**
	 * Gets the height of the plane Image.
	 * 
	 * @return height see above.
	 */
	int	getImageScaledHeight()
	{
		if (planeImage != null )
			return (int)(planeImage.getHeight()*imageZoomFactor);
		return 0;
	}
	/**
	 * Gets the height of the plane Image.
	 * 
	 * @return height see above.
	 */
	int	getImageHeight()
	{
		if (planeImage != null ) return planeImage.getHeight();
		return 0;
	}

	/**
     * Returns the zoomedImage from the model. 
     * 
     * @return zoomedImage. 
     */
	BufferedImage getZoomedImage()
	{
		if (planeImage == null) return null;
		ColorModel cm = planeImage.getColorModel();
		Raster r = planeImage.getData().createChild(getX(), 
				            getY(), getWidth(), getHeight(), 0, 0, null);
		BufferedImage img = new BufferedImage(cm, (WritableRaster) r, false,
																	null);
		return scaleBufferedImage(img, zoomFactor, zoomFactor); 
	}
	
	/**
	 * Gets the height of the lens. 
	 * 
	 * @return the height
	 */
	int getHeight() { return height; }
	
	/**
	 * Get the width of the lens. 
	 * 
	 * @return the width
	 */
	int getWidth() { return width; }	
    
    /**
	 * Gets the height of the lens. 
	 * 
	 * @return the height
	 */
	
	int getScaledHeight() { return (int) Math.ceil(height*imageZoomFactor); }
	
	/**
	 * Gets the width of the lens. 
	 * 
	 * @return the width
	 */
	int getScaledWidth() { return (int) Math.ceil(width*imageZoomFactor); }

	/**
	 * Gets the x coordinate of the lens.
	 * 
	 * @return the x
	 */
	int getX() { return x; }

	/**
	 * Gets the y coordinate of the lens.

	 * @return the y
	 */
	int getY() { return y; }
    
	/**
	 * Gets the x coordinate of the lens.
	 * 
	 * @return the x
	 */

	int getScaledX() { return (int)(x*imageZoomFactor); }

	/**
	 * Gets the y coordinate of the lens.
     * 
	 * @return the y
	 */
	int getScaledY() { return (int)(y*imageZoomFactor); }

	/**
	 * Sets the height of the lens. 
	 * 
	 * @param height the height to set.
	 */
	void setHeight(int height)  { this.height = height; }

	/**
	 * Sets the width of the lens. 
	 * 
	 * @param width the width to set.
	 */
	void setWidth(int width) { this.width = width; }

	/**
	 * Sets the X coordinate of the lens. 
	 * 
	 * @param x the x to set.
	 */
	void setX(int x) { this.x = x; }

	/**
	 * Sets the y-Coordinate of the lens. 
	 * 
	 * @param y the y to set.
	 */
	void setY(int y) { this.y = y; }

	/**
	 * Sets the location of the lens.
	 * 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	void setLensLocation(int x, int y)
	{
		setX(x);
		setY(y);
	}
	
	/**
	 * Gets the current Zoomfactor of the lens. 
	 * 
	 * @return the zoomFactor
	 */
	float getZoomFactor() { return zoomFactor; }

	/**
     * Sets the zoom factor.
     * 
	 * @param zoomFactor the zoomFactor to set
	 */
	void setZoomFactor(float zoomFactor) { this.zoomFactor = zoomFactor; }
	
	/**
	 * Sets the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @param imageZoomFactor the amount of zooming that has occurred on the 
	 * image. 
	 */
	void setImageZoomFactor(float imageZoomFactor)
	{
		this.imageZoomFactor = imageZoomFactor;
	}

	/**
	 * Returns the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @return see above.
	 */
	float getImageZoomFactor() { return imageZoomFactor; }
	
    /**
     * Returns the scaled size of the lens, scaled by the imageZoomFactor.
     * 
     * @return scaled lens size.
     */
    Dimension getLensScaledSize()
    {
    	return new Dimension(getScaledWidth(), getScaledHeight());
    }
    
    /**
     * Returns the scaled location of the lens, scaled by the 
     * imageZoomFactor.
     * 
     * @return scaled lens location.
     */
    Point getLensScaledLocation()
    {
    	return new Point(getScaledX(), getScaledY());
    }
    
    /**
     * Returns the location of the lens.
     * 
     * @return scaled lens location.
     */
    Point getLensLocation() { return new Point(getX(), getY()); }
    	  
	/**
	 * Returns the bounds of the scaled image size, takes into account the zoom 
	 * factor of the image viewer.
	 *  
	 * @return See above.
	 */
	Rectangle getLensScaledBounds() 
	{
		Point p = getLensScaledLocation();
		Dimension d = getLensScaledSize();
		return new Rectangle(p.x, p.y, d.width, d.height);
	}
	
    /** 
     * Depending on the sampled colour of the image; if the image is 
     * predominantly dark return a light lens else return a dark lens 
     * colour. 
     * 
     * @return see above.
     */
    Color getLensPreferredColour()
    {
    	if (planeImage != null)
    	{
    		long r  = 0, g  = 0, b = 0;
    		long cnt = 0, total = 0;
    		for (int i = 0 ; i < planeImage.getWidth() ; i+=10)
    			for (int j = 0 ; j < planeImage.getHeight() ; j+= 10)
    			{
    				cnt++;
    				Color c = new Color(planeImage.getRGB(i, j));
    				r = c.getRed(); 
    				g = c.getGreen(); 
    				b = c.getBlue();
    				total += (r+g+b)/3;
    			}
    
    		if ((total)/(cnt) > 128) return LENS_DARK_COLOUR;
    		return LENS_LIGHT_COLOUR;
    	}
    	return null;
    }
	    
}
