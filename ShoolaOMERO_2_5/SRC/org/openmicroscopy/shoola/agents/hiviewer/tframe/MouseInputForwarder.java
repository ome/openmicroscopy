/*
 * org.openmicroscopy.shoola.agents.hiviewer.tframe.MouseInputForwarder
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

package org.openmicroscopy.shoola.agents.hiviewer.tframe;


//Java imports
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class to forward mouse events that happened within a component to
 * another component.
 * <p>This class should only be used if a source component sits on top of a 
 * target component to which events should be forwarded.  In fact, events
 * happening in the source components are retargeted before forwarding &#151;
 * the point is translated into the target's coordinate system.</p> 
 * <p>This class is used by the {@link TitleBar} to have the title and icon
 * components pass events on to the underlying title bar component.  This
 * way clients of the {@link TinyFrame} can easily attach listeners to the
 * title bar and get notified of mouse events that, to clients, belong to
 * the title bar &#151; the title and icon cover most part of the bar, but
 * have no function, they logically belong to the bar's background.</p>
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
class MouseInputForwarder
    implements MouseInputListener
{

    /** The component that originates the events. */
    private Component   source;
    
    /** The component to which events should be forwarded. */
    private Component   target;
    
    
    /**
     * Forwards the original event.
     * This method clones the original event <code>me</code> to translate
     * the coordinates relative to the {@link #target}.
     * 
     * @param me
     */
    private void forward(MouseEvent me)
    {
        Point p = SwingUtilities.convertPoint(source, 
                                              me.getX(), me.getY(),
                                              target);
        MouseEvent retargeted = new MouseEvent(target, me.getID(),
                                    me.getWhen(),
                                    me.getModifiers() | me.getModifiersEx(),
                                    p.x, p.y,
                                    me.getClickCount(),
                                    me.isPopupTrigger());
        target.dispatchEvent(retargeted);
    }
    
    /**
     * Creates a new instance to forward mouse events within <code>source</code>
     * to <code>target</code>.
     * 
     * @param source The component that originates the events.
     *               Musnt't be <code>null</code>.
     * @param target The component to which events should be forwarded.
     *               Musnt't be <code>null</code>.
     */
    MouseInputForwarder(Component source, Component target)
    {
        if (source == null) throw new NullPointerException("No source.");
        if (target == null) throw new NullPointerException("No target.");
        this.source = source;
        this.target = target;
        source.addMouseListener(this);
        source.addMouseMotionListener(this);
    }
    
    /**
     * Forwards the event.
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) { forward(me); }

    /**
     * Forwards the event.
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent me) { forward(me); }

    /**
     * Forwards the event.
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent me) { forward(me); }

    /**
     * Forwards the event.
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) { forward(me); }

    /**
     * Forwards the event.
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) { forward(me); }
   
    /**
     * Forwards the event.
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me) { forward(me); }

    /**
     * Forwards the event.
     * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent me) { forward(me); }

}
