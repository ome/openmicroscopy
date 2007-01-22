/*
 * org.openmicroscopy.shoola.env.ui.ServerListRenderer 
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
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies

/** 
 * ColourListRenderer will render the server icons and server names in the list
 * box.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ServerListRenderer 
	extends JLabel  
	implements ListCellRenderer 
{

	/** Border colour of the cell when the icon is selected. */
	private Border lineBorder = BorderFactory.createLineBorder(Color.gray, 1);
	
	/** Border colour of the cell when the icon is not selected. */
	private Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	
	/** 
	 * Creates a new instance. 
	 * Sets the background to opaque. 
	 */
	ServerListRenderer() { setOpaque(true); }
	
	/** 
     * Overridden to display icon and server name.
	 * @see ListCellRenderer#getListCellRendererComponent(JList, Object, int, 
     *                                                  boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus) 
	{
		Object [] array = (Object[]) value;
		setIcon((Icon) array[0]);
		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setIconTextGap(10);
		setText((String) array[1]);
		if (isSelected) {
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		
		if (cellHasFocus) setBorder(lineBorder);
		else setBorder(emptyBorder);
		return this;
	}

}
