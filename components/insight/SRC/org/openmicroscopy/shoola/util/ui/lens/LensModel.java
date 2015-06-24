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
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The Lens model controls the manipulation of the lens, creating the zoomed 
 * version of the image to be displayed in the zoomPanel. 
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
	
	/** Dark lens color, when the background of the image is light. */
    static final Color 		LENS_DARK_COLOUR = new Color(96, 96, 96, 255);

	/** Light lens color, when the background of the image is Dark. */
	static final Color 		LENS_LIGHT_COLOUR = 
											new Color(196, 196, 196, 255);

	/** The default color of the background. */
	static final Color	DEFAULT_BACKGROUND = new Color(200, 200, 200);
	
	/** Minimum zoom allowed. */
	final static int		MINIMUM_ZOOM = 1;

	/** Maximum zoom allowed. */
	final static int		MAXIMUM_ZOOM = 10;
	
	/** 
	 * The default size of the pre-allocated buffer used to store the 
	 * zoomed image. 
	 */
	final static int		DEFAULT_SIZE = 562500;
		
	/** The minimum size of heapspace for the lens to still work. */
	final static long 		MINHEAPSIZE = UIUtilities.MEGABYTE*8;
	
	/** X-coordinate of the lens. */
	private int		      	x;

	/** Y-coordinate of the lens. */
	private int		      	y;
	
	/** Width of the lens. */
	private int  	      	width;
	
	/** Height of the lens. */
	private int		      	height;
	
	/** 
	 * ZoomFactor which will be used to convert the original image 
	 * represented by the viewport of the lens component to the image
	 * shown by the zoomWindow.
	 */
	private float          	zoomFactor;
	
	/** The amount of zooming in the original image. */
	private float          	imageZoomFactor;
	
	/** plane image. */
	private BufferedImage  	planeImage;
	
	/** Pre-allocated buffer storing image data */
	private DataBuffer     zoomedDataBuffer;
	
	/** 
	 * Size of the zoomedDatabuffer, used to see if a new Buffer 
	 * will exceed the size of the current buffer. 
	 */
	private int            zoomedDataBufferSize;

	/** The background color. */
	private Color			background;
	
	/** The name of the image.*/
	private String			imageName;
	
	/** Flushes the data buffer. */
	private void flushDataBuffer()
	{
		//System.gc();
		zoomedDataBuffer = null;
	}
	
	/**
	 * Returns a writable raster to the {@link #scaleBufferedImage} method.
	 * This method will allocate a new databuffer only if the new raster is
	 * larger than the pre-allocated one. 
	 * 
	 * @param dataBufferType	The type of the data buffer.
	 * @param colorModel		The color model. 
	 * @param w 				The width of new image.
	 * @param h 				The height of new image
	 * @return See above.
	 */
	private WritableRaster getZoomedRaster(int dataBufferType, 
										ColorModel colorModel, int w, int h)
	{
		double f = zoomFactor*zoomFactor;
		//reset dataBuffer 
		switch (dataBufferType) {
			case DataBuffer.TYPE_INT:
				if (zoomedDataBuffer instanceof DataBufferByte)
					flushDataBuffer();
				break;
			case DataBuffer.TYPE_BYTE:
				if (zoomedDataBuffer instanceof DataBufferInt)
					flushDataBuffer();
		}
		if (zoomedDataBufferSize < height*width*f) {
			flushDataBuffer();
			switch (dataBufferType) {
				case DataBuffer.TYPE_INT:
					zoomedDataBuffer = new DataBufferInt((int)(150*150*f), 1);
					break;
				case DataBuffer.TYPE_BYTE:
					zoomedDataBuffer = new DataBufferByte((int)(150*150*f), 1);
					break;
			}
		} else {
			if (zoomedDataBuffer != null && 
				zoomedDataBuffer.getSize() !=  DEFAULT_SIZE)
				flushDataBuffer();
		}
		
		if (zoomedDataBuffer == null) {
    		switch (dataBufferType) {
				case DataBuffer.TYPE_INT:
					zoomedDataBuffer =  new DataBufferInt(DEFAULT_SIZE, 1);
					break;
				case DataBuffer.TYPE_BYTE:
					zoomedDataBuffer =  new DataBufferByte(DEFAULT_SIZE, 1);
					break;
    		}
    	}
		SampleModel sm = colorModel.createCompatibleSampleModel(w, h);
        return Raster.createWritableRaster(sm, zoomedDataBuffer, null);			
 	}
	
	/**
	 * Scales the image to the new size xScale, yScale.
	 * 
	 * @param image 	Buffered Image to be scaled.
	 * @param xScale	x scale factor for image, as a percent. 
	 * @param yScale	y scale factor for image, as a percent.
	 * @return thumbImage scaled image.
	 */
    private BufferedImage scaleBufferedImage(BufferedImage image,
                                            float xScale, float yScale)
    {
     	int thumbHeight = (int) (image.getHeight()*yScale);
    	int thumbWidth  = (int) (image.getWidth()*xScale);
        
    	// Create the required compatible (thumbnail) buffered image to  
    	// avoid potential errors from Java's ImagingLib.
    	ColorModel cm = image.getColorModel();
    	int type = image.getData().getDataBuffer().getDataType();
    	
    	WritableRaster r = getZoomedRaster(type, cm, thumbWidth, thumbHeight);
    	BufferedImage thumbImage = new BufferedImage(cm, r, false, null);
 
    	// Do the actual scaling and return the result
        Graphics2D graphics2D = thumbImage.createGraphics();
   
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
        graphics2D.dispose();
        
        //System.gc();
        return thumbImage;
    }
    
	/**
	 * Creates a new instance. 
	 * 
	 * @param planeImage The image to handle.
	 */
	LensModel(BufferedImage planeImage)
	{
		this.planeImage = planeImage;
		x = 0;
		y = 0;
		width = LensComponent.LENS_DEFAULT_WIDTH;
		height = LensComponent.LENS_DEFAULT_WIDTH;
		setBackgroundColor(DEFAULT_BACKGROUND);
		zoomedDataBufferSize = DEFAULT_SIZE;
	}

	/** 
	 * Sets the plane image to a new image.
	 *  
	 * @param img new PlaneImage.
	 */
	void setPlaneImage(BufferedImage img) { planeImage = img; }

	/**
	 * Returns the width of the plane Image.
	 * 
	 * @return See above.
	 */
	int	getImageWidth()
	{
	    if (planeImage != null) return planeImage.getWidth();
		return 0;
	}
	
	/**
	 * Returns the width of the plane Image.
	 * 
	 * @return See above.
	 */
	int	getImageScaledWidth()
	{
		return (int) (getImageWidth()*imageZoomFactor);
	}

	/**
	 * Returns the height of the plane Image.
	 * 
	 * @return See above.
	 */
	int	getImageScaledHeight()
	{
		return (int) (getImageHeight()*imageZoomFactor);
	}
	
	/**
	 * Returns the height of the plane Image.
	 * 
	 * @return See above.
	 */
	int	getImageHeight()
	{
	    if (planeImage != null) return planeImage.getHeight();
		return 0;
	}

	/**
     * Returns the zoomedImage from the model. 
     * 
     * @return See above. 
     */
	BufferedImage getZoomedImage()
	{
		/*
		if (UIUtilities.getFreeMemory() < MINHEAPSIZE) {
			Runtime.getRuntime().gc();
			return null;
		}
		*/
		try {
			if (planeImage == null) return null;
			ColorModel cm = planeImage.getColorModel();
			Raster raster = planeImage.getData();
			Raster r = raster.createChild(getX(), 
					            getY(), getWidth(), getHeight(), 0, 0, null);
			BufferedImage img = new BufferedImage(cm, (WritableRaster) r, false,
																		null);
			return scaleBufferedImage(img, zoomFactor, zoomFactor);
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * Creates a zoomed version of the passed image.
	 * 
	 * @param image		The image to zoom.
	 * @return See above.
	 */
	BufferedImage createZoomedImage(BufferedImage image)
	{
		if (image == null) return null;
		ColorModel cm = image.getColorModel();
		Raster r = image.getData().createChild(getX(), 
				            getY(), getWidth(), getHeight(), 0, 0, null);
		BufferedImage img = new BufferedImage(cm, (WritableRaster) r, false,
																	null);
		int thumbHeight = (int) (img.getHeight()*zoomFactor);
    	int thumbWidth  = (int) (img.getWidth()*zoomFactor);
    	
    	// Create the required compatible (thumbnail) buffered image to  
    	// avoid potential errors from Java's ImagingLib.
    	int type = image.getData().getDataBuffer().getDataType();
    	WritableRaster wr = getZoomedRaster(type, cm, thumbWidth, thumbHeight);
    	BufferedImage thumbImage = new BufferedImage(img.getColorModel(), 
				wr, false, null);

		//Do the actual scaling and return the result
    	Graphics2D graphics2D = thumbImage.createGraphics();

    	graphics2D.drawImage(img, 0, 0, thumbWidth, thumbHeight, null);
    	graphics2D.dispose();
    	//System.gc();
		return thumbImage;
	}
	
	/**
	 * Returns the height of the lens. 
	 * 
	 * @return See above.
	 */
	int getHeight() { return height; }
	
	/**
	 * Returns the width of the lens. 
	 * 
	 * @return See above.
	 */
	int getWidth() { return width; }	
    
    /**
	 * Returns the height of the lens. 
	 * 
	 * @return See above.
	 */
	
	int getScaledHeight() { return (int) Math.ceil(height*imageZoomFactor); }
	
	/**
	 * Returns the width of the lens. 
	 * 
	 * @return See above.
	 */
	int getScaledWidth() { return (int) Math.ceil(width*imageZoomFactor); }

	/**
	 * Returns the x-coordinate of the lens.
	 * 
	 * @return See above.
	 */
	int getX() { return x; }

	/**
	 * Returns the y-coordinate of the lens.

	 * @return See above.
	 */
	int getY() { return y; }
    
	/**
	 * Returns the x-coordinate of the lens multiplied by the magnification 
	 * factor.
	 * 
	 * @return See above.
	 */

	int getScaledX() { return (int) (x*imageZoomFactor); }

	/**
	 * Returns the y-coordinate of the lens multiplied by the magnification 
	 * factor.
     * 
	 * @return See above.
	 */
	int getScaledY() { return (int) (y*imageZoomFactor); }

	/**
	 * Sets the height of the lens. 
	 * 
	 * @param height The height to set.
	 */
	void setHeight(int height)  { this.height = height; }

	/**
	 * Sets the width of the lens. 
	 * 
	 * @param width The width to set.
	 */
	void setWidth(int width) { this.width = width; }

	/**
	 * Sets the location of the lens.
	 * 
	 * @param x The x-coordinate to set.
	 * @param y The y-coordinate to set.
	 */
	void setLensLocation(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the current magnification factor of the lens. 
	 * 
	 * @return See above.
	 */
	float getZoomFactor() { return zoomFactor; }

	/**
     * Sets the zoom factor.
     * 
	 * @param zoomFactor The zoomFactor to set.
	 */
	void setZoomFactor(float zoomFactor) { this.zoomFactor = zoomFactor; }
	
	/**
	 * Sets the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @param imageZoomFactor 	The amount of zooming that has occurred on the 
	 * 							image. 
	 */
	void setImageZoomFactor(float imageZoomFactor)
	{
		this.imageZoomFactor = imageZoomFactor;
	}

	/**
	 * Returns the image zoom factor. The image in the viewer has been zoomed by
	 * this number.
	 * 
	 * @return See above.
	 */
	float getImageZoomFactor() { return imageZoomFactor; }
	
    /**
     * Returns the scaled size of the lens, scaled by the imageZoomFactor.
     * 
     * @return See above.
     */
    Dimension getLensScaledSize()
    {
    	return new Dimension(getScaledWidth(), getScaledHeight());
    }
    
    /**
     * Returns the scaled location of the lens, scaled by the 
     * imageZoomFactor.
     * 
     * @return See above.
     */
    Point getLensScaledLocation()
    {
    	return new Point(getScaledX(), getScaledY());
    }
    
    /**
     * Returns the location of the lens.
     * 
     * @return See above.
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
     * predominantly dark returns a light lens else returns a dark lens 
     * colour. Returns <code>null</code> if the image is <code>null</code>.
     * 
     * @return See above.
     */
    Color getLensPreferredColour()
    {
    	/*
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
    	*/
    	return LENS_LIGHT_COLOUR;
    }
	    
    /** Resets the data buffer. */
    void resetDataBuffer()
    { 
    	zoomedDataBuffer = null; 
    	zoomedDataBufferSize = DEFAULT_SIZE;
    }

    /**
     * Returns the background.
     * 
     * @return See above.
     */
	Color getBackgroundColor() { return background; }

	/**
	 * Sets the background color.
	 * 
	 * @param color The value to set.
	 */
	void setBackgroundColor(Color color)
	{ 
		if (color == null) background = DEFAULT_BACKGROUND;
		background = color; 
	}
	
	/**
	 * Sets the name of the image.
	 * 
	 * @param imageName The name of the image.
	 */
	void setImageName(String imageName) { this.imageName = imageName;}
	
	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
    String getImageName() { return imageName; }
    
}
