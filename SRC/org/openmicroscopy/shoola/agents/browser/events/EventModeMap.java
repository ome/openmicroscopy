/*
 * org.openmicroscopy.shoola.agents.browser.events.EventModeMap
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

import java.util.*;

import org.openmicroscopy.shoola.agents.browser.BrowserMode;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

/**
 * Maps BrowserModes to input event handlers.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class EventModeMap
{
    // TODO: replace null handler calls w/NOOP handler
    private Map mouseDragMap;
    private Map mouseOverMap;
    private Map mouseSelectMap;
    private Map keyPressMap;
    private Map mouseWheelMap;

    /**
     * Creates an EventModeMap with no set mode/handler mappings.
     */
    public EventModeMap()
    {
        mouseDragMap = new HashMap();
        mouseOverMap = new HashMap();
        mouseSelectMap = new HashMap();
        keyPressMap = new HashMap();
        mouseWheelMap = new HashMap();
    }

    /**
     * Returns the mouse drag handler for the specified mode.
     * @param mode The target browser mode.
     * @return The mouse drag handler for that mode, or null if there is no
     *         such mapping.
     */
    public PBasicInputEventHandler getMouseDragHandler(BrowserMode mode)
    {
        if (mode == null)
        {
            return null;
        }
        return (PBasicInputEventHandler) mouseDragMap.get(mode);
    }

    /**
     * Sets the mouse drag handler for the specified node.
     * @param mode The browser mode to map.
     * @param handler The handler to bind to the mode.
     */
    public void setMouseDragHandler(
        BrowserMode mode,
        PBasicInputEventHandler handler)
    {
        if (mode == null || handler == null)
        {
            return;
        }
        mouseDragMap.put(mode, handler);
    }

    /**
     * Returns the mouse over handler for the specified mode.
     * @param mode The target browser mode.
     * @return The mouse over handler for that mode, or null if there is no
     *         such mapping.
     */
    public PBasicInputEventHandler getMouseOverHandler(BrowserMode mode)
    {
        if (mode == null)
        {
            return null;
        }
        return (PBasicInputEventHandler) mouseOverMap.get(mode);
    }

    /**
     * Sets the mouse over handler for the specified node.
     * @param mode The browser mode to map.
     * @param handler The handler to bind to the mode.
     */
    public void setMouseOverHandler(
        BrowserMode mode,
        PBasicInputEventHandler handler)
    {
        if (mode == null || handler == null)
        {
            return;
        }
        mouseOverMap.put(mode, handler);
    }

    /**
     * Returns the mouse select handler for the specified mode.
     * @param mode The target browser mode.
     * @return The mouse select handler for that mode, or null if there is no
     *         such mapping.
     */
    public PBasicInputEventHandler getMouseSelectHandler(BrowserMode mode)
    {
        if (mode == null)
        {
            return null;
        }
        return (PBasicInputEventHandler) mouseSelectMap.get(mode);
    }

    /**
     * Sets the mouse select handler for the specified node.
     * @param mode The browser mode to map.
     * @param handler The handler to bind to the mode.
     */
    public void setMouseSelectHandler(
        BrowserMode mode,
        PBasicInputEventHandler handler)
    {
        if (mode == null || handler == null)
        {
            return;
        }
        mouseSelectMap.put(mode, handler);
    }

    /**
     * Returns the key press handler for the specified mode.
     * @param mode The target browser mode.
     * @return The key press handler for that mode, or null if there is no
     *         such mapping.
     */
    public PBasicInputEventHandler getKeyPressHandler(BrowserMode mode)
    {
        if (mode == null)
        {
            return null;
        }
        return (PBasicInputEventHandler) keyPressMap.get(mode);
    }

    /**
     * Sets the key press handler for the specified node.
     * @param mode The browser mode to map.
     * @param handler The handler to bind to the mode.
     */
    public void setKeyPressHandler(
        BrowserMode mode,
        PBasicInputEventHandler handler)
    {
        if (mode == null || handler == null)
        {
            return;
        }
        keyPressMap.put(mode, handler);
    }

    /**
     * Returns the mouse wheel handler for the specified mode.
     * @param mode The target browser mode.
     * @return The mouse wheel handler for that mode, or null if there is no
     *         such mapping.
     */
    public PBasicInputEventHandler getMouseWheelHandler(BrowserMode mode)
    {
        if (mode == null)
        {
            return null;
        }
        return (PBasicInputEventHandler) mouseWheelMap.get(mode);
    }

    /**
     * Sets the mouse wheel handler for the specified node.
     * @param mode The browser mode to map.
     * @param handler The handler to bind to the mode.
     */
    public void setMouseWheelHandler(
        BrowserMode mode,
        PBasicInputEventHandler handler)
    {
        if (mode == null || handler == null)
        {
            return;
        }
        mouseWheelMap.put(mode, handler);
    }
}
