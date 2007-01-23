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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.border.SeparatorBorder;

/** 
 * This class will render the server icon and name in the list
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
	private static final Border	LINE_BORDER = 
								BorderFactory.createLineBorder(Color.GRAY);
	
	/** Border colour of the cell when the icon is not selected. */
	private static final Border	SEPARATOR_BORDER = new SeparatorBorder();
	
	/** Distance between the icon and the text. */
	private static final int	GAP = 10;

	/** The vertical space added to the preferred size. */
	private static final int	VERTICAL = 4;
	
	/** 
	 * Creates a new instance. 
	 * Sets the background to opaque. 
	 */
	ServerListRenderer() { setOpaque(true); }
	
	/** 
     * Overridden to display the icon and the server name.
	 * @see ListCellRenderer#getListCellRendererComponent(JList, Object, int, 
     *                                                  boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus) 
	{
		Object [] array = (Object[]) value;
		Icon icon = (Icon) array[0];
		setIcon(icon);
		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setIconTextGap(GAP);
		setText((String) array[1]);
		if (icon != null)
			setPreferredSize(new Dimension(getWidth(), 
							icon.getIconHeight()+VERTICAL));
		if (isSelected) {
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		if (cellHasFocus) setBorder(LINE_BORDER);
		else setBorder(SEPARATOR_BORDER);
		return this;
	}

}
