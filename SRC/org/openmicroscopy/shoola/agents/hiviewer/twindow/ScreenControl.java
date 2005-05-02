/*
 * org.openmicroscopy.shoola.agents.hiviewer.twindow.ScreenControl
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

package org.openmicroscopy.shoola.agents.hiviewer.twindow;




//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Control to handle mouse events.
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
class ScreenControl
{
    
    /** MousePressed location in absolute coordinate system .*/
    private int             xAbs, yAbs;
    
    /** MousePressed location in source view's coordinate system .*/
    private int             xView, yView;

    /** Dragging control. */
    private boolean         dragging;
    
    private Cursor          initialCursor;
    
    /** The controller. */
    private WindowControl   controller;
    
    ScreenControl(WindowControl controller)
    {
        this.controller = controller;
        dragging = false;
        initialCursor = controller.getModel().getRootPane().getCursor();
    }
    
    /** Handles the moussePressed event. */
    void mousePressed(MouseEvent e)
    {
        Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                e.getX(), e.getY(), null); 
        xView = e.getX();
        yView = e.getY();
        xAbs = p.x;
        yAbs = p.y;
        Insets i = controller.getModel().getInsets();
        Point ep = new Point(xView, yView);
        if (ep.x > i.left && ep.y > i.top && ep.x < 
                controller.getModel().getWidth()-i.right) {
            dragging = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
    }
    
    /** Handles the mouseReleased event. */
    void  mouseReleased(MouseEvent e)
    {
        if (dragging) {
            moveFrame(e);
            setCursor(initialCursor);
            dragging = false;
        }
    }
    
    /** Handles the mouseDragged event. */
    void mouseDragged(MouseEvent e)
    {
        if (!dragging) return;
        //Don't allow moving of frames left mouse button was not used.
        if (((e.getModifiers() & 
           InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        moveFrame(e);
    }
    
    /** Set the mouse cursor. */
    private void setCursor(Cursor c)
    {
        controller.getModel().getRootPane().setCursor(c);
    }
    
    /** Set the location of the {@link TinyWindow window}. */
    private void moveFrame(MouseEvent e)
    {
        Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                e.getX(), e.getY(), null); 
        int deltaX = xAbs-p.x;
        int deltaY = yAbs-p.y;
        Rectangle r = controller.getModel().getBounds();
        int newX = r.x-deltaX;
        int newY = r.y-deltaY;
        controller.getModel().setLocation(newX, newY);
    }
    
}

