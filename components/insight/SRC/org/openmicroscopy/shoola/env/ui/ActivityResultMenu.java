/*
 * org.openmicroscopy.shoola.env.ui.ActivityResultMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Displays the View and Download options.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ActivityResultMenu 
	extends JPopupMenu
	implements ActionListener
{

	/** Indicates to download the object. */
	private static final int DOWNLOAD = 0;
	
	/** Indicates to view the object. */
	private static final int VIEW = 1;
	
	/** Reference to the activity. */
	private ActivityComponent activity;
	
	/** The result to handle. */
	private Object row;
	
	/** The item indicating to download the file.*/
	private JMenuItem downloadItem;
	
	/** The item indicating to view the file.*/
	private JMenuItem viewItem;
	
	/**
     * Creates a button.
     * 
     * @param text The text of the button.
     * @param actionID The action command id.
     * @return See above.
     */
	private JMenuItem createItem(String text, int actionID)
    {
		JMenuItem b = new JMenuItem(text);
    	Font f = b.getFont();
    	b.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		b.setActionCommand(""+actionID);
		b.addActionListener(this);
		//b.setOpaque(false);
		b.setForeground(UIUtilities.HYPERLINK_COLOR);
		//UIUtilities.unifiedButtonLookAndFeel(b);
		return b;
    }
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		downloadItem = createItem("Download", DOWNLOAD);
		viewItem = createItem("View", VIEW);
		add(downloadItem);
		add(viewItem);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param row The object to display.
	 * @param activity The activity of reference.
	 */
	ActivityResultMenu(Object row, ActivityComponent activity)
	{
		this.activity = activity;
		this.row = row;
		buildGUI();
	}
	
	/**
	 * Views or downloads the returned value.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DOWNLOAD:
				downloadItem.setEnabled(false);
				activity.download("", row);
				break;
			case VIEW:
				viewItem.setEnabled(false);
				activity.view(row, viewItem);
		}
	}
	
}
