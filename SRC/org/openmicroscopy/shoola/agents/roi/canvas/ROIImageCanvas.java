/*
 * org.openmicroscopy.shoola.agents.roi.canvas.ROIImageCanvas
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

package org.openmicroscopy.shoola.agents.roi.canvas;

//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.pane.ROIViewerMng;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;

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
public class ROIImageCanvas
    extends JPanel
{
    
    /** Original ROIImage. */
    private BufferedImage   roiImage;
    
    private BufferedImage   displayedImage;
    
    /** 
     * Width and height of the canvas. 
     * The <code>width</code> (resp. the <code>height</code>) is equal to 
     * <code>width</code> (resp. <code>height</code>) of the buffered image 
     * multiplied by the <code>magFactor</code>.
     */
    private int             w, h;
    
    /** Coordinates of the top-left corner of the canvas. */
    private int              x, y;
    
    private ROIViewerMng    manager;
    
    private PlaneArea       clip;

    public ROIImageCanvas(ROIViewerMng manager) 
    {
        this.manager = manager;
        displayedImage = null;
        setBackground(ROIAgtUIF.BACKGROUND_COLOR);
        setDoubleBuffered(true);
    }
    
    public BufferedImage getROIImage() { return roiImage; }
    
    public void paintImage(BufferedImage roiImage, PlaneArea clip)
    {
        this.clip = clip;
        this.roiImage = roiImage;
        displayedImage = roiImage;
        if (roiImage != null) {
            double f = manager.getFactor();
            paintImage(f, (int) (roiImage.getWidth()*f), 
                    (int) (roiImage.getHeight()*f));
        } else repaint();
    }
    
    public void magnify(double f)
    {
        if (roiImage != null) 
            paintImage(f, (int) (roiImage.getWidth()*f), 
                    (int) (roiImage.getHeight()*f));
    }
    
    private void paintImage(double level, int w, int h)
    {
        this.w = w;
        this.h = h;
        AffineTransform at = new AffineTransform();
        at.scale(level, level);
        displayedImage = Factory.magnifyImage(roiImage, level, at, 0);
        repaint();
    }
    
    /** Overrides the paintComponent. */
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
        if (displayedImage != null) 
            g2D.drawImage(paintNewImage(), null, 0, 0); 
    }
    
    private BufferedImage paintNewImage()
    {
        BufferedImage newImage = (BufferedImage) createImage(w, h);
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        
        if (clip != null) {
            PlaneArea p = (PlaneArea) clip.copy();
            p.scale(manager.getFactor());
            g2.setClip(p);
        }
        g2.drawImage(displayedImage, null, 0, 0); 
        return newImage;
    }
    
    /** Set the location of the canvas. */
    private void setLocation()
    {
        Rectangle r = manager.getViewportBounds();
        x = ((r.width-w)/2);
        y = ((r.height-h)/2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        setBounds(x, y, w, h);
    }
    
}
