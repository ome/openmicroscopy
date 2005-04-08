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
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.defs.ScreenPlaneArea;
import org.openmicroscopy.shoola.agents.viewer.ImageFactory;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.RectangleArea;


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

    private BufferedImage               displayImage, zoomForLens;
    
    /** The original bufferedImage to display. */   
    private BufferedImage               image;
    
    /** Width and height of the canvas. */
    private int                         w, h, height;

    /** Coordinates of the top-left corner of the canvas. */
    private int                         x, y;
    
    /** Reference to the {@link Viewer view}. */
    private ViewerUIF                   view;
    
    private ImageCanvasMng              manager;
    
    private ViewerCtrl                  control;
    
    private ImageTransformMng           itMng;
    
    private int                         txtWidth;
    
    private double                      realValue;
    
    private LensCanvas                  lensCanvas;
    
    public ImageCanvas(ViewerUIF view, ViewerCtrl control, LensCanvas lensCanvas)
    {
        this.view = view;
        this.control = control;
        this.lensCanvas = lensCanvas;
        initTxtWidth();
        manager = new ImageCanvasMng(this, control);
        itMng = new ImageTransformMng();
        setBackground(ViewerUIF.BACKGROUND_COLOR); 
        setDoubleBuffered(true);
        realValue = 2*ViewerUIF.LENGTH/itMng.getMagFactor();
    }
    
    public void setUnitBarSize(double s)
    { 
        realValue = s/itMng.getMagFactor(); 
    }

    public ImageCanvasMng getManager() { return manager; }
    
    public BufferedImage getPinOnSideTopLeft(boolean painting, Color c)
    {
        if (lensCanvas.lensImage == null || displayImage == null) return null;
        Object[] r = prepareLensImage();
        return ImageFactory.getImagePinTopLeft(
                        paintNewImage(displayImage, null), 
                        paintNewLensImage((BufferedImage) r[0], false), 
                                (RectangleArea) r[1], painting, c);
    }
    
    public BufferedImage getPinOnSideTopRight(boolean painting, Color c)
    {
        if (lensCanvas.lensImage == null || displayImage == null) return null;
        Object[] r = prepareLensImage();
        return ImageFactory.getImagePinTopRight(
                        paintNewImage(displayImage, null), 
                        paintNewLensImage((BufferedImage) r[0], false), 
                            (RectangleArea) r[1], painting, c);
    }
    
    public BufferedImage getPinOnSideBottomLeft(boolean painting, Color c)
    {
        if (lensCanvas.lensImage == null || displayImage == null) return null;
        Object[] r = prepareLensImage();
        return ImageFactory.getImagePinBottomLeft(
                    paintNewImage(displayImage, null), 
                    paintNewLensImage((BufferedImage) r[0], false), 
                        (RectangleArea) r[1], painting, c);
    }
    
    public BufferedImage getPinOnSideBottomRight(boolean painting, Color c)
    {
        if (lensCanvas.lensImage == null || displayImage == null) return null;
        Object[] r = prepareLensImage();
        return ImageFactory.getImagePinBottomRight(
                      paintNewImage(displayImage, null), 
                      paintNewLensImage((BufferedImage) r[0], false), 
                                         (RectangleArea) r[1], painting, c);
    }

    public BufferedImage getImageAndROIs(List rois)
    {
        if (displayImage == null) return null;
        return ImageFactory.getImage(paintNewImage(displayImage, rois));
    }
    
    /** 
     * Return a buffered image with databuffer. The pin area
     * is drawn on the image.
     */ 
    public BufferedImage getDisplayImageWithPinArea(boolean painting, Color c)
    {
        if (displayImage == null) return null;
        int widthNew = (int) (manager.getWidth()/manager.getMagFactorLens());
        RectangleArea pa = new RectangleArea(lensCanvas.xTopCorner, 
                                            lensCanvas.yTopCorner, 
                                            widthNew, widthNew);
        return ImageFactory.getImageWithPinArea(
                paintNewImage(displayImage, null), pa, painting, c);
    }
    
    /** Compose the pin image and the main one. */
    public BufferedImage getPinOnImage()
    {
        if (lensCanvas.lensImage == null || displayImage == null) return null;
        BufferedImage lImg = itMng.buildDisplayImage(lensCanvas.lensImage);
        return ImageFactory.getImagePinOn(paintNewImage(displayImage, null), 
                                paintNewLensImage(lImg, false), 
                                lensCanvas.xLens-ViewerUIF.START, 
                                lensCanvas.yLens-ViewerUIF.START);
    }
    
    /** Return a buffered image with databuffer of the pin image. */
    public BufferedImage getPinImage()
    {
        if (lensCanvas.lensImage == null) return null;
        BufferedImage lImg = itMng.buildDisplayImage(lensCanvas.lensImage);
        return ImageFactory.getImage(paintNewLensImage(lImg, true));
    }
    
    /** Return a buffered image with databuffer of the image. */
    public BufferedImage getDisplayImage()
    { 
        if (displayImage == null) return null;
        return ImageFactory.getImage(paintNewImage(displayImage, null));
    }

    /** Reset the default. */
    public void resetDefault()
    { 
        itMng.setDefault();
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
        if (image == null) return;
        displayImage = image;
        double f = itMng.getMagFactor();
        paintImage(f, (int) (image.getWidth()*f)+2*ViewerUIF.START, 
                    (int) (image.getHeight()*f)+2*ViewerUIF.START);
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
        this.w = w;
        this.h = h;
        height = h-2*ViewerUIF.START;
        manager.setZoomImageRatio(level/itMng.getMagFactor());
        manager.setDrawingArea(ViewerUIF.START, ViewerUIF.START,
                                            (int)(image.getWidth()*level), 
                                            (int)(image.getHeight()*level));
        realValue = realValue*itMng.getMagFactor()/level;
        itMng.setMagFactor(level);
        displayImage = itMng.buildDisplayImage(image);
        control.imageDisplayedUpdated(displayImage, level);
        repaint();
    } 

    /** Apply filter to the image displayed. */
    public void filterImage(float[] filter)
    {
        resetLens();
        displayImage = itMng.filterImage(displayImage, filter);
        control.imageDisplayedUpdated(displayImage, itMng.getMagFactor());
        repaint();
    }
    
    /** Roll back to the original image. */
    public void undoFiltering()
    {
        if (image == null) return;
        itMng.removeAllFilters();
        double f = itMng.getMagFactor();
        paintImage(f, (int) (image.getWidth()*f)+2*ViewerUIF.START, 
                (int) (image.getHeight()*f)+2*ViewerUIF.START);
    }
    
    public void resetLens() { lensCanvas.resetLens(); }
    
    /** Reset the drawing area, easiest way to do it. */
    void resetDrawingArea()
    {
        manager.setDrawingArea(ViewerUIF.START, ViewerUIF.START,
                            (int)(image.getWidth()*itMng.getMagFactor()), 
                            (int)(image.getHeight()*itMng.getMagFactor()));
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
        if (getLens() == null) {
            zoomForLens = itMng.buildDisplayImage(displayImage, f);
            lensCanvas.paintLensImage(f, p, lensWidth, painting, c, 
                                    zoomForLens);
        } else 
            lensCanvas.paintLensImage(f, p, lensWidth, painting, c, 
                                    zoomForLens);
    }
    
    /** Return the lens image. */
    BufferedImage getLens() { return lensCanvas.lensImage; }
    
    /** 
     * Return an Object array of length 2, first element contained a
     * bufferedImage, the second one the planeArea corresponding to the lens'
     * area.
     */
    private Object[] prepareLensImage()
    {
        Object[] results = new Object[2];
        int widthNew = (int) (manager.getWidth()/manager.getMagFactorLens());
        BufferedImage lImg = itMng.buildDisplayImage(lensCanvas.lensImage);
        RectangleArea pa = new RectangleArea(lensCanvas.xTopCorner, 
                        lensCanvas.yTopCorner, widthNew, 
                        widthNew);
        results[0] = lImg;
        results[1] = pa;
        return results;
    }
    
    /** Overrides the {@link #paint(Graphics)} method. */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        setLocation();
        //Set bounds of the drawing area.
        control.setImageArea(x+ViewerUIF.START, y+ViewerUIF.START,
                (int)(image.getWidth()*itMng.getMagFactor()), 
                (int)(image.getHeight()*itMng.getMagFactor()));
        
        //lens location
        lensCanvas.setImageCanvasCoordinates(x, y);
        
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2D.setColor(Color.black);
        paintXYFrame(g2D);
        if (realValue > 0)
              paintScaleBar(g2D, ViewerUIF.START, 3*ViewerUIF.START/2+height, 
                      2*ViewerUIF.LENGTH, 
                      ""+(int) (realValue*2*ViewerUIF.LENGTH)+ 
                      ViewerUIF.NANOMETER, Color.GRAY);
        if (displayImage != null)
            g2D.drawImage(displayImage, null, ViewerUIF.START, ViewerUIF.START);
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
    
    /** Paint the scaleBar. */
    private void paintScaleBar(Graphics2D g2, int xBar, int yBar, 
                               int lengthBar, String value, Color c)
    {
        FontMetrics fontMetrics = g2.getFontMetrics();
        int hFont = fontMetrics.getHeight()/3;
        int size = value.length()*txtWidth;
        g2.setColor(c);
        g2.drawString(value, xBar+lengthBar/2-size/2, yBar-hFont);
        g2.setStroke(ViewerUIF.SCALE_STROKE);
        g2.drawLine(xBar, yBar, xBar+lengthBar, yBar);
    }

    private BufferedImage paintNewImage(BufferedImage img, List overlays)
    {
        BufferedImage newImage = 
            (BufferedImage) createImage(img.getWidth(), img.getHeight());
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, null, 0, 0); 
        if (overlays != null) paintOverLays(g2, overlays);
        paintScaleBar(g2, ViewerUIF.SCALE_BORDER, 
                        img.getHeight()-ViewerUIF.SCALE_BORDER, 
                        2*ViewerUIF.LENGTH, 
                        ""+(int) (realValue*2*ViewerUIF.LENGTH)+ 
                        ViewerUIF.NANOMETER, ViewerUIF.SCALE_COLOR);
        return newImage;
    }

    private BufferedImage paintNewLensImage(BufferedImage img, boolean withBar)
    {
        BufferedImage newImage = 
            (BufferedImage) createImage(img.getWidth(), img.getHeight());
        Graphics2D g2 = (Graphics2D) newImage.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, null, 0, 0); 
        g2.setColor(ViewerUIF.SCALE_COLOR);
        if (withBar) {
            double v = realValue/manager.getMagFactorLens();
            v = v*img.getWidth()/3;
            paintScaleBar(g2, ViewerUIF.SCALE_BORDER, 
                    img.getHeight()-ViewerUIF.SCALE_BORDER, img.getWidth()/3, 
                    ""+(int) v, ViewerUIF.SCALE_COLOR);
        }                
        return newImage;
    }
    
    /** Paint the ROI selection on the image to save. */
    private void paintOverLays(Graphics2D g2, List overlays)
    {
        Iterator i = overlays.iterator();
        ScreenPlaneArea spa;
        PlaneArea pa;
        while (i.hasNext()) {
            spa = (ScreenPlaneArea) i.next();
            g2.setColor(spa.getAreaColor());
            if (spa.getPlaneArea() != null) {
                pa = (PlaneArea) spa.getPlaneArea().copy();
                pa.scale(itMng.getMagFactor());
                g2.draw(pa);
            }
        }
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
    
    /** Initializes the width of the text. */
    private void initTxtWidth()
    {
        txtWidth = getFontMetrics(getFont()).charWidth('m');
    }
    
}

