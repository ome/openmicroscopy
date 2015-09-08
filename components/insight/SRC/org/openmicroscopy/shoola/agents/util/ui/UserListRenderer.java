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


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import omero.gateway.model.ExperimenterData;

/** 
 * Renders the list of users.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class UserListRenderer 
	extends JLabel 
	implements ListCellRenderer
{

	/** Border of the component. */
	private static final Border BORDER = new EmptyBorder(5, 5, 5, 5);
	
	/** Background color of the even rows. */
	private static final Color BACKGROUND = Color.WHITE;
	
	/** Background color of the add rows. */
	private static final Color BACKGROUND_ONE = new Color(236, 243, 254);
	
	/** Gap between icon and text. */
	private static final int GAP = 20;
	
    /** Reference to the icon used to represent a user. */
    private Icon userIcon;
    
    /** The font to set when the value is a string.*/
    private Font font;
    
    /** The font to set when the value is a string.*/
    private Font defaultFont;
    
    /** Reference to the model.*/
    private UserManagerDialog model;
    
    /**
     * Returns <code>true</code> if the experimenter is already displayed, 
     * <code>false</code> otherwise.
     * 
     * @param experimenter The value to check.
     * @return See above.
     */
    private boolean isAlreadySelected(ExperimenterData experimenter)
    {
    	long id = experimenter.getId();
    	Iterator<ExperimenterData> i = model.getSelectedUsers().iterator();
    	ExperimenterData exp;
    	while (i.hasNext()) {
			exp = i.next();
			if (exp.getId() == id)
				return true;
		}
    	return false;
    }
    
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model Reference to the model.
	 * @param userIcon The icon used to represent a user.
	 */
	public UserListRenderer(UserManagerDialog model, Icon userIcon)
	{
		setOpaque(true);
		this.model = model;
		this.userIcon = userIcon;
		defaultFont = getFont();
		font = defaultFont.deriveFont(Font.BOLD | Font.ITALIC,
				defaultFont.getSize()-2);
	}
	
	/**
	 * Overridden to set border and color.
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent
	 *      (javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) 
    {
    	setEnabled(true);
    	if (value instanceof String) {
    		setFont(font);
    		setIcon(null);
    		setText((String) value);
    		setForeground(list.getForeground());
			setBackground(BACKGROUND);
    	} else if (value instanceof ExperimenterData) {
    		setVerticalAlignment(SwingConstants.CENTER);
    		setFont(defaultFont);
    		setIcon(userIcon);
    		setIconTextGap(GAP);
    		ExperimenterData data = (ExperimenterData) value;
    		setText(data.getFirstName()+" "+data.getLastName());
    		if (isSelected) {
        		setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
        	} else {
        		 setForeground(list.getForeground());
        		 if (index%2 == 0) setBackground(BACKGROUND);
                 else setBackground(BACKGROUND_ONE);
        	}
    		setEnabled(!isAlreadySelected((ExperimenterData) value));
    	}
    	
    	setBorder(BORDER);
    	return this;
    }
    
}
