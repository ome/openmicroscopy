/*
 * org.openmicroscopy.shoola.agents.roi.canvas.DrawingCanvasMng
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgt;
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;
import org.openmicroscopy.shoola.agents.roi.defs.ROIShape;
import org.openmicroscopy.shoola.agents.viewer.defs.ImageAffineTransform;

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
public class DrawingCanvasMng
    implements MouseListener, MouseMotionListener
{
    
    /** Default line color. */
    public static final Color   LINE_COLOR = Color.RED;
    
    private static final int    DEFAULT_CURSOR = 0, HAND_CURSOR = 1;
    
    private Cursor              handCursor, defaultCursor;
    
    /** Reference to the {@link DrawingCanvas view}. */
    private DrawingCanvas       view;
   
    /** Reference to the {@link ROIAgtCtrl control}. */
    private ROIAgtCtrl          control;
    
    /** Control to handle dragged event. */
    private boolean             dragging;
    
    /** Control to handle pressed event. */
    private boolean             pressed;
    
    /** Control to display annotation info. */
    private boolean             annotationOnOff;
    
    /** Control to handle ROI moved. */
    private boolean             moving;
    
    private Point               anchor;
    
    private int                 state;
    
    private int                 shapeType;
    
    private int                 channelIndex;
    
    private ROIShape            currentROI;
    
    private Shape               currentShape;
    
    private List                listROI, listROIErase;
    
    private Color               lineColor;
    
    private Rectangle           drawingArea;
    
    private int                 xControl, yControl;
    
    private boolean             erase, eraseAll;

    public DrawingCanvasMng(DrawingCanvas view)
    {
        this.view = view;
        drawingArea = new Rectangle();
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        handCursor = new Cursor(Cursor.HAND_CURSOR);
        attachListeners();
        setDefault(); 
    }
    
    /** Attach mouse listener. */
    private void attachListeners()
    {
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }
    
    public boolean isAnnotationOnOff() { return annotationOnOff; }
    
    /** Set the drawing area. */
    public void setDrawingArea(Rectangle area)
    {
        drawingArea.setBounds(0, 0, area.width, area.height);
        //size the drawing canvas.
        view.setPreferredSize(new Dimension(area.width, area.height));
        view.setBounds(area);
    }

    /** Set the default. */
    public void setDefault()
    {
        lineColor = LINE_COLOR;
        state = ROIAgt.CONSTRUCTING;  
        shapeType = ROIFactory.RECTANGLE;
        listROI = new ArrayList(); 
        listROIErase = new ArrayList();
        view.setTextOnOff(false);
    }
    
    ImageAffineTransform getCurrentImageAffineTransform()
    {
        return control.getImageAffineTransform();
    }
    
    boolean isMoving() { return moving; }
    
    int getShapeType() { return shapeType; }
    
    Color getLineColor() { return lineColor; }
    
    List getListROI() { return listROI; }
    
    public void setControl(ROIAgtCtrl control) { this.control = control; }
    
    public void setOnOff(boolean b) { view.setOnOff(b); }
    
    public void setTextOnOff(boolean b)
    {
        view.setTextOnOff(b);
        view.repaint();
    }
    
    public void setAnnotationOnOff(boolean b)
    {
        annotationOnOff = b;
        view.repaint();
    }
   
    public ROIShape getCurrentROI() { return currentROI; }
    
    /** Restore the erase roi. */
    public void undoErase()
    {
       //get the last one.
        int  n = listROIErase.size()-1;
        if (n >= 0) {
            ROIShape roi = (ROIShape) listROIErase.get(n);
            if (erase && roi != null) undoEraseOne(roi);
            else if (eraseAll && roi != null) undoEraseAll(roi);
        }
    }

    /** Remove the current ROI. */
    public void erase()
    {
        erase = true;
        if (eraseAll) listROIErase.removeAll(listROIErase);
        eraseAll = false;
        listROIErase.add(currentROI);
        int index = currentROI.getIndex();
        listROI.remove(currentROI);
        Iterator i = listROI.iterator();
        ROIShape roi;
        int j;
        while (i.hasNext()) {
            roi = (ROIShape) i.next();
            j = roi.getIndex();
            if (j > index) {
                j--;
                roi.setIndex(j);
                roi.setLabel("#"+j);
            }
        }
        //Select the last ROI if any left.
        int n = listROI.size()-1;
        if (n >= 0) {
            currentROI = (ROIShape) listROI.get(n);
            currentShape = currentROI.getShape();
            view.setIndexSelected(currentROI.getIndex());
        } else {
            currentROI = null;
            currentShape = null;
            view.erase(); 
        }
    }
    
    /** Remove all ROIs. */
    public void eraseAll()
    {
        eraseAll = true;
        if (erase) listROIErase.removeAll(listROIErase);
        erase = false;
        listROIErase.addAll(listROI);
        listROI.removeAll(listROI);
        currentROI = null;
        currentShape = null;
        view.erase();
    }
    
    /** Set the color of the ROI. */
    public void setLineColor(Color lineColor)
    {
        this.lineColor = lineColor;
    }
    
    /** Set the ROI type. */
    public void setType(int type)
    {
        if (type == ROIFactory.RECTANGLE || type == ROIFactory.ELLIPSE) 
            shapeType = type;
    }
    
    public void setState(int state)
    { 
        if (state == ROIAgt.CONSTRUCTING || state == ROIAgt.MOVING ||
            state == ROIAgt.RESIZING)
            this.state = state; 
    }
    
    public void setChannelIndex(int index) { channelIndex = index; }
    
    /** Mouse pressed event. */
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging && drawingArea.contains(p)) 
            handleMousePressed(p, e.getClickCount());
    }

    /** Mouse dragged event. */
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging && drawingArea.contains(p)) {
            pressed = false;
            handleMouseDrag(p);
        }       
    }
    
    /** Mouse released event. */
    public void mouseReleased(MouseEvent e)
    {
        dragging = false;
        moving = false;
        setCursor(DEFAULT_CURSOR); 
        switch (state) {
            case ROIAgt.CONSTRUCTING: 
                if (!pressed) saveROI();
                break;
            case ROIAgt.MOVING: 
            case ROIAgt.RESIZING: 
                
                if (currentShape != null) saveShape();   
        } 
        pressed = false;
    }
    
    /** Handle mouse moved event on existing ROIShape. */
    public void mouseMoved(MouseEvent e)
    {
        if (annotationOnOff) {
            Iterator i = listROI.iterator();
            ROIShape roi;
            Shape s;
            String txt;
            dragging = false;
            control.setAnnotation(null);
            while (i.hasNext()) {
                roi = (ROIShape) (i.next());
                txt = roi.getAnnotation();
                s = roi.getShape();
                if (s.contains(e.getPoint())){
                    if (txt != null &&  txt.length() != 0)
                        txt = roi.getLabel()+" "+txt;
                    else txt = null;
                    control.setAnnotation(txt);
                } 
            }     
        }
    }
    
    /** Restore the last erased shape if the erase button was pressed. */
    private void undoEraseOne(ROIShape roi)
    {
        int index = roi.getIndex();
        Iterator i = listROI.iterator();
        ROIShape r;
        int j;
        while (i.hasNext()) {
            r = (ROIShape) i.next();
            j = r.getIndex();
            if (j >= index) {
                j++;
                r.setIndex(j);
                r.setLabel("#"+j);
            }
        }
        listROI.add(roi);
        listROIErase.remove(roi);
        currentROI = roi;
        currentShape = roi.getShape();
        if (listROIErase.size() == 0) erase = false;
        view.setIndexSelected(index);
    }
    
    /** Restore the last erased shapes if the eraseAll button was pressed. */
    private void undoEraseAll(ROIShape roi)
    {
        int n = listROIErase.size();
        Iterator i = listROI.iterator();
        ROIShape r;
        while (i.hasNext()) {
            r = (ROIShape) i.next();
            r.setIndex(n);
            r.setLabel("#"+n);
            n++;
        }
        listROI.addAll(listROIErase);
        listROIErase.removeAll(listROIErase);
        currentROI = roi;
        currentShape = roi.getShape();
        eraseAll = false;
        view.setIndexSelected(roi.getIndex());
    }
    
    /** Erase or not the selected shape. */
    private void handleMousePressed(Point p, int clickCount)
    {
        pressed = true;
        dragging = true;
        currentROI = null;
        currentShape = null;
        anchor = p;
        Iterator i = listROI.iterator();
        ROIShape roi;
        Shape s;
        if (state == ROIAgt.MOVING) setCursor(HAND_CURSOR);
        else setCursor(DEFAULT_CURSOR);
        while (i.hasNext()) {
            roi = (ROIShape) (i.next());
            s = roi.getShape();
            if (s.contains(p)) {
                currentROI = roi;
                currentShape = s;
                Rectangle r = s.getBounds();
                xControl = r.x;
                yControl = r.y;
                view.setIndexSelected(roi.getIndex());
                if (clickCount == 2) {
                    dragging = false;
                    control.annotateROI(roi);
                }
            }
        }
    }
    
    /** Handle mouse dragged event. */
    private void handleMouseDrag(Point p)
    {
        switch (state) {
            case ROIAgt.CONSTRUCTING:
                currentShape = ROIFactory.makeShape(anchor, p, shapeType);
                view.draw(currentShape);
                break;
            case ROIAgt.MOVING: 
                if (currentShape != null) move(p);
                break;
            case ROIAgt.RESIZING: 
                if (currentShape != null) resize(p);
                break;    
        }
    }

    /** Same the modified existing shape. */
    private void saveShape()
    {
        currentROI.setShape(currentShape);
        view.setIndexSelected(currentROI.getIndex());   
    }
    
    /** Build a ROIShape object, and draw it. */
    private void saveROI()
    {
        int index = listROI.size();
        ImageAffineTransform at = control.getImageAffineTransform();
        ROIShape roi = new ROIShape(currentShape, index, lineColor, shapeType, 
                            channelIndex, at.copy());
        currentROI = roi;
        listROI.add(index, roi); 
        view.setIndexSelected(index);   
    }
    
    /** Move the current roi. */
    private void move(Point p)
    {
        int diffX = p.x-anchor.x, diffY = p.y-anchor.y;
        
        Rectangle r = currentShape.getBounds();
        int x = xControl+diffX, y = yControl+diffY,
            w = r.width,  h = r.height;
        if (areaValid(x, y, w, h)) {
            ROIFactory.setShapeBounds(currentShape, currentROI.getShapeType(), 
                                        x , y, w, h);
            moving = true;
            view.moveAndDraw(currentShape); 
        }
    }
    
    /** Check if the shape is still in the drawingArea. */
    private boolean areaValid(int x, int y, int w, int h)
    {
        boolean b = true;
        if (!drawingArea.contains(x, y)) b = false;
        if (!drawingArea.contains(x+w, y)) b = false;
        if (!drawingArea.contains(x, y+h)) b = false;
        return b;
    }
    
    /** Resize the current roi. */
    private void resize(Point p)
    {
        Rectangle r = currentShape.getBounds();
        int width = Math.abs(r.x-p.x), 
            height = Math.abs(r.y-p.y); 
        ROIFactory.setShapeBounds(currentShape, currentROI.getShapeType(), 
                                 r.x, r.y, width, height);
        moving = true;
        view.draw(currentShape); 
    }
    
    /** Set the cursor type according to event. */
    private void setCursor(int type)
    {
        Cursor c = null;
        if (type == DEFAULT_CURSOR) c = defaultCursor;
        else if (type == HAND_CURSOR) c = handCursor;
        if (c != null) view.setCursor(c);
    }
    
    /** 
     * Required by I/F but not actually needed in our case, 
     * no op implementation.
     */   
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by I/F but not actually needed in our case, 
     * no op implementation.
     */   
    public void mouseEntered(MouseEvent e) {}
    
    /** 
     * Required by I/F but not actually needed in our case,
     * no op implementation.
     */   
    public void mouseExited(MouseEvent e) {}
   
}

