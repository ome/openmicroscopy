/*
 * org.openmicroscopy.shoola.agents.browser.images.ResponsiveNode
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
package org.openmicroscopy.shoola.agents.browser.images;

import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Defines a node that listens for UI input.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class ResponsiveNode extends PNode
                                     implements MouseDownSensitive,
                                                MouseOverSensitive,
                                                MouseDragSensitive
{
    protected MouseDownActions mouseDownActions;
    protected MouseOverActions mouseOverActions;
    protected MouseDragActions mouseDragActions;
    
    public ResponsiveNode()
    {
        mouseDownActions = new MouseDownActions();
        mouseOverActions = new MouseOverActions();
        mouseDragActions = new MouseDragActions();
    }
    
    public MouseDownActions getMouseDownActions()
    {
        return mouseDownActions;
    }
    
    public MouseOverActions getMouseOverActions()
    {
        return mouseOverActions;
    }
    
    public MouseDragActions getMouseDragActions()
    {
        return mouseDragActions;
    }
    
    public void respondMouseClick(PInputEvent event)
    {
        PiccoloAction action =
            mouseDownActions.getMouseClickAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondMouseDoubleClick(PInputEvent event)
    {
        // TODO is this wrong?
        PiccoloAction action =
            mouseDownActions.getMouseClickAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondMousePress(PInputEvent event)
    {
        PiccoloAction action =
            mouseDownActions.getMousePressAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondMouseRelease(PInputEvent event)
    {
        PiccoloAction action =
            mouseDownActions.getMouseReleaseAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondMouseEnter(PInputEvent event)
    {
        PiccoloAction action =
            mouseOverActions.getMouseEnterAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondMouseExit(PInputEvent event)
    {
        PiccoloAction action =
            mouseOverActions.getMouseExitAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondStartDrag(PInputEvent event)
    {
        PiccoloAction action =
            mouseDragActions.getStartDragAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondDrag(PInputEvent event)
    {
        PiccoloAction action =
            mouseDragActions.getDragAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void respondEndDrag(PInputEvent event)
    {
        PiccoloAction action =
            mouseDragActions.getEndDragAction(PiccoloModifiers.getModifier(event));
        action.execute(event);
    }
    
    public void setMouseDownActions(MouseDownActions actions)
    {
        if(actions != null)
        {
            this.mouseDownActions = actions;
        }
    }
    
    public void setMouseOverActions(MouseOverActions actions)
    {
        if(actions != null)
        {
            this.mouseOverActions = actions;
        }
    }
    
    public void setMouseDragActions(MouseDragActions actions)
    {
        if(actions != null)
        {
            this.mouseDragActions = actions;
        }
    }
    
    
    
}
