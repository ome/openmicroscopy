/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.LensCanvas
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ImageFactory;
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
public class LensCanvas
    extends JComponent
{
    
    BufferedImage           lensImage;
    
    int                     xTopCorner, yTopCorner, xLens, yLens;
    
    private int             xImageCanvas, yImageCanvas;
    
    public LensCanvas()
    {
        setDoubleBuffered(true);
        xImageCanvas = 0;
        yImageCanvas = 0;
    }
    
    void setImageCanvasCoordinates(int x, int y)
    {
        xImageCanvas = x;
        yImageCanvas = y;
    }
    
    void resetLens()
    { 
        lensImage = null;
        setSize(0, 0);
    }
    
    /** 
     * Paint the lens image on top of the displayed image.
     * Note that we draw a square lens.
     * @param p               
     * @param lensWidth width of the lens.
     */
    void paintLensImage(double f, Point p, int lensWidth, boolean painting, 
                        Color c, BufferedImage mainImage)
    {
        xLens = p.x-lensWidth/2;
        yLens = p.y-lensWidth/2;        
        int w = (int) (lensWidth/(2*f));
        xTopCorner = p.x-ViewerUIF.START-w;
        yTopCorner = p.y-ViewerUIF.START-w;
        int x = (int) ((p.x-ViewerUIF.START)*f)-lensWidth/2;
        int y = (int) ((p.y-ViewerUIF.START)*f)-lensWidth/2; 
        lensImage = ImageFactory.getImage(mainImage, x,  y, lensWidth, 
                                            lensWidth, painting, c);
        setPreferredSize(new Dimension(lensImage.getWidth(), 
                lensImage.getHeight()));
        setBounds(xImageCanvas+xLens, yImageCanvas+yLens, lensImage.getWidth(),
                    lensImage.getHeight());
        //repaint();
    }
    
    /** Overrides the {@link #paint(Graphics)} method. */
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
        if (lensImage != null)
            g2D.drawImage(lensImage, null, 0, 0);  
    }

}
