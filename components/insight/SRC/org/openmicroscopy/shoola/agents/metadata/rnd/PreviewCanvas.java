/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.PreviewCanvas 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

//Third-party libraries



//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.util.image.geom.Factory;

/** 
 * Component displaying the preview image. 
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
class PreviewCanvas 
	extends JPanel
{

	/** The image to paint. */
	private BufferedImage image;
	
	/** Creates a new instance. */
	PreviewCanvas()
	{
		setDoubleBuffered(true);
	}
	
	/**
	 * Sets the image to paint.
	 * 
	 * @param image The value to set.
	 */
	void setImage(BufferedImage image) 
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
        if (image == null)
            return;

        Dimension d = getSize();
        double xFactor = (double)d.width/(double)image.getWidth();
        double yFactor = (double)d.height/(double)image.getHeight();
        double factor = xFactor < yFactor ? xFactor : yFactor;
        int w = (int)(image.getWidth()*factor);
        int h = (int)(image.getHeight()*factor);
        BufferedImage scaledImage = Factory.scaleBufferedImage(image, w, h);

        int x = (d.width - scaledImage.getWidth()) / 2;
        int y = (d.height - scaledImage.getHeight()) / 2;

        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        g2D.drawImage(scaledImage, null, x, y);
        g2D.dispose();
    }
    
}
