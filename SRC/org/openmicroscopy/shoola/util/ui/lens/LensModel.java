/*
 * LensModel.java
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui.lens;

//Java imports
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
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

public class LensModel 
{
		/** Minimum zoom allowed. */
		final static int		MINIMUM_ZOOM	= 1;

		/** Maximum zoom allowed. */
		final static int		MAXIMUM_ZOOM	= 10;
		
		/** x co-ordinate of the lens. */
		private int		x;

		/** y co-ordinate of the lens. */
		private int		y;
		
		/** Width of the lens. */
		private int  	width;
		
		/** Height of the lens. */
		private int		height;
		
		/** 
		 * ZoomFactor which will be used to convvert the original image 
		 * represented by the viewport of the lens component to the image
		 * shown by the zoomWindow.
		 */
		private float  zoomFactor;
		
		/** plane image. */
		private BufferedImage planeImage;
		
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
		void setPlaneImage(BufferedImage img)
		{
			planeImage = img;
		}
		
		int	getImageWidth()
		{
			if(planeImage != null )
				return planeImage.getWidth();
			else
				return 0;
		}

		int	getImageHeight()
		{
			if(planeImage != null )
				return planeImage.getHeight();
			else
				return 0;
		}
		
		/**
	     * Returns the zoomedImage from the model. 
	     * 
	     * @return zoomedImage. 
	     */
		BufferedImage getZoomedImage()
		{
			if( planeImage == null )
				return null;
			ColorModel cm = planeImage.getColorModel();
			Raster r = planeImage.getData().createChild(x, y, width, height, 0, 
																	0, null);
			BufferedImage img = new BufferedImage(cm, (WritableRaster) r, false,
																		null);
	    	BufferedImage scaledImg = scaleBufferedImage(img, zoomFactor, 
	    															zoomFactor);
	    	return scaledImg;
		}
		
		/**
		 * Get the height of the lens. 
		 * 
		 * @return the height
		 */
		int getHeight() 
		{
			return height;
		}
		
		/**
		 * Get the width of the lens. 
		 * 
		 * @return the width
		 */
		int getWidth() 
		{
			return width;
		}

		/**
		 * Get the x coordinate of the lens.
		 * 
		 * @return the x
		 */
		int getX() 
		{
			return x;
		}

		/**
		 * Get the y coordinate of the lens.

		 * @return the y
		 */
		int getY() 
		{
			return y;
		}

		/**
		 * Set the height of the lens. 
		 * 
		 * @param height the height to set
		 */
		void setHeight(int height) 
		{
			this.height = height;
		}

		/**
		 * Sets the width of the lens. 
		 * 
		 * @param width the width to set
		 */
		void setWidth(int width) 
		{
			this.width = width;
		}

		/**
		 * Set the X coordinate of the lens. 
		 * 
		 * @param x the x to set
		 */
		void setX(int x) 
		{
			this.x = x;
		}

		/**
		 * Set the y-Coordinate of the lens. 
		 * 
		 * @param y the y to set
		 */
		void setY(int y) 
		{
			this.y = y;
		}

		/**
		 * Set the location of the lens.
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
		 * Get the current Zoomfactor of the lens. 
		 * 
		 * @return the zoomFactor
		 */
		float getZoomFactor() 
		{
			return zoomFactor;
		}

		/**
		 * @param zoomFactor the zoomFactor to set
		 */
		void setZoomFactor(float zoomFactor) 
		{
			this.zoomFactor = zoomFactor;
		}
		
		/**
		* Scales the image to the new size xScale, yScale.
		* 
	    * @see ome.api.IScale#scaleBufferedImage(java.awt.image.BufferedImage, 
	    * float, float)
	    * 
	    * @param image 	Buffered Image to be scaled.
	    * @param xScale	x scale factor for image, as a percent. 
	    * @param yScale	y scale factor for image, as a percent.
	    * 
	    * @return thumbImage scaled image.
	    */
	    private BufferedImage scaleBufferedImage(BufferedImage image,
	                                            float xScale, float yScale)
	    {
	    	int thumbHeight = (int) (image.getHeight() * yScale);
	    	int thumbWidth  = (int) (image.getWidth() * xScale);
	    	
	    	// Create the required compatible (thumbnail) buffered image to  
	    	// avoid potential errors from Java's ImagingLib.
	    	ColorModel cm = image.getColorModel();
	    	WritableRaster r =
	    		cm.createCompatibleWritableRaster(thumbWidth, thumbHeight);
	    	BufferedImage thumbImage = new BufferedImage(cm, r, false, null);
	    	
	    	// Do the actual scaling and return the result
	        Graphics2D graphics2D = thumbImage.createGraphics();
	        //graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	        //  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
	        return thumbImage;
	    }
		
}
