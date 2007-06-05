/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GridCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Paints the main image and the split channels.
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
class GridCanvas 
	extends ImageCanvas
{
    
    /** 
     * Paints the image.
     * 
     * @param g2D	The graphics context.
     * @param w		The width of an image composing the grid.
     * @param h		The height of an image composing the grid.
     * @param bar	Pass <code>true</code> to paint the scale bar,
     * 				<code>false</code> otherwise.
     */
    private void paintImage(Graphics2D g2D, int w, int h, boolean bar)
	{
		List images = model.getGridImages();
    	if (images == null) return; 
    	SplitImage combined = null;
		g2D.setColor(model.getBackgroundColor());
    	Dimension d = getSize();
        g2D.fillRect(0, 0, d.width, d.height);
    	int n = images.size();
    	if (n <=3) n = 4;
    	if (n > 4 && n%2 != 0) {
    		combined = (SplitImage) images.get(images.size()-1);
    		images.remove(images.size()-1);
    	}
    	n = (int) Math.floor(Math.sqrt(n));
        Iterator channels = images.iterator();
        BufferedImage image;
        int x = 0, y = 0;
        SplitImage channel;
        String v = model.getUnitBarValue(); 
        int s = (int) (model.getUnitBarSize()*Browser.RATIO);
        Color c = model.getUnitBarColor();
        FontMetrics fm = getFontMetrics(getFont());
        int textWidth;
        boolean text = model.isTextVisible();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (!channels.hasNext()) return; //Done
                channel = (SplitImage) channels.next();
                image = channel.getImage();
                x = j*(w+BrowserModel.GAP);
                if (image != null) {
                	g2D.drawImage(image, null, x, y);
                	//draw string.
                	if (text) {
                		textWidth = fm.stringWidth(channel.getName());
                    	g2D.setColor(BACKGROUND);
                    	g2D.fillRect(x, y, textWidth+4, 3*height/2);
                    	g2D.setColor(getBackground());
                        g2D.drawString(channel.getName(), x+2, y+height);
                	}
                    if (bar && v != null && s < w) 
                    	ImagePaintingFactory.paintScaleBar(g2D, x+w-s-5, y+h-5, 
                    							s, v, c);
                    
                } else { //just paint rectangle.
                	g2D.setColor(BACKGROUND);
                	g2D.drawRect(x, y, w-1, h-1);
                	//g2D.setColor(getBackground());
                    g2D.drawString(channel.getName(), x+2, y+height);
                }
            }
            x = 0;
            y = (i+1)*(h+BrowserModel.GAP);
        }   
        if (combined != null) {
        	image = combined.getImage();
        	y = 0;
        	x = n*(w+BrowserModel.GAP);
        	if (image != null) {
        		g2D.drawImage(image, null, x, y);
            	//draw string.
            	if (text) {
            		textWidth = fm.stringWidth(combined.getName());
                	g2D.setColor(BACKGROUND);
                	g2D.fillRect(x, y, textWidth+4, 3*height/2);
                	g2D.setColor(getBackground());
                    g2D.drawString(combined.getName(), x+2, y+height);
            	}
                if (bar && v != null && s < w) 
                	ImagePaintingFactory.paintScaleBar(g2D, x+w-s-5, y+h-5, 
                							s, v, c);
        	}
        }
	}

	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	GridCanvas(BrowserModel model)
	{
		super(model); 
	}
	
	/**
	 * Creates and returns an image representing the grid.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage()
	{
		BufferedImage gridImage;
		BufferedImage original = model.getAnnotateImage();
    	int w = original.getWidth();
    	int h = original.getHeight();
		if (model.getRGBSplit()) 
	    	gridImage = new BufferedImage(2*w+BrowserModel.GAP, 
	    					2*h+BrowserModel.GAP, BufferedImage.TYPE_INT_RGB);
	    		
		else {
			Dimension d = getSize();
			gridImage = new BufferedImage(d.width, d.height, 
											BufferedImage.TYPE_INT_RGB);
		
		}
		Graphics2D g2D = (Graphics2D) gridImage.getGraphics();
		ImagePaintingFactory.setGraphicRenderingSettings(g2D);
		paintImage(g2D, w, h, model.isUnitBar());
		return gridImage;
	}

	/**
	 * Returns the location of the point of the top-left corner of the 
	 * the rectangle with respect to the image coordinate system if the 
	 * passed rectangle is contained in an image composing the grid.
	 * Returns <code>null</code> otherwise.
	 * 
	 * @param r The rectangle to handle.
	 * @return See above.
	 */
	Point isOnImageInGrid(Rectangle r)
	{
		List images = model.getGridImages();
    	if (images == null) return null; 
    	SplitImage combined = null;
    	int n = images.size();
    	if (n <=3) n = 4;
    	if (n > 4 && n%2 != 0) {
    		combined = (SplitImage) images.get(images.size()-1);
    		images.remove(images.size()-1);
    	}
    	n = (int) Math.floor(Math.sqrt(n));
        Iterator channels = images.iterator();
        BufferedImage image;
        int x = 0, y = 0;
        SplitImage channel;
        BufferedImage original = model.getAnnotateImage();
    	int w = original.getWidth(), h = original.getHeight();
    	Rectangle imageRectangle;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (!channels.hasNext()) break; //Done
                channel = (SplitImage) channels.next();
                image = channel.getImage();
                x = j*(w+BrowserModel.GAP);
                if (image != null) {
                	imageRectangle = new Rectangle (x, y, w, h); 
                	if (imageRectangle.contains(r)) {
                		return new Point(r.x-imageRectangle.x, 
                						r.y-imageRectangle.y);
                		
                	}
                } 
            }
            x = 0;
            y = (i+1)*(h+BrowserModel.GAP);
        }   
        if (combined != null) {
        	image = combined.getImage();
        	y = 0;
        	x = n*(w+BrowserModel.GAP);
        	if (image != null) {
        		imageRectangle = new Rectangle (x, y, w, h); 
        		if (imageRectangle.contains(r)) {
            		return new Point(r.x-imageRectangle.x, 
            						r.y-imageRectangle.y);
            		
            	}
        	} return null;
        }
        return null;
	}
	
	/**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        BufferedImage original = model.getAnnotateImage();
    	paintImage(g2D, original.getWidth(), original.getHeight(), 
    				model.isUnitBar());
    }

	
    
}
