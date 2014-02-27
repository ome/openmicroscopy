/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.plugins.util;


//Java imports
import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.env.config.AddOnInfo;

/**
 * Menu item hosting an add-on element.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class AddOnMenuItem
    extends JMenuItem
{

    /** The add-on object this item is hosting.*/
    private AddOnInfo info;

    /** The script if any.*/
    private String script;

    /**
     * Creates a new instance.
     *
     * @param info The add-on this item is hosting
     */
    public AddOnMenuItem(AddOnInfo info)
    {
        this(info, null);
    }

    /**
     * Creates a new instance.
     *
     * @param info The add-on this item is hosting.
     * @param script The name of the script
     */
    public AddOnMenuItem(AddOnInfo info, String script)
    {
        this.info = info;
        this.script = script;
        if (StringUtils.isBlank(script)) setText(info.getName());
        else setText(script);
        setToolTipText(info.getDescription());
    }

}
