/*
 * org.openmicroscopy.shoola.agents.browser.events.PiccoloAction
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
 * Specifies an action that occurs within the Piccolo environment.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class PiccoloAction implements BrowserAction
{
    /**
     * Specifies that no action should be taken.
     */
    public static PiccoloAction PNOOP_ACTION = new PiccoloAction()
    {
        public void execute()
        {
            // do nothing
        }
        
        public void execute(PInputEvent e)
        {
            // call execute unless overridden
            execute();
        }

    };
    
    /**
     * Executes the action, independent of a PInputEvent.  Should be
     * overridden if to be used.
     */
    public void execute() {}
    
    /**
     * Executes the action, using the PInputEvent to transport node and
     * state information.  Should be overridden if it is to be used.
     * 
     * @param e The event and associated metadata.
     */
    public void execute(PInputEvent e) {}
}
