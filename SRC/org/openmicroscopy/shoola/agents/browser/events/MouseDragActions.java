/*
 * org.openmicroscopy.shoola.agents.browser.events.MouseDragActions
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

import java.util.HashMap;
import java.util.Map;

/**
 * Specifies a set mouse press-to-action bindings.
 * 
 * I highly recommend using PiccoloModifiers to look up the modifiers,
 * although this is internally consistent with the modifier integers in
 * java.awt.event.InputEvent.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class MouseDragActions
{
    private Map startDragModifierMap;
    private Map dragModifierMap;
    private Map endDragModifierMap;
    
    private final Integer normalInteger = new Integer(PiccoloModifiers.NORMAL);
    
    /**
     * Initializes the sets with all mouse events at all modifiers bound to
     * a NOOP action.
     */
    public MouseDragActions()
    {
        startDragModifierMap = new HashMap();
        setAction(PiccoloAction.PNOOP_ACTION,startDragModifierMap,
                  PiccoloModifiers.NORMAL);
        dragModifierMap = new HashMap();
        setAction(PiccoloAction.PNOOP_ACTION,dragModifierMap,
                  PiccoloModifiers.NORMAL);
        endDragModifierMap = new HashMap();
        setAction(PiccoloAction.PNOOP_ACTION,endDragModifierMap,
                  PiccoloModifiers.NORMAL);
    }
      
    /**
     * Returns the action bound to a dragging event with the specified
     * modifier.
     * 
     * @return See above.
     */
    public PiccoloAction getDragAction(int modifier)
    {
        return getAction(dragModifierMap,modifier);
    }

    /**
     * Returns the action bound to a drag start event, with the specified
     * modifier.
     * 
     * @return See above.
     */
    public PiccoloAction getStartDragAction(int modifier)
    {
        return getAction(startDragModifierMap,modifier);
    }

    /**
     * Returns the action bound to a drag end event, with the specified
     * modifier.
     * 
     * @return See above.
     */
    public PiccoloAction getEndDragAction(int modifier)
    {
        return getAction(endDragModifierMap,modifier);
    }

    /**
     * Sets the action bound to a dragging event to the
     * specified action and modifier.  If the action is NULL, the bound mouse
     * click action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setDragAction(int modifier, PiccoloAction action)
    {
        setAction(action,dragModifierMap,modifier);
    }

    /**
     * Sets the action bound to a start drag event to the
     * specified action and modifier.  If the action is NULL, the bound mouse
     * press action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setStartDragAction(int modifier, PiccoloAction action)
    {
        setAction(action,startDragModifierMap,modifier);
    }

    /**
     * Sets the action bound to an end drag event to the
     * specified action and modifier.  If the action is NULL, the bound mouse
     * release action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setEndDragAction(int modifier, PiccoloAction action)
    {
        setAction(action,endDragModifierMap,modifier);
    }
    
    /*
     * shortcut method for all actions.
     * 
     * @param whichMap The map to draw from.
     * @param modifier The modifier to index on.
     * @return The action for the selected event type and modifier.  If there
     *         is no explicit event mapped to that modifier, it will return
     *         the default (no modifier) event.
     */
    private PiccoloAction getAction(Map whichMap, int modifier)
    {
        Integer modInt = new Integer(modifier);
        if(whichMap.containsKey(modInt))
        {
            return (PiccoloAction)whichMap.get(modInt);
        }
        else
        {
            return (PiccoloAction)whichMap.get(normalInteger);
        }
    }
    
    /*
     * Binds an action to an event type and modifier.
     * 
     * @param whichMap The event type map to update.
     * @param action The action to bind.
     * @param modifier The modifier to index on.
     */
    private void setAction(PiccoloAction action, Map whichMap, int modifier)
    {
        Integer modInt = new Integer(modifier);
        if(action != null)
        {
            whichMap.put(modInt,action);
        }
        else
        {
            whichMap.put(modInt,PiccoloAction.PNOOP_ACTION);
        }
    }

}
