/*
 * org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive
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
 * Specifies a node or other object that is susceptible to clicks.  If the
 * action is local and non-atomic (i.e., relies on a sequence of events that
 * rely on internal state), then it is best to override these methods within
 * the implementing class.  Otherwise, you can take advantage of action
 * reusability with the get/set action classes. 
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface MouseDownSensitive
{
    /**
     * Returns the current set of mouse event-action bindings.
     * @return See above.
     */
    public MouseDownActions getMouseDownActions();
    
    /**
     * Sets the set of mouse event-action bindings to the specified
     * MouseDownActions set.
     * @param actions See above.
     */
    public void setMouseDownActions(MouseDownActions actions);
    
    /**
     * Respond to the mouse press.
     * @param event The event parameters.
     */
    public void respondMousePress(PInputEvent event);
    
    /**
     * Respond to the mouse release.
     * @param event The event parameters.
     */
    public void respondMouseRelease(PInputEvent event);
    
    /**
     * Respond to the mouse click.
     * @param event The event parameters.
     */
    public void respondMouseClick(PInputEvent event);
}
