/*
 * org.openmicroscopy.shoola.agents.browser.images.OverlayNode
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
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Simplified version of overlay nodes that have to respond to UI input
 * (such as mouse overs, mouse downs)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class OverlayNode extends PNode
                                          implements MouseDownSensitive,
                                                     MouseOverSensitive
{
    protected MouseDownActions mouseDownActions;
    protected MouseOverActions mouseOverActions;
    protected String overlayNodeType;
    protected Thumbnail parentNode;
    
    /**
     * Default constructor
     * @param type The type to mark this node as.
     * @param parentNode The parent thumbnail of the node.
     * @throws IllegalArgumentException If either parameter is null.
     */
    public OverlayNode(String type, Thumbnail parentNode)
        throws IllegalArgumentException
    {
        mouseDownActions = new MouseDownActions();
        mouseOverActions = new MouseOverActions();
        
        if(type == null || parentNode == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        overlayNodeType = type;
        this.parentNode = parentNode;
    }
    
    public String getOverlayType()
    {
        return overlayNodeType;
    }
    
    public void setOverlayType(String type)
    {
        if(type != null)
        {
            this.overlayNodeType = type;
        }
    }
    
    public Thumbnail getParentNode()
    {
        return parentNode;
    }
    
    public void setParentNode(Thumbnail parent)
    {
        if(parent != null)
        {
            this.parentNode = parent;
        }
    }
    
    public MouseDownActions getMouseDownActions()
    {
        return mouseDownActions;
    }
    
    public MouseOverActions getMouseOverActions()
    {
        return mouseOverActions;
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
}
