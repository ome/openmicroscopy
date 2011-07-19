/*
 * org.openmicroscopy.shoola.util.ui.OMEComboBox 
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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * Customized {@link JComboBox}.
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
public class OMEComboBox 
	extends JComboBox
{

	/** The color used for the foreground when the user is editing the value. */
	private Color	editedColor;
	
	/** The default foreground color. */
	private Color	defaultForeground;
	
	/** The default index. */
	private int 	originalIndex;
	
	/**
	 * Updates the <code>foreground</code> color depending on the text entered.
	 */
	private void updateForeground()
	{
		int index = getSelectedIndex();
		if (editedColor != null) {
			if (originalIndex == index) setForeground(defaultForeground);
			else setForeground(editedColor);
		}
	}
	
	/** 
	 * Creates a default instance. 
	 * 
	 * @param values An array of objects to insert into the combo box.
	 */
	public OMEComboBox(Object[] values)
	{
		this(values, null);
	}
	
	/**
	 * Creates a default instance.
	 * 
	 * @param values 		An array of objects to insert into the combo box.
	 * @param editedColor 	The foreground when the value is modified.
	 */
	public OMEComboBox(Object[] values, Color editedColor)
	{
		super(values);
		setEditedColor(editedColor);
		addActionListener(new ActionListener() {
		
			/**
			 * Updates the colors.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				updateForeground();
			}
		
		});
		defaultForeground = getForeground();
		originalIndex = -1;
		setFocusable(false);
	}

	/**
	 * Sets the edited color. 
	 * 
	 * @param editedColor The value to set.
	 */
	public void setEditedColor(Color editedColor)
	{ 
		this.editedColor = editedColor;
	}
	
	/**
	 * Overridden to set the value of the original index.
	 * @see JComboBox#setSelectedItem(Object)
	 */
	public void setSelectedItem(Object value)
	{
		super.setSelectedItem(value);
		if (originalIndex == -1) originalIndex = getSelectedIndex();
	}
	
	/**
	 * Overridden to set the value of the original index.
	 * @see JComboBox#setSelectedIndex(int)
	 */
	public void setSelectedIndex(int index)
	{
		if (originalIndex == -1) originalIndex = index;
		super.setSelectedIndex(index);
	}

	/**
	 * Overridden to set the background and the disabled background.
	 * @see JComboBox#setBackground(Color)
	 */
	public void setBackground(Color c)
	{
		super.setBackground(c);
		UIManager.getDefaults().put("ComboBox.disabledBackground", c);
	}
	
	/**
	 * Overridden to set the background and the disabled background.
	 * @see JComboBox#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		setBackground(getBackground());
		super.setEnabled(enabled);
	}
	
}

