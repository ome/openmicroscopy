 /*
 * org.openmicroscopy.shoola.agents.editor.uiComponents.DropDownMenu 
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

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;

/** 
 * This is a lite alternative to a {@link JComboBox} that appears as a text
 * label with a small icon to the right. Based on a {@link JButton}, that 
 * launches a {@link CustomPopupMenu} to display the items in a list. 
 * When an item is selected, the 
 * {@link #firePropertyChange(String, Object, Object)} method is called, using 
 * the {@link #SELECTION} property. 
 * Therefore, listeners to this {@link DropDownMenu} should be 
 * {@link PropertyChangeListener}s with this property, rather than 
 * {@link ActionListener}s. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DropDownMenu 
	extends CustomButton
	implements PropertyChangeListener {
	
	/** A pop-up menu to display the options */
	private CustomPopupMenu 		popupMenu;
	
	/** A list of the String items to be displayed in the popUp menu */
	private String[] 				options;
	
	/** The property associated with a change in the selected item */
	public static final String 		SELECTION = "itemSelection";
	
	/** The index of the selected item within the options list */
	private int 					selectedIndex;
	
	/**
	 * Creates an instance, builds the UI etc. 
	 * 
	 * @param options		The list of options to display in the drop-down menu
	 */
	public DropDownMenu(String[] options) 
	{
		Icon upDownIcon = IconManager.getInstance().getIcon(IconManager.UP_DOWN_9_12);
		setIcon(upDownIcon);
		setHorizontalTextPosition(SwingConstants.LEFT);
		setIconTextGap(2);
		
		this.options = options;
		
		addMouseListener(new PopupListener());
		
		popupMenu = new CustomPopupMenu(options);
		popupMenu.addPropertyChangeListener(this);
	}
	
	/**
	 * Sets the selected item, if it is within the list of {@link #options}
	 * Also sets the {@link #selectedIndex} accordingly. 
	 * 
	 * @param item	The new item
	 */
	public void setSelectedItem(String item) {
		
		if (item == null)	return;
		
		for (int i=0; i<options.length; i++) {
			if (item.equals(options[i])) {
				setText(item);
				selectedIndex = i;
				return;
			}
		}
	}
	
	/**
	 * Sets the selected index, if within the range of the {@link #options} list
	 * 
	 * @param i		The new index
	 */
	public void setSelectedIndex(int i) {
		if ((i >= 0 ) && (i < options.length)) {
			setText(options[i]);
			selectedIndex = i;
		}
	}
	
	/**
	 * Returns the index of the currently selected item. 
	 * 
	 * @return	see above
	 */
	public int getSelectedIndex() 
	{
		return selectedIndex;
	}

	/**
	 * A MouseAdapter that shows the {@link #popupMenu} when the button
	 * is clicked. 
	 * 
	 * @author will
	 *
	 */
	private class PopupListener extends MouseAdapter 
	{
	    public void mouseClicked(MouseEvent e) {
	    	// pop-up is positioned aligned with top left of button
	    	popupMenu.show(e.getComponent(), 0, 0);   
	    }
	}

	/**
	 *  A listener applied to the Popup Menu, to respond to item selection. 
	 *  Delegates handling of the selection by calling 
	 *  {@link #firePropertyChange(String, Object, Object)} method is called, using 
	 *  the {@link #SELECTION} property.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (CustomPopupMenu.ITEM_NAME.equals(evt.getPropertyName())) {
			String oldValue = getText();
			String newValue = evt.getNewValue().toString();
			setSelectedItem(newValue);
	
			firePropertyChange(SELECTION, oldValue, newValue);
		}
	}
}
