/*
 * org.openmicroscopy.shoola.agents.browser.events.ModularEventHandler
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.events;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class ModularEventHandler
{
    private Set handlerListeners;

    // not sure if these handlers are the way to go yet but we'll see
    // also TODO: make dummy (do-nothing) event handlers to replace null
    private PBasicInputEventHandler mouseDragHandler = null;
    private PBasicInputEventHandler mouseOverHandler = null;
    private PBasicInputEventHandler mouseSelectHandler = null;
    private PBasicInputEventHandler keyPressHandler = null;
    private PBasicInputEventHandler mouseWheelHandler = null;

    /**
     * Creates a modular event handler (that can be shared among multiple
     * objects).  Awesome.
     *
     */
    public ModularEventHandler()
    {
        handlerListeners = new HashSet();
    }

    /**
     * Add a listener to this handler, so it can get events whenever this
     * handler changes behavior.
     * 
     * @param listener The listener to add.
     */
    public void addChangeListener(MEHChangeListener listener)
    {
        if (listener == null)
        {
            return;
        }
        handlerListeners.add(listener);
    }

    /**
     * Removes a listener from this handler.
     * 
     * @param listener The listener to remove.
     */
    public void removeChangeListener(MEHChangeListener listener)
    {
        if (listener == null)
        {
            return;
        }
        handlerListeners.add(listener);
    }

    /**
     * Returns the current key press handler.
     * @return The key press handler.
     */
    public PBasicInputEventHandler getKeyPressHandler()
    {
        return keyPressHandler;
    }

    /**
     * Returns the current mouse drag handler.
     * @return The mouse drag handler.
     */
    public PBasicInputEventHandler getMouseDragHandler()
    {
        return mouseDragHandler;
    }

    /**
     * Returns the current mouse over handler.
     * @return The mouse over handler.
     */
    public PBasicInputEventHandler getMouseOverHandler()
    {
        return mouseOverHandler;
    }

    /**
     * Returns the current mouse select handler.
     * @return The mouse select handler.
     */
    public PBasicInputEventHandler getMouseSelectHandler()
    {
        return mouseSelectHandler;
    }

    /**
     * Returns the current mouse wheel handler.
     * @return The mouse wheel handler.
     */
    public PBasicInputEventHandler getMouseWheelHandler()
    {
        return mouseWheelHandler;
    }

    /**
     * Sets the current key press handler to the specified value and
     * notifies the listeners that a change has occurred.  Specifying the
     * handler to be null will set the handler to a NOOP handler.
     * @param handler The new key press event handler.
     */
    public void setKeyPressHandler(PBasicInputEventHandler handler)
    {
        // TODO: add null -> NOOP clause
        keyPressHandler = handler;
        for (Iterator iter = handlerListeners.iterator(); iter.hasNext();)
        {
            MEHChangeListener listener = (MEHChangeListener) iter.next();
            listener.eventListenerChanged(MEHChangeListener.KEY_CHANGE);
        }
    }

    /**
     * Sets the current mouse drag handler to the specified value and
     * notifies the listeners that a change has occurred.  Specifying the
     * handler to be null will set the handler to a NOOP handler.
     * @param handler The new mouse drag event handler.
     */
    public void setMouseDragHandler(PBasicInputEventHandler handler)
    {
        // TODO: add null -> NOOP clause
        mouseDragHandler = handler;
        for (Iterator iter = handlerListeners.iterator(); iter.hasNext();)
        {
            MEHChangeListener listener = (MEHChangeListener) iter.next();
            listener.eventListenerChanged(MEHChangeListener.DRAG_CHANGE);
        }
    }

    /**
     * Sets the current mouse over handler to the specified value and
     * notifies the listeners that a change has occurred.  Specifying the
     * handler to be null will set the handler to a NOOP handler.
     * @param handler The new mouse over event handler.
     */
    public void setMouseOverHandler(PBasicInputEventHandler handler)
    {
        mouseOverHandler = handler;
        for (Iterator iter = handlerListeners.iterator(); iter.hasNext();)
        {
            MEHChangeListener listener = (MEHChangeListener) iter.next();
            listener.eventListenerChanged(MEHChangeListener.OVER_CHANGE);
        }
    }

    /**
     * Sets the current mouse drag handler to the specified value and
     * notifies the listeners that a change has occurred.  Specifying the
     * handler to be null will set the handler to a NOOP handler.
     * @param handler The new mouse select event handler.
     */
    public void setMouseSelectHandler(PBasicInputEventHandler handler)
    {
        keyPressHandler = handler;
        for (Iterator iter = handlerListeners.iterator(); iter.hasNext();)
        {
            MEHChangeListener listener = (MEHChangeListener) iter.next();
            listener.eventListenerChanged(MEHChangeListener.SELECT_CHANGE);
        }
    }

    /**
     * Sets the current mouse wheel handler to the specified value and
     * notifies the listeners that a change has occurred.  Specifying the
     * handler to be null will set the handler to a NOOP handler.
     * @param handler THe new mouse wheel event handler.
     */
    public void setMouseWheelHandler(PBasicInputEventHandler handler)
    {
        mouseWheelHandler = handler;
        for (Iterator iter = handlerListeners.iterator(); iter.hasNext();)
        {
            MEHChangeListener listener = (MEHChangeListener) iter.next();
            listener.eventListenerChanged(MEHChangeListener.WHEEL_CHANGE);
        }
    }

}
