/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.GlassPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Draws a rectangular overlay to indicate the selection.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
class GlassPane 
	extends JPanel
{

	/** The color to indicate the selection.*/
	private static Color SELECTION = new Color(Color.GRAY.getRed(),
			Color.GRAY.getGreen(), Color.GRAY.getBlue(), 100);
	
	/** The rectangle used to select multiple nodes.*/
    private Rectangle selection;
    
    /** Creates a new instance.*/
    GlassPane()
    {
    	setOpaque(false);
    }
    
    /**
     * Sets the selection.
     * 
     * @param selection The value to set.
     */
	void setSelection(Rectangle selection)
	{
		this.selection = selection;
		repaint();
	}
	
	/**
	 * Overridden to draw the selection.
	 * @see JPanel#paintComponents(Graphics)
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (selection == null) return;
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(SELECTION);
		g2D.fillRect(selection.x, selection.y, selection.width,
				selection.height);
	}

}
