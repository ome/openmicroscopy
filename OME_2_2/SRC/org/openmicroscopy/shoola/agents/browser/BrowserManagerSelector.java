/*
 * org.openmicroscopy.shoola.agents.browser.BrowserManagerSelector
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
package org.openmicroscopy.shoola.agents.browser;

import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

/**
 * Returns the appropriate browser manager for the application context.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.1
 * @since OME2.2.1
 */
public class BrowserManagerSelector
{
    /**
     * Returns the appropriate browser manager for the application context.
     * (represented by the registry)
     * @return See above.
     */
    public static BrowserManager getInstance(Registry registry)
    {
        if(registry == null)
        {
            return null;
        }
        Environment env = (Environment)registry.lookup(LookupNames.ENV);
        // in taskbar mode.  return the standalone frame browser manager.
        if(env.getTaskbarMode())
        {
            return new BrowserManagerSFImpl(registry);
        }
        // in internal frame mode.  return the internal frame browser manager.
        else return new BrowserManagerIFImpl(registry);
    }
}
