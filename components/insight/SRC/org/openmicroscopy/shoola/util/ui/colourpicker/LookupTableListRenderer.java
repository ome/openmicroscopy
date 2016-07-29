/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourMenuUI
 *
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
package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Simple DefaultListCellRenderer which returns a {@link JSeparator} to show the
 * {@link LookupTableItem#SEPARATOR}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LookupTableListRenderer extends JLabel implements ListCellRenderer {

    /** Create the colouricon which will hold the colours. */
    private static ColourIcon icon = new ColourIcon(32, 24);

    /** Border colour of the cell when the icon is selected. */
    private Border lineBorder = BorderFactory.createLineBorder(Color.gray, 1);

    /** Border colour of the cell when the icon is not selected. */
    private Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        
        if (!(value instanceof LookupTableItem))
            return defaultRenderer.getListCellRendererComponent(list, value,
                    index, isSelected, cellHasFocus);
        
        setOpaque(true);
        
        LookupTableItem item = (LookupTableItem) value;

        if (value == LookupTableItem.SEPARATOR)
            return new JSeparator(JSeparator.HORIZONTAL);

        setForeground( isSelected ? list.getSelectionForeground() : list.getForeground());
        setBackground( isSelected ? list.getSelectionBackground() : list.getBackground());
        setBorder(cellHasFocus ? lineBorder : emptyBorder);
        
        if (item.hasLookupTable()) {
            setIcon(null);
            setText(item.getLabel());
        } else {
            Color newCol = new Color(item.getColor().getRed(),
                    item.getColor().getGreen(), item.getColor().getBlue());

            icon.setColour(newCol);
            setIcon(icon);
            setVerticalAlignment(SwingConstants.CENTER);
            setIconTextGap(40);
            setText(item.getLabel());
        }

        return this;
    }
}
