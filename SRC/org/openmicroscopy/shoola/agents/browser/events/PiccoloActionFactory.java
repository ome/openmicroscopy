/*
 * org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory
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

import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModeClass;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Factory classes for commonly used browser actions that require some form
 * of composition to work properly.  Which is like, all of them.  Whooooops.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PiccoloActionFactory
{
    /**
     * Generates a mode change action.
     * @param target The browser model to change.
     * @param mode The mode to change the model to.
     * @return The action which executes this, which can be applied to any
     *         node.
     */
    public static PiccoloAction getModeChangeAction(final BrowserModel target,
                                                    final String familyName,
                                                    final BrowserMode mode)
    {
        if(target == null || mode == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.setCurrentMode(familyName,mode);
            }

        };
        return action;
    }
}
