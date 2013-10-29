/*
 * org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays icons in the combobox.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class JComboBoxImageRenderer
	extends DefaultListCellRenderer
{

    /** The tool tips to set.*/
    private List<String> tooltips;
    
	/**
	 * Creates a new instance.
	 */
	public JComboBoxImageRenderer()
	{
	}
	
	/**
	 * Populates the renderer with the tooltips provided
	 * 
	 * @param tooltips The value to set.
	 */
	public void setTooltips(List<String> tooltips)
	{
	    this.tooltips = tooltips;
	}
    
	/**
	 * Overridden to set icon and text.
	 * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel label = (JLabel) super.getListCellRendererComponent(list,
			value, index, isSelected, cellHasFocus);

		if (index > -1 && value != null && tooltips != null
                && tooltips.size() > index) {
            list.setToolTipText(tooltips.get(index));
        }
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		String txt = "";
		Icon icon = null;
		if (value instanceof ImageIcon) {
			icon = (ImageIcon) value;
			txt = ((ImageIcon) value).getDescription();
		} else if (value instanceof JComboBoxImageObject) {
			JComboBoxImageObject object = (JComboBoxImageObject) value;
			icon = object.getIcon();
			txt = object.getText();
		}
		label.setIcon(icon);
		label.setText(txt);
		int h = label.getFontMetrics(label.getFont()).getHeight();
		int max = h;
		if (icon != null) {
			
			max = Math.max(h, icon.getIconHeight())+4;
		}
		Dimension d = label.getPreferredSize();
		label.setPreferredSize(new Dimension(d.width, max));
		return label;
	}
	
}
