/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ProjectionCanvas 
 *
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
 */
package org.openmicroscopy.shoola.agents.imviewer.util.proj;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the projected image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class ProjectionCanvas 
	extends JPanel
{

	/** The image to paint. */
	private BufferedImage image;
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param background The background color.
	 */
	ProjectionCanvas(Color background)
	{
		setBackground(background);
		setDoubleBuffered(true);
	}
	
	/** 
	 * Sets the image to display.
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
        super.paintComponent(g);
       
        if (image == null) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
       
        g2D.drawImage(image, null, 0, 0); 
    }
    
}
