/*
 * org.openmicroscopy.shoola.agents.browser.events.MouseDownActions
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
 * Specifies a set mouse press-to-action bindings.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class MouseDownActions
{
    // the bound mousePressAction
    private PiccoloAction mousePressAction;
    
    // the bound mouseReleaseAction
    private PiccoloAction mouseReleaseAction;
    
    // the bound mouseClickAction
    private PiccoloAction mouseClickAction;
    
    /**
     * Initializes the sets with all mouse events bound to
     * a NOOP action.
     */
    public MouseDownActions()
    {
        mousePressAction = PiccoloAction.PNOOP_ACTION;
        mouseReleaseAction = PiccoloAction.PNOOP_ACTION;
        mouseClickAction = PiccoloAction.PNOOP_ACTION;
    }
    
    /**
     * Returns the action bound to a mouse click event.
     * 
     * @return See above.
     */
    public PiccoloAction getMouseClickAction()
    {
        return mouseClickAction;
    }

    /**
     * Returns the action bound to a mouse press event.
     * 
     * @return See above.
     */
    public PiccoloAction getMousePressAction()
    {
        return mousePressAction;
    }

    /**
     * Returns the action bound to a mouse release event.
     * 
     * @return See above.
     */
    public PiccoloAction getMouseReleaseAction()
    {
        return mouseReleaseAction;
    }

    /**
     * Sets the action bound to a mouse click event to the
     * specified action.  If the action is NULL, the bound mouse
     * click action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setMouseClickAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            mouseClickAction = action;
        }
    }

    /**
     * Sets the action bound to a mouse press event to the
     * specified action.  If the action is NULL, the bound mouse
     * press action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setMousePressAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            mousePressAction = action;
        }
    }

    /**
     * Sets the action bound to a mouse release event to the
     * specified action.  If the action is NULL, the bound mouse
     * release action will be PiccoloAction.PNOOP_ACTION.
     * 
     * @param action See above.
     */
    public void setMouseReleaseAction(PiccoloAction action)
    {
        if(action == null)
        {
            action = PiccoloAction.PNOOP_ACTION;
        }
        else
        {
            mouseReleaseAction = action;
        } 
    }

}
