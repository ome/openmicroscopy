/*
 * org.openmicroscopy.shoola.util.ui.JLabelButton 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * A label behaving like a button.
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
public class JLabelButton 
	extends JLabel
{
	
	/** Bound property indicating that the label has been selected. */
	public static final String SELECTED_PROPERTY = "selected";
	
	/** The action id associated to the label. */
	private long actionID;
	
	/** Installs the default listeners. */
	private void installDefaultListeners()
	{
		addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				onMouseReleased();
			}
		});
	}
	
	/** Fires a property the mouse is released. */
	private void onMouseReleased()
	{
		firePropertyChange(SELECTED_PROPERTY, actionID-1, actionID);
	}
	
	/**
	 * Creates a default instance.
	 * 
	 * @param text The text to set.
	 */
	public JLabelButton(String text)
	{
		super(text);
		setBorder(null);
		actionID = -1;
		installDefaultListeners();
	}

	/**
	 * Sets the action ID.
	 * 
	 * @param actionID The value to set.
	 */
	public void setActionID(long actionID)
	{
		this.actionID = actionID;
	}
	
}
