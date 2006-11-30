/*
 * org.openmicroscopy.shoola.agents.hiviewer.saver.PreviewCanvas
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

package org.openmicroscopy.shoola.agents.hiviewer.saver;




//Java imports
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * The canvas on which the previewed image is painted.
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
class PreviewCanvas
    extends JPanel
{

    /** Reference to the view. */
    private Preview view;
    
    /** 
     * Creates a new instance. 
     * 
     * @param view Reference to the view. Mustn't be <code>null</code>.
     */
    PreviewCanvas(Preview view)
    {
        if (view == null) throw new IllegalArgumentException("No view.");
        this.view = view;
        setDoubleBuffered(true);
    }
    
    /** 
     * Paints the image.
     */
    void paintImage()
    {
        BufferedImage image = view.getImage();
        if (image == null) return;
        Dimension d = new Dimension(image.getWidth(), image.getHeight());
        setSize(d);
        setPreferredSize(d);
    }
    
    /** Overrides the {@link #paintComponent(Graphics)} method. */
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
        BufferedImage image = view.getImage();
        if (image != null) g2D.drawImage(image, null, 0, 0);
    }
    
}
