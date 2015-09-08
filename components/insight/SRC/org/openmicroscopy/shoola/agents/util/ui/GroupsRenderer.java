/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import omero.gateway.model.GroupData;

/** 
 * Customized list renderer displaying the users groups. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class GroupsRenderer 
	extends JLabel 
	implements ListCellRenderer
{

	/** Creates a new instance. */
	public GroupsRenderer()
	{
		setOpaque(true);
	}
	
	/**
	 * Overridden to set boder and color.
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent
	 *      (javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) 
    {
    	setVerticalAlignment(SwingConstants.CENTER);
    	if (value instanceof String) setText((String) value);
    	else if (value instanceof GroupData)
    		setText(((GroupData) value).getName());
    	if (isSelected) {
    		setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
    	} else {
    		 setForeground(list.getForeground());
    		 setBackground(list.getBackground());
    	}
    	return this;
    }

}
