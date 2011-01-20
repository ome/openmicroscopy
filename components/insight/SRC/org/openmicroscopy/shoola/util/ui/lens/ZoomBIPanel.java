/*
 * org.openmicroscopy.shoola.util.ui.lens.ZoomBIPanel 
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
package org.openmicroscopy.shoola.util.ui.lens;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Shows the zoomed image of the lens, in the centre of the UI.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * </small>
 * @since 3.0-Beta4
 */
class ZoomBIPanel 
	extends JPanel
{

	/** Used to clear the background of the panel. */
	private final static Color	CLEAR_COLOUR = new Color(0, 0, 0, 255);
	
	/** Reference to the model. */
	private LensModel 	model;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model.
	 */
	ZoomBIPanel(LensModel model)
	{
		this.model = model;
		setOpaque(true);
	}
	
	/**
     * Overridden to draw the zoomed image and the current position of the lens
     * on the canvas.
     * @see java.awt.Component#paint(java.awt.Graphics)
     */

    public void paint(Graphics g)
    {
        int w = this.getWidth();
        int h = this.getHeight();
        BufferedImage img = model.getZoomedImage();
        if (img == null) return;
        g.setColor(CLEAR_COLOUR);
        g.fillRect(0, 0, w, h);
        int x = (w/2)-img.getWidth()/2;
        int y = (h/2)-img.getHeight()/2;
        g.drawImage(img, x, y, img.getWidth(), img.getHeight(), null);
    }
    
}
