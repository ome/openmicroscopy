/*
 * uiComponents.CustomButton 
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies

/** 
* A combination of JButton and JPopupMenu, that are linked so that the 
* button pops up the meun.
* This class extends JButton.
* Constructor takes a list of actions, and an icon for the button.
* 
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class PopupMenuButton 
	extends CustomButton
{
	
	/**
	 * A popup menu, displayed when the button is clicked. 
	 */
	private JPopupMenu popupMenu;

	/**
	 * Creates an instance and builds the UI (links button to popup menu)
	 * 
	 * @param toolTipText   A tool-tip-text for the Button	
	 * @param icon			The icon for the Button
	 * @param actions		The list of Actions to be displayed in pop-up menu
	 */
	public PopupMenuButton(String toolTipText, Icon icon, Action[] actions)
	{
		super(icon);
		setToolTipText(toolTipText);
		
		popupMenu = new JPopupMenu();
		
		
		for (int i=0; i<actions.length; i++) {
			JMenuItem menuItem = new JMenuItem(actions[i]);
			popupMenu.add(menuItem);
		}
		
		
		this.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
	}
}
