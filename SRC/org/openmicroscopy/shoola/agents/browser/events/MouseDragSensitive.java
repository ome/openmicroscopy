/*
 * org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive
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

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Specifies a node or other class that is sensitive to dragging.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface MouseDragSensitive
{
    /**
     * Return the set of drag event-to-action bindings.
     * @return See above.
     */
    public MouseDragActions getMouseDragActions();
    
    /**
     * Set the set of drag event-to-action bindings to the specified set.
     * @param e See above.
     */
    public void setMouseDragActions(MouseDragActions actions);
    
    /**
     * React to drag start.
     * @param e The parameters of the drag start.
     */
    public void respondStartDrag(PInputEvent e);
    
    /**
     * React to dragging.
     * @param e The parameters of the drag.
     */
    public void respondDrag(PInputEvent e);
    
    /**
     * React to drag end.
     * @param e The parameters of the drag end.
     */
    public void respondEndDrag(PInputEvent e);
}
