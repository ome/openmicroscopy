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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.env.config.AddOnInfo;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;

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

    /** Bound property indicating that the item is selected.*/
    public static final String ADD_ON_SELECTION_PROPERTY;

    static {
        ADD_ON_SELECTION_PROPERTY = "addOnSelection";
    }
    /** The add-on object this item is hosting.*/
    private AddOnInfo info;

    /** The script if any.*/
    private String script;

    /** The application relate to that menu item.*/
    private ApplicationData data;

    /** Fires a property indicating the selection of the menu.*/
    private void selected()
    {
        firePropertyChange(ADD_ON_SELECTION_PROPERTY, null, this);
    }

    /**
     * Creates a new instance.
     *
     * @param info The add-on this item is hosting
     */
    public AddOnMenuItem(AddOnInfo info)
    {
        this(info, null);
        addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                selected();
            }
        });
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

    /**
     * Sets the application linked to that item.
     *
     * @param data The data.
     */
    public void setApplicationData(ApplicationData data)
    {
        this.data = data;
    }

    /**
     * Returns the application data if registered or <code>null</code>.
     *
     * @return See above.
     */
    public ApplicationData getApplicationData() { return data; }

    /**
     * Returns the name of the add-on.
     *
     * @return See above.
     */
    public String getAddOnName() { return info.getName(); }

    /**
     * Returns the script if any.
     *
     * @return See above.
     */
    public String getScript() { return script; }

}
