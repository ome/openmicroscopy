/*
 * org.openmicroscopy.shoola.agents.imviewer.util.HistoryItemCanvas 
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
package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * Customizes <code>JPanel</code> to paint an image on an {@link HistoryItem}.
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
class HistoryItemCanvas
	extends JPanel
{

	/** The node for which we're painting the image. */
	private HistoryItem node;

	/**
     * Creates a new instance.
     * 
     * @param node  	The node on which the image is painted.
     *              	Mustn't be <code>null</code>.
     */
	HistoryItemCanvas(HistoryItem node)
    {
        if (node == null) throw new NullPointerException("No image node.");
        this.node = node;
        setDoubleBuffered(true);
    }
    
    /** 
     * Overridden to paint the image. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        BufferedImage img = node.getThumbnail();
        if (img == null) return;
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(img, null, 0, 0);
    }
    
}
