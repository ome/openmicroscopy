/*
 * org.openmicroscopy.shoola.agents.metadata.util.FigureCanvas 
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
package org.openmicroscopy.shoola.agents.metadata.util;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Display one of the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FigureCanvas
	extends JPanel
{

	/** The image to paint. */
	private BufferedImage  image;
	
	/** Indicates if the image should be visible. */
	private boolean			imageVisible;
	
	/** Creates a new instance. */
	FigureCanvas()
	{
		setBackground(getBackground());
		setDoubleBuffered(true);
		imageVisible = true;
	}
	
	/**
	 * Sets the image to paint.
	 * 
	 * @param image The image to paint.
	 */
	void setImage(BufferedImage image)
	{
		this.image = image;
		repaint();
	}
	
	/**
	 * Sets the flag.
	 * 
	 * @param imageVisible  Pass <code>true</code> to show the image 
	 * 						(if not <code>null</code>), <code>false</code>
	 * 						to hide it.
	 */
	void setImageVisible(boolean imageVisible)
	{
		this.imageVisible = imageVisible;
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
    	
    	if (image == null) {
    		Rectangle r = getBounds();
        	g2D.drawRect(0, 0, r.width-1, r.height-1);
        	return;
    	}
    	if (!imageVisible) {
    		g2D.drawRect(0, 0, image.getWidth()-1, image.getHeight()-1);
    		return;
    	}
    	g2D.drawImage(image, null, 0, 0); 
    }
    
}
