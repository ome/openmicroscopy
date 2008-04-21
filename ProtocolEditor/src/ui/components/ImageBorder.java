
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

package ui.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import util.ImageFactory;

/**
 * This code was taken from:
 * "Swing Hacks", by Joshua Marinacci and Chris Adamson. Copyright 2005 O'Reilly Media, Inc., 0-596-00907-0
 */
public class ImageBorder extends AbstractBorder {
    
    Image top_center, top_left, top_right;
    Image left_center, right_center;
    Image bottom_center, bottom_left, bottom_right;
    Insets insets;
    
    public static Border getImageBorder() {
    	return new ImageBorder( ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP_LEFT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP_RIGHT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_LEFT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_RIGHT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM_LEFT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM_RIGHT).getImage());
    }
    
    public static Border getImageBorderHighLight() {
    	return new ImageBorder( ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP_LEFT_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_TOP_RIGHT_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_LEFT_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_RIGHT_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM_LEFT_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM_HLT).getImage(),
    			ImageFactory.getInstance().getImageIcon(ImageFactory.BORDER_BOTTOM_RIGHT_HLT).getImage());
    }
    
    public ImageBorder(Image top_left, Image top_center, Image top_right,
        Image left_center, Image right_center,
        Image bottom_left, Image bottom_center, Image bottom_right) {
            
        this.top_left = top_left;
        this.top_center = top_center;
        this.top_right = top_right;
        this.left_center = left_center;
        this.right_center = right_center;
        this.bottom_left = bottom_left;
        this.bottom_center = bottom_center;
        this.bottom_right = bottom_right;
    }
    
    public void setInsets(Insets insets) {
        this.insets = insets;
    }
    
    public Insets getBorderInsets(Component c) {
        if(insets != null) {
            return insets;
        } else {
            return new Insets(top_center.getHeight(null),left_center.getWidth(null),
                bottom_center.getHeight(null), right_center.getWidth(null));
        }
    }
    
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        // g.setColor(Color.white);
        //g.fillRect(x,y,width,height);

        Graphics2D g2 = (Graphics2D)g;
        
        int tlw = top_left.getWidth(null);
        int tlh = top_left.getHeight(null);
        int tcw = top_center.getWidth(null);
        int tch = top_center.getHeight(null);
        int trw = top_right.getWidth(null);
        int trh = top_right.getHeight(null);
        
        int lcw = left_center.getWidth(null);
        int lch = left_center.getHeight(null);
        int rcw = right_center.getWidth(null);
        int rch = right_center.getHeight(null);
        
        int blw = bottom_left.getWidth(null);
        int blh = bottom_left.getHeight(null);
        int bcw = bottom_center.getWidth(null);
        int bch = bottom_center.getHeight(null);
        int brw = bottom_right.getWidth(null);
        int brh = bottom_right.getHeight(null);
        
        fillTexture(g2,top_left,      x,            
          y,            tlw,           tlh);
        fillTexture(g2,top_center,    x+tlw,        
          y,            width-tlw-trw, tch);
        fillTexture(g2,top_right,     x+width-trw,  y,            trw,           trh);
        
        fillTexture(g2,left_center,   x,            y+tlh,        lcw,           height-tlh-blh);
        fillTexture(g2,right_center,  x+width-rcw,  y+trh,        rcw,           height-trh-brh);
        
        fillTexture(g2,bottom_left,   x,            y+height-blh, blw,           blh);
        fillTexture(g2,bottom_center, x+blw,        y+height-bch, width-blw-brw, bch);
        fillTexture(g2,bottom_right,  x+width-brw,  y+height-brh, brw,           brh);
    }
    
    public void fillTexture(Graphics2D g2, Image img, int x, int y, int w, int h) {
        BufferedImage buff = createBufferedImage(img);
        Rectangle anchor = new Rectangle(x,y,
            img.getWidth(null),img.getHeight(null));
        TexturePaint paint = new TexturePaint(buff,anchor);
        g2.setPaint(paint);
        g2.fillRect(x,y,w,h);
    }

    public BufferedImage createBufferedImage(Image img) {
        BufferedImage buff = new BufferedImage(img.getWidth(null), 
            img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics gfx = buff.createGraphics();
        gfx.drawImage(img, 0, 0, null);
        gfx.dispose();
        return buff;
    }
}


