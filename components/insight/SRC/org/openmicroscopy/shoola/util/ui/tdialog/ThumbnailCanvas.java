/*
 * org.openmicroscopy.shoola.util.ui.tdialog.ThumbnailCanvas
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.tdialog;


//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies

/** 
 * Custom <code>JComponent</code> to paint the thumbnail.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4695 $ $Date: 2006-12-15 17:08:05 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
class ThumbnailCanvas
    extends JComponent
{

    /** The {@link BufferedImage} to paint. */
    private BufferedImage   	image;
    
    /**
     * Creates a new instance. 
     * 
     * @param image The {@link BufferedImage} to paint. 
     */
    ThumbnailCanvas(BufferedImage image)
    {
        setOpaque(false);
        this.image = image;
    }
    
    /** 
     * Sets the image to paint.
     * 
     * @param image The {@link BufferedImage} to paint. 
     */
    void setImage(BufferedImage image) { this.image = image; }
	
    /**
     * Overridden to paint the thumbnail.
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        if (image != null) {
            Insets i = getInsets();
            g2D.drawImage(image, null, i.left+TinyDialogUI.INNER_PADDING, 
                            i.top+TinyDialogUI.INNER_PADDING);
        }  
    }

}
