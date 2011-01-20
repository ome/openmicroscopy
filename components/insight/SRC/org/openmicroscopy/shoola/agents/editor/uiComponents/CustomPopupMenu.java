 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.CustomPopupMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a {@link JPopupMenu} that displays a list of 
 * {@link JCheckBoxMenuItem}s so that the selected item can be indicated by
 * a "tick".
 * When an item is selected, {@link #firePropertyChange(String, Object, Object)}
 * is called with the property {@link #ITEM_NAME} and the new item text. 
 * 
 * For example of usage, see {@link DropDownMenu};
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CustomPopupMenu extends JPopupMenu {
	
	/**
	 * The property name associated with a change in the selected item
	 */
	public static final String ITEM_NAME = "itemName";
	
	/**
	 * Creates an instance
	 * 
	 * @param items			A list of the Strings to display in the popup list. 
	 */
	public CustomPopupMenu (String[] items) {
	
		// create an ActionListener to add to each item in the menu
		// calls firePropertyChange when item is selected
		ActionListener itemListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source instanceof JCheckBoxMenuItem) {
					((JCheckBoxMenuItem)source).setSelected(true);
					String itemText = ((JMenuItem)source).getText();
					setSelectedItem(itemText);	// not done automatically
					CustomPopupMenu.this.firePropertyChange(ITEM_NAME, "", itemText);
				}
			}
		};
	
		// Create the list of menu items, add ActionListener and add to list
		JCheckBoxMenuItem menuItem;
		for (int i=0; i<items.length; i++) {
			
			menuItem = new JCheckBoxMenuItem(items[i]);
			menuItem.setFont(new CustomFont());
			menuItem.addActionListener(itemListener);
			add(menuItem);
		}
	}
	
	/**
	 * Sets the currently selected item in the list, which will be displayed
	 * with a "tick". 
	 * Has to manually de-select the other items in the list. 	
	 * 
	 * @param itemText		The text of the selected item. 
	 */
	public void setSelectedItem(String itemText) {
		for (int i=0; i<getComponentCount(); i++) {
			Object component = getComponent(i);
			if (component instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem)component;
				if (item.getText().equals(itemText))
					item.setSelected(true);
				else
					item.setSelected(false);
			}
		}
	}
	
}
