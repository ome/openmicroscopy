/*
 * org.openmicroscopy.shoola.examples.browser.BrowserCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.examples.browser;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the images contained in a dataset.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class BrowserCanvas 
	extends JPanel
{

	/** The images to display. */
	private List<BufferedImage> images;
	
	/** Creates a new instance. */
	BrowserCanvas()
	{
		setDoubleBuffered(true);
		setBackground(Color.white);
		Dimension d = new Dimension(300, 500);
		setSize(d);
		setPreferredSize(d);
	}
	
	/**
	 * Sets the images to display.
	 * 
	 * @param images The images to display.
	 */
	void setImages(List<BufferedImage> images)
	{
		this.images = images;
		repaint();
	}
	
    /**
     * Overridden to paint the images.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
    	if (images == null || images.size() == 0) return;
    	Graphics2D g2D = (Graphics2D) g;
    	g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    			RenderingHints.VALUE_ANTIALIAS_ON);
    	g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
    			RenderingHints.VALUE_RENDER_QUALITY);
    	g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
    			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	//layout the images.
    	g2D.setColor(getBackground());
    	g2D.fillRect(0, 0, getWidth(), getHeight());
    	Iterator<BufferedImage> i = images.iterator();
    	int x = 0;
    	int y = 0;
    	int w = 0;
    	int h = 0;
    	BufferedImage image;
    	int width = getWidth();
    	int maxY = 0;
    	int gap = 2;
    	while (i.hasNext()) {
    		image = i.next();
			h = image.getHeight();
			w = image.getWidth();
			if (maxY < h) maxY = h;
			if (x != 0) {
				if (x+w > width) {
					x = 0;
					y += maxY;
					y += gap;
					maxY = 0;
				}
			}
			g2D.drawImage(image, x, y, null);
			x += w;
			x += gap;
		}
    }
    
    
}
