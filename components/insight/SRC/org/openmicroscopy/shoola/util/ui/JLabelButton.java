/*
 * org.openmicroscopy.shoola.util.ui.JLabelButton 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
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
	
	/** Indicates if the 'hover' effect (mouse cursor change) is enabled */
	private boolean hover = false;
	
	/** Installs the default listeners. */
	private void installDefaultListeners()
	{
		addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				onMouseReleased();
			}
			
                        public void mouseEntered(MouseEvent e) {
                            if(hover)
                                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }
            
                        public void mouseExited(MouseEvent e) {
                            if(hover)
                                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
                this(text, false);
        }
        
	/**
	 * Creates a default instance.
	 * 
	 * @param text The text to set.
	 * @param hover If <code>true</code> changes the cursor to a hand
         *  cursor when above the label
	 */
	public JLabelButton(String text, boolean hover)
	{
		super(text);
		setBorder(null);
		actionID = -1;
		this.hover = hover;
		installDefaultListeners();
	}

	/**
         * Creates a default instance.
         * 
         * @param icon The icon to set.
         */
        public JLabelButton(Icon icon)
        {
                this(icon, false);
        }
        
	/**
         * Creates a default instance.
         * 
         * @param icon The icon to set.
         * @param hover If <code>true</code> changes the cursor to a hand
         *  cursor when above the label
         */
        public JLabelButton(Icon icon, boolean hover)
        {
                super(icon);
                setBorder(null);
                actionID = -1;
                this.hover = hover;
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
