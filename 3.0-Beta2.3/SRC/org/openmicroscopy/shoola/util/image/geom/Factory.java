/*
 * org.openmicroscopy.shoola.util.image.geom.Factory
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

package org.openmicroscopy.shoola.util.image.geom;

//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

//Third-party libraries
import sun.awt.image.IntegerInterleavedRaster;

//Application-internal dependencies

/** 
 * Utility class. Applies some basic filtering methods and affine transformations
 * to a {@link BufferedImage}.
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
    
	/** The red mask. */
	public static final int		RED_MASK = 0x00ff0000;
	
	/** The green mask. */
	public static final int		GREEN_MASK = 0x0000ff00;
	
	/** The blue mask. */
	public static final int		BLUE_MASK = 0x000000ff;
	
	/** The blank mask. */
	public static final int		BLANK_MASK = 0x00000000;
	
	/** Indicates that the text will be added in the top-left corner. */ 
	public static final int		LOC_TOP_LEFT = 0;

	/** Indicates that the text will be added in the top-right corner. */ 
	public static final int		LOC_TOP_RIGHT = 1;

	/** Indicates that the text will be added in the bottom-left corner. */ 
	public static final int		LOC_BOTTOM_LEFT = 2;

	/** Indicates that the text will be added in the bottom-right corner. */ 
	public static final int     LOC_BOTTOM_RIGHT = 3;

	/** The default width and height of a thumbnail. */
	public static final int		DEFAULT_THUMB = 96;

	/** Border added to the text. */
	private static final int	BORDER = 2;

	/** The default message for the default thumbnail. */
	private static final String	DEFAULT_TEXT = "No thumbnail";
    
	/** Default text drawn on thumbnail before retrieval. */
	private static final String	LOADING_TEXT = "Loading...";
	
	/** The RGB masks. */
	public static final int[]	RGB = {RED_MASK, GREEN_MASK, BLUE_MASK};
	
    /** Sharpen filter. */
    public static final float[] SHARPEN = {
            0.f, -1.f,  0.f,
            -1.f,  5.f, -1.f,
            0.f, -1.f,  0.f};
    
    /** Low pass filter. */
    public static final float[] LOW_PASS = {
            0.1f, 0.1f, 0.1f,   
            0.1f, 0.2f, 0.1f,
            0.1f, 0.1f, 0.1f
            };

    /**
     * Creates a default thumbnail image.
     * 
     * @param sizeX The width of the thumbnail
     * @param sizeY The height of the thumbnail.
     * @param text	The text to draw.
     * @return See above.
     */
    public static BufferedImage createDefaultThumbnail(int sizeX, int sizeY, 
    													String text)
    {
        BufferedImage thumbPix = new BufferedImage(sizeX, sizeY, 
                                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) thumbPix.getGraphics();
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, sizeX, sizeY);
        if (text != null && text.trim().length() != 0) {
        	FontMetrics fontMetrics = g.getFontMetrics();
            int xTxt = BORDER;
            int yTxt = sizeY/2-fontMetrics.getHeight();
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD));
            g.drawString(text, xTxt, yTxt);
        }
        return thumbPix;
    }

    /**
     * Creates a default thumbnail whose width is {@link #DEFAULT_THUMB}
     * and height is {@link #DEFAULT_THUMB} and default message
     * {@link #LOADING_TEXT}.
     * 
     * @return See above,
     */
    public static BufferedImage createDefaultThumbnail()
    {
    	return createDefaultThumbnail(DEFAULT_THUMB, DEFAULT_THUMB, 
    									LOADING_TEXT);
    }
    
    /**
     * Creates a default thumbnail image.
     * 
     * @param sizeX The width of the thumbnail
     * @param sizeY The height of the thumbnail.
     * @return See above.
     */
    public static BufferedImage createDefaultThumbnail(int sizeX, int sizeY)
    {
    	return createDefaultThumbnail(sizeX, sizeY, DEFAULT_TEXT);
    }
    
    /** 
     * Magnifies the specified {@link BufferedImage}.
     * 
     * @param img   The buffered image to magnify.
     * @param level The magnification factor.
     * @param w     Extra space, necessary b/c of the lens option.         
     * @return      The magnified image.
     */
    public static BufferedImage magnifyImage(BufferedImage img, double level, 
                                         int w)
    {
    	if (img == null) return null;
    	
        AffineTransform a = new AffineTransform();
        a.scale(level, level);
        BufferedImageOp biop = new AffineTransformOp(a,
                                        AffineTransformOp.TYPE_BILINEAR); 
        int width = (int) (img.getWidth()*level)+w;
        int height = (int) (img.getHeight()*level)+w;
        if (width <= 0 || height <= 0) return null;
        BufferedImage rescaleBuff = new BufferedImage(width, height,
                						BufferedImage.TYPE_INT_RGB);
        BufferedImage bimg = new BufferedImage(img.getWidth(), img.getHeight(), 
                BufferedImage.TYPE_INT_RGB);
        RescaleOp rop = new RescaleOp(1, 0.0f, null);
        rop.filter(img, bimg);
        Graphics2D g = rescaleBuff.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bimg, biop, 0, 0);
        return rescaleBuff;
    }   
    
    /** 
     * Magnifies the specified image.
     * 
     * @param f 	The magnification factor.
     * @param img 	The image to magnify.
     * 
     * @return The magnified image.
     */
    public static BufferedImage magnifyImage(double f, BufferedImage img)
    {
        if (img == null) return null;
        int width = img.getWidth(), height = img.getHeight();
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        BufferedImageOp biop = new AffineTransformOp(at, 
        							AffineTransformOp.TYPE_BILINEAR); 
        int type = img.getType();
        if (type == BufferedImage.TYPE_CUSTOM)
        	type = BufferedImage.TYPE_INT_RGB;
        int scaleWidth = (int) (width*f);
        int scaleHeight = (int) (height*f);
        if (scaleWidth <= 0 || scaleHeight <= 0) return null;
        BufferedImage rescaleBuff = new BufferedImage(scaleWidth, 
        							scaleHeight, type); //img.getType()
        biop.filter(img, rescaleBuff);
        return rescaleBuff;
    }

    /** 
     * Applies a sharpen filter or a low_pass filter. 
     * 
     * @param img       The image to transform.
     * @param filter    The filter to apply.
     * @return          The transformed image.
     */
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
    
    /**
     * Creates a buffered image from another buffered image with 
     * some text on top.
     * 
     * @param img           The original buffered image.
     * @param text          The text to add.
     * @param indexLocation The location of the text.
     * @param c             The color of the text.
     * @return              A new buffered image.
     */
    public static BufferedImage createImageWithText(BufferedImage img, 
            String text, int indexLocation, Color c)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImage = new BufferedImage(w, h, 
                                            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        //Paint the original image.
        g2.drawImage(img, null, 0, 0); 
        //Paint the text
        FontMetrics fontMetrics = g2.getFontMetrics();
        int charWidth = fontMetrics.charWidth('m');
        int hFont = fontMetrics.getHeight();
        int length = text.length()*charWidth;
        int xTxt = 0, yTxt = 0;
        switch (indexLocation) {
            case LOC_TOP_LEFT:
                xTxt = BORDER;
                yTxt = BORDER+hFont;
                break;
            case LOC_TOP_RIGHT:
                xTxt = w-BORDER-length;
                yTxt = BORDER+hFont;
                break;
            case LOC_BOTTOM_LEFT:
                xTxt = BORDER;
                yTxt = h-BORDER-hFont;
                break;
            case LOC_BOTTOM_RIGHT:
                xTxt = w-BORDER-length;
                yTxt = h-BORDER-hFont;
        }
        if (c != null) {
            g2.setColor(c);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD));
            g2.drawString(text, xTxt, yTxt);
        }
        return newImage;
    }
    
    /**
     * Creates a {@link BufferedImage} with a <code>Raster</code> from a
     * <code>BufferedImage</code> built from a graphics context.
     * 
     * @param img The image to transform.
     * @return See above.
     */
    public static BufferedImage createImage(BufferedImage img)
    {
        int sizeY = img.getWidth();
        int sizeX = img.getHeight();
        DataBufferByte buffer = new DataBufferByte(sizeX*sizeY, 3);
        DataBuffer dataBuf = img.getRaster().getDataBuffer();
        ColorModel cm = img.getColorModel();
        int pos = 0;
        int v = 0;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                pos = sizeX*y+x;
                v = dataBuf.getElem(0, pos);
                buffer.setElem(0, pos, cm.getRed(v));
                buffer.setElem(1, pos, cm.getGreen(v));
                buffer.setElem(2, pos, cm.getBlue(v));
            }
        }
        ComponentColorModel ccm = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB), 
                null, false, false, Transparency.OPAQUE, 
                DataBuffer.TYPE_BYTE);
        BandedSampleModel csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
                sizeX, sizeY, 3);
        return new BufferedImage(ccm, 
                Raster.createWritableRaster(csm, buffer, null), false, null);
    }
    
    /**
     * Creates a buffer image from the specified <code>DataBuffer</code>.
     * 
     * @param buf	The buffer to handle.
     * @param bits	The number of bits in the pixel values.
     * @param sizeX	The width (in pixels) of the region of image data described.
     * @param sizeY	The height (in pixels) of the region of image data 
     * 				described.
     * @return See above.
     */
    public static BufferedImage createImage(DataBuffer buf, int bits, int sizeX, 
    										int sizeY)
    {
    	return createImage(buf, bits, RGB, sizeX, sizeY);
    }
    
    /**
     * Creates a buffer image from the specified <code>DataBuffer</code>.
     * 
     * @param buf	The buffer to handle.
     * @param bits	The number of bits in the pixel values.
     * @param masks The bit masks for all bands.
     * @param sizeX	The width (in pixels) of the region of image data described.
     * @param sizeY	The height (in pixels) of the region of image data 
     * 				described.
     * @return See above.
     */
    public static BufferedImage createImage(DataBuffer buf, int bits, 
    								int[] masks, int sizeX,  int sizeY)
	{
		if (buf instanceof DataBufferInt) {
			DataBufferInt j2DBuf = (DataBufferInt) buf;
			SinglePixelPackedSampleModel sampleModel =
						new SinglePixelPackedSampleModel(
							DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, masks);
			WritableRaster raster = 
			new IntegerInterleavedRaster(sampleModel, j2DBuf, 
						new Point(0, 0));
			
			ColorModel colorModel = new DirectColorModel(bits, masks[0],   
													masks[1], masks[2]);
			return new BufferedImage(colorModel, raster, false, null);
		}
		return null;
	}
    
    /**
     * Creates a buffer image from the specified <code>array</code> of 
     * integers.
     * 
     * @param buf	The array to handle.
     * @param bits	The number of bits in the pixel values.
     * @param sizeX	The width (in pixels) of the region of image data described.
     * @param sizeY	The height (in pixels) of the region of image data 
     * 				described.
     * @return See above.
     */
    public static BufferedImage createImage(int[] buf, int bits, int sizeX, 
			int sizeY)
    {
    	DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY); 
		return createImage(j2DBuf, bits, sizeX, sizeY);
    }
    
    /**
     * Creates a buffer image from the specified <code>array</code> of 
     * integers.
     * 
     * @param buf	The array to handle.
     * @param bits	The number of bits in the pixel values.
     * @param masks The bit masks for all bands.
     * @param sizeX	The width (in pixels) of the region of image data described.
     * @param sizeY	The height (in pixels) of the region of image data 
     * 				described.
     * @return See above.
     */
    public static BufferedImage createImage(int[] buf, int bits, int[] masks,
    									int sizeX, int sizeY)
    {
    	DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY); 
		return createImage(j2DBuf, bits, masks, sizeX, sizeY);
    }
    
}
