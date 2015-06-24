/*
 * org.openmicroscopy.shoola.util.image.geom.Factory
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.SampleModel;
import java.awt.image.ShortLookupTable;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;


//Third-party libraries
import com.mortennobel.imagescaling.ResampleOp;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;



/** 
 * Utility class. Applies some basic filtering methods and affine 
 * transformations to a {@link BufferedImage}.
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

	/** Identifies the default icon for an image. */
	public static final int IMAGE_ICON = IconManager.BROKEN_FILE_96;

	/** Identifies the default icon for an experimenter. */
	public static final int EXPERIMENTER_ICON = IconManager.PERSONAL_96;

	/** The default width of the icon. */
	public static final int DEFAULT_ICON_WIDTH = 16;

	/** The default height of the icon. */
	public static final int DEFAULT_ICON_HEIGHT = 16;

	/** The default width of a thumbnail. */
	public static final int THUMB_DEFAULT_WIDTH = 96;

	/** The default width of a thumbnail. */
	public static final int THUMB_DEFAULT_HEIGHT = 96;

	/** The red mask. */
	public static final int RED_MASK = 0x00ff0000;

	/** The green mask. */
	public static final int GREEN_MASK = 0x0000ff00;

	/** The blue mask. */
	public static final int BLUE_MASK = 0x000000ff;

	/** The blank mask. */
	public static final int BLANK_MASK = 0x00000000;

	/** Indicates that the text will be added in the top-left corner. */ 
	public static final int LOC_TOP_LEFT = 0;

	/** Indicates that the text will be added in the top-right corner. */
	public static final int LOC_TOP_RIGHT = 1;

	/** Indicates that the text will be added in the bottom-left corner. */
	public static final int LOC_BOTTOM_LEFT = 2;

	/** Indicates that the text will be added in the bottom-right corner. */
	public static final int LOC_BOTTOM_RIGHT = 3;

	/** The default width and height of a thumbnail. */
	public static final int DEFAULT_THUMB = 96;

	/** Border added to the text. */
	private static final int BORDER = 2;

	/** The default message for the default thumbnail. */
	private static final String DEFAULT_TEXT = "No thumbnail";

	/** Default text drawn on thumbnail before retrieval. */
	private static final String LOADING_TEXT = "Loading...";

	/** The RGB masks. */
	public static final int[] RGB = {RED_MASK, GREEN_MASK, BLUE_MASK};

	/** Color of the rainbow starting 
	 * from <code>red</code> to 
	 * <code>blue</code>. */
	public static final Color[] RED_TO_BLUE_RAINBOW = {Color.red, Color.orange,
		Color.yellow, Color.green, Color.blue};

	/** The violet color. */
	public static final Color VIOLET = new Color(138, 43, 226);

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
	 * Resizes an image using a Graphics2D object.
	 * 
	 * @param src The image to scale.
	 * @param width The desired width.
	 * @param height The desired height.
	 * @return See above.
	 */
	private static Image scaleImage(Image src, int width, int height)
	{
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, width, height);
		g2.drawImage(src, 0, 0, width, height, null);
		g2.dispose();
		src = null;
		return img;
	}

	/**
	 * Creates a default thumbnail image.
	 * 
	 * @param width The required width of the thumbnail.
	 * @param height The required height of the thumbnail.
	 * @return See above.
	 */
	public static BufferedImage createDefaultImageThumbnail(int width,
			int height)
	{
		if (width == 0) width = THUMB_DEFAULT_WIDTH;
		if (height == 0) height = THUMB_DEFAULT_HEIGHT;
		return createDefaultThumbnail(width, height, null);
	}

	/**
	 * Creates a default thumbnail image.
	 * 
	 * @param icon One of the following {@link #IMAGE_ICON},
	 * 				{@link #EXPERIMENTER_ICON} or <code>-1</code>;
	 * @return See above.
	 */
	public static BufferedImage createDefaultImageThumbnail(int icon)
	{
		IconManager icons = IconManager.getInstance();
		ImageIcon img = null;
		Color background = Color.BLACK;
		switch (icon) {
		case IMAGE_ICON:
			background = null;
			img = icons.getImageIcon(IconManager.BROKEN_FILE_96);
			break;
		case EXPERIMENTER_ICON:
			background = null;
			img = icons.getImageIcon(IconManager.PERSONAL_96);
			break;
		}
		if (img == null) return createDefaultThumbnail(96, 96, null);
		int h = img.getIconHeight();
		int w = img.getIconWidth();
		BufferedImage thumbPix = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) thumbPix.getGraphics();
		if (background != null) g.setBackground(background);
		g.drawImage(img.getImage(), 0, 0, null);
		g.dispose();
		img = null;
		return thumbPix;
	}

	/**
	 * Creates a default thumbnail image.
	 * 
	 * @param sizeX The width of the thumbnail
	 * @param sizeY The height of the thumbnail.
	 * @param text The text to draw.
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
			g.dispose();
		}
		return thumbPix;
	}

	/**
	 * Creates a default thumbnail whose width is {@link #DEFAULT_THUMB}
	 * and height is {@link #DEFAULT_THUMB} and the passed message.
	 * 
	 * @param text The text to set.
	 * @return See above,
	 */
	public static BufferedImage createDefaultThumbnail(String text)
	{
		return createDefaultThumbnail(DEFAULT_THUMB, DEFAULT_THUMB,
				text);
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
         * @param img The buffered image to magnify.
         * @param level The magnification factor.
         * @param w Extra space, necessary b/c of the lens option.
         * @return The magnified image.
         */
        public static BufferedImage magnifyImage(BufferedImage img, double level,
                        int w)
        {
                return magnifyImage(img, level, w, true);
        }
        
	/** 
	 * Magnifies the specified {@link BufferedImage}.
	 * 
	 * @param img The buffered image to magnify.
	 * @param level The magnification factor.
	 * @param w Extra space, necessary b/c of the lens option.
	 * @param interpolate Turns interpolation on or off
	 * @return The magnified image.
	 */
	public static BufferedImage magnifyImage(BufferedImage img, double level,
			int w, boolean interpolate)
	{
		if (img == null) return null;
		int width = (int) (img.getWidth()*level)+w;
		int height = (int) (img.getHeight()*level)+w;
		
		// image must be at least 3x3px
		if (width < 3)
			width = 3;
		if (height < 3)
			height = 3;

		if (interpolate && img.getWidth() >= 3 && img.getHeight() >= 3) {
		    ResampleOp  resampleOp = new ResampleOp(width, height);
                    return resampleOp.filter(img, null);
		}
		else {
			// Use plain Graphics2D, as ResampleOp apparently doesn't provide an option
			// for disabling interpolation; also have to use Graphics2D for
			// images < 3px (ResampleOp will fail in this case)
		    BufferedImage result = new BufferedImage(width, height, img.getType());
		    Graphics2D g = result.createGraphics();
		    g.getRenderingHints().add(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
		    g.drawImage(img, 0, 0, width, height, 0, 0, img.getWidth(), img.getHeight(), null);
		    return result;
		}
	}

	/** 
	 * Magnifies the specified image.
	 * 
	 * @param f The magnification factor.
	 * @param img The image to magnify.
	 * @return The magnified image.
	 */
	public static BufferedImage magnifyImage(double f, BufferedImage img)
	{
		if (img == null) return null;

		AffineTransform at = new AffineTransform();
		at.scale(f, f);
		int type = img.getType();
		if (type == BufferedImage.TYPE_CUSTOM)
			type = BufferedImage.TYPE_INT_ARGB;
		Rectangle bounds = img.getRaster().getBounds();
		bounds = at.createTransformedShape(bounds).getBounds();
		BufferedImage rescaleBuff;
		rescaleBuff = new BufferedImage(bounds.width, 
				bounds.height, type); //img.getType()
		Graphics2D g2 = rescaleBuff.createGraphics();
		g2.drawImage(img, at, null);
		g2.dispose();
		img.flush();
		return rescaleBuff;
	}

	/** 
	 * Applies a sharpen filter or a low_pass filter.
	 * 
	 * @param img The image to transform.
	 * @param filter The filter to apply.
	 * @return The transformed image.
	 */
	public static BufferedImage convolveImage(BufferedImage img, float[] filter)
	{
		if (img == null) return null;
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
	 * @param img The original buffered image.
	 * @param text The text to add.
	 * @param indexLocation The location of the text.
	 * @param c The color of the text.
	 * @return  A new buffered image.
	 */
	public static BufferedImage createImageWithText(BufferedImage img,
			String text, int indexLocation, Color c)
	{
		if (img == null) return null;
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
		img.flush();
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
		//TODO: ONLY NEED THIS METHOD AS THE TIFF WRITER DOES NOT WORK WITH 
		//BLITTED IMAGES.
		if (img == null) return null;
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
		img.flush();
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
	 * Creates a {@link BufferedImage} from an Image.
	 *  
	 * @param img The Image to convert to a buffered image
	 * @return The buffered image
	 */
	public static BufferedImage createImage(Image img)
	{
		if (img == null) return null;
		BufferedImage buff = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics gfx = buff.createGraphics();
		gfx.drawImage(img, 0, 0, null);
		gfx.dispose();
		img.flush();
		return buff;
	}

	/**
	 * Creates a {@link BufferedImage} from an Image.
	 *  
	 * @param img The BufferedImage to copy to a buffered image
	 * @return The buffered image
	 */
	public static  BufferedImage copyBufferedImage(BufferedImage img)
	{
		if (img == null) return null;
		BufferedImage buff = new BufferedImage(img.getWidth(null),
				img.getHeight(null), img.getType());
		Graphics gfx = buff.createGraphics();
		gfx.drawImage(img, 0, 0, null);
		gfx.dispose();
		img.flush();
		return buff;
	}

	/**
	 * Creates a buffer image from the specified <code>DataBuffer</code>.
	 * 
	 * @param buf The buffer to handle.
	 * @param bits The number of bits in the pixel values.
	 * @param sizeX The width (in pixels) of the region of image data described.
	 * @param sizeY The height (in pixels) of the region of image data 
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
	 * @param buf The buffer to handle.
	 * @param bits The number of bits in the pixel values.
	 * @param masks The bit masks for all bands.
	 * @param sizeX The width (in pixels) of the region of image data described.
	 * @param sizeY The height (in pixels) of the region of image data 
	 * 				described.
	 * @return See above.
	 */
	public static BufferedImage createImage(DataBuffer buf, int bits,
			int[] masks, int sizeX,  int sizeY)
	{
		if (buf instanceof DataBufferInt) {
			DataBufferInt db = (DataBufferInt) buf;
			final SampleModel sm = new SinglePixelPackedSampleModel(
							DataBuffer.TYPE_INT, sizeX, sizeY, sizeX, masks);
			final WritableRaster raster = Raster.createWritableRaster(sm, db,
							new Point(0, 0));

			final ColorModel colorModel = new DirectColorModel(bits, masks[0],
					masks[1], masks[2]);
			return new BufferedImage(colorModel, raster, false, null);
		}
		return null;
	}

	/**
	 * Creates a buffer image from the specified <code>array</code> of 
	 * integers.
	 * 
	 * @param buf The array to handle.
	 * @param bits The number of bits in the pixel values.
	 * @param sizeX The width (in pixels) of the region of image data described.
	 * @param sizeY The height (in pixels) of the region of image data
	 * 				described.
	 * @return See above.
	 */
	public static BufferedImage createImage(int[] buf, int bits, int sizeX,
			int sizeY)
	{
		if (buf == null) return null;
		DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY);
		return createImage(j2DBuf, bits, sizeX, sizeY);
	}

	/**
	 * Creates a buffer image from the specified <code>array</code> of
	 * integers.
	 * 
	 * @param buf The array to handle.
	 * @param bits The number of bits in the pixel values.
	 * @param masks The bit masks for all bands.
	 * @param sizeX The width (in pixels) of the region of image data described.
	 * @param sizeY The height (in pixels) of the region of image data 
	 * 				described.
	 * @return See above.
	 */
	public static BufferedImage createImage(int[] buf, int bits, int[] masks,
			int sizeX, int sizeY)
	{
		if (buf == null) return null;
		DataBuffer j2DBuf = new DataBufferInt(buf, sizeX*sizeY);
		return createImage(j2DBuf, bits, masks, sizeX, sizeY);
	}

	/**
	 * Determines the size of the thumbnail.
	 * 
	 * @param sizeX The thumbnail's size along the X-axis.
	 * @param sizeY The thumbnail's size along the Y-axis.
	 * @param realSizeX The real size along the X-axis.
	 * @param realSizeY The real size along the Y-axis.
	 * @return See above.
	 */
	public static Dimension computeThumbnailSize(int sizeX, int sizeY,
			double realSizeX, double realSizeY)
	{
		double ratio = 0;
		int value = 0;
		if (realSizeY != 0) ratio = realSizeX/realSizeY;
		if (sizeX <= 0 && sizeY <= 0)
			return new Dimension(THUMB_DEFAULT_WIDTH, THUMB_DEFAULT_HEIGHT);
		else if (sizeX <= 0 && sizeY > 0)
			return new Dimension(THUMB_DEFAULT_WIDTH, sizeY);
		else if (sizeX > 0 && sizeY <= 0)
			return new Dimension(sizeX, THUMB_DEFAULT_HEIGHT);
		if (ratio < 1) {
			value = (int) (sizeX*ratio);
			if (value != 0) return new Dimension(value, sizeY);
			return new Dimension(THUMB_DEFAULT_WIDTH, sizeY);
		} else if (ratio > 1 && ratio != 0) {
			value = (int) (sizeY*1/ratio);
			if (value != 0) return new Dimension(sizeX, value);
			return new Dimension(sizeX, THUMB_DEFAULT_HEIGHT);
		}
		return new Dimension(sizeX, sizeY);
	}

	/**
	 * Creates the splash screen logo and login
	 * 
	 * @param name The name of the image.
	 * @param path The path to the configuration file.
	 * @return See above.
	 */
	public static Icon createIcon(String name, String path)
	{
		StringBuffer buf;
		if (name == null || path == null) return null;
		buf = new StringBuffer(path);
		buf.append(File.separatorChar);
		buf.append(name);
		try {
			Image img = Toolkit.getDefaultToolkit().getImage(buf.toString());
			if (img == null) return null;
			Icon icon = new ImageIcon(img);
			if (icon.getIconHeight() <= 0 || icon.getIconWidth() <= 0)
				return null;
			img = null;
			return icon;
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Creates the splash screen logo and login
	 * 
	 * @param absolutePath The path to the file.
	 * @return See above.
	 */
	public static Icon createIcon(String absolutePath)
	{
		StringBuffer buf;
		if (absolutePath == null) return null;
		buf = new StringBuffer(absolutePath);
		try {
			Image img = Toolkit.getDefaultToolkit().getImage(buf.toString());
			if (img == null) return null;
			Icon icon = new ImageIcon(img);
			if (icon.getIconHeight() <= 0 || icon.getIconWidth() <= 0)
				return null;
			return icon;
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Creates the splash screen logo and login
	 * 
	 * @param absolutePath The path to the file.
	 * @param width The width of the icon.
	 * @param height The height of the icon.
	 * @return See above.
	 */
	public static Icon createIcon(String absolutePath, int width, int height)
	{
		StringBuffer buf;
		if (absolutePath == null) return null;
		buf = new StringBuffer(absolutePath);
		try {
			Image img = Toolkit.getDefaultToolkit().getImage(buf.toString());
			if (img == null) return null;
			Icon icon = new ImageIcon(img);
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			if (w <= 0 || h <= 0) return null;
			if (w > width && h > height)
				return new ImageIcon(scaleImage(img, width, height));
			if (w > width && h <= height) {
				double r = ((double) w)/h;
				int value = (int) (w*r);
				if (value != 0) 
					return new ImageIcon(scaleImage(img, value, height));
				return new ImageIcon(scaleImage(img, width, height));
			}
			if (w <= width && h >  height) {
				double r = ((double) w)/h;
				int value = (int) (h*1/r);
				if (value != 0) 
					return new ImageIcon(scaleImage(img, width, value));
				return new ImageIcon(scaleImage(img, width, height));
			}
			return icon;
		} catch (Exception e) {}
		return null;
	}

	/**
	 * Scales the passed icon.
	 * 
	 * @param icon 	The icon to scale.
	 * @return See above.
	 */
	public static Icon scaleIcon(Icon icon)
	{
		return scaleIcon(icon, DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT);
	}

	/**
	 * Scales the passed icon.
	 * 
	 * @param icon 	The icon to scale.
	 * @param width	The width of the new icon.
	 * @param height The height of the new icon.
	 * @return See above.
	 */
	public static Icon scaleIcon(Icon icon, int width, int height)
	{
		if (icon == null) return null;
		if (width <= 0) width = DEFAULT_ICON_WIDTH;
		if (height <= 0) height = DEFAULT_ICON_HEIGHT;
		ImageIcon img = (ImageIcon) icon;
		BufferedImage bi = new BufferedImage(width,
				height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		g.drawImage(img.getImage(), 0, 0, width, height, null);
		return (Icon) (new ImageIcon(bi));
	}

	/**
	 * Scales the passed buffered image.
	 * 
	 * @param image The image to scale.
	 * @param width The width of the new image.
	 * @param height The height of the new image.
	 * @return See above.
	 */
	public static BufferedImage scaleBufferedImage(BufferedImage image, int
			width, int height)
	{

		if (image == null) return null;
		if (width <= 0) width = DEFAULT_ICON_WIDTH;
		if (height <= 0) height = DEFAULT_ICON_HEIGHT;
		ColorModel cm = image.getColorModel();
		WritableRaster r = cm.createCompatibleWritableRaster(width, height);
		BufferedImage thumbImage = new BufferedImage(cm, r, false, null);

		// Do the actual scaling and return the result
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.drawImage(image, 0, 0, width, height, null);
		return thumbImage;
	}

	/**
	 * Scales the passed buffered image.
	 * 
	 * @param image The image to scale.
	 * @param maxLength The maxLength of the new image.
	 * @return See above.
	 */
	public static BufferedImage scaleBufferedImage(BufferedImage image, int
			maxLength)
	{
		if (image == null) return null;
		int w = image.getWidth();
		int h = image.getHeight();
		if (w == maxLength && h == maxLength) return image;
		Dimension d = computeThumbnailSize(maxLength, maxLength, w, h);
		ColorModel cm = image.getColorModel();
		WritableRaster r = cm.createCompatibleWritableRaster(d.width, d.height);
		BufferedImage thumbImage = new BufferedImage(cm, r, false, null);

		// Do the actual scaling and return the result
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.drawImage(image, 0, 0, d.width, d.height, null);
		return thumbImage;
	}


	/**
	 * Creates a buffered image.
	 * 
	 * @param buf The buffer hosting the data.
	 * @param sizeX The image's width.
	 * @param sizeY The image's height.
	 * @param redMask The mask applied on the red component.
	 * @param greenMask The mask applied on the green component.
	 * @param blueMask The mask applied on the blue component.
	 * @return See above.
	 */
	public static BufferedImage createBandImage(DataBuffer buf, int sizeX,
			int sizeY, int redMask, int greenMask, int blueMask)
	{
		if (buf == null) return null;
		int[] masks = {redMask, greenMask, blueMask};

		switch (buf.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			DataBufferByte bufferByte = (DataBufferByte) buf;
			byte[] values = bufferByte.getData();
			int i = 0, j = 0, l = values.length/3;
			int[] buffer = new int[l];
			while (i<l)
				buffer[i++] = (values[j++] & 0xff) |
				(values[j++] & 0xff <<8) | (values[j++] & 0xff <<16);
			return Factory.createImage(buffer, 24, masks, sizeX, sizeY);
		case DataBuffer.TYPE_INT:
			return Factory.createImage(buf, 32, masks, sizeX, sizeY);
		}
		return null;
	}

	/**
	 * Creates a buffered image.
	 * 
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param pixels The data to use.
	 * @return See above.
	 */
	public static BufferedImage create(int width, int height, int[] pixels)
	{
		if (pixels == null) return null;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	/**
	 * Creates a buffered image from the passed image file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	public static BufferedImage createImage(File file)
	{
		if (file == null) return null;
		try {
			Image img = ImageIO.read(file);
			BufferedImage image = new BufferedImage(img.getWidth(null),
					img.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(img, null, null);
			g.dispose();
			img = null;
			return image;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Maps from 4 ints to 4 byte colour.
	 * 
	 * @param a The alpha component, value in [0..255].
	 * @param r The red component, value in [0..255].
	 * @param g The greed component, value in [0..255].
	 * @param b The blue component, value in [0..255].
	 * @return 4 byte int composed of the 4 params, Alpha-Red-Green-Blue.
	 */
	public static int makeARGB(int a, int r, int g, int b)
	{
		return a << 24 | r << 16 | g << 8 | b;
	}

	/**
	 * Creates a buffered image of the specified size using the color
	 * of the rainbow going from the <code>Red</code> to <code>blue</code>.
	 * 
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return See above.
	 */
	public static BufferedImage createGradientImage(int width, int height)
	{
		return createGradientImage(width, height, RED_TO_BLUE_RAINBOW);
	}

	/**
	 * Creates a buffered image of the specified size using the colors.
	 * 
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param colors The colors to use.
	 * @return See above.
	 */
	public static BufferedImage createGradientImage(int width, int height,
			Color[] colors)
	{
		if (width < 1 || height < 1)  return null;
		if (colors == null || colors.length < 2)
			colors = RED_TO_BLUE_RAINBOW;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = (Graphics2D) image.getGraphics();
		int x = 0;
		int y = 0;
		int n = colors.length;
		int l = width/(n-1);
		if (l < 1) l = 1;
		GradientPaint paint;
		for (int i = 0; i < colors.length-1; i++) {
			paint = new GradientPaint(x, y, colors[i], x+l, y, colors[i+1]);
			g2D.setPaint(paint);
			g2D.fill(new Rectangle(x, y, l, height));
			x += l;
		}
		return image;
	}

	/**
	 * Applies a look up operation to the specified image.
	 * 
	 * @param image The image to handle.
	 * @return See above.
	 */
	public static Image makeConstrastDecImage(Image image)
	{
		if (image == null) return null;
		short values[] = new short[256];
		short v;
		for (int i = 0; i < 256; i++) {
			v = (short) (i/1.5);
			if (v > 255) v = 255;
			else if (v < 0) v = 0;
			values[i] = v;
		}
		ShortLookupTable lookupTable = new ShortLookupTable(0, values);
		BufferedImage img = new BufferedImage(image.getWidth(null),
				image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics g = img.createGraphics();
		g.drawImage(image, 0, 0, null);
		LookupOp lop = new LookupOp(lookupTable, null);
		lop.filter(img, img);
		return img;
	}

}
