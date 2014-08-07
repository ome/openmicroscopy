/*
 * org.openmicroscopy.shoola.util.ui.tdialog.ScreenControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.tdialog;




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
 * (<b>Internal version:</b> $Revision: 4695 $ $Date: 2006-12-15 17:08:05 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
class ScreenControl
{
    
    /** 
     * The x-coordinate of mouse in absolute coordinate system 
     * when the mouse is pressed.
     */
    private int             xAbs;
    
    /** 
     * The y-coordinate of mouse in absolute coordinate system 
     * when the mouse is pressed.
     */
    private int 			yAbs;
    
    /** 
     * The y-coordinate of mouse in source view's coordinate system 
     * when the mouse is pressed.
     */
    private int             xView;
    
    /** 
     * The y-coordinate of mouse in source view's coordinate system 
     * when the mouse is pressed.
     */
    private int             yView;
    
    /** Dragging control. */
    private boolean         dragging;

    /** The controller. */
    private DialogControl   controller;
    
    /** 
     * Sets the mouse cursor. 
     * 
     * @param c The cursor to set.
     */
    private void setCursor(Cursor c)
    {
        controller.getModel().getRootPane().setCursor(c);
    }
    
    /** 
     * Sets the location of the {@link TinyDialog window}. 
     * 
     * @param e The mouse event.
     */
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
    
    /**
     * Creates a new instance.
     * @param controller    Back pointer to the main Controller.   
     *                      Mustn't be <code>null</code>.
     */
    ScreenControl(DialogControl controller)
    {
        if (controller == null)
            throw new IllegalArgumentException("No control.");
        this.controller = controller;
        dragging = false;
    }
    
    /** 
     * Handles the <code>moussePressed</code> event. 
     * 
     * @param e The event to handle.
     */
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
    
    /** 
     * Handles the <code>mouseReleased</code> event. 
     * 
     * @param e The event to handle.
     */
    void mouseReleased(MouseEvent e)
    {
        if (!dragging) return;
        moveFrame(e);
        setCursor(Cursor.getDefaultCursor());
        dragging = false;
    }
    
    /** 
     * Handles the <code>mouseDragged</code> event. 
     * 
     * @param e The event to handle.
     */
    void mouseDragged(MouseEvent e)
    {
        if (!dragging) return;
        //Don't allow moving of frames left mouse button was not used.
        if (((e.getModifiers() & 
           InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        moveFrame(e);
    }
    
}

