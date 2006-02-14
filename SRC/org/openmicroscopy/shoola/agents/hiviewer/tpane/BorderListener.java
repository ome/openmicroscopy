/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.BorderListener
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

package org.openmicroscopy.shoola.agents.hiviewer.tpane;





//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

//Third-party libraries

//Application-internal dependencies

/** 
 * Listens to mouse motions and inputs events to move and resize the
 * {@link TinyPane}. The {@link TinyPane} will behave like a 
 * {@link javax.swing.JInternalFrame}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BorderListener
    extends MouseInputAdapter
    implements SwingConstants
{

    /** Identifies the resizing zone. */
    private static final int RESIZE_NONE  = 0;
    
    /** Flag to discard the release action. */
    private boolean                 discardRelease;
       
    /** Value added to resize the corner of the frame. */
    private int                     resizeCornerSize = 16;
    
    /** Reference to the desktopManager. */
    private static DesktopManager   sharedDesktopManager;
    
    /** Flag to control the dragging events. */
    private boolean                 dragging;
    
    /** The mousePressed location in absolute coordinate system. */
    private int                     _x, _y;
    
    /** The mousePressed location in source view's coordinate system. */
    private int                     __x, __y;
    
    /** The starting rectangle. */
    private Rectangle               startingBounds;
    
    /** The bounds of the parent of the {@link TinyPane} frame. */
    private Rectangle               parentBounds;
    
    /** The direction of a move. */
    private int                     resizeDir;
    
    /** The Model this listener is for. */
    private TinyPane                frame;
    
    /**
     * Creates a {@link DesktopManager}. 
     * 
     * @return See above.
     */
    protected DesktopManager getDesktopManager()
    {
        if (sharedDesktopManager == null)
              sharedDesktopManager = new DefaultDesktopManager();
        return sharedDesktopManager;  
    }
    
    /**
     * Creates a new instance.
     * 
     * @param frame Reference to the {@link TinyPane model}. Mustn't be 
     *              <code>null</code>.
     */
    BorderListener(TinyPane frame)
    {
        if (frame == null) throw new NullPointerException("No frame.");
        this.frame = frame;
        if (frame.getParent() != null) 
          parentBounds = frame.getParent().getBounds();
    }
  
    /**
     * Handles <code>mouseReleased</code> event. 
     * @see MouseInputAdapter#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
        if (discardRelease) {
            discardRelease = false;
            return;
        }
        if (resizeDir == RESIZE_NONE) {
            getDesktopManager().endDraggingFrame(frame);    
            dragging = false;
        } else {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getDesktopManager().endResizingFrame(frame);
        }
        _x = 0;
        _y = 0;
        __x = 0;
        __y = 0;
        startingBounds = null;
        resizeDir = RESIZE_NONE;
    }
     
    /**
     * Handles <code>mousePressed</code> event.
     * @see MouseInputAdapter#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        Point p = SwingUtilities.convertPoint((Component)e.getSource(), 
                    e.getX(), e.getY(), null);
        __x = e.getX();
        __y = e.getY();
        _x = p.x;
        _y = p.y;
        startingBounds = frame.getBounds();
        resizeDir = RESIZE_NONE;
        
        Insets i = frame.getInsets();
        Point ep = new Point(__x, __y);
        JComponent titleBar = frame.getTitleBar();
        if (e.getSource() == frame.getTitleBar()) {
            Point np = titleBar.getLocation();
            ep.x += np.x;
            ep.y += np.y;
        }

        if (e.getSource() == titleBar) {
            if (ep.x > i.left && ep.y > i.top && ep.x < 
                    frame.getWidth()-i.right) {
                getDesktopManager().beginDraggingFrame(frame);
                dragging = true;
                return;
            }
        }
        if (!frame.isResizable()) return;
        if (e.getSource() == frame || e.getSource() == titleBar) {
            if (ep.x <= i.left) {
                if (ep.y < resizeCornerSize + i.top) resizeDir = NORTH_WEST;
                else if (ep.y > frame.getHeight()-resizeCornerSize-i.bottom)
                    resizeDir = SOUTH_WEST;
                else resizeDir = WEST;
            } else if (ep.x >= frame.getWidth()-i.right) {
                if (ep.y < resizeCornerSize+i.top) resizeDir = NORTH_EAST;
                else if (ep.y > frame.getHeight()-resizeCornerSize-i.bottom)
                    resizeDir = SOUTH_EAST;
                else resizeDir = EAST;
            } else if (ep.y <= i.top) {
                if (ep.x < resizeCornerSize+i.left) resizeDir = NORTH_WEST;
                else if (ep.x > frame.getWidth()-resizeCornerSize-i.right)
                    resizeDir = NORTH_EAST;
                else resizeDir = NORTH;
            } else if (ep.y >= frame.getHeight()-i.bottom) {
                if (ep.x < resizeCornerSize+i.left) resizeDir = SOUTH_WEST;
                else if (ep.x > frame.getWidth()-resizeCornerSize-i.right)
                    resizeDir = SOUTH_EAST;
                else resizeDir = SOUTH;
            } else {
                /* the mouse press happened inside the frame, not in the 
                 * border */
                discardRelease = true;
                return;
            }
    
            getDesktopManager().beginResizingFrame(frame, resizeDir);
            
            return;
        }
    }

    /** 
     * Handles <code>mouseDragged</code> event.
     * @see MouseInputAdapter#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent e)
    {   
        if (startingBounds == null) return;
                                 
        Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                                                e.getX(), e.getY(), null);
        int deltaX = _x-p.x;
        int deltaY = _y-p.y;
        Dimension min = frame.getMinimumSize();
        Dimension max = frame.getMaximumSize();
        int newX, newY, newW, newH;
        Insets i = frame.getInsets();
    
        // Handle a MOVE 
        if (dragging) {
            //Don't allow moving of frames if left mouse button was not used.
            if ((e.getModifiers() & 
                 InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
                return;
            
            int pWidth, pHeight;
            Dimension s = frame.getParent().getSize();
            pWidth = s.width;
            pHeight = s.height;
            newX = startingBounds.x-deltaX;
            newY = startingBounds.y-deltaY;

            // Make sure we stay in-bounds
            if (newX+i.left <= -__x)   newX = -__x-i.left+1;
            if (newY+i.top <= -__y) newY = -__y - i.top + 1;
            if (newX+__x+i.right >= pWidth) 
                newX = pWidth-__x-i.right-1;
            if (newY+__y+i.bottom >= pHeight)
                newY = pHeight-__y-i.bottom-1;
            //frame.setCursor(new Cursor(Cursor.HAND_CURSOR));
            getDesktopManager().dragFrame(frame, newX, newY);
            return;
        }

        if (!frame.isResizable()) return;

        newX = frame.getX();
        newY = frame.getY();
        newW = frame.getWidth();
        newH = frame.getHeight();

        parentBounds = frame.getParent().getBounds();
        switch (resizeDir) {
            case RESIZE_NONE:   return;
            case NORTH:      
                if (startingBounds.height+deltaY < min.height)
                    deltaY = min.height-startingBounds.height;
                else if (startingBounds.height+deltaY > max.height)
                    deltaY = max.height-startingBounds.height;
                if (startingBounds.y-deltaY < 0) deltaY = startingBounds.y;
                newX = startingBounds.x;
                newY = startingBounds.y-deltaY;
                newW = startingBounds.width;
                newH = startingBounds.height+deltaY;
            break;
            case NORTH_EAST:     
                if (startingBounds.height+deltaY < min.height)
                    deltaY = min.height-startingBounds.height;
                else if (startingBounds.height+deltaY > max.height)
                    deltaY = max.height-startingBounds.height;
                if (startingBounds.y-deltaY < 0) deltaY = startingBounds.y;

                if (startingBounds.width-deltaX < min.width)
                    deltaX = startingBounds.width-min.width;
                else if (startingBounds.width-deltaX > max.width)
                    deltaX = -(max.width-startingBounds.width);
                if (startingBounds.x+startingBounds.width-deltaX >
                        parentBounds.width) 
                    deltaX = startingBounds.x+startingBounds.width-
                            parentBounds.width;
                newX = startingBounds.x;
                newY = startingBounds.y - deltaY;
                newW = startingBounds.width-deltaX;
                newH = startingBounds.height+deltaY;
                break;
            case EAST:      
                if (startingBounds.width-deltaX < min.width)
                    deltaX = startingBounds.width-min.width;
                else if(startingBounds.width - deltaX > max.width)
                    deltaX = startingBounds.width-max.width;
                if (startingBounds.x+startingBounds.width-deltaX >
                        parentBounds.width)
                    deltaX = startingBounds.x+startingBounds.width -
                            parentBounds.width;
                newW = startingBounds.width-deltaX;
                newH = startingBounds.height;
                break;
            case SOUTH_EAST:     
                if (startingBounds.width-deltaX < min.width)
                    deltaX = startingBounds.width-min.width;
                else if (startingBounds.width-deltaX > max.width)
                    deltaX = startingBounds.width-max.width;
                if (startingBounds.x+startingBounds.width-deltaX >
                        parentBounds.width)
                    deltaX = startingBounds.x+startingBounds.width-
                            parentBounds.width;
                if (startingBounds.height-deltaY < min.height)
                    deltaY = startingBounds.height-min.height;
                else if (startingBounds.height-deltaY > max.height)
                    deltaY = -(max.height-startingBounds.height);
                if (startingBounds.y+startingBounds.height-deltaY >
                        parentBounds.height) 
                    deltaY = startingBounds.y+startingBounds.height-
                            parentBounds.height ;   
                newW = startingBounds.width-deltaX;
                newH = startingBounds.height-deltaY;
                break;
            case SOUTH:      
                if (startingBounds.height-deltaY < min.height)
                    deltaY = startingBounds.height-min.height;
                else if (startingBounds.height-deltaY > max.height)
                    deltaY = -(max.height-startingBounds.height);
                if (startingBounds.y+startingBounds.height-deltaY >
                        parentBounds.height)
                    deltaY = startingBounds.y+startingBounds.height-
                            parentBounds.height ;
                newW = startingBounds.width;
                newH = startingBounds.height-deltaY;
                break;
            case SOUTH_WEST:
                if (startingBounds.height-deltaY < min.height)
                    deltaY = startingBounds.height - min.height;
                else if (startingBounds.height-deltaY > max.height)
                    deltaY = -(max.height-startingBounds.height);
                if (startingBounds.y+startingBounds.height-deltaY >
                        parentBounds.height)
                    deltaY = startingBounds.y+startingBounds.height-
                                parentBounds.height ;
                if (startingBounds.width+deltaX < min.width)
                    deltaX = -(startingBounds.width-min.width);
                else if (startingBounds.width+deltaX > max.width)
                    deltaX = max.width-startingBounds.width;
                if (startingBounds.x-deltaX < 0) deltaX = startingBounds.x;

                newX = startingBounds.x-deltaX;
                newY = startingBounds.y;
                newW = startingBounds.width+deltaX;
                newH = startingBounds.height-deltaY;
                break;
            case WEST:      
                if (startingBounds.width+deltaX < min.width)
                    deltaX = -(startingBounds.width-min.width);
                else if (startingBounds.width+deltaX > max.width)
                    deltaX = max.width-startingBounds.width;
                if (startingBounds.x-deltaX < 0) deltaX = startingBounds.x;
                
                newX = startingBounds.x-deltaX;
                newY = startingBounds.y;
                newW = startingBounds.width+deltaX;
                newH = startingBounds.height;
                break;
            case NORTH_WEST:     
                if (startingBounds.width+deltaX < min.width)
                    deltaX = -(startingBounds.width-min.width);
                else if (startingBounds.width+deltaX > max.width)
                    deltaX = max.width-startingBounds.width;
                if (startingBounds.x-deltaX < 0) deltaX = startingBounds.x;

                if (startingBounds.height+deltaY < min.height)
                    deltaY = -(startingBounds.height-min.height);
                else if (startingBounds.height+deltaY > max.height)
                    deltaY = max.height-startingBounds.height;
                if (startingBounds.y-deltaY < 0) deltaY = startingBounds.y;

                newX = startingBounds.x-deltaX;
                newY = startingBounds.y-deltaY;
                newW = startingBounds.width+deltaX;
                newH = startingBounds.height+deltaY;
                break;
            default: return;
        }
        getDesktopManager().resizeFrame(frame, newX, newY, newW, newH);
    }

    /** 
     * Handles <code>mouseMoved</code> event. 
     * @see MouseInputAdapter#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent e)   
    {
        if (!frame.isResizable()) return;
    
        JComponent titleBar = frame.getTitleBar();
        if (e.getSource() == frame || e.getSource() == titleBar) {
            Point ep = new Point(e.getX(), e.getY());
            if (e.getSource() == titleBar) {
                Point np = titleBar.getLocation();
                ep.x += np.x;
                ep.y += np.y;
            }
        }
    }

    /**
     * Required but no-op implementation in our case.
     * @see MouseInputAdapter#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent e) {}
    
    /**
     * Required but no-op implementation in our case.
     * @see MouseInputAdapter#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {}
    
}

