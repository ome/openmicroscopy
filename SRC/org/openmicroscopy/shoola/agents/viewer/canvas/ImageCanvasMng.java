/*
 * org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvasMng
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openmicroscopy.shoola.agents.viewer.ImageFactory;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.ViewerUIF;

//Third-party libraries

//Application-internal dependencies


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
public class ImageCanvasMng
    implements MouseListener, MouseMotionListener
{

    private ImageCanvas         view;
    
    private Rectangle           drawingArea;
    
    /** Control to handle dragged event. */
    private boolean             dragging, onOff, pin, painting, click;
    
    /** Width of the lens. */
    private int                 width;
    
    /** Magnification factor for the lens image. */
    private double              magFactor;
    
    /** Anchor point. */
    private Point               anchor;
    
    /** Color of the lens' border. */
    private Color               c;
    
    private ViewerCtrl          control;
    
    ImageCanvasMng(ImageCanvas view, ViewerCtrl control)
    {
        this.view = view;
        this.control = control;
        width = ViewerUIF.DEFAULT_WIDTH;
        magFactor = ViewerUIF.DEFAULT_MAG;
        drawingArea = new Rectangle();
        onOff = true;
        pin = false;
        painting = false;
        click = false;
        attachListeners();
    }
    
    /** Attach mouse listener. */
    private void attachListeners()
    {
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }

    int getWidth() { return width; }
    
    Color getColor() { return c; }
    
    boolean getPainting() { return painting; }
    
    public void setMagFactor(double f)
    {
        magFactor = f;
        if (view.getLens() != null) {
            view.resetLens();
            view.repaint();
        }
    }
    
    public void setClick(boolean b) { click = b; }
    
    /** Call when the image inspector widget is closed. */
    public void resetDefault(boolean b)
    { 
        click = b;
        view.resetLens();
        view.repaint();
    }
    
    /** Set the width of the lens. */
    public void setWidth(int w)
    {
        width = w;
        view.resetDrawingArea();
        if (view.getLens() != null) {
            view.resetLens();
            view.repaint();
        }
    }
    
    public void setOnOff(boolean b)
    { 
        onOff = b; 
        if (!b && pin) {
            view.resetLens();
            view.repaint(); 
        }
    }
    
    public void setPin(boolean b)
    { 
        pin = b;
        if (view.getLens() != null) {
            view.resetLens();
            view.repaint();
        }
    }
    
    public void setPainting(boolean b, Color c)
    {
        painting = b;
        this.c = c;
        view.resetLens();
        if (onOff && pin && anchor != null) drawLens(anchor);
    }
   
    public void sharpenImage() { view.paintFilterImage(ImageFactory.SHARPEN); }
    
    public void lowPassImage() { view.paintFilterImage(ImageFactory.LOW_PASS); }
    
    public void resetImage() { view.resetImage(); }
    
    void setDrawingArea(int x, int y, int w, int h)
    { 
        drawingArea.setBounds(x+width/2, y+width/2, w-width, h-width);
    }
    
    /** Handle Mouse pressed event. */
    public void mousePressed(MouseEvent e)
    {
        if (e.getClickCount() == 1 && click) {
            Point p = new Point(e.getPoint());
            view.resetLens();
            if (!dragging && onOff && drawingArea.contains(p)) {
                dragging = true;
                drawLens(p);
            }
        } else if (e.getClickCount() == 2 && !click) control.showInspector();
    }

    /** Handle Mouse dragged event. */
    public void mouseDragged(MouseEvent e)
    {
        Point p = new Point(e.getPoint());
        if (dragging && onOff && drawingArea.contains(p)) drawLens(p); 
    }
    
    /** Handle Mouse released event. */
    public void mouseReleased(MouseEvent e)
    { 
        dragging = false;
        if (onOff && !pin) {
            view.resetLens();
            view.repaint();  
        }
    }
    
    /** Forward event to the view. */
    private void drawLens(Point p)
    {
        anchor = p;
        view.paintLensImage(magFactor, p, width, painting, c);
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

    /** 
     * Required by I/F but not actually needed in our case,
     * no op implementation.
     */   
    public void mouseMoved(MouseEvent e) {}
    
}
