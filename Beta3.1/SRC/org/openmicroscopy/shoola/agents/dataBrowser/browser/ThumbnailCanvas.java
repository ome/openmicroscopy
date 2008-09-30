/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.ThumbnailCanvas 
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Customizes <code>JPanel</code> to paint an image on an {@link ImageNode}.
 *
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
class ThumbnailCanvas     
	extends JPanel
{

    /** The node for which we're painting the image. */
    private ImageNode   imageFrame;
    
    /**
     * Creates a new instance.
     * 
     * @param node  The node on which the image is painted.
     *              Mustn't be <code>null</code>.
     */
    ThumbnailCanvas(ImageNode node)
    {
        if (node == null) throw new NullPointerException("No image node.");
        imageFrame = node;
        setDoubleBuffered(true);
    }
    
    /** 
     * Overridden to paint the image. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Thumbnail thumb = imageFrame.getThumbnail();
        BufferedImage img = thumb.getDisplayedImage();
        if (img == null) return;
        ((Graphics2D) g).drawImage(img, null, 0, 0);
    }
    
}
