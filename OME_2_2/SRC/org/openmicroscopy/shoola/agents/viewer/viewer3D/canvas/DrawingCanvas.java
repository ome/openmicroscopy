/*
 * org.openmicroscopy.shoola.agents.viewer3D.canvas.DrawingCanvas
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

package org.openmicroscopy.shoola.agents.viewer.viewer3D.canvas;

//Java imports
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3D;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager;

/** 
 * A simple visual component to draw one shape at a time.
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
public class DrawingCanvas
    extends JComponent
{
    
    /** The model and controller component of this widget. */    
    private DrawingCanvasMng    manager;  
    
    /** current selected point. */
    private Point               currentPointXY, currentPointZY, currentPointXZ;
    
    /** Width and height of the XY-image.  */
    private int                 width, height;
    
    /** width (resp. height) of the ZYimage (resp. XZimage) */
    private int                 zWidth;
    
    /** 
     * x-coordinate of the top-left corner of the XYImage
     * (Same value for y-coordinate).
     */
    private int                 x;
    
    /** Control to paint or not the line on the images. */
    private boolean             linesShown;
    
    private int                 defaultZ, maxZ, curValueXZ, curValueZY;
    
    public DrawingCanvas(Viewer3DManager control, int defaultZ, int sizeZ)
    {
        this.defaultZ = defaultZ;
        maxZ = sizeZ;
        manager = new DrawingCanvasMng(this, control);
        currentPointXY = null;
        linesShown = false;
        setOpaque(false);
        currentPointXY = new Point(0, 0);
        currentPointXZ = new Point(0, 0);
        currentPointZY = new Point(0, 0);
    }
    
    public DrawingCanvasMng getManager() { return manager; }
    
    /** 
     * Set the dimension of the image. 
     * 
     * @param width     Width of the XYImage.
     * @param height    Height of the XYImage.
     * @param zWidth    Width of the ZYImage = Height of  the XZImage.
     */
    public void setSizes(int width, int height, int zWidth) 
    {
        this.width = width;
        this.height = height;
        this.zWidth = zWidth;
        curValueXZ = (zWidth*defaultZ/maxZ);
        curValueZY = (zWidth*defaultZ/maxZ);
        currentPointXZ.y = curValueXZ+Viewer3D.SPACE;
        currentPointZY.x = curValueZY+Viewer3D.SPACE;
        x = 2*Viewer3D.SPACE+zWidth;
    }
    
    /** Erases current shape, if any. */
    void erase()
    {
        linesShown = false;
        repaint();
    }
    
    /** 
     * Set the current points.
     * 
     * @param p   The selected point on the XYimage.
     */
    void drawXY(Point p)
    {
        if (p != null) {
            linesShown = true;
            currentPointXY.x = p.x;
            currentPointXY.y = p.y;
        }
    }
    
    /** 
     * Set the current points.
     * 
     * @param p   The selected point on the XZimage.
     */
    void drawXZ(Point p)
    {
        if (p != null) {
            linesShown = true;
            currentPointXZ.y = p.y;
            currentPointXY.x = p.x;
            curValueZY = p.y-Viewer3D.SPACE;
            curValueXZ = p.y-Viewer3D.SPACE;
        }
    }
    
    /** 
     * Set the current points.
     * 
     * @param p   The selected point on the ZYimage.
     */
    void drawZY(Point p)
    {
        if (p != null) {
            linesShown = true;
            currentPointZY.x = p.x;
            currentPointXY.y = p.y;
            curValueXZ = p.x-Viewer3D.SPACE;
            curValueZY = p.x-Viewer3D.SPACE;
        } 
    }
    
    /** Overrides the paintComponent. */
    public void paintComponent(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        if (linesShown) {
            paintXY(g2D);
            paintZ(g2D);
        }
    }

    /** Paint the X and Y lines on the 3 2Dimages. */
    private void paintXY(Graphics2D g2D)
    {
        g2D.setColor(Viewer3D.YlineColor);
        //line on the XYimage.
        g2D.drawLine(currentPointXY.x, x, currentPointXY.x, x+height);
        //line on the XZimage
        g2D.drawLine(currentPointXY.x, x-zWidth-Viewer3D.SPACE, 
                        currentPointXY.x, x-Viewer3D.SPACE);         
        g2D.setColor(Viewer3D.XlineColor);
        //line on the ZYimage;
        g2D.drawLine(x-zWidth-Viewer3D.SPACE, currentPointXY.y, 
                        x-Viewer3D.SPACE, currentPointXY.y);
        //line on the XYimage.
        g2D.drawLine(x, currentPointXY.y, x+width, currentPointXY.y);
    }
    
    /** Paint the Zlines on the XZimage and ZYimage. */
    private void paintZ(Graphics2D g2D)
    {
        g2D.setColor(Viewer3D.ZlineColor);
        //line on the ZYimage.
        g2D.drawLine(currentPointZY.x, x, currentPointZY.x, x+height);
        //line on the XZimage.
        g2D.drawLine(x, currentPointXZ.y, x+width, currentPointXZ.y);
    }
    
}
