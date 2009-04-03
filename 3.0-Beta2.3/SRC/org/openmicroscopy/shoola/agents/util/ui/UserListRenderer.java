/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.UserListRenderer 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;

/** 
 * Renders the list of users.
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
class UserListRenderer 
	extends JLabel 
	implements ListCellRenderer
{

	/** Border of the component. */
	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);
	
	/** Background color of the even rows. */
	private static final Color	BACKGROUND = Color.WHITE;
	
	/** Background color of the add rows. */
	private static final Color	BACKGROUND_ONE = new Color(236, 243, 254);
	
	/** Gap between icon and text. */
	private static final int	GAP = 20;
	
    /** Reference to the icon used to represent a user. */
    private Icon         userIcon;
    
	/** 
	 * Creates a new instance. 
	 * 
	 * @param userIcon The icon used to represent a user.
	 */
	public UserListRenderer(Icon userIcon)
	{
		setOpaque(true);
		this.userIcon = userIcon;
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
    	setIcon(userIcon);
		setIconTextGap(GAP);
    	if (value instanceof String) setText((String) value);
    	else if (value instanceof ExperimenterData) {
    		ExperimenterData data = (ExperimenterData) value;
    		setText(data.getFirstName()+" "+data.getLastName());
    	}
    	if (isSelected) {
    		setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
    	} else {
    		 setForeground(list.getForeground());
    		 if (index%2 == 0) setBackground(BACKGROUND);
             else setBackground(BACKGROUND_ONE);
    	}
    	setBorder(BORDER);
    	return this;
    }
    
}
