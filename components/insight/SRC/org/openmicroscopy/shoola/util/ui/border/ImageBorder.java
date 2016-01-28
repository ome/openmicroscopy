/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.util.ui.border;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import org.openmicroscopy.shoola.util.image.geom.Factory;

/**
 * This Border is constructed from a series of images, which are 
 * painted appropriately to form a border. 
 * 8 Images must be specified: One for each corner, and one for 
 * each side.
 * The center area of the border is not painted.
 * 
 * This code was taken from:
 * "Swing Hacks", by Joshua Marinacci and Chris Adamson. 
 * Copyright 2005 O'Reilly Media, Inc., 0-596-00907-0
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ImageBorder 
	extends AbstractBorder 
{
    
	/** The Image displayed in the centre of the top border. */
    private Image	top_center;
    
    /** The Image displayed on the left of the top border. */
    private Image	top_left;
    
    /** The Image displayed on the left of the top border. */
    private Image	top_right;
    
    /** The image used for the left side of the border. */
    private Image	left_center;
    
    /** The image used for the right side of the border. */
    private Image	right_center;
    
	/** The Image displayed in the centre of the bottom border. */
    private Image	bottom_center;
    
    /** The Image displayed on the left of the bottom border. */
    private Image	bottom_left;
    
    /** The Image displayed on the right of the bottom border. */
    private Image	bottom_right;
    
    /**
     * The border Insets. Can be manually set using {@link #setInsets(Insets)}
     * Otherwise, {@link Border#getBorderInsets(Component)} will return 
     * Insets calculated from the size of the side Images. 
     */
    private Insets	insets;
    
    /**
     * Creates an instance. 
     * 
     * @param top_left			The image at the top_left of the border
     * @param top_center		The image at the top_center of the border
     * @param top_right			The image at the top_right of the border
     * @param left_center		The image at the left_center of the border
     * @param right_center		The image at the right_center of the border
     * @param bottom_left		The image at the bottom_left of the border
     * @param bottom_center		The image at the bottom_center of the border
     * @param bottom_right		The image at the bottom_right of the border
     */
    public ImageBorder(Image top_left, Image top_center, Image top_right,
        Image left_center, Image right_center,
        Image bottom_left, Image bottom_center, Image bottom_right) 
    {
    	if (top_left == null)
    		throw new IllegalArgumentException("The top left image " +
    				"cannot be null.");
    	if (top_center == null)
    		throw new IllegalArgumentException("The top center image " +
    				"cannot be null.");
    	if (top_right == null)
    		throw new IllegalArgumentException("The top right image " +
    				"cannot be null.");
    	if (left_center == null)
    		throw new IllegalArgumentException("The left  center image " +
    				"cannot be null.");
    	if (right_center == null)
    		throw new IllegalArgumentException("The right  center image " +
    				"cannot be null.");
    	if (bottom_left == null)
    		throw new IllegalArgumentException("The bottom left image " +
    				"cannot be null.");
    	if (bottom_center == null)
    		throw new IllegalArgumentException("The bottom center image " +
    				"cannot be null.");
    	if (bottom_right == null)
    		throw new IllegalArgumentException("The bottom right image " +
    				"cannot be null.");
        this.top_left = top_left;
        this.top_center = top_center;
        this.top_right = top_right;
        this.left_center = left_center;
        this.right_center = right_center;
        this.bottom_left = bottom_left;
        this.bottom_center = bottom_center;
        this.bottom_right = bottom_right;
    }
    
    /** 
     * Sets the border Insets. (Optional)
     * Otherwise, {@link Border#getBorderInsets(Component)} will return 
     * Insets calculated from the size of the side Images. 
     * 
     * @param insets The value to set.
     */
    public void setInsets(Insets insets) 
    {
        this.insets = insets;
    }
    
    /**
     * Returns Insets calculated from the thickness of the side Images. 
     * Unless the insets have been manually set using {@link #setInsets(Insets)}
     * 
     * @return See above.
     */
    public Insets getBorderInsets(Component c) 
    {
        if (insets != null) return insets;
        return 
        new Insets(top_center.getHeight(null), left_center.getWidth(null),
                bottom_center.getHeight(null), right_center.getWidth(null));
    }
    
    /**
     * Paints the border using the images specified in the constructor.
     * 
     * @see AbstractBorder#paintBorder(Component, Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, 
    		int height) 
    {
        Graphics2D g2 = (Graphics2D) g;
        
        int tlw = top_left.getWidth(null);
        int tlh = top_left.getHeight(null);
        //int tcw = top_center.getWidth(null);
        int tch = top_center.getHeight(null);
        int trw = top_right.getWidth(null);
        int trh = top_right.getHeight(null);
        
        int lcw = left_center.getWidth(null);
        //int lch = left_center.getHeight(null);
        int rcw = right_center.getWidth(null);
        //int rch = right_center.getHeight(null);
        
        int blw = bottom_left.getWidth(null);
        int blh = bottom_left.getHeight(null);
        //int bcw = bottom_center.getWidth(null);
        int bch = bottom_center.getHeight(null);
        int brw = bottom_right.getWidth(null);
        int brh = bottom_right.getHeight(null);
        
        fillTexture(g2, top_left, x, y, tlw, tlh);
        fillTexture(g2, top_center, x+tlw, y, width-tlw-trw, tch);
        fillTexture(g2, top_right, x+width-trw, y, trw, trh);
        
        fillTexture(g2, left_center, x,	y+tlh, lcw, height-tlh-blh);
        fillTexture(g2, right_center, x+width-rcw, y+trh, rcw, height-trh-brh);
        
        fillTexture(g2, bottom_left, x, y+height-blh, blw, blh);
        fillTexture(g2, bottom_center, x+blw, y+height-bch, width-blw-brw, bch);
        fillTexture(g2, bottom_right, x+width-brw, y+height-brh, brw, brh);
    }
    
    /**
     * A helper method to fill a portion of a {@link Graphics2D} object with
     * a specified Image.
     * 
     * @param g2  The Graphics2D object to fill
     * @param img The image to paint into the Graphics2D object
     * @param x   The x coordinate of the area to paint
     * @param y   The y coordinate of the area to paint
     * @param w   The width of the area to paint
     * @param h The height of the area to paint
     */
    public static void fillTexture(Graphics2D g2, Image img, int x, int y, 
    							int w, int h) 
    {
        BufferedImage buff = Factory.createImage(img);
        Rectangle anchor = new Rectangle(x, y, img.getWidth(null),
        					img.getHeight(null));
        TexturePaint paint = new TexturePaint(buff,anchor);
        g2.setPaint(paint);
        g2.fillRect(x,y,w,h);
    }
}


