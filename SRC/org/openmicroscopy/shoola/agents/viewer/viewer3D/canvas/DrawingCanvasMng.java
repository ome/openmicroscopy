/*
 * org.openmicroscopy.shoola.agents.viewer3D.canvas.DrawingCanvasMng
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3D;
import org.openmicroscopy.shoola.agents.viewer.viewer3D.Viewer3DManager;

/** 
 * Manager of the drawing {@link DrawingCanvas component}.
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
    
    private Viewer3DManager     control;
    
    private DrawingCanvas       view;
    
    /** Rectangle to control the different drawing areas. */
    private Rectangle           drawingAreaXY, drawingAreaXZ, drawingAreaZY;
    
    /** x-coordinate (= y-coordinate) of the XYImage. */
    private int                 space;
    
    /** Dragging control. */
    private boolean             dragging;
    
    public DrawingCanvasMng(DrawingCanvas view, Viewer3DManager control)
    {
        this.view = view;
        this.control = control;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
        drawingAreaXY = new Rectangle();  
        drawingAreaXZ = new Rectangle();
        drawingAreaZY = new Rectangle();
    }
    
    public void setDrawingAreas(int XYWidth, int XYHeight, int XZHeight)
    {
        space = 2*Viewer3D.SPACE+XZHeight;
        drawingAreaXY.setBounds(space, space, XYWidth, XYHeight);
        drawingAreaZY.setBounds(Viewer3D.SPACE, space, XZHeight, XYHeight);
        drawingAreaXZ.setBounds(space, Viewer3D.SPACE, XYWidth, XZHeight);
    }
    
    /** Handle mouse pressed event. */ 
    public void mousePressed(MouseEvent e)
    {
        Point p = e.getPoint();
        if (!dragging) {
            dragging = true;
            if (drawingAreaXY.contains(p)) drawXY(p);
            if (drawingAreaXZ.contains(p)) drawXZ(p);
            if (drawingAreaZY.contains(p)) drawZY(p);
        }
    }

    /** Handle mouse dragged event. */ 
    public void mouseDragged(MouseEvent e)
    {
        Point p = e.getPoint();
        if (dragging) {
            if (drawingAreaXY.contains(p)) drawXY(p);
            if (drawingAreaXZ.contains(p)) drawXZ(p);
            if (drawingAreaZY.contains(p)) drawZY(p);   
        }
    }
    
    /** Handle mouse moved on the XYarea. */
    private void drawXY(Point p)
    {
        control.onPlaneSelected(p.x-space, p.y-space);
        System.out.println(p);
        view.drawXY(p);
    }
    
    /** Handle mouse moved on the XZarea. */
    private void drawXZ(Point p) 
    {
        control.onXZPlaneSelected(p.x-space, p.y-Viewer3D.SPACE);
        view.drawXZ(p);
    }
    
    /** Handle mouse moved on the ZYarea. */
    private void drawZY(Point p) 
    {
        control.onZYPlaneSelected(p.x-Viewer3D.SPACE, p.y-space);
        view.drawZY(p);
    }
    
    /** 
     * Set the dragging control to <code>false</code> 
     * and erase the shape drawn on the canvas.
     */
    public void mouseReleased(MouseEvent e)
    {
        dragging = false;
        view.erase();
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
