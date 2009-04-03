/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.PreviewCanvas
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

package org.openmicroscopy.shoola.agents.viewer.canvas;

//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;

/** 
 * 
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
public class PreviewCanvas
    extends JPanel
{
    
    /**  Width and height of the canvas. */
    private int                 w, h;
    
    /** Coordinates of the top-left corner of the canvas. */
    private int                 x, y;
    
    /** The original bufferedImage to display. */   
    private BufferedImage       image, sideImage;
    
    
    private JScrollPane         scrollPane;
    
    public PreviewCanvas()
    {
        setBackground(ViewerUIF.BACKGROUND_COLOR); 
        setDoubleBuffered(true);
    }
    
    public void setContainer(JScrollPane c) { scrollPane = c; }
    
    /** 
     * Paint the specified image in the canvas
     * 
     * @param img   {@link BufferedImage img} to pain.
     * . */
    public void paintImage(BufferedImage image)
    {
        this.image = image;
        if (image != null) {
            w = image.getWidth()+2*ViewerUIF.START;
            h = image.getHeight()+2*ViewerUIF.START;
            repaint();
        }
    }
    
    public void paintImages(BufferedImage image, BufferedImage sideImage)
    {
        this.image = image;
        this.sideImage = sideImage;
        if (image != null && sideImage != null) {
            w = image.getWidth()+sideImage.getWidth()+3*ViewerUIF.START;
            h = image.getHeight()+sideImage.getHeight()+2*ViewerUIF.START;
            repaint();
        }
            
    }
    /** Overrides the {@link #paint(Graphics)} method. */
    public void paint(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        setLocation();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (image != null)
            g2D.drawImage(image, null, ViewerUIF.START, ViewerUIF.START);
        if (sideImage != null && image != null) 
            g2D.drawImage(sideImage, null, image.getWidth()+2*ViewerUIF.START, 
                    ViewerUIF.START);
    }
    
    /** Set the coordinates of the top-left corner of the image. */
    private void setLocation()
    {
        Rectangle r = scrollPane.getViewportBorderBounds();
        x = ((r.width-w)/2);
        y = ((r.height-h)/2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        setBounds(x, y, w, h);
    }
    
}
