/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ImageFactory;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

/** 
 * Canvas to display the selected buffered 2D-image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImageCanvas
    extends JPanel
{

    /** 
     * Width and height of the canvas. 
     * The <code>width</code> (resp. the <code>height</code>) is equal to 
     * <code>width</code> (resp. <code>height</code>) of the buffered image 
     * multiplied by the <code>magFactor</code>.
     */
    private int                         w, h;
    
    /** Magnification factor of the main image. */
    private double                      magFactor;
    
    /** Magnification factor of the pin image. */
    private double                      pinMagFactor;
    
    private BufferedImage               lensImage;
    
    private BufferedImage               displayImage;
    
    /** The original bufferedImage to display. */   
    private BufferedImage               image;
    
    /** Coordinates of the top-left corner of the canvas. */
    private int                         x, y;
    
    private int                         xTopCorner, yTopCorner;
    
    private int                         xLens, yLens;
    
    /** Reference to the {@link Viewer view}. */
    private ViewerUIF                   view;
    
    private ImageCanvasMng              manager;
    
    private ViewerCtrl                  control;
    
    private List                        filters;
    
    public ImageCanvas(ViewerUIF view, ViewerCtrl control)
    {
        this.view = view;
        this.control = control;
        filters = new ArrayList();
        manager = new ImageCanvasMng(this);
        magFactor = 1.0;
        setBackground(Viewer.BACKGROUND_COLOR); 
        setDoubleBuffered(true);
    }
    
    public ImageCanvasMng getManager() { return manager; }
    
    public BufferedImage getPinOnSide(boolean painting, Color c)
    {
        if (lensImage == null || displayImage == null) return null;
        int widthNew = (int) (manager.getWidth()/pinMagFactor);
        return ImageFactory.getImagePinOnSide(displayImage, lensImage, 
                xTopCorner, yTopCorner, widthNew, widthNew, painting, c);
    }
    
    /** 
     * Return a buffered image with databuffer. The pin area
     * is drawn on the image.
     */ 
    public BufferedImage getDisplayImageWithPinArea(boolean painting, Color c)
    {
        if (displayImage == null) return null;
        int widthNew = (int) (manager.getWidth()/pinMagFactor);
        return ImageFactory.getImageWithPinArea(displayImage, xTopCorner, 
                        yTopCorner, widthNew, widthNew, painting, c);
    }
    
    /** Compose the pin image and the main one. */
    public BufferedImage getPinOnImage()
    {
        if (lensImage == null || displayImage == null) return null;
        return ImageFactory.getImagePinOn(displayImage, lensImage, 
                                xLens-ViewerUIF.START, yLens-ViewerUIF.START);
    }
    
    /** Return a buffered image with databuffer of the pin image. */
    public BufferedImage getPinImage()
    {
        if (lensImage == null) return null;
        return ImageFactory.getImage(lensImage);
    }
    
    /** Return a buffered image with databuffer of the image. */
    public BufferedImage getDisplayImage()
    { 
        if (displayImage == null) return null;
        return ImageFactory.getImage(displayImage);
    }
     
    /** Reset the default. */
    public void resetMagFactor()
    { 
        magFactor = 1.0;
        image = null;
        displayImage = null;
    }
    
    /** 
     * Paint the image. 
     * 
     * @param image     buffered image to display.
     *  
     */ 
    public void paintImage(BufferedImage image)
    {
        this.image = image;
        resetLens();
        if (image != null) {
            displayImage = image;
            paintImage(magFactor, 
                    (int) (image.getWidth()*magFactor)+2*ViewerUIF.START, 
                    (int) (image.getHeight()*magFactor)+2*ViewerUIF.START);
        }
    } 
    
    /** 
     * Paint the zoomed image. 
     * 
     * @param level     zoom level, value between 
     *                  {@link ImageInspectorManager#MIN_ZOOM_LEVEL} and
     *                  {@link ImageInspectorManager#MAX_ZOOM_LEVEL}.
     * @param w         width of the zoomed image.
     * @param h         height of the zoomed image.
     *  
     */ 
    public void paintImage(double level, int w, int h)
    {
        magFactor = level;
        lensImage = null;
        this.w = w;
        this.h = h;
        manager.setDrawingArea(ViewerUIF.START, ViewerUIF.START,
                (int)(image.getWidth()*magFactor), 
                (int)(image.getHeight()*magFactor));
        AffineTransform at = new AffineTransform();
        at.scale(magFactor, magFactor);
        resetLens();
        displayImage = ImageFactory.magnifyImage(image, magFactor, at, 0);
        Iterator i = filters.iterator();
        while (i.hasNext()) {
            displayImage = ImageFactory.convolveImage(displayImage, 
                            (float[]) i.next());
            
        }
        repaint();
    } 

    public void paintFilterImage(float[] filter)
    {
        resetLens();
        filters.add(filter);
        displayImage = ImageFactory.convolveImage(displayImage, filter);
        repaint();
    }
    
    public void resetLens() { lensImage = null; }
    
    /** Reset the drawing area, easiest way to do it. */
    void resetDrawingArea()
    {
        manager.setDrawingArea(ViewerUIF.START, ViewerUIF.START,
                (int)(image.getWidth()*magFactor), 
                (int)(image.getHeight()*magFactor));
    }

    /** Reset the original image if some filters have been applied. */
    void resetImage()
    {
        if (image != null) {
            filters.removeAll(filters);
            paintImage(magFactor, 
                    (int) (image.getWidth()*magFactor)+2*ViewerUIF.START, 
                    (int) (image.getHeight()*magFactor)+2*ViewerUIF.START);
        }
    }

    /** 
     * Paint the lens image on top of the displayed image.
     * Note that we draw a square lens.
     * @param p               
     * @param lensWidth width of the lens.
     */
    void paintLensImage(double f, Point p, int lensWidth, boolean painting, 
                        Color c)
    {
        xLens = p.x-lensWidth/2;
        yLens = p.y-lensWidth/2;        
        pinMagFactor = f;
        int w = (int) (lensWidth/pinMagFactor);
        xTopCorner = p.x-w;
        yTopCorner = p.y-w;
        BufferedImage img = ImageFactory.getImage(displayImage, xTopCorner, 
                                            yTopCorner, w, w, painting, c);
        AffineTransform at = new AffineTransform();
        at.scale(f, f);
        lensImage = ImageFactory.magnifyImage(img, f, at, 0);
        Iterator i = filters.iterator();
        while (i.hasNext()) {
            lensImage = ImageFactory.convolveImage(lensImage, 
                                                (float[]) i.next());
            
        }
        repaint();
    }
    
    /** Return the lens image. */
    BufferedImage getLens() { return lensImage; }
    
    /** Overrides the paintComponent. */
    public void paint(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        setLocation();
        //Set bounds of the drawing area.
        control.setDrawingArea(x+ViewerUIF.START, y+ViewerUIF.START,
                (int)(image.getWidth()*magFactor), 
                (int)(image.getHeight()*magFactor));

        paintXYFrame(g2D);
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2D.setColor(Color.black);
        if (displayImage != null)
            g2D.drawImage(displayImage, null, ViewerUIF.START, ViewerUIF.START);
        if (lensImage != null)
            g2D.drawImage(lensImage, null, xLens, yLens);     
    }

    /** Paint the XY-frame. */
    private void paintXYFrame(Graphics2D g2D)
    {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        int hFont = fontMetrics.getHeight()/4;
        //x-axis
        int x1 = ViewerUIF.START-ViewerUIF.ORIGIN;
        int y1 = ViewerUIF.START-ViewerUIF.ORIGIN;
        g2D.drawLine(x1, y1, x1+ViewerUIF.LENGTH, y1);
        g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1-ViewerUIF.ARROW, 
                    x1+ViewerUIF.LENGTH, y1);
        g2D.drawLine(x1-ViewerUIF.ARROW+ViewerUIF.LENGTH, y1+ViewerUIF.ARROW, 
                    x1+ViewerUIF.LENGTH, y1);
        //y-axis
        g2D.drawLine(x1, y1, x1, ViewerUIF.LENGTH+y1);
        g2D.drawLine(x1-ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
                    x1, ViewerUIF.LENGTH+y1);
        g2D.drawLine(x1+ViewerUIF.ARROW, ViewerUIF.LENGTH+y1-ViewerUIF.ARROW, 
                    x1, ViewerUIF.LENGTH+y1);   
        //name
        g2D.drawString("o", x1-hFont, y1-hFont);
        g2D.drawString("x", x1+ViewerUIF.LENGTH/2, y1-hFont);
        g2D.drawString("y", x1-2*hFont, y1+ViewerUIF.LENGTH-hFont); 
    }
    
    /** Set the coordinates of the top-left corner of the image. */
    private void setLocation()
    {
        Rectangle r = view.getScrollPane().getViewportBorderBounds();
        x = ((r.width-w)/2);
        y = ((r.height-h)/2);
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        setBounds(x, y, w, h);
    }
    
}

