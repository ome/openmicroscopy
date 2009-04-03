/*
 * org.openmicroscopy.shoola.agents.imviewer.util.ImgSaverPreviewerCanvas
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

package org.openmicroscopy.shoola.agents.imviewer.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Component hosting the images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImgSaverPreviewerCanvas
    extends JPanel
{

    /** Reference to the parent. */
    private ImgSaverPreviewer   model;
    
    /**
     * Paints the images composing the main image.
     * 
     * @param g2D   The graphics context.
     * @param w     The location of the first image.
     * @return  The location of the main image.
     */
    private int paintComponents(Graphics2D g2D, int w)
    {
        Iterator i = model.getImageComponents().iterator();
        BufferedImage img;
        while (i.hasNext()) {
            img = (BufferedImage) i.next();
            g2D.drawImage(img, null, w, ImgSaverPreviewer.SPACE);
            w += (img.getWidth()+ImgSaverPreviewer.SPACE);
        }
        return w;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link ImgSaverPreviewer}.
     *              Mustn't be <code>null</code>.
     */
    ImgSaverPreviewerCanvas(ImgSaverPreviewer model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    
    /**
     * Overriden to paint the main image and its components if any.
     * @see JPanel#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        BufferedImage img = model.getImage();
        if (img != null) {
            int w = ImgSaverPreviewer.SPACE;
            if (model.getImageComponents() != null) w = paintComponents(g2D, w);
            g2D.drawImage(img, null, w, ImgSaverPreviewer.SPACE);
        }
    }
    
}
