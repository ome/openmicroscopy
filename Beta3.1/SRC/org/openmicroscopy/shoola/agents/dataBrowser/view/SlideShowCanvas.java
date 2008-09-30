/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.SlideShowCanvas 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays a full size image.
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
class SlideShowCanvas 
	extends JComponent
{

	/** Bound property indicating to select the next image. */
	static final String		SELECT_NEXT_PROPERTY = "selectNext";
	
	/** The image to paint. */
	private BufferedImage image;
	
	/** Creates a new instance. */
	SlideShowCanvas()
	{
		setDoubleBuffered(true);
		setOpaque(false); 
		setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED, UIUtilities.INNER_BORDER_HIGHLIGHT, 
                UIUtilities.INNER_BORDER_SHADOW));
		addMouseListener(new MouseAdapter() {
		
			/**
			 * Fires a property to select the next image.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				firePropertyChange(SELECT_NEXT_PROPERTY, Boolean.FALSE, 
								Boolean.TRUE);
			}
		
		});
	}

	/**
	 * Paints the passed image.
	 * 
	 * @param image The image to paint.
	 */
	void paintImage(BufferedImage image)
	{
		if (image == null) return;
		this.image = image;
		Dimension d = new Dimension(image.getWidth(), image.getHeight());
		setPreferredSize(d);
		setSize(d);
		repaint();
	}
	
	 /** 
     * Overridden to paint the thumbnail.
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        if (image == null) return;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        g2D.drawImage(image, null,0, 0);
    }
    
}
