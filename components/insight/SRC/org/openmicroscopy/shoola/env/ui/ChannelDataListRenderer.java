/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import omero.gateway.model.ChannelData;

import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.ColourIcon;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A {@link ListCellRenderer} for {@link ChannelData} items
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ChannelDataListRenderer extends JLabel implements ListCellRenderer {

    /** Reference to the RenderingControl */
    private RenderingControl rnd;

    /**
     * Creates a new instance
     * 
     * @param rnd
     *            Reference to the RenderingControl
     */
    public ChannelDataListRenderer(RenderingControl rnd) {
        this.rnd = rnd;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        if (!(value instanceof ChannelData)) {
            setText("");
            setIcon(null);
            return this;
        }

        ChannelData ch = (ChannelData) value;

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        String lut = rnd.getLookupTable(ch.getIndex());
        Color col = rnd.getRGBA(ch.getIndex());

        ColourIcon icon = new ColourIcon(14, 14);
        if (StringUtils.isNotBlank(lut))
            icon.setLookupTable(lut);
        else
            icon.setColour(col);
        
        setIcon(icon);
        setText(UIUtilities.formatPartialName2(ch.getName(), 10));

        return this;
    }
    
}
