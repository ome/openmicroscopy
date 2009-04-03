/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapModeListener
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import org.openmicroscopy.shoola.agents.browser.datamodel.DisplayValueMode;

/**
 * Listener interface for changing heat map modes.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface HeatMapModeListener
{
    /**
     * Indicates that the mode changed to the specified mode.
     * @param newMode The new mode.
     */
    public void modeChanged(DisplayValueMode newMode);
    
    /**
     * Indicates that the scale changed to the specified scale.  The listener
     * would be responsible for creating a new scale based on the name.
     * @param newScale The new scale.
     */
    public void scaleChanged(String scaleName);
}
