/*
 * org.openmicroscopy.shoola.agents.util.ui.PermissionMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies

/** 
 * Menu displaying 3 options: 
 *  - All X
 *  - X added by me
 *  - X added by others
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class PermissionMenu 
	extends JPopupMenu
	implements ActionListener
{

	/**
	 * The term used to remove links.
	 */
	public static final String REMOVE = "Remove";
	
	/**
	 * The term used to remove links.
	 */
	public static final String DELETE = "Delete";
	
	/**
	 * Bound property indicating the selected level.
	 */
	public static String SELECTED_LEVEL_PROPERTY = "selectedLevel";
	
	/** Identifies <code>all</code> the objects.*/
	public static final int ALL = 0;
	
	/** Identifies the objects added by current user.*/
	public static final int ME = 1;
	
	/** Identifies the objects added by others.*/
	public static final int OTHER = 2;
	
	/** 
	 * Builds the menu.
	 * 
	 * @param action The action to perform.
	 * @param type The type of objects to handle.
	 */
	private void buildMenu(String action, String type)
	{
	        type = type.toLowerCase();
		JMenuItem item = new JMenuItem(action+" all "+type);
		item.addActionListener(this);
		item.setActionCommand(""+ALL);
		add(item);
		item = new JMenuItem(action+" "+type+" added by me");
		item.addActionListener(this);
		item.setActionCommand(""+ME);
		add(item);
		item = new JMenuItem(action+" "+type+" added by others");
		item.addActionListener(this);
		item.setActionCommand(""+OTHER);
		add(item);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param action The action to perform.
	 * @param type The type of objects to handle.
	 */
	public PermissionMenu(String action, String type)
	{
		buildMenu(action, type);
	}

	/** 
	 * Fires a property depending on the selected index.
	 * 
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case ALL:
			case ME:
			case OTHER:
				firePropertyChange(SELECTED_LEVEL_PROPERTY, -1, index);
		}
	}
	
}
