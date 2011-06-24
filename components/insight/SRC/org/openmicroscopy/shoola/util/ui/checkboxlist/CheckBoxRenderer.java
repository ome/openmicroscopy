/*
* org.openmicroscopy.shoola.util.ui.checkboxlist.CheckBoxRenderer
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
*  This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.ui.checkboxlist;

//Java imports
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */

public class CheckBoxRenderer	 
	extends JComponent 
	implements TableCellRenderer
{
	
	/** The font size of the label. */
	static final int FONTSIZE = 10;
		
	/** 
	 * Set the renderer for the checkbox list.
	 */
	public CheckBoxRenderer() 
	{
		setOpaque(false);		
	}

	/**
	 * Override.
	 * @see TableCellRenderer#getTableCellRendererComponent(
	 * JTable, Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component thisComponent;
		if (column == 0)
		{
			JLabel label = new JLabel();
			label.setText(value+"");
			label.setOpaque(true);
			label.setFont(label.getFont().deriveFont(FONTSIZE));
			thisComponent = label;
		}
		else
		{
			JCheckBox checkBox = new JCheckBox();
			checkBox.setSelected((Boolean) value);
			checkBox.setBackground(UIUtilities.BACKGROUND_COLOR);
			thisComponent = checkBox;
		}
		if (row % 2 == 0)
			thisComponent.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		else
			thisComponent.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
		return thisComponent;
	}
}
