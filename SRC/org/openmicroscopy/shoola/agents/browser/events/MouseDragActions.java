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

/**
 * Specifies a set mouse drag-to-action bindings.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class MouseDragActions
{
    // the bound startDragAction
    private PiccoloAction startDragAction;
    
    // the bound whileDragAction
    private PiccoloAction whileDragAction;
    
    // the bound endDragAction
    private PiccoloAction endDragAction;
    
    /**
     * Initializes the sets with all mouse events bound to
     * a NOOP action.
     */
    public MouseDragActions()
    {
        startDragAction = PiccoloAction.PNOOP_ACTION;
        whileDragAction = PiccoloAction.PNOOP_ACTION;
        endDragAction = PiccoloAction.PNOOP_ACTION;
    }
    
    /**
     * Returns the action bound to an end drag event.
     * 
     * @return See above.
     */
    public PiccoloAction getEndDragAction()
    {
        return endDragAction;
    }

    /**
     * Returns the action bound to a start drag event.
     * 
     * @return See above.
     */
    public PiccoloAction getStartDragAction()
    {
        return startDragAction;
    }

    /**
     * Returns the action bound to a dragging event.
     * 
     * @return See above.
     */
    public PiccoloAction getWhileDragAction()
    {
        return whileDragAction;
    }

    /**
     * Sets the action bound to an end drag event to the
     * specified action.  If the action is NULL, the bound end
     * drag action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setEndDragAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            endDragAction = action;
        }
    }

    /**
     * Sets the action bound to a start drag event to the
     * specified action.  If the action is NULL, the bound start
     * drag action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setStartDragAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            startDragAction = action;
        }
    }

    /**
     * Sets the action bound to a dragging event to the
     * specified action.  If the action is NULL, the bound 
     * dragging action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setWhileDragAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            whileDragAction = action;
        } 
    }

}
