/*
 * org.openmicroscopy.shoola.agents.browser.events.BrowserActionFactory
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

import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserManager;
import org.openmicroscopy.shoola.agents.browser.colormap.ColorMapManager;

/**
 * Factory method for non-Piccolo browser actions.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class BrowserActionFactory
{
    /**
     * Instructs the color map to load with the specified group displayed.
     * @param group The group to load.
     * @return A suitable browser action that can be executed.
     */
    public static BrowserAction getLoadColorMapWithGroup(final CategoryGroup group)
    {
        BrowserAction action = new BrowserAction()
        {
            public void execute()
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserManager bManager = env.getBrowserManager();
                ColorMapManager cManager = env.getColorMapManager();
                bManager.showStaticWindow(BrowserManager.COLORMAP_KEY);
                cManager.getUI().fireGroupSelect(group);
            }
        };
        return action;
    }
}
