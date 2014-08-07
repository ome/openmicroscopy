/*
 * org.openmicroscopy.shoola.util.ui.tdialog.BorderListener
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * We no longer use the usual decoration of a <code>JDialog</code> so we need to 
 * provide our own border listener.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
class BorderListener
    implements MouseMotionListener, MouseListener
{

    /** The default border thickness. */
    private static int THICKNESS = 10;
    
    /** Reference to the model. */
    private TinyDialog  model;

    /** 
     * Sets the bounds of the model.
     *
     * @param bounds The bounds to set.
     */
    private void setModelBounds(Rectangle bounds)
    {
        model.setBounds(bounds);
        model.setRestoreSize(new Dimension(bounds.width, bounds.height));
        model.validate();
    }

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    BorderListener(TinyDialog  model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }

    /** Sets the cursor to be the hand cursor.*/
    private void enter()
    {
        model.getRootPane().setCursor(
                Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
    }

    /** Sets the cursor to be the default cursor.*/
    private void exit()
    {
        model.getRootPane().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Handles mouseDragged events. 
     * @see MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent e)
    {
        enter();
        model.getRootPane().setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Rectangle bounds = model.getBounds();
        Point p = e.getPoint();
        Dimension min = model.getMinimumSize();
        Dimension max = model.getMaximumSize();
        if (p.x < THICKNESS) {
            model.invalidate();
            SwingUtilities.convertPointToScreen(p, model.getContentPane());
            int diff = bounds.x-p.x;
            bounds.x = p.x;
            bounds.width += diff;
            if (bounds.width > min.width && bounds.width < max.width)
                setModelBounds(bounds);
        } else if (e.getX() > (model.getWidth()-THICKNESS)) {
            model.invalidate();
            bounds.width = p.x;
            if (bounds.width > min.width && bounds.width < max.width)
                setModelBounds(bounds);
        } else if (e.getY() < THICKNESS) {
            model.invalidate();
            SwingUtilities.convertPointToScreen(p, model.getContentPane());
            int diff = bounds.y-p.y;
            bounds.y = p.y;
            bounds.height += diff;
            if (bounds.height < max.height && bounds.height > min.height)
                setModelBounds(bounds);
        } else if (e.getY() > (model.getHeight()-THICKNESS)) {
            model.invalidate();
            bounds.height = p.y;
            if (bounds.height < max.height && bounds.height > min.height)
                setModelBounds(bounds);
        }
    }

    /**
     * Modifies the cursor
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent e) { enter(); }

    /**
     * Modifies the cursor
     * @see MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseExited(MouseEvent e) { exit(); }

    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see MouseMotionListener#mouseMoved(MouseEvent)
     */  
    public void mouseMoved(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see MouseListener#mouseReleased(MouseEvent)
     */  
    public void mouseReleased(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {}

    /** 
     * Required by {@link MouseListener} I/F but not actually needed in
     * our case, no op implementation.
     * @see MouseListener#mouseReleased(MouseEvent)
     */
    public void mousePressed(MouseEvent e) {}

}
