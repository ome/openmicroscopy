/*
 * org.openmicroscopy.shoola.examples.viewer.ImageCanvas 
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
package org.openmicroscopy.shoola.examples.viewer;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Paints the image.
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
class ImageCanvas 
	extends JPanel
	implements ImageCanvasInterface
{

	/** The image to display. */
	private BufferedImage image;
	
	/** Creates a new instance. */
	ImageCanvas()
	{
		setDoubleBuffered(true);
	}
	
    /**
     * Implemented as specified by the {@link ImageCanvasInterface} I/F.
     * @see ImageCanvasInterface#setImage(BufferedImage)
     */
	public void setImage(BufferedImage image)
	{
		this.image = image;
		repaint();
	}
	
    /**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
    	if (image == null) return;
    	Graphics2D g2D = (Graphics2D) g;
    	g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    			RenderingHints.VALUE_ANTIALIAS_ON);
    	g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
    			RenderingHints.VALUE_RENDER_QUALITY);
    	g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
    			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	g2D.drawImage(image, null, 0, 0); 
    }

    /**
     * Implemented as specified by the {@link ImageCanvasInterface} I/F.
     * @see ImageCanvasInterface#getCanvas()
     */
	public Component getCanvas()
	{
		return this;
	}

	/**
	 * Implemented as specified by the {@link ImageCanvasInterface} I/F.
	 * @see ImageCanvasInterface#setCanvasSize(Dimension)
	 */
	public void setCanvasSize(Dimension d)
	{
		setPreferredSize(d);
		setSize(d);
	}
       
}
