/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.GridBICanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.Font;
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
 * Paints the split view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * </small>
 * @since 3.0-Beta4
 */
class GridBICanvas
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
		List images = model.getSplitImages();
    	if (images == null) return; 
    	SplitImage combined = null;
		g2D.setColor(model.getBackgroundColor());
    	Dimension d = getSize();
        g2D.fillRect(0, 0, d.width, d.height);
        int row = model.getGridRow();
        int colum = model.getGridColumn();
    	int n = model.getMaxC();
    	if (n == 1) row = 1;
    	else if (n == 3 || n == 2) row = 2;
    	else if (n >= 4) {
    		combined = (SplitImage) images.get(images.size()-1);
    		images.remove(images.size()-1);
    	}
        Iterator channels = images.iterator();
        BufferedImage image;
        
        SplitImage channel;
        String v = model.getUnitBarValue(); 
        int s = (int) (model.getGridBarSize());
        Color c = model.getUnitBarColor();
        Font font = getFont();;
        FontMetrics fm = getFontMetrics(font);
        int textWidth;
        boolean text = model.isTextVisible();
        String name;
        g2D.setColor(BACKGROUND);
        int widthGrid = w*colum;
        int heightGrid = h*row;
        int x = 0, y = 0;
        for (int i = 0; i <= row; ++i) {
        	g2D.drawLine(x, y, x+widthGrid, y);
            y = (i+1)*(h)-1;
        }
        x = 0;
        y = 0;
        for (int i = 0; i <= colum; ++i) {
        	g2D.drawLine(x, y, x, y+heightGrid);
            x = (i+1)*(w)-1;
        }
        x = 0;
        y = 0;

        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < colum; ++j) {
                if (channels.hasNext()) {
                	channel = (SplitImage) channels.next();
                    image = channel.getImage();
                    x = j*(w+BrowserModel.GAP);
                    if (image != null) {
                    	g2D.drawImage(image, null, x, y);
       
                    	//draw string.
                    	if (text) {
                    		name = channel.getName();
                    		textWidth = fm.stringWidth(name)+4;
                    		if (textWidth < w) {
                    			//g2D.setColor(BACKGROUND);
                            	//g2D.fillRect(x, y, textWidth, 3*height/2);
                            	g2D.setColor(getBackground());
                                g2D.drawString(name, x+2, y+height);
                    		}
                    	}
                        if (bar && v != null) {
                        	textWidth = fm.stringWidth(v)+4;
                        	if (textWidth < w/2)
                        		ImagePaintingFactory.paintScaleBar(g2D, x+w-s-5, 
                        									y+h-5, s, v, c);
                        }
                    } else { //just paint rectangle.
                    	//if (text) {
                    	name = channel.getName();
                    	//g2D.setColor(BACKGROUND);
                    	//g2D.drawRect(x, y, w-1, h-1);
                    	textWidth = fm.stringWidth(name)+4;
                    	if (textWidth < w && text) 
                    		g2D.drawString(name, x+2, y+height);
                    	//}
                    }
                }
                
            }
            x = 0;
            y = (i+1)*(h+BrowserModel.GAP);
        }  
        
        if (combined != null) {
        	image = combined.getImage();
        	y = 0;
        	x = colum*(w+BrowserModel.GAP);
        	if (image != null) {
        		g2D.drawImage(image, null, x, y);
            	
        		//g2D.drawImage(image, null, x, y);
            	//draw string.
            	if (text) {
            		name = combined.getName();
            		textWidth = fm.stringWidth(name)+4;
            		if (textWidth < w) {
            			//g2D.setColor(BACKGROUND);
                    	//g2D.fillRect(x, y, textWidth, 3*height/2);
                    	g2D.setColor(getBackground());
                        g2D.drawString(name, x+2, y+height);
            		}
            	}
            	if (bar && v != null) {
                	textWidth = fm.stringWidth(v)+4;
                	if (textWidth < w/2)
                		ImagePaintingFactory.paintScaleBar(g2D, x+w-s-5, 
                									y+h-5, s, v, c);
                }
        	} else {
        		name = combined.getName();
            	//g2D.setColor(BACKGROUND);
            	//g2D.drawRect(x, y, w-1, h-1);
            	textWidth = fm.stringWidth(name)+4;
            	if (textWidth < w && text) 
            		g2D.drawString(name, x+2, y+height);
        	}
        }
	}
	
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
	GridBICanvas(BrowserModel model, BrowserUI view)
    {
        super(model, view);
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
		List images = model.getSplitImages();
    	if (images == null) return null; 
    	SplitImage combined = null;
    	int n = images.size();
    	if (n <= 3) n = 4;
    	if (n > 4 && n%2 != 0) {
    		combined = (SplitImage) images.get(images.size()-1);
    		images.remove(images.size()-1);
    	}
    	n = (int) Math.floor(Math.sqrt(n));
        Iterator channels = images.iterator();
        BufferedImage image;
        int x = 0, y = 0;
        SplitImage channel;
        BufferedImage original = model.getCombinedImage();
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
	 * Creates and returns an image representing the grid.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage()
	{
		BufferedImage gridImage;
		BufferedImage original = model.getCombinedImage();
		if (original == null) return null;
    	int w = original.getWidth();
    	int h = original.getHeight();
    	Dimension d = getSize();
    	if (d.width == 0 || d.height == 0) return null;
		gridImage = new BufferedImage(d.width, d.height, 
										BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = (Graphics2D) gridImage.getGraphics();
		ImagePaintingFactory.setGraphicRenderingSettings(g2D);
		paintImage(g2D, w, h, model.isUnitBar());
		return gridImage;
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
        BufferedImage original = model.getCombinedImage();
        if (original == null) return;
    	paintImage(g2D, original.getWidth(), original.getHeight(), 
    				model.isUnitBar());
    }
    
}
