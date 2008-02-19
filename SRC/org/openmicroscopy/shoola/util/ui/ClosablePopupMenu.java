/*
 * org.openmicroscopy.shoola.util.ui.ClosablePopupMenu 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies


/** 
 * Helper menu used to close all tabs.
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
class ClosablePopupMenu
	extends JPopupMenu
	implements ActionListener
{

	/** Action ID to close the selected tabbed pane. */
	private static final int CLOSE = 0;
	
	/** Action ID to close all tabbed panes. */
	private static final int CLOSE_ALL = 1;
	
	/** Action ID to close all tabbed panes excepted the selected one. */
	private static final int CLOSE_OTHERS = 2;
	
	/** The owner of the menu. */
	private ClosableTabbedPane tabPane;
	
	/** Initializes and builds menu. */
	private void initialize()
	{
		JMenuItem item = new JMenuItem("Close");
		item.addActionListener(this);
		item.setActionCommand(""+CLOSE);
		add(item);
		item = new JMenuItem("Close Others");
		item.addActionListener(this);
		item.setActionCommand(""+CLOSE_OTHERS);
		add(item);
		item = new JMenuItem("Close All");
		item.addActionListener(this);
		item.setActionCommand(""+CLOSE_ALL);
		add(item);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param tabPane 	Reference to the tabbed pane. 
	 * 					Mustn't be <code>null</code>.
	 */
	ClosablePopupMenu(ClosableTabbedPane tabPane)
	{
		if (tabPane == null)
			throw new IllegalArgumentException("No tabbed pane.");
		this.tabPane = tabPane;
		initialize();
	}
	
	/**
	 * Closes the relevant components.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				tabPane.removeTabAt(tabPane.getSelectedIndex());
				break;
			case CLOSE_ALL:
				tabPane.removeAll();
				break;
			case CLOSE_OTHERS:
				tabPane.removeOthers();
				break;
		}
	}
	
}
