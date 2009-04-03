/*
 * measurement.component.ui.ChannelComboBoxRenderer 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.util;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelField;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ChannelComboBoxRenderer
	extends JLabel  
	implements ListCellRenderer  
{
	
	/** Create the colouricon which will hold the colours. */
	private static ColourIcon icon = new ColourIcon(64, 12);
	
	/** Border colour of the cell when the icon is selected. */
	private Border lineBorder = BorderFactory.createLineBorder(Color.gray, 1);
	
	/** Border colour of the cell when the icon is not selected. */
	private Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		
	/**
	 * Creates a new instance.
     * Sets the background to opaque.
	 */
	ChannelComboBoxRenderer()
	{
		super();
		setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
		setOpaque(true);
	}
	
	/** 
     * Overridden method
	 * @see ListCellRenderer#getListCellRendererComponent(JList, Object, int, 
     *                                                  boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean isSelected, boolean hasFocus) 
	{
		ChannelField data = (ChannelField)value;
		Color newCol = data.channelColour;
		
		icon.setColour(newCol);
		setIcon(icon);
		this.setIconTextGap(20);
		setText(data.channelWavelength);
		if (isSelected)
		{
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		}
		else
		{
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		
		if (hasFocus) setBorder(lineBorder);
		else setBorder(emptyBorder);
		this.setPreferredSize(new Dimension(130,30));
		this.setMinimumSize(new Dimension(130,30));
		this.setMaximumSize(new Dimension(130,30));
		return this;
	}

}



