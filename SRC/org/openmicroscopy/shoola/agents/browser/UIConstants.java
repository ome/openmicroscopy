/*
 * org.openmicroscopy.shoola.agents.browser.UIConstants
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

import java.awt.Color;
import java.awt.Font;

/**
 * Specifies UI constants for the browser.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public interface UIConstants
{
    /**
     * The default background color for the entire browser.
     */
    public static Color BUI_BACKGROUND_COLOR = new Color(224, 224, 224);

    /**
     * The default background color for each group/region.
     */
    public static Color BUI_GROUPAREA_COLOR = new Color(224, 224, 224);

    /**
     * The default foreground/text color for each group/region.
     */
    public static Color BUI_GROUPTEXT_COLOR = new Color(51, 51, 51);

    /**
     * The default text font for the group/region to display the group name.
     */
    public static Font BUI_GROUPTEXT_FONT = new Font(null, Font.BOLD, 14);
}
