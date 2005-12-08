/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ThumbnailCanvas
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Custom <code>JComponent</code> to paint an image on an {@link ImageNode}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ThumbnailCanvas
    extends JPanel
{

    /** The node for which we're painting the image. */
    private ImageNode   imageFrame;
    
    
    /**
     * Creates a new instance.
     * 
     * @param node The node on which to paint the image.
     */
    ThumbnailCanvas(ImageNode node)
    {
        if (node == null) throw new NullPointerException("No image node.");
        imageFrame = node;
        setDoubleBuffered(true);
    }
    
    /** Overridden to paint the image. */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Thumbnail thumb = imageFrame.getThumbnail();
        BufferedImage img = thumb.getDisplayedImage();
        if (img == null) return;
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(img, null, 0, 0);
    }
    
}
