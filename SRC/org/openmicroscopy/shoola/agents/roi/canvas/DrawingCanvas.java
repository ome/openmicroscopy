/*
 * org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvas
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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIFactory;
import org.openmicroscopy.shoola.agents.roi.defs.ROIShape;


/** 
 * 
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

    private static final int        LENGTH = 6;
    
    private static final Color      squareColor = Color.LIGHT_GRAY;

    private DrawingCanvasMng        manager;
    
    private Shape                   currentShape;
    
    private boolean                 onOff, textOnOff;

    private Rectangle2D             square;           
    
    /** Index of the ROI selected. */
    private int                     indexSelected;
    
    private double                  magFactor;
    
    public DrawingCanvas()
    {
        indexSelected = -1;
        square = new Rectangle2D.Double(0, 0, LENGTH, LENGTH);
        manager = new DrawingCanvasMng(this);
    }

    public void setTextOnOff(boolean b) { textOnOff = b; }
    
    public void setOnOff(boolean b) { onOff = b; }
    
    public boolean getOnOff() { return onOff; }
    
    public DrawingCanvasMng getManager() { return manager; }
    
    /** Draw the specified shape on the canvas. */
    void draw(Shape roi)
    {
        currentShape = roi;
        indexSelected = -1;
        repaint();
    }
    
    void setIndexSelected(int index)
    {
        currentShape = null;
        indexSelected = index;
        repaint();
    }
    
    /** Erase selection, the current one or all. */
    void erase()
    {
        currentShape = null;
        repaint();
    }
    
    /** Overrides the paintComponent method. */
    public void paintComponent(Graphics g)
    {
        if (onOff) paintROI((Graphics2D) g);
    }
    
    private void paintROI(Graphics2D g2D)
    {
        if (currentShape != null) {
            if (!manager.isMoving()) g2D.setColor(manager.getLineColor());
            g2D.draw(currentShape);
        }
        List l = manager.getListROI();
        if (l.size()>0) paintROICollection(g2D, l);
    }
    
    /** Paint the existing ROI if any. */
    private void paintROICollection(Graphics2D g2D, List l)
    {
        Iterator i = l.iterator();
        ROIShape roi;
        Shape s;
        Rectangle r;
        magFactor = manager.getCurrentImageAffineTransform().getMagFactor();
        while (i.hasNext()) {
            roi = (ROIShape) i.next();
            setShapeBounds(roi);
            s = roi.getShape();
            g2D.setColor(roi.getLineColor());
            g2D.draw(s);
            r = s.getBounds();
            if (textOnOff) {
                Point p = ROIFactory.setLabelLocation(s, roi.getShapeType(), 
                                                        LENGTH);
                g2D.drawString(roi.getText(), p.x, p.y);
            }
            if (roi.getIndex() == indexSelected) {
                
                square.setRect(r.x-LENGTH/2, r.y+r.height/2-LENGTH/2, LENGTH,
                                LENGTH);
                g2D.setPaint(squareColor);
                g2D.fill(square);
            }
        }
    }
    
    private void setShapeBounds(ROIShape roi)
    {
        Shape shape = roi.getShape();
        Rectangle r = shape.getBounds();
        double factor = roi.getAffineTransform().getMagFactor();
        int shapeType = roi.getShapeType();
        double coeff = magFactor/factor;
        
        ROIFactory.setShapeBounds(shape, shapeType, (int) (r.x*coeff), 
                                (int) (r.y*coeff), (int) (r.width*coeff), 
                                (int) (r.height*coeff));
        roi.getAffineTransform().setMagFactor(magFactor);
        roi.setShape(shape);
    }
    
}

