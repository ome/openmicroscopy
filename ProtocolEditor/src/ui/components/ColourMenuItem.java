/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


package ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

public class ColourMenuItem extends JMenuItem implements MouseListener {

	public ColourMenuItem(String text) {
		super (text);
		
		// remove the default mouseListeners, since they cause unwanted mouseOver effects
		MouseListener[] mouseListeners = this.getMouseListeners();
		for (int i=0; i<mouseListeners.length; i++){
			removeMouseListener(mouseListeners[i]);
		}
		// add my own mouseListener to process the mouseClicked event.
		this.addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		JComponent parent = (JComponent)getParent();
		//parent.setSelected(this);
		parent.setVisible(false);
		this.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ActionCommand"));
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {	
	}

	public void mousePressed(MouseEvent e) {	
	}

	public void mouseReleased(MouseEvent e) {
	}
	
	
}
